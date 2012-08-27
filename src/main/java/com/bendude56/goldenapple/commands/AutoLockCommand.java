package com.bendude56.goldenapple.commands;

import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import com.bendude56.goldenapple.GoldenApple;
import com.bendude56.goldenapple.User;

public class AutoLockCommand implements CommandExecutor {
	@Override
	public boolean onCommand(CommandSender sender, Command command, String commandLabel, String[] args) {
		GoldenApple instance = GoldenApple.getInstance();
		User user = User.getUser(sender);
		
		if (user.getHandle() instanceof Player) {
			user.setAutoLockEnabled(!user.isAutoLockEnabled());
			instance.locale.sendMessage(user, (user.isAutoLockEnabled()) ? "general.lock.auto.on" : "general.lock.auto.off", false);
		} else {
			instance.locale.sendMessage(user, "shared.noConsole", false);
		}
		
		return true;
	}
}
