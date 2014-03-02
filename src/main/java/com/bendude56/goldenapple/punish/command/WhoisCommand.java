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
			user.sendLocalizedMessage("header.punish");
			
			IPermissionUser target = PermissionManager.getInstance().getUser(args[0]);
			
			if (target != null) {
				boolean none = true;
				
				for (Punishment p : PunishmentManager.getInstance().getPunishments(target, Punishment.class)) {
					if (p.isExpired()) continue;
					
					none = false;
					
					if (p instanceof PunishmentBan) {
						if (p.isPermanent()) user.sendLocalizedMessage("general.whois.ban.permanent", target.getName());
						else user.sendLocalizedMessage("general.whois.ban.temporary", target.getName(), p.getRemainingDuration().toString());
					} else if (p instanceof PunishmentMute) {
						if (((PunishmentMute)p).isGlobal()) {
							if (p.isPermanent()) user.sendLocalizedMessage("general.whois.globalmute.permanent", target.getName());
							else user.sendLocalizedMessage("general.whois.globalmute.temporary", target.getName(), p.getRemainingDuration().toString());
						} else {
							if (p.isPermanent()) user.sendLocalizedMessage("general.whois.mute.permanent", target.getName(), ((PunishmentMute)p).getChannelIdentifier());
							else user.sendLocalizedMessage("general.whois.mute.temporary", target.getName(), ((PunishmentMute)p).getChannelIdentifier(), p.getRemainingDuration().toString());
						}
					} else {
						if (p.isPermanent()) user.sendLocalizedMessage("general.whois.unknown.permanent", target.getName(), p.getClass().getName());
						else user.sendLocalizedMessage("general.whois.unknown.temporary", target.getName(), p.getClass().getName(), p.getRemainingDuration().toString());
					}
				}
				
				if (none) user.sendLocalizedMessage("general.whois.none", target.getName());
			} else {
				user.sendLocalizedMessage("shared.userNotFoundError", args[0]);
			}
		} else {
			GoldenApple.logPermissionFail(user, commandLabel, args, true);
		}
		
		return true;
	}

}
