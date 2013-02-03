package com.bendude56.goldenapple.commands;

import java.sql.SQLException;
import java.util.logging.Level;

import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import com.bendude56.goldenapple.GoldenApple;
import com.bendude56.goldenapple.User;
import com.bendude56.goldenapple.warp.HomeWarp;
import com.bendude56.goldenapple.warp.WarpManager;

public class SetHomeCommand implements CommandExecutor {
	@Override
	public boolean onCommand(CommandSender sender, Command command, String commandLabel, String[] args) {
		GoldenApple instance = GoldenApple.getInstance();
		User user = User.getUser(sender);
		
		int homeNumber = 1;
		boolean isHomePublic = false;
		String alias = null;
		
		if (args.length >= 1) {
			try {
				homeNumber = Integer.parseInt(args[0]);
			} catch (NumberFormatException e) {
				instance.locale.sendMessage(user, "shared.notANumber", false, args[0]);
				return true;
			}
			if (homeNumber <= 0) {
				instance.locale.sendMessage(user, "shared.notANumber", false, args[0]);
				return true;
			}
		}
		
		if (args.length >= 2) {
			isHomePublic = (args[1].equalsIgnoreCase("public"));
		}
		
		if (args.length >= 3) {
			alias = args[2];
		}
		
		if (user.hasPermission(WarpManager.homeEditOwn)) {
			if (homeNumber > WarpManager.maxHomes) {
				instance.locale.sendMessage(user, "error.home.setMax", false, WarpManager.maxHomes + "");
				return true;
			}
			
			HomeWarp h = new HomeWarp(user.getId(), homeNumber, user.getPlayerHandle().getLocation(), alias, isHomePublic);
			try {
				h.delete();
				h.insert();
				instance.locale.sendMessage(user, "general.home.set", false, homeNumber + "");
			} catch (SQLException e) {
				GoldenApple.log(Level.SEVERE, "Failed to edit " + user.getName() + "'s home " + homeNumber + ":");
				GoldenApple.log(Level.SEVERE, e);
				instance.locale.sendMessage(user, "error.home.setFail", false);
			}
		} else {
			GoldenApple.logPermissionFail(user, commandLabel, args, true);
		}
		
		return true;
	}
}
