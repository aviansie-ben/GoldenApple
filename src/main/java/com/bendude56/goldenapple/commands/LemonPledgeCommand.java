package com.bendude56.goldenapple.commands;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;

public class LemonPledgeCommand implements CommandExecutor {
	@Override
	public boolean onCommand(CommandSender sender, Command command, String commandLabel, String[] args) {
		Bukkit.dispatchCommand(sender, "me " + ChatColor.YELLOW + "demands more lemon pledge!");
		return true;
	}
}
