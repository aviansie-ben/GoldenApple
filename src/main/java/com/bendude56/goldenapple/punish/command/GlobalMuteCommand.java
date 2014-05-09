package com.bendude56.goldenapple.punish.command;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;

import com.bendude56.goldenapple.SimpleCommandManager;
import com.bendude56.goldenapple.GoldenApple;
import com.bendude56.goldenapple.ModuleLoader.ModuleState;
import com.bendude56.goldenapple.User;
import com.bendude56.goldenapple.audit.AuditLog;
import com.bendude56.goldenapple.command.DualSyntaxCommand;
import com.bendude56.goldenapple.permissions.IPermissionUser;
import com.bendude56.goldenapple.permissions.PermissionManager;
import com.bendude56.goldenapple.punish.PunishmentManager;
import com.bendude56.goldenapple.punish.PunishmentMute;
import com.bendude56.goldenapple.punish.Punishment.RemainingTime;
import com.bendude56.goldenapple.punish.audit.MuteEvent;
import com.bendude56.goldenapple.punish.audit.MuteVoidEvent;
import com.bendude56.goldenapple.util.ComplexArgumentParser;
import com.bendude56.goldenapple.util.ComplexArgumentParser.ArgumentInfo;

public class GlobalMuteCommand extends DualSyntaxCommand {

	@Override
	public void onExecuteComplex(GoldenApple instance, User user, String commandLabel, String[] args) {
		if (GoldenApple.getInstance().getModuleManager().getModule("Chat").getCurrentState() != ModuleState.LOADED) {
			SimpleCommandManager.defaultCommand.onCommand(user.getHandle(), Bukkit.getPluginCommand("gaglobalmute"), commandLabel, args);
		} else if (args.length == 0 || args[0].equalsIgnoreCase("-?") || args[0].equalsIgnoreCase("help")) {
			sendHelp(user, commandLabel, true);
		} else {
			ComplexArgumentParser arg = new ComplexArgumentParser(new ArgumentInfo[] {
				ArgumentInfo.newUser("target", "u", "user", true, false),
				ArgumentInfo.newString("duration", "t", "time", false),
				ArgumentInfo.newString("reason", "r", "reason", true),
				ArgumentInfo.newSwitch("void", "v", "void"),
				ArgumentInfo.newSwitch("info", "i", "info")
			});
			
			user.sendLocalizedMessage("header.punish");
			
			if (!arg.parse(user, args)) return;
			
			if (!arg.isDefined("target")) {
				user.sendLocalizedMessage("error.globalmute.noUserSelected");
				return;
			}
			
			IPermissionUser target = arg.getUser("target");
			
			if (arg.isDefined("info")) {
				muteInfo(target, user, commandLabel, args);
			} else if (arg.isDefined("void")) {
				muteVoid(target, user, commandLabel, args);
			} else {
				muteAdd(target, (arg.isDefined("duration")) ? arg.getString("duration") : null, (arg.isDefined("reason")) ? arg.getString("reason") : null, user, commandLabel, args);
			}
		}
	}
	
	public static void muteInfo(IPermissionUser target, User user, String commandLabel, String[] args) {
		if (!user.hasPermission(PunishmentManager.globalMuteInfoPermission)) {
			GoldenApple.logPermissionFail(user, commandLabel, args, true);
			return;
		}
		
		PunishmentMute m = PunishmentManager.getInstance().getActiveMute(target, null);
		
		if (m == null) {
			user.sendLocalizedMessage("general.globalmute.info.notMuted", target.getName());
		} else if (m.isPermanent()) {
			user.sendLocalizedMessage("general.globalmute.info.permMuted", target.getName(), m.getAdmin().getName());
			user.getHandle().sendMessage(ChatColor.GRAY + m.getReason());
		} else {
			user.sendLocalizedMessage("general.globalmute.info.tempMuted", target.getName(), m.getRemainingDuration().toString(), m.getAdmin().getName());
			user.getHandle().sendMessage(ChatColor.GRAY + m.getReason());
		}
	}
	
	public static void muteVoid(IPermissionUser target, User user, String commandLabel, String[] args) {
		if (!user.hasPermission(PunishmentManager.globalMuteVoidPermission)) {
			GoldenApple.logPermissionFail(user, commandLabel, args, true);
			return;
		}
		
		PunishmentMute m = PunishmentManager.getInstance().getActiveMute(target, null);
		
		if (m == null) {
			user.sendLocalizedMessage("error.globalmute.notMuted");
		} else {
			if (m.getAdminId() != user.getId() && !user.hasPermission(PunishmentManager.globalMuteVoidAllPermission)) {
				GoldenApple.logPermissionFail(user, commandLabel, args, true);
				return;
			} else {
				m.voidPunishment();
				m.update();
				
				AuditLog.logEvent(new MuteVoidEvent(user.getName(), target.getName(), "GLOBAL"));
				
				user.sendLocalizedMessage("general.globalmute.voidMute", target.getName());
			}
		}
	}
	
