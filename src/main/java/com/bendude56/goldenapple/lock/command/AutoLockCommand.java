package com.bendude56.goldenapple.lock.command;

import org.bukkit.entity.Player;

import com.bendude56.goldenapple.GoldenApple;
import com.bendude56.goldenapple.User;
import com.bendude56.goldenapple.command.GoldenAppleCommand;

public class AutoLockCommand extends GoldenAppleCommand {
	@Override
	public boolean onExecute(GoldenApple instance, User user, String commandLabel, String[] args) {
		if (user.getHandle() instanceof Player) {
			user.setAutoLockEnabled(!user.isAutoLockEnabled());
			user.sendLocalizedMessage((user.isAutoLockEnabled()) ? "general.lock.auto.on" : "general.lock.auto.off");
		} else {
			user.sendLocalizedMessage("shared.noConsole");
		}
		
		return true;
	}
}
