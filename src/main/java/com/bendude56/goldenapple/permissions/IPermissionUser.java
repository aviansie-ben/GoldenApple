package com.bendude56.goldenapple.permissions;

import java.util.List;

import com.bendude56.goldenapple.permissions.PermissionManager.Permission;

public interface IPermissionUser {
	/**
	 * Gets the name of the user represented by this instance.
	 */
	String getName();

	/**
	 * Gets the ID associated with this instance.
	 * <em>Where applicable, this value should be stored in place of the user's name.</em>
	 */
	long getId();

	/**
	 * Gets a list of permissions that have been granted to this user.
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
	 * Checks whether this user has a given permission.
	 * 
	 * @param permission The permission to check for.
	 * @return True if the user has the specified permission, false otherwise.
	 */
	boolean hasPermission(String permission);

	/**
	 * Checks whether this user has a given permission.
	 * 
	 * @param permission The permission to check for.
	 * @return True if the user has the specified permission, false otherwise.
	 */
	boolean hasPermission(Permission permission);

	/**
	 * Checks whether this user has a given permission.
	 * 
	 * @param permission The permission to check for.
	 * @param specific Determines whether or not indirect permissions should be
	 *            considered. If true, only permissions given specifically to
	 *            this user will be checked. If false, all permissions
	 *            (including indirect permissions) will be considered.
	 * @return True if the user has the specified permission, false otherwise.
	 */
	boolean hasPermission(String permission, boolean specific);

	/**
	 * Checks whether this user has a given permission.
	 * 
	 * @param permission The permission to check for.
	 * @param specific Determines whether or not indirect permissions should be
	 *            considered. If true, only permissions given specifically to
	 *            this user will be checked. If false, all permissions
	 *            (including indirect permissions) will be considered.
	 * @return True if the user has the specified permission, false otherwise.
	 */
	boolean hasPermission(Permission permission, boolean specific);

	/**
	 * Gets the user's preferred locale, if they have specifically set one.
	 * 
	 * @return A string representing the user's preferred locale. If default,
	 *         will return an empty string.
	 */
	String getPreferredLocale();

	/**
	 * Grants this user a given permission and saves the user's data
	 * automatically. If the user already has this permission, nothing will
	 * occur.
	 * 
	 * @param permission The permission that the user should be granted
	 */
	void addPermission(Permission permission);

	/**
	 * Grants this user a given permission and saves the user's data
	 * automatically. If the user already has this permission, nothing will
	 * occur.
	 * 
	 * @param permission The permission that the user should be granted
	 */
	void addPermission(String permission);

	/**
	 * Revokes a given permission from this user and save's the user's data
	 * automatically. If the user doesn't have the specified permission, nothing
	 * will occur.
	 * 
	 * @param permission The permissions that should be revoked from this user
	 */
	void removePermission(Permission permission);

	/**
	 * Revokes a given permission from this user and save's the user's data
	 * automatically. If the user doesn't have the specified permission, nothing
	 * will occur.
	 * 
	 * @param permission The permissions that should be revoked from this user
	 */
	void removePermission(String permission);
}