	public static void muteAdd(IPermissionUser target, String duration, String reason, User user, String commandLabel, String[] args) {
		if (!user.hasPermission(PunishmentManager.globalMuteTempPermission)) {
			GoldenApple.logPermissionFail(user, commandLabel, args, true);
			return;
		}
		
		PunishmentMute m = PunishmentManager.getInstance().getActiveMute(target, null);
		
		if (m == null) {
			try {
				User tUser;
				RemainingTime t = (duration != null) ? RemainingTime.parseTime(duration) : null;
				
				if (!user.hasPermission(PunishmentManager.globalMuteTempOverridePermission) &&
						GoldenApple.getInstanceMainConfig().getInt("modules.punish.maxTempChannelMuteTime") > 0 &&
						t != null && t.getTotalSeconds() > GoldenApple.getInstanceMainConfig().getInt("modules.punish.maxTempGlobalMuteTime")) {
					user.sendLocalizedMessage("error.globalmute.tooLong", new RemainingTime(GoldenApple.getInstanceMainConfig().getInt("modules.punish.maxTempGlobalMuteTime")).toString());
				} else {
					if (reason == null)
						reason = (t == null) ? GoldenApple.getInstanceMainConfig().getString("modules.punish.defaultPermaGlobalMuteReason", "You have been silenced!") :
							GoldenApple.getInstanceMainConfig().getString("modules.punish.defaultTempGlobalMuteReason", "An administrator has temporarily silenced you!");
					
					PunishmentManager.getInstance().addMute(target, user, reason, t, null);
					AuditLog.logEvent(new MuteEvent(user.getName(), target.getName(), (t == null) ? "PERMANENT" : t.toString(), reason, "GLOBAL"));
					
					if (t == null) {
						user.sendLocalizedMessage("general.globalmute.permaMute", target.getName());
					} else {
						user.sendLocalizedMessage("general.globalmute.tempMute", target.getName(), t.toString());
					}
					
					if ((tUser = User.getUser(target.getId())) != null) {
						if (t == null) {
							tUser.sendLocalizedMessage("general.globalmute.permaKick", user.getName());
							tUser.getHandle().sendMessage(reason);
						} else {
							tUser.sendLocalizedMessage("general.globalmute.tempKick", target.getName(), t.toString());
							tUser.getHandle().sendMessage(reason);
						}
					}
				}
			} catch (NumberFormatException e) {
				user.sendLocalizedMessage("error.globalmute.invalidDuration", duration);
			}
		} else {
			user.sendLocalizedMessage("error.globalmute.alreadyMuted");
		}
	}

	@Override
	public void onExecuteSimple(GoldenApple instance, User user, String commandLabel, String[] args) {
		if (GoldenApple.getInstance().getModuleManager().getModule("Chat").getCurrentState() != ModuleState.LOADED) {
			SimpleCommandManager.defaultCommand.onCommand(user.getHandle(), Bukkit.getPluginCommand("gamute"), commandLabel, args);
		} else if (args.length == 0 || args[0].equalsIgnoreCase("-?") || args[0].equalsIgnoreCase("help")) {
			sendHelp(user, commandLabel, false);
		} else {
			user.sendLocalizedMessage("header.punish");
			
			IPermissionUser target = PermissionManager.getInstance().findUser(args[0], false);
			
			if (target == null) {
				user.sendLocalizedMessage("shared.userNotFoundError", args[0]);
				return;
			}
			
			if (args.length == 1) {
				muteAdd(target, null, null, user, commandLabel, args);
			} else if (args[1].equalsIgnoreCase("info")) {
				muteInfo(target, user, commandLabel, args);
			} else if (args[1].equalsIgnoreCase("void")) {
				muteVoid(target, user, commandLabel, args);
			} else {
				String reason = null;
				
				if (args.length > 2) {
					reason = args[2];
					for (int i = 3; i < args.length; i++) {
						reason += " " + args[i];
					}
				}
				
				muteAdd(target, (args[1].equalsIgnoreCase("permanent")) ? null : args[1], reason, user, commandLabel, args);
			}
		}
	}
	
	private void sendHelp(User user, String commandLabel, boolean complex) {
		user.sendLocalizedMessage("header.help");
		user.sendLocalizedMultilineMessage((complex) ? "help.globalmute.complex" : "help.globalmute.simple", commandLabel);
	}

}
