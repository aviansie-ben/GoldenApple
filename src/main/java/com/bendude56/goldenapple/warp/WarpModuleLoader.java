package com.bendude56.goldenapple.warp;

import com.bendude56.goldenapple.CommandManager;
import com.bendude56.goldenapple.ModuleLoader;
import com.bendude56.goldenapple.commands.BackCommand;
import com.bendude56.goldenapple.commands.DelHomeCommand;
import com.bendude56.goldenapple.commands.DelWarpCommand;
import com.bendude56.goldenapple.commands.HomeCommand;
import com.bendude56.goldenapple.commands.SetHomeCommand;
import com.bendude56.goldenapple.commands.SetWarpCommand;
import com.bendude56.goldenapple.commands.SpawnCommand;
import com.bendude56.goldenapple.commands.TpCommand;
import com.bendude56.goldenapple.commands.TpHereCommand;
import com.bendude56.goldenapple.commands.WarpCommand;
import com.bendude56.goldenapple.listener.WarpListener;
import com.bendude56.goldenapple.permissions.PermissionManager;

public class WarpModuleLoader extends ModuleLoader {
	
	public WarpModuleLoader() {
		super("Warp", new String[] { "Permissions" }, "modules.warp.enabled", "securityPolicy.blockModules.warp", "securityPolicy.blockManualUnload.warp");
	}
	
	@Override
	protected void preregisterCommands(CommandManager commands) {
		commands.insertCommand("gahome", "Warp", new HomeCommand());
		commands.insertCommand("gasethome", "Warp", new SetHomeCommand());
		commands.insertCommand("gadelhome", "Warp", new DelHomeCommand());
		commands.insertCommand("gaspawn", "Warp", new SpawnCommand());
		commands.insertCommand("gaback" ,"Warp", new BackCommand());
		commands.insertCommand("gatp", "Warp", new TpCommand());
		commands.insertCommand("gatphere", "Warp", new TpHereCommand());
		commands.insertCommand("gawarp", "Warp", new WarpCommand());
		commands.insertCommand("gasetwarp", "Warp", new SetWarpCommand());
		commands.insertCommand("gadelwarp", "Warp", new DelWarpCommand());
	}
	
	@Override
	protected void registerPermissions(PermissionManager permissions) {
		WarpManager.warpNode = permissions.registerNode("warp", PermissionManager.goldenAppleNode);
		WarpManager.backPermission = permissions.registerPermission("back", WarpManager.warpNode);
		WarpManager.editPermission = permissions.registerPermission("edit", WarpManager.warpNode);
		
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
	protected void registerCommands(CommandManager commands) {
		commands.getCommand("gaspawn").register();
		commands.getCommand("gatp").register();
		commands.getCommand("gatphere").register();
		commands.getCommand("gaback").register();
		commands.getCommand("gahome").register();
		commands.getCommand("gasethome").register();
		commands.getCommand("gadelhome").register();
		commands.getCommand("gawarp").register();
		commands.getCommand("gasetwarp").register();
		commands.getCommand("gadelwarp").register();
	}
	
	@Override
	protected void registerListener() {
		WarpListener.startListening();
	}
	
	@Override
	protected void initializeManager() {
		WarpManager.instance = new SimpleWarpManager();
	}
	
	@Override
	protected void unregisterPermissions(PermissionManager permissions) {
		WarpManager.warpNode = null;
		WarpManager.backPermission = null;
		WarpManager.editPermission = null;
		
		WarpManager.tpNode = null;
		WarpManager.tpSelfToOtherPermission = null;
		WarpManager.tpOtherToSelfPermission = null;
		WarpManager.tpOtherToOtherPermission = null;
		
		WarpManager.spawnNode = null;
		WarpManager.spawnCurrentPermission = null;
		WarpManager.spawnAllPermission = null;
		
		WarpManager.homeNode = null;
		
		WarpManager.homeTpNode = null;
		WarpManager.homeTpOwn = null;
		WarpManager.homeTpPublic = null;
		WarpManager.homeTpAll = null;
		
		WarpManager.homeEditNode = null;
		WarpManager.homeEditOwn = null;
		WarpManager.homeEditPublic = null;
		WarpManager.homeEditAll = null;
	}
	
	@Override
	protected void unregisterCommands(CommandManager commands) {
		commands.getCommand("gaspawn").unregister();
		commands.getCommand("gatp").unregister();
		commands.getCommand("gatphere").unregister();
		commands.getCommand("gaback").unregister();
		commands.getCommand("gahome").unregister();
		commands.getCommand("gasethome").unregister();
		commands.getCommand("gadelhome").unregister();
		commands.getCommand("gawarp").unregister();
		commands.getCommand("gasetwarp").unregister();
		commands.getCommand("gadelwarp").unregister();
	}
	
	@Override
	protected void unregisterListener() {
		WarpListener.stopListening();
	}
	
	@Override
	protected void destroyManager() {
		WarpManager.instance = null;
	}

}
