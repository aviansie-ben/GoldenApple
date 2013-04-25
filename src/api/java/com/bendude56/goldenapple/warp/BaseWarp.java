package com.bendude56.goldenapple.warp;

import java.sql.SQLException;

import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.bukkit.event.player.PlayerTeleportEvent.TeleportCause;

import com.bendude56.goldenapple.User;

public abstract class BaseWarp {
	protected Location loc;
	
	public abstract String getDisplayName();
	public abstract boolean canTeleport(User u);
	public abstract boolean canEdit(User u);
	public abstract void update() throws SQLException;
	public abstract void insert() throws SQLException;
	public abstract void delete() throws SQLException;
	
	public Location getLocation() {
		return loc;
	}
	
	public void setLocation(Location loc) {
		this.loc = loc;
	}
	
	public void teleport(User u) {
		if (u.getHandle() instanceof Player) {
			u.getPlayerHandle().teleport(loc, TeleportCause.COMMAND);
		} else {
			throw new UnsupportedOperationException();
		}
	}
}
