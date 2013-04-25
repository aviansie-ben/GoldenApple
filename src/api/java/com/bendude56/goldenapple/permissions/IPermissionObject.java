package com.bendude56.goldenapple.permissions;

import java.util.List;

import com.bendude56.goldenapple.permissions.PermissionManager.Permission;

public interface IPermissionObject {
	/**
	 * Gets the ID associated with this instance.
	 * <em>Where applicable, this value should be stored in place of the name.</em>
	 */
	public long getId();

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
	public List<Permission> getPermissions(boolean inherited);

	/**
	 * Checks whether this permission object has a given permission.
	 * 
	 * @param permission The permission to check for.
	 * @return True if the user has the specified permission, false otherwise.
	 */
	public boolean hasPermission(String permission);

	/**
	 * Checks whether this permission object has a given permission.
	 * 
	 * @param permission The permission to check for.
	 * @return True if the permission object has the specified permission, false
	 *         otherwise.
	 */
	public boolean hasPermission(Permission permission);

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
	public boolean hasPermission(String permission, boolean specific);

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
	public boolean hasPermission(Permission permission, boolean specific);
	
	/**
	 * Checks whether this permission object has been specifically granted a
	 * permission. Does not check for any star permissions.
	 * 
	 * @param permission The permission to check for.
	 * @return True if the permission object has the specified permission
	 *         specifically, false otherwise.
	 */
	public boolean hasPermissionSpecific(Permission permission);

	/**
	 * Grants this permission object a given permission and saves the object's
	 * data automatically. If the permission object already has this permission,
	 * nothing will occur.
	 * 
	 * @param permission The permission that the object should be granted
	 */
	public void addPermission(Permission permission);

	/**
	 * Grants this permission object a given permission and saves the object's
	 * data automatically. If the permission object already has this permission,
	 * nothing will occur.
	 * 
	 * @param permission The permission that the object should be granted
	 */
	public void addPermission(String permission);

	/**
	 * Revokes a given permission from this permission object and save's the
	 * objects's data automatically. If the permission object doesn't have the
	 * specified permission, nothing will occur.
	 * 
	 * @param permission The permission that should be revoked from this object
	 */
	public void removePermission(Permission permission);

	/**
	 * Revokes a given permission from this permission object and save's the
	 * objects's data automatically. If the permission object doesn't have the
	 * specified permission, nothing will occur.
	 * 
	 * @param permission The permission that should be revoked from this object
	 */
	public void removePermission(String permission);
	
	/**
	 * Gets a list of the IDs for all parent groups that contain this object.
	 * 
	 * @param directOnly When false, any groups that are parents of the parent
	 *                   groups of this object are included as well.
	 * @return A list of the IDs of all parent groups
	 */
	public List<Long> getParentGroups(boolean directOnly);

}
