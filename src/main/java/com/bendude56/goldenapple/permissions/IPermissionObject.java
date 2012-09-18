package com.bendude56.goldenapple.permissions;

import java.util.List;

import com.bendude56.goldenapple.permissions.PermissionManager.Permission;

public interface IPermissionObject {
	/**
	 * Gets the ID associated with this instance.
	 * <em>Where applicable, this value should be stored in place of the name.</em>
	 */
	long getId();

	/**
	 * Gets a list of permissions that have been granted to this permission
	 * object.
	 * <p>
	 * <em><strong>Note:</strong> This list should <strong>not</strong> be used
	 * to check for permissions!</em>
	 * 
	 * @param inherited Determines whether or not this user's group permissions
	 *            will be included in this list. If true, all applicable
	 *            permissions (including indirect permissions) will be returned.
	 *            If false, only permissions specifically given to this user
	 *            will be returned.
	 */
	List<Permission> getPermissions(boolean inherited);

	/**
	 * Checks whether this permission object has a given permission.
	 * 
	 * @param permission The permission to check for.
	 * @return True if the user has the specified permission, false otherwise.
	 */
	boolean hasPermission(String permission);

	/**
	 * Checks whether this permission object has a given permission.
	 * 
	 * @param permission The permission to check for.
	 * @return True if the permission object has the specified permission, false
	 *         otherwise.
	 */
	boolean hasPermission(Permission permission);

	/**
	 * Checks whether this permission object has a given permission.
	 * 
	 * @param permission The permission to check for.
	 * @param specific Determines whether or not indirect permissions should be
	 *            considered. If true, only permissions given specifically to
	 *            this permission object will be checked. If false, all
	 *            permissions (including indirect permissions) will be
	 *            considered.
	 * @return True if the permission object has the specified permission, false
	 *         otherwise.
	 */
	boolean hasPermission(String permission, boolean specific);

	/**
	 * Checks whether this permission object has a given permission.
	 * 
	 * @param permission The permission to check for.
	 * @param specific Determines whether or not indirect permissions should be
	 *            considered. If true, only permissions given specifically to
	 *            this permission object will be checked. If false, all
	 *            permissions (including indirect permissions) will be
	 *            considered.
	 * @return True if the permission object has the specified permission, false
	 *         otherwise.
	 */
	boolean hasPermission(Permission permission, boolean specific);

	/**
	 * Grants this permission object a given permission and saves the object's
	 * data automatically. If the permission object already has this permission,
	 * nothing will occur.
	 * 
	 * @param permission The permission that the object should be granted
	 */
	void addPermission(Permission permission);

	/**
	 * Grants this permission object a given permission and saves the object's
	 * data automatically. If the permission object already has this permission,
	 * nothing will occur.
	 * 
	 * @param permission The permission that the object should be granted
	 */
	void addPermission(String permission);

	/**
	 * Revokes a given permission from this permission object and save's the
	 * objects's data automatically. If the permission object doesn't have the
	 * specified permission, nothing will occur.
	 * 
	 * @param permission The permission that should be revoked from this object
	 */
	void removePermission(Permission permission);

	/**
	 * Revokes a given permission from this permission object and save's the
	 * objects's data automatically. If the permission object doesn't have the
	 * specified permission, nothing will occur.
	 * 
	 * @param permission The permission that should be revoked from this object
	 */
	void removePermission(String permission);

}
