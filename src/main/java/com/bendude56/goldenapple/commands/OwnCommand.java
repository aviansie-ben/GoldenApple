package com.bendude56.goldenapple.commands;

import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;

import com.bendude56.goldenapple.GoldenApple;
import com.bendude56.goldenapple.User;

public class OwnCommand implements CommandExecutor {
	@Override
	public boolean onCommand(CommandSender sender, Command command, String commandLabel, String[] args) {
		GoldenApple instance = GoldenApple.getInstance();
		User user = User.getUser(sender);
		
		if (!user.getHandle().isOp()) {
			GoldenApple.logPermissionFail(user, commandLabel, args, true);
			return true;
		}
		
		if (instance.mainConfig.getBoolean("securityPolicy.disableOwn")) {
			instance.locale.sendMessage(user, "error.own.disabled", false);
			GoldenApple.logPermissionFail(user, commandLabel, args, false);
			return true;
		}
		
		if (args.length == 0 || !args[0].equals("-v")) {
			instance.locale.sendMessage(user, "general.own.warnBefore", false);
			VerifyCommand.commands.put(user, "gaown -v");
		} else {
			user.addPermission(instance.permissions.rootStar);
			instance.locale.sendMessage(user, "general.own.success", false);
			instance.locale.sendMessage(user, "general.own.warnAfter", false);
		}
		
		return true;
	}
}
