package com.bendude56.goldenapple.commands;

import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.event.player.PlayerTeleportEvent.TeleportCause;

import com.bendude56.goldenapple.GoldenApple;
import com.bendude56.goldenapple.User;
import com.bendude56.goldenapple.listener.WarpListener;
import com.bendude56.goldenapple.warp.WarpModuleLoader;

public class BackCommand implements CommandExecutor {
	@Override
	public boolean onCommand(CommandSender sender, Command command, String commandLabel, String[] args) {
		GoldenApple instance = GoldenApple.getInstance();
		User user = User.getUser(sender);
		
		if (user.getHandle() instanceof Player) {
			if (!user.hasPermission(WarpModuleLoader.backPermission)) {
				GoldenApple.logPermissionFail(user, commandLabel, args, true);
			} else if (!WarpListener.backLocation.containsKey(user)) {
				GoldenApple.getInstance().locale.sendMessage(user, "error.warps.noBack", false);
			} else if (user.getPlayerHandle().teleport(WarpListener.backLocation.get(user), TeleportCause.COMMAND)) {
			} else {
				GoldenApple.getInstance().locale.sendMessage(user, "error.warps.pluginCancel", false);
			}
		} else {
			instance.locale.sendMessage(user, "shared.noConsole", false);
		}
		
		return true;
	}
}
