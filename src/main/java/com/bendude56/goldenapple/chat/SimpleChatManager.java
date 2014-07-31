package com.bendude56.goldenapple.chat;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.logging.Level;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;

import com.bendude56.goldenapple.GoldenApple;
import com.bendude56.goldenapple.User;
import com.bendude56.goldenapple.chat.IChatChannel.ChatChannelAccessLevel;
import com.bendude56.goldenapple.punish.PunishmentManager;
import com.bendude56.goldenapple.punish.PunishmentMute;

public class SimpleChatManager extends ChatManager {
    private HashMap<String, IChatChannel> activeChannels = new HashMap<String, IChatChannel>();
    protected HashMap<User, IChatChannel> userChannels = new HashMap<User, IChatChannel>();
    
    private IChatChannel defaultChannel;
    private List<User> tellSpy = new ArrayList<User>();
    private List<User> afkUsers = new ArrayList<User>();
    
    private HashMap<User, Long> replyTo = new HashMap<User, Long>();
    
    public SimpleChatManager() {
        activeChannels = new HashMap<String, IChatChannel>();
        userChannels = new HashMap<User, IChatChannel>();
        
        GoldenApple.getInstanceDatabaseManager().createOrUpdateTable("channels");
        GoldenApple.getInstanceDatabaseManager().createOrUpdateTable("channelusers");
        GoldenApple.getInstanceDatabaseManager().createOrUpdateTable("channelgroups");
        
        try {
            ResultSet r = GoldenApple.getInstanceDatabaseManager().executeQuery("SELECT * FROM Channels");
            try {
                while (r.next()) {
                    IChatChannel c = new DatabaseChatChannel(r);
                    activeChannels.put(c.getName(), c);
                }
            } finally {
                GoldenApple.getInstanceDatabaseManager().closeResult(r);
            }
        } catch (SQLException e) {
            GoldenApple.log(Level.SEVERE, "Failed to load channels:");
            GoldenApple.log(Level.SEVERE, e);
            throw new RuntimeException(e);
        }
        
        if (!channelExists(GoldenApple.getInstanceMainConfig().getString("modules.chat.defaultChatChannel", "default"))) {
            defaultChannel = createChannel(GoldenApple.getInstanceMainConfig().getString("modules.chat.defaultChatChannel", "default"));
        } else {
            defaultChannel = getChannel(GoldenApple.getInstanceMainConfig().getString("modules.chat.defaultChatChannel", "default"));
        }
    }
    
    @Override
    public void postInit() {
        for (Player p : Bukkit.getOnlinePlayers()) {
            User u = User.getUser(p);
            defaultChannel.join(u, false);
        }
    }
    
    @Override
    public void setActiveChannel(User user, IChatChannel channel) {
        if (userChannels.containsKey(user) && userChannels.get(user).isInChannel(user)) {
            throw new IllegalStateException("Cannot set active channel of user '" + user + "' because their old channel has not been notified!");
        } else if (channel != null && !channel.isInChannel(user)) {
            throw new IllegalStateException("Cannot set active channel of user '" + user + "' because their new channel has not been notified!");
        }
        
        if (channel == null) {
            userChannels.remove(user);
        } else {
            userChannels.put(user, channel);
        }
    }
    
    @Override
    public ChatChannelAccessLevel getActiveChannelLevel(User user) {
        return (userChannels.containsKey(user)) ? userChannels.get(user).getCachedAccessLevel(user) : null;
    }
    
    @Override
    public IChatChannel getActiveChannel(User user) {
        if (userChannels.containsKey(user)) {
            return userChannels.get(user);
        } else {
            return null;
        }
    }
    
    @Override
    public List<IChatChannel> getActiveChannels() {
        return Collections.unmodifiableList(new ArrayList<IChatChannel>(activeChannels.values()));
    }
    
    @Override
    public IChatChannel getDefaultChannel() {
        return defaultChannel;
    }
    
    @Override
    public void removeChannel(IChatChannel channel) {
        activeChannels.remove(channel.getName());
    }
    
    @Override
    public IChatChannel createChannel(String identifier) {
        try {
            GoldenApple.getInstanceDatabaseManager().execute("INSERT INTO Channels (Identifier, DisplayName, MOTD, StrictCensor, DefaultLevel) VALUES (?, ?, NULL, 0, 2)", identifier, ChatColor.WHITE + identifier);
            
            ResultSet r = GoldenApple.getInstanceDatabaseManager().executeQuery("SELECT * FROM Channels WHERE Identifier=?", identifier);
            try {
                if (r.next()) {
                    IChatChannel c = new DatabaseChatChannel(r);
                    activeChannels.put(identifier, c);
                    return c;
                } else {
                    return null;
                }
            } finally {
                GoldenApple.getInstanceDatabaseManager().closeResult(r);
            }
        } catch (SQLException e) {
            GoldenApple.log(Level.SEVERE, "Failed to create channel '" + identifier + "':");
            GoldenApple.log(Level.SEVERE, e);
            return null;
        }
    }
    
