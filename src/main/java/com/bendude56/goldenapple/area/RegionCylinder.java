package com.bendude56.goldenapple.area;

import java.sql.ResultSet;
import java.sql.SQLException;

import org.bukkit.Location;

public class RegionCylinder extends Region {
    private double x_rad, z_rad;
    private double x_origin, z_origin;
    
    public RegionCylinder(long id, long areaId, Location min, Location max, boolean ignoreY) {
        super(id, areaId, min, max, ignoreY);
        updateFields();
    }
    
    public RegionCylinder(ResultSet r) throws SQLException {
        super(r);
        updateFields();
    }
    
    @Override
    public void setBounds(Location c1, Location c2, boolean ignoreY) {
        super.setBounds(c1, c2, ignoreY);
        updateFields();
    }
    
    @Override
    public RegionShape getAreaShape() {
        return RegionShape.CYLINDER;
    }
    
    @Override
    public boolean containsLocation(Location loc) {
        return (loc.getWorld() == getWorld()
            && ((Math.pow(loc.getX() - x_origin, 2) / (x_rad * x_rad))
            + (Math.pow(loc.getZ() - z_origin, 2) / (z_rad * z_rad)))
            <= 1.0
            && loc.getY() >= getMinY()
            && loc.getY() <= getMaxY());
    }
    
    /*
     * @Override public boolean overlapsRegion(AreaRegion region) { //
     * Short-circuit check if there is no chance of overlap if
     * (region.getWorld() != getWorld() || region.getMinX() > getMaxX() ||
     * region.getMinY() > getMaxY() || region.getMinZ() > getMaxZ() ||
     * region.getMaxX() < getMinX() || region.getMaxY() < getMinY() ||
     * region.getMaxZ() < getMinZ()) return false; // TODO Auto-generated method
     * stub return false; }
     */
    
    @Override
    public Region fromAreaRegion(Region src) {
        return new RegionCylinder(src.getId(), src.getAreaId(), src.getMinLocation(), src.getMaxLocation(), src.ignoreY());
    }
    
    private void updateFields() {
        x_rad = (getMaxX() - getMinX()) / 2.0;
        z_rad = (getMaxZ() - getMinZ()) / 2.0;
        x_origin = getMinX() + x_rad;
        z_origin = getMinZ() + z_rad;
    }
}
