package com.bendude56.goldenapple.commands;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;

import com.bendude56.goldenapple.SimpleCommandManager;
import com.bendude56.goldenapple.GoldenApple;
import com.bendude56.goldenapple.ModuleLoader.ModuleState;
import com.bendude56.goldenapple.User;
import com.bendude56.goldenapple.audit.AuditLog;
import com.bendude56.goldenapple.chat.ChatChannel;
import com.bendude56.goldenapple.chat.ChatChannel.ChatChannelUserLevel;
import com.bendude56.goldenapple.chat.ChatManager;
import com.bendude56.goldenapple.permissions.IPermissionUser;
import com.bendude56.goldenapple.punish.PunishmentManager;
import com.bendude56.goldenapple.punish.PunishmentMute;
import com.bendude56.goldenapple.punish.Punishment.RemainingTime;
import com.bendude56.goldenapple.punish.audit.MuteEvent;
import com.bendude56.goldenapple.punish.audit.MuteVoidEvent;
import com.bendude56.goldenapple.util.ComplexArgumentParser;
import com.bendude56.goldenapple.util.ComplexArgumentParser.ArgumentInfo;

public class MuteCommand extends DualSyntaxCommand {

	@Override
	public void onExecuteComplex(GoldenApple instance, User user, String commandLabel, String[] args) {
		if (GoldenApple.getInstance().getModuleManager().getModule("Chat").getCurrentState() != ModuleState.LOADED) {
			SimpleCommandManager.defaultCommand.onCommand(user.getHandle(), Bukkit.getPluginCommand("gamute"), commandLabel, args);
		} else if (args.length == 0 || args[0].equalsIgnoreCase("-?") || args[0].equalsIgnoreCase("help")) {
			sendHelp(user, commandLabel, true);
		} else {
			ComplexArgumentParser arg = new ComplexArgumentParser(new ArgumentInfo[] {
				ArgumentInfo.newUser("target", "u", "user", true, false),
				ArgumentInfo.newString("duration", "t", "time", false),
				ArgumentInfo.newString("channel", "c", "channel", false),
				ArgumentInfo.newString("reason", "r", "reason", true),
				ArgumentInfo.newSwitch("void", "v", "void"),
				ArgumentInfo.newSwitch("info", "i", "info")
			});
			
			user.sendLocalizedMessage("header.punish");
			
			if (!arg.parse(user, args)) return;
			
			if (!arg.isDefined("target")) {
				user.sendLocalizedMessage("error.mute.noUserSelected");
				return;
			} else if (!arg.isDefined("channel") && ChatManager.getInstance().getActiveChannel(user) == null) {
				user.sendLocalizedMessage("error.mute.noChannelSelected");
				return;
			}
			
			IPermissionUser target = arg.getUser("target");
			ChatChannel c;
			
			if (arg.isDefined("channel")) {
				 c = ChatManager.getInstance().getChannel(arg.getString("channel"));
				 
				 if (c == null) {
					 user.sendLocalizedMessage("error.channel.notFound", arg.getString("channel"));
					 return;
				 }
			} else {
				c = ChatManager.getInstance().getActiveChannel(user);
			}
			
			if (c.isTemporary()) {
				user.sendLocalizedMessage("error.mute.tempChannel");
				return;
			}
			
			if (arg.isDefined("info")) {
				muteInfo(target, c, user, commandLabel, args);
			} else if (arg.isDefined("void")) {
				muteVoid(target, c, user, commandLabel, args);
			} else {
				muteAdd(target, c, (arg.isDefined("duration")) ? arg.getString("duration") : null, (arg.isDefined("reason")) ? arg.getString("reason") : null, user, commandLabel, args);
			}
		}
	}
	
	public void muteInfo(IPermissionUser target, ChatChannel c, User user, String commandLabel, String[] args) {
		if (c.calculateLevel(user).id < ChatChannelUserLevel.MODERATOR.id) {
			GoldenApple.logPermissionFail(user, commandLabel, args, true);
			return;
		}
		
		PunishmentMute m = PunishmentManager.getInstance().getActiveMute(target, c);
		
		if (m == null) {
			user.sendLocalizedMessage("general.mute.info.notMuted", target.getName());
		} else if (m.isPermanent()) {
			user.sendLocalizedMessage("general.mute.info.permMuted", target.getName(), m.getAdmin().getName());
			user.getHandle().sendMessage(ChatColor.GRAY + m.getReason());
			if (m.isGlobal())
				user.sendLocalizedMessage("general.mute.info.global");
		} else {
			user.sendLocalizedMessage("general.mute.info.tempMuted", target.getName(), m.getRemainingDuration().toString(), m.getAdmin().getName());
			user.getHandle().sendMessage(ChatColor.GRAY + m.getReason());
			if (m.isGlobal())
				user.sendLocalizedMessage("general.mute.info.global");
		}
	}
	
