package com.bendude56.goldenapple.chat;

import java.util.List;

import com.bendude56.goldenapple.User;
import com.bendude56.goldenapple.chat.ChatChannel.ChatChannelUserLevel;
import com.bendude56.goldenapple.permissions.PermissionManager.Permission;
import com.bendude56.goldenapple.permissions.PermissionManager.PermissionNode;

public abstract class ChatManager {
	// goldenapple.chat
	public static PermissionNode	chatNode;
	public static Permission		tellPermission;
	public static Permission        tellSpyPermission;

	// goldenapple.chat.channel
	public static PermissionNode	channelsNode;
	public static Permission		channelAddPermission;
	public static Permission		channelModPermission;
	public static Permission		channelAdminPermission;
	
	protected static ChatManager instance;
	
	public static ChatManager getInstance() {
		return instance;
	}
	
	public abstract void postInit();
	
	public abstract boolean getTellSpyStatus(User user);
	public abstract void setTellSpyStatus(User user, boolean spy);
	public abstract void removeReplyEntry(User user);
	
	public abstract void sendTellMessage(User sender, User receiver, String message);
	public abstract boolean sendReplyMessage(User sender, String message);
	
	public abstract void tryJoinChannel(User user, ChatChannel channel, boolean broadcast);
	public abstract void leaveChannel(User user, boolean broadcast);
	public abstract void kickFromChannel(User user);
	
	public abstract ChatChannelUserLevel getActiveChannelLevel(User user);
	public abstract ChatChannel getActiveChannel(User user);
	
	public abstract List<ChatChannel> getActiveChannels();
	public abstract ChatChannel getDefaultChannel();
	
	public abstract ChatChannel createTemporaryChannel(String identifier);
	public abstract ChatChannel createChannel(String identifier);
	
	public abstract boolean channelExists(String identifier);
	public abstract ChatChannel getChannel(String identifier);
	
	public abstract void deleteChannel(String identifier);
	
	protected abstract void removeChannelAttachment(User user);
	
	public abstract ChatCensor getDefaultCensor();
	public abstract ChatCensor getStrictCensor();
	
	public abstract void setAfkStatus(User user, boolean afk, boolean broadcast);
	public abstract boolean getAfkStatus(User user);
}
