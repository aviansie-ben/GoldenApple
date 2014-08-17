package com.bendude56.goldenapple.command;

import java.util.HashMap;

import org.bukkit.Bukkit;

import com.bendude56.goldenapple.GoldenApple;
import com.bendude56.goldenapple.User;

public class VerifyCommand extends GoldenAppleCommand {
	public static final HashMap<User, String>	commands	= new HashMap<User, String>();

	@Override
	public boolean onExecute(GoldenApple instance, User user, String commandLabel, String[] args) {
		if (commands.containsKey(user)) {
			Bukkit.dispatchCommand(user.getHandle(), commands.get(user));
			commands.remove(user);
		} else {
			user.sendLocalizedMessage("module.base.verify.noCommand");
		}
		return true;
	}
}
