package com.bendude56.goldenapple.lock;

import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayDeque;
import java.util.Deque;
import java.util.HashMap;
import java.util.Map;
import java.util.logging.Level;

import org.bukkit.Location;

import com.bendude56.goldenapple.GoldenApple;
import com.bendude56.goldenapple.lock.LockedBlock.LockLevel;
import com.bendude56.goldenapple.lock.LockedBlock.RegisteredBlock;
import com.bendude56.goldenapple.permissions.IPermissionUser;
import com.bendude56.goldenapple.permissions.PermissionManager.Permission;
import com.bendude56.goldenapple.permissions.PermissionManager.PermissionNode;

public class LockManager {

	// goldenapple.lock
	public static PermissionNode		lockNode;
	public static Permission			addPermission;

	// goldenapple.lock.delete
	public static PermissionNode		removeNode;
	public static Permission			removeOwnPermission;
	public static Permission			removeAllPermission;

	// goldenapple.lock.guest
	public static PermissionNode		guestNode;
	public static Permission			guestOwnPermission;
	public static Permission			guestAllPermission;

	private HashMap<Long, LockedBlock>	lockCache;
	private Deque<Long>					cacheOut;
	private int							cacheSize;

	public LockManager() {
		lockCache = new HashMap<Long, LockedBlock>();
		cacheOut = new ArrayDeque<Long>();
		cacheSize = GoldenApple.getInstance().mainConfig.getInt("modules.lock.cacheSize", 100);
		if (cacheSize < 3)
			cacheSize = 3;
		
		try {
			GoldenApple.getInstance().database.execute("CREATE TABLE IF NOT EXISTS Locks (ID BIGINT PRIMARY KEY, X BIGINT, Y BIGINT, Z BIGINT, World VARCHAR(128), Type VARCHAR(32), AccessLevel INT, Owner BIGINT, Guests TEXT)");
		} catch (SQLException e) {
			GoldenApple.log(Level.SEVERE, "Failed to create table 'Locks':");
			GoldenApple.log(Level.SEVERE, e);
		}
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

	public LockedBlock getLock(long id) {
		LockedBlock b = checkCache(id);
		if (b != null)
			return b;

		try {
			ResultSet r = GoldenApple.getInstance().database.executeQuery("SELECT * FROM Locks WHERE ID=?", String.valueOf(id));
			try {
				return (r.next()) ? loadIntoCache(r) : null;
			} finally {
				r.close();
			}
		} catch (SQLException e) {
			GoldenApple.log(Level.WARNING, "Error while attempting to retrieve a lock from the database:");
			GoldenApple.log(Level.WARNING, e);
			return null;
		}
	}

	public LockedBlock getLock(Location l) {
		LockedBlock.correctLocation(l);
		LockedBlock b = checkCache(l);
		if (b != null)
			return b;

		try {
			ResultSet r = GoldenApple.getInstance().database.executeQuery("SELECT * FROM Locks WHERE X=? AND Y=? AND Z=? AND World=?", l.getBlockX(), l.getBlockY(), l.getBlockZ(), l.getWorld().getName());
			try {
				return (r.next()) ? loadIntoCache(r) : null;
			} finally {
				r.close();
			}
		} catch (SQLException e) {
			GoldenApple.log(Level.WARNING, "Error while attempting to retrieve a lock from the database:");
			GoldenApple.log(Level.WARNING, e);
			return null;
		}
	}

	public LockedBlock createLock(Location loc, LockLevel access, IPermissionUser owner) throws IOException, InvocationTargetException {
		LockedBlock.correctLocation(loc);

		RegisteredBlock r = LockedBlock.getBlock(loc.getBlock().getType());
		if (r == null)
			throw new UnsupportedOperationException();

		try {
			LockedBlock b = r.blockClass.getConstructor(Long.class, Location.class, Long.class, LockLevel.class).newInstance(nextId(), loc, owner.getId(), access);
			b.save(true);
			return b;
		} catch (SQLException | IOException e) {
			throw new IOException(e);
		} catch (Exception e) {
			throw new InvocationTargetException(e);
		}
	}

	public void deleteLock(long id) throws SQLException {
		if (cacheOut.contains(id))
			cacheOut.remove(id);
		if (lockCache.containsKey(id))
			lockCache.remove(id);
		
		GoldenApple.getInstance().database.execute("DELETE FROM Locks WHERE ID=?", String.valueOf(id));
	}

	public boolean lockExists(long id) throws SQLException {
		ResultSet r = GoldenApple.getInstance().database.executeQuery("SELECT ID FROM Locks WHERE ID=?", String.valueOf(id));
		try {
			return r.next();
		} finally {
			r.close();
		}
	}

	public long nextId() throws SQLException {
		long id;
		for (id = 0; lockExists(id); id++);
		return id;
	}

}
