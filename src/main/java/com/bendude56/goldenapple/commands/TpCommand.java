package com.bendude56.goldenapple.commands;

import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.event.player.PlayerTeleportEvent.TeleportCause;

import com.bendude56.goldenapple.GoldenApple;
import com.bendude56.goldenapple.User;
import com.bendude56.goldenapple.warp.WarpModuleLoader;

public class TpCommand implements CommandExecutor {
	@Override
	public boolean onCommand(CommandSender sender, Command command, String commandLabel, String[] args) {
		GoldenApple instance = GoldenApple.getInstance();
		User user = User.getUser(sender);
		
		if (args.length == 0) {
			return false;
		} else if (args.length == 1) {
			User user2 = User.getUser(args[0]);
			if (!(user.getHandle() instanceof Player)) {
				instance.locale.sendMessage(user, "shared.noConsole", false);
			} else if (!user.hasPermission(WarpModuleLoader.tpSelfToOtherPermission)) {
				GoldenApple.logPermissionFail(user, commandLabel, args, true);
			} else if (user2 == null) {
				GoldenApple.getInstance().locale.sendMessage(user, "shared.userNotFoundError", false, args[0]);
			} else if (user.getPlayerHandle().teleport(user2.getPlayerHandle(), TeleportCause.COMMAND)) {
			} else {
				GoldenApple.getInstance().locale.sendMessage(user, "error.warps.pluginCancel", false);
			}
		} else {
			User user1 = User.getUser(args[0]);
			User user2 = User.getUser(args[1]);
			if (!user.hasPermission(WarpModuleLoader.tpOtherToOtherPermission)) {
				GoldenApple.logPermissionFail(user, commandLabel, args, true);
			} else if (user1 == null) {
				GoldenApple.getInstance().locale.sendMessage(user, "shared.userNotFoundError", false, args[0]);
			} else if (user2 == null) {
				GoldenApple.getInstance().locale.sendMessage(user, "shared.userNotFoundError", false, args[1]);
			} else if (user1.getPlayerHandle().teleport(user2.getPlayerHandle(), TeleportCause.COMMAND)) {
				GoldenApple.getInstance().locale.sendMessage(user1, "general.warps.teleportBy", false, user.getName());
			} else {
				GoldenApple.getInstance().locale.sendMessage(user, "error.warps.pluginCancel", false);
			}
		}
		
		return true;
	}
}
