package com.bendude56.goldenapple.area;

import java.lang.reflect.InvocationTargetException;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map.Entry;
import java.util.logging.Level;

import org.bukkit.Location;

import com.bendude56.goldenapple.GoldenApple;
import com.bendude56.goldenapple.permissions.IPermissionGroup;
import com.bendude56.goldenapple.permissions.IPermissionUser;
import com.bendude56.goldenapple.permissions.PermissionManager;

public class Area {
    private final long areaId;
    private String label = null;
    private int priority = 0;
    private HashMap<Long, AreaAccessLevel> users = null;
    private HashMap<Long, AreaAccessLevel> groups = null;
    private List<Long> regions = null;
    private List<AreaFlag> flags = null;
    
    protected Area(ResultSet r) throws SQLException, ClassNotFoundException {
        this.areaId = r.getLong("ID");
        this.label = r.getString("Label");
        this.priority = r.getInt("Priority");
    }
    
    /**
     * Gets the unique id for the area.
     * 
     * @return The area's id.
     */
    public long getAreaId() {
        return areaId;
    }
    
    /**
     * Gets the label for this area. Do not assume that returned value is a
     * valid item. Returned value could be an empty string or null.
     * 
     * @return This label's area or null if no label exists.
     */
    public String getLabel() {
        return label;
    }
    
    /**
     * Changes the value of the area's label. Only saves the first 128
     * characters. Null values are valid. Any string of length greater than 128
     * characters will be truncated, along with any trailing or leading
     * whitespace.
     * 
     * @param label The new label for the area.
     */
    public void setLabel(String label) {
        if (label != null) {
            /*if (label.length() > 128) {
                label = label.substring(0, 128);
            }*/
            this.label = label.trim();
        } else {
            this.label = label;
        }
        AreaManager.getInstance().updateAreaLabel(areaId, this.label);
    }
    
    /**
     * Removes the existing label from the area.
     */
    public void removeLabel() {
        this.label = null;
        
        AreaManager.getInstance().updateAreaLabel(areaId, this.label);
    }
    
    private HashMap<Long, AreaAccessLevel> getUserAccessLevels() {
        if (users == null) {
            users = SimpleAreaManager.getInstance().retrieveAreaUserAccessLevels(areaId);
        }
        return users;
    }
    
    private HashMap<Long, AreaAccessLevel> getGroupAccessLevels() {
        if (groups == null) {
            groups = SimpleAreaManager.getInstance().retrieveAreaGroupAccessLevels(areaId);
        }
        return groups;
    }
    
    /**
     * Changes a user's access level to the area. Also causes a database update,
     * so use sparingly.
     * 
     * @param userId The permission object representing the user.
     * @param level The new access level of the user.
     */
    public void setUserAccessLevel(long userId, AreaAccessLevel level) {
        if (level == null) {
            return;
        }
        
        if (level != AreaAccessLevel.NONE) {
            getUserAccessLevels().put(userId, level);
            AreaManager.getInstance().updateAreaUser(areaId, userId, level);
        } else if (getUserAccessLevels().containsKey(userId)) {
            getUserAccessLevels().remove(userId);
            AreaManager.getInstance().updateAreaUser(areaId, userId, level);
        }
    }
    
    /**
     * Changes a group's access level to the area. Also causes a database
     * update, so use sparingly.
     * 
     * @param groupId The permission object representing the group.
     * @param area The new access level of the group.
     */
    public void setGroupAccessLevel(long groupId, AreaAccessLevel area) {
        if (area == null) {
            return;
        }
        
        if (area != AreaAccessLevel.NONE) {
            getGroupAccessLevels().put(groupId, area);
            AreaManager.getInstance().updateAreaGroup(areaId, groupId, area);
        } else if (getGroupAccessLevels().containsKey(groupId)) {
            getGroupAccessLevels().remove(groupId);
            AreaManager.getInstance().updateAreaGroup(areaId, groupId, area);
        }
    }
    
    /**
     * Retrieves the access level a certain user has to the area.
     * 
     * @param userId The user to check access level for.
     * @return The AreaAccessLevel of the user.
     */
    public AreaAccessLevel getUserAccessLevel(long userId) {
        if (getUserAccessLevels().containsKey(userId)) {
            return getUserAccessLevels().get(userId);
        } else {
            return AreaAccessLevel.NONE;
        }
    }
    
    /**
     * Retrieves the access level a certain group has to the area.
     * 
     * @param groupId The group to check access level for.
     * @return The AreaAccessLevel of the group.
     */
    public AreaAccessLevel getGroupAccessLevel(long groupId) {
        if (getGroupAccessLevels().containsKey(groupId)) {
            return getGroupAccessLevels().get(groupId);
        } else {
            return AreaAccessLevel.NONE;
        }
    }
    
