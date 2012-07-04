package com.bendude56.goldenapple.area;

import org.bukkit.Location;
import org.bukkit.World;

import com.bendude56.goldenapple.GoldenApple;
import com.bendude56.goldenapple.util.Calculations;
import com.bendude56.goldenapple.warps.AreaWarp;

/**
 * The basic area object that manages land from which other area-based classes
 * inherit properties from.
 * 
 * @author Deaboy
 * 
 */
public class Area {
	final Long			ID;
	
	private Location	corner1;
	private Location	corner2;
	
	private boolean		ignoreY;
	private boolean		disabled;
	
	public Area(Long ID, Location corner1, Location corner2, boolean ignoreY){
		this.ID = ID;
		this.corner1 = corner1;
		this.corner2 = corner2;
		this.ignoreY = ignoreY;
	}
	
	public Long getID() {
		return ID;
	}
	
	public Location getCorner1() {
		return corner1;
	}

	public Location getCorner2() {
		return corner1;
	}

	public boolean ignoreY() {
		return ignoreY;
	}

	public World getWorld() {
		return corner1.getWorld();
	}

	public boolean isDisabled() {
		return disabled;
	}

	public void setCorner1(Location c) {
		corner1 = c;
	}
	
	public void setCorner2(Location c) {
		corner2 = c;
	}
	
	public AreaWarp getWarp(){
		return GoldenApple.getInstance().warps.getAreaWarp(this);
	}
	
	public void ignoreY(boolean ignore) {
		ignoreY = ignore;
	}
	
	public void disable() {
		disabled = true;
	}
	
	public void enable() {
		disabled = false;
	}
	
	public boolean contains(Location location) {
		if (this.disabled) {
			return false;
		} else if (this.getWorld() != location.getWorld()) {
			return false;
		} else if (Calculations.isBetween(corner1.getX(), location.getX(), corner2.getX()) && Calculations.isBetween(corner1.getZ(), location.getZ(), corner2.getZ()) && (!ignoreY || Calculations.isBetween(corner1.getY(), location.getY(), corner2.getY()))) {
			return true;
		} else {
			return false;
		}
	}
}
