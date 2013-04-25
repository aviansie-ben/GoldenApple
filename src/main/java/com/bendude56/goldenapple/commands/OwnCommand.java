package com.bendude56.goldenapple.commands;

import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;

import com.bendude56.goldenapple.GoldenApple;
import com.bendude56.goldenapple.User;
import com.bendude56.goldenapple.permissions.PermissionManager;

public class OwnCommand implements CommandExecutor {
	@Override
	public boolean onCommand(CommandSender sender, Command command, String commandLabel, String[] args) {
		User user = User.getUser(sender);
		
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
			user.addPermission(PermissionManager.getInstance().getRootStar());
			user.sendLocalizedMessage("general.own.success");
			user.sendLocalizedMessage("general.own.warnAfter");
		}
		
		return true;
	}
}
