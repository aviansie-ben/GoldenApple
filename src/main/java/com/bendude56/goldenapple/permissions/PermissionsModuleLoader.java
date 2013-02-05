package com.bendude56.goldenapple.permissions;

import com.bendude56.goldenapple.CommandManager;
import com.bendude56.goldenapple.GoldenApple;
import com.bendude56.goldenapple.IModuleLoader;
import com.bendude56.goldenapple.ModuleLoadException;
import com.bendude56.goldenapple.User;
import com.bendude56.goldenapple.listener.PermissionListener;

public class PermissionsModuleLoader implements IModuleLoader {
	
	private static ModuleState state = ModuleState.UNLOADED_USER;

	@Override
	public void loadModule(GoldenApple instance) {
		state = ModuleState.LOADING;
		try {
			instance.permissions = new PermissionManager();
			User.clearCache();
			registerPermissions(instance.permissions);
			registerEvents();
			registerCommands(instance.commands);
			state = ModuleState.LOADED;
		} catch (Throwable e) {
			// This module should NEVER fail to load! This is a major problem.
			throw new ModuleLoadException("Permissions", e);
		}
	}
	
	@Override
	public void registerPermissions(PermissionManager permissions) {
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
		PermissionManager.moduleQueryPermission = permissions.registerPermission("query", PermissionManager.moduleNode);
	}
	
	private void registerEvents() {
		PermissionListener.startListening();
	}
	
	private void registerCommands(CommandManager commands) {
		commands.getCommand("gapermissions").register();
		commands.getCommand("gaown").register();
	}

	@Override
	public void unloadModule(GoldenApple instance) {
		unregisterEvents();
		unregisterCommands(instance.commands);
		User.clearCache();
		GoldenApple.getInstance().permissions.close();
		GoldenApple.getInstance().permissions = null;
		state = ModuleState.UNLOADED_USER;
	}
	
	private void unregisterEvents() {
		PermissionListener.stopListening();
	}
	
	private void unregisterCommands(CommandManager commands) {
		commands.getCommand("gapermissions").unregister();
		commands.getCommand("gaown").unregister();
	}

	@Override
	public String getModuleName() {
		return "Permissions";
	}

	@Override
	public ModuleState getCurrentState() {
		return state;
	}
	
	@Override
	public void setState(ModuleState state) {
		PermissionsModuleLoader.state = state;
	}

	@Override
	public String[] getModuleDependencies() {
		return new String[] { "Base" };
	}

	@Override
	public boolean canLoadAuto() {
		return true;
	}

	@Override
	public boolean canPolicyLoad() {
		return true;
	}
	
	@Override
	public boolean canPolicyUnload() {
		return !GoldenApple.getInstance().mainConfig.getBoolean("securityPolicy.blockManualUnload.permissions", true);
	}

}
