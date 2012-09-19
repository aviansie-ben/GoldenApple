package com.bendude56.goldenapple.chat;

import com.bendude56.goldenapple.chat.ChatChannel.ChannelAccessLevel;
import com.bendude56.goldenapple.permissions.IPermissionObject;
import com.bendude56.goldenapple.permissions.IPermissionUser;

public interface IChatChannel {
	/**
	 * Gets the ID representing this specific chat channel
	 */
	public long getId();

	/**
	 * Gets the name of this chat channel that should be displayed to the
	 * end-user
	 */
	public String getName();

	/**
	 * Gets a boolean representing whether this chat channel is permanent or
	 * temporary. Temporary chat channels should not be saved to the database,
	 * and should be used in memory only. In addition, when empty, they should
	 * be automatically deleted.
	 */
	public boolean isTemporary();

	/**
	 * Gets the specific access level of a particular permissions object. This
	 * should not be used to check if a player can join, as it does NOT check
	 * inheritance.
	 * 
	 * @param obj The object to check against
	 */
	public ChannelAccessLevel getSpecificAccess(IPermissionObject obj);

	/**
	 * Gets the access level that a user would be given if they were to enter
	 * the channel. Takes inheritance into account. The user's direct level
	 * ALWAYS overrides any group level, whereas group levels are
	 * least-restrictive.
	 * 
	 * @param user The user to check against
	 */
	public ChannelAccessLevel getAccess(IPermissionUser user);
}
