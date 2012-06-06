package com.bendude56.goldenapple.lock;

import java.io.IOException;
import java.sql.ResultSet;
import java.sql.SQLException;

import org.bukkit.Location;

public final class LockedChest extends LockedBlock {
	
	protected LockedChest(ResultSet r) throws SQLException, ClassNotFoundException, IOException {
		super(r);
	}

	protected LockedChest(long id, Location l, long ownerId, LockLevel level) {
		super(id, l, ownerId, level);
	}

}
