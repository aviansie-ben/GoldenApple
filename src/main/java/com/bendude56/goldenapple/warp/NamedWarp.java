package com.bendude56.goldenapple.warp;

import java.sql.ResultSet;
import java.sql.SQLException;

import org.bukkit.Bukkit;
import org.bukkit.Location;

import com.bendude56.goldenapple.GoldenApple;
import com.bendude56.goldenapple.User;

public class NamedWarp extends PermissibleWarp {
	private String name;
	
	public NamedWarp(String name, Location loc) {
		setLocation(loc);
		
		this.name = name;
	}
	
	public NamedWarp(ResultSet r) throws SQLException {
		setLocation(new Location(Bukkit.getWorld(r.getString("World")), r.getDouble("X"), r.getDouble("Y"), r.getDouble("Z"), r.getFloat("Yaw"), r.getFloat("Pitch")));
		
		name = r.getString("Name");
	}

	@Override
	public String getDisplayName() {
		return name;
	}

	@Override
	public boolean canTeleport(User u) {
		return true;
	}

	@Override
	public boolean canEdit(User u) {
		return u.hasPermission(WarpManager.editPermission);
	}

	@Override
	public void update() throws SQLException {
		GoldenApple.getInstanceDatabaseManager().execute("UPDATE Warps SET X=?, Y=?, Z=?, Yaw=?, Pitch=?, World=? WHERE Name=?", loc.getX(), loc.getY(), loc.getZ(), loc.getYaw(), loc.getPitch(), loc.getWorld().getName(), name);
	}

	@Override
	public void insert() throws SQLException {
		GoldenApple.getInstanceDatabaseManager().execute("INSERT INTO Warps (Name, X, Y, Z, Yaw, Pitch, World) VALUES (?, ?, ?, ?, ?, ?, ?)", name, loc.getX(), loc.getY(), loc.getZ(), loc.getYaw(), loc.getPitch(), loc.getWorld().getName());
	}

	@Override
	public void delete() throws SQLException {
		GoldenApple.getInstanceDatabaseManager().execute("DELETE FROM Warps WHERE Name=?", name);
	}

}
