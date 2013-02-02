package com.bendude56.goldenapple.warp;

import org.bukkit.Bukkit;

import com.bendude56.goldenapple.GoldenApple;
import com.bendude56.goldenapple.IModuleLoader;
import com.bendude56.goldenapple.commands.BackCommand;
import com.bendude56.goldenapple.commands.SpawnCommand;
import com.bendude56.goldenapple.commands.TpCommand;
import com.bendude56.goldenapple.commands.TpHereCommand;
import com.bendude56.goldenapple.listener.WarpListener;
import com.bendude56.goldenapple.permissions.PermissionManager;
import com.bendude56.goldenapple.permissions.PermissionManager.Permission;
import com.bendude56.goldenapple.permissions.PermissionManager.PermissionNode;

public class WarpModuleLoader implements IModuleLoader {
	
	// goldenapple.warp
	public static PermissionNode warpNode;
	public static Permission backPermission;
	
	// goldenapple.tp
	public static PermissionNode tpNode;
	public static Permission tpSelfToOtherPermission;
	public static Permission tpOtherToSelfPermission;
	public static Permission tpOtherToOtherPermission;
	
	// goldenapple.spawn
	public static PermissionNode spawnNode;
	public static Permission spawnCurrentPermission;
	public static Permission spawnAllPermission;

	private static ModuleState	state	= ModuleState.UNLOADED_USER;

	@Override
	public void loadModule(GoldenApple instance) {
		state = ModuleState.LOADING;
		try {
			registerCommands();
			registerPermissions(instance.permissions);
			WarpListener.startListening();
			state = ModuleState.LOADED;
		} catch (Throwable e) {
			state = ModuleState.UNLOADED_ERROR;
		}
	}

	private void registerCommands() {
		Bukkit.getPluginCommand("gaspawn").setExecutor(new SpawnCommand());
		Bukkit.getPluginCommand("gatp").setExecutor(new TpCommand());
		Bukkit.getPluginCommand("gatphere").setExecutor(new TpHereCommand());
		Bukkit.getPluginCommand("gaback").setExecutor(new BackCommand());
	}

	@Override
	public void registerPermissions(PermissionManager permissions) {
		warpNode = permissions.registerNode("warp", PermissionManager.goldenAppleNode);
		backPermission = permissions.registerPermission("back", warpNode);
		
		tpNode = permissions.registerNode("tp", warpNode);
		tpSelfToOtherPermission = permissions.registerPermission("selfToOther", tpNode);
		tpOtherToSelfPermission = permissions.registerPermission("otherToSelf", tpNode);
		tpOtherToOtherPermission = permissions.registerPermission("otherToOther", tpNode);
		
		spawnNode = permissions.registerNode("spawn", warpNode);
		spawnCurrentPermission = permissions.registerPermission("current", spawnNode);
		spawnAllPermission = permissions.registerPermission("all", spawnNode);
	}

	@Override
	public void unloadModule(GoldenApple instance) {
		Bukkit.getPluginCommand("gaspawn").setExecutor(GoldenApple.defCmd);
		Bukkit.getPluginCommand("gatp").setExecutor(GoldenApple.defCmd);
		Bukkit.getPluginCommand("gatphere").setExecutor(GoldenApple.defCmd);
		Bukkit.getPluginCommand("gaback").setExecutor(GoldenApple.defCmd);
		WarpListener.stopListening();
		state = ModuleState.UNLOADED_USER;
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
