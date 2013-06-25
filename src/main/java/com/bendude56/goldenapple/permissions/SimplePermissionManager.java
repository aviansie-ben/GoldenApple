package com.bendude56.goldenapple.permissions;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Deque;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;

import com.bendude56.goldenapple.GoldenApple;

/**
 * Manages the inner workings of the GoldenApple permissions system
 * 
 * @author Deaboy
 * @author ben_dude56
 */
public class SimplePermissionManager extends PermissionManager {

	

	private int                             userCacheSize;
	private HashMap<Long, PermissionUser>	userCache		= new HashMap<Long, PermissionUser>();
	private Deque<Long>						userCacheOut	= new ArrayDeque<Long>();
	
	private int                             groupCacheSize;
	private HashMap<Long, PermissionGroup>	groupCache		= new HashMap<Long, PermissionGroup>();
	private Deque<Long>						groupCacheOut   = new ArrayDeque<Long>();
	
	private List<Permission>				permissions		= new ArrayList<Permission>();
	private List<PermissionNode>			nodes			= new ArrayList<PermissionNode>();
	
	private PermissionNode					rootNode;
	public Permission						rootStar;

	public SimplePermissionManager() {
		rootNode = new PermissionNode("", null);
		nodes.add(rootNode);
		rootStar = new Permission("*", rootNode);
		permissions.add(rootStar);
		
		userCacheSize = Math.max(GoldenApple.getInstanceMainConfig().getInt("modules.permissions.userCacheSize", 20), 5);
		groupCacheSize = Math.max(GoldenApple.getInstanceMainConfig().getInt("modules.permissions.groupCacheSize", 20), 5);

		GoldenApple.getInstanceDatabaseManager().createOrUpdateTable("Users");
		GoldenApple.getInstanceDatabaseManager().createOrUpdateTable("UserPermissions");
		GoldenApple.getInstanceDatabaseManager().createOrUpdateTable("Groups");
		GoldenApple.getInstanceDatabaseManager().createOrUpdateTable("GroupPermissions");
		GoldenApple.getInstanceDatabaseManager().createOrUpdateTable("GroupGroupMembers");
		GoldenApple.getInstanceDatabaseManager().createOrUpdateTable("GroupUserMembers");

		checkDefaultGroups();
	}
	
	private void popCache() {
		while (userCacheOut.size() > userCacheSize) {
			userCache.remove(userCacheOut.pop());
		}
		
		while (groupCacheOut.size() > groupCacheSize) {
			groupCache.remove(groupCacheOut.pop());
		}
	}

	private void checkDefaultGroups() {
		if (!GoldenApple.getInstanceMainConfig().getString("modules.permissions.reqGroup").equals(""))
			createGroup(GoldenApple.getInstanceMainConfig().getString("modules.permissions.reqGroup"));
		for (String g : GoldenApple.getInstanceMainConfig().getStringList("modules.permissions.defaultGroups")) {
			createGroup(g);
		}
		for (String g : GoldenApple.getInstanceMainConfig().getStringList("modules.permissions.opGroups")) {
			createGroup(g);
		}
		for (String g : GoldenApple.getInstanceMainConfig().getStringList("modules.permissions.devGroups")) {
			createGroup(g);
		}
	}

	/**
	 * Registers a new permission for use with the GoldenApple permissions
	 * system.
	 * 
	 * @param name The short name (excluding node) of the permission to add.
	 * @param node The node in which to add the permission.
	 * @return The permission that has been registered. If the permission
	 *         already exists, the existing one will be returned.
	 */
	public Permission registerPermission(String name, PermissionNode node) {
		if (nodes.contains(node)) {
			for (Permission p : this.permissions) {
				if (p.getName().equalsIgnoreCase(name) && p.getNode() == node) {
					return p;
				}
			}
			Permission newPermission = new Permission(name, node);
			permissions.add(newPermission);
			return newPermission;
		} else {
			return null;
		}
	}

	/**
	 * Registers a new permission for use with the GoldenApple permissions
	 * system.
	 * 
	 * @param fullName The full name (including node) of the permission to add.
	 * @return The permission that has been registered. If the permission
	 *         already exists, the existing one will be returned.
	 */
	public Permission registerPermission(String fullName) {
		String[] name = fullName.split("\\.");
		PermissionNode node = rootNode;
		for (int i = 0; i < name.length; i++) {
			if (i == name.length - 1) {
				return registerPermission(name[i], node);
			} else {
				node = registerNode(name[i], node);
			}
		}
		return null;
	}

