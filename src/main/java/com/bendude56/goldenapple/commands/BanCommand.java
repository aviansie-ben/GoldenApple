package com.bendude56.goldenapple.commands;

import com.bendude56.goldenapple.GoldenApple;
import com.bendude56.goldenapple.User;
import com.bendude56.goldenapple.permissions.IPermissionUser;
import com.bendude56.goldenapple.permissions.PermissionManager;
import com.bendude56.goldenapple.punish.Punishment;
import com.bendude56.goldenapple.punish.Punishment.RemainingTime;
import com.bendude56.goldenapple.punish.PunishmentManager;
import com.bendude56.goldenapple.punish.PunishmentBan;

public class BanCommand extends DualSyntaxCommand {

	@Override
	public void onCommandComplex(GoldenApple instance, User user, String commandLabel, String[] args) {
		if (args.length == 0 || args[0].equalsIgnoreCase("-?") || args[0].equalsIgnoreCase("help")) {
			sendHelp(user, commandLabel, true);
		} else {
			boolean addToReason = false;
			boolean toVoid = false;
			String reason = null;
			IPermissionUser toBanish = null;
			RemainingTime duration = null;
			for (int i = 0; i < args.length; i++) {
				if (args[i].equals("-u")) {
					addToReason = false;
					
					if (i == args.length - 1) {
						user.sendLocalizedMessage("shared.parameterMissing", "-u");
						return;
					} else {
						toBanish = PermissionManager.getInstance().getUser(args[++i]);
						
						if (toBanish == null) {
							user.sendLocalizedMessage("shared.userNotFoundError", args[i]);
							return;
						}
					}
				} else if (args[i].equals("-r")) {
					if (i == args.length - 1) {
						user.sendLocalizedMessage("shared.parameterMissing", "-r");
						return;
					} else {
						addToReason = true;
						reason = "";
					}
				} else if (args[i].equals("-t")) {
					addToReason = false;
					if (i == args.length - 1) {
						user.sendLocalizedMessage("shared.parameterMissing", "-t");
						return;
					} else {
						duration = RemainingTime.parseTime(args[++i]);
					}
				} else if (args[i].equals("-v")) {
					addToReason = false;
					toVoid = true;
				} else if (addToReason) {
					reason += args[i] + " ";
				} else {
					user.sendLocalizedMessage("shared.unknownOption", args[i]);
					return;
				}
			}
			
			if (toBanish == null) {
				user.sendLocalizedMessage("error.ban.noUserSelected");
			} else if (duration == null && !user.hasPermission(PunishmentManager.banPermPermission)) {
				GoldenApple.logPermissionFail(user, commandLabel, args, true);
			} else if (duration != null && !user.hasPermission(PunishmentManager.banTempPermission)) {
				GoldenApple.logPermissionFail(user, commandLabel, args, true);
			} else if (duration != null && duration.getTotalSeconds() > GoldenApple.getInstanceMainConfig().getLong("modules.punish.maxTempBanTime") &&
					!user.hasPermission(PunishmentManager.banTempOverridePermission)) {
				user.sendLocalizedMessage("error.ban.tooLong", new RemainingTime(GoldenApple.getInstanceMainConfig().getLong("modules.punish.maxTempBanTime")).toString());
			} else {
				if (reason == null) reason = GoldenApple.getInstanceMainConfig().getString((duration == null) ? "modules.punish.defaultPermaBanReason" : "modules.punish.defaultTempBanReason", "");
				
				if (toVoid) {
					Punishment p = PunishmentManager.getInstance().getActivePunishment(toBanish, PunishmentBan.class);
					if (p == null) {
						user.sendLocalizedMessage("error.ban.notBanned");
					} else {
						p.voidPunishment();
						p.update();
						user.sendLocalizedMessage("general.ban.voidban", toBanish.getName());
					}
				} else {
					if (PunishmentManager.getInstance().hasActivePunishment(toBanish, PunishmentBan.class)) {
						user.sendLocalizedMessage("error.ban.alreadyBanned");
					} else {
						PunishmentManager.getInstance().addBan(toBanish, user, reason, duration);
						if (duration == null)
							user.sendLocalizedMessage("general.ban.permaban", toBanish.getName());
						else
							user.sendLocalizedMessage("general.ban.tempban", toBanish.getName(), duration.toString());
						
						User u = User.getUser(toBanish.getId());
						
						if (u != null && duration == null) {
							u.getPlayerHandle().kickPlayer(GoldenApple.getInstance().getLocalizationManager().processMessageDefaultLocale("general.ban.permakick", user.getName()) +
									"\n" + reason +
									"\n" + GoldenApple.getInstanceMainConfig().getString("banAppealMessage", "Contact an administrator to dispute this ban."));
						} else if (u != null) {
							u.getPlayerHandle().kickPlayer(GoldenApple.getInstance().getLocalizationManager().processMessageDefaultLocale("general.ban.tempkick", duration.toString(), user.getName()) +
									"\n" + reason +
									"\n" + GoldenApple.getInstanceMainConfig().getString("banAppealMessage", "Contact an administrator to dispute this ban."));
						}
					}
				}
			}
		}
	}

	@Override
	public void onCommandSimple(GoldenApple instance, User user, String commandLabel, String[] args) {
		if (args.length == 0 || args[0].equalsIgnoreCase("-?") || args[0].equalsIgnoreCase("help")) {
			sendHelp(user, commandLabel, false);
		} else {
			
		}
	}
	
	private void sendHelp(User user, String commandLabel, boolean complex) {
		user.sendLocalizedMessage("header.help");
		user.sendLocalizedMessage((complex) ? "help.ban.complex" : "help.ban.simple", commandLabel);
	}

}
