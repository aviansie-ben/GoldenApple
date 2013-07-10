package com.bendude56.goldenapple.commands;

import org.bukkit.entity.Player;

import com.bendude56.goldenapple.GoldenApple;
import com.bendude56.goldenapple.User;

public class ComplexCommand extends GoldenAppleCommand {
	@Override
	public boolean onExecute(GoldenApple instance, User user, String commandLabel, String[] args) {
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
