package com.bendude56.goldenapple.lock;

import java.io.IOException;
import java.sql.ResultSet;
import java.sql.SQLException;

public final class LockedDoor extends LockedBlock {
	
	public LockedDoor(ResultSet r) throws SQLException, ClassNotFoundException, IOException {
		super(r, "GA_DOOR");
	}
	
	@Override
	public boolean isHopperAccessApplicable() {
		return false;
	}

	@Override
	public boolean isRedstoneAccessApplicable() {
		return true;
	}

}
