package com.bendude56.goldenapple.permissions;

import java.io.IOException;
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
import com.bendude56.goldenapple.util.Serializer;

/**
 * Manages the inner workings of the GoldenApple permissions system
 * 
 * @author Deaboy
 * @author ben_dude56
 */
public class PermissionManager {
	private HashMap<Long, PermissionUser>	userCache	= new HashMap<Long, PermissionUser>();
	private Deque<Long>						cacheOut	= new ArrayDeque<Long>();
	private HashMap<Long, PermissionGroup>	groups		= new HashMap<Long, PermissionGroup>();
	private List<Permission>				permissions	= new ArrayList<Permission>();
	private List<PermissionNode>			nodes		= new ArrayList<PermissionNode>();
	private PermissionNode					rootNode;

	public PermissionManager() {
		rootNode = new PermissionNode("");
		nodes.add(rootNode);
		try {
			GoldenApple.getInstance().database.execute("CREATE TABLE IF NOT EXISTS Users (ID BIGINT PRIMARY KEY, Name VARCHAR(128), Locale VARCHAR(128), Permissions TEXT)");
		} catch (SQLException e) {
			GoldenApple.log(Level.SEVERE, "Failed to create table 'Users':");
			GoldenApple.log(Level.SEVERE, e);
		}
		try {
			GoldenApple.getInstance().database.execute("CREATE TABLE IF NOT EXISTS Groups (ID BIGINT PRIMARY KEY, Name VARCHAR(128), Permissions TEXT, Users TEXT, Groups TEXT)");
		} catch (SQLException e) {
			GoldenApple.log(Level.SEVERE, "Failed to create table 'Groups':");
			GoldenApple.log(Level.SEVERE, e);
		}
		checkUserColumns();
		checkGroupColumns();
		try {
			ResultSet r = GoldenApple.getInstance().database.executeQuery("SELECT ID, Name, Permissions, Users, Groups FROM Groups");
			while (r.next()) {
				groups.put(r.getLong("ID"), new PermissionGroup(r.getLong("ID"), r.getString("Name"), r.getString("Users"), r.getString("Groups"), r.getString("Permissions")));
			}
			r.close();
		} catch (SQLException e) {
			GoldenApple.log(Level.SEVERE, "Failed to load groups:");
			GoldenApple.log(Level.SEVERE, e);
		}
		checkDefaultGroups();
	}

	private void checkUserColumns() {
		try {
			ArrayList<String> columns = new ArrayList<String>();
			ResultSet r = GoldenApple.getInstance().database.executeQuery("PRAGMA TABLE_INFO(Users)");
			while (r.next()) {
				columns.add(r.getString(2));
			}
			r.close();
			if (!columns.contains("Name")) {
				GoldenApple.getInstance().database.execute("ALTER TABLE Users ADD COLUMN Name VARCHAR(128)");
			}
			if (!columns.contains("Locale")) {
				GoldenApple.getInstance().database.execute("ALTER TABLE Users ADD COLUMN Locale VARCHAR(128)");
			}
			if (!columns.contains("Permissions")) {
				GoldenApple.getInstance().database.execute("ALTER TABLE Users ADD COLUMN Permissions TEXT");
			}
		} catch (SQLException e) {
			GoldenApple.log(Level.SEVERE, "Failed to verify structure of table 'Users':");
			GoldenApple.log(Level.SEVERE, e);
		}
	}

	private void checkGroupColumns() {
		try {
			ArrayList<String> columns = new ArrayList<String>();
			ResultSet r = GoldenApple.getInstance().database.executeQuery("PRAGMA TABLE_INFO(Groups)");
			while (r.next()) {
				columns.add(r.getString(2));
			}
			r.close();
			if (!columns.contains("Name")) {
				GoldenApple.getInstance().database.execute("ALTER TABLE Groups ADD COLUMN Name VARCHAR(128)");
			}
			if (!columns.contains("Permissions")) {
				GoldenApple.getInstance().database.execute("ALTER TABLE Groups ADD COLUMN Permissions TEXT");
			}
			if (!columns.contains("Users")) {
				GoldenApple.getInstance().database.execute("ALTER TABLE Groups ADD COLUMN Users TEXT");
			}
			if (!columns.contains("Groups")) {
				GoldenApple.getInstance().database.execute("ALTER TABLE Groups ADD COLUMN Groups TEXT");
			}
		} catch (SQLException e) {
			GoldenApple.log(Level.SEVERE, "Failed to verify structure of table 'Groups':");
			GoldenApple.log(Level.SEVERE, e);
		}
	}

