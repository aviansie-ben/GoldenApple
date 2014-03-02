package com.bendude56.goldenapple.lock;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.plugin.Plugin;

import com.bendude56.goldenapple.GoldenApple;
import com.bendude56.goldenapple.User;
import com.bendude56.goldenapple.permissions.IPermissionUser;
import com.bendude56.goldenapple.permissions.IPermissionGroup;
import com.bendude56.goldenapple.permissions.PermissionManager;

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
			if (b.identifier.equals(identifier) && (b.blockClass != blockClass || b.blockType == blockType)) {
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

	public static RegisteredBlock getBlock(String identifier) {
		for (int i = 0; i < registeredBlocks.size(); i++) {
			if (registeredBlocks.get(i).identifier.equals(identifier)) {
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

	private final long	lockId;
	private Location	l;
	private long		ownerId;
	private LockLevel	level;
	private String		typeId;
	private boolean     allowExternal;
	
	private HashMap<Long, GuestLevel> userLevel;
	private HashMap<Long, GuestLevel> groupLevel;

	protected LockedBlock(ResultSet r, String typeId) throws SQLException, ClassNotFoundException {
		this.lockId = r.getLong("ID");
		this.l = new Location(Bukkit.getWorld(r.getString("World")), r.getLong("X"), r.getLong("Y"), r.getLong("Z"));
		this.ownerId = r.getLong("Owner");
		this.level = LockLevel.getLevel(r.getInt("AccessLevel"));
		this.typeId = typeId;
		this.allowExternal = r.getBoolean("AllowExternal");
		
		this.loadGroupsAndUsers();
	}
	
	public abstract boolean isRedstoneAccessApplicable();
	public abstract boolean isHopperAccessApplicable();
	
	private final void loadGroupsAndUsers() {
		userLevel = new HashMap<Long, GuestLevel>();
		try {
			ResultSet r = GoldenApple.getInstanceDatabaseManager().executeQuery("SELECT GuestID, AccessLevel FROM LockUsers WHERE LockID=?", lockId);
			try {
				while (r.next()) {
					userLevel.put(r.getLong("GuestID"), GuestLevel.getLevel(r.getInt("AccessLevel")));
				}
			} finally {
				GoldenApple.getInstanceDatabaseManager().closeResult(r);
			}
		} catch (SQLException e) {
			GoldenApple.log(Level.SEVERE, "Error while determining user guests for lock " + lockId + ":");
			GoldenApple.log(Level.SEVERE, e);
		}
		
		groupLevel = new HashMap<Long, GuestLevel>();
		try {
			ResultSet r = GoldenApple.getInstanceDatabaseManager().executeQuery("SELECT GuestID, AccessLevel FROM LockGroups WHERE LockID=?", lockId);
			try {
				while (r.next()) {
					groupLevel.put(r.getLong("GuestID"), GuestLevel.getLevel(r.getInt("AccessLevel")));
				}
			} finally {
				GoldenApple.getInstanceDatabaseManager().closeResult(r);
			}
		} catch (SQLException e) {
			GoldenApple.log(Level.SEVERE, "Error while determining guests for lock " + lockId + ":");
			GoldenApple.log(Level.SEVERE, e);
		}
	}

	/**
	 * Saves the lock into the SQL database
	 */
	public void save() {
		try {
			GoldenApple.getInstanceDatabaseManager().execute("UPDATE Locks SET AccessLevel=?, Owner=?, AllowExternal=? WHERE ID=?", level.levelId, (ownerId <= 0) ? null : ownerId, allowExternal, lockId);
		} catch (SQLException e) {
			GoldenApple.log(Level.SEVERE, "Failed to save changes to lock " + lockId + ":");
			GoldenApple.log(Level.SEVERE, e);
		}
	}

	/**
	 * Gets the unique identifier for this locked block.
	 */
	public final long getLockId() {
		return lockId;
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
	 * Changes the user that owns this block. Pass 0 to make this block owned by
	 * nobody.
	 * 
	 * @param ownerId The ID of the user that should be the new owner of this
	 *            block.
	 */
	public void setOwner(long ownerId) {
		this.ownerId = ownerId;
		save();
	}

	/**
	 * Gets a <em>read-only</em> list of users and their access levels
	 * associated with this lock.
	 */
	public final Map<Long, GuestLevel> getUsers() {
		return Collections.unmodifiableMap(userLevel);
	}

	/**
	 * Gets a <em>read-only</em> list of groups and their access levels
	 * associated with this lock.
	 */
	public final Map<Long, GuestLevel> getGroups() {
		return Collections.unmodifiableMap(groupLevel);
	}

	/**
	 * Adds a user to the guestlist with the specified level of permissions for
	 * this locked block.
	 * 
	 * @param user The user to add to the guest list
	 * @param level The level of access that this user should be given
	 */
	public void addUser(IPermissionUser user, GuestLevel level) {
		try {
			if (userLevel.containsKey(user.getId())) {
				GoldenApple.getInstanceDatabaseManager().execute("UPDATE LockUsers SET AccessLevel=? WHERE LockID=? AND GuestID=?", level.levelId, lockId, user.getId());
			} else {
				GoldenApple.getInstanceDatabaseManager().execute("INSERT INTO LockUsers (LockID, GuestID, AccessLevel) VALUES (?, ?, ?)", lockId, user.getId(), level.levelId);
			}
			
			userLevel.put(user.getId(), level);
		} catch (SQLException e) {
			GoldenApple.log(Level.SEVERE, "Error while adding user '" + user.getName() + "' to the guestlist for lock " + lockId + ":");
			GoldenApple.log(Level.SEVERE, e);
		}
	}

	/**
	 * Removes a user from this block's guestlist.
	 * 
	 * @param user The user that should be removed from the guestlist
	 */
	public void remUser(IPermissionUser user) {
		try {
			userLevel.remove(user.getId());
			GoldenApple.getInstanceDatabaseManager().execute("DELETE FROM LockUsers WHERE LockID=? AND GuestID=?", lockId, user.getId());
		} catch (SQLException e) {
			GoldenApple.log(Level.SEVERE, "Error while removing user '" + user.getName() + "' from the guestlist for lock " + lockId + ":");
			GoldenApple.log(Level.SEVERE, e);
		}
	}
	
	public void addGroup(IPermissionGroup group, GuestLevel level) {
		try {
			if (groupLevel.containsKey(group.getId())) {
				GoldenApple.getInstanceDatabaseManager().execute("UPDATE LockGroups SET AccessLevel=? WHERE LockID=? AND GuestID=?", level.levelId, lockId, group.getId());
			} else {
				GoldenApple.getInstanceDatabaseManager().execute("INSERT INTO LockGroups (LockID, GuestID, AccessLevel) VALUES (?, ?, ?)", lockId, group.getId(), level.levelId);
			}
			
			groupLevel.put(group.getId(), level);
		} catch (SQLException e) {
			GoldenApple.log(Level.SEVERE, "Error while adding group '" + group.getName() + "' to the guestlist for lock " + lockId + ":");
			GoldenApple.log(Level.SEVERE, e);
		}
	}
	
	public void remGroup(IPermissionGroup group) {
		try {
			userLevel.remove(group.getId());
			GoldenApple.getInstanceDatabaseManager().execute("DELETE FROM LockGroups WHERE LockID=? AND GuestID=?", lockId, group.getId());
		} catch (SQLException e) {
			GoldenApple.log(Level.SEVERE, "Error while removing group '" + group.getName() + "' from the guestlist for lock " + lockId + ":");
			GoldenApple.log(Level.SEVERE, e);
		}
	}
	
	public GuestLevel getOverrideLevel(User user) {
		if (user.hasPermission(LockManager.fullPermission))
			return GuestLevel.FULL;
		else if (user.hasPermission(LockManager.modifyBlockPermission))
			return GuestLevel.ALLOW_BLOCK_MODIFY;
		else if (user.hasPermission(LockManager.invitePermission))
			return GuestLevel.ALLOW_INVITE;
		else if (user.hasPermission(LockManager.usePermission))
			return GuestLevel.USE;
		else
			return GuestLevel.NONE;
	}
	
	public GuestLevel getActualLevel(IPermissionUser u) {
		GuestLevel l = (this.level == LockLevel.PUBLIC) ? GuestLevel.USE : GuestLevel.NONE;

		if (ownerId == u.getId())
			return GuestLevel.FULL;
		
		if (userLevel.containsKey(u.getId()) && userLevel.get(u.getId()).levelId > l.levelId)
			l = userLevel.get(u.getId());

		for (Map.Entry<Long, GuestLevel> group : groupLevel.entrySet()) {
			IPermissionGroup g = PermissionManager.getInstance().getGroup(group.getKey());

			if (g.isMember(u, false) && group.getValue().levelId > l.levelId) {
				l = group.getValue();
			}
		}

		return l;
	}

	public GuestLevel getEffectiveLevel(User user) {
		GuestLevel actual = getActualLevel(user), override = getOverrideLevel(user);
		
		if (LockManager.getInstance().isOverrideOn(user))
			return (actual.levelId > override.levelId) ? actual : override;
		else
			return actual;
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
		save();
	}

	/**
	 * Gets a unique identifier for the lock type associated with this lock.
	 */
	public final String getTypeIdentifier() {
		return typeId;
	}
	
	public final boolean getAllowExternal() {
		return allowExternal;
	}
	
	public final void setAllowExternal(boolean allowExternal) {
		this.allowExternal = allowExternal;
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
		return (getEffectiveLevel(user).levelId >= GuestLevel.USE.levelId);
	}

	/**
	 * Checks whether or not the user may invite users for access to this block
	 * as a user with use priviledges only.
	 * 
	 * @param user The user that is being checked for access to this block.
	 * @return True if the user is allowed to edit this block, false otherwise.
	 */
	public boolean canInvite(User user) {
		return (getEffectiveLevel(user).levelId >= GuestLevel.ALLOW_INVITE.levelId);
	}

	/**
	 * Checks whether or not the user may modify properties of the lock on this
	 * block. In this case, modification means moving, deleting, and changing
	 * default access level.
	 * 
	 * @param user The user that is being checked for access to this block.
	 * @return True if the user is allowed to edit this block, false otherwise.
	 */
	public boolean canModifyBlock(User user) {
		return (getEffectiveLevel(user).levelId >= GuestLevel.ALLOW_BLOCK_MODIFY.levelId);
	}
	
	/**
	 * Checks whether or not the user has full control over the lock on this block
	 * 
	 * @param user The user that is being checked for access to this block.
	 * @return True if the user is allowed to edit this block, false otherwise.
	 */
	public boolean hasFullControl(User user) {
		return (getEffectiveLevel(user).levelId >= GuestLevel.FULL.levelId);
	}

	/**
	 * Moves the lock to a new location. Usually used if a block change has
	 * caused the corrected location to change.
	 * 
	 * @param l The location to which the lock should be moved.
	 */
	public void moveLock(Location l) throws SQLException {
		this.l = l;
		GoldenApple.getInstanceDatabaseManager().execute("UPDATE Locks SET X=?, Y=?, Z=?, World=? WHERE ID=?", l.getBlockX(), l.getBlockY(), l.getBlockZ(), l.getWorld().getName(), lockId);
	}

	public static enum GuestLevel {
		UNKNOWN(-1), NONE(0), USE(1), ALLOW_INVITE(2), ALLOW_BLOCK_MODIFY(3), FULL(4);

		/**
		 * The level ID that this access level should be represented by in the
		 * SQL database.
		 */
		public int	levelId;

		GuestLevel(int levelId) {
			this.levelId = levelId;
		}

		/**
		 * Gets the access level corresponding to a specific level ID from the
		 * SQL database.
		 * 
		 * @param levelId The level ID of the access level to find.
		 * @return If the level was found, it is returned, otherwise
		 *         GuestLevel.UNKNOWN is returned.
		 */
		public static GuestLevel getLevel(int levelId) {
			for (GuestLevel l : GuestLevel.values()) {
				if (l.levelId == levelId)
					return l;
			}
			return GuestLevel.UNKNOWN;
		}
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
