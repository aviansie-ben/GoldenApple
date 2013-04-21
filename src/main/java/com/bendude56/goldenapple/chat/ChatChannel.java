package com.bendude56.goldenapple.chat;

import java.util.HashMap;
import java.util.Map;

import org.bukkit.ChatColor;

import com.bendude56.goldenapple.GoldenApple;
import com.bendude56.goldenapple.User;
import com.bendude56.goldenapple.punish.PunishmentMute;

public abstract class ChatChannel {
	protected String name;
	protected String displayName;
	public String motd;
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
	public abstract void save();
	
	public void delete() {
		broadcastLocalizedMessage("general.channel.deleteBroadcast", displayName);
		for (Map.Entry<User, ChatChannelUserLevel> user : connectedUsers.entrySet()) {
			GoldenApple.getInstance().chat.userChannels.remove(user.getKey());
		}
	}
	
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
		if (broadcast) broadcastLocalizedMessage("general.channel.joinBroadcast", user.getChatDisplayName());
		
		if (level == ChatChannelUserLevel.JOIN) {
			GoldenApple.getInstance().locale.sendMessage(user, "general.channel.noTalk", false);
		}
		
		if (motd != null)
			user.getHandle().sendMessage(ChatColor.YELLOW + motd);
		
		connectedUsers.put(user, level);
		return true;
	}
	
	public void leave(User user, boolean broadcast) {
		connectedUsers.remove(user);
		
		GoldenApple.getInstance().locale.sendMessage(user, "general.channel.leave", false, displayName);
		if (broadcast) broadcastLocalizedMessage("general.channel.leaveBroadcast", user.getChatDisplayName());
	}
	
	public void kick(User user) {
		connectedUsers.remove(user);
		
		GoldenApple.getInstance().locale.sendMessage(user, "general.channel.kick", false, displayName);
		broadcastLocalizedMessage("general.channel.kickBroadcast", user.getChatDisplayName());
	}
	
	public final boolean isStrictCensorOn() {
		return censor == ChatCensor.strictChatCensor;
	}
	
	public void setStrictCensorOn(boolean value) {
		censor = (value) ? ChatCensor.strictChatCensor : ChatCensor.defaultChatCensor;
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
		
		if (user.hasPermission(ChatManager.channelAdminPermission) && level.id < ChatChannelUserLevel.ADMINISTRATOR.id)
			level = ChatChannelUserLevel.ADMINISTRATOR;
		else if (user.hasPermission(ChatManager.channelModPermission) && level.id < ChatChannelUserLevel.MODERATOR.id)
			level = ChatChannelUserLevel.MODERATOR;
		
		return level;
	}
	
	public ChatChannelDisplayLevel getDisplayLevel(User user) {
		if (connectedUsers.containsKey(user))
			return ChatChannelDisplayLevel.CONNECTED;
		else if (calculateLevel(user).id > ChatChannelUserLevel.NO_ACCESS.id)
			return ChatChannelDisplayLevel.NORMAL;
		else
			return ChatChannelDisplayLevel.GRAYED_OUT;
	}
	
	public abstract ChatChannelUserLevel getSpecificLevel(User user);
	public abstract ChatChannelUserLevel getGroupLevel(long group);
	
	public abstract void setUserLevel(long user, ChatChannelUserLevel level);
	public abstract void setGroupLevel(long group, ChatChannelUserLevel level);
	
	public final void setDefaultLevel(ChatChannelUserLevel defaultLevel) {
		this.defaultLevel = defaultLevel;
	}
	
	public final ChatChannelUserLevel getDefaultLevel() {
		return defaultLevel;
	}
	
	public void sendMessage(User user, String message) {
		if (connectedUsers.get(user).id < ChatChannelUserLevel.CHAT.id) {
			GoldenApple.getInstance().locale.sendMessage(user, "error.channel.noTalk", false);
		} else if (GoldenApple.getInstance().punish.isMuted(user, this)) {
			PunishmentMute m = (PunishmentMute)GoldenApple.getInstance().punish.getActiveMute(user, this);
			if (m.isPermanent()) {
				GoldenApple.getInstance().locale.sendMessage(user, "error.channel.muted.perma", false);
			} else {
				GoldenApple.getInstance().locale.sendMessage(user, "error.channel.muted.temp", false, m.getDuration().toString());
			}
		} else {
			message = censor.censorMessage(message);
			broadcastMessage(user.getChatDisplayName() + ChatColor.WHITE + ": " + message);
		}
	}
	
	public void sendMeMessage(User user, String message) {
		if (connectedUsers.get(user).id < ChatChannelUserLevel.CHAT.id) {
			GoldenApple.getInstance().locale.sendMessage(user, "error.channel.noTalk", false);
		} else if (GoldenApple.getInstance().punish.isMuted(user, this)) {
			PunishmentMute m = (PunishmentMute)GoldenApple.getInstance().punish.getActiveMute(user, this);
			if (m.isPermanent()) {
				GoldenApple.getInstance().locale.sendMessage(user, "error.channel.muted.perma", false);
			} else {
				GoldenApple.getInstance().locale.sendMessage(user, "error.channel.muted.temp", false, m.getDuration().toString());
			}
		} else {
			message = censor.censorMessage(message);
			broadcastMessage(user.getChatDisplayName() + ChatColor.WHITE + " " + message);
		}
	}
	
	public final void broadcastMessage(String message) {
		GoldenApple.log("[" + name + "] " + message);
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
		if (multiline) {
			for (int i = 0; GoldenApple.getInstance().locale.messages.containsKey(message + "." + i); i++) {
				GoldenApple.log("[" + name + "] " + GoldenApple.getInstance().locale.processMessageDefaultLocale(message + "." + i, arguments));
			}
		} else {
			GoldenApple.log("[" + name + "] " + GoldenApple.getInstance().locale.processMessageDefaultLocale(message, arguments));
		}
		for (Map.Entry<User, ChatChannelUserLevel> user : connectedUsers.entrySet()) {
			GoldenApple.getInstance().locale.sendMessage(user.getKey(), message, multiline, arguments);
		}
	}
	
	public final boolean isInChannel(User user) {
		return connectedUsers.containsKey(user);
	}
	
	public enum ChatChannelUserLevel {
		UNKNOWN(-1, null, null, "???"), NO_ACCESS(0, "n", "none", ChatColor.RED + "None"),
		JOIN(1, "j", "join", ChatColor.GRAY + "Join"), CHAT(2, "c", "chat", ChatColor.GRAY + "Chat"),
		VIP(3, "v", "vip", ChatColor.GREEN + "VIP"), MODERATOR(4, "m", "mod", ChatColor.GOLD + "Moderator"),
		SUPER_MODERATOR(5, "s", "supermod", ChatColor.GOLD + "Super Moderator"),
		ADMINISTRATOR(6, "a", "admin", ChatColor.GOLD + "Administrator");
		
		public int id;
		public String complexCmd;
		public String simpleCmd;
		public String display;
		
		ChatChannelUserLevel(int id, String complexCmd, String simpleCmd, String display) {
			this.id = id;
			this.complexCmd = complexCmd;
			this.simpleCmd = simpleCmd;
			this.display = display;
		}
		
		public static ChatChannelUserLevel getLevel(int id) {
			for (ChatChannelUserLevel l : ChatChannelUserLevel.values()) {
				if (l.id == id)
					return l;
			}
			return ChatChannelUserLevel.UNKNOWN;
		}
		
		public static ChatChannelUserLevel fromCmdComplex(String id) {
			for (ChatChannelUserLevel l : ChatChannelUserLevel.values()) {
				if (l.complexCmd != null && l.complexCmd.equals(id))
					return l;
			}
			return ChatChannelUserLevel.UNKNOWN;
		}
		
		public static ChatChannelUserLevel fromCmdSimple(String id) {
			for (ChatChannelUserLevel l : ChatChannelUserLevel.values()) {
				if (l.simpleCmd != null && l.simpleCmd.equals(id))
					return l;
			}
			return ChatChannelUserLevel.UNKNOWN;
		}
	}
	
	public enum ChatChannelDisplayLevel {
		HIDDEN, GRAYED_OUT, NORMAL, CONNECTED
	}
}
