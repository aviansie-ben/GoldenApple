package com.bendude56.goldenapple.commands;

import org.bukkit.Bukkit;
import org.bukkit.World;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.event.player.PlayerTeleportEvent.TeleportCause;

import com.bendude56.goldenapple.GoldenApple;
import com.bendude56.goldenapple.User;
import com.bendude56.goldenapple.warp.WarpModuleLoader;

public class SpawnCommand implements CommandExecutor {
	@Override
	public boolean onCommand(CommandSender sender, Command command, String commandLabel, String[] args) {
		GoldenApple instance = GoldenApple.getInstance();
		User user = User.getUser(sender);
		
		if (user.getHandle() instanceof Player) {
			if (args.length == 0) {
				if (!user.hasPermission(WarpModuleLoader.spawnCurrentPermission)) {
					GoldenApple.logPermissionFail(user, commandLabel, args, true);
				} else if (user.getPlayerHandle().teleport(user.getPlayerHandle().getWorld().getSpawnLocation(), TeleportCause.COMMAND)) {
					GoldenApple.getInstance().locale.sendMessage(user, "general.warps.teleportSpawn", false);
				} else {
					GoldenApple.getInstance().locale.sendMessage(user, "error.warps.pluginCancel", false);
				}
			} else {
				World w = Bukkit.getWorld(args[0]);
				if (!user.hasPermission(WarpModuleLoader.spawnAllPermission)) {
					GoldenApple.logPermissionFail(user, commandLabel, args, true);
				} else if (w == null) {
					GoldenApple.getInstance().locale.sendMessage(user, "shared.worldNotFoundError", false, args[0]);
				} else if (user.getPlayerHandle().teleport(w.getSpawnLocation(), TeleportCause.COMMAND)) {
					GoldenApple.getInstance().locale.sendMessage(user, "general.warps.teleportSpawnWorld", false, w.getName());
				} else {
					GoldenApple.getInstance().locale.sendMessage(user, "error.warps.pluginCancel", false);
				}
			}
		} else {
			instance.locale.sendMessage(user, "shared.noConsole", false);
		}
		
		return true;
	}
}
