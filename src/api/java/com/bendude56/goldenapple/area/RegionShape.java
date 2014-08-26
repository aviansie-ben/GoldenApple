package com.bendude56.goldenapple.area;

import com.bendude56.goldenapple.User;

public enum RegionShape {
    CUBOID(0), ELLIPSOID(1), CYLINDER(2);
    
    private final int id;
    
    private RegionShape(int code) {
        this.id = code;
    }
    
    public int getId() {
        return this.id;
    }
    
    public static RegionShape fromId(int id) {
        for (RegionShape i : RegionShape.values()) {
            if (i.id == id) {
                return i;
            }
        }
        return null;
    }
    
    public String getLocalizedName(User user) {
        switch (this) {
            case CUBOID:
                return user.getLocalizedMessage("module.area.shape.cuboid");
            case ELLIPSOID:
                return user.getLocalizedMessage("module.area.shape.ellipsoid");
            case CYLINDER:
                return user.getLocalizedMessage("module.area.shape.cylinder");
            default:
                return "";
        }
    }
}
