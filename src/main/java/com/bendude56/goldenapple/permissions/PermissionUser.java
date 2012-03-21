package com.bendude56.goldenapple.permissions;

import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;

public class PermissionUser {
	private String name;
	
	protected PermissionUser(String name) {
		this.name = name;
	}
	
	public String getName() {
		return name;
	}
	
	public OfflinePlayer getOfflinePlayer() {
		return Bukkit.getOfflinePlayer(this.name);
	}
}