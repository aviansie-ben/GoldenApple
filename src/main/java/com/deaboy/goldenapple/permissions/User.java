package com.deaboy.goldenapple.permissions;

import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;
import org.bukkit.entity.Player;

public class User {
	
	private String name;
	private Player handle;
	
	
	
	// -- SIMPLE, ROUTINE METHODS AND FUNCTIONS -- //
	
	public String getName() {
		return name;
	}
	
	public Player getHandle() {
		return handle;
	}
	
	public OfflinePlayer getOfflineHandle() {
		return Bukkit.getOfflinePlayer(this.name);
	}
}