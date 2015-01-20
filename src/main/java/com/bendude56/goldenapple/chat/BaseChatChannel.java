package com.bendude56.goldenapple.chat;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.logging.Level;

import org.bukkit.ChatColor;

import com.bendude56.goldenapple.GoldenApple;
import com.bendude56.goldenapple.User;
import com.bendude56.goldenapple.permissions.IPermissionUser;
import com.bendude56.goldenapple.punish.PunishmentManager;
import com.bendude56.goldenapple.punish.PunishmentMute;

public abstract class BaseChatChannel implements IChatChannel {
    private final String name;
    private String displayName;
    
    private IChatCensor censor;
    private String motd;
    private ChatChannelAccessLevel defaultLevel;
    
    private HashMap<User, ChatChannelAccessLevel> cachedAccessLevels;
    private List<User> activeUsers;
    private List<User> listeningUsers;
    
    public BaseChatChannel(String name) {
        this.name = name;
        this.displayName = null;
        
        this.censor = SimpleChatCensor.defaultChatCensor;
        this.motd = null;
        this.defaultLevel = ChatChannelAccessLevel.NO_ACCESS;
        
        this.cachedAccessLevels = new HashMap<User, ChatChannelAccessLevel>();
        this.activeUsers = new ArrayList<User>();
        this.listeningUsers = new ArrayList<User>();
    }
    
    @Override
    public final String getName() {
        return this.name;
    }
    
    @Override
    public final String getDisplayName() {
        return (this.displayName == null) ? this.name : this.displayName;
    }
    
    @Override
    public void setDisplayName(String displayName) {
        this.displayName = displayName;
    }
    
    @Override
    public boolean join(User user, boolean broadcast) {
        ChatChannelAccessLevel level = calculateAccessLevel(user);
        
        if (!level.canJoin()) {
            user.sendLocalizedMessage("module.chat.error.notAllowed.join");
            return false;
        }
        
        if (ChatManager.getInstance().getActiveChannel(user) != null) {
            ChatManager.getInstance().getActiveChannel(user).leave(user, true);
        }
        
        user.sendLocalizedMessage("module.chat.join.success", getDisplayName());
        
        if (broadcast) {
            broadcastLocalizedMessage("module.chat.join.broadcast", user.getChatDisplayName());
        }
        
        if (!level.canChat()) {
            user.sendLocalizedMessage("module.chat.join.warning.noTalk");
        }
        
        if (PunishmentManager.getInstance() != null && PunishmentManager.getInstance().isMuted(user, this)) {
            PunishmentMute m = PunishmentManager.getInstance().getActiveMute(user, this);
            if (m.isPermanent()) {
                user.sendLocalizedMessage("module.chat.join.warning.muted.perm");
            } else {
                user.sendLocalizedMessage("module.chat.join.warning.muted.temp", m.getRemainingDuration().toString(user));
            }
        }
        
        if (motd != null) {
            user.getHandle().sendMessage(ChatColor.YELLOW + motd);
        }
        
        setCachedAccessLevel(user, level);
        activeUsers.add(user);
        
        ChatManager.getInstance().setActiveChannel(user, this);
        
        return true;
    }
    
    @Override
    public void leave(User user, boolean broadcast) {
        if (!isInChannel(user)) {
            throw new IllegalArgumentException("Attempt to have user leave channel they are not in!");
        }
        
        setCachedAccessLevel(user, null);
        activeUsers.remove(user);
        
        ChatManager.getInstance().setActiveChannel(user, null);
        
        user.sendLocalizedMessage("module.chat.leave.success", getDisplayName());
        
        if (broadcast) {
            broadcastLocalizedMessage("module.chat.leave.broadcast", user.getChatDisplayName());
        }
    }
    
    @Override
    public void kick(User user, boolean broadcast) {
        if (!isInChannel(user)) {
            throw new IllegalArgumentException("Attempt to kick user from channel they are not in!");
        }
        
        setCachedAccessLevel(user, null);
        activeUsers.remove(user);
        
        ChatManager.getInstance().setActiveChannel(user, null);
        
        user.sendLocalizedMessage("module.chat.kick.notify", getDisplayName());
        
        if (broadcast) {
            broadcastLocalizedMessage("module.chat.kick.broadcast", user.getChatDisplayName());
        }
    }
    
    @Override
    public final IChatCensor getCensor() {
        return this.censor;
    }
    
    @Override
    public void setCensor(IChatCensor censor) {
        this.censor = censor;
    }
    
    @Override
    public final String getMotd() {
        return this.motd;
    }
    
    @Override
    public void setMotd(String motd) {
        this.motd = motd;
    }
    
    @Override
    public final ChatChannelAccessLevel getDefaultAccessLevel() {
        return this.defaultLevel;
    }
    
    @Override
    public void setDefaultAccessLevel(ChatChannelAccessLevel level) {
        if (level.isVip()) {
            throw new IllegalArgumentException("Cannot set a channel's default level above CHAT!");
        }
        
        this.defaultLevel = level;
    }
    
    @Override
    public ChatChannelAccessLevel getCachedAccessLevel(User user) {
        return cachedAccessLevels.get(user);
    }
    
    @Override
    public void setCachedAccessLevel(User user, ChatChannelAccessLevel level) {
        if (level != null) {
            cachedAccessLevels.put(user, level);
        } else {
            cachedAccessLevels.remove(user);
        }
    }
    
