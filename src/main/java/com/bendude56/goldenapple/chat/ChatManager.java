package com.bendude56.goldenapple.chat;

import java.io.IOException;
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
import com.bendude56.goldenapple.permissions.PermissionManager.Permission;
import com.bendude56.goldenapple.permissions.PermissionManager.PermissionNode;

public class ChatManager {

	// goldenapple.chat
	public static PermissionNode			chatNode;
	public static Permission				tellPermission;

	// goldenapple.chat.channel
	public static PermissionNode			channelsNode;
	public static Permission				channelAddPermission;
	public static Permission				channelModPermission;
	public static Permission				channelAdminPermission;

	private HashMap<String, ChatChannel>	activeChannels	= new HashMap<String, ChatChannel>();
	protected HashMap<User, String>			userChannels	= new HashMap<User, String>();

	private ChatChannel						defaultChannel;
	
	public ChatManager() {
		activeChannels = new HashMap<String, ChatChannel>();
		userChannels = new HashMap<User, String>();
		
		tryCreateTable("channels");
		tryCreateTable("channelusers");
		tryCreateTable("channelgroups");
		
		try {
			ResultSet r = GoldenApple.getInstance().database.executeQuery("SELECT * FROM Channels");
			try {
				while (r.next()) {
					ChatChannel c = new DatabaseChatChannel(r);
					activeChannels.put(c.getName(), c);
				}
			} finally {
				GoldenApple.getInstance().database.closeResult(r);
			}
		} catch (SQLException e) {
			GoldenApple.log(Level.SEVERE, "Failed to load channels:");
			GoldenApple.log(Level.SEVERE, e);
			throw new RuntimeException(e);
		}
		
		if (!channelExists(GoldenApple.getInstance().mainConfig.getString("modules.chat.defaultChatChannel", "default"))) {
			defaultChannel = createChannel(GoldenApple.getInstance().mainConfig.getString("modules.chat.defaultChatChannel", "default"));
		} else {
			defaultChannel = getChannel(GoldenApple.getInstance().mainConfig.getString("modules.chat.defaultChatChannel", "default"));
		}
		
		for (Player p : Bukkit.getOnlinePlayers()) {
			User u = User.getUser(p);
			tryJoinChannel(u, defaultChannel, false);
		}
	}
	
	private void tryCreateTable(String tableName) {
		try {
			GoldenApple.getInstance().database.executeFromResource(tableName.toLowerCase() + "_create");
		} catch (SQLException | IOException e) {
			GoldenApple.log(Level.SEVERE, "Failed to create table '" + tableName + "':");
			GoldenApple.log(Level.SEVERE, e);
		}
	}
	
	public void tryJoinChannel(User user, ChatChannel channel, boolean broadcast) {
		if (userChannels.containsKey(user))
			leaveChannel(user, broadcast);
		
		if (channel.tryJoin(user, broadcast)) {
			userChannels.put(user, channel.getName());
		}
	}
	
	public void leaveChannel(User user, boolean broadcast) {
		if (userChannels.containsKey(user)) {
			activeChannels.get(userChannels.get(user)).leave(user, broadcast);
			userChannels.remove(user);
		}
	}
	
	public void kickFromChannel(User user) {
		if (userChannels.containsKey(user)) {
			activeChannels.get(userChannels.get(user)).kick(user);
			userChannels.remove(user);
		}
	}
	
	public ChatChannelUserLevel getActiveChannelLevel(User user) {
		return (userChannels.containsKey(user)) ? activeChannels.get(userChannels.get(user)).connectedUsers.get(user) : ChatChannelUserLevel.UNKNOWN;
	}
	
	public ChatChannel getActiveChannel(User user) {
		if (userChannels.containsKey(user))
			return activeChannels.get(userChannels.get(user));
		else
			return null;
	}
	
	public List<ChatChannel> getActiveChannels() {
		return Collections.unmodifiableList(new ArrayList<ChatChannel>(activeChannels.values()));
	}

	public ChatChannel getDefaultChannel() {
		return defaultChannel;
	}

	public ChatChannel createTemporaryChannel(String identifier) {
		// TODO Temporary chat channel creation
		return null;
	}
	
	public ChatChannel createChannel(String identifier) {
		try {
			GoldenApple.getInstance().database.execute("INSERT INTO Channels (Identifier, DisplayName, MOTD, StrictCensor, DefaultLevel) VALUES (?, ?, NULL, FALSE, 2)", identifier, ChatColor.WHITE + identifier);
			
			ResultSet r = GoldenApple.getInstance().database.executeQuery("SELECT * FROM Channels WHERE Identifier=?", identifier);
			try {
				if (r.next()) {
					ChatChannel c = new DatabaseChatChannel(r);
					activeChannels.put(identifier, c);
					return c;
				} else {
					return null;
				}
			} finally {
				GoldenApple.getInstance().database.closeResult(r);
			}
		} catch (SQLException e) {
			GoldenApple.log(Level.SEVERE, "Failed to create channel '" + identifier + "':");
			GoldenApple.log(Level.SEVERE, e);
			return null;
		}
	}
	
	public boolean channelExists(String identifier) {
		return activeChannels.containsKey(identifier);
	}

	public ChatChannel getChannel(String identifier) {
		if (activeChannels.containsKey(identifier)) {
			return activeChannels.get(identifier);
		} else {
			return null;
		}
	}

	public void deleteChannel(String identifier) {
		if (activeChannels.containsKey(identifier)) {
			activeChannels.get(identifier).delete();
			activeChannels.remove(identifier);
		}
	}
}
