package com.bendude56.goldenapple.area;

import java.lang.reflect.InvocationTargetException;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.List;

import org.bukkit.Location;

import com.bendude56.goldenapple.User;
import com.bendude56.goldenapple.permissions.IPermissionUser;
import com.bendude56.goldenapple.permissions.PermissionManager.Permission;
import com.bendude56.goldenapple.permissions.PermissionManager.PermissionNode;

public abstract class AreaManager {
    /**
     * Root permission node for the entire Area module. Parent permission node:
     * goldenapple.area
     */
    public static PermissionNode areaNode;
    /**
     * Permission node to retrieve a list of areas.
     */
    public static PermissionNode areaListNode;
    /**
     * Permission node to view information of an area.
     */
    public static PermissionNode areaInfoNode;
    /**
     * Permission node to edit the properties of existing areas.
     */
    public static PermissionNode areaEditNode;
    /**
     * Permission node to edit the properties of existing areas that the user is
     * an owner of.
     */
    public static PermissionNode areaEditOwnNode;
    /**
     * Permission to create new areas.
     */
    public static Permission addPermission;
    /**
     * Permission to delete existing areas.
     */
    public static Permission removePermission;
    /**
     * Permission to list all areas on the server.
     */
    public static Permission listAllPermission;
    /**
     * Permission to list all areas at a user's current location.
     */
    public static Permission listLocationPermission;
    /**
     * Permission to list all areas owned by the user.
     */
    public static Permission listOwnPermission;
    /**
     * Permission to get information on any area on the server.
     */
    public static Permission infoAllPermission;
    /**
     * Permission to get information on any area owned by the user.
     */
    public static Permission infoOwnPermission;
    /**
     * Permission to override building restrictions.
     */
    public static Permission overridePermission;
    /**
     * Permission to edit an area's label.
     */
    public static Permission editLabelPermission;
    /**
     * Permission to change an area's priority.
     */
    public static Permission editPriorityPermission;
    /**
     * Permission to change an area's owners.
     */
    public static Permission editOwnersPermission;
    /**
     * Permission to change an area's group owners.
     */
    public static Permission editGroupOwnersPermission;
    /**
     * Permission to change an area's guests.
     */
    public static Permission editGuestsPermission;
    /**
     * Permission to change an area's group guests.
     */
    public static Permission editGroupGuestsPermission;
    /**
     * Permission to add/remove regions to/from an area.
     */
    public static Permission editRegionsPermission;
    /**
     * Permission to change an area's flags.
     */
    public static Permission editFlagsPermission;
    /**
     * Permission for user to change their own area's label.
     */
    public static Permission editOwnLabelPermission;
    /**
     * Permission for user to change their own area's priority.
     */
    public static Permission editOwnPriorityPermission;
    /**
     * Permission for user to change their own area's owners.
     */
    public static Permission editOwnOwnersPermission;
    /**
     * Permission for user to change their own area's group owners.
     */
    public static Permission editOwnGroupOwnersPermission;
    /**
     * Permission for user to change their own area's guests.
     */
    public static Permission editOwnGuestsPermission;
    /**
     * Permission for user to change their own area's group guests.
     */
    public static Permission editOwnGroupGuestsPermission;
    /**
     * Permission for user to modify their own area's regions.
     */
    public static Permission editOwnRegionsPermission;
    /**
     * Permission for user to modify their own area's flags.
     */
    public static Permission editOwnFlagsPermission;
    
    /**
     * Static reference to the class instance.
     */
    protected static AreaManager instance;
    
    /**
     * Gets the active instance of AreaManager.
     * 
     * @return The active instance of the AreaManager.
     */
    public static AreaManager getInstance() {
        return instance;
    }
    
    /**
     * Gets an area based on id.
     * 
     * @param id The id of the area to get.
     * @return The area with matching id or null if none is found.
     */
    public abstract Area getArea(long id);
    
    /**
     * Gets an area based on label. If no areas have a matching label, will
     * return null.
     * 
     * @param label The label to search for.
     * @return The area with matching label or null if none.
     */
    public abstract Area getArea(String label);
    