	/**
	 * Registers a new permission node for use with the GoldenApple permissions
	 * system.
	 * 
	 * @param name The short name (excluding node) of the node to add.
	 * @param node The node in which to add the node.
	 * @return The node that has been registered. If the node already exists,
	 *         the existing one will be returned.
	 */
	public PermissionNode registerNode(String name, PermissionNode node) {
		if (nodes.contains(node)) {
			for (PermissionNode n : nodes) {
				if (n.getName().equalsIgnoreCase(name) && n.getNode() == node) {
					return n;
				}
			}
			PermissionNode newNode = new PermissionNode(name, node);
			nodes.add(newNode);
			registerPermission("*", node);
			return newNode;
		} else {
			return null;
		}
	}
	
	public Permission getRootStar() {
		return rootStar;
	}

	/**
	 * Gets a list of all users in the database that are currently in the user
	 * cache.
	 * <p>
	 * <em><strong>Note:</strong> Does not return handles to users that are not currently in the
	 * user cache. In order to find a specific user, use {@link SimplePermissionManager#getUser(long id)}
	 * or {@link SimplePermissionManager#getUser(String name)}</em>
	 */
	public HashMap<Long, PermissionUser> getUserCache() {
		return userCache;
	}

	/**
	 * Gets a list of all groups in the database that are currently in the group
	 * cache.
	 * <p>
	 * <em><strong>Note:</strong> Does not return handles to groups that are not currently in the
	 * group cache. In order to find a specific group, use {@link SimplePermissionManager#getGroup(long id)}
	 * or {@link SimplePermissionManager#getGroup(String name)}</em>
	 */
	public HashMap<Long, PermissionGroup> getGroupCache() {
		return groupCache;
	}

	/**
	 * Gets a list of all currently registered permissions.
	 */
	public List<Permission> getRegisteredPermissions() {
		return permissions;
	}

	/**
	 * Gets detailed information about a permission based on its name
	 * 
	 * @param name The name of the permission to get information on
	 * @return Information about the requested permission
	 */
	public Permission getPermissionByName(String name) {
		for (Permission perm : permissions) {
			if (perm.getFullName().equalsIgnoreCase(name)) {
				return perm;
			}
		}
		return null;
	}

	/**
	 * Gets a list of all currently registered permission nodes
	 */
	public List<PermissionNode> getRegisteredNodes() {
		return nodes;
	}

	/**
	 * Gets detailed information about a permission node based on its name
	 * 
	 * @param name The name of the permission node to get information on
	 * @return Information about the requested permission node
	 */
	public PermissionNode getNodeByName(String name) {
		String[] path = name.split(".");
		PermissionNode node = rootNode;
		pathSearch: for (int i = 0; i < path.length; i++) {
			for (PermissionNode n : nodes) {
				if (n.getName().equalsIgnoreCase(path[i]) && n.getNode() == node) {
					node = n;
					continue pathSearch;
				}
			}
			return null;
		}
		return node;
	}

	/**
	 * Gets the root node of the permissions system
	 */
	public PermissionNode getRootNode() {
		return rootNode;
	}

	/**
	 * Retrieves the user ID of a specific user based on their username. Note
	 * that this method does <strong>not</strong> load the actual user data into
	 * memory, nor will it add the user to the user cache.
	 * 
	 * @param name The name of the user to search for
	 * @return If the user was found, their ID will be returned. If the user was
	 *         not found or an error occurred, -1 will be returned.
	 */
	public long getUserId(String name) {
		try {
			ResultSet r = GoldenApple.getInstanceDatabaseManager().executeQuery("SELECT ID FROM Users WHERE Name=?", new Object[] { name });
			try {
				if (r.next()) {
					return r.getLong("ID");
				} else {
					return -1;
				}
			} finally {
				GoldenApple.getInstanceDatabaseManager().closeResult(r);
			}
		} catch (SQLException e) {
			GoldenApple.log(Level.WARNING, "Failed to retrieve ID for user '" + name + "':");
			GoldenApple.log(Level.WARNING, e);
			return -1;
		}
	}

	/**
	 * Retrieves a user instance based off of their user ID. Without the use of
	 * {@link SimplePermissionManager#setSticky(long id, boolean sticky)}, the return
	 * value of this function should only be used for short-term use.
	 * 
	 * @param id The ID of the user instance that should be retrieved from the
	 *            database.
	 * @return If the user was found successfully, their instance will be
	 *         cached, then returned. Otherwise, null will be returned.
	 */
	public PermissionUser getUser(long id) {
		if (userCache.containsKey(id)) {
			return userCache.get(id);
		} else {
			try {
				ResultSet r = GoldenApple.getInstanceDatabaseManager().executeQuery("SELECT * FROM Users WHERE ID=?", id);
				try {
					if (r.next()) {
						return new PermissionUser(r.getLong("ID"), r.getString("Name"), r.getString("Locale"), r.getBoolean("ComplexCommands"), r.getBoolean("AutoLock"));
					} else {
						return null;
					}
				} finally {
					GoldenApple.getInstanceDatabaseManager().closeResult(r);
				}
			} catch (SQLException e) {
				GoldenApple.log(Level.WARNING, "Failed to load user " + id + ":");
				GoldenApple.log(Level.WARNING, e);
				return null;
			}
		}
	}