    /**
     * Retrieves a list of all users with any access level to this area above
     * NONE.
     * 
     * @return List of users with access to this area above NONE.
     */
    public List<IPermissionUser> getUsers() {
        List<IPermissionUser> users = new ArrayList<IPermissionUser>();
        
        for (Long id : getUserAccessLevels().keySet()) {
            users.add(PermissionManager.getInstance().getUser(id));
        }
        
        return users;
    }
    
    /**
     * Retrieves a list of all users that have access to this area matching the
     * given access level.
     * 
     * @param level The access level to match.
     * @return An ArrayList of users that have the matching access level as the
     * one specified.
     */
    public List<IPermissionUser> getUsers(AreaAccessLevel level) {
        List<IPermissionUser> users = new ArrayList<IPermissionUser>();
        
        for (Entry<Long, AreaAccessLevel> entry : getUserAccessLevels().entrySet()) {
            if (entry.getValue() == level) {
                users.add(PermissionManager.getInstance().getUser(entry.getKey()));
            }
        }
        
        return users;
    }
    
    /**
     * Retrieves a list of groups that have access to this area above NONE.
     * 
     * @return List of groups that have access to this area.
     */
    public List<IPermissionGroup> getGroups() {
        List<IPermissionGroup> groups = new ArrayList<IPermissionGroup>();
        
        for (Long id : getGroupAccessLevels().keySet()) {
            groups.add(PermissionManager.getInstance().getGroup(id));
        }
        
        return groups;
    }
    
    /**
     * Retrieves a list of groups that have access to this area matching the
     * given access level.
     * 
     * @param level The access level to match.
     * @return List of groups that have access to this area matching the given
     * access level.
     */
    public List<IPermissionGroup> getGroups(AreaAccessLevel level) {
        List<IPermissionGroup> groups = new ArrayList<IPermissionGroup>();
        
        for (Entry<Long, AreaAccessLevel> entry : getGroupAccessLevels().entrySet()) {
            if (entry.getValue() == level) {
                groups.add(PermissionManager.getInstance().getGroup(entry.getKey()));
            }
        }
        
        return groups;
    }
    
    private List<AreaFlag> getFlagList() {
        if (flags == null) {
            flags = SimpleAreaManager.getInstance().retrieveAreaFlags(areaId);
        }
        return flags;
    }
    
    /**
     * Returns a list of all flags set for this area.
     * 
     * @return ArrayList of set flags.
     */
    public List<AreaFlag> getFlags() {
        return new ArrayList<AreaFlag>(getFlagList());
    }
    
    /**
     * Checks whether a certain flag is set on this area.
     * 
     * @param flag The flag to check
     * @return True if the flag is set, false if not.
     */
    public boolean checkFlag(AreaFlag flag) {
        return (getFlagList().contains(flag));
    }
    
    /**
     * Sets or resets (removes) a flag on this area. Also updates entry in
     * database automatically, so use sparingly.
     * 
     * @param flag The flag to modify.
     * @param set True to set the flag, false to reset it.
     */
    public void setFlag(AreaFlag flag, boolean set) {
        if (set && !getFlagList().contains(flag)) {
            getFlagList().add(flag);
            AreaManager.getInstance().updateAreaFlag(areaId, flag, set);
        } else if (!set && getFlagList().contains(flag)) {
            getFlagList().remove(flag);
            AreaManager.getInstance().updateAreaFlag(areaId, flag, set);
        }
    }
    
    /**
     * Gets a list of ids of regions belonging to the area.
     * 
     * @return List of region ids belonging to the area.
     */
    public List<Long> getRegionIds() {
        if (regions == null) {
            regions = SimpleAreaManager.getInstance().retrieveAreaRegionIds(areaId);
        }
        return regions;
    }
    
    /**
     * Gets a list of regions assigned to this area.
     * 
     * @return List of regions belonging to this area.
     */
    public List<Region> getRegions() {
        List<Region> list = new ArrayList<Region>();
        for (Long id : getRegionIds()) {
            list.add(AreaManager.getInstance().getRegion(id));
        }
        return list;
    }
    
    /**
     * Creates a new region and adds it to both this area and the database.
     * 
     * @param shape The shape of the new region.
     * @param c1 The first corner of the region.
     * @param c2 The second corner of the region.
     * @param ignoreY True if the region should enclose the entire y-axis, false
     * if the region's y-axis should remain bounded by the given coordinates.
     * @return The newly created Region or null if an error occured.
     */
    public Region addRegion(RegionShape shape, Location c1, Location c2, boolean ignoreY) {
        if (shape == null || c1 == null || c2 == null) {
            return null;
        }
        Region r;
        try {
            r = AreaManager.getInstance().createRegion(areaId, shape, c1, c2, ignoreY);
            if (regions != null) {
                regions.add(r.getId());
            }
            return r;
        } catch (SQLException e) {
            GoldenApple.log(Level.SEVERE, "A database error occured while attempting to add a new region to area " + areaId);
            GoldenApple.log(Level.SEVERE, e);
        } catch (InvocationTargetException e) {
            GoldenApple.log(Level.SEVERE, "An error occured while attempting to add a new region to area " + areaId);
            GoldenApple.log(Level.SEVERE, "Please send the following stack trace to the creators of GoldenApple:");
            GoldenApple.log(Level.SEVERE, e);
        }
        return null;
    }
    
