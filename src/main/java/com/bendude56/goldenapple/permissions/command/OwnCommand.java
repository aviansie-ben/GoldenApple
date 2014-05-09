package com.bendude56.goldenapple.permissions.command;

import com.bendude56.goldenapple.GoldenApple;
import com.bendude56.goldenapple.User;
import com.bendude56.goldenapple.command.GoldenAppleCommand;
import com.bendude56.goldenapple.command.VerifyCommand;
import com.bendude56.goldenapple.permissions.PermissionManager;

public class OwnCommand extends GoldenAppleCommand {
	@Override
	public boolean onExecute(GoldenApple instance, User user, String commandLabel, String[] args) {
		if (!user.getHandle().isOp()) {
			GoldenApple.logPermissionFail(user, commandLabel, args, true);
			return true;
		}
		
		if (GoldenApple.getInstanceMainConfig().getBoolean("securityPolicy.disableOwn")) {
			user.sendLocalizedMessage("error.own.disabled");
			GoldenApple.logPermissionFail(user, commandLabel, args, false);
			return true;
		}
		
		if (args.length == 0 || !args[0].equals("-v")) {
			user.sendLocalizedMessage("general.own.warnBefore");
			VerifyCommand.commands.put(user, "gaown -v");
		} else {
			user.addPermission(PermissionManager.getInstance().getRootNode().getStarPermission());
			user.sendLocalizedMessage("general.own.success");
			user.sendLocalizedMessage("general.own.warnAfter");
		}
		
		return true;
	}
}
