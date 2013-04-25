package com.bendude56.goldenapple.punish;

import com.bendude56.goldenapple.CommandManager;
import com.bendude56.goldenapple.GoldenApple;
import com.bendude56.goldenapple.ModuleLoader;
import com.bendude56.goldenapple.listener.PunishmentListener;
import com.bendude56.goldenapple.permissions.PermissionManager;

public class PunishModuleLoader implements ModuleLoader {

	private static ModuleState state = ModuleState.UNLOADED_USER;

	@Override
	public void loadModule(GoldenApple instance) {
		state = ModuleState.LOADING;
		try {
			PunishmentManager.instance = new SimplePunishmentManager();
			registerPermissions(PermissionManager.getInstance());
			registerEvents();
			registerCommands(instance.getCommandManager());
			state = ModuleState.LOADED;
		} catch (Throwable e) {
			state = ModuleState.UNLOADED_ERROR;
			unregisterCommands(instance.getCommandManager());
		}
	}
	
	@Override
	public void registerPermissions(PermissionManager permissions) {
		PunishmentManager.punishNode = permissions.registerNode("punish", PermissionManager.goldenAppleNode);
		
		PunishmentManager.banNode = permissions.registerNode("ban", PunishmentManager.punishNode);
		PunishmentManager.banTempPermission = permissions.registerPermission("temp", PunishmentManager.banNode);
		PunishmentManager.banTempOverridePermission = permissions.registerPermission("tempOverride", PunishmentManager.banNode);
		PunishmentManager.banPermPermission = permissions.registerPermission("perm", PunishmentManager.banNode);
		PunishmentManager.banVoidPermission = permissions.registerPermission("void", PunishmentManager.banNode);
		PunishmentManager.banVoidAllPermission = permissions.registerPermission("voidAll", PunishmentManager.banNode);
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
		unregisterCommands(instance.getCommandManager());
		PunishmentManager.instance = null;
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
		return GoldenApple.getInstanceMainConfig().getBoolean("modules.punish.enabled", true);
	}

	@Override
	public boolean canPolicyLoad() {
		return !GoldenApple.getInstanceMainConfig().getBoolean("securityPolicy.blockModules.punish", false);
	}
	
	@Override
	public boolean canPolicyUnload() {
		return !GoldenApple.getInstanceMainConfig().getBoolean("securityPolicy.blockManualUnload.punish", false);
	}

}
