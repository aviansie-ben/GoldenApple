package com.bendude56.goldenapple.chat;

import java.util.List;

import com.bendude56.goldenapple.User;
import com.bendude56.goldenapple.chat.IChatChannel.ChatChannelAccessLevel;
import com.bendude56.goldenapple.permissions.PermissionManager.Permission;
import com.bendude56.goldenapple.permissions.PermissionManager.PermissionNode;

public abstract class ChatManager {
    // goldenapple.chat
    public static PermissionNode chatNode;
    public static Permission tellPermission;
    public static Permission tellSpyPermission;
    
    // goldenapple.chat.channel
    public static PermissionNode channelsNode;
    public static Permission channelAddPermission;
    public static Permission channelModPermission;
    public static Permission channelAdminPermission;
    
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
    
    public abstract void setActiveChannel(User user, IChatChannel channel);
    
    public abstract ChatChannelAccessLevel getActiveChannelLevel(User user);
    public abstract IChatChannel getActiveChannel(User user);
    
    public abstract List<IChatChannel> getActiveChannels();
    public abstract void removeChannel(IChatChannel channel);
    public abstract IChatChannel getDefaultChannel();
    
    public abstract IChatChannel createChannel(String identifier);
    
    public abstract boolean channelExists(String identifier);
    public abstract IChatChannel getChannel(String identifier);
    
    protected abstract void removeChannelAttachment(User user);
    
    public abstract IChatCensor getDefaultCensor();
    public abstract IChatCensor getStrictCensor();
    
    public abstract void setAfkStatus(User user, boolean afk, boolean broadcast);
    public abstract boolean getAfkStatus(User user);
}
