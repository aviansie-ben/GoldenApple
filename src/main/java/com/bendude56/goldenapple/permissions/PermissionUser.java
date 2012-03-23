package com.bendude56.goldenapple.permissions;

import java.util.ArrayList;
import java.util.List;

import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;

import com.bendude56.goldenapple.permissions.PermissionManager.Permission;

public class PermissionUser {
	private String				name;
	private List<Permission>	permissions	= new ArrayList<Permission>();

	protected PermissionUser(String name) {
		this.name = name;
	}

	public String getName() {
		return name;
	}

	public OfflinePlayer getOfflinePlayer() {
		return Bukkit.getOfflinePlayer(this.name);
	}

	/**
	 * Returns an ArrayList of permissions this group has.
	 * 
	 * @param inherited Set to true if you want to include inherited permissions
	 * @return The permissions this group has
	 */
	public List<Permission> getPermissions(boolean inherited) {
		List<Permission> returnPermissions = permissions;
		/*
		 * if (inherited) { List<PermissionGroup> previousGroups = new
		 * ArrayList<PermissionGroup>(); int checkedGroups = 1;
		 * 
		 * while (checkedGroups > 0) { checkedGroups = 0; for (PermissionGroup
		 * group) } }
		 */
		return returnPermissions;
	}
}