    /**
     * Fetches a sorted list of Areas that cover the given location, sorted by
     * the value of their priorities in descending order. Sorting behavior in
     * regards to areas with matching priorities is undefined.
     * 
     * @param l The location to search for.
     * @return An ArrayList of Areas that cover the given location.
     */
    public abstract List<Area> getAreas(Location l);
    
    /**
     * Fetches a list of Areas sorted by ID in ascending order, starting at the
     * given page and including the indicated number of results per page.
     * 
     * @param page The page to start on.
     * @param per The number of results per page.
     * @return ArrayList of Areas
     */
    public abstract List<Area> getAreas(int page, int per);
    
    /**
     * Fetches a sorted list of Areas that are owned either directly or
     * indirectly (through group membership) by the user with the given id.
     * Sorted by area ID in ascending order.
     * 
     * @param userId The ID of the user to search for.
     * @return An ArrayList of Areas that the given user has ownership rights
     * rights to.
     */
    public abstract List<Area> getAreasByOwner(long userId);
    
    /**
     * Gets the total number of areas in the database.
     * 
     * @return Returns the total number of areas that exist on the server.
     */
    public abstract int getTotalAreas();
    
    /**
     * Fetches a hashmap of user access levels for a given area.
     * 
     * @param areaId The id of the area to search for.
     * @return A HashMap of user id to AreaAccessLevel relationships.
     */
    public abstract HashMap<Long, AreaAccessLevel> retrieveAreaUserAccessLevels(long areaId);
    
    /**
     * Fetches a hashmap of group access levels for a given area.
     * 
     * @param areaId The id of the area to search for.
     * @return A HashMap of group id to AreaAccessLevel relationships.
     */
    public abstract HashMap<Long, AreaAccessLevel> retrieveAreaGroupAccessLevels(long areaId);
    
    /**
     * Fetches an unsorted list of flags set for an area.
     * 
     * @param areaId The id of the area to search for.
     * @return ArrayList of the given Area's set AreaFlags.
     */
    public abstract List<AreaFlag> retrieveAreaFlags(long areaId);
    
    /**
     * Gets a region based on region id.
     * 
     * @param id The id of the region to get.
     * @return The region with matching id or null of none is found.
     */
    public abstract Region getRegion(long id);
    
    /**
     * Fetches an unsorted list of regions that cover the given location.
     * 
     * @param l The location to search for.
     * @return An ArrayList of Areas that cover the given location.
     */
    public abstract List<Region> getRegions(Location l);
    
    /**
     * Fetches all region ids belonging to specific area.
     * 
     * @param id The id of the area to search for.
     * @return ArrayList of region ids belonging to the area with the given id.
     */
    public abstract List<Long> retrieveAreaRegionIds(long id);
    
    /**
     * Checks if an Area with the given id exists.
     * 
     * @param id The id of the area to check for.
     * @return True if the area exists, false if not.
     * @throws SQLException
     */
    public abstract boolean areaExists(long id) throws SQLException;
    
    /**
     * Creates a new area with a region to go with the area with the given
     * specifications. Adds the area to the database and caches the newly
     * created area.
     * 
     * @param owner The user to be made owner of the area.
     * @param label The label of the area.
     * @param priority The priority of the area.
     * @param shape The shape that the area's region should take.
     * @param c1 The first corner of the area's region.
     * @param c2 The second corner of the area's region.
     * @param ignoreY True if the area's region should span from bedrock to
     * skybox, false if not.
     * @return The newly created Area or null if an error occurred.
     * @throws SQLException
     * @throws InvocationTargetException
     */
    public abstract Area createArea(IPermissionUser owner, String label, int priority, RegionShape shape, Location c1, Location c2, boolean ignoreY) throws SQLException, InvocationTargetException;
    
    /**
     * Updates an area's label in the database.
     * 
     * @param areaId The id of the area to update.
     * @param label The value of the area's label. Null values are valid.
     */
    protected abstract void updateAreaLabel(long areaId, String label);
    
    /**
     * Updates an area's priority in the database.
     * 
     * @param areaId The id of the area to update.
     * @param priority The new priority of the area.
     */
    protected abstract void updateAreaPriority(long areaId, int priority);
    
