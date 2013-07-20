package com.bendude56.goldenapple.warp;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

import org.bukkit.Bukkit;
import org.bukkit.Location;

import com.bendude56.goldenapple.GoldenApple;
import com.bendude56.goldenapple.User;
import com.bendude56.goldenapple.permissions.PermissionGroup;

public class NamedWarp extends PermissibleWarp {
	private String name;
	private List<Long> groups;
	
	public NamedWarp(String name, Location loc) {
		setLocation(loc);
		
		this.name = name;
		groups = new ArrayList<Long>();
	}
	
	public NamedWarp(ResultSet r) throws SQLException {
		setLocation(new Location(Bukkit.getWorld(r.getString("World")), r.getDouble("X"), r.getDouble("Y"), r.getDouble("Z"), r.getFloat("Yaw"), r.getFloat("Pitch")));
		
		name = r.getString("Name");
		loadGroups();
	}
	
	private void loadGroups() throws SQLException {
		groups = new ArrayList<Long>();
		
		ResultSet r = GoldenApple.getInstanceDatabaseManager().executeQuery("SELECT * FROM WarpGroups WHERE Warp=?", name);
		try {
			while (r.next()) {
				groups.add(r.getLong("GroupID"));
			}
		} finally {
			GoldenApple.getInstanceDatabaseManager().closeResult(r);
		}
	}

	@Override
	public String getDisplayName() {
		return name;
	}

	@Override
	public boolean canTeleport(User u) {
		if (canEverybodyTeleport()) {
			return true;
		} else {
			List<Long> g = u.getParentGroups(false);
			
			for (Long group : g)
				if (groups.contains(group))
					return true;
			
			return false;
		}
	}

	@Override
	public boolean canEdit(User u) {
		return u.hasPermission(WarpManager.editPermission);
	}

	@Override
	public void update() throws SQLException {
		GoldenApple.getInstanceDatabaseManager().execute("UPDATE Warps SET X=?, Y=?, Z=?, Yaw=?, Pitch=?, World=? WHERE Name=?", loc.getX(), loc.getY(), loc.getZ(), loc.getYaw(), loc.getPitch(), loc.getWorld().getName(), name);
		
		GoldenApple.getInstanceDatabaseManager().execute("DELETE FROM WarpGroups WHERE Warp=?", name);
		
		for (long g : groups)
			GoldenApple.getInstanceDatabaseManager().execute("INSERT INTO WarpGroups (Warp, Group) VALUES (?, ?)", name, g);
	}

	@Override
	public void insert() throws SQLException {
		GoldenApple.getInstanceDatabaseManager().execute("INSERT INTO Warps (Name, X, Y, Z, Yaw, Pitch, World) VALUES (?, ?, ?, ?, ?, ?, ?)", name, loc.getX(), loc.getY(), loc.getZ(), loc.getYaw(), loc.getPitch(), loc.getWorld().getName());
		
		for (long g : groups)
			GoldenApple.getInstanceDatabaseManager().execute("INSERT INTO WarpGroups (Warp, Group) VALUES (?, ?)", name, g);
	}

	@Override
	public void delete() throws SQLException {
		GoldenApple.getInstanceDatabaseManager().execute("DELETE FROM Warps WHERE Name=?", name);
	}

	@Override
	public boolean canEverybodyTeleport() {
		return groups.size() == 0;
	}

	@Override
	public boolean canTeleport(PermissionGroup g) {
		return groups.contains(g.getId());
	}

	@Override
	public void addGroup(PermissionGroup g) {
		groups.add(g.getId());
		try {
			update();
		} catch (SQLException e) { }
	}

	@Override
	public void removeGroup(PermissionGroup g) {
		groups.remove(g.getId());
		try {
			update();
		} catch (SQLException e) { }
	}

}
