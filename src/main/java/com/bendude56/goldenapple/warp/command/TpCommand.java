package com.bendude56.goldenapple.warp.command;

import org.bukkit.entity.Player;
import org.bukkit.event.player.PlayerTeleportEvent.TeleportCause;

import com.bendude56.goldenapple.GoldenApple;
import com.bendude56.goldenapple.User;
import com.bendude56.goldenapple.command.GoldenAppleCommand;
import com.bendude56.goldenapple.warp.WarpManager;

public class TpCommand extends GoldenAppleCommand {
	@Override
	public boolean onExecute(GoldenApple instance, User user, String commandLabel, String[] args) {
		if (args.length == 0) {
			return false;
		} else if (args.length == 1) {
			User user2 = User.findUser(args[0]);
			if (!(user.getHandle() instanceof Player)) {
				user.sendLocalizedMessage("shared.noConsole");
			} else if (!user.hasPermission(WarpManager.tpSelfToOtherPermission)) {
				GoldenApple.logPermissionFail(user, commandLabel, args, true);
			} else if (user2 == null) {
				user.sendLocalizedMessage("shared.userNotFoundError", args[0]);
			} else if (user.getPlayerHandle().teleport(user2.getPlayerHandle(), TeleportCause.COMMAND)) {
			} else {
				user.sendLocalizedMessage("error.warps.pluginCancel");
			}
		} else {
			User user1 = User.findUser(args[0]);
			User user2 = User.findUser(args[1]);
			if (!user.hasPermission(WarpManager.tpOtherToOtherPermission)) {
				GoldenApple.logPermissionFail(user, commandLabel, args, true);
			} else if (user1 == null) {
				user.sendLocalizedMessage("shared.userNotFoundError", args[0]);
			} else if (user2 == null) {
				user.sendLocalizedMessage("shared.userNotFoundError", args[1]);
			} else if (user1.getPlayerHandle().teleport(user2.getPlayerHandle(), TeleportCause.COMMAND)) {
				user1.sendLocalizedMessage("general.warps.teleportBy", user.getName());
			} else {
				user.sendLocalizedMessage("error.warps.pluginCancel");
			}
		}
		
		return true;
	}
}
