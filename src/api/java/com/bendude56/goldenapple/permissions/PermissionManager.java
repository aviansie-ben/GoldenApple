package com.bendude56.goldenapple.permissions;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

public abstract class PermissionManager {
	public static final String[]		devs	= new String[] { "ben_dude56", "Deaboy" };

	// goldenapple
	public static PermissionNode		goldenAppleNode;
	public static Permission			importPermission;

	// goldenapple.permissions
	public static PermissionNode		permissionNode;

	// goldenapple.permissions.user
	public static PermissionNode		userNode;
	public static Permission			userAddPermission;
	public static Permission			userRemovePermission;
	public static Permission			userEditPermission;

	// goldenapple.permissions.group
	public static PermissionNode		groupNode;
	public static Permission			groupAddPermission;
	public static Permission			groupRemovePermission;
	public static Permission			groupEditPermission;

	// goldenapple.module
	public static PermissionNode		moduleNode;
	public static Permission			moduleLoadPermission;
	public static Permission			moduleUnloadPermission;
	public static Permission            moduleClearCachePermission;
	public static Permission			moduleQueryPermission;

	protected static PermissionManager	instance;

	public static PermissionManager getInstance() {
		return instance;
	}

	public abstract Permission registerPermission(String name, PermissionNode node);
	public abstract Permission registerPermission(String fullName);
	public abstract PermissionNode registerNode(String name, PermissionNode node);

	public abstract List<Permission> getRegisteredPermissions();
	public abstract List<PermissionNode> getRegisteredNodes();

	public abstract Permission getPermissionByName(String name);
	public abstract PermissionNode getNodeByName(String name);
	public abstract PermissionNode getRootNode();
	public abstract Permission getRootStar();

	public abstract long getUserId(String name);
	public abstract IPermissionUser getUser(String name);
	public abstract IPermissionUser getUser(long id);
	public abstract boolean userExists(String name) throws SQLException;

	public abstract IPermissionUser createUser(String name);
	public abstract void deleteUser(long id) throws SQLException;

	public abstract void setUserSticky(long id, boolean sticky);
	public abstract boolean isUserSticky(long id);

	public abstract IPermissionGroup getGroup(String name);
	public abstract IPermissionGroup getGroup(long id);
	public abstract boolean groupExists(String name);
	
	public abstract IPermissionGroup createGroup(String name);
	public abstract void deleteGroup(long id) throws SQLException;
	
	public abstract void close();
	public abstract void clearCache();

	/**
	 * Represents a specific permission in the GoldenApple permissions system
	 * 
	 * @author Deaboy
	 */
	public class Permission {
		private String			name;
		private PermissionNode	node;

		public Permission(String name, PermissionNode node) {
			this.name = name;
			this.node = node;
		}

		/**
		 * Gets the full name (including node name) of the permission
		 * represented by this object
		 */
		public String getFullName() {
			if (node == getRootNode())
				return name;
			else
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

		public PermissionNode(String name) {
			this.name = name;
			this.node = this;
		}

		public PermissionNode(String name, PermissionNode parentNode) {
			this.name = name;
			this.node = parentNode;
		}

		/**
		 * Gets the full name (including parent node) of the node represented by
		 * this object
		 */
		public String getFullName() {
			String path = name;
			PermissionNode currentNode = node;
			while (currentNode != getRootNode()) {
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
			for (Permission p : getRegisteredPermissions()) {
				if (p.node == this)
					currentPermissions.add(p);
			}
			return currentPermissions;
		}
	}
}
