package com.bendude56.goldenapple.lock;

import java.io.IOException;
import java.sql.ResultSet;
import java.sql.SQLException;

public class LockedDispenser extends LockedBlock {

	public LockedDispenser(ResultSet r) throws SQLException, ClassNotFoundException, IOException {
		super(r, "GA_DISPENSER");
	}
	
	@Override
	public boolean isHopperAccessApplicable() {
		return true;
	}

	@Override
	public boolean isRedstoneAccessApplicable() {
		return true;
	}

}
