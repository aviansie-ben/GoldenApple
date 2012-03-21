package com.deaboy.goldenapple.permissions;

import java.util.ArrayList;
import java.util.List;

public class PermissionNode {
	
	//DECLARATIONS
	private String					name;
	private PermissionNode			node;	//The PermissionNode that this node resides in.
	private List<Permission>		permissions		= new ArrayList<Permission>();
	//END DECLARATIONS
	
	//CONSTRUCTORS
	public PermissionNode(String name) {
		this.name = name;
		this.node = this;
	}
	
	public PermissionNode(String name, PermissionNode parentNode) {
		this.name = name;
		this.node = parentNode;
	}
	//END CONSTRUCTORS
	
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
	
	
	
	// -- SIMPLE AND ROUTINE MOTHODS AND FUNCTIONS -- //
	
	public String getName() {
		return name;
	}
	
	public PermissionNode getNode() {
		return node;
	}
	
	public List<Permission> getPermissions() {
		return permissions;
	}
	
	public boolean addPermission(Permission permission) {
		if (!this.permissions.contains(permission)) {
			return this.permissions.add(permission);
		}
		return false;
	}
	
	public boolean removePermission(Permission permission) {
		if (this.permissions.contains(permission)) {
			return this.permissions.remove(permission);
		}
		return false;
	}
	
	public boolean removePermission(String name) {
		for (Permission permission : this.permissions) {
			if (permission.getName().equalsIgnoreCase(name)) {
				return this.permissions.remove(permission);
			}
		}
		return false;
	}
}