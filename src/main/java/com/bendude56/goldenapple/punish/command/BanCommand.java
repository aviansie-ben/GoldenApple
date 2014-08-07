package com.bendude56.goldenapple.punish.command;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;

import com.bendude56.goldenapple.GoldenApple;
import com.bendude56.goldenapple.SimpleCommandManager;
import com.bendude56.goldenapple.User;
import com.bendude56.goldenapple.ModuleLoader.ModuleState;
import com.bendude56.goldenapple.audit.AuditLog;
import com.bendude56.goldenapple.command.DualSyntaxCommand;
import com.bendude56.goldenapple.command.VerifyCommand;
import com.bendude56.goldenapple.mail.MailManager;
import com.bendude56.goldenapple.permissions.IPermissionUser;
import com.bendude56.goldenapple.permissions.PermissionManager;
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
				ArgumentInfo.newSwitch("info", "i", "info"),
				ArgumentInfo.newSwitch("verify", null, "verify")
			});
			
			user.sendLocalizedMessage("header.punish");
			
			if (!arg.parse(user, args)) return;
			
			IPermissionUser target = arg.getUser("target");
			
			if (arg.isDefined("info")) {
				banInfo(target, user, commandLabel, args);
			} else if (arg.isDefined("void")) {
				banVoid(target, user, commandLabel, args, arg.isDefined("verify"));
			} else {
				banAdd(target, (arg.isDefined("duration")) ? arg.getString("duration") : null, (arg.isDefined("reason")) ? arg.getString("reason") : null, user, commandLabel, args);
			}
		}
	}
	
	public static void banInfo(IPermissionUser target, User user, String commandLabel, String[] args) {
		if (!user.hasPermission(PunishmentManager.banInfoPermission)) {
			GoldenApple.logPermissionFail(user, commandLabel, args, true);
			return;
		}
		
		PunishmentBan b = (PunishmentBan)PunishmentManager.getInstance().getActivePunishment(target, PunishmentBan.class);
		
		if (b == null) {
			user.sendLocalizedMessage("general.ban.info.notBanned", target.getName());
		} else if (b.isPermanent()) {
			user.sendLocalizedMessage("general.ban.info.permBanned", target.getName(), b.getAdmin().getName());
			user.getHandle().sendMessage(ChatColor.GRAY + b.getReason());
		} else {
			user.sendLocalizedMessage("general.ban.info.tempBanned", target.getName(), b.getRemainingDuration().toString(), b.getAdmin().getName());
			user.getHandle().sendMessage(ChatColor.GRAY + b.getReason());
		}
	}
	
	public static void banVoid(IPermissionUser target, User user, String commandLabel, String[] args, boolean verified) {
		if (!user.hasPermission(PunishmentManager.banVoidPermission)) {
			GoldenApple.logPermissionFail(user, commandLabel, args, true);
			return;
		}
		
		PunishmentBan b = (PunishmentBan)PunishmentManager.getInstance().getActivePunishment(target, PunishmentBan.class);
		
		if (b == null) {
			user.sendLocalizedMessage("error.ban.notBanned");
		} else {
			if (b.getAdminId() != user.getId() && (!verified || !user.hasPermission(PunishmentManager.banVoidAllPermission))) {
				if (!user.hasPermission(PunishmentManager.banVoidAllPermission)) {
					GoldenApple.logPermissionFail(user, commandLabel, args, true);
				} else {
					user.sendLocalizedMessage("general.ban.voidWarn", PermissionManager.getInstance().getUser(b.getAdminId()).getName());
					
					String cmd = commandLabel;
					for (String a : args) {
						cmd += " " + a;
					}
					cmd += " --verify";
					VerifyCommand.commands.put(user, cmd);
				}
			} else {
				b.voidPunishment();
				b.update();
				
				AuditLog.logEvent(new BanVoidEvent(user.getName(), target.getName()));
				
				if (MailManager.getInstance() != null) {
				    MailManager.getInstance().sendSystemMessage(target, "mail.ban.void", user.getName());
				}
				
				user.sendLocalizedMessage("general.ban.voidBan", target.getName());
			}
		}
	}
	
	public static void banAdd(IPermissionUser target, String duration, String reason, User user, String commandLabel, String[] args) {
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
					user.sendLocalizedMessage("error.ban.tooLong", new RemainingTime(GoldenApple.getInstanceMainConfig().getInt("modules.punish.maxTempBanTime")).toString());
				} else if (!user.hasPermission(PunishmentManager.banPermPermission) && t == null) {
					GoldenApple.logPermissionFail(user, commandLabel, args, true);
				} else {
					if (reason == null)
						reason = (t == null) ? GoldenApple.getInstanceMainConfig().getString("modules.punish.defaultPermaBanReason", "You have been banished from this server!") :
							GoldenApple.getInstanceMainConfig().getString("modules.punish.defaultTempBanReason", "You have been temporarily banished from this server!");
					
					PunishmentManager.getInstance().addBan(target, user, reason, t);
					AuditLog.logEvent(new BanEvent(user.getName(), target.getName(), (t == null) ? "PERMANENT" : t.toString(), reason));
					
					if (MailManager.getInstance() != null) {
					    if (t == null) {
					        MailManager.getInstance().sendSystemMessage(target, "mail.ban.perm", user.getName(), reason);
					    } else {
					        MailManager.getInstance().sendSystemMessage(target, "mail.ban.temp", user.getName(), reason, t.toString());
					    }
	                }
					
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
			user.sendLocalizedMessage("header.punish");
			
			IPermissionUser target = PermissionManager.getInstance().findUser(args[0], false);
			
			if (target == null) {
				user.sendLocalizedMessage("shared.userNotFoundError", args[0]);
				return;
			}
			
			if (args.length == 1) {
				banAdd(target, null, null, user, commandLabel, args);
			} else if (args[1].equalsIgnoreCase("info")) {
				banInfo(target, user, commandLabel, args);
			} else if (args[1].equalsIgnoreCase("void")) {
				banVoid(target, user, commandLabel, args, args.length >= 3 && args[2].equalsIgnoreCase("--verify"));
			} else {
				String reason = null;
				
				if (args.length > 2) {
					reason = args[2];
					for (int i = 3; i < args.length; i++) {
						reason += " " + args[i];
					}
				}
				
				banAdd(target, (args[1].equalsIgnoreCase("permanent") || args[1].equalsIgnoreCase("perm")) ? null : args[1], reason, user, commandLabel, args);
			}
		}
	}
	
	private void sendHelp(User user, String commandLabel, boolean complex) {
		user.sendLocalizedMessage("header.help");
		user.sendLocalizedMultilineMessage((complex) ? "help.ban.complex" : "help.ban.simple", commandLabel);
	}

}
