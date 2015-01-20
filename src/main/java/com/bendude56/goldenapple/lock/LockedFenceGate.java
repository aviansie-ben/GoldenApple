package com.bendude56.goldenapple.lock;

import java.sql.ResultSet;
import java.sql.SQLException;

public final class LockedFenceGate extends LockedBlock {
    
    public LockedFenceGate(ResultSet r) throws SQLException, ClassNotFoundException {
        super(r, "GA_FENCEGATE");
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
