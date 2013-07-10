package com.bendude56.goldenapple.commands;

import org.bukkit.entity.Player;
import org.bukkit.event.player.PlayerTeleportEvent.TeleportCause;

import com.bendude56.goldenapple.GoldenApple;
import com.bendude56.goldenapple.User;
import com.bendude56.goldenapple.listener.WarpListener;
import com.bendude56.goldenapple.warp.WarpManager;

public class BackCommand extends GoldenAppleCommand {
	@Override
	public boolean onExecute(GoldenApple instance, User user, String commandLabel, String[] args) {
		if (user.getHandle() instanceof Player) {
			if (!user.hasPermission(WarpManager.backPermission)) {
				GoldenApple.logPermissionFail(user, commandLabel, args, true);
			} else if (!WarpListener.backLocation.containsKey(user)) {
				user.sendLocalizedMessage("error.warps.noBack");
			} else if (user.getPlayerHandle().teleport(WarpListener.backLocation.get(user), TeleportCause.COMMAND)) {
			} else {
				user.sendLocalizedMessage("error.warps.pluginCancel");
			}
		} else {
			user.sendLocalizedMessage("shared.noConsole");
		}
		
		return true;
	}
}
