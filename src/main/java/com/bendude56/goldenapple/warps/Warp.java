package com.bendude56.goldenapple.warps;

import org.bukkit.Location;

/**
 * The primitive warp object for GoldenApple.
 * All subclasses of warps derive extend this one. 
 * @author Deaboy
 *
 */
public class Warp {
	private final long ID;
	private Location location;
	
	public Warp(long ID, Location location){
		this.ID = ID;
		this.location = location;
	}
	
	public long getId(){
		return ID;
	}
	
	public Location getLocation(){
		return location;
	}
	
	public void setLocation(Location location){
		this.location = location;
	}
}
