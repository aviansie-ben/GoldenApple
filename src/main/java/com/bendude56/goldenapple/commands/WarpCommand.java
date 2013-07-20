package com.bendude56.goldenapple.commands;

import com.bendude56.goldenapple.GoldenApple;
import com.bendude56.goldenapple.User;
import com.bendude56.goldenapple.warp.PermissibleWarp;
import com.bendude56.goldenapple.warp.WarpManager;

public class WarpCommand extends GoldenAppleCommand {
	@Override
	public boolean onExecute(GoldenApple instance, User user, String commandLabel, String[] args) {
		if (args.length != 1) return false;
		
		PermissibleWarp w = WarpManager.getInstance().getNamedWarp(args[0]);
		
		if (w == null) {
			user.sendLocalizedMessage("error.warp.notFound", args[0]);
		} else if (w.canTeleport(user)) {
			w.teleport(user);
		} else {
			GoldenApple.logPermissionFail(user, commandLabel, args, true);
		}
		
		return true;
	}
}
