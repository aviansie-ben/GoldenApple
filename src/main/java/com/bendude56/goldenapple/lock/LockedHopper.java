package com.bendude56.goldenapple.lock;

import java.io.IOException;
import java.sql.ResultSet;
import java.sql.SQLException;

public class LockedHopper extends LockedBlock {
    
    public LockedHopper(ResultSet r) throws SQLException, ClassNotFoundException, IOException {
        super(r, "GA_HOPPER");
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
