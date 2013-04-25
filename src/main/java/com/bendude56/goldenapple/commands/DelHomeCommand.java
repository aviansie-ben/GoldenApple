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
				h = (HomeWarp)WarpManager.getInstance().getHome(user, homeNumber);
				if (h == null) {
					user.sendLocalizedMessage("error.home.notFoundIdSelf", homeNumber + "");
				}
			} else {
				h = (HomeWarp)WarpManager.getInstance().getHome(user, homeAlias);
				if (h == null) {
					user.sendLocalizedMessage("error.home.notFoundAliasSelf", args[0]);
				}
			}
			
			try {
				h.delete();
				if (homeAlias == null)
					user.sendLocalizedMessage("general.home.deleteId", homeNumber + "");
				else
					user.sendLocalizedMessage("general.home.deleteAlias", homeNumber + "");
			} catch (SQLException e) {
				GoldenApple.log(Level.SEVERE, "Failed to edit " + user.getName() + "'s home " + homeNumber + ":");
				GoldenApple.log(Level.SEVERE, e);
				user.sendLocalizedMessage("error.home.setFail");
			}
		}
		
		return true;
	}
}