	public void muteVoid(IPermissionUser target, ChatChannel c, User user, String commandLabel, String[] args) {
		if (c.calculateLevel(user).id < ChatChannelUserLevel.MODERATOR.id) {
			GoldenApple.logPermissionFail(user, commandLabel, args, true);
			return;
		}
		
		PunishmentMute m = PunishmentManager.getInstance().getActiveMute(target, c);
		
		if (m == null) {
			user.sendLocalizedMessage("error.mute.notMuted");
		} else if (m.isGlobal()) {
			user.sendLocalizedMessage("error.mute.voidGlobal");
		} else {
			if (m.getAdminId() != user.getId() && c.calculateLevel(user).id < ChatChannelUserLevel.SUPER_MODERATOR.id) {
				GoldenApple.logPermissionFail(user, commandLabel, args, true);
				return;
			} else {
				m.voidPunishment();
				m.update();
				
				AuditLog.logEvent(new MuteVoidEvent(user.getName(), target.getName(), c.getName()));
				
				user.sendLocalizedMessage("general.mute.voidMute", target.getName());
			}
		}
	}
	
	public void muteAdd(IPermissionUser target, ChatChannel c, String duration, String reason, User user, String commandLabel, String[] args) {
		if (c.calculateLevel(user).id < ChatChannelUserLevel.MODERATOR.id) {
			GoldenApple.logPermissionFail(user, commandLabel, args, true);
			return;
		}
		
		PunishmentMute m = PunishmentManager.getInstance().getActiveMute(target, c);
		
		if (m == null) {
			try {
				User tUser;
				RemainingTime t = (duration != null) ? RemainingTime.parseTime(duration) : null;
				
				if (c.calculateLevel(user).id < ChatChannelUserLevel.SUPER_MODERATOR.id &&
						GoldenApple.getInstanceMainConfig().getInt("modules.punish.maxTempChannelMuteTime") > 0 &&
						t != null && t.getTotalSeconds() > GoldenApple.getInstanceMainConfig().getInt("modules.punish.maxTempChannelMuteTime")) {
					user.sendLocalizedMessage("error.mute.tooLong");
				} else {
					if (reason == null)
						reason = (t == null) ? GoldenApple.getInstanceMainConfig().getString("modules.punish.defaultPermaChannelMuteReason", "You have been silenced from this channel.") :
							GoldenApple.getInstanceMainConfig().getString("modules.punish.defaultTempChannelMuteReason", "You have been temporarily silenced from this channel.");
					
					PunishmentManager.getInstance().addMute(target, user, reason, t, c.getName());
					AuditLog.logEvent(new MuteEvent(user.getName(), target.getName(), (t == null) ? "PERMANENT" : t.toString(), reason, c.getName()));
					
					if (t == null) {
						user.sendLocalizedMessage("general.mute.permaMute", target.getName());
					} else {
						user.sendLocalizedMessage("general.mute.tempMute", target.getName(), t.toString());
					}
					
					if ((tUser = User.getUser(target.getId())) != null && ChatManager.getInstance().getActiveChannel(tUser) == c) {
						if (t == null) {
							tUser.sendLocalizedMessage("general.mute.permaKick", user.getName());
							tUser.getHandle().sendMessage(reason);
						} else {
							tUser.sendLocalizedMessage("general.mute.tempKick", target.getName(), t.toString());
							tUser.getHandle().sendMessage(reason);
						}
					}
				}
			} catch (NumberFormatException e) {
				user.sendLocalizedMessage("error.mute.invalidDuration", duration);
			}
		} else {
			user.sendLocalizedMessage("error.mute.alreadyMuted");
		}
	}

	@Override
	public void onExecuteSimple(GoldenApple instance, User user, String commandLabel, String[] args) {
		if (args.length == 0 || args[0].equalsIgnoreCase("-?") || args[0].equalsIgnoreCase("help")) {
			sendHelp(user, commandLabel, false);
		} else {
			// TODO Implement this
		}
	}
	
	private void sendHelp(User user, String commandLabel, boolean complex) {
		user.sendLocalizedMessage("header.help");
		user.sendLocalizedMultilineMessage((complex) ? "help.mute.complex" : "help.mute.simple", commandLabel);
	}

}
