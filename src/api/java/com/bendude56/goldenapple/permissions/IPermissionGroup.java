package com.bendude56.goldenapple.permissions;

import java.util.List;

import org.bukkit.ChatColor;

public interface IPermissionGroup extends IPermissionObject {
	/**
	 * Gets the name of the group represented by this object. The name of a
	 * group should be used for <strong>user input only</strong>! Where a
	 * persistent reference to a group is necessary, it is preferable to use the
	 * ID returned by {@link IPermissionObject#getId()}.
	 */
	public String getName();

	/**
	 * Gets the priority of this group. A group with a higher priority will have
	 * its settings (e.g. chat prefix) override those set by a group with a
	 * lower priority.
	 */
	public int getPriority();

	/**
	 * Sets the priority of this group. See
	 * {@link IPermissionGroup#getPriority()} for information on the effects of
	 * priority.
	 * 
	 * @param priority The priority that this group should be given.
	 */
	public void setPriority(int priority);

	/**
	 * Checks whether this group has an explicitly defined chat color that it
	 * should push down to child users.
	 * 
	 * @return True if a chat color has been set, false otherwise.
	 */
	public boolean isChatColorSet();

	/**
	 * Gets the color that should be inherited by users from this group. If
	 * {@link IPermissionGroup#isChatColorSet()} returns false, this color
	 * should not be used and will likely be a junk value.
	 */
	public ChatColor getChatColor();

	/**
	 * Gets the prefix that any child users should have when their names are
	 * displayed in chat.
	 * 
	 * @return The chat prefix, if it has been set, null otherwise.
	 */
	public String getPrefix();

	/**
	 * Sets the color that any users of this group should have when they speak
	 * in chat.
	 * 
	 * @param isSet Whether this group should have users inherit its chat color.
	 * @param color The color that users of this group should inherit. If
	 *            <tt>isSet</tt> is false, this value has no effect.
	 */
	public void setChatColor(boolean isSet, ChatColor color);

	/**
	 * Sets the prefix that should be displayed for users who are part of this
	 * group in chat.
	 * 
	 * @param prefix The prefix that users should be given. <tt>null</tt> if
	 *            users should not inherit a prefix from this group.
	 */
	public void setPrefix(String prefix);

	public List<Long> getUsers();

	public List<Long> getAllUsers();

	public void addUser(IPermissionUser user);

	public void removeUser(IPermissionUser user);

	public boolean isMember(IPermissionUser user, boolean directOnly);

	public List<Long> getGroups();

	public List<Long> getAllGroups();

	public void addGroup(IPermissionGroup group);

	public void removeGroup(IPermissionGroup group);

	public boolean isMember(IPermissionGroup group, boolean directOnly);
	
	public void reloadFromDatabase();
}
