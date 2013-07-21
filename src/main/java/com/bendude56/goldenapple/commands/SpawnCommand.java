package com.bendude56.goldenapple.commands;

import org.bukkit.Bukkit;
import org.bukkit.World;
import org.bukkit.entity.Player;
import org.bukkit.event.player.PlayerTeleportEvent.TeleportCause;

import com.bendude56.goldenapple.GoldenApple;
import com.bendude56.goldenapple.User;
import com.bendude56.goldenapple.warp.WarpManager;

public class SpawnCommand extends GoldenAppleCommand {
	@Override
	public boolean onExecute(GoldenApple instance, User user, String commandLabel, String[] args) {
		if (user.getHandle() instanceof Player) {
			if (args.length == 0) {
				if (!user.hasPermission(WarpManager.spawnCurrentPermission)) {
					GoldenApple.logPermissionFail(user, commandLabel, args, true);
				} else {
					String w = GoldenApple.getInstanceMainConfig().getString("modules.warps.defaultSpawn", "current");
					World world = (w == "current") ? user.getPlayerHandle().getWorld() : Bukkit.getWorld(w);
					
					if (world == null) {
						user.sendLocalizedMessage("shared.worldNotFoundError", w);
					} else if (user.getPlayerHandle().teleport(world.getSpawnLocation(), TeleportCause.COMMAND)) {
						user.sendLocalizedMessage("general.warps.teleportSpawn");
					} else {
						user.sendLocalizedMessage("error.warps.pluginCancel");
					}
				}
			} else {
				World w = Bukkit.getWorld(args[0]);
				if (!user.hasPermission(WarpManager.spawnAllPermission)) {
					GoldenApple.logPermissionFail(user, commandLabel, args, true);
				} else if (w == null) {
					user.sendLocalizedMessage("shared.worldNotFoundError", args[0]);
				} else if (user.getPlayerHandle().teleport(w.getSpawnLocation(), TeleportCause.COMMAND)) {
					user.sendLocalizedMessage("general.warps.teleportSpawnWorld", w.getName());
				} else {
					user.sendLocalizedMessage("error.warps.pluginCancel");
				}
			}
		} else {
			user.sendLocalizedMessage("shared.noConsole");
		}
		
		return true;
	}
}
