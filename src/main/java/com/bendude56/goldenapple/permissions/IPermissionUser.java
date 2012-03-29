package com.bendude56.goldenapple.permissions;

import java.util.List;

import com.bendude56.goldenapple.permissions.PermissionManager.Permission;

public interface IPermissionUser {
	String getName();
	long getId();
	List<Permission> getPermissions(boolean inherited);
	boolean hasPermission(String permission);
	boolean hasPermission(Permission permission);
	boolean hasPermission(String permission, boolean specific);
	boolean hasPermission(Permission permission, boolean specific);
	String getPreferredLocale();
}
