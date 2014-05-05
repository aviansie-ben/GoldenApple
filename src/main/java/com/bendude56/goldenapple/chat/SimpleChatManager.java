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
import com.bendude56.goldenapple.chat.ChatChannel.ChatChannelUserLevel;

public class SimpleChatManager extends ChatManager {
	private HashMap<String, ChatChannel>	activeChannels	= new HashMap<String, ChatChannel>();
	protected HashMap<User, String>			userChannels	= new HashMap<User, String>();

	private ChatChannel						defaultChannel;
	private List<User> tellSpy = new ArrayList<User>();
	
	public SimpleChatManager() {
		activeChannels = new HashMap<String, ChatChannel>();
		userChannels = new HashMap<User, String>();
		
		GoldenApple.getInstanceDatabaseManager().createOrUpdateTable("channels");
		GoldenApple.getInstanceDatabaseManager().createOrUpdateTable("channelusers");
		GoldenApple.getInstanceDatabaseManager().createOrUpdateTable("channelgroups");
		
		try {
			ResultSet r = GoldenApple.getInstanceDatabaseManager().executeQuery("SELECT * FROM Channels");
			try {
				while (r.next()) {
					ChatChannel c = new DatabaseChatChannel(r, this);
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
            tryJoinChannel(u, defaultChannel, false);
        }
	}
	
	@Override
	public void tryJoinChannel(User user, ChatChannel channel, boolean broadcast) {
		if (userChannels.containsKey(user))
			leaveChannel(user, broadcast);
		
		if (channel.tryJoin(user, broadcast)) {
			userChannels.put(user, channel.getName());
		}
	}
	
	@Override
	public void leaveChannel(User user, boolean broadcast) {
		if (userChannels.containsKey(user)) {
			activeChannels.get(userChannels.get(user)).leave(user, broadcast);
			userChannels.remove(user);
		}
	}
	
	@Override
	public void kickFromChannel(User user) {
		if (userChannels.containsKey(user)) {
			activeChannels.get(userChannels.get(user)).kick(user);
			userChannels.remove(user);
		}
	}
	
	@Override
	public ChatChannelUserLevel getActiveChannelLevel(User user) {
		return (userChannels.containsKey(user)) ? activeChannels.get(userChannels.get(user)).connectedUsers.get(user) : ChatChannelUserLevel.UNKNOWN;
	}
	
	@Override
	public ChatChannel getActiveChannel(User user) {
		if (userChannels.containsKey(user))
			return activeChannels.get(userChannels.get(user));
		else
			return null;
	}
	
	@Override
	public List<ChatChannel> getActiveChannels() {
		return Collections.unmodifiableList(new ArrayList<ChatChannel>(activeChannels.values()));
	}

	@Override
	public ChatChannel getDefaultChannel() {
		return defaultChannel;
	}

	@Override
	public ChatChannel createTemporaryChannel(String identifier) {
		// TODO Temporary chat channel creation
		return null;
	}
	
	@Override
	public ChatChannel createChannel(String identifier) {
		try {
			GoldenApple.getInstanceDatabaseManager().execute("INSERT INTO Channels (Identifier, DisplayName, MOTD, StrictCensor, DefaultLevel) VALUES (?, ?, NULL, FALSE, 2)", identifier, ChatColor.WHITE + identifier);
			
			ResultSet r = GoldenApple.getInstanceDatabaseManager().executeQuery("SELECT * FROM Channels WHERE Identifier=?", identifier);
			try {
				if (r.next()) {
					ChatChannel c = new DatabaseChatChannel(r, this);
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
	public ChatChannel getChannel(String identifier) {
		if (activeChannels.containsKey(identifier)) {
			return activeChannels.get(identifier);
		} else {
			return null;
		}
	}

	@Override
	public void deleteChannel(String identifier) {
		if (activeChannels.containsKey(identifier)) {
			activeChannels.get(identifier).delete();
			activeChannels.remove(identifier);
		}
	}

	@Override
	protected void removeChannelAttachment(User user) {
		userChannels.remove(user);
	}

	@Override
	public ChatCensor getDefaultCensor() {
		return SimpleChatCensor.defaultChatCensor;
	}

	@Override
	public ChatCensor getStrictCensor() {
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
        GoldenApple.log("(" + sender.getDisplayName() + " => " + receiver.getDisplayName() + ") " + message);
        
        if (sender != User.getConsoleUser()) {
            sender.getHandle().sendMessage("(" + ChatColor.YELLOW + "You" + ChatColor.WHITE + " => " + receiver.getChatColor() + receiver.getDisplayName() + ChatColor.WHITE + ") " + message);
        }
        
        if (receiver != User.getConsoleUser()) {
            receiver.getHandle().sendMessage("(" + sender.getChatColor() + sender.getDisplayName() + ChatColor.WHITE + " => " + ChatColor.YELLOW + "You" + ChatColor.WHITE + ") " + message);
        }
        
        for (User spy : tellSpy) {
            if (spy != sender && spy != receiver) {
                spy.getHandle().sendMessage("(" + sender.getChatColor() + sender.getDisplayName() + ChatColor.WHITE + " => " + receiver.getChatColor() + receiver.getDisplayName() + ChatColor.WHITE + ") " + message);
            }
        }
    }
}
