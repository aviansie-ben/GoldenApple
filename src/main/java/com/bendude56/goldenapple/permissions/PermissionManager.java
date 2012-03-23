package com.bendude56.goldenapple.permissions;

import java.util.ArrayList;
import java.util.List;

/**
 * Manages the inner workings of the GoldenApple permissions system
 * 
 * @author Deaboy
 * @author ben_dude56
 */
public class PermissionManager {
	private List<PermissionUser>	users		= new ArrayList<PermissionUser>();
	private List<PermissionGroup>	groups		= new ArrayList<PermissionGroup>();
	private List<Permission>		permissions	= new ArrayList<Permission>();
	private List<PermissionNode>	nodes		= new ArrayList<PermissionNode>();
	private PermissionNode			rootNode;

	public PermissionManager() {
		rootNode = new PermissionNode("");
		nodes.add(rootNode);
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
	public List<PermissionUser> getUsers() {
		return users;
	}

	/**
	 * Gets a list of all groups in the database.
	 */
	public List<PermissionGroup> getGroups() {
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
