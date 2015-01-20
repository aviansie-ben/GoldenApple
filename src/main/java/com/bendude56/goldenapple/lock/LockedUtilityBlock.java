package com.bendude56.goldenapple.lock;

import java.sql.ResultSet;
import java.sql.SQLException;

public class LockedUtilityBlock extends LockedBlock {
    
    public LockedUtilityBlock(ResultSet r) throws SQLException, ClassNotFoundException {
        super(r, "GA_UTILITYBLOCK");
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
