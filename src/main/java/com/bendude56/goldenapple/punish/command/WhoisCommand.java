package com.bendude56.goldenapple.punish.command;

import com.bendude56.goldenapple.GoldenApple;
import com.bendude56.goldenapple.User;
import com.bendude56.goldenapple.command.GoldenAppleCommand;
import com.bendude56.goldenapple.permissions.IPermissionUser;
import com.bendude56.goldenapple.permissions.PermissionManager;
import com.bendude56.goldenapple.punish.Punishment;
import com.bendude56.goldenapple.punish.PunishmentBan;
import com.bendude56.goldenapple.punish.PunishmentManager;
import com.bendude56.goldenapple.punish.PunishmentMute;

public class WhoisCommand extends GoldenAppleCommand {

	@Override
	public boolean onExecute(GoldenApple instance, User user, String commandLabel, String[] args) {
		if (args.length != 1) return false;
		
		if (user.hasPermission(PunishmentManager.whoisPermission)) {
			user.sendLocalizedMessage("module.punish.header");
			
			IPermissionUser target = PermissionManager.getInstance().findUser(args[0], false);
			
			if (target != null) {
				boolean none = true;
				
				for (Punishment p : PunishmentManager.getInstance().getPunishments(target, Punishment.class)) {
					if (p.isExpired()) continue;
					
					none = false;
					
					if (p instanceof PunishmentBan) {
						if (p.isPermanent()) user.sendLocalizedMessage("module.punish.whois.ban.perm", target.getName());
						else user.sendLocalizedMessage("module.punish.whois.ban.temp", target.getName(), p.getRemainingDuration().toString(user));
					} else if (p instanceof PunishmentMute) {
						if (((PunishmentMute)p).isGlobal()) {
							if (p.isPermanent()) user.sendLocalizedMessage("module.punish.whois.globalMute.perm", target.getName());
							else user.sendLocalizedMessage("module.punish.whois.globalMute.temp", target.getName(), p.getRemainingDuration().toString(user));
						} else {
							if (p.isPermanent()) user.sendLocalizedMessage("module.punish.whois.mute.perm", target.getName(), ((PunishmentMute)p).getChannelIdentifier());
							else user.sendLocalizedMessage("module.punish.whois.mute.temp", target.getName(), ((PunishmentMute)p).getChannelIdentifier(), p.getRemainingDuration().toString(user));
						}
					} else {
						if (p.isPermanent()) user.sendLocalizedMessage("module.punish.whois.unknown.perm", target.getName(), p.getClass().getName());
						else user.sendLocalizedMessage("module.punish.whois.unknown.temp", target.getName(), p.getClass().getName(), p.getRemainingDuration().toString(user));
					}
				}
				
				if (none) user.sendLocalizedMessage("module.punish.whois.none", target.getName());
			} else {
				user.sendLocalizedMessage("shared.parser.userNotFound.error", args[0]);
			}
		} else {
			GoldenApple.logPermissionFail(user, commandLabel, args, true);
		}
		
		return true;
	}

}
