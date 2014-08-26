package com.bendude56.goldenapple.area;

import java.sql.ResultSet;
import java.sql.SQLException;

import org.bukkit.Location;

public class RegionCuboid extends Region {
    
    public RegionCuboid(long id, long areaId, Location min, Location max, boolean ignoreY) {
        super(id, areaId, min, max, ignoreY);
    }
    
    public RegionCuboid(ResultSet r) throws SQLException {
        super(r);
    }
    
    @Override
    public RegionShape getAreaShape() {
        return RegionShape.CUBOID;
    }
    
    @Override
    public double getVolume() {
        return Math.abs((getMaxX() - getMinX()) * (getMaxY() - getMinY()) * (getMaxZ() - getMinZ()));
    }
    
    @Override
    public boolean containsLocation(Location loc) {
        return (loc.getWorld() == getWorld() && loc.getX() >= getMinX() && loc.getY() >= getMinY() && loc.getZ() >= getMinZ() && loc.getX() <= getMaxX() && loc.getY() <= getMaxY() && loc.getZ() <= getMaxZ());
    }
    
    /*
     * @Override public boolean overlapsRegion(AreaRegion region) { //
     * Short-circuit check if there is no chance of overlap if
     * (region.getWorld() != getWorld() || region.getMinX() > getMaxX() ||
     * region.getMinY() > getMaxY() || region.getMinZ() > getMaxZ() ||
     * region.getMaxX() < getMinX() || region.getMaxY() < getMinY() ||
     * region.getMaxZ() < getMinZ()) return false; return false; }
     */
    
    @Override
    public RegionCuboid fromAreaRegion(Region src) {
        return new RegionCuboid(src.getId(), src.getAreaId(), src.getMinLocation(), src.getMaxLocation(), src.ignoreY());
    }
    
}
