package com.bendude56.goldenapple.commands;

import java.util.HashMap;

import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;

import com.bendude56.goldenapple.GoldenApple;
import com.bendude56.goldenapple.User;
import com.bendude56.goldenapple.permissions.PermissionManager;

public class UnloadedCommand implements CommandExecutor {
	private static HashMap<String, String> reqModule = new HashMap<String, String>();
	
	static {
		reqModule.put("gapermissions", "Permissions");
		reqModule.put("gaown", "Permissions");
		reqModule.put("galock", "Lock");
		reqModule.put("gaautolock", "Lock");
		reqModule.put("game", "Chat");
		reqModule.put("gachannel", "Chat");
		reqModule.put("gaspawn", "Warp");
		reqModule.put("gatp", "Warp");
		reqModule.put("gatphere", "Warp");
		reqModule.put("gaback", "Warp");
	}
	
	@Override
	public boolean onCommand(CommandSender sender, Command command, String commandLabel, String[] args) {
		User user = User.getUser(sender);
		
		if (user.getHandle().isOp() || user.hasPermission(PermissionManager.moduleQueryPermission) && reqModule.containsKey(command.getName()))
			GoldenApple.getInstance().locale.sendMessage(user, "shared.cmdUnload.specific", false, reqModule.get(command.getName()));
		else
			GoldenApple.getInstance().locale.sendMessage(user, "shared.cmdUnload.generic", false);
		
		return true;
	}
}
