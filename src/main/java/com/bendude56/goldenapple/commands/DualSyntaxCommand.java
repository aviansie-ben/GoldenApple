package com.bendude56.goldenapple.commands;

import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;

import com.bendude56.goldenapple.GoldenApple;
import com.bendude56.goldenapple.User;

public abstract class DualSyntaxCommand implements CommandExecutor {

	@Override
	public final boolean onCommand(CommandSender sender, Command command, String commandLabel, String[] args) {
		User user = User.getUser(sender);
		
		if (user.isUsingComplexCommands())
			onCommandComplex(GoldenApple.getInstance(), user, commandLabel, args);
		else
			onCommandSimple(GoldenApple.getInstance(), user, commandLabel, args);
		
		return true;
	}
	
	public abstract void onCommandComplex(GoldenApple instance, User user, String commandLabel, String[] args);
	
	public abstract void onCommandSimple(GoldenApple instance, User user, String commandLabel, String[] args);

}
