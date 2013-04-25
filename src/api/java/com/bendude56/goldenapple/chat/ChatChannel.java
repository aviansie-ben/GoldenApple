package com.bendude56.goldenapple.chat;

import java.util.HashMap;
import java.util.Map;

import org.bukkit.ChatColor;

import com.bendude56.goldenapple.GoldenApple;
import com.bendude56.goldenapple.User;
import com.bendude56.goldenapple.punish.PunishmentManager;
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
		this.censor = ChatManager.getInstance().getDefaultCensor();
		this.connectedUsers = new HashMap<User, ChatChannelUserLevel>();
	}
	
	public abstract boolean isTemporary();
	public abstract void save();
	
	public void delete() {
		broadcastLocalizedMessage("general.channel.deleteBroadcast", displayName);
		for (Map.Entry<User, ChatChannelUserLevel> user : connectedUsers.entrySet()) {
			ChatManager.getInstance().removeChannelAttachment(user.getKey());
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
			user.sendLocalizedMessage("error.channel.noJoin");
			return false;
		}
		
		user.sendLocalizedMessage("general.channel.join", displayName);
		if (broadcast) broadcastLocalizedMessage("general.channel.joinBroadcast", user.getChatDisplayName());
		
		if (level == ChatChannelUserLevel.JOIN) {
			user.sendLocalizedMessage("general.channel.noTalk");
		}
		
		if (motd != null)
			user.getHandle().sendMessage(ChatColor.YELLOW + motd);
		
		connectedUsers.put(user, level);
		return true;
	}
	
	public void leave(User user, boolean broadcast) {
		connectedUsers.remove(user);
		
		user.sendLocalizedMessage("general.channel.leave", displayName);
		if (broadcast) broadcastLocalizedMessage("general.channel.leaveBroadcast", user.getChatDisplayName());
	}
	
	public void kick(User user) {
		connectedUsers.remove(user);
		
		user.sendLocalizedMessage("general.channel.kick", displayName);
		broadcastLocalizedMessage("general.channel.kickBroadcast", user.getChatDisplayName());
	}
	
	public final boolean isStrictCensorOn() {
		return censor == ChatManager.getInstance().getStrictCensor();
	}
	
	public void setStrictCensorOn(boolean value) {
		censor = (value) ? ChatManager.getInstance().getStrictCensor() : ChatManager.getInstance().getDefaultCensor();
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
			user.sendLocalizedMessage("error.channel.noTalk");
		} else if (PunishmentManager.getInstance().isMuted(user, this)) {
			PunishmentMute m = PunishmentManager.getInstance().getActiveMute(user, this);
			if (m.isPermanent()) {
				user.sendLocalizedMessage("error.channel.muted.perma");
			} else {
				user.sendLocalizedMessage("error.channel.muted.temp", m.getDuration().toString());
			}
		} else {
			message = censor.censorMessage(message);
			broadcastMessage(user.getChatDisplayName() + ChatColor.WHITE + ": " + message);
		}
	}
	
	public void sendMeMessage(User user, String message) {
		if (connectedUsers.get(user).id < ChatChannelUserLevel.CHAT.id) {
			user.sendLocalizedMessage("error.channel.noTalk");
		} else if (PunishmentManager.getInstance().isMuted(user, this)) {
			PunishmentMute m = PunishmentManager.getInstance().getActiveMute(user, this);
			if (m.isPermanent()) {
				user.sendLocalizedMessage("error.channel.muted.perma");
			} else {
				user.sendLocalizedMessage("error.channel.muted.temp", m.getDuration().toString());
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
			for (int i = 0; GoldenApple.getInstance().getLocalizationManager().messageExists(message + "." + i); i++) {
				GoldenApple.log("[" + name + "] " + GoldenApple.getInstance().getLocalizationManager().processMessageDefaultLocale(message + "." + i, arguments));
			}
		} else {
			GoldenApple.log("[" + name + "] " + GoldenApple.getInstance().getLocalizationManager().processMessageDefaultLocale(message, arguments));
		}
		for (Map.Entry<User, ChatChannelUserLevel> user : connectedUsers.entrySet()) {
			GoldenApple.getInstance().getLocalizationManager().sendMessage(user.getKey(), message, multiline, arguments);
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
