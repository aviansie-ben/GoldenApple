package com.bendude56.goldenapple.chat;

import java.util.HashMap;

import com.bendude56.goldenapple.User;

public abstract class ChatChannel {
	protected String name;
	protected ChatChannelUserLevel defaultLevel;
	protected ChatCensor censor;
	protected HashMap<User, ChatChannelUserLevel> connectedUsers;
	
	public abstract boolean isTemporary();
	
	public String getName() {
		return name;
	}
	
	public void tryJoin(User user) {
		// TODO Implement this
	}
	
	public ChatChannelUserLevel calculateLevel(User user) {
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
	
	public ChatChannelUserLevel getDefaultLevel() {
		return defaultLevel;
	}
	
	public void sendMessage(User user, String message) {
		// TODO Implement this
	}
	
	public void broadcastMessage(String message) {
		// TODO Implement this
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
