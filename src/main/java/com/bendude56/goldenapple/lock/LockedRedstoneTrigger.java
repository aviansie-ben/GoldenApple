package com.bendude56.goldenapple.lock;

import java.io.IOException;
import java.sql.ResultSet;
import java.sql.SQLException;

import org.bukkit.Location;

public final class LockedRedstoneTrigger extends LockedBlock {
	
	public LockedRedstoneTrigger(ResultSet r) throws SQLException, ClassNotFoundException, IOException {
		super(r, "GA_REDSTONE");
	}

	public LockedRedstoneTrigger(Long id, Location l, Long ownerId, LockLevel level) {
		super(id, l, ownerId, level, "GA_REDSTONE");
	}

}
