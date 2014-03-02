package com.bendude56.goldenapple.lock;

import java.lang.reflect.InvocationTargetException;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Deque;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;

import org.bukkit.Location;
import org.bukkit.Material;

import com.bendude56.goldenapple.GoldenApple;
import com.bendude56.goldenapple.User;
import com.bendude56.goldenapple.lock.LockedBlock.LockLevel;
import com.bendude56.goldenapple.lock.LockedBlock.RegisteredBlock;
import com.bendude56.goldenapple.permissions.IPermissionUser;

public class SimpleLockManager extends LockManager {
	static {
		LockedBlock.registerBlock("GA_CHEST", GoldenApple.getInstance(), Material.CHEST, LockedChest.class);
		LockedBlock.registerBlock("GA_CHEST", GoldenApple.getInstance(), Material.TRAPPED_CHEST, LockedChest.class);
		LockedBlock.registerBlock("GA_DOOR", GoldenApple.getInstance(), Material.WOODEN_DOOR, LockedDoor.class);
		LockedBlock.registerBlock("GA_FENCEGATE", GoldenApple.getInstance(), Material.FENCE_GATE, LockedFenceGate.class);
		LockedBlock.registerBlock("GA_TRAPDOOR", GoldenApple.getInstance(), Material.TRAP_DOOR, LockedTrapDoor.class);
		LockedBlock.registerBlock("GA_FURNACE", GoldenApple.getInstance(), Material.FURNACE, LockedFurnace.class);
		LockedBlock.registerBlock("GA_FURNACE", GoldenApple.getInstance(), Material.BURNING_FURNACE, LockedFurnace.class);
		LockedBlock.registerBlock("GA_REDSTONE", GoldenApple.getInstance(), Material.LEVER, LockedRedstoneTrigger.class);
		LockedBlock.registerBlock("GA_REDSTONE", GoldenApple.getInstance(), Material.STONE_BUTTON, LockedRedstoneTrigger.class);
		LockedBlock.registerBlock("GA_HOPPER", GoldenApple.getInstance(), Material.HOPPER, LockedHopper.class);
		LockedBlock.registerBlock("GA_DISPENSER", GoldenApple.getInstance(), Material.DISPENSER, LockedDispenser.class);
		LockedBlock.registerBlock("GA_DISPENSER", GoldenApple.getInstance(), Material.DROPPER, LockedDispenser.class);
		LockedBlock.registerBlock("GA_UTILITYBLOCK", GoldenApple.getInstance(), Material.ENCHANTMENT_TABLE, LockedUtilityBlock.class);
		LockedBlock.registerBlock("GA_UTILITYBLOCK", GoldenApple.getInstance(), Material.ANVIL, LockedUtilityBlock.class);
		LockedBlock.registerBlock("GA_UTILITYBLOCK", GoldenApple.getInstance(), Material.BEACON, LockedUtilityBlock.class);
		LockedBlock.registerBlock("GA_UTILITYBLOCK", GoldenApple.getInstance(), Material.JUKEBOX, LockedUtilityBlock.class);
		LockedBlock.registerBlock("GA_BREWINGSTAND", GoldenApple.getInstance(), Material.BREWING_STAND, LockedBrewingStand.class);
		LockedBlock.registerCorrector(DoubleChestLocationCorrector.class);
		LockedBlock.registerCorrector(DoorLocationCorrector.class);
	}
	
	private HashMap<Long, LockedBlock>	lockCache;
	private Deque<Long>					cacheOut;
	private int							cacheSize;
	private List<User>                  overriding;

	public SimpleLockManager() {
		lockCache = new HashMap<Long, LockedBlock>();
		cacheOut = new ArrayDeque<Long>();
		cacheSize = GoldenApple.getInstanceMainConfig().getInt("modules.lock.cacheSize", 100);
		if (cacheSize < 3)
			cacheSize = 3;
		overriding = new ArrayList<User>();

		GoldenApple.getInstanceDatabaseManager().createOrUpdateTable("locks");
		GoldenApple.getInstanceDatabaseManager().createOrUpdateTable("lockusers");
		GoldenApple.getInstanceDatabaseManager().createOrUpdateTable("lockgroups");
	}

	private LockedBlock checkCache(long id) {
		if (lockCache.containsKey(id)) {
			return lockCache.get(id);
		} else {
			return null;
		}
	}

	private LockedBlock checkCache(Location l) {
		for (Map.Entry<Long, LockedBlock> b : lockCache.entrySet()) {
			if (b.getValue().getLocation().equals(l)) {
				return b.getValue();
			}
		}
		return null;
	}

