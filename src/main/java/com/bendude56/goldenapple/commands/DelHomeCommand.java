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

public class DelHomeCommand implements CommandExecutor {
	@Override
	public boolean onCommand(CommandSender sender, Command command, String commandLabel, String[] args) {
		GoldenApple instance = GoldenApple.getInstance();
		User user = User.getUser(sender);
		
		int homeNumber = 1;
		String homeAlias = null;
		
		if (args.length > 1) {
			return false;
		} else if (args.length == 1) {
			try {
				homeNumber = Integer.parseInt(args[0]);
			} catch (NumberFormatException e) {
				homeAlias = args[0];
			}
		}
		
		if (user.hasPermission(WarpManager.homeEditOwn)) {
			HomeWarp h;
			
			if (homeAlias == null) {
				h = GoldenApple.getInstance().warps.getHome(user, homeNumber);
				if (h == null) {
					instance.locale.sendMessage(user, "error.home.notFoundIdSelf", false, homeNumber + "");
				}
			} else {
				h = GoldenApple.getInstance().warps.getHome(user, homeAlias);
				if (h == null) {
					instance.locale.sendMessage(user, "error.home.notFoundAliasSelf", false, args[0]);
				}
			}
			
			try {
				h.delete();
				if (homeAlias == null)
					instance.locale.sendMessage(user, "general.home.deleteId", false, homeNumber + "");
				else
					instance.locale.sendMessage(user, "general.home.deleteAlias", false, homeNumber + "");
			} catch (SQLException e) {
				GoldenApple.log(Level.SEVERE, "Failed to edit " + user.getName() + "'s home " + homeNumber + ":");
				GoldenApple.log(Level.SEVERE, e);
				instance.locale.sendMessage(user, "error.home.setFail", false);
			}
		}
		
		return true;
	}
}
