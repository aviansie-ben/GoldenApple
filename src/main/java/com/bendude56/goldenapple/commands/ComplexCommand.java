package com.bendude56.goldenapple.commands;

import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import com.bendude56.goldenapple.User;

public class ComplexCommand implements CommandExecutor {
	@Override
	public boolean onCommand(CommandSender sender, Command command, String commandLabel, String[] args) {
		User user = User.getUser(sender);
		
		if (user.getHandle() instanceof Player) {
			if (args.length == 1 && args[0].equals("-v")) {
				user.setUsingComplexCommands(!user.isUsingComplexCommands());
				user.sendLocalizedMessage((user.isUsingComplexCommands()) ? "general.complex.successOn" : "general.complex.successOff");
			} else {
				user.sendLocalizedMessage((user.isUsingComplexCommands()) ? "general.complex.warnOff" : "general.complex.warnOn");
				VerifyCommand.commands.put(user, commandLabel + " -v");
			}
		} else {
			user.sendLocalizedMessage("shared.noConsole");
		}
		
		return true;
	}
}
