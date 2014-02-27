package com.bendude56.goldenapple.permissions;

import com.bendude56.goldenapple.CommandManager;
import com.bendude56.goldenapple.ModuleLoader;
import com.bendude56.goldenapple.User;
import com.bendude56.goldenapple.commands.LangCommand;
import com.bendude56.goldenapple.commands.OwnCommand;
import com.bendude56.goldenapple.commands.PermissionsCommand;
import com.bendude56.goldenapple.listener.PermissionListener;

public class PermissionsModuleLoader extends ModuleLoader {
	
	public PermissionsModuleLoader() {
		super("Permissions", new String[] { "Base" }, null, null, "securityPolicy.blockManualUnload.permissions");
	}
	
	@Override
	protected void preregisterCommands(CommandManager commands) {
		commands.insertCommand("gapermissions", "Permissions", new PermissionsCommand());
		commands.insertCommand("gaown", "Permissions", new OwnCommand());
		commands.insertCommand("galang", "Permissions", new LangCommand());
	}
	
	@Override
	protected void registerPermissions(PermissionManager permissions) {
		PermissionManager.goldenAppleNode = permissions.registerNode("goldenapple", permissions.getRootNode());
		PermissionManager.importPermission = permissions.registerPermission("import", PermissionManager.goldenAppleNode);
		
		PermissionManager.permissionNode = permissions.registerNode("permissions", PermissionManager.goldenAppleNode);
		
		PermissionManager.userNode = permissions.registerNode("user", PermissionManager.permissionNode);
		PermissionManager.userAddPermission = permissions.registerPermission("add", PermissionManager.userNode);
		PermissionManager.userRemovePermission = permissions.registerPermission("remove", PermissionManager.userNode);
		PermissionManager.userEditPermission = permissions.registerPermission("edit", PermissionManager.userNode);
		
		PermissionManager.groupNode = permissions.registerNode("group", PermissionManager.permissionNode);
		PermissionManager.groupAddPermission = permissions.registerPermission("add", PermissionManager.groupNode);
		PermissionManager.groupRemovePermission = permissions.registerPermission("remove", PermissionManager.groupNode);
		PermissionManager.groupEditPermission = permissions.registerPermission("edit", PermissionManager.groupNode);
		
		PermissionManager.moduleNode = permissions.registerNode("module", PermissionManager.goldenAppleNode);
		PermissionManager.moduleLoadPermission = permissions.registerPermission("load", PermissionManager.moduleNode);
		PermissionManager.moduleUnloadPermission = permissions.registerPermission("unload", PermissionManager.moduleNode);
		PermissionManager.moduleClearCachePermission = permissions.registerPermission("clearCache", PermissionManager.moduleNode);
		PermissionManager.moduleQueryPermission = permissions.registerPermission("query", PermissionManager.moduleNode);
	}
	
	@Override
	protected void registerCommands(CommandManager commands) {
		commands.getCommand("gapermissions").register();
		commands.getCommand("gaown").register();
		commands.getCommand("galang").register();
	}
	
	@Override
	protected void registerListener() {
		PermissionListener.startListening();
	}
	
	@Override
	protected void initializeManager() {
		PermissionManager.instance = new SimplePermissionManager();
		
		((SimplePermissionManager)PermissionManager.instance).loadGroups();
		((SimplePermissionManager)PermissionManager.instance).checkDefaultGroups();
		
		User.clearCache();
	}
	
	@Override
	public void clearCache() {
		PermissionManager.getInstance().clearCache();
	}
	
	@Override
	protected void unregisterPermissions(PermissionManager permissions) {
		PermissionManager.goldenAppleNode = null;
		PermissionManager.importPermission = null;
		
		PermissionManager.permissionNode = null;
		
		PermissionManager.userNode = null;
		PermissionManager.userAddPermission = null;
		PermissionManager.userRemovePermission = null;
		PermissionManager.userEditPermission = null;
		
		PermissionManager.groupNode = null;
		PermissionManager.groupAddPermission = null;
		PermissionManager.groupRemovePermission = null;
		PermissionManager.groupEditPermission = null;
		
		PermissionManager.moduleNode = null;
		PermissionManager.moduleLoadPermission = null;
		PermissionManager.moduleUnloadPermission = null;
		PermissionManager.moduleQueryPermission = null;
	}
	
	@Override
	protected void unregisterCommands(CommandManager commands) {
		commands.getCommand("gapermissions").unregister();
		commands.getCommand("gaown").unregister();
		commands.getCommand("galang").unregister();
	}
	
	@Override
	protected void unregisterListener() {
		PermissionListener.stopListening();
	}
	
	@Override
	protected void destroyManager() {
		User.clearCache();
		PermissionManager.getInstance().close();
		PermissionManager.instance = null;
	}

}
