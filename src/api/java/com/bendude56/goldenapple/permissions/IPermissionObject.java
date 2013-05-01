package com.bendude56.goldenapple.permissions;

import java.util.List;

import com.bendude56.goldenapple.permissions.PermissionManager.Permission;

/**
 * Represents a GoldenApple object that can have permissions assigned to it.
 * Usually, these objects will be saved into a database; it is safe to assume
 * that any changes made to objects of this type will be persistent between
 * server reboots.
 * 
 * @author ben_dude56
 */
public interface IPermissionObject {
	/**
	 * Gets the ID associated with this permission object. This ID is only
	 * unique within the specific type of permission object being checked,
	 * however it is guaranteed not to conflict with another permission object
	 * of the same type.
	 */
	public long getId();

	/**
	 * Gets a list of permissions that this object has been granted. This list
	 * should only be used for displaying existing permissions, never for
	 * checking permissions!
	 * 
	 * @param inherited False to return only direct permissions. True to include
	 *            permissions inherited from parent groups.
	 */
	public List<Permission> getPermissions(boolean inherited);

	/**
	 * Checks if this object has a specific permission. Where possible, a
	 * {@link Permission} should be used in place of a string.
	 * 
	 * @param permission The name of the permission to check against.
	 * @return True if the object has the specified permission, false otherwise.
	 * 
	 * @throws NullPointerException The permission specified does not exist.
	 */
	public boolean hasPermission(String permission);

	/**
	 * Checks if this object has a specific permission.
	 * 
	 * @param permission The permission to check against.
	 * @return True if the object has the specified permission, false otherwise.
	 */
	public boolean hasPermission(Permission permission);

	/**
	 * Checks if this object has a specific permission. Where possible, a
	 * {@link Permission} should be used in place of a string.
	 * 
	 * @param permission The name of the permission to check against.
	 * @param inherited When set to false, all permissions inherited from parent
	 *            groups will be ignored.
	 * @return True if the object has the specified permission, false otherwise.
	 * 
	 * @throws NullPointerException The permission specified does not exist.
	 */
	public boolean hasPermission(String permission, boolean inherited);

	/**
	 * Checks if this object has a specific permission.
	 * 
	 * @param permission The permission to check against.
	 * @param inherited When set to false, all permissions inherited from parent
	 *            groups will be ignored.
	 * @return True if the object has the specified permission, false otherwise.
	 */
	public boolean hasPermission(Permission permission, boolean inherited);

	/**
	 * Checks explicitly for a permission. When using this method, inherited
	 * permissions and star permissions will be ignored and a check will be done
	 * only against the specified permission.
	 * 
	 * @param permission The permission to check against.
	 * @return True if this object explicitly has the specified permission,
	 *         false otherwise.
	 */
	public boolean hasPermissionSpecific(Permission permission);

	/**
	 * Grants this object the specified permission. This method will not perform
	 * any action if the object already has the specified permission.
	 * 
	 * @param permission The permission that this object should be granted.
	 */
	public void addPermission(Permission permission);

	/**
	 * Grants this object the specified permission. This method will not perform
	 * any action if the object already has the specified permission. Where
	 * possible, a {@link Permission} should be used in place of a string.
	 * 
	 * @param permission The name of the permission that this object should be
	 *            granted.
	 * 
	 * @throws NullPointerException The permission specified does not exist.
	 */
	public void addPermission(String permission);

	/**
	 * Removes the specified permission from this object. If this object doesn't
	 * have the specified permission, no action will be performed.
	 * 
	 * @param permission The permission that should be removed from this object.
	 */
	public void removePermission(Permission permission);

	/**
	 * Removes the specified permission from this object. If this object doesn't
	 * have the specified permission, no action will be performed. Where
	 * possible, a {@link Permission} should be used in place of a string.
	 * 
	 * @param permission The name of the permission that should be removed from
	 *            this object.
	 * 
	 * @throws NullPointerException The permission specified does not exist.
	 */
	public void removePermission(String permission);

	/**
	 * Retrieves a list of groups that this object inherits permissions from.
	 * 
	 * @param directOnly When true, only groups that this object is a direct
	 *            child of will be returned. When false, parents will be
	 *            recursively checked and all parents will be returned.
	 * 
	 * @return A list of IDs for groups that this object inherits from.
	 */
	public List<Long> getParentGroups(boolean directOnly);

}
