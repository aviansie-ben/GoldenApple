package com.bendude56.goldenapple.commands;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import com.bendude56.goldenapple.GoldenApple;
import com.bendude56.goldenapple.User;

public class LemonPledgeCommand extends GoldenAppleCommand {
	@Override
	public boolean onExecute(GoldenApple instance, User user, String commandLabel, String[] args) {
		Bukkit.dispatchCommand(user.getHandle(), "me " + ChatColor.YELLOW + "demands more lemon pledge!");
		return true;
	}
}
