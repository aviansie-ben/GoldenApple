package com.bendude56.goldenapple.warp.command;

import java.sql.SQLException;
import java.util.logging.Level;

import com.bendude56.goldenapple.GoldenApple;
import com.bendude56.goldenapple.User;
import com.bendude56.goldenapple.command.GoldenAppleCommand;
import com.bendude56.goldenapple.warp.HomeWarp;
import com.bendude56.goldenapple.warp.WarpManager;

public class SetHomeCommand extends GoldenAppleCommand {
	@Override
	public boolean onExecute(GoldenApple instance, User user, String commandLabel, String[] args) {
		int homeNumber = 1;
		boolean isHomePublic = false;
		String alias = null;
		
		if (args.length >= 1) {
			try {
				homeNumber = Integer.parseInt(args[0]);
			} catch (NumberFormatException e) {
				user.sendLocalizedMessage("shared.notANumber", args[0]);
				return true;
			}
			if (homeNumber <= 0) {
				user.sendLocalizedMessage("shared.notANumber", args[0]);
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
			if (homeNumber > WarpManager.getInstance().getMaxHomes()) {
				user.sendLocalizedMessage("error.home.setMax", WarpManager.getInstance().getMaxHomes() + "");
				return true;
			}
			
			HomeWarp h = new HomeWarp(user.getId(), homeNumber, user.getPlayerHandle().getLocation(), alias, isHomePublic);
			try {
				h.delete();
				h.insert();
				user.sendLocalizedMessage("general.home.set", homeNumber + "");
			} catch (SQLException e) {
				GoldenApple.log(Level.SEVERE, "Failed to edit " + user.getName() + "'s home " + homeNumber + ":");
				GoldenApple.log(Level.SEVERE, e);
				user.sendLocalizedMessage("error.home.setFail");
			}
		} else {
			GoldenApple.logPermissionFail(user, commandLabel, args, true);
		}
		
		return true;
	}
}