    @Override
    public boolean channelExists(String identifier) {
        return activeChannels.containsKey(identifier);
    }
    
    @Override
    public IChatChannel getChannel(String identifier) {
        if (activeChannels.containsKey(identifier)) {
            return activeChannels.get(identifier);
        } else {
            return null;
        }
    }
    
    @Override
    protected void removeChannelAttachment(User user) {
        userChannels.remove(user);
    }
    
    @Override
    public IChatCensor getDefaultCensor() {
        return SimpleChatCensor.defaultChatCensor;
    }
    
    @Override
    public IChatCensor getStrictCensor() {
        return SimpleChatCensor.strictChatCensor;
    }
    
    @Override
    public void setTellSpyStatus(User user, boolean spy) {
        if (spy) {
            tellSpy.add(user);
        } else {
            tellSpy.remove(user);
        }
    }
    
    @Override
    public boolean getTellSpyStatus(User user) {
        return tellSpy.contains(user);
    }
    
    @Override
    public void sendTellMessage(User sender, User receiver, String message) {
        if (PunishmentManager.getInstance() != null && !sender.hasPermission(ChatManager.tellAlwaysPermission)) {
            PunishmentMute mute = PunishmentManager.getInstance().getActiveMute(sender, null);
            
            if (mute != null) {
                if (mute.isPermanent()) {
                    sender.sendLocalizedMessage("error.tell.muted.perma");
                } else {
                    sender.sendLocalizedMessage("error.tell.muted.temp", mute.getRemainingDuration().toString());
                }
                
                return;
            }
        }
        
        GoldenApple.log("(" + sender.getDisplayName() + " => " + receiver.getDisplayName() + ") " + message);
        
        if (sender != User.getConsoleUser()) {
            sender.getHandle().sendMessage("(" + ChatColor.YELLOW + "You" + ChatColor.WHITE + " => " + receiver.getChatColor() + receiver.getDisplayName() + ChatColor.WHITE + ") " + message);
        }
        
        if (receiver != User.getConsoleUser()) {
            receiver.getHandle().sendMessage("(" + sender.getChatColor() + sender.getDisplayName() + ChatColor.WHITE + " => " + ChatColor.YELLOW + "You" + ChatColor.WHITE + ") " + message);
        }
        
        replyTo.put(receiver, sender.getId());
        
        for (User spy : tellSpy) {
            if (spy != sender && spy != receiver) {
                spy.getHandle().sendMessage("(" + sender.getChatColor() + sender.getDisplayName() + ChatColor.WHITE + " => " + receiver.getChatColor() + receiver.getDisplayName() + ChatColor.WHITE + ") " + message);
            }
        }
    }
    
    @Override
    public boolean sendReplyMessage(User sender, String message) {
        if (!replyTo.containsKey(sender)) {
            return false;
        } else {
            User receiver = User.getUser(replyTo.get(sender));
            
            if (receiver == null) {
                return false;
            } else {
                sendTellMessage(sender, receiver, message);
                return true;
            }
        }
    }
    
    @Override
    public void removeReplyEntry(User user) {
        replyTo.remove(user);
    }
    
    @Override
    public void setAfkStatus(User user, boolean afk, boolean broadcast) {
        if (afk && afkUsers.contains(user)) {
            return;
        }
        if (!afk && !afkUsers.contains(user)) {
            return;
        }
        
        IChatChannel channel = ChatManager.getInstance().getActiveChannel(user);
        
        if (broadcast && channel != null && channel.getCachedAccessLevel(user).canChat()) {
            if (afk) {
                channel.broadcastLocalizedMessage("general.channel.afk.on", user.getChatColor() + user.getDisplayName());
            } else {
                channel.broadcastLocalizedMessage("general.channel.afk.off", user.getChatColor() + user.getDisplayName());
            }
        }
        
        if (afk) {
            if (user.getName().length() > 10) {
                user.getPlayerHandle().setPlayerListName("[AFK] " + user.getName().substring(0, 10));
            } else {
                user.getPlayerHandle().setPlayerListName("[AFK] " + user.getName());
            }
            afkUsers.add(user);
        } else {
            user.getPlayerHandle().setPlayerListName(user.getName());
            afkUsers.remove(user);
        }
    }
    
    @Override
    public boolean getAfkStatus(User user) {
        return afkUsers.contains(user);
    }
}
