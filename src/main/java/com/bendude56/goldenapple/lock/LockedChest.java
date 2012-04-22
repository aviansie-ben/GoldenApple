package com.bendude56.goldenapple.lock;

import org.bukkit.Location;

public final class LockedChest extends LockedBlock {

	protected LockedChest(Location l, long ownerId, LockLevel level) {
		super(l, ownerId, level);
	}

}
