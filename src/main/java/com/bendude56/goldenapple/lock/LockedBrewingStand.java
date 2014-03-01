package com.bendude56.goldenapple.lock;

import java.sql.ResultSet;
import java.sql.SQLException;

public class LockedBrewingStand extends LockedBlock {

	public LockedBrewingStand(ResultSet r) throws SQLException, ClassNotFoundException {
		super(r, "GA_BREWINGSTAND");
	}

	@Override
	public boolean isRedstoneAccessApplicable() {
		return false;
	}

	@Override
	public boolean isHopperAccessApplicable() {
		return false;
	}

}
