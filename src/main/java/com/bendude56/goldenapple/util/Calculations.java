package com.bendude56.goldenapple.util;

import org.bukkit.Location;
import org.bukkit.entity.Entity;

/**
 * This class will contain various functions and methods that are important to
 * Minecraft plugins. A repository of miscellaneous calculations that are less
 * specialized and more generalized.
 * 
 * @author Deaboy
 * 
 */
public class Calculations {
	private Calculations() {}

	/**
	 * Returns a double of the exact distance between two entities
	 * 
	 * @param entity1 The first entity
	 * @param entity2 The second entity
	 * @param height Set to true if you want to take the y-coordinate into
	 *            account. Set to false to ignore the Y-coordinate.
	 * @return The distance between the two entities.
	 */
	public static double getDistance(Entity entity1, Entity entity2, boolean ignoreY) {
		return getDistance(entity1.getLocation(), entity2.getLocation(), ignoreY);
	}

	/**
	 * Returns a double of the exact distance between two locations
	 * 
	 * @param location1 The first location
	 * @param location2 The second location
	 * @param height Set to true if you want to take the y-coordinate into
	 *            account. Set to false to ignore the Y-coordinate.
	 * @return The distance between the two locations.
	 */
	public static double getDistance(Location location1, Location location2, boolean ignoreY) {
		if (!ignoreY) {
			return getDistance(location1.getX(), location1.getY(), location1.getZ(), location2.getX(), location2.getY(), location2.getZ());
		} else {
			return getDistance(location1.getX(), 0, location1.getZ(), location2.getX(), 0, location2.getZ());
		}
	}

	/**
	 * Returns a double of the square root of the sum of the 2nd powers of these
	 * 
	 * @return The distance between the two entities.
	 */
	public static double getDistance(double x1, double y1, double z1, double x2, double y2, double z2) {
		return Math.sqrt(Math.pow((x2 - x1), 2) + Math.pow((z2 - z1), 2));
	}

	public static boolean isBetween(int top, int middle, int bottom) {
		return isBetween((double)top, (double)middle, (double)bottom);
	}

	public static boolean isBetween(double top, double middle, double bottom) {
		if (top >= bottom && middle <= top && middle >= bottom)
			return true;
		if (top <= bottom && middle >= top && middle <= bottom)
			return true;
		return false;
	}
}
