package com.bendude56.goldenapple.commands;

import java.util.HashMap;

import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import com.bendude56.goldenapple.GoldenApple;
import com.bendude56.goldenapple.IModuleLoader.ModuleState;
import com.bendude56.goldenapple.permissions.PermissionManager;
import com.bendude56.goldenapple.User;

public class ImportCommand implements CommandExecutor {
	private static HashMap<String, String> typeModules = new HashMap<String, String>();
	
	static {
		typeModules.put("homes", "Warp");
	}
	
	@Override
	public boolean onCommand(CommandSender sender, Command command, String commandLabel, String[] args) {
		GoldenApple instance = GoldenApple.getInstance();
		User user = User.getUser(sender);
		
		if (args.length < 2) {
			return false;
		} else if (!user.hasPermission(PermissionManager.importPermission)) {
			GoldenApple.logPermissionFail(user, commandLabel, args, true);
		} else {
			String type = args[0].toLowerCase();
			if (!typeModules.containsKey(type)) {
				instance.locale.sendMessage(user, "error.import.typeNotFound", false);
				return true;
			} else if (GoldenApple.modules.get(typeModules.get(type)).getCurrentState() != ModuleState.LOADED) {
				instance.locale.sendMessage(user, "error.import.moduleNotReady", false);
				return true;
			}
			
			ImportablePlugin pl;
			if (args[1].equalsIgnoreCase("Essentials")) {
				pl = new EssentialsImporter();
			} else {
				instance.locale.sendMessage(user, "error.import.pluginNotFound", false);
				return true;
			}
			
			if (!pl.isImportTypeSupported(type)) {
				instance.locale.sendMessage(user, "error.import.typeNotSupported", false, type, pl.getName());
			} else if (args.length >= 3 && args[2].equals("-v")) {
				pl.executeImport(type, user, args);
			} else {
				instance.locale.sendMessage(user, "general.import.warning", true, type, pl.getName());
				VerifyCommand.commands.put(user, "gaimport " + type + " " + pl.getName() + " -v");
			}
		}
		
		return true;
	}
	
	private abstract class ImportablePlugin {
		public abstract String getName();
		public abstract String[] getSupportedImportTypes();
		public abstract void executeImport(String type, User u, String[] args);
		
		public boolean isImportTypeSupported(String type) {
			String[] supported = getSupportedImportTypes();
			
			for (String sType : supported) {
				if (sType.equalsIgnoreCase(type))
					return true;
			}
			
			return false;
		}
	}
	
	private class EssentialsImporter extends ImportablePlugin {
		@Override
		public String getName() { return "Essentials"; }
		@Override
		public String[] getSupportedImportTypes() { return new String[] { "homes" }; }

		@Override
		public void executeImport(String type, User u, String[] args) {
			if (type.equalsIgnoreCase("homes")) {
				GoldenApple.getInstance().locale.sendMessage(u, "general.import.started", false);
				GoldenApple.getInstance().warps.importHomesFromEssentials(u);
			} else {
				throw new UnsupportedOperationException();
			}
		}
	}
}