    /**
     * Deletes an existing region from the area. Region must be given by ID.
     * Removes the region from the database.
     * 
     * @param regionId The ID of the region to be deleted from the area.
     */
    public void deleteRegion(long regionId) {
        if (regions != null && regions.contains(regionId)) {
            regions.remove(regionId);
        }
        AreaManager.getInstance().deleteRegion(regionId);
    }
    
    /**
     * Gets the priority of the Area. Areas with higher priority take greater
     * precedence in permission checks. Priority value may be positive or
     * negative.
     * 
     * @return Current priority of the Area. May be positive or negative.
     */
    public int getPriority() {
        return priority;
    }
    
    /**
     * Sets the priority of the Area. Areas with higher priority take greater
     * precedence in permission checks. Priority value may be positive or
     * negative.
     * 
     * @param priority New priority of the Area. May be positive or negative.
     */
    public void setPriority(int priority) {
        this.priority = priority;
        AreaManager.getInstance().updateAreaPriority(areaId, priority);
    }
    
    /**
     * Saves the area's regions that area already cached. If no region ids are
     * cached, will do nothing.
     */
    public void saveRegions() {
        
        // Save only if region ids area cached
        if (regions == null) {
            return;
        }
        
        for (long regionId : regions) {
            AreaManager.getInstance().saveRegion(regionId);
        }
    }
    
    /**
     * Checks if a user can edit blocks within this area. Includes inheritance
     * checks and area access level checks (owner/guest).
     * 
     * @param u The IPermissionUser to check permissions for.
     * @return True if the user has permission to edit blocks in this area,
     * false if not. A player has permission if they or a group they belong to
     * has the "goldenapple.area.ignore" permission or is a guest or owner of
     * the area.
     */
    public boolean canEditBlocks(IPermissionUser u) {
        AreaAccessLevel level = getEffectiveAccessLevel(u);
        
        if (level.getComparableValue() >= AreaAccessLevel.GUEST.getComparableValue()) {
            return true;
        }
        
        if (u.hasPermission(AreaManager.overridePermission, true) && AreaManager.getInstance().isOverrideOn(u)) {
            return true;
        }
        
        if (AreaManager.getInstance().isOverrideOn(u)) {
            return true;
        }
        
        return false;
    }
    
    /**
     * Checks if a group can edit blocks within this area. Includes inheritance
     * checks and area access level checks (owner/guest).
     * 
     * @param g The IPermissionGroup to check permissions for.
     * @return True if the group has permission to edit blocks in this area,
     * false if not. A group has permission if it has or inherits the permission
     * "goldenapple.area.ignore" or is a guest or owner of the area.
     */
    public boolean canEditBlocks(IPermissionGroup g) {
        AreaAccessLevel level = getEffectiveAccessLevel(g);
        
        if (level.getComparableValue() >= AreaAccessLevel.GUEST.getComparableValue()) {
            return true;
        }
        
        if (g.hasPermission(AreaManager.overridePermission, true)) {
            return true;
        }
        
        return false;
    }
    
    /**
     * Checks the effective access level of the IPermissionUser. Differs from
     * getUserAccessLevel() by both accepting an IPermissionUser rather than
     * just an ID and checks group access levels, including inheritance.
     * 
     * @param u The user to check access level for.
     * @return The greatest AreaAccessLevel that the user and any parent groups
     * has to the area.
     */
    public AreaAccessLevel getEffectiveAccessLevel(IPermissionUser u) {
        AreaAccessLevel level = getUserAccessLevel(u.getId());
        
        for (Long groupId : u.getParentGroups(true)) {
            if (level == AreaAccessLevel.OWNER) {
                break;
            }
            AreaAccessLevel l = getEffectiveAccessLevel(PermissionManager.getInstance().getGroup(groupId));
            if (l.getId() > level.getId()) {
                level = l;
            }
        }
        
        return level;
    }
    
    /**
     * Checks the effective access level of the IPermissionGroup. Differs from
     * getGroupAccessLevel() by checking parent groups and taking inheritance
     * into account.
     * 
     * @param g The group to check access level for.
     * @return The greatest AreaAccessLevel that the group has to the area.
     */
    public AreaAccessLevel getEffectiveAccessLevel(IPermissionGroup g) {
        AreaAccessLevel level = getGroupAccessLevel(g.getId());
        
        for (Long parentId : g.getParentGroups(false)) {
            AreaAccessLevel l = getGroupAccessLevel(parentId);
            if (l.getId() > level.getId()) {
                level = l;
            }
        }
        
        return level;
    }
}
