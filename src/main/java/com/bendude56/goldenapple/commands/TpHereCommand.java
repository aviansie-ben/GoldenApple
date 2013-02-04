package com.bendude56.goldenapple.commands;

import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.event.player.PlayerTeleportEvent.TeleportCause;

import com.bendude56.goldenapple.GoldenApple;
import com.bendude56.goldenapple.User;
import com.bendude56.goldenapple.warp.WarpManager;

public class TpHereCommand implements CommandExecutor {
	@Override
	public boolean onCommand(CommandSender sender, Command command, String commandLabel, String[] args) {
		GoldenApple instance = GoldenApple.getInstance();
		User user = User.getUser(sender);
		
		if (args.length == 0) {
			return false;
		} else {
			User user2 = User.getUser(args[0]);
			if (!(user.getHandle() instanceof Player)) {
				instance.locale.sendMessage(user, "shared.noConsole", false);
			} else if (!user.hasPermission(WarpManager.tpOtherToSelfPermission)) {
				GoldenApple.logPermissionFail(user, commandLabel, args, true);
			} else if (user2 == null) {
				GoldenApple.getInstance().locale.sendMessage(user, "shared.userNotFoundError", false, args[0]);
			} else if (user2.getPlayerHandle().teleport(user.getPlayerHandle(), TeleportCause.COMMAND)) {
				GoldenApple.getInstance().locale.sendMessage(user2, "general.warps.teleportBy", false, user.getName());
			} else {
				GoldenApple.getInstance().locale.sendMessage(user, "error.warps.pluginCancel", false);
			}
		}
		
		return true;
	}
}