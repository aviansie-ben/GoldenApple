package com.bendude56.goldenapple.commands;

import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import com.bendude56.goldenapple.GoldenApple;
import com.bendude56.goldenapple.User;

public class ComplexCommand implements CommandExecutor {
	@Override
	public boolean onCommand(CommandSender sender, Command command, String commandLabel, String[] args) {
		GoldenApple instance = GoldenApple.getInstance();
		User user = User.getUser(sender);
		
		if (user.getHandle() instanceof Player) {
			if (args.length == 1 && args[0].equals("-v")) {
				user.setUsingComplexCommands(!user.isUsingComplexCommands());
				instance.locale.sendMessage(user, (user.isUsingComplexCommands()) ? "general.complex.successOn" : "general.complex.successOff", false);
			} else {
				instance.locale.sendMessage(user, (user.isUsingComplexCommands()) ? "general.complex.warnOff" : "general.complex.warnOn", false);
				VerifyCommand.commands.put(user, commandLabel + " -v");
			}
		} else {
			instance.locale.sendMessage(user, "shared.noConsole", false);
		}
		
		return true;
	}
}
