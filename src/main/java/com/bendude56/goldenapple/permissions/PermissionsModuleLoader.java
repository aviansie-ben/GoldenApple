package com.bendude56.goldenapple.permissions;

import org.bukkit.Bukkit;

import com.bendude56.goldenapple.GoldenApple;
import com.bendude56.goldenapple.IModuleLoader;
import com.bendude56.goldenapple.User;
import com.bendude56.goldenapple.commands.OwnCommand;
import com.bendude56.goldenapple.commands.PermissionsCommand;
import com.bendude56.goldenapple.listener.PermissionListener;

public class PermissionsModuleLoader implements IModuleLoader {
	
	private static ModuleState state = ModuleState.UNLOADED_USER;

	@Override
	public void loadModule(GoldenApple instance) {
		state = ModuleState.LOADING;
		instance.permissions = new PermissionManager();
		User.clearCache();
		registerPermissions(instance.permissions);
		registerEvents();
		registerCommands();
		state = ModuleState.LOADED;
	}
	
	@Override
	public void registerPermissions(PermissionManager permissions) {
		PermissionManager.goldenAppleNode = permissions.registerNode("goldenapple", permissions.getRootNode());
		
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
	
	private void registerCommands() {
		Bukkit.getPluginCommand("gapermissions").setExecutor(new PermissionsCommand());
		Bukkit.getPluginCommand("gaown").setExecutor(new OwnCommand());
	}

	@Override
	public void unloadModule(GoldenApple instance) {
		PermissionListener.stopListening();
		User.clearCache();
		state = ModuleState.UNLOADED_USER;
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
		return false;
	}

	@Override
	public boolean canPolicyLoad() {
		return true;
	}

}