    /**
     * Updates a user's access level to an area in the database. Does not update
     * cached areas.
     * 
     * @param areaId The id of the area to update.
     * @param userId The id of the user to update.
     * @param level The new level of the user. If set to NONE, will delete entry
     * from database.
     */
    protected abstract void updateAreaUser(long areaId, long userId, AreaAccessLevel level);
    
    /**
     * Updates a group's access level to an area in the database. Does not
     * update cached areas.
     * 
     * @param areaId The id of the area to update.
     * @param groupId The id of the group to update.
     * @param level The new level of the group. If set to NONE, will delete
     * entry from the database.
     */
    protected abstract void updateAreaGroup(long areaId, long groupId, AreaAccessLevel level);
    
    /**
     * Sets or resets (removes) a flag for a specific area.
     * 
     * @param areaId The ID of the area to modify the flag for.
     * @param flag The flag to set/reset.
     * @param set The new state of the flag. True to set it, false to reset it.
     */
    protected abstract void updateAreaFlag(long areaId, AreaFlag flag, boolean set);
    
    /**
     * Deletes an existing area with the given id. If no area exists with given
     * id, will silently fail.
     * 
     * @param id The id of the Area to delete.
     * @return True if the area is successfully deleted, false if not.
     * @throws SQLException
     */
    public abstract boolean deleteArea(long id) throws SQLException;
    
    /**
     * Create a new region and add it to the area with the given id.
     * 
     * @param areaId The id of the area to add this region to.
     * @param shape The shape of the region.
     * @param c1 The first corner of the region's bounding box.
     * @param c2 The second corner of the region's bounding box.
     * @param ignoreY Weather or not the region is to ignore the Y-axis.
     * @return The newly created region, or null if an error occurred.
     * @throws SQLException
     * @throws InvocationTargetException
     */
    protected abstract Region createRegion(long areaId, RegionShape shape, Location c1, Location c2, boolean ignoreY) throws SQLException, InvocationTargetException;
    
    /**
     * Given a region id, will update the region's properties in the database.
     * If the region is not already loaded into cache, will not save the region.
     * 
     * @param regionId The id of the region to save.
     */
    protected abstract void saveRegion(long regionId);
    
    /**
     * Deletes an existing region with the given id from the database and any
     * cached memory locations.
     * 
     * @param regionId The region to delete.
     */
    protected abstract boolean deleteRegion(long regionId);
    
    /**
     * Checks if a given user adequate permissions to edit anything at the given
     * location. Performs both a permission check and an area access level
     * check. Only checks against the area at the given location with the
     * greatest priority. If multiple areas exist at the given location, will
     * return true if user has permission to edit at least one of the areas.
     * 
     * @param u The user to check for.
     * @param loc The location to check at.
     * @return True if the user has proper permissions, false if not.
     */
    public boolean canEditAtLocation(User u, Location loc) {
        if (u == null || loc == null) {
            return false;
        }
        
        if (u.hasPermission(AreaManager.overridePermission) && checkOverride(u)) {
            return true;
        }
        
        List<Area> areas = AreaManager.getInstance().getAreas(loc);
        int priority;
        
        if (areas.isEmpty()) {
            return true;
        } else {
            priority = areas.get(0).getPriority();
        }
        
        // Sort areas by priority
        for (Area a : areas) {
            if (a.getPriority() != priority) {
                break;
            }
            
            if (a.canEditBlocks(u)) {
                return true;
            }
        }
        
        return false;
    }
    
    /**
     * Determines whether the given user has the permissions required to
     * override Area permissions.
     * 
     * @param u The user to check permissions for.
     * @return True if the user can override permissions, false if not.
     */
    public static boolean canOverride(IPermissionUser u) {
        return u.hasPermission(AreaManager.overridePermission);
    }
    
    /**
     * Checks whether or not the user is currently overriding Area permissions.
     * 
     * @param u The user to check for.
     * @return True if the user is currently overriding area permissions.
     */
    public abstract boolean checkOverride(IPermissionUser u);
    
    /**
     * Sets the overriding status of the user.
     * 
     * @param u The user to set the status for.
     * @param override The new status of the user. True for overriding, false to
     * not.
     */
    public abstract void setOverride(IPermissionUser u, boolean override);
}