    @Override
    public ChatChannelAccessLevel getAccessLevel(IPermissionUser user) {
        if (user instanceof User && cachedAccessLevels.containsKey(user)) {
            return cachedAccessLevels.get(user);
        } else {
            return calculateAccessLevel(user);
        }
    }
    
    @Override
    public void sendWhoisInformation(User user, IPermissionUser target) {
        PunishmentMute m = (PunishmentManager.getInstance() == null) ? null : PunishmentManager.getInstance().getActiveMute(target, this);
        
        user.sendLocalizedMessage("module.chat.whois.header", target.getName());
        user.sendLocalizedMessage("module.chat.whois.overallLevel", getAccessLevel(target).getDisplayName(user));
        
        if (m == null) {
            user.sendLocalizedMessage("module.chat.whois.mute.none");
        } else {
            if (m.isPermanent()) {
                user.sendLocalizedMessage((m.isGlobal()) ? "module.chat.whois.mute.permGlobal" : "module.chat.whois.mute.perm", m.getAdmin().getName());
            } else {
                user.sendLocalizedMessage((m.isGlobal()) ? "module.chat.whois.mute.tempGlobal" : "module.chat.whois.mute.temp", m.getRemainingDuration().toString(user), m.getAdmin().getName());
            }
            
            user.sendLocalizedMessage("module.chat.whois.muteReason", m.getReason());
        }
    }
    
    @Override
    public ChatChannelDisplayType getDisplayType(User user) {
        ChatChannelAccessLevel level = getAccessLevel(user);
        
        if (isInChannel(user)) {
            return ChatChannelDisplayType.CONNECTED;
        } else if (!level.canJoin()) {
            return ChatChannelDisplayType.HIDDEN;
        } else if (!level.canChat()) {
            return ChatChannelDisplayType.GRAYED_OUT;
        } else {
            return ChatChannelDisplayType.NORMAL;
        }
    }
    
    @Override
    public String getListedName() {
        if (this.displayName == null || ChatColor.stripColor(this.displayName).equalsIgnoreCase(this.name)) {
            return ChatColor.stripColor(this.getDisplayName());
        } else {
            return ChatColor.stripColor(this.getDisplayName() + " (" + this.getName() + ")");
        }
    }
    
    @Override
    public void broadcastMessage(String message) {
        GoldenApple.log(Level.INFO, "[" + getName() + "] " + message);
        
        for (User user : activeUsers) {
            user.getHandle().sendMessage(message);
        }
        
        for (User user : listeningUsers) {
            user.getHandle().sendMessage("[" + getName() + "] " + message);
        }
    }
    
    @Override
    public void broadcastLocalizedMessage(String message, Object... arguments) {
        GoldenApple.log(Level.INFO, "[" + getName() + "] " + User.getConsoleUser().getLocalizedMessage(message, arguments));
        
        for (User user : activeUsers) {
            user.sendLocalizedMessage(message, arguments);
        }
        
        for (User user : listeningUsers) {
            user.getHandle().sendMessage("[" + getName() + "] " + user.getLocalizedMessage(message, arguments));
        }
    }
    
    @Override
    public void sendMessage(User user, String message) {
        if (!getCachedAccessLevel(user).canChat()) {
            user.sendLocalizedMessage("module.chat.error.notAllowed.chat");
        } else if (PunishmentManager.getInstance() != null && PunishmentManager.getInstance().isMuted(user, this)) {
            PunishmentMute m = PunishmentManager.getInstance().getActiveMute(user, this);
            
            if (m.isPermanent()) {
                user.sendLocalizedMessage("module.chat.error.muted.chat.perm");
            } else {
                user.sendLocalizedMessage("module.chat.error.muted.chat.temp", m.getRemainingDuration().toString(user));
            }
        } else {
            message = getCensor().censorMessage(message);
            broadcastMessage(user.getChatDisplayName() + ChatColor.WHITE + ": " + message);
        }
    }
    
    @Override
    public void sendMeMessage(User user, String message) {
        if (!getCachedAccessLevel(user).canChat()) {
            user.sendLocalizedMessage("module.chat.error.notAllowed.me");
        } else if (PunishmentManager.getInstance() != null && PunishmentManager.getInstance().isMuted(user, this)) {
            PunishmentMute m = PunishmentManager.getInstance().getActiveMute(user, this);
            
            if (m.isPermanent()) {
                user.sendLocalizedMessage("module.chat.error.muted.me.perm");
            } else {
                user.sendLocalizedMessage("module.chat.error.muted.me.temp", m.getRemainingDuration().toString(user));
            }
        } else {
            message = censor.censorMessage(message);
            broadcastMessage("* " + user.getDisplayName() + " " + message);
        }
    }
    
    @Override
    public boolean isInChannel(User user) {
        return activeUsers.contains(user);
    }
    
    @Override
    public List<User> getActiveUsers() {
        return Collections.unmodifiableList(activeUsers);
    }
    
    @Override
    public void delete() {
        broadcastLocalizedMessage("module.chat.delete.broadcast", displayName);
        
        for (User user : activeUsers) {
            ChatManager.getInstance().removeChannelAttachment(user);
        }
        
        ChatManager.getInstance().removeChannel(this);
    }
    
    @Override
    public boolean isFeatureAccessible(User user, ChatChannelFeature feature) {
        if (feature != ChatChannelFeature.SET_ACCESS_LEVELS) {
            return true;
        } else {
            return false;
        }
    }
}
