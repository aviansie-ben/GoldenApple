package com.bendude56.goldenapple.punish;

import com.bendude56.goldenapple.CommandManager;
import com.bendude56.goldenapple.GoldenApple;
import com.bendude56.goldenapple.IModuleLoader;
import com.bendude56.goldenapple.listener.PunishmentListener;
import com.bendude56.goldenapple.permissions.PermissionManager;

public class PunishModuleLoader implements IModuleLoader {

	private static ModuleState state = ModuleState.UNLOADED_USER;

	@Override
	public void loadModule(GoldenApple instance) {
		state = ModuleState.LOADING;
		try {
			instance.punish = new PunishmentManager();
			registerPermissions(instance.permissions);
			registerEvents();
			registerCommands(instance.commands);
			state = ModuleState.LOADED;
		} catch (Throwable e) {
			state = ModuleState.UNLOADED_ERROR;
			unregisterCommands(instance.commands);
		}
	}
	
	@Override
	public void registerPermissions(PermissionManager permissions) {
		PunishmentManager.punishNode = permissions.registerNode("punish", PermissionManager.goldenAppleNode);
		
		PunishmentManager.banNode = permissions.registerNode("ban", PunishmentManager.punishNode);
		PunishmentManager.banTempPermission = permissions.registerPermission("temp", PermissionManager.goldenAppleNode);
		PunishmentManager.banTempOverridePermission = permissions.registerPermission("tempOverride", PermissionManager.goldenAppleNode);
		PunishmentManager.banPermPermission = permissions.registerPermission("perm", PermissionManager.goldenAppleNode);
		PunishmentManager.banVoidPermission = permissions.registerPermission("void", PermissionManager.goldenAppleNode);
		PunishmentManager.banVoidAllPermission = permissions.registerPermission("voidAll", PermissionManager.goldenAppleNode);
	}
	
	private void registerEvents() {
		PunishmentListener.startListening();
	}
	
	private void registerCommands(CommandManager commands) {
		commands.getCommand("gaban").register();
		commands.getCommand("gamute").register();
		commands.getCommand("gaglobalmute").register();
		commands.getCommand("gawhois").register();
	}

	@Override
	public void unloadModule(GoldenApple instance) {
		unregisterEvents();
		unregisterCommands(instance.commands);
		GoldenApple.getInstance().locks = null;
		state = ModuleState.UNLOADED_USER;
	}
	
	private void unregisterCommands(CommandManager commands) {
		commands.getCommand("gaban").unregister();
		commands.getCommand("gamute").unregister();
		commands.getCommand("gaglobalmute").unregister();
		commands.getCommand("gawhois").unregister();
	}
	
	private void unregisterEvents() {
		PunishmentListener.stopListening();
	}

	@Override
	public String getModuleName() {
		return "Punish";
	}

	@Override
	public ModuleState getCurrentState() {
		return state;
	}
	
	@Override
	public void setState(ModuleState state) {
		PunishModuleLoader.state = state;
	}

	@Override
	public String[] getModuleDependencies() {
		return new String[] { "Permissions" };
	}

	@Override
	public boolean canLoadAuto() {
		return GoldenApple.getInstance().mainConfig.getBoolean("modules.punish.enabled", true);
	}

	@Override
	public boolean canPolicyLoad() {
		return !GoldenApple.getInstance().mainConfig.getBoolean("securityPolicy.blockModules.punish", false);
	}
	
	@Override
	public boolean canPolicyUnload() {
		return !GoldenApple.getInstance().mainConfig.getBoolean("securityPolicy.blockManualUnload.punish", false);
	}

}
