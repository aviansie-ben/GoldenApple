package com.bendude56.goldenapple.permissions;

import java.util.List;

public interface IGroup extends IPermissionStored {
    
    /**
     * Gets the friendly name of the group that should be displayed to users and
     * will be used in commands. This name should not be used for any references
     * to this group, as it may be changed; instead, the internal ID number of
     * this group retrieved from {@link #getId()} should be used to refer to
     * this group.
     * 
     * @return The friendly name of this group.
     */
    public String getName();
    
    /**
     * Gets a list of IDs representing users that are explicitly part of this
     * group. This function does not check for users who are indirect members;
     * to list members including indirect members, use {@link #getAllUsers()}.
     * 
     * @return A list of IDs of users explicitly added to this group.
     */
    public List<Long> getUsers();
    
    /**
     * Gets a list of IDs representing users that are either explicitly part of
     * this group or are indirectly part of this group. To list members without
     * indirect members, use {@link #getUsers()}.
     * 
     * @return A list of IDs of all users inheriting settings from this group.
     */
    public List<Long> getAllUsers();
    
    /**
     * Explicitly adds a user to this group's member list. The user will
     * immediately begin inheriting any permissions and settings defined on this
     * group. If the user is already explicitly a part of this group, nothing
     * will occur.
     * 
     * @param user The user to be added to the membership list.
     */
    public void addUser(IPermissionUser user);
    
    /**
     * Removes a user from this group's member list. This method will not work
     * for removing members who are indirectly inheriting from this group. To
     * remove a user from this group if they are indirectly inheriting from it,
     * they must be removed from any groups that inherit from this group. If the
     * specified user is not a member of this group, nothing will occur.
     * 
     * @param user The user to be removed from the membership list.
     */
    public void removeUser(IPermissionUser user);
    
    /**
     * Checks whether a given user is inheriting from this group.
     * 
     * @param user The user that this group should be checked for.
     * @param directOnly If true, then this function will only check the
     * explicit user list and will skip checking for indirect inheritance.
     * 
     * @return True if the user is a member of this group. False otherwise.
     */
    public boolean isMember(IPermissionUser user, boolean directOnly);
    
    /**
     * Gets a list of IDs representing users who are considered to be owners of
     * this group and are capable of adding/removing users from this group's
     * membership list.
     * 
     * @return A list of IDs of all owners of this group.
     */
    public List<Long> getOwners();
    
    /**
     * Adds a user to this group's owner list. Group owners are capable of
     * adding and removing users to this group's membership list. They cannot
     * modify permissions nor can they add or remove groups from this group's
     * membership list.
     * 
     * @param owner The user to be made an owner of this group.
     */
    public void addOwner(IPermissionUser owner);
    
    /**
     * Removes a user from this group's owner list, preventing them from adding
     * or removing users from this group's membership list.
     * 
     * @param owner The user to be removed from this group's owner list.
     */
    public void removeOwner(IPermissionUser owner);
    
    /**
     * Checks whether a given user is an owner of this group.
     * 
     * @param user The user to check for ownership.
     * 
     * @return True if the specified user is an owner of this group. False
     * otherwise.
     */
    public boolean isOwner(IPermissionUser user);
    
    /**
     * Gets a list of IDs representing groups that are explicitly part of this
     * group. This function does not check for groups who are indirect members;
     * to list members including indirect members, use {@link #getAllGroups()}.
     * 
     * @return A list of IDs of groups explicitly added to this group.
     */
    public List<Long> getGroups();
    
    /**
     * Gets a list of IDs representing groups that are either explicitly part of
     * this group or are indirectly part of this group. To list members without
     * indirect members, use {@link #getGroups()}.
     * 
     * @return A list of IDs of all groups inheriting settings from this group.
     */
    public List<Long> getAllGroups();
    
    /**
     * Explicitly adds a group to this group's membership list. Any members of
     * the specified group will immediately begin inheriting any permissions and
     * settings defined on this group. If the group is already explicitly a part
     * of this group, nothing will occur.
     * 
     * @param group The group to be added to the membership list.
     */
    public void addGroup(IPermissionGroup group);
    
    /**
     * Removes a group from this group's member list. This method will not work
     * for removing members who are indirectly inheriting from this group. To
     * remove a group from this group if they are indirectly inheriting from it,
     * they must be removed from any groups that inherit from this group. If the
     * specified group is not a member of this group, nothing will occur.
     * 
     * @param group The group to be removed from the membership list.
     */
    public void removeGroup(IPermissionGroup group);
    
    /**
     * Checks whether a given group is inheriting from this group.
     * 
     * @param group The group that this group should be checked for.
     * @param directOnly If true, then this function will only check the
     * explicit group list and will skip checking for indirect inheritance.
     * 
     * @return True if the specified group is a member of this group. False
     * otherwise.
     */
    public boolean isMember(IPermissionGroup group, boolean directOnly);
    
    /**
     * Causes the properties of this group to be reloaded from the database. Any
     * changes made directly to the database will then be reflected in this
     * object.
     */
    public void reloadFromDatabase();
}
