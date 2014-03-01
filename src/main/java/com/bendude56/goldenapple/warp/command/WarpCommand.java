package com.bendude56.goldenapple.warp.command;

import com.bendude56.goldenapple.GoldenApple;
import com.bendude56.goldenapple.User;
import com.bendude56.goldenapple.command.GoldenAppleCommand;
import com.bendude56.goldenapple.warp.PermissibleWarp;
import com.bendude56.goldenapple.warp.WarpManager;

public class WarpCommand extends GoldenAppleCommand {
	@Override
	public boolean onExecute(GoldenApple instance, User user, String commandLabel, String[] args) {
		if (args.length != 1) return false;
		
		if (!user.hasPermission(WarpManager.warpPermission)) {
			GoldenApple.logPermissionFail(user, commandLabel, args, true);
			return true;
		}
		
		int deathCooldown = WarpManager.getInstance().getDeathCooldown(user), teleportCooldown = WarpManager.getInstance().getTeleportCooldown(user);
		PermissibleWarp w = WarpManager.getInstance().getNamedWarp(args[0]);
		
		if (w == null) {
			user.sendLocalizedMessage("error.warp.notFound", args[0]);
		} else if (!w.canTeleport(user)) {
			GoldenApple.logPermissionFail(user, commandLabel, args, true);
		} else if (deathCooldown > 0) {
			user.sendLocalizedMessage("error.warps.cooldownDeath", deathCooldown + "");
		} else if (teleportCooldown > 0) {
			user.sendLocalizedMessage("error.warps.cooldown", teleportCooldown + "");
		} else {
			w.teleport(user);
			WarpManager.getInstance().startTeleportCooldown(user);
		}
		
		return true;
	}
}
