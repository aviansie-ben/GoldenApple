package com.bendude56.goldenapple.lock;

import java.io.IOException;
import java.sql.ResultSet;
import java.sql.SQLException;

public final class LockedFurnace extends LockedBlock {
    
    public LockedFurnace(ResultSet r) throws SQLException, ClassNotFoundException, IOException {
        super(r, "GA_FURNACE");
    }
    
    @Override
    public boolean isHopperAccessApplicable() {
        return true;
    }
    
    @Override
    public boolean isRedstoneAccessApplicable() {
        return false;
    }
    
}