	private LockedBlock loadIntoCache(ResultSet r) throws SQLException {
		RegisteredBlock lockClass = LockedBlock.getBlock(r.getString("Type"));
		if (lockClass == null) {
			GoldenApple.log(Level.WARNING, "The specified lock type is not loaded: " + r.getString("Type"));
			GoldenApple.log(Level.WARNING, "This lock will be ignored...");
			return null;
		}
		try {
			LockedBlock b = lockClass.blockClass.getConstructor(new Class<?>[] { ResultSet.class }).newInstance(r);
			lockCache.put(b.getLockId(), b);
			cacheOut.add(b.getLockId());
			if (cacheOut.size() > cacheSize) {
				long id = cacheOut.pop();
				lockCache.remove(id);
			}
			return b;
		} catch (Exception e) {
			GoldenApple.log(Level.SEVERE, "There was an error while loading a lock of type '" + lockClass.identifier + "'");
			GoldenApple.log(Level.SEVERE, "Please report this error to the creator of '" + lockClass.plugin.getName() + "'. Please include the following stack trace:");
			GoldenApple.log(Level.SEVERE, e);
			return null;
		}
	}

	@Override
	public LockedBlock getLock(long id) {
		LockedBlock b = checkCache(id);
		if (b != null)
			return b;

		try {
			ResultSet r = GoldenApple.getInstanceDatabaseManager().executeQuery("SELECT * FROM Locks WHERE ID=?", String.valueOf(id));
			try {
				return (r.next()) ? loadIntoCache(r) : null;
			} finally {
				GoldenApple.getInstanceDatabaseManager().closeResult(r);
			}
		} catch (SQLException e) {
			GoldenApple.log(Level.WARNING, "Error while attempting to retrieve a lock from the database:");
			GoldenApple.log(Level.WARNING, e);
			return null;
		}
	}

	@Override
	public LockedBlock getLock(Location l) {
		LockedBlock.correctLocation(l);
		return getLockSpecific(l);
	}

	@Override
	public LockedBlock getLockSpecific(Location l) {
		LockedBlock b = checkCache(l);
		if (b != null)
			return b;

		try {
			ResultSet r = GoldenApple.getInstanceDatabaseManager().executeQuery("SELECT * FROM Locks WHERE X=? AND Y=? AND Z=? AND World=?", l.getBlockX(), l.getBlockY(), l.getBlockZ(), l.getWorld().getName());
			try {
				return (r.next()) ? loadIntoCache(r) : null;
			} finally {
				GoldenApple.getInstanceDatabaseManager().closeResult(r);
			}
		} catch (SQLException e) {
			GoldenApple.log(Level.WARNING, "Error while attempting to retrieve a lock from the database:");
			GoldenApple.log(Level.WARNING, e);
			return null;
		}
	}

	@Override
	public LockedBlock createLock(Location loc, LockLevel access, IPermissionUser owner) throws SQLException, InvocationTargetException {
		LockedBlock.correctLocation(loc);

		RegisteredBlock r = LockedBlock.getBlock(loc.getBlock().getType());
		if (r == null)
			throw new UnsupportedOperationException();

		GoldenApple.getInstanceDatabaseManager().execute("INSERT INTO Locks (X, Y, Z, World, Type, AccessLevel, Owner, AllowExternal) VALUES (?, ?, ?, ?, ?, ?, ?, 0)",
				loc.getBlockX(), loc.getBlockY(), loc.getBlockZ(), loc.getWorld().getName(), r.identifier, access.levelId, (owner == null) ? 0 : owner.getId());
		return getLockSpecific(loc);
	}

	@Override
	public void deleteLock(long id) throws SQLException {
		if (cacheOut.contains(id))
			cacheOut.remove(id);
		if (lockCache.containsKey(id))
			lockCache.remove(id);

		GoldenApple.getInstanceDatabaseManager().execute("DELETE FROM Locks WHERE ID=?", String.valueOf(id));
	}

	@Override
	public boolean lockExists(long id) throws SQLException {
		ResultSet r = GoldenApple.getInstanceDatabaseManager().executeQuery("SELECT NULL FROM Locks WHERE ID=?", String.valueOf(id));
		try {
			return r.next();
		} finally {
			GoldenApple.getInstanceDatabaseManager().closeResult(r);
		}
	}
	
	@Override
	public boolean isOverrideOn(User u) {
		return !GoldenApple.getInstanceMainConfig().getBoolean("modules.lock.explicitOverrideRequired", true) || overriding.contains(u);
	}

	@Override
	public void setOverrideOn(User u, boolean override) {
		if (override && !overriding.contains(u))
			overriding.add(u);
		else if (!override && overriding.contains(u))
			overriding.remove(u);
	}
	
	@Override
	public boolean canOverride(User u) {
		return u.hasPermission(LockManager.fullPermission) || u.hasPermission(LockManager.modifyBlockPermission) || u.hasPermission(LockManager.invitePermission) || u.hasPermission(LockManager.usePermission);
	}

	@Override
	public void clearCache() {
		lockCache.clear();
		cacheOut.clear();
	}

	@Override
	public boolean isLockable(Material m) {
		return LockedBlock.getBlock(m) != null;
	}

}
