package com.bendude56.goldenapple.warp;

import com.bendude56.goldenapple.CommandManager;
import com.bendude56.goldenapple.GoldenApple;
import com.bendude56.goldenapple.IModuleLoader;
import com.bendude56.goldenapple.listener.WarpListener;
import com.bendude56.goldenapple.permissions.PermissionManager;

public class WarpModuleLoader implements IModuleLoader {

	private static ModuleState	state	= ModuleState.UNLOADED_USER;

	@Override
	public void loadModule(GoldenApple instance) {
		state = ModuleState.LOADING;
		try {
			GoldenApple.getInstance().warps = new WarpManager();
			
			registerCommands(instance.commands);
			registerPermissions(instance.permissions);
			WarpListener.startListening();
			state = ModuleState.LOADED;
		} catch (Throwable e) {
			state = ModuleState.UNLOADED_ERROR;
		}
	}

	private void registerCommands(CommandManager commands) {
		commands.getCommand("gaspawn").register();
		commands.getCommand("gatp").register();
		commands.getCommand("gatphere").register();
		commands.getCommand("gaback").register();
		commands.getCommand("gahome").register();
		commands.getCommand("gasethome").register();
		commands.getCommand("gadelhome").register();
	}

	@Override
	public void registerPermissions(PermissionManager permissions) {
		WarpManager.warpNode = permissions.registerNode("warp", PermissionManager.goldenAppleNode);
		WarpManager.backPermission = permissions.registerPermission("back", WarpManager.warpNode);
		
		WarpManager.tpNode = permissions.registerNode("tp", WarpManager.warpNode);
		WarpManager.tpSelfToOtherPermission = permissions.registerPermission("selfToOther", WarpManager.tpNode);
		WarpManager.tpOtherToSelfPermission = permissions.registerPermission("otherToSelf", WarpManager.tpNode);
		WarpManager.tpOtherToOtherPermission = permissions.registerPermission("otherToOther", WarpManager.tpNode);
		
		WarpManager.spawnNode = permissions.registerNode("spawn", WarpManager.warpNode);
		WarpManager.spawnCurrentPermission = permissions.registerPermission("current", WarpManager.spawnNode);
		WarpManager.spawnAllPermission = permissions.registerPermission("all", WarpManager.spawnNode);
		
		WarpManager.homeNode = permissions.registerNode("home", WarpManager.warpNode);
		
		WarpManager.homeTpNode = permissions.registerNode("tp", WarpManager.homeNode);
		WarpManager.homeTpOwn = permissions.registerPermission("own", WarpManager.homeTpNode);
		WarpManager.homeTpPublic = permissions.registerPermission("public", WarpManager.homeTpNode);
		WarpManager.homeTpAll = permissions.registerPermission("all", WarpManager.homeTpNode);
		
		WarpManager.homeEditNode = permissions.registerNode("edit", WarpManager.homeNode);
		WarpManager.homeEditOwn = permissions.registerPermission("own", WarpManager.homeEditNode);
		WarpManager.homeEditPublic = permissions.registerPermission("public", WarpManager.homeEditNode);
		WarpManager.homeEditAll = permissions.registerPermission("all", WarpManager.homeEditNode);
	}

	@Override
	public void unloadModule(GoldenApple instance) {
		unregisterCommands(instance.commands);
		WarpListener.stopListening();
		GoldenApple.getInstance().warps = null;
		state = ModuleState.UNLOADED_USER;
	}
	
	private void unregisterCommands(CommandManager commands) {
		commands.getCommand("gaspawn").unregister();
		commands.getCommand("gatp").unregister();
		commands.getCommand("gatphere").unregister();
		commands.getCommand("gaback").unregister();
		commands.getCommand("gahome").unregister();
		commands.getCommand("gasethome").unregister();
		commands.getCommand("gadelhome").unregister();
	}

	@Override
	public String getModuleName() {
		return "Warp";
	}

	@Override
	public ModuleState getCurrentState() {
		return state;
	}

	@Override
	public void setState(ModuleState state) {
		WarpModuleLoader.state = state;
	}

	@Override
	public String[] getModuleDependencies() {
		return new String[0];
	}

	@Override
	public boolean canLoadAuto() {
		return GoldenApple.getInstance().mainConfig.getBoolean("modules.warps.enabled", true);
	}

	@Override
	public boolean canPolicyLoad() {
		return !GoldenApple.getInstance().mainConfig.getBoolean("securityPolicy.blockModules.warp", true);
	}
	
	@Override
	public boolean canPolicyUnload() {
		return !GoldenApple.getInstance().mainConfig.getBoolean("securityPolicy.blockManualUnload.warp", false);
	}

}
