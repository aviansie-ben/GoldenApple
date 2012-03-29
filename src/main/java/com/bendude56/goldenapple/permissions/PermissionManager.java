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
			GoldenApple.getInstance().database.execute("CREATE TABLE IF NOT EXISTS Users (ID BIGINT, Name VARCHAR(128), Locale VARCHAR(128), Permissions TEXT)");
		} catch (SQLException e) {
			GoldenApple.log(Level.SEVERE, "Failed to create table 'Users':");
			GoldenApple.log(Level.SEVERE, e);
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

	public long getUserId(String name) {
		try {
			ResultSet r = GoldenApple.getInstance().database.executeQuery("SELECT ID FROM Users WHERE Name=?", new Object[] { name });
			if (r.first()) {
				r.close();
				return r.getLong("ID");
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

	public PermissionUser getUser(long id) {
		if (userCache.containsKey(id)) {
			return userCache.get(id);
		} else {
			try {
				ResultSet r = GoldenApple.getInstance().database.executeQuery("SELECT ID, Name, Locale, Permissions FROM Users WHERE ID=?", new Object[] { id });
				if (r.first()) {
					r.close();
					return new PermissionUser(r.getLong("ID"), r.getString("Name"), r.getString("Locale"), r.getString("Permissions"));
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
	
	public PermissionUser getUser(String name) {
		for (Map.Entry<Long, PermissionUser> entry : userCache.entrySet()) {
			if (entry.getValue().getName().equalsIgnoreCase(name)) {
				return entry.getValue();
			}
		}
		try {
			ResultSet r = GoldenApple.getInstance().database.executeQuery("SELECT ID, Name, Locale, Permissions FROM Users WHERE Name=?", new Object[] { name });
			if (r.first()) {
				r.close();
				PermissionUser p = new PermissionUser(r.getLong("ID"), r.getString("Name"), r.getString("Locale"), r.getString("Permissions"));
				userCache.put(p.getId(), p);
				cacheOut.addLast(p.getId());
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

	public PermissionGroup getGroup(long id) {
		if (getGroups().containsKey(id)) {
			return getGroups().get(id);
		} else {
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
			}
		} else {
			throw new NullPointerException("Cannot make a user sticky before they are loaded into the cache!");
		}
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