	/**
	 * Retrieves a user instance based off of their username. Without the use of
	 * {@link SimplePermissionManager#setSticky(long id, boolean sticky)}, the return
	 * value of this function should only be used for short-term use.
	 * 
	 * @param name The username of the user instance that should be retrieved
	 *            from the database.
	 * @return If the user was found successfully, their instance will be
	 *         cached, then returned. Otherwise, null will be returned.
	 */
	public PermissionUser getUser(String name) {
		for (Map.Entry<Long, PermissionUser> entry : userCache.entrySet()) {
			if (entry.getValue().getName().equalsIgnoreCase(name)) {
				return entry.getValue();
			}
		}
		try {
			ResultSet r = GoldenApple.getInstanceDatabaseManager().executeQuery("SELECT * FROM Users WHERE Name=?", name);
			try {
				if (r.next()) {
					PermissionUser u = new PermissionUser(r.getLong("ID"), r.getString("Name"), r.getString("Locale"), r.getBoolean("ComplexCommands"), r.getBoolean("AutoLock"));
					userCache.put(u.getId(), u);
					userCacheOut.addLast(u.getId());
					popCache();
					return u;
				} else {
					return null;
				}
			} finally {
				GoldenApple.getInstanceDatabaseManager().closeResult(r);
			}
		} catch (SQLException e) {
			GoldenApple.log(Level.WARNING, "Failed to load user '" + name + "':");
			GoldenApple.log(Level.WARNING, e);
			return null;
		}
	}

	/**
	 * Allows a user to be set in the cache to be sticky. A sticky user will
	 * never be removed from the cache. This is normally only used to make
	 * online users never unload.
	 * 
	 * @param id The ID of the user to change the stickiness of
	 * @param sticky True to never unload this user from the cache, false to
	 *            unload them when space is needed in the cache
	 */
	public void setUserSticky(long id, boolean sticky) {
		if (userCache.containsKey(id)) {
			if (sticky && userCacheOut.contains(id)) {
				userCacheOut.remove(id);
			} else if (!sticky && !userCacheOut.contains(id)) {
				userCacheOut.addLast(id);
				if (userCacheOut.size() > 10) {
					long id2 = userCacheOut.pop();
					userCache.remove(id2);
				}
			}
		} else {
			if (getUser(id) == null) {
				throw new NullPointerException("Attempting to change the stickyness of a user that does not exist!");
			}
		}
	}

	/**
	 * Checks whether or not a user with a given ID is currently marked as
	 * sticky. (See {@link SimplePermissionManager#setUserSticky(long id, boolean sticky)}
	 * for info on stickyness)
	 * 
	 * @param id The ID of the user to check
	 */
	public boolean isUserSticky(long id) {
		return userCache.containsKey(id) && !userCacheOut.contains(id);
	}

	/**
	 * Retrieves a group instance based off of the provided group ID.
	 * 
	 * @param id The ID of the group instance that should be retrieved from the
	 *            database.
	 * @return If the group was found successfully, their instance will be
	 *         cached, then returned. Otherwise, null will be returned.
	 */
	public PermissionGroup getGroup(long id) {
		if (groupCache.containsKey(id)) {
			return groupCache.get(id);
		} else {
			try {
				ResultSet r = GoldenApple.getInstanceDatabaseManager().executeQuery("SELECT * FROM Groups WHERE ID=?", id);
				try {
					if (r.next()) {
						return new PermissionGroup(r);
					} else {
						return null;
					}
				} finally {
					GoldenApple.getInstanceDatabaseManager().closeResult(r);
				}
			} catch (SQLException e) {
				GoldenApple.log(Level.WARNING, "Failed to load group " + id + ":");
				GoldenApple.log(Level.WARNING, e);
				return null;
			}
		}
	}

	/**
	 * Retrieves a user instance based off of the provided group name.
	 * 
	 * @param name The name of the group instance that should be retrieved from
	 *            the database.
	 * @return If the group was found successfully, their instance will be
	 *         cached, then returned. Otherwise, null will be returned.
	 */
	public PermissionGroup getGroup(String name) {
		for (Map.Entry<Long, PermissionGroup> entry : groupCache.entrySet()) {
			if (entry.getValue().getName().equalsIgnoreCase(name)) {
				return entry.getValue();
			}
		}
		try {
			ResultSet r = GoldenApple.getInstanceDatabaseManager().executeQuery("SELECT * FROM Groups WHERE Name=?", name);
			try {
				if (r.next()) {
					PermissionGroup g = new PermissionGroup(r);
					groupCache.put(g.getId(), g);
					groupCacheOut.addLast(g.getId());
					popCache();
					return g;
				} else {
					return null;
				}
			} finally {
				GoldenApple.getInstanceDatabaseManager().closeResult(r);
			}
		} catch (SQLException e) {
			GoldenApple.log(Level.WARNING, "Failed to load group '" + name + "':");
			GoldenApple.log(Level.WARNING, e);
			return null;
		}
	}

