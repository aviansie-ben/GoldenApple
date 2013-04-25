package com.bendude56.goldenapple.commands;

import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import com.bendude56.goldenapple.GoldenApple;
import com.bendude56.goldenapple.User;
import com.bendude56.goldenapple.permissions.IPermissionUser;
import com.bendude56.goldenapple.permissions.PermissionManager;
import com.bendude56.goldenapple.warp.HomeWarp;
import com.bendude56.goldenapple.warp.WarpManager;

public class HomeCommand implements CommandExecutor {
	@Override
	public boolean onCommand(CommandSender sender, Command command, String commandLabel, String[] args) {
		User user = User.getUser(sender);
		
		IPermissionUser userHome;
		int homeNumber = -1;
		String homeAlias = null;
		
		if (args.length < 2) {
			userHome = user;
		} else {
			userHome = PermissionManager.getInstance().getUser(args[1]);
			if (userHome == null) {
				user.sendLocalizedMessage("shared.userNotFoundError", args[1]);
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
				user.sendLocalizedMessage("error.home.notFoundIdSelf", homeNumber + "");
				return true;
			} else if (h == null) {
				user.sendLocalizedMessage("error.home.notFoundIdOther", userHome.getName(), homeNumber + "");
				return true;
			}
		} else {
			h = (HomeWarp)WarpManager.getInstance().getHome(userHome, homeAlias);
			if (h == null && user == userHome) {
				user.sendLocalizedMessage("error.home.notFoundAliasSelf", args[0]);
				return true;
			} else if (h == null) {
				user.sendLocalizedMessage("error.home.notFoundAliasOther", userHome.getName(), args[0]);
				return true;
			}
		}
		
		if (h.canTeleport(user)) {
			h.teleport(user);
		} else {
			GoldenApple.logPermissionFail(user, commandLabel, args, true);
		}
		
		return true;
	}
}
