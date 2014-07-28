package com.bendude56.goldenapple.permissions;

import java.util.List;

import org.bukkit.ChatColor;

/**
 * Represents a group in the GoldenApple permissions system. A group can have
 * users or other groups as members. Permissions and variables specified on a
 * group will propagate downwards to any users that are members, either directly
 * or indirectly, of this group.
 * 
 * @author ben_dude56
 */
public interface IPermissionGroup extends IPermissionObject {
    
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
     * Gets the priority of this group. Priority determines which group's
     * settings will apply if one or more groups have conflicting settings.
     * Groups with higher values will have their settings preferred over those
     * with lower values. The behaviour if two groups have the same priority is
     * <strong>undefined</strong> and should not be relied on.
     * 
     * @return An integer representing the priority of this group.
     */
    public int getPriority();
    
    /**
     * Changes the priority of this group. For further information about
     * priority, see {@link #getPriority()}.
     * 
     * @param priority An integer representation of the priority that this group
     * should have assigned to it.
     */
    public void setPriority(int priority);
    
    /**
     * Determines whether or not a chat color has been explicitly specified for
     * this group. If no chat color has been specified, then any color
     * information obtained from this group <strong>should be ignored</strong>.
     * 
     * @return True if a chat color has been specified for this group. False
     * otherwise.
     */
    public boolean isChatColorSet();
    
    /**
     * Gets the chat color that users in this group should inherit when their
     * name is displayed in chat. This color <strong>should be ignored</strong>
     * if {@link #isChatColorSet()} returns false.
     * 
     * @return The color that group members' names should be when displayed in
     * chat.
     */
    public ChatColor getChatColor();
    
    /**
     * Sets the chat color that users in this group should inherit when their
     * name is displayed on chat.
     * 
     * @param isSet Determines whether or not this chat color should be applied
     * to members. If false, the chat color of this group will be ignored.
     * @param color The color to display group members' names in. Ignored if
     * isSet is false.
     */
    public void setChatColor(boolean isSet, ChatColor color);
    
    /**
     * Gets the chat prefix that users in this group should inherit when they
     * talk in chat.
     * 
     * @return The prefix to be displayed alongside members' names when they
     * talk in chat. Returns {@code null} if no prefix has been specified.
     */
    public String getPrefix();
    
    /**
     * Sets the prefix that should be displayed alongside members' names when
     * they talk in chat. Set to {@code null} if no prefix should be inherited
     * from this group.
     * 
     * @param prefix The prefix to be applied to members, or {@code null} if no
     * prefix should be applied.
     */
    public void setPrefix(String prefix);
    
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
