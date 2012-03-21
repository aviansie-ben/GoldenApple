package com.bendude56.goldenapple.permissions;

import java.util.ArrayList;
import java.util.List;

public class PermissionGroup {
	
	private String name;
	private List<PermissionUser> members = new ArrayList<PermissionUser>();
	private List<PermissionGroup> subGroups = new ArrayList<PermissionGroup>();
	
	public String getName() {
		return name;
	}
	
	public List<PermissionUser> getMembers() {
		return members;
	}
	
	public List<PermissionGroup> getSubGroups() {
		return subGroups;
	}
}