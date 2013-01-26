package com.bendude56.goldenapple.chat;

import java.util.HashMap;
import java.util.Map;

import org.bukkit.ChatColor;

import com.bendude56.goldenapple.GoldenApple;
import com.bendude56.goldenapple.User;

public abstract class ChatChannel {
	protected String name;
	protected String displayName;
	protected String motd;
	protected ChatChannelUserLevel defaultLevel;
	protected ChatCensor censor;
	protected HashMap<User, ChatChannelUserLevel> connectedUsers;
	
	protected ChatChannel(String name) {
		this.name = name;
		this.displayName = name;
		this.motd = null;
		this.defaultLevel = ChatChannelUserLevel.NO_ACCESS;
		this.censor = ChatCensor.defaultChatCensor;
		this.connectedUsers = new HashMap<User, ChatChannelUserLevel>();
	}
	
	public abstract boolean isTemporary();
	
	public final String getName() {
		return name;
	}
	
	public final String getDisplayName() {
		return (displayName == null) ? name : displayName;
	}
	
	public boolean tryJoin(User user, boolean broadcast) {
		ChatChannelUserLevel level = calculateLevel(user);
		if (level.id <= ChatChannelUserLevel.NO_ACCESS.id) {
			GoldenApple.getInstance().locale.sendMessage(user, "error.channel.noJoin", false);
			return false;
		}
		
		GoldenApple.getInstance().locale.sendMessage(user, "general.channel.join", false, displayName);
		if (broadcast) broadcastLocalizedMessage("general.channel.joinBroadcast", user.getDisplayName());
		
		if (level == ChatChannelUserLevel.JOIN) {
			GoldenApple.getInstance().locale.sendMessage(user, "general.channel.noTalk", false);
		}
		
		if (motd != null)
			user.getHandle().sendMessage(ChatColor.YELLOW + motd);
		
		connectedUsers.put(user, level);
		return true;
	}
	
	public void leave(User user) {
		connectedUsers.remove(user);
		
		GoldenApple.getInstance().locale.sendMessage(user, "general.channel.leave", false, displayName);
		broadcastLocalizedMessage("general.channel.leaveBroadcast", user.getDisplayName());
	}
	
	public void kick(User user) {
		connectedUsers.remove(user);
		
		GoldenApple.getInstance().locale.sendMessage(user, "general.channel.kick", false, displayName);
		broadcastLocalizedMessage("general.channel.kickBroadcast", user.getDisplayName());
	}
	
	public final ChatChannelUserLevel getActiveLevel(User user) {
		return connectedUsers.get(user);
	}
	
	public final ChatChannelUserLevel calculateLevel(User user) {
		ChatChannelUserLevel level = getSpecificLevel(user);
		
		if (level == ChatChannelUserLevel.UNKNOWN) {
			for (long group : user.getParentGroups(false)) {
				ChatChannelUserLevel groupLevel = getGroupLevel(group);
				level = (groupLevel.id > level.id) ? groupLevel : level; 
			}
			level = (getDefaultLevel().id > level.id) ? getDefaultLevel() : level;
		}
		
		return level;
	}
	
	public abstract ChatChannelUserLevel getSpecificLevel(User user);
	public abstract ChatChannelUserLevel getGroupLevel(long group);
	
	public abstract void setUserLevel(long user, ChatChannelUserLevel level);
	public abstract void setGroupLevel(long group, ChatChannelUserLevel level);
	
	public abstract void save();
	
	public final void setDefaultLevel(ChatChannelUserLevel defaultLevel) {
		this.defaultLevel = defaultLevel;
	}
	
	public final ChatChannelUserLevel getDefaultLevel() {
		return defaultLevel;
	}
	
	public void sendMessage(User user, String message) {
		if (connectedUsers.get(user).id < ChatChannelUserLevel.CHAT.id) {
			GoldenApple.getInstance().locale.sendMessage(user, "error.channel.noTalk", false);
		} else {
			message = censor.censorMessage(message);
			broadcastMessage(user.getDisplayName() + ChatColor.WHITE + ": " + message);
		}
	}
	
	public final void broadcastMessage(String message) {
		for (Map.Entry<User, ChatChannelUserLevel> user : connectedUsers.entrySet()) {
			user.getKey().getHandle().sendMessage(message);
		}
	}
	
	public final void broadcastLocalizedMessage(String message) {
		broadcastLocalizedMessage(message, false, new String[0]);
	}
	
	public final void broadcastLocalizedMessage(String message, boolean multiline) {
		broadcastLocalizedMessage(message, multiline, new String[0]);
	}
	
	public final void broadcastLocalizedMessage(String message, String... arguments) {
		broadcastLocalizedMessage(message, false, arguments);
	}
	
	public final void broadcastLocalizedMessage(String message, boolean multiline, String... arguments) {
		for (Map.Entry<User, ChatChannelUserLevel> user : connectedUsers.entrySet()) {
			GoldenApple.getInstance().locale.sendMessage(user.getKey(), message, multiline);
		}
	}
	
	
	public enum ChatChannelUserLevel {
		UNKNOWN(-1), NO_ACCESS(0), JOIN(1), CHAT(2), VIP(3), MODERATOR(4), SUPER_MODERATOR(5), ADMINISTRATOR(6);
		
		public int id;
		
		ChatChannelUserLevel(int id) {
			this.id = id;
		}
		
		public static ChatChannelUserLevel getLevel(int id) {
			for (ChatChannelUserLevel l : ChatChannelUserLevel.values()) {
				if (l.id == id)
					return l;
			}
			return ChatChannelUserLevel.UNKNOWN;
		}
	}
}