	private void checkDefaultGroups() {
		createGroup(GoldenApple.getInstance().mainConfig.getString("modules.permissions.reqGroup"));
		for (String g : GoldenApple.getInstance().mainConfig.getStringList("modules.permissions.defaultGroups")) {
			createGroup(g);
		}
		for (String g : GoldenApple.getInstance().mainConfig.getStringList("modules.permissions.opGroups")) {
			createGroup(g);
		}
		for (String g : GoldenApple.getInstance().mainConfig.getStringList("modules.permissions.devGroups")) {
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
		String[] name = fullName.split(".");
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
			return newNode;
		} else {
			return null;
		}
	}

	/**
	 * Gets a list of all users in the database that are currently in the user
	 * cache.
	 * <p>
	 * <em><strong>Note:</strong> Does not return handles to users that are not currently in the
	 * user cache. In order to find a specific user, use {@link PermissionManager#getUser(long id)}
	 * or {@link PermissionManager#getUser(String name)}</em>
	 */
	public HashMap<Long, PermissionUser> getUserCache() {
		return userCache;
	}

	/**
	 * Gets a list of all groups in the database.
	 */
	public HashMap<Long, PermissionGroup> getGroups() {
		return groups;
	}

	/**
	 * Gets a list of all currently registered permissions.
	 */
	public List<Permission> getPermissions() {
		return permissions;
	}

	/**
	 * Gets detailed information about a permission based on its name
	 * 
	 * @param name The name of the permission to get information on
	 * @return Information about the requested permission
	 */
	public Permission getPermission(String name) {
		String[] path = name.split(".");
		PermissionNode node = rootNode;
		pathSearch: for (int i = 0; i < path.length; i++) {
			if (i == path.length - 1) {
				for (Permission p : permissions) {
					if (p.getName().equalsIgnoreCase(path[i]) && p.getNode() == node) {
						return p;
					}
				}
			} else {
				for (PermissionNode n : nodes) {
					if (n.getName().equalsIgnoreCase(path[i]) && n.getNode() == node) {
						node = n;
						continue pathSearch;
					}
				}
				return null;
			}
		}
		return null;
	}
	
	public Permission getPermissionByName(String name) {
		String[] path = name.split(".");
		String PermName = path[path.length-1];
		String PermNode = "";
		if (path.length > 1) {
			PermNode = path[path.length-2];
		}
		for (Permission perm : permissions) {
			if (perm.getName() == PermName && (PermNode != "" || perm.getNode().getName() == PermNode))
				return perm;
		}
		return null;
	}

	/**
	 * Gets a list of all currently registered permission nodes
	 */
	public List<PermissionNode> getNodes() {
		return nodes;
	}

