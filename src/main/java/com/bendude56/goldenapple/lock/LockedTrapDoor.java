package com.bendude56.goldenapple.lock;

import java.sql.ResultSet;
import java.sql.SQLException;

public final class LockedTrapDoor extends LockedBlock {
    
    public LockedTrapDoor(ResultSet r) throws SQLException, ClassNotFoundException {
        super(r, "GA_TRAPDOOR");
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
