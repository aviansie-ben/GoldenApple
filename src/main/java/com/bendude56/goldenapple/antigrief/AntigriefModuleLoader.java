package com.bendude56.goldenapple.antigrief;

import com.bendude56.goldenapple.CommandManager;
import com.bendude56.goldenapple.ModuleLoader;
import com.bendude56.goldenapple.listener.AntigriefListener;
import com.bendude56.goldenapple.permissions.PermissionManager;
import com.bendude56.goldenapple.permissions.PermissionManager.Permission;
import com.bendude56.goldenapple.permissions.PermissionManager.PermissionNode;

public class AntigriefModuleLoader extends ModuleLoader {
	public static PermissionNode antigriefNode;
	public static Permission tntPermission;
	
	public AntigriefModuleLoader() {
		super("Antigrief", new String[] { "Base" }, "modules.antigrief.enabled", "securityPolicy.blockModules.antigrief", "securityPolicy.blockManualUnload.antigrief");
	}
	
	@Override
	protected void preregisterCommands(CommandManager commands) {
	}
	
	@Override
	protected void registerPermissions(PermissionManager permissions) {
		antigriefNode = permissions.registerNode("antigrief", PermissionManager.goldenAppleNode);
		tntPermission = permissions.registerPermission("tnt", antigriefNode);
	}
	
	@Override
	protected void registerCommands(CommandManager commands) {
	}
	
	@Override
	protected void registerListener() {
		AntigriefListener.startListening();
	}
	
	@Override
	protected void unregisterPermissions(PermissionManager permissions) {
		antigriefNode = null;
		tntPermission = null;
	}
	
	@Override
	protected void initializeManager() {
	}
	
	@Override
	protected void unregisterCommands(CommandManager commands) {
	}
	
	@Override
	protected void unregisterListener() {
		AntigriefListener.stopListening();
	}
	
	@Override
	protected void destroyManager() {
	}
}