	/**
	 * Gets detailed information about a permission node based on its name
	 * 
	 * @param name The name of the permission node to get information on
	 * @return Information about the requested permission node
	 */
	public PermissionNode getNode(String name) {
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
			ResultSet r = GoldenApple.getInstance().database.executeQuery("SELECT ID FROM Users WHERE Name=?", new Object[] { name });
			if (r.next()) {
				long id = r.getLong("ID");
				r.close();
				return id;
			} else {
				r.close();
				return -1;
			}
		} catch (SQLException e) {
			GoldenApple.log(Level.WARNING, "Failed to retrieve ID for user '" + name + "':");
			GoldenApple.log(Level.WARNING, e);
			return -1;
		}
	}

	/**
	 * Retrieves a user instance based off of their user ID. Without the use of
	 * {@link PermissionManager#setSticky(long id, boolean sticky)}, the return
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
				ResultSet r = GoldenApple.getInstance().database.executeQuery("SELECT ID, Name, Locale, Permissions FROM Users WHERE ID=?", new Object[] { id });
				if (r.next()) {
					PermissionUser u = new PermissionUser(r.getLong("ID"), r.getString("Name"), r.getString("Locale"), r.getString("Permissions"));
					r.close();
					return u;
				} else {
					r.close();
					return null;
				}
			} catch (SQLException e) {
				GoldenApple.log(Level.WARNING, "Failed to retrieve user with ID " + id + ":");
				GoldenApple.log(Level.WARNING, e);
				return null;
			}
		}
	}

	/**
	 * Retrieves a user instance based off of their username. Without the use of
	 * {@link PermissionManager#setSticky(long id, boolean sticky)}, the return
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
			ResultSet r = GoldenApple.getInstance().database.executeQuery("SELECT ID, Name, Locale, Permissions FROM Users WHERE Name=?", new Object[] { name });
			if (r.next()) {
				PermissionUser p = new PermissionUser(r.getLong("ID"), r.getString("Name"), r.getString("Locale"), r.getString("Permissions"));
				r.close();
				userCache.put(p.getId(), p);
				cacheOut.addLast(p.getId());
				if (cacheOut.size() > 10) {
					long id = cacheOut.pop();
					userCache.remove(id);
				}
				return p;
			} else {
				r.close();
				return null;
			}
		} catch (SQLException e) {
			GoldenApple.log(Level.WARNING, "Failed to retrieve user with name '" + name + "':");
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
	public void setSticky(long id, boolean sticky) {
		if (userCache.containsKey(id)) {
			if (sticky && cacheOut.contains(id)) {
				cacheOut.remove(id);
			} else if (!sticky && !cacheOut.contains(id)) {
				cacheOut.addLast(id);
				if (cacheOut.size() > 10) {
					long id2 = cacheOut.pop();
					userCache.remove(id2);
				}
			}
		} else {
			throw new NullPointerException("Cannot change a user's stickyness before they are loaded into the cache!");
		}
	}

	/**
	 * Checks whether or not a user with a given ID is currently marked as
	 * sticky. (See {@link PermissionManager#setSticky(long id, boolean sticky)}
	 * for info on stickyness)
	 * 
	 * @param id The ID of the user to check
	 */
	public boolean isSticky(long id) {
		return userCache.containsKey(id) && !cacheOut.contains(id);
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
		if (groups.containsKey(id)) {
			return groups.get(id);
		} else {
			return null;
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
		for (Map.Entry<Long, PermissionGroup> group : groups.entrySet()) {
			if (group.getValue().getName().equalsIgnoreCase(name)) {
				return group.getValue();
			}
		}
		return null;
	}

	/**
	 * Checks whether or not a user exists in the database
	 * 
	 * @param name The name to check the database against
	 */
	public boolean userExists(String name) throws SQLException {
		ResultSet r = GoldenApple.getInstance().database.executeQuery("SELECT NULL FROM Users WHERE Name=?", new Object[] { name });
		if (r.next()) {
			r.close();
			return true;
		} else {
			r.close();
			return false;
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
			if (userExists(name)) {
				return getUser(name);
			} else {
				ResultSet r = null;
				long id = -1;
				do {
					id++;
					if (r != null)
						r.close();
					r = GoldenApple.getInstance().database.executeQuery("SELECT NULL FROM Users WHERE ID=?", new Object[] { id });
				} while (r.next());
				r.close();
				GoldenApple.getInstance().database.execute("INSERT INTO Users (ID, Name, Locale, Permissions) VALUES (?, ?, '', ?)", new Object[] { id, name, Serializer.serialize(new ArrayList<String>()) });
				return getUser(id);
			}
		} catch (SQLException | IOException e) {
			GoldenApple.log(Level.WARNING, "Failed to create new user:");
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
		ResultSet r = GoldenApple.getInstance().database.executeQuery("SELECT NULL FROM Groups WHERE Name=?", new Object[] { name });
		if (r.next()) {
			r.close();
			return true;
		} else {
			r.close();
			return false;
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
			if (groupExists(name)) {
				return getGroup(name);
			} else {
				ResultSet r = null;
				long id = -1;
				do {
					id++;
					if (r != null)
						r.close();
					r = GoldenApple.getInstance().database.executeQuery("SELECT NULL FROM Groups WHERE ID=?", new Object[] { id });
				} while (r.next());
				r.close();
				GoldenApple.getInstance().database.execute("INSERT INTO Groups (ID, Name, Permissions, Users, Groups) VALUES (?, ?, ?, ?, ?)", new Object[] { id, name, Serializer.serialize(new ArrayList<String>()), Serializer.serialize(new ArrayList<Long>()), Serializer.serialize(new ArrayList<Long>()) });
				PermissionGroup g;
				groups.put(id, g = new PermissionGroup(id, name));
				return g;
			}
		} catch (SQLException | IOException e) {
			GoldenApple.log(Level.WARNING, "Failed to create new group:");
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
		} else if (isSticky(id)) {
			throw new UnsupportedOperationException("Cannot delete a sticky user");
		}
		if (userCache.containsKey(id)) {
			userCache.remove(id);
		}
		if (cacheOut.contains(id)) {
			cacheOut.remove(id);
		}
		GoldenApple.getInstance().database.execute("DELETE FROM Users WHERE ID=?", id);
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
		if (groups.containsKey(id)) {
			groups.remove(id);
		}
		GoldenApple.getInstance().database.execute("DELETE FROM Groups WHERE ID=?", id);
	}

	/**
	 * Closes the database connection and flushes the cache
	 */
	public void close() {
		userCache = null;
		groups = null;
		permissions = null;
		nodes = null;
		rootNode = null;
	}

	/**
	 * Represents a specific permission in the GoldenApple permissions system
	 * 
	 * @author Deaboy
	 */
	public class Permission {
		private String			name;
		private PermissionNode	node;

		private Permission(String name, PermissionNode node) {
			this.name = name;
			this.node = node;
		}

		/**
		 * Gets the full name (including node name) of the permission
		 * represented by this object
		 */
		public String getFullName() {
			return node.getFullName() + "." + name;
		}

		/**
		 * Gets the short name (excluding node name) of the permission
		 * represented by this object
		 */
		public String getName() {
			return name;
		}

		/**
		 * Gets the parent node of this permission
		 */
		public PermissionNode getNode() {
			return node;
		}
	}

	/**
	 * Represents a specific permission node in the GoldenApple permissions
	 * system
	 * 
	 * @author Deaboy
	 */
	public class PermissionNode {
		private String			name;
		private PermissionNode	node;

		private PermissionNode(String name) {
			this.name = name;
			this.node = this;
		}

		private PermissionNode(String name, PermissionNode parentNode) {
			this.name = name;
			this.node = parentNode;
		}

		/**
		 * Gets the full name (including parent node) of the node represented by
		 * this object
		 */
		public String getFullName() {
			List<PermissionNode> previousNodes = new ArrayList<PermissionNode>();
			String path = name;
			PermissionNode currentNode = this;
			while (!previousNodes.contains(currentNode)) {
				previousNodes.add(currentNode);
				path = currentNode.getName() + "." + path;
				currentNode = currentNode.getNode();
			}
			return path;
		}

		/**
		 * Gets the short name (excluding parent node) of the node represented
		 * by this object
		 */
		public String getName() {
			return name;
		}

		/**
		 * Gets the parent node above this node (Returns this node if root node)
		 */
		public PermissionNode getNode() {
			return node;
		}

		/**
		 * Gets a list of all permissions that are directly under this node
		 * (does not search child nodes)
		 */
		public List<Permission> getPermissions() {
			List<Permission> currentPermissions = new ArrayList<Permission>();
			for (Permission p : permissions) {
				if (p.node == this)
					currentPermissions.add(p);
			}
			return currentPermissions;
		}
	}
}
