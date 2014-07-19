package com.bendude56.goldenapple.chat;

import java.util.HashMap;

import com.bendude56.goldenapple.User;
import com.bendude56.goldenapple.permissions.IPermissionGroup;
import com.bendude56.goldenapple.permissions.IPermissionObject;
import com.bendude56.goldenapple.permissions.IPermissionUser;
import com.bendude56.goldenapple.permissions.PermissionManager;

public abstract class BaseAclChatChannel extends BaseChatChannel implements IAclChatChannel {
    private HashMap<Long, ChatChannelAccessLevel> userLevels;
    private HashMap<Long, ChatChannelAccessLevel> groupLevels;
    
    public BaseAclChatChannel(String name) {
        super(name);
        
        this.userLevels = new HashMap<Long, ChatChannelAccessLevel>();
        this.groupLevels = new HashMap<Long, ChatChannelAccessLevel>();
    }
    
    @Override
    public ChatChannelAccessLevel calculateAccessLevel(IPermissionUser user) {
        ChatChannelAccessLevel minLevel = calculateMinimumAccessLevel(user);
        ChatChannelAccessLevel explicitLevel = getExplicitAccessLevel(user);
        
        return (explicitLevel == null || minLevel.getLevelId() > explicitLevel.getLevelId()) ? minLevel : explicitLevel;
    }
    
    @Override
    public ChatChannelAccessLevel calculateMinimumAccessLevel(IPermissionUser user) {
        ChatChannelAccessLevel level = getDefaultAccessLevel();
        
        if (user.hasPermission(ChatManager.channelAdminPermission)) {
            level = ChatChannelAccessLevel.ADMINISTRATOR;
        } else if (user.hasPermission(ChatManager.channelModPermission)) {
            level = ChatChannelAccessLevel.MODERATOR;
        }
        
        for (Long group : user.getParentGroups(false)) {
            ChatChannelAccessLevel newLevel = getExplicitAccessLevel(PermissionManager.getInstance().getGroup(group));
            
            if (newLevel != null && newLevel.getLevelId() > level.getLevelId()) {
                level = newLevel;
            }
        }
        
        return level;
    }
    
    @Override
    public ChatChannelAccessLevel getExplicitAccessLevel(IPermissionObject obj) {
        if (obj instanceof IPermissionUser) {
            return userLevels.get(obj.getId());
        } else if (obj instanceof IPermissionGroup) {
            return groupLevels.get(obj.getId());
        } else {
            throw new IllegalArgumentException("Unrecognized permission object type!");
        }
    }
    
    @Override
    public void setExplicitAccessLevel(IPermissionObject obj, ChatChannelAccessLevel level) {
        if (obj instanceof IPermissionUser) {
            if (level != null && level != ChatChannelAccessLevel.NO_ACCESS) {
                userLevels.put(obj.getId(), level);
            } else {
                userLevels.remove(obj);
            }
        } else if (obj instanceof IPermissionGroup) {
            if (level != null && level != ChatChannelAccessLevel.NO_ACCESS) {
                groupLevels.put(obj.getId(), level);
            } else {
                groupLevels.remove(obj);
            }
        } else {
            throw new IllegalArgumentException("Unrecognized permission object type!");
        }
    }
    
    @Override
    public void sendWhoisInformation(User user, IPermissionUser target) {
        ChatChannelAccessLevel level;
        
        super.sendWhoisInformation(user, target);
        
        if (getAccessLevel(target) != ChatChannelAccessLevel.NO_ACCESS) {
            user.sendLocalizedMessage("general.channel.whois.level.sourceHead");
            
            for (Long groupId : target.getParentGroups(false)) {
                IPermissionGroup group = PermissionManager.getInstance().getGroup(groupId);
                level = getExplicitAccessLevel(group);
                
                if (level != null && level != ChatChannelAccessLevel.NO_ACCESS) {
                    user.sendLocalizedMessage("general.channel.whois.level.sourceGroup", group.getName(), level.getDisplayName(user));
                }
            }
            
            level = getExplicitAccessLevel(target);
            
            if (level != null && level != ChatChannelAccessLevel.NO_ACCESS) {
                user.sendLocalizedMessage("general.channel.whois.level.sourceUser", target.getName(), level.getDisplayName(user));
            }
            
            level = getDefaultAccessLevel();
            
            if (level != null && level != ChatChannelAccessLevel.NO_ACCESS) {
                user.sendLocalizedMessage("general.channel.whois.level.sourceDefault", level.getDisplayName(user));
            }
            
            if (target.hasPermission(ChatManager.channelAdminPermission)) {
                user.sendLocalizedMessage("general.channel.whois.level.sourcePermission", ChatChannelAccessLevel.ADMINISTRATOR.getDisplayName(user));
            } else if (target.hasPermission(ChatManager.channelModPermission)) {
                user.sendLocalizedMessage("general.channel.whois.level.sourcePermission", ChatChannelAccessLevel.MODERATOR.getDisplayName(user));
            }
        }
    }
    
    @Override
    public boolean isFeatureAccessible(User user, ChatChannelFeature feature) {
        if (feature == ChatChannelFeature.SET_ACCESS_LEVELS) {
            return true;
        } else {
            return super.isFeatureAccessible(user, feature);
        }
    }
}
