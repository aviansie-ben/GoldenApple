package com.bendude56.goldenapple.permissions;

import java.io.IOException;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;

import com.bendude56.goldenapple.GoldenApple;
import com.bendude56.goldenapple.permissions.PermissionManager.Permission;
import com.bendude56.goldenapple.util.Serializer;

/**
 * Represents a group in the GoldenApple permissions database.
 * <p>
 * <em><strong>Note:</strong> Do not store direct references to this class. Store the
 * ID of the instance instead!</em>
 * 
 * @author Deaboy
 * @author ben_dude56
 */
public class PermissionGroup implements IPermissionObject {
	private long					id;
	private String					name;

	private PermissionGroup() {
	}

	/**
	 * Pushes any changes made to this group to the SQL database
	 */
	public void save() {
		try {
			GoldenApple.getInstance().database.execute("UPDATE Groups SET Name=? WHERE ID=?", name, id);
		} catch (SQLException e) {
			GoldenApple.log(Level.SEVERE, "Failed to save changes to group '" + name + "':");
			GoldenApple.log(Level.SEVERE, e);
		}
	}

	@Override
	public long getId() {
		return id;
	}

	/**
	 * Gets the name of the group represented by this instance.
	 */
	public String getName() {
		return name;
	}

	/**
	 * Gets a list of user IDs for users that inherit this group's permissions.
	 */
	public List<Long> getUsers() {
		// TODO Implement group membership
		return null;
	}
	
	public void addUser(IPermissionUser user) {
		// TODO Implement group membership
	}
	
	public void removeUser(IPermissionUser user) {
		// TODO Implement group membership
	}
	
	public boolean isMember(IPermissionUser user, boolean directOnly) {
		// TODO Implement group membership
		return false;
	}

	/**
	 * Gets a list of group IDs for groups that inherit this group's
	 * permissions.
	 */
	public List<Long> getGroups() {
		// TODO Implement group membership
		return null;
	}
	
	public void addGroup(PermissionGroup group) {
		// TODO Implement group membership
	}
	
	public void removeGroup(PermissionGroup group) {
		// TODO Implement group membership
	}

	@Override
	public List<Permission> getPermissions(boolean inherited) {
		// TODO Implement group permissions
		return null;
	}
	
	@Override
	public void addPermission(Permission permission) {
		// TODO Implement group permissions
	}
	
	@Override
	public void addPermission(String permission) {
		addPermission(GoldenApple.getInstance().permissions.getPermissionByName(permission));
	}
	
	@Override
	public void removePermission(Permission permission) {
		// TODO Implement group permissions
	}
	
	@Override
	public void removePermission(String permission) {
		removePermission(GoldenApple.getInstance().permissions.registerPermission(permission));
	}

	/**
	 * Checks whether this group has a given permission.
	 * 
	 * @param permission The permission to check for.
	 * @return True if the group has the specified permission, false otherwise.
	 */
	public boolean hasPermission(String permission) {
		return hasPermission(permission, false);
	}

	/**
	 * Checks whether this group has a given permission.
	 * 
	 * @param permission The permission to check for.
	 * @return True if the group has the specified permission, false otherwise.
	 */
	public boolean hasPermission(Permission permission) {
		return hasPermission(permission, false);
	}

	/**
	 * Checks whether this group has a given permission.
	 * 
	 * @param permission The permission to check for.
	 * @param specific Determines whether or not indirect permissions should be
	 *            considered. If true, only permissions given specifically to
	 *            this group will be checked. If false, all permissions
	 *            (including indirect permissions) will be considered.
	 * @return True if the group has the specified permission, false otherwise.
	 */
	public boolean hasPermission(String permission, boolean specific) {
		return hasPermission(GoldenApple.getInstance().permissions.registerPermission(permission), specific);
	}

	/**
	 * Checks whether this group has a given permission.
	 * 
	 * @param permission The permission to check for.
	 * @param specific Determines whether or not indirect permissions should be
	 *            considered. If true, only permissions given specifically to
	 *            this group will be checked. If false, all permissions
	 *            (including indirect permissions) will be considered.
	 * @return True if the group has the specified permission, false otherwise.
	 */
	public boolean hasPermission(Permission permission, boolean specific) {
		// TODO Implement group permissions
		return false;
	}

	@Override
	public boolean hasPermissionSpecific(Permission permission) {
		// TODO Implement group permissions
		return false;
	}

	@Override
	public List<Long> getParentGroups(boolean directOnly) {
		// TODO Implement group membership
		return null;
	}
}
