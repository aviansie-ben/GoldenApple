package com.bendude56.goldenapple.commands;

import com.bendude56.goldenapple.GoldenApple;
import com.bendude56.goldenapple.User;
import com.bendude56.goldenapple.permissions.PermissionUser;
import com.bendude56.goldenapple.punish.Punishment;
import com.bendude56.goldenapple.punish.Punishment.RemainingTime;
import com.bendude56.goldenapple.punish.PunishmentBan;
import com.bendude56.goldenapple.punish.PunishmentManager;

public class BanCommand extends DualSyntaxCommand {

	@Override
	public void onCommandComplex(GoldenApple instance, User user, String commandLabel, String[] args) {
		if (args.length == 0 || args[0].equalsIgnoreCase("-?") || args[0].equalsIgnoreCase("help")) {
			sendHelp(user, commandLabel, true);
		} else {
			boolean addToReason = false;
			boolean toVoid = false;
			String reason = null;
			PermissionUser toBanish = null;
			RemainingTime duration = null;
			for (int i = 0; i < args.length; i++) {
				if (args[i].equals("-u")) {
					addToReason = false;
					
					if (i == args.length - 1) {
						instance.locale.sendMessage(user, "shared.parameterMissing", false, "-u");
						return;
					} else {
						toBanish = instance.permissions.getUser(args[++i]);
						
						if (toBanish == null) {
							instance.locale.sendMessage(user, "shared.userNotFoundError", false, args[i]);
							return;
						}
					}
				} else if (args[i].equals("-r")) {
					if (i == args.length - 1) {
						instance.locale.sendMessage(user, "shared.parameterMissing", false, "-r");
						return;
					} else {
						addToReason = true;
						reason = "";
					}
				} else if (args[i].equals("-t")) {
					addToReason = false;
					if (i == args.length - 1) {
						instance.locale.sendMessage(user, "shared.parameterMissing", false, "-t");
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
					instance.locale.sendMessage(user, "shared.unknownOption", false, args[i]);
					return;
				}
			}
			
			if (toBanish == null) {
				instance.locale.sendMessage(user, "error.ban.noUserSelected", false);
			} else if (duration == null && !user.hasPermission(PunishmentManager.banPermPermission)) {
				GoldenApple.logPermissionFail(user, commandLabel, args, true);
			} else if (duration != null && !user.hasPermission(PunishmentManager.banTempPermission)) {
				GoldenApple.logPermissionFail(user, commandLabel, args, true);
			} else if (duration != null && duration.getTotalSeconds() > instance.mainConfig.getLong("modules.punish.maxTempBanTime") &&
					!user.hasPermission(PunishmentManager.banTempOverridePermission)) {
				instance.locale.sendMessage(user, "error.ban.tooLong", false, new RemainingTime(instance.mainConfig.getLong("modules.punish.maxTempBanTime")).toString());
			} else {
				if (reason == null) reason = instance.mainConfig.getString((duration == null) ? "modules.punish.defaultPermaBanReason" : "modules.punish.defaultTempBanReason", "");
				
				if (toVoid) {
					Punishment p = instance.punish.getActivePunishment(toBanish, PunishmentBan.class);
					if (p == null) {
						instance.locale.sendMessage(user, "error.ban.notBanned", false);
					} else {
						p.voidPunishment();
						p.update();
						instance.locale.sendMessage(user, "general.ban.voidban", false, toBanish.getName());
					}
				} else {
					if (instance.punish.hasActivePunishment(toBanish, PunishmentBan.class)) {
						instance.locale.sendMessage(user, "error.ban.alreadyBanned", false);
					} else {
						instance.punish.addPunishment(new PunishmentBan(toBanish, user, reason, duration), toBanish);
						if (duration == null)
							instance.locale.sendMessage(user, "general.ban.permaban", false, toBanish.getName());
						else
							instance.locale.sendMessage(user, "general.ban.tempban", false, toBanish.getName(), duration.toString());
						
						User u = User.getUser(toBanish.getId());
						
						if (u != null && duration == null) {
							u.getPlayerHandle().kickPlayer(instance.locale.processMessageDefaultLocale("general.ban.permakick", user.getName()) +
									"\n" + reason +
									"\n" + instance.mainConfig.getString("banAppealMessage", "Contact an administrator to dispute this ban."));
						} else if (u != null) {
							u.getPlayerHandle().kickPlayer(instance.locale.processMessageDefaultLocale("general.ban.tempkick", duration.toString(), user.getName()) +
									"\n" + reason +
									"\n" + instance.mainConfig.getString("banAppealMessage", "Contact an administrator to dispute this ban."));
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
		GoldenApple.getInstance().locale.sendMessage(user, "header.help", false);
		GoldenApple.getInstance().locale.sendMessage(user, (complex) ? "help.ban.complex" : "help.ban.simple", true, commandLabel);
	}

}
