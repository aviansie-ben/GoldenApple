package com.bendude56.goldenapple.permissions;

public class Permission {
	
	//DECLARATIONS
	private String name;
	private PermissionNode node;
	//END DECLARATIONS
	
	//CONSTRUCTOR
	public Permission(String name, PermissionNode node) {
		this.name = name;
		this.node = node;
	}
	//END CONSTRUCTOR
	
	public String getFullName() {
		return name + "." + node.getFullName();
	}
	
	// -- SIMPLE AND ROUTINE METHODS AND FUNCTIONS -- //
	
	public String getName() {
		return name;
	}
	
	public PermissionNode getNode() {
		return node;
	}
}