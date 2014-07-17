package com.bendude56.goldenapple.area;

import java.lang.reflect.InvocationTargetException;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Deque;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;

import org.bukkit.Location;

import com.bendude56.goldenapple.GoldenApple;
import com.bendude56.goldenapple.permissions.IPermissionUser;

public class SimpleAreaManager extends AreaManager {
    
    private HashMap<Long, Area> areaCache = new HashMap<Long, Area>();
    private Deque<Long> areaCacheOut = new ArrayDeque<Long>();
    private HashMap<Long, Region> regionCache = new HashMap<Long, Region>();
    private Deque<Long> regionCacheOut = new ArrayDeque<Long>();
    private List<Long> overrides = new ArrayList<Long>();
    private int cacheSize;
    
    public SimpleAreaManager() {
        cacheSize = GoldenApple.getInstanceMainConfig().getInt("modules.area.cacheSize", 100);
        if (cacheSize < 3) {
            cacheSize = 3;
        }
        
        GoldenApple.getInstanceDatabaseManager().createOrUpdateTable("areas");
        GoldenApple.getInstanceDatabaseManager().createOrUpdateTable("areausers");
        GoldenApple.getInstanceDatabaseManager().createOrUpdateTable("areagroups");
        GoldenApple.getInstanceDatabaseManager().createOrUpdateTable("areaflags");
        GoldenApple.getInstanceDatabaseManager().createOrUpdateTable("arearegions");
    }
    
    private Area checkAreaCache(long id, boolean promote) {
        if (areaCache.containsKey(id)) {
            
            // Promote area in cache
            if (promote) {
                areaCacheOut.remove(id);
                areaCacheOut.add(id);
            }
            return areaCache.get(id);
        } else {
            return null;
        }
    }
    
    private Region checkRegionCache(long id, boolean promote) {
        if (regionCache.containsKey(id)) {
            
            // Promote region in cache
            if (promote) {
                regionCacheOut.remove(id);
                regionCacheOut.add(id);
            }
            return regionCache.get(id);
        } else {
            return null;
        }
    }
    
    private HashMap<Long, Region> checkRegionCache(Location l) {
        HashMap<Long, Region> regions = new HashMap<Long, Region>();
        for (Map.Entry<Long, Region> r : regionCache.entrySet()) {
            if (r.getValue().containsLocation(l)) {
                
                // Promote region in cache
                regionCacheOut.remove(r.getValue().getId());
                regionCacheOut.add(r.getValue().getId());
                regions.put(r.getKey(), r.getValue());
            }
        }
        return regions;
    }
    
    private Area loadAreaIntoCache(ResultSet r) throws SQLException {
        try {
            Area a = new Area(r);
            areaCache.put(a.getAreaId(), a);
            areaCacheOut.add(a.getAreaId());
            if (areaCacheOut.size() > cacheSize) {
                long id = areaCacheOut.pop();
                areaCache.remove(id);
            }
            return a;
        } catch (Exception e) {
            GoldenApple.log(Level.SEVERE, "There was an error while loading an area.");
            GoldenApple.log(Level.SEVERE, "Please report this error to the creator of 'GoldenApple'. Please include the following stack trace:");
            GoldenApple.log(Level.SEVERE, e);
            return null;
        }
    }
    
    private Region loadRegionIntoCache(ResultSet r) throws SQLException {
        Class<? extends Region> regionClass = Region.getRegionSubclass(RegionShape.fromId(r.getInt("Shape")));
        if (regionClass == null) {
            GoldenApple.log(Level.WARNING, "The specified region shape is unrecognized: " + r.getInt("Shape"));
            GoldenApple.log(Level.WARNING, "This region will be ignored...");
            return null;
        }
        try {
            Region a = regionClass.getConstructor(new Class<?>[] { ResultSet.class }).newInstance(r);
            regionCache.put(a.getId(), a);
            regionCacheOut.add(a.getId());
            if (regionCacheOut.size() > cacheSize) {
                long id = regionCacheOut.pop();
                regionCache.remove(id);
            }
            return a;
        } catch (Exception e) {
            GoldenApple.log(Level.SEVERE, "There was an error while loading a region.");
            GoldenApple.log(Level.SEVERE, "Please report this error to the creator of 'GoldenApple'. Please include the following stack trace:");
            GoldenApple.log(Level.SEVERE, e);
            return null;
        }
    }
    
    @Override
    public Area getArea(long id) {
        Area a = checkAreaCache(id, true);
        if (a != null) {
            return a;
        }
        
        try {
            ResultSet r = GoldenApple.getInstanceDatabaseManager().executeQuery("SELECT * FROM Areas WHERE ID=?", String.valueOf(id));
            try {
                a = (r.next()) ? loadAreaIntoCache(r) : null;
            } finally {
                GoldenApple.getInstanceDatabaseManager().closeResult(r);
            }
            return a;
        } catch (SQLException e) {
            GoldenApple.log(Level.WARNING, "Error while attempting to retrieve an area from the database:");
            GoldenApple.log(Level.SEVERE, "Please report this error to the creator of 'GoldenApple'. Please include the following stack trace:");
            GoldenApple.log(Level.WARNING, e);
            return null;
        }
    }
    
    @Override
    public Area getArea(String label) {
        if (label == null) {
            return null;
        }
        
        // Search the cache
        for (Area a : areaCache.values()) {
            if (label.equalsIgnoreCase(a.getLabel())) {
                return a;
            }
        }
        
        // Check the database
        try {
            
            Area a;
            ResultSet r = GoldenApple.getInstanceDatabaseManager().executeQuery("SELECT * FROM Areas WHERE Label=?", label);
            try {
                a = (r.next()) ? loadAreaIntoCache(r) : null;
            } finally {
                GoldenApple.getInstanceDatabaseManager().closeResult(r);
            }
            return a;
        } catch (SQLException e) {
            GoldenApple.log(Level.WARNING, "Error while attempting to retrieve an area from the database:");
            GoldenApple.log(Level.WARNING, e);
            return null;
        }
        
    }
    
    @Override
    public List<Area> getAreas(Location l) {
        HashMap<Long, Area> areaMap = new HashMap<Long, Area>();
        List<Area> areaList;
        
        for (Region r : getRegions(l)) {
            if (!areaMap.containsKey(r.getAreaId())) {
                areaMap.put(r.getAreaId(), getArea(r.getAreaId()));
            }
        }
        
        // Sort areas by priority, greatest priority first
        areaList = new ArrayList<Area>(areaMap.values());
        Collections.sort(areaList, new Comparator<Area>() {
            @Override
            public int compare(Area a1, Area a2) {
                return -((Integer) a1.getPriority()).compareTo(a2.getPriority());
            }
        });
        return areaList;
    }
    
    @Override
    public List<Area> getAreas(int page, int per) {
        List<Area> areas = new ArrayList<Area>();
        Area a;
        
        try {
            ResultSet r = GoldenApple.getInstanceDatabaseManager().executeQuery("SELECT * FROM Areas LIMIT ?, ?", (page - 1) * per, per);
            try {
                while (r.next()) {
                    a = checkAreaCache(r.getLong("ID"), false);
                    if (a == null) {
                        a = loadAreaIntoCache(r);
                    }
                    if (a != null) {
                        areas.add(a);
                    }
                }
            } finally {
                GoldenApple.getInstanceDatabaseManager().closeResult(r);
            }
            return areas;
        } catch (SQLException e) {
            GoldenApple.log(Level.SEVERE, "There was an error while loading an area.");
            GoldenApple.log(Level.SEVERE, "Please report this error to the creator of 'GoldenApple'. Please include the following stack trace:");
            GoldenApple.log(Level.SEVERE, e);
            return null;
        }
    }
    
    @Override
    public List<Area> getAreasByOwner(long userId) {
        List<Area> areas = new ArrayList<Area>();
        Area a;
        
        String query = "(SELECT Areas.* FROM Areas, AreaUsers WHERE AreaUsers.UserID=? AND AreaUsers.AreaID=Areas.ID AND AreaUsers.AccessLevel=?) UNION DISTINCT (SELECT Areas.* FROM Areas, AreaGroups, GroupUserMembers WHERE GroupUserMembers.MemberID=? AND GroupUserMembers.GroupID=AreaGroups.GroupID AND AreaGroups.AreaID=Areas.ID AND AreaGroups.AccessLevel=?) ORDER BY ID";
        
        try {
            ResultSet r = GoldenApple.getInstanceDatabaseManager().executeQuery(query, userId, AreaAccessLevel.OWNER.getId(), userId, AreaAccessLevel.OWNER.getId());
            try {
                while (r.next()) {
                    a = checkAreaCache(r.getLong("ID"), false);
                    if (a == null) {
                        a = loadAreaIntoCache(r);
                    }
                    if (a != null) {
                        areas.add(a);
                    }
                }
            } finally {
                GoldenApple.getInstanceDatabaseManager().closeResult(r);
            }
            return areas;
        } catch (SQLException e) {
            GoldenApple.log(Level.SEVERE, "There was an error while loading an area.");
            GoldenApple.log(Level.SEVERE, "Please report this error to the creator of 'GoldenApple'. Please include the following stack trace:");
            GoldenApple.log(Level.SEVERE, e);
            return null;
        }
    }
    
    @Override
    public int getTotalAreas() {
        int total;
        try {
            ResultSet r = GoldenApple.getInstanceDatabaseManager().executeQuery("SELECT COUNT(*) AS count FROM Areas");
            try {
                total = (r.next() ? r.getInt("count") : -1);
            } finally {
                GoldenApple.getInstanceDatabaseManager().closeResult(r);
            }
            return total;
        } catch (SQLException e) {
            GoldenApple.log(Level.WARNING, "Error while attempting to retrieve number of areas from the database:");
            GoldenApple.log(Level.WARNING, e);
            return -1;
        }
    }
    
    @Override
    public HashMap<Long, AreaAccessLevel> retrieveAreaUserAccessLevels(long areaId) {
        HashMap<Long, AreaAccessLevel> groups = new HashMap<Long, AreaAccessLevel>();
        
        try {
            ResultSet r = GoldenApple.getInstanceDatabaseManager().executeQuery("SELECT * FROM AreaUsers WHERE AreaID=?", String.valueOf(areaId));
            try {
                while (r.next()) {
                    groups.put(r.getLong("UserID"), AreaAccessLevel.fromId(r.getInt("AccessLevel")));
                }
            } finally {
                GoldenApple.getInstanceDatabaseManager().closeResult(r);
            }
        } catch (SQLException e) {
            GoldenApple.log(Level.WARNING, "Error while attempting to retrieve a user's area permission from the database:");
            GoldenApple.log(Level.WARNING, e);
            return null;
        }
        
        return groups;
    }
    
    @Override
    public HashMap<Long, AreaAccessLevel> retrieveAreaGroupAccessLevels(long areaId) {
        HashMap<Long, AreaAccessLevel> groups = new HashMap<Long, AreaAccessLevel>();
        
        try {
            ResultSet r = GoldenApple.getInstanceDatabaseManager().executeQuery("SELECT * FROM AreaGroups WHERE AreaID=?", String.valueOf(areaId));
            try {
                while (r.next()) {
                    groups.put(r.getLong("GroupID"), AreaAccessLevel.fromId(r.getInt("AccessLevel")));
                }
            } finally {
                GoldenApple.getInstanceDatabaseManager().closeResult(r);
            }
        } catch (SQLException e) {
            GoldenApple.log(Level.WARNING, "Error while attempting to retrieve a group's area permission from the database:");
            GoldenApple.log(Level.WARNING, e);
            return null;
        }
        
        return groups;
    }
    
    @Override
    public List<AreaFlag> retrieveAreaFlags(long areaId) {
        List<AreaFlag> flags = new ArrayList<AreaFlag>();
        
        try {
            ResultSet r = GoldenApple.getInstanceDatabaseManager().executeQuery("SELECT Flag FROM AreaFlags WHERE AreaID=?", String.valueOf(areaId));
            try {
                while (r.next()) {
                    flags.add(AreaFlag.fromId(r.getInt("Flag")));
                }
            } finally {
                GoldenApple.getInstanceDatabaseManager().closeResult(r);
            }
        } catch (SQLException e) {
            GoldenApple.log(Level.WARNING, "Error while attempting to retrieve an area flag from the database:");
            GoldenApple.log(Level.WARNING, e);
            return null;
        }
        
        return flags;
    }
    
    @Override
    public Region getRegion(long id) {
        Region a = checkRegionCache(id, true);
        if (a != null) {
            return a;
        }
        
        try {
            ResultSet r = GoldenApple.getInstanceDatabaseManager().executeQuery("SELECT * FROM AreaRegions WHERE ID=?", String.valueOf(id));
            try {
                a = (r.next()) ? loadRegionIntoCache(r) : null;
            } finally {
                GoldenApple.getInstanceDatabaseManager().closeResult(r);
            }
            return a;
        } catch (SQLException e) {
            GoldenApple.log(Level.WARNING, "Error while attempting to retrieve an area region from the database:");
            GoldenApple.log(Level.WARNING, e);
            return null;
        }
    }
    
    @Override
    public List<Region> getRegions(Location l) {
        HashMap<Long, Region> regions = checkRegionCache(l);
        
        try {
            ResultSet r = GoldenApple.getInstanceDatabaseManager().executeQuery("SELECT * FROM AreaRegions WHERE MinX<=? AND ?<=MaxX AND MinZ<=? AND ?<=MaxZ AND (IgnoreY=TRUE OR (MinY<=? AND ?<=MaxY)) AND World=?", l.getX(), l.getX(), l.getZ(), l.getZ(), l.getY(), l.getY(), l.getWorld().getName());
            try {
                while (r.next()) {
                    if (!regions.containsKey(r.getLong("ID"))) {
                        Region region = loadRegionIntoCache(r);
                        if (region != null && region.containsLocation(l)) {
                            regions.put(region.getId(), region);
                        }
                    }
                }
            } finally {
                GoldenApple.getInstanceDatabaseManager().closeResult(r);
            }
        } catch (SQLException e) {
            GoldenApple.log(Level.WARNING, "Error while attempting to retrieve regions from the database.");
            GoldenApple.log(Level.WARNING, e);
        }
        
        return new ArrayList<Region>(regions.values());
    }
    
    @Override
    public List<Long> retrieveAreaRegionIds(long areaId) {
        List<Long> regions = new ArrayList<Long>();
        try {
            ResultSet r = GoldenApple.getInstanceDatabaseManager().executeQuery("SELECT ID FROM AreaRegions WHERE AreaID=?", areaId);
            try {
                while (r.next()) {
                    regions.add(r.getLong(1));
                }
            } finally {
                GoldenApple.getInstanceDatabaseManager().closeResult(r);
            }
        } catch (SQLException e) {
            GoldenApple.log(Level.WARNING, "Error while attempting to retrieve region list from the database:");
            GoldenApple.log(Level.WARNING, e);
            return null;
        }
        return regions;
    }
    
    @Override
    public boolean areaExists(long id) throws SQLException {
        boolean exists = false;
        
        if (areaCache.containsKey(id)) {
            return true;
        }
        
        ResultSet r = GoldenApple.getInstanceDatabaseManager().executeQuery("SELECT NULL FROM Areas WHERE ID=?", String.valueOf(id));
        try {
            exists = r.next();
        } finally {
            GoldenApple.getInstanceDatabaseManager().closeResult(r);
        }
        
        return exists;
    }
    
    // Create new area
    @Override
    public Area createArea(IPermissionUser owner, String label, int priority, RegionShape shape, Location c1, Location c2, boolean ignoreY) throws SQLException, InvocationTargetException {
        long areaId;
        
        // Validate arguments
        if (owner == null || shape == null || c1 == null || c2 == null) {
            throw new IllegalArgumentException("Arguments cannot be null");
        }
        if (c1.getWorld() != c2.getWorld()) {
            throw new IllegalArgumentException("Locations must be in same world");
        }
        
        // Insert into database, retrieve id
        try {
            GoldenApple.getInstanceDatabaseManager().execute("INSERT INTO Areas (Label, Priority) VALUES (?, ?)", label, priority);
            ResultSet r = GoldenApple.getInstanceDatabaseManager().executeQuery("SELECT LAST_INSERT_ID()");
            r.next();
            areaId = r.getLong("LAST_INSERT_ID()");
            r.close();
        } catch (SQLException e) {
            GoldenApple.log(Level.SEVERE, "An error occured while inserting new area into the database.");
            GoldenApple.log(Level.SEVERE, e);
            return null;
        }
        
        // Assign owner to area
        updateAreaUser(areaId, owner.getId(), AreaAccessLevel.OWNER);
        
        // Create region for area
        createRegion(areaId, shape, c1, c2, ignoreY);
        
        // Fetch area from database, load into cache, return result
        return getArea(areaId);
    }
    
    // Updating individual area properties in database
    @Override
    protected void updateAreaLabel(long areaId, String label) {
        
        // Insert/upate entry in database
        try {
            GoldenApple.getInstanceDatabaseManager().execute("UPDATE Areas SET Label=? WHERE ID=?", label, areaId);
        } catch (SQLException e) {
            GoldenApple.log(Level.SEVERE, "An error occured while updating label of area " + areaId + " in the database.");
            GoldenApple.log(Level.SEVERE, e);
        }
    }
    
    @Override
    protected void updateAreaPriority(long areaId, int priority) {
        
        // Insert/update entry in database
        try {
            GoldenApple.getInstanceDatabaseManager().execute("UPDATE Areas SET Priority=? WHERE ID=?", priority, areaId);
        } catch (SQLException e) {
            GoldenApple.log(Level.SEVERE, "An error occured while updating priority of area " + areaId + " in the database.");
            GoldenApple.log(Level.SEVERE, e);
        }
    }
    
    @Override
    protected void updateAreaUser(long areaId, long userId, AreaAccessLevel level) {
        
        // Validate arguments
        if (level == null) {
            throw new IllegalArgumentException("Arguments cannot be null");
        }
        
        // Update/remove entry from database
        try {
            if (level == AreaAccessLevel.NONE) {
                GoldenApple.getInstanceDatabaseManager().execute("DELETE FROM AreaUsers WHERE AreaID=? AND UserID=?", areaId, userId);
            } else {
                GoldenApple.getInstanceDatabaseManager().execute("DELETE FROM AreaUsers WHERE AreaID=? AND UserID=?; INSERT INTO AreaUsers (AreaID, UserID, AccessLevel) VALUES (?, ?, ?)", areaId, userId, areaId, userId, level.getId());
            }
        } catch (SQLException e) {
            GoldenApple.log(Level.SEVERE, "An error occured while changing user " + userId + " access level for area " + areaId + " to " + level.toString());
            GoldenApple.log(Level.SEVERE, e);
        }
    }
    
    @Override
    protected void updateAreaGroup(long areaId, long groupId, AreaAccessLevel level) {
        
        // Validate arguments
        if (level == null) {
            throw new IllegalArgumentException("Arguments cannot be null");
        }
        
        // Update/remove entry from database
        try {
            if (level == AreaAccessLevel.NONE) {
                GoldenApple.getInstanceDatabaseManager().execute("DELETE FROM AreaGroups WHERE AreaID=? AND GroupID=?", areaId, groupId);
            } else {
                GoldenApple.getInstanceDatabaseManager().execute("DELETE FROM AreaGroups WHERE AreaID=? AND GroupID=?; INSERT INTO AreaGroups (AreaID, GroupID, AccessLevel) VALUES (?, ?, ?)", areaId, groupId, areaId, groupId, level.getId());
            }
        } catch (SQLException e) {
            GoldenApple.log(Level.SEVERE, "An error occured while updating group " + groupId + " access level for area " + areaId);
            GoldenApple.log(Level.SEVERE, e);
        }
    }
    
    @Override
    protected void updateAreaFlag(long areaId, AreaFlag flag, boolean set) {
        
        // Validate arguments
        if (flag == null) {
            throw new IllegalArgumentException("Arguments cannot be null");
        }
        
        // Insert/remove entry from database
        try {
            if (!set) {
                GoldenApple.getInstanceDatabaseManager().execute("DELETE FROM AreaFlags WHERE AreaID=? AND Flag=?", areaId, flag.getId());
            } else {
                GoldenApple.getInstanceDatabaseManager().execute("DELETE FROM AreaFlags WHERE AreaID=? AND Flag=?; INSERT INTO AreaFlags (AreaID, Flags) VALUES (?, ?)", areaId, flag.getId(), areaId, flag.getId());
            }
        } catch (SQLException e) {
            GoldenApple.log(Level.SEVERE, "An error occured while updating flag " + flag.toString() + " on area " + areaId);
            GoldenApple.log(Level.SEVERE, e);
        }
    }
    
    // Deleting an area
    @Override
    public boolean deleteArea(long areaId) {
        
        // Remove the regions from cache
        for (Long regionId : retrieveAreaRegionIds(areaId)) {
            if (regionCache.containsKey(regionId)) {
                regionCache.remove(regionId);
            }
            if (regionCacheOut.contains(regionId)) {
                regionCacheOut.remove(regionId);
            }
        }
        
        // Remove the area from cache
        if (areaCacheOut.contains(areaId)) {
            areaCacheOut.remove(areaId);
        }
        if (areaCache.containsKey(areaId)) {
            areaCache.remove(areaId);
        }
        
        // Delete the area from the database
        try {
            GoldenApple.getInstanceDatabaseManager().execute("DELETE FROM Areas WHERE ID=?", String.valueOf(areaId));
        } catch (SQLException e) {
            GoldenApple.log(Level.SEVERE, "An error occured while deleting area " + areaId + " from the database.");
            GoldenApple.log(Level.SEVERE, e);
            return false;
        }
        return true;
    }
    
    // Creating a region
    @Override
    protected Region createRegion(long areaId, RegionShape shape, Location c1, Location c2, boolean ignoreY) throws SQLException, InvocationTargetException {
        
        // Validate arguments
        if (shape == null || c1 == null || c2 == null) {
            throw new IllegalArgumentException("Arguments cannot be null");
        }
        if (c1.getWorld() != c2.getWorld()) {
            throw new IllegalArgumentException("Locations must be in same world");
        }
        
        // Insert into database
        try {
            GoldenApple.getInstanceDatabaseManager().execute("INSERT INTO AreaRegions (AreaID, World, MinX, MinY, MinZ, MaxX, MaxY, MaxZ, IgnoreY, Shape) VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?)", areaId, c1.getWorld().getName(), Math.min(c1.getX(), c2.getX()), Math.min(c1.getY(), c2.getY()), Math.min(c1.getZ(), c2.getZ()), Math.max(c1.getX(), c2.getX()), Math.max(c1.getY(), c2.getY()), Math.max(c1.getZ(), c2.getZ()), ignoreY, shape.getId());
            
            // Fetch from database, load into cache, return result
            ResultSet r = GoldenApple.getInstanceDatabaseManager().executeQuery("SELECT LAST_INSERT_ID()");
            r.next();
            Region region = getRegion(r.getLong("LAST_INSERT_ID()"));
            r.close();
            return region;
        } catch (SQLException e) {
            GoldenApple.log(Level.SEVERE, "An error occured while inserting new region into the database.");
            GoldenApple.log(Level.SEVERE, e);
            return null;
        }
    }
    
    // Saving region properties to database
    @Override
    protected void saveRegion(long regionId) {
        Region region = checkRegionCache(regionId, false);
        
        // Only update if region is cached
        if (region == null) {
            return;
        }
        
        try {
            GoldenApple.getInstanceDatabaseManager().execute("UPDATE AreaRegions SET AreaID=?, World=?, MinX=?, MinY=?, MinZ=?, MaxX=?, MaxY=?, MaxZ=?, IgnoreY=?, Shape=? WHERE ID=?", region.getAreaId(), region.getWorld().getName(), region.getMinX(), region.getMinY(), region.getMinZ(), region.getMaxX(), region.getMaxY(), region.getMaxZ(), region.ignoreY(), region.getAreaShape().getId(), regionId);
        } catch (SQLException e) {
            GoldenApple.log(Level.SEVERE, "Failed to save changes to region " + regionId + ":");
            GoldenApple.log(Level.SEVERE, e);
        }
    }
    
    // Deleting regions
    @Override
    protected boolean deleteRegion(long regionId) {
        
        // Remove the region from cache
        if (regionCache.containsKey(regionId)) {
            regionCache.remove(regionId);
        }
        if (regionCacheOut.contains(regionId)) {
            regionCacheOut.remove(regionId);
        }
        
        // Delete the region from the database
        try {
            GoldenApple.getInstanceDatabaseManager().execute("DELETE FROM AreaRegions WHERE ID=?", regionId);
        } catch (SQLException e) {
            GoldenApple.log(Level.SEVERE, "An error occured while removing region " + regionId + " from the database.");
            GoldenApple.log(Level.SEVERE, e);
            return false;
        }
        
        return true;
    }
    
    @Override
    public boolean checkOverride(IPermissionUser u) {
        return !GoldenApple.getInstanceMainConfig().getBoolean("modules.area.explicitOverrideRequired", true) || overrides.contains(u.getId());
    }
    
    @Override
    public void setOverride(IPermissionUser u, boolean override) {
        if (override && !this.overrides.contains(u.getId())) {
            this.overrides.add(u.getId());
        } else if (!override && this.overrides.contains(u.getId())) {
            this.overrides.remove(u.getId());
        }
    }
    
}
