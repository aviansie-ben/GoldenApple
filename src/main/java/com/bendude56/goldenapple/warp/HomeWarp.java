package com.bendude56.goldenapple.warp;

import java.sql.ResultSet;
import java.sql.SQLException;

import org.bukkit.Bukkit;
import org.bukkit.Location;

import com.bendude56.goldenapple.GoldenApple;

public class HomeWarp extends PlayerBoundWarp {
    private int homeNum;
    private String alias;
    
    public HomeWarp(ResultSet r) throws SQLException {
        super(r.getLong("UserID"), r.getBoolean("Public"), new Location(Bukkit.getWorld(r.getString("World")), r.getDouble("X"), r.getDouble("Y"), r.getDouble("Z"), r.getFloat("Yaw"), r.getFloat("Pitch")));
        
        homeNum = r.getInt("Home");
        alias = r.getString("Alias");
    }
    
    public HomeWarp(long owner, int homeNum, Location loc) {
        this(owner, homeNum, loc, null, false);
    }
    
    public HomeWarp(long owner, int homeNum, Location loc, String alias, boolean isPublic) {
        super(owner, isPublic, loc);
        
        this.homeNum = homeNum;
        this.alias = alias;
    }
    
    @Override
    public String getDisplayName() {
        return (alias == null) ? ("Home #" + homeNum) : alias;
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
    
    @Override
    public void update() throws SQLException {
        GoldenApple.getInstanceDatabaseManager().execute("UPDATE Homes SET Alias=?, X=?, Y=?, Z=?, Yaw=?, Pitch=?, World=?, Public=? WHERE UserID=? AND Home=?", alias, loc.getX(), loc.getY(), loc.getZ(), loc.getYaw(), loc.getPitch(), loc.getWorld().getName(), isPublic(), owner, homeNum);
    }
    
    @Override
    public void insert() throws SQLException {
        GoldenApple.getInstanceDatabaseManager().execute("INSERT INTO Homes (UserID, Home, Alias, X, Y, Z, Yaw, Pitch, World, Public) VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?)", owner, homeNum, alias, loc.getX(), loc.getY(), loc.getZ(), loc.getYaw(), loc.getPitch(), loc.getWorld().getName(), isPublic());
    }
    
    @Override
    public void delete() throws SQLException {
        GoldenApple.getInstanceDatabaseManager().execute("DELETE FROM Homes WHERE UserID=? AND Home=?", owner, homeNum);
    }
    
}
