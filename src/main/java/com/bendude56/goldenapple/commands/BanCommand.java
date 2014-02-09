package com.bendude56.goldenapple.commands;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;

import com.bendude56.goldenapple.GoldenApple;
import com.bendude56.goldenapple.SimpleCommandManager;
import com.bendude56.goldenapple.User;
import com.bendude56.goldenapple.ModuleLoader.ModuleState;
import com.bendude56.goldenapple.audit.AuditLog;
import com.bendude56.goldenapple.permissions.IPermissionUser;
import com.bendude56.goldenapple.punish.Punishment.RemainingTime;
import com.bendude56.goldenapple.punish.audit.BanEvent;
import com.bendude56.goldenapple.punish.audit.BanVoidEvent;
import com.bendude56.goldenapple.punish.PunishmentManager;
import com.bendude56.goldenapple.punish.PunishmentBan;
import com.bendude56.goldenapple.util.ComplexArgumentParser;
import com.bendude56.goldenapple.util.ComplexArgumentParser.ArgumentInfo;

public class BanCommand extends DualSyntaxCommand {

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
				ArgumentInfo.newString("reason", "r", "reason", true),
				ArgumentInfo.newSwitch("void", "v", "void"),
				ArgumentInfo.newSwitch("info", "i", "info")
			});
			
			user.sendLocalizedMessage("header.punish");
			
			if (!arg.parse(user, args)) return;
			
			IPermissionUser target = arg.getUser("target");
			
			if (arg.isDefined("info")) {
				banInfo(target, user, commandLabel, args);
			} else if (arg.isDefined("void")) {
				banVoid(target, user, commandLabel, args);
			} else {
				banAdd(target, (arg.isDefined("duration")) ? arg.getString("duration") : null, (arg.isDefined("reason")) ? arg.getString("reason") : null, user, commandLabel, args);
			}
		}
	}
	
	public void banInfo(IPermissionUser target, User user, String commandLabel, String[] args) {
		if (!user.hasPermission(PunishmentManager.banInfoPermission)) {
			GoldenApple.logPermissionFail(user, commandLabel, args, true);
			return;
		}
		
		PunishmentBan b = (PunishmentBan)PunishmentManager.getInstance().getActivePunishment(target, PunishmentBan.class);
		
		if (b == null) {
			user.sendLocalizedMessage("general.ban.info.notBanned", target.getName());
		} else if (b.isPermanent()) {
			user.sendLocalizedMessage("general.ban.info.permBanned", target.getName(), b.getAdmin().getName());
			user.sendLocalizedMessage(ChatColor.GRAY + b.getReason());
		} else {
			user.sendLocalizedMessage("general.ban.info.tempBanned", target.getName(), b.getRemainingDuration().toString(), b.getAdmin().getName());
			user.sendLocalizedMessage(ChatColor.GRAY + b.getReason());
		}
	}
	
	public void banVoid(IPermissionUser target, User user, String commandLabel, String[] args) {
		if (!user.hasPermission(PunishmentManager.banVoidPermission)) {
			GoldenApple.logPermissionFail(user, commandLabel, args, true);
			return;
		}
		
		PunishmentBan b = (PunishmentBan)PunishmentManager.getInstance().getActivePunishment(target, PunishmentBan.class);
		
		if (b == null) {
			user.sendLocalizedMessage("error.ban.notBanned");
		} else {
			if (b.getAdminId() != user.getId() && !user.hasPermission(PunishmentManager.banVoidAllPermission)) {
				GoldenApple.logPermissionFail(user, commandLabel, args, true);
				return;
			} else {
				b.voidPunishment();
				b.update();
				
				AuditLog.logEvent(new BanVoidEvent(user.getName(), target.getName()));
				
				user.sendLocalizedMessage("general.ban.voidBan", target.getName());
			}
		}
	}
	
	public void banAdd(IPermissionUser target, String duration, String reason, User user, String commandLabel, String[] args) {
		if (!user.hasPermission(PunishmentManager.banTempPermission)) {
			GoldenApple.logPermissionFail(user, commandLabel, args, true);
			return;
		}
		
		PunishmentBan b = (PunishmentBan)PunishmentManager.getInstance().getActivePunishment(target, PunishmentBan.class);
		
		if (b == null) {
			try {
				User tUser;
				RemainingTime t = (duration != null) ? RemainingTime.parseTime(duration) : null;
				
				if (!user.hasPermission(PunishmentManager.banTempOverridePermission) &&
						GoldenApple.getInstanceMainConfig().getInt("modules.punish.maxTempBanTime") > 0 &&
						t != null && t.getTotalSeconds() > GoldenApple.getInstanceMainConfig().getInt("modules.punish.maxTempBanTime")) {
					user.sendLocalizedMessage("error.ban.tooLong");
				} else if (!user.hasPermission(PunishmentManager.banPermPermission) && t == null) {
					GoldenApple.logPermissionFail(user, commandLabel, args, true);
				} else {
					if (reason == null)
						reason = (t == null) ? GoldenApple.getInstanceMainConfig().getString("modules.punish.defaultPermaBanReason", "You have been banished from this server!") :
							GoldenApple.getInstanceMainConfig().getString("modules.punish.defaultTempBanReason", "You have been temporarily banished from this server!");
					
					PunishmentManager.getInstance().addBan(target, user, reason, t);
					AuditLog.logEvent(new BanEvent(user.getName(), target.getName(), (t == null) ? "PERMANENT" : t.toString(), reason));
					
					if (t == null) {
						user.sendLocalizedMessage("general.ban.permaBan", target.getName());
					} else {
						user.sendLocalizedMessage("general.ban.tempBan", target.getName(), t.toString());
					}
					
					if ((tUser = User.getUser(target.getId())) != null) {
						if (t == null) {
							tUser.getPlayerHandle().kickPlayer(GoldenApple.getInstance().getLocalizationManager().processMessageDefaultLocale("general.ban.permaKick", user.getName()) +
									"\n" + reason +
									"\n" + GoldenApple.getInstanceMainConfig().getString("banAppealMessage", "Contact an administrator to dispute this ban."));
						} else {
							tUser.getPlayerHandle().kickPlayer(GoldenApple.getInstance().getLocalizationManager().processMessageDefaultLocale("general.ban.tempKick", t.toString(), user.getName()) +
									"\n" + reason +
									"\n" + GoldenApple.getInstanceMainConfig().getString("banAppealMessage", "Contact an administrator to dispute this ban."));
						}
					}
				}
			} catch (NumberFormatException e) {
				user.sendLocalizedMessage("error.ban.invalidDuration", duration);
			}
		} else {
			user.sendLocalizedMessage("error.ban.alreadyBanned");
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
		user.sendLocalizedMessage((complex) ? "help.ban.complex" : "help.ban.simple", commandLabel);
	}

}
