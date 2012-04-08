package com.bendude56.goldenapple.commands;

import java.util.HashMap;

import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;

import com.bendude56.goldenapple.GoldenApple;
import com.bendude56.goldenapple.User;

public class VerifyCommand implements CommandExecutor {
	public static final HashMap<User, String> commands = new HashMap<User, String>();
	
	@Override
	public boolean onCommand(CommandSender sender, Command command, String commandLabel, String[] args) {
		User user = User.getUser(sender);
		if (commands.containsKey(user)) {
			Bukkit.dispatchCommand(sender, commands.get(user));
			commands.remove(user);
		} else {
			GoldenApple.getInstance().locale.sendMessage(user, "error.verify.noCommand", false);
		}
		return true;
	}
}