	/**
	 * Checks whether or not a user exists in the database
	 * 
	 * @param name The name to check the database against
	 */
	public boolean userExists(String name) throws SQLException {
		ResultSet r = GoldenApple.getInstanceDatabaseManager().executeQuery("SELECT NULL FROM Users WHERE Name=?", new Object[] { name });
		try {
			return r.next();
		} finally {
			GoldenApple.getInstanceDatabaseManager().closeResult(r);
		}
	}

	/**
	 * Attempts to add a new user entry into the database in order to store user
	 * data. The user will be added into the default groups as defined in
	 * config.yml
	 * 
	 * @param name The name of the user to create and add to the database
	 * @return If the user already exists, the existing user is returned. If the
	 *         user was created successfully, the new user is returned. If an
	 *         error occurred, null is returned.
	 */
	public PermissionUser createUser(String name) {
		try {
			if (userExists(name))
				return getUser(name);
		} catch (SQLException e) { }
		try {
			GoldenApple.getInstanceDatabaseManager().execute("INSERT INTO Users (Name, ComplexCommands, AutoLock) VALUES (?, ?, ?)", name,
					GoldenApple.getInstanceMainConfig().getBoolean("modules.permissions.defaultComplexCommands", true),
					GoldenApple.getInstanceMainConfig().getBoolean("modules.lock.autoLockDefault", true));
			return getUser(name);
		} catch (SQLException e) {
			GoldenApple.log(Level.WARNING, "Failed to create user '" + name + "':");
			GoldenApple.log(Level.WARNING, e);
			return null;
		}
	}

	/**
	 * Checks whether or not a group exists in the database
	 * 
	 * @param name The name to check the database against
	 */
	public boolean groupExists(String name) throws SQLException {
		ResultSet r = GoldenApple.getInstanceDatabaseManager().executeQuery("SELECT NULL FROM Groups WHERE Name=?", new Object[] { name });
		try {
			return r.next();
		} finally {
			GoldenApple.getInstanceDatabaseManager().closeResult(r);
		}
	}

	/**
	 * Attempts to add a new group entry into the database in order to store
	 * group data.
	 * 
	 * @param name The name of the group to create and add to the database
	 * @return If the group already exists, the existing group is returned. If
	 *         the group was created successfully, the new group is returned. If
	 *         an error occurred, null is returned.
	 */
	public PermissionGroup createGroup(String name) {
		try {
			if (groupExists(name))
				return getGroup(name);
		} catch (SQLException e) { }
		try {
			GoldenApple.getInstanceDatabaseManager().execute("INSERT INTO Groups (Name, Priority) VALUES (?, 1)", name);
			return getGroup(name);
		} catch (SQLException e) {
			GoldenApple.log(Level.WARNING, "Failed to create group '" + name + "':");
			GoldenApple.log(Level.WARNING, e);
			return null;
		}
	}

	/**
	 * Deletes a user from the database, and from the cache if applicable.
	 * 
	 * @param id The ID of the user that should be deleted
	 */
	public void deleteUser(long id) throws SQLException {
		if (id < 0) {
			throw new IllegalArgumentException("id cannot be < 0");
		} else if (isUserSticky(id)) {
			throw new UnsupportedOperationException("Cannot delete a sticky user");
		}
		
		if (userCache.containsKey(id))
			userCache.remove(id);
		if (userCacheOut.contains(id))
			userCacheOut.remove(id);
		
		GoldenApple.getInstanceDatabaseManager().execute("DELETE FROM Users WHERE ID=?", id);
	}

	/**
	 * Deletes a user from the database, and from the cache if applicable.
	 * 
	 * @param id The ID of the user that should be deleted
	 */
	public void deleteGroup(long id) throws SQLException {
		if (id < 0) {
			throw new IllegalArgumentException("id cannot be < 0");
		}
		
		if (groupCache.containsKey(id))
			groupCache.remove(id);
		
		GoldenApple.getInstanceDatabaseManager().execute("DELETE FROM Groups WHERE ID=?", id);
	}

	/**
	 * Closes the database connection and flushes the cache
	 */
	public void close() {
		userCache = null;
		groupCache = null;
		permissions = null;
		nodes = null;
		rootNode = null;
	}
}
