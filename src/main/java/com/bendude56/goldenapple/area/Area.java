package com.bendude56.goldenapple.area;

import org.bukkit.Location;
import org.bukkit.World;

import com.bendude56.goldenapple.GoldenApple;

/**
 * The basic area object that manages land from which other area-based classes inherit properties from.
 * @author Danny
 *
 */
public class Area {
	private Location corner1;
	private Location corner2;
	private World world;
	private boolean disabled;
	private boolean ignoreY;
	private boolean isWorldwide;
	
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
		return world;
	}
	
	public boolean insideArea(Location location) {
		if (this.disabled) {
			return false;
		}
		if (this.isWorldwide && location.getWorld() == world) {
			return true;
		}
		if (this.getWorld() != location.getWorld()) {
			return false;
		}
		if (GoldenApple.getInstance().calculator().isBetween(corner1.getX(), location.getX(), corner2.getX())
				&& GoldenApple.getInstance().calculator().isBetween(corner1.getZ(), location.getZ(), corner2.getZ())
				&& (!ignoreY || GoldenApple.getInstance().calculator().isBetween(corner1.getY(), location.getY(), corner2.getY()))) {
			return true;
		} else
			return false;
	}
}