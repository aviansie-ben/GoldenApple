package com.bendude56.goldenapple.warp;

import org.bukkit.Bukkit;

import com.bendude56.goldenapple.GoldenApple;
import com.bendude56.goldenapple.IModuleLoader;
import com.bendude56.goldenapple.commands.BackCommand;
import com.bendude56.goldenapple.commands.DelHomeCommand;
import com.bendude56.goldenapple.commands.HomeCommand;
import com.bendude56.goldenapple.commands.SetHomeCommand;
import com.bendude56.goldenapple.commands.SpawnCommand;
import com.bendude56.goldenapple.commands.TpCommand;
import com.bendude56.goldenapple.commands.TpHereCommand;
import com.bendude56.goldenapple.listener.WarpListener;
import com.bendude56.goldenapple.permissions.PermissionManager;

public class WarpModuleLoader implements IModuleLoader {

	private static ModuleState	state	= ModuleState.UNLOADED_USER;

	@Override
	public void loadModule(GoldenApple instance) {
		state = ModuleState.LOADING;
		try {
			GoldenApple.getInstance().warps = new WarpManager();
			
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
		Bukkit.getPluginCommand("gahome").setExecutor(new HomeCommand());
		Bukkit.getPluginCommand("gasethome").setExecutor(new SetHomeCommand());
		Bukkit.getPluginCommand("gadelhome").setExecutor(new DelHomeCommand());
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
		GoldenApple.getInstance().warps = null;
		Bukkit.getPluginCommand("gaspawn").setExecutor(GoldenApple.defCmd);
		Bukkit.getPluginCommand("gatp").setExecutor(GoldenApple.defCmd);
		Bukkit.getPluginCommand("gatphere").setExecutor(GoldenApple.defCmd);
		Bukkit.getPluginCommand("gaback").setExecutor(GoldenApple.defCmd);
		Bukkit.getPluginCommand("gahome").setExecutor(GoldenApple.defCmd);
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
