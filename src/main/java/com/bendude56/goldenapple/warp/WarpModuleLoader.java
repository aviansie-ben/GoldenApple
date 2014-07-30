package com.bendude56.goldenapple.warp;

import com.bendude56.goldenapple.CommandManager;
import com.bendude56.goldenapple.ModuleLoader;
import com.bendude56.goldenapple.User;
import com.bendude56.goldenapple.permissions.PermissionManager;
import com.bendude56.goldenapple.warp.command.BackCommand;
import com.bendude56.goldenapple.warp.command.DelHomeCommand;
import com.bendude56.goldenapple.warp.command.DelWarpCommand;
import com.bendude56.goldenapple.warp.command.HomeCommand;
import com.bendude56.goldenapple.warp.command.SetHomeCommand;
import com.bendude56.goldenapple.warp.command.SetWarpCommand;
import com.bendude56.goldenapple.warp.command.SpawnCommand;
import com.bendude56.goldenapple.warp.command.TpCommand;
import com.bendude56.goldenapple.warp.command.TpHereCommand;
import com.bendude56.goldenapple.warp.command.WarpCommand;

public class WarpModuleLoader extends ModuleLoader {
	
	public WarpModuleLoader() {
		super("Warp", new String[] { "Permissions" }, "modules.warps.enabled", "securityPolicy.blockModules.warp", "securityPolicy.blockManualUnload.warp");
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
	public void preregisterPermissions() {
	    WarpManager.warpNode = PermissionManager.goldenAppleNode.createNode("warp");
	    WarpManager.backPermission = WarpManager.warpNode.createPermission("back");
	    WarpManager.editPermission = WarpManager.warpNode.createPermission("edit");
	    WarpManager.warpPermission = WarpManager.warpNode.createPermission("warp");
	    WarpManager.warpOtherPermission = WarpManager.warpNode.createPermission("warpOther");
	    WarpManager.overrideCooldownPermission = WarpManager.warpNode.createPermission("overrideCooldown");
	    
	    WarpManager.tpNode = WarpManager.warpNode.createNode("tp");
	    
	    WarpManager.tpSelfNode = WarpManager.tpNode.createNode("self");
	    WarpManager.tpSelfToCoordPermission = WarpManager.tpSelfNode.createPermission("coord");
	    WarpManager.tpSelfToPlayerPermission = WarpManager.tpSelfNode.createPermission("player");
	    
	    WarpManager.tpOtherNode = WarpManager.tpNode.createNode("other");
	    WarpManager.tpOtherToCoordPermission = WarpManager.tpOtherNode.createPermission("coord");
	    WarpManager.tpOtherToSelfPermission = WarpManager.tpOtherNode.createPermission("self");
	    WarpManager.tpOtherToPlayerPermission = WarpManager.tpOtherNode.createPermission("player");
	    
	    WarpManager.spawnNode = WarpManager.warpNode.createNode("spawn");
	    WarpManager.spawnCurrentPermission = WarpManager.spawnNode.createPermission("current");
	    WarpManager.spawnAllPermission = WarpManager.spawnNode.createPermission("all");
	    
	    WarpManager.homeNode = WarpManager.warpNode.createNode("home");
	    
	    WarpManager.homeTpNode = WarpManager.homeNode.createNode("tp");
	    WarpManager.homeTpOwn = WarpManager.homeTpNode.createPermission("own");
	    WarpManager.homeTpPublic = WarpManager.homeTpNode.createPermission("public");
	    WarpManager.homeTpAll = WarpManager.homeTpNode.createPermission("all");
	    
	    WarpManager.homeEditNode = WarpManager.homeNode.createNode("edit");
	    WarpManager.homeEditOwn = WarpManager.homeEditNode.createPermission("own");
	    WarpManager.homeEditPublic = WarpManager.homeEditNode.createPermission("public");
	    WarpManager.homeEditAll = WarpManager.homeEditNode.createPermission("all");
	    
	    WarpManager.tpNode.createPermissionAlias("selfToCoord", WarpManager.tpSelfToCoordPermission);
	    WarpManager.tpNode.createPermissionAlias("selfToOther", WarpManager.tpSelfToPlayerPermission);
	    WarpManager.tpNode.createPermissionAlias("otherToCoord", WarpManager.tpOtherToCoordPermission);
	    WarpManager.tpNode.createPermissionAlias("otherToSelf", WarpManager.tpOtherToSelfPermission);
	    WarpManager.tpNode.createPermissionAlias("otherToOther", WarpManager.tpOtherToPlayerPermission);
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
		
		User.setGlobalNegative("bukkit.command.teleport");
        
        User.setGlobalNegative("minecraft.command.tp");
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
	public void clearCache() {
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
		
		User.unsetGlobalNegative("bukkit.command.teleport");
        
        User.unsetGlobalNegative("minecraft.command.tp");
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
