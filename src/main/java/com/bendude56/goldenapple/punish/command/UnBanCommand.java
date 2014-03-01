package com.bendude56.goldenapple.punish.command;

import com.bendude56.goldenapple.GoldenApple;
import com.bendude56.goldenapple.User;
import com.bendude56.goldenapple.command.GoldenAppleCommand;
import com.bendude56.goldenapple.permissions.IPermissionUser;
import com.bendude56.goldenapple.permissions.PermissionManager;

public class UnBanCommand extends GoldenAppleCommand {
	
	@Override
	public boolean onExecute(GoldenApple instance, User user, String commandLabel, String[] args) {
		if (args.length != 1) return false;
		
		user.sendLocalizedMessage("header.punish");
		
		IPermissionUser target = PermissionManager.getInstance().getUser(args[0]);
		
		if (target == null) {
			user.sendLocalizedMessage("shared.userNotFoundError", args[0]);
		} else {
			BanCommand.banVoid(target, user, "gaban", new String[] { "-u", target.getName(), "-v" }, false);
		}
		
		return true;
	}

}
