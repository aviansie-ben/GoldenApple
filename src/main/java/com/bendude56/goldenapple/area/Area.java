package com.bendude56.goldenapple.area;

import org.bukkit.Location;
import org.bukkit.World;

import com.bendude56.goldenapple.util.Calculations;

/**
 * The basic area object that manages land from which other area-based classes
 * inherit properties from.
 * 
 * @author Deaboy
 * 
 */
public class Area {
	private Long		ID;
	private Location	corner1;
	private Location	corner2;
	private boolean		ignoreY;
	private boolean		disabled;
	
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

	public boolean getDisabled() {
		return disabled;
	}

	public void setCorner1(Location c) {
		corner1 = c;
	}
	
	public void setCorner2(Location c) {
		corner2 = c;
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

	public boolean insideArea(Location location) {
		if (this.disabled) {
			return false;
		}
		if (this.getWorld() != location.getWorld()) {
			return false;
		}
		if (Calculations.isBetween(corner1.getX(), location.getX(), corner2.getX()) && Calculations.isBetween(corner1.getZ(), location.getZ(), corner2.getZ()) && (!ignoreY || Calculations.isBetween(corner1.getY(), location.getY(), corner2.getY()))) {
			return true;
		} else {
			return false;
		}
	}
}
