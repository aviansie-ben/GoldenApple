package com.bendude56.goldenapple.area;

import java.sql.ResultSet;
import java.sql.SQLException;

import org.bukkit.Location;

public class RegionEllipsoid extends Region {
    private double x_rad, y_rad, z_rad;
    private double x_origin, y_origin, z_origin;
    
    public RegionEllipsoid(long id, long areaId, Location min, Location max, boolean ignoreY) {
        super(id, areaId, min, max, ignoreY);
        updateFields();
    }
    
    public RegionEllipsoid(ResultSet r) throws SQLException {
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
        return RegionShape.ELLIPSOID;
    }
    
    @Override
    public double getVolume() {
        return Math.abs(((getMaxX() - getMinX()) / 2) * ((getMaxY() - getMinY()) / 2) * ((getMaxZ() - getMinZ()) / 2) * Math.PI * 4 / 3);
    }
    
    @Override
    public boolean containsLocation(Location loc) {
        return (loc.getWorld() == getWorld() && ((Math.pow(loc.getX() - x_origin, 2) / (x_rad * x_rad)) + (Math.pow(loc.getY() - y_origin, 2) / (y_rad * y_rad)) + (Math.pow(loc.getZ() - z_origin, 2) / (z_rad * z_rad))) <= 1.0);
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
        return new RegionEllipsoid(src.getId(), src.getAreaId(), src.getMinLocation(), src.getMaxLocation(), src.ignoreY());
    }
    
    private void updateFields() {
        x_rad = (getMaxX() - getMinX()) / 2.0;
        y_rad = (getMaxY() - getMinY()) / 2.0;
        z_rad = (getMaxZ() - getMinZ()) / 2.0;
        x_origin = getMinX() + x_rad;
        y_origin = getMinY() + y_rad;
        z_origin = getMinZ() + z_rad;
    }
    
}
