package com.bendude56.goldenapple.permissions;

import org.bukkit.ChatColor;

/**
 * Represents a group in the GoldenApple permissions system. A group can have
 * users or other groups as members. Permissions and variables specified on a
 * group will propagate downwards to any users that are members, either directly
 * or indirectly, of this group.
 * 
 * @author ben_dude56
 */
public interface IPermissionGroup extends IGroup, IPermissionObject {
    
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
}
