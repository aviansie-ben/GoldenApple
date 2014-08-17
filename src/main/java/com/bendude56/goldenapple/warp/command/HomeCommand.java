package com.bendude56.goldenapple.warp.command;

import com.bendude56.goldenapple.GoldenApple;
import com.bendude56.goldenapple.User;
import com.bendude56.goldenapple.command.GoldenAppleCommand;
import com.bendude56.goldenapple.permissions.IPermissionUser;
import com.bendude56.goldenapple.permissions.PermissionManager;
import com.bendude56.goldenapple.warp.HomeWarp;
import com.bendude56.goldenapple.warp.WarpManager;

public class HomeCommand extends GoldenAppleCommand {
	@Override
	public boolean onExecute(GoldenApple instance, User user, String commandLabel, String[] args) {
		IPermissionUser userHome;
		int homeNumber = -1;
		String homeAlias = null;
		
		if (args.length < 2) {
			userHome = user;
		} else {
			userHome = PermissionManager.getInstance().findUser(args[1], false);
			if (userHome == null) {
				user.sendLocalizedMessage("shared.parser.userNotFound.error", args[1]);
				return true;
			}
		}
		
		if (args.length == 0) {
			homeNumber = 1;
		} else {
			try {
				homeNumber = Integer.parseInt(args[0]);
			} catch (NumberFormatException e) {
				homeAlias = args[0];
			}
		}
		
		HomeWarp h = null;
		
		if (homeAlias == null) {
			h = (HomeWarp)WarpManager.getInstance().getHome(userHome, homeNumber);
			if (h == null && user == userHome) {
				user.sendLocalizedMessage("module.warp.home.notFound.id", homeNumber);
				return true;
			} else if (h == null) {
				user.sendLocalizedMessage("module.warp.home.notFound.other.id", userHome.getName(), homeNumber);
				return true;
			}
		} else {
			h = (HomeWarp)WarpManager.getInstance().getHome(userHome, homeAlias);
			if (h == null && user == userHome) {
				user.sendLocalizedMessage("module.warp.home.notFound.alias", args[0]);
				return true;
			} else if (h == null) {
				user.sendLocalizedMessage("module.warp.home.notFound.other.alias", userHome.getName(), args[0]);
				return true;
			}
		}
		
		if (h.canTeleport(user)) {
			int deathCooldown = WarpManager.getInstance().getDeathCooldown(user), teleportCooldown = WarpManager.getInstance().getTeleportCooldown(user);
			
			if (deathCooldown > 0) {
				user.sendLocalizedMessage("module.warp.error.cooldown.death", deathCooldown);
			} else if (teleportCooldown > 0) {
				user.sendLocalizedMessage("module.warp.error.cooldown.normal", teleportCooldown);
			} else {
				h.teleport(user);
				WarpManager.getInstance().startTeleportCooldown(user);
			}
		} else {
			GoldenApple.logPermissionFail(user, commandLabel, args, true);
		}
		
		return true;
	}
}
