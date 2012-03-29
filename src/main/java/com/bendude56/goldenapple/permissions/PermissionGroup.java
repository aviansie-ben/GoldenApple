package com.bendude56.goldenapple.permissions;

import java.util.ArrayList;
import java.util.List;

import com.bendude56.goldenapple.permissions.PermissionManager.Permission;

/**
 * Represents a group in the GoldenApple permissions database.
 * <p>
 * <em><strong>Note:</strong> Do not store direct references to this class. Store the
 * ID of the instance instead!</em>
 * 
 * @author Deaboy
 * @author ben_dude56
 */
public class PermissionGroup {
	private long				id;
	private String				name;
	private List<Long>			members		= new ArrayList<Long>();
	private List<Long>			subGroups	= new ArrayList<Long>();
	private List<Permission>	permissions	= new ArrayList<Permission>();

	/**
	 * Creates a new group with the provided ID and name.
	 * <p>
	 * <em><strong>Note:</strong> Before a group created in this way will save properly, you must alert
	 * the {@link PermissionManager} using the {@link PermissionManager#saveGroup(PermissionGroup group)}
	 * function.</em>
	 * 
	 * @param id The ID of the group to create. (To get the next available group
	 *            ID use {@link PermissionManager#nextGroupId()} function)
	 * @param name The name of the group to create.
	 */
	public PermissionGroup(long id, String name) {
		this.id = id;
		this.name = name;
		this.members = new ArrayList<Long>();
		this.subGroups = new ArrayList<Long>();
		this.permissions = new ArrayList<Permission>();
	}

	/**
	 * Gets the ID associated with this group.
	 */
	public Long getId() {
		return id;
	}

	/**
	 * Gets the name associated with this group.
	 */
	public String getName() {
		return name;
	}

	/**
	 * Gets a list of user IDs for users that inherit this group's permissions.
	 */
	public List<Long> getMembers() {
		return members;
	}

	/**
	 * Gets a list of group IDs for groups that inherit this group's
	 * permissions.
	 */
	public List<Long> getSubGroups() {
		return subGroups;
	}

	/**
	 * Gets a list of permissions that this group has been given.
	 * 
	 * @param inherited True to include inherited permissions. False to fetch
	 *            explicit permissions only.
	 */
	public List<Permission> getPermissions(boolean inherited) {
		List<Permission> returnPermissions = permissions;
		return returnPermissions;
	}
}
