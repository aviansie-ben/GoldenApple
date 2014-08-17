package com.bendude56.goldenapple.warp.command;

import org.bukkit.entity.Player;
import org.bukkit.event.player.PlayerTeleportEvent.TeleportCause;

import com.bendude56.goldenapple.GoldenApple;
import com.bendude56.goldenapple.User;
import com.bendude56.goldenapple.command.GoldenAppleCommand;
import com.bendude56.goldenapple.warp.WarpListener;
import com.bendude56.goldenapple.warp.WarpManager;

public class BackCommand extends GoldenAppleCommand {
	@Override
	public boolean onExecute(GoldenApple instance, User user, String commandLabel, String[] args) {
		if (user.getHandle() instanceof Player) {
			if (!user.hasPermission(WarpManager.backPermission)) {
				GoldenApple.logPermissionFail(user, commandLabel, args, true);
			} else if (!WarpListener.backLocation.containsKey(user)) {
				user.sendLocalizedMessage("module.warp.error.noBack");
			} else {
				int deathCooldown = WarpManager.getInstance().getDeathCooldown(user), teleportCooldown = WarpManager.getInstance().getTeleportCooldown(user);
				
				if (deathCooldown > 0) {
					user.sendLocalizedMessage("module.warp.error.cooldown.death", deathCooldown );
				} else if (teleportCooldown > 0) {
					user.sendLocalizedMessage("module.warp.error.cooldown.normal", teleportCooldown );
				} else if (!user.getPlayerHandle().teleport(WarpListener.backLocation.get(user), TeleportCause.COMMAND)) {
					user.sendLocalizedMessage("module.warp.error.pluginCancel");
				} else {
					WarpManager.getInstance().startTeleportCooldown(user);
				}
			}
		} else {
			user.sendLocalizedMessage("shared.consoleNotAllowed");
		}
		
		return true;
	}
}
