package com.bendude56.goldenapple.permissions;

import java.util.ArrayList;
import java.util.List;

public class PermissionManager {
	
	//DECLARATIONS
	private List<User> users = new ArrayList<User>();
	private List<Group> groups = new ArrayList<Group>();
	private List<Permission> permissions = new ArrayList<Permission>();
	private List<PermissionNode> nodes = new ArrayList<PermissionNode>();
	
	private PermissionNode rootNode;
	//END DECLARATIONS
	
	//CONSTRUCTORS
	public PermissionManager() {
		rootNode = new PermissionNode("root");
		nodes.add(rootNode);
	}
	//END CONSTRUCTORS
	
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
	
	public Permission registerPermission(String fullName) {
		String[] name = fullName.split(".");
		PermissionNode node = rootNode;
		
		for (int i = 0; i < name.length; i++) {
			if (i == name.length-1) {
				return registerPermission(name[i], node);
			} else {
				node = registerNode(name[i], node);
			}
		}
		return null;
	}
	
	public PermissionNode registerNode(String name, PermissionNode node) {
		if (nodes.contains(node)) {
			for (PermissionNode n : nodes) {
				if (n.getName().equalsIgnoreCase(name) && n.getNode()==node) {
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
	
	
	
	// -- SIMPLE AND ROUTINE METHODS AND FUNCTIONS -- //
	
	public List<User> getUsers() {
		return users;
	}
	
	public List<Group> getGroups() {
		return groups;
	}
	
	public List<Permission> getPermissions() {
		return permissions;
	}
	
	public Permission getPermission(String name) {
		String[] path = name.split(".");
		PermissionNode node = rootNode;
		Permission permission = null;
		boolean found = false;
		
		for (int i = 0; i < path.length; i++) {
			if (i == path.length-1) {
				for (Permission p : permissions) {
					if (p.getName().equalsIgnoreCase(path[i]) && p.getNode()==node) {
						permission = p;
					}
				}
			} else {
				found = false;
				for (PermissionNode n : nodes) {
					if (n.getName().equalsIgnoreCase(path[i]) && n.getNode()==node) {
						node = n;
						found = true;
					}
				}
				if (found == false) {
					return null;
				}
			}
		}
		return permission;
	}
	
	public List<PermissionNode> getNodes() {
		return nodes;
	}
	
	public PermissionNode getNode(String name) {
		String[] path = name.split(".");
		PermissionNode node = rootNode;
		boolean found = false;
		
		for (int i = 0; i < path.length; i++) {
			found = false;
			for (PermissionNode n : nodes) {
				if (n.getName().equalsIgnoreCase(path[i]) && n.getNode()==node) {
					node = n;
					found = true;
				}
			}
			if (found == false) {
				return null;
			}
		}
		return node;
	}
	
	public PermissionNode getRootNode() {
		return rootNode;
	}
}