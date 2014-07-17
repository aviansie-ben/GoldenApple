package com.bendude56.goldenapple.area;

import java.sql.ResultSet;
import java.sql.SQLException;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.World;

public abstract class Region {
    private final long regionId;
    private final long areaId;
    private Location min;
    private Location max;
    private boolean ignoreY;
    
    public Region(long id, long areaId, Location min, Location max, boolean ignoreY) {
        this.regionId = id;
        this.areaId = areaId;
        this.min = min.clone();
        this.max = max.clone();
        this.ignoreY = ignoreY;
    }
    
    /**
     * Constructs the region based off of a database result set.
     * 
     * @param r The result set to use
     * @throws SQLException
     */
    public Region(ResultSet r) throws SQLException {
        this.regionId = r.getLong("ID");
        this.areaId = r.getLong("AreaID");
        this.min = new Location(Bukkit.getWorld(r.getString("World")), r.getDouble("MinX"), r.getDouble("MinY"), r.getDouble("MinZ"));
        this.max = new Location(Bukkit.getWorld(r.getString("World")), r.getDouble("MaxX"), r.getDouble("MaxY"), r.getDouble("MaxZ"));
        this.ignoreY = r.getBoolean("IgnoreY");
    }
    
    /**
     * Gets the ID of the region used for indexing in storage.
     * 
     * @return The unique identifying number of this region.
     */
    public long getId() {
        return regionId;
    }
    
    /**
     * Gets the ID of the area to which the region belongs.
     * 
     * @return The Id of this region's area.
     */
    public long getAreaId() {
        return areaId;
    }
    
    /**
     * Sets the bounds of the region.
     * 
     * @param c1 The first corner.
     * @param c2 The second corner.
     * @param ignoreY True if the region should span the height of the world,
     * false if not.
     */
    public void setBounds(Location c1, Location c2, boolean ignoreY) {
        if (c1 == null || c2 == null || c1.getWorld() != c2.getWorld()) {
            return;
        }
        
        // Set the worlds
        min.setWorld(c1.getWorld());
        max.setWorld(c2.getWorld());
        
        // Save min x, y, and z
        min.setX(Math.min(c1.getX(), c2.getX()));
        min.setY(Math.min(c1.getY(), c2.getY()));
        min.setZ(Math.min(c1.getZ(), c2.getZ()));
        
        // Save max x, y, and z
        max.setX(Math.max(c1.getX(), c2.getX()));
        max.setY(Math.max(c1.getY(), c2.getY()));
        max.setZ(Math.max(c1.getZ(), c2.getZ()));
        
        return;
    }
    
    /**
     * Gets the world in which the region exists.
     * 
     * @return The world the region is in.
     */
    public World getWorld() {
        return min.getWorld();
    }
    
    /**
     * Gets the lower bound of the region.
     * 
     * @return The corner with the lowest coordinates.
     */
    public Location getMinLocation() {
        return min.clone();
    }
    
    /**
     * Gets the upper bound of the region.
     * 
     * @return The corner with the upper coordinates.
     */
    public Location getMaxLocation() {
        return max.clone();
    }
    
    /**
     * Gets the minimum X-coordinate that the region reaches.
     * 
     * @return The minimum X-coordinate of this region.
     */
    public double getMinX() {
        return min.getX();
    }
    
    /**
     * Gets the minimum Y-coordinate that the region reaches.
     * 
     * @return The maximum Y-coordinate of this region.
     */
    public double getMinY() {
        return (ignoreY ? 0 : min.getY());
    }
    
    /**
     * Gets the minimum Z-coordinate that the region reaches.
     * 
     * @return The minimum Z-coordinate of the region.
     */
    public double getMinZ() {
        return min.getZ();
    }
    
    /**
     * Gets the maximum X-coordinate that this region reaches.
     * 
     * @return The maximum X-coordinate of this region.
     */
    public double getMaxX() {
        return max.getX();
    }
    
    /**
     * Gets the maximum Y-coordinate that the region reaches.
     * 
     * @return The maximum Y-coordinate of this region.
     */
    public double getMaxY() {
        return (ignoreY ? max.getWorld().getMaxHeight() : max.getY());
    }
    
    /**
     * Gets the maximum Z-coordinate that the region reaches.
     * 
     * @return The maximum Z-coordinate of this region.
     */
    public double getMaxZ() {
        return max.getZ();
    }
    
    /**
     * Checks if the region expands to the top and bottom of the world.
     * 
     * @return True if the region expands to Y
     */
    public boolean ignoreY() {
        return ignoreY;
    }
    
    /**
     * Gets the shape enum of this region.
     * 
     * @return AreaShape enum representing the shape of the region.
     */
    public abstract RegionShape getAreaShape();
    
    /**
     * Checks if a location is contained within the bounds of the region.
     * 
     * @param loc Location to check for.
     * @return True if the location is contained within the bounds of the
     * region, false if not.
     */
    public abstract boolean containsLocation(Location loc);
    
    /**
     * Checks if another region is contained within the bounds of the region.
     * 
     * @param region The region to compare with
     * @return True if the two regions overlap, false if not.
     */
    // public abstract boolean overlapsRegion(AreaRegion region); DISABLED
    
    /**
     * Constructs a new region from an existing region.
     * 
     * @param src The existing region to build a new region from.
     * @return The newly constructed region.
     */
    public abstract Region fromAreaRegion(Region src);
    
    /**
     * Determines the appropriate Region child class to use based on the given
     * RegionShape.
     * 
     * @param shape The shape of the region.
     * @return A class that extends Region that corresponds to the given
     * parameter.
     */
    public static Class<? extends Region> getRegionSubclass(RegionShape shape) {
        switch (shape) {
            case CUBOID:
                return RegionCuboid.class;
            case ELLIPSOID:
                return RegionEllipsoid.class;
            case CYLINDER:
                return RegionCylinder.class;
            default:
                return null;
        }
    }
    
    /**
     * Saves the region's properties to the database.
     */
    public void save() {
        AreaManager.getInstance().saveRegion(regionId);
    }
    
}
