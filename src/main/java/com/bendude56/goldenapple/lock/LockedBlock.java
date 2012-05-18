package com.bendude56.goldenapple.lock;

import java.io.IOException;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.plugin.Plugin;

import com.bendude56.goldenapple.GoldenApple;
import com.bendude56.goldenapple.User;
import com.bendude56.goldenapple.util.Serializer;

/**
 * Represents a GoldenApple lock on a block. This class can represent
 * <em>any</em> block, including blocks that have been added by a third party
 * plugin.
 * 
 * @author ben_dude56
 */
public abstract class LockedBlock {
	private static List<RegisteredBlock>	registeredBlocks	= new ArrayList<RegisteredBlock>();
	private static List<ILocationCorrector>	locationCorrectors	= new ArrayList<ILocationCorrector>();

	/**
	 * Registers a lockable block in the GoldenApple lock system. Two or more
	 * classes can exist for one block type (in order to preserve
	 * compatibility). In this case, the first one registered (except the
	 * default GoldenApple locks) will be used by default when locking a block
	 * of that type.
	 * 
	 * @param identifier The identifier that should be used to identify the type
	 *            of lock when saving it to the database.
	 * @param plugin The plugin instance that is requesting the lock. This is
	 *            used to unregister locks when a plugin is unloaded.
	 * @param blockType The type of block that this lock should be used on.
	 * @param blockClass The class that should be used to represent this lock
	 *            when it is loaded into memory.
	 */
	public static void registerBlock(String identifier, Plugin plugin, Material blockType, Class<? extends LockedBlock> blockClass) {
		for (RegisteredBlock b : registeredBlocks) {
			if (b.identifier == identifier) {
				throw new IllegalArgumentException("Identifier " + identifier + " already registered to another class!");
			}
		}
		registeredBlocks.add(new RegisteredBlock(identifier, plugin, blockType, blockClass));
	}

	/**
	 * Unregisters a lockable block from the GoldenApple lock list.
	 * <p>
	 * <em><strong>Warning:</strong> This method does not check if any locks are
	 * loaded using the specified class. This method should only be used after you
	 * have made sure that no more instances of the specified lock are present in
	 * the GoldenApple lock cache.</em>
	 * 
	 * @param identifier The identifier used when the lock was first registered.
	 */
	public static void unregisterBlock(String identifier) {
		for (int i = 0; i < registeredBlocks.size(); i++) {
			if (registeredBlocks.get(i).identifier == identifier) {
				registeredBlocks.remove(i);
				return;
			}
		}
	}

	/**
	 * Unregisters all lockable blocks that were registered by a specific
	 * plugin.
	 * <p>
	 * <em><strong>Warning:</strong> This method does not check if any locks are
	 * loaded using the specified plugin. This method should only be used after you
	 * have made sure that no more instances of the specified plugin's registered
	 * locks are present in the GoldenApple lock cache.</em>
	 * 
	 * @param plugin The plugin that should have all locks unregistered.
	 */
	public static void unregisterBlocks(Plugin plugin) {
		for (int i = 0; i < registeredBlocks.size(); i++) {
			if (registeredBlocks.get(i).plugin == plugin) {
				registeredBlocks.remove(i);
			}
		}
	}
	
	public static RegisteredBlock getBlock(Material m) {
		for (int i = 0; i < registeredBlocks.size(); i++) {
			if (registeredBlocks.get(i).plugin == GoldenApple.getInstance()) {
				continue;
			}
			if (registeredBlocks.get(i).blockType == m) {
				return registeredBlocks.get(i);
			}
		}
		for (int i = 0; i < registeredBlocks.size(); i++) {
			if (registeredBlocks.get(i).blockType == m) {
				return registeredBlocks.get(i);
			}
		}
		return null;
	}

	/**
	 * Registers a location corrector that can adjust locations before
	 * adding/checking for locks. For example, this could be useful in order to
	 * lock a double-chest with a single lock by correcting the location.
	 * 
	 * @param corrector The class of the {@link ILocationCorrector} to register.
	 */
	public static void registerCorrector(Class<? extends ILocationCorrector> corrector) {
		for (ILocationCorrector c : locationCorrectors) {
			if (corrector.isInstance(c)) {
				throw new IllegalArgumentException("Location corrector already registered!");
			}
		}
		try {
			locationCorrectors.add(corrector.newInstance());
		} catch (Exception e) {
			throw new RuntimeException(e);
		}
	}

	/**
	 * Unregisters a location corrector.
	 * 
	 * @param corrector The class of the {@link ILocationCorrector} to register.
	 */
	public static void unregisterCorrector(Class<? extends ILocationCorrector> corrector) {
		for (int i = 0; i < locationCorrectors.size(); i++) {
			if (corrector.isInstance(locationCorrectors.get(i))) {
				locationCorrectors.remove(i);
				return;
			}
		}
	}

	/**
	 * Takes a location and corrects it such that it points to the location that
	 * would be used as the lock location.
	 * 
	 * @param l The location that should be corrected
	 */
	public static void correctLocation(Location l) {
		for (ILocationCorrector corrector : locationCorrectors) {
			corrector.correctLocation(l);
		}
	}

