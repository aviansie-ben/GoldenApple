package com.bendude56.goldenapple.commands;

import java.util.HashMap;

import com.bendude56.goldenapple.GoldenApple;
import com.bendude56.goldenapple.ModuleLoader.ModuleState;
import com.bendude56.goldenapple.permissions.PermissionManager;
import com.bendude56.goldenapple.warp.WarpManager;
import com.bendude56.goldenapple.User;

public class ImportCommand extends GoldenAppleCommand {
	private static HashMap<String, String> typeModules = new HashMap<String, String>();
	
	static {
		typeModules.put("homes", "Warp");
	}
	
	@Override
	public boolean onExecute(GoldenApple instance, User user, String commandLabel, String[] args) {
		if (args.length < 2) {
			return false;
		} else if (!user.hasPermission(PermissionManager.importPermission)) {
			GoldenApple.logPermissionFail(user, commandLabel, args, true);
		} else {
			String type = args[0].toLowerCase();
			if (!typeModules.containsKey(type)) {
				user.sendLocalizedMessage("error.import.typeNotFound");
				return true;
			} else if (GoldenApple.getInstance().getModuleManager().getModule(typeModules.get(type)).getCurrentState() != ModuleState.LOADED) {
				user.sendLocalizedMessage("error.import.moduleNotReady");
				return true;
			}
			
			ImportablePlugin pl;
			if (args[1].equalsIgnoreCase("Essentials")) {
				pl = new EssentialsImporter();
			} else {
				user.sendLocalizedMessage("error.import.pluginNotFound");
				return true;
			}
			
			if (!pl.isImportTypeSupported(type)) {
				user.sendLocalizedMessage("error.import.typeNotSupported", type, pl.getName());
			} else if (args.length >= 3 && args[2].equals("-v")) {
				pl.executeImport(type, user, args);
			} else {
				user.sendLocalizedMultilineMessage("general.import.warning", type, pl.getName());
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
				u.sendLocalizedMessage("general.import.started");
				WarpManager.getInstance().importHomesFromEssentials(u);
			} else {
				throw new UnsupportedOperationException();
			}
		}
	}
}
