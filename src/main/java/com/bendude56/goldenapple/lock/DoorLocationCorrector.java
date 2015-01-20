package com.bendude56.goldenapple.lock;

import org.bukkit.Location;
import org.bukkit.Material;

public class DoorLocationCorrector implements ILocationCorrector {
    
    @Override
    public void correctLocation(Location l) {
        Material m = l.getBlock().getType();
        
        if (isDoor(m)) {
            l.setY(l.getY() - 1);
            if (l.getBlock().getType() == m) {
                return;
            }
            l.setY(l.getY() + 1);
        } else {
            l.setY(l.getY() + 1);
            if (isDoor(l.getBlock().getType())) {
                return;
            }
            l.setY(l.getY() - 1);
        }
    }
    
    private boolean isDoor(Material m) {
        switch (m) {
        case ACACIA_DOOR:
        case BIRCH_DOOR:
        case DARK_OAK_DOOR:
        case JUNGLE_DOOR:
        case SPRUCE_DOOR:
        case WOODEN_DOOR:
            return true;
        default:
            return false;
        }
    }
    
}
