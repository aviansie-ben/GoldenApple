package com.bendude56.goldenapple.commands;

import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;

import com.bendude56.goldenapple.GoldenApple;
import com.bendude56.goldenapple.User;

public class PermissionsCommand implements CommandExecutor {
	@Override
	public boolean onCommand(CommandSender sender, Command command, String commandLabel, String[] args) {
		User user = User.getUser(sender);
		if (args.length == 0 || args[0] == "help") {
			GoldenApple.getInstance().locale.sendMessage(user, "shared.header.help", false);
			GoldenApple.getInstance().locale.sendMessage(user, "help.permissions", true, commandLabel);
		}
		return true;
	}
}
