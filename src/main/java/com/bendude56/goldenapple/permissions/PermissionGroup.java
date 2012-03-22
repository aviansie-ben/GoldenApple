package com.bendude56.goldenapple.permissions;

import java.util.ArrayList;
import java.util.List;

import com.bendude56.goldenapple.permissions.PermissionManager.Permission;

public class PermissionGroup {
	
	private String name;
	private List<PermissionUser> members = new ArrayList<PermissionUser>();
	private List<PermissionGroup> subGroups = new ArrayList<PermissionGroup>();
	private List<Permission> permissions = new ArrayList<Permission>();
	
	public String getName() {
		return name;
	}
	
	public List<PermissionUser> getMembers() {
		return members;
	}
	
	/**
	 * Returns a list of groups that inherit this group's permissions.
	 */
	public List<PermissionGroup> getSubGroups() {
		return subGroups;
	}
	
	/**
	 * Returns an ArrayList of permissions this group has.
	 * @param inherited Set to true if you want to include inherited permissions 
	 * @return The permissions this group has
	 */
	public List<Permission> getPermissions(boolean inherited) {
		List<Permission> returnPermissions = permissions;
				
		/*if (inherited) {
			List<PermissionGroup> previousGroups = new ArrayList<PermissionGroup>();
			int checkedGroups = 1;
			
			while (checkedGroups > 0) {
				checkedGroups = 0;
				for (PermissionGroup group)
			}
		}*/
		
		return returnPermissions;
	}
}