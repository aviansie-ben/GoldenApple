package com.bendude56.goldenapple.warp;

import java.sql.ResultSet;
import java.sql.SQLException;

import org.bukkit.Bukkit;
import org.bukkit.Location;

import com.bendude56.goldenapple.GoldenApple;
import com.bendude56.goldenapple.User;
import com.bendude56.goldenapple.permissions.PermissionUser;

public class HomeWarp extends BaseWarp {
	private long userId;
	private int homeNum;
	private String alias;
	private boolean isPublic;
	
	public HomeWarp(ResultSet r) throws SQLException {
		userId = r.getLong("UserID");
		homeNum = r.getInt("Home");
		alias = r.getString("Alias");
		isPublic = r.getBoolean("Public");
		loc = new Location(Bukkit.getWorld(r.getString("World")), r.getDouble("X"), r.getDouble("Y"), r.getDouble("Z"), r.getFloat("Yaw"), r.getFloat("Pitch"));
	}
	
	public HomeWarp(long userId, int homeNum, Location loc) {
		this(userId, homeNum, loc, null, false);
	}
	
	public HomeWarp(long userId, int homeNum, Location loc, String alias, boolean isPublic) {
		this.userId = userId;
		this.homeNum = homeNum;
		this.loc = loc;
		this.alias = alias;
		this.isPublic = isPublic;
	}

	@Override
	public String getDisplayName() {
		return (alias == null) ? ("Home #" + homeNum) : alias;
	}
	
	public PermissionUser getUser() {
		return GoldenApple.getInstance().permissions.getUser(userId);
	}
	
	public int getHomeNumber() {
		return homeNum;
	}
	
	public String getAlias() {
		return alias;
	}
	
	public void setAlias(String alias) {
		this.alias = alias;
	}
	
	public boolean isPublic() {
		return isPublic;
	}
	
	public void setPublic(boolean isPublic) {
		this.isPublic = isPublic;
	}

	@Override
	public boolean canTeleport(User u) {
		if (u.getId() == userId && u.hasPermission(WarpManager.homeTpOwn))
			return true;
		else if (isPublic && u.hasPermission(WarpManager.homeTpPublic))
			return true;
		else if (u.hasPermission(WarpManager.homeTpAll))
			return true;
		else
			return false;
	}

	@Override
	public boolean canEdit(User u) {
		if (u.getId() == userId && u.hasPermission(WarpManager.homeEditOwn))
			return true;
		else if (isPublic && u.hasPermission(WarpManager.homeEditPublic))
			return true;
		else if (u.hasPermission(WarpManager.homeEditAll))
			return true;
		else
			return false;
	}
	
	@Override
	public void update() throws SQLException {
		GoldenApple.getInstance().database.execute("UPDATE Homes SET Alias=?, X=?, Y=?, Z=?, Yaw=?, Pitch=?, World=?, Public=? WHERE UserID=? AND Home=?", alias, loc.getX(), loc.getY(), loc.getZ(), loc.getYaw(), loc.getPitch(), loc.getWorld().getName(), isPublic, userId, homeNum);
	}
	
	@Override
	public void insert() throws SQLException {
		GoldenApple.getInstance().database.execute("INSERT INTO Homes (UserID, Home, Alias, X, Y, Z, Yaw, Pitch, World, Public) VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?)", userId, homeNum, alias, loc.getX(), loc.getY(), loc.getZ(), loc.getYaw(), loc.getPitch(), loc.getWorld().getName(), isPublic);
	}
	
	@Override
	public void delete() throws SQLException {
		GoldenApple.getInstance().database.execute("DELETE FROM Homes WHERE UserID=? AND Home=?", userId, homeNum);
	}

}
