package com.bendude56.goldenapple.commands;

import java.sql.SQLException;
import java.util.logging.Level;

import com.bendude56.goldenapple.GoldenApple;
import com.bendude56.goldenapple.User;
import com.bendude56.goldenapple.warp.WarpManager;

public class SetWarpCommand extends GoldenAppleCommand {
	@Override
	public boolean onExecute(GoldenApple instance, User user, String commandLabel, String[] args) {
		if (args.length != 1) return false;
		
		if (user.hasPermission(WarpManager.editPermission)) {
			try {
				WarpManager.getInstance().setNamedWarp(args[0], user.getPlayerHandle().getLocation());
				user.sendLocalizedMessage("general.warp.set", args[0]);
			} catch (SQLException e) {
				GoldenApple.log(Level.SEVERE, "Failed to edit warp '" + args[0] + "':");
				GoldenApple.log(Level.SEVERE, e);
				user.sendLocalizedMessage("error.warp.setFail");
			}
		} else {
			GoldenApple.logPermissionFail(user, commandLabel, args, true);
		}
		
		return true;
	}
}