	static {
		registerBlock("GA_CHEST", GoldenApple.getInstance(), Material.CHEST, LockedChest.class);
		registerBlock("GA_FURNACE", GoldenApple.getInstance(), Material.FURNACE, null);
		registerCorrector(DoubleChestLocationCorrector.class);
	}

	private final Location	l;
	private long			ownerId;
	private ArrayList<Long>	guests;
	private LockLevel		level;

	@SuppressWarnings("unchecked")
	protected LockedBlock(ResultSet r) throws SQLException, ClassNotFoundException, IOException {
		this.l = Serializer.deserializeLocation(r.getString("Location"));
		this.ownerId = r.getLong("Owner");
		this.guests = (ArrayList<Long>)Serializer.deserialize(r.getString("Guests"));
		this.level = LockLevel.getLevel(r.getInt("Level"));
	}

	protected LockedBlock(Location l, long ownerId, LockLevel level) {
		this.l = l;
		this.ownerId = ownerId;
		this.guests = new ArrayList<Long>();
		this.level = level;
	}

	/**
	 * Gets the location of this locked block.
	 */
	public final Location getLocation() {
		return l;
	}

	/**
	 * Gets the ID of the user that owns this block. Returns -1 if the block has
	 * no owner.
	 */
	public final long getOwner() {
		return ownerId;
	}

	/**
	 * Changes the user that owns this block. Pass -1 to make this block owned
	 * by no one player.
	 * <p>
	 * <em><strong>Note:</strong> When overriding this method, <strong>never
	 * </strong> cancel the event! Cancelling this call may have unintended
	 * consequences!</em>
	 * 
	 * @param ownerId The ID of the user that should be the new owner of this
	 *            block.
	 */
	public void setOwner(long ownerId) {
		this.ownerId = ownerId;
	}

	/**
	 * Gets a <em>read-only</em> list of guests that have limited access to this
	 * block.
	 */
	public final List<Long> getGuests() {
		return Collections.unmodifiableList(guests);
	}

	/**
	 * Adds a user to the list of guests that can have limited access. A user
	 * appointed as a guest will be able to use the block and change the
	 * contents of the block (if applicable).
	 * 
	 * @param guestId The ID of the user that should be added to the guest list
	 *            of this block.
	 */
	public void addGuest(long guestId) {
		if (!guests.contains(guestId))
			guests.add(guestId);
	}

	/**
	 * Removes a user from the guest list of this block.
	 * 
	 * <em><strong>Note:</strong> When overriding this method, <strong>never
	 * </strong> cancel the event! Cancelling this call may have unintended
	 * consequences!</em>
	 * 
	 * @param guestId The ID of the user that should be removed from the guest
	 *            list of this block.
	 */
	public void remGuest(long guestId) {
		if (guests.contains(guestId))
			guests.remove(guestId);
	}

	/**
	 * Gets the level of protection on this block.
	 */
	public final LockLevel getLevel() {
		return level;
	}

	/**
	 * Sets the level of protection on this block.
	 * 
	 * @param level The level to set the protection on this block to.
	 */
	public void setLevel(LockLevel level) {
		this.level = level;
	}

	/**
	 * Checks whether or not the user may use this block. Use in this context is
	 * defined as being able to use right-click actions and, if applicable,
	 * modify the contents of the block represented by this protection.
	 * 
	 * @param user The user that is being checked for access to this block.
	 * @return True if the user is allowed to use this block, false otherwise.
	 */
	public boolean canUse(User user) {
		return (user.getId() == ownerId || guests.contains(user.getId()));
	}

	/**
	 * Checks whether or not the user may edit this block. Edit in this context
	 * is defined as being able to destroy the block, or change the properties
	 * of the lock on this block.
	 * 
	 * @param user The user that is being checked for access to this block.
	 * @return True if the user is allowed to edit this block, false otherwise.
	 */
	public boolean canEdit(User user) {
		return (user.getId() == ownerId);
	}

	/**
	 * An enum representing the various access levels that can be applied to a
	 * lock.
	 * 
	 * @author ben_dude56
	 * 
	 */
	public static enum LockLevel {
		UNKNOWN(-1), PRIVATE(0), PUBLIC(1);

		/**
		 * The level ID that this access level should be represented by in the
		 * SQL database.
		 */
		public int	levelId;

		LockLevel(int levelId) {
			this.levelId = levelId;
		}

		/**
		 * Gets the access level corresponding to a specific level ID from the
		 * SQL database.
		 * 
		 * @param levelId The level ID of the access level to find.
		 * @return If the level was found, it is returned, otherwise
		 *         LockLevel.UNKNOWN is returned.
		 */
		public static LockLevel getLevel(int levelId) {
			for (LockLevel l : LockLevel.values()) {
				if (l.levelId == levelId)
					return l;
			}
			return LockLevel.UNKNOWN;
		}
	}

	public static class RegisteredBlock {
		public final String							identifier;
		public final Plugin							plugin;
		public final Material						blockType;
		public final Class<? extends LockedBlock>	blockClass;

		private RegisteredBlock(String identifier, Plugin plugin, Material blockType, Class<? extends LockedBlock> blockClass) {
			this.identifier = identifier;
			this.plugin = plugin;
			this.blockType = blockType;
			this.blockClass = blockClass;
		}
	}
}
