package com.bendude56.goldenapple.lock;

import org.bukkit.Location;
import org.bukkit.Material;

public class DoubleChestLocationCorrector implements ILocationCorrector {
	@Override
	public void correctLocation(Location l) {
		if (l.getWorld().getBlockAt(l).getType() == Material.CHEST) {
			l.setX(l.getX() + 1);
			if (l.getWorld().getBlockAt(l).getType() == Material.CHEST) {
				return;
			}
			l.setX(l.getX() - 1);
			l.setZ(l.getZ() + 1);
			if (l.getWorld().getBlockAt(l).getType() == Material.CHEST) {
				return;
			}
			l.setZ(l.getZ() - 1);
		}
	}
}
