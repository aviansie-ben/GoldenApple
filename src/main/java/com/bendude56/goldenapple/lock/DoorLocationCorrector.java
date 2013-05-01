package com.bendude56.goldenapple.lock;

import org.bukkit.Location;
import org.bukkit.Material;

public class DoorLocationCorrector implements ILocationCorrector {

	@Override
	public void correctLocation(Location l) {
		if (l.getBlock().getType() == Material.WOODEN_DOOR) {
			l.setY(l.getY() - 1);
			if (l.getBlock().getType() == Material.WOODEN_DOOR) {
				return;
			}
			l.setY(l.getY() + 1);
		}
	}

}
