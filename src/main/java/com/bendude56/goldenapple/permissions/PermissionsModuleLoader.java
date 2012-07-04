package com.bendude56.goldenapple.permissions;

import org.bukkit.Bukkit;

import com.bendude56.goldenapple.GoldenApple;
import com.bendude56.goldenapple.IModuleLoader;
import com.bendude56.goldenapple.commands.PermissionsCommand;
import com.bendude56.goldenapple.listener.PermissionListener;

public class PermissionsModuleLoader implements IModuleLoader {
	
	private static ModuleState state = ModuleState.UNLOADED_USER;

	@Override
	public void loadModule(PermissionManager permissions) {
		state = ModuleState.LOADING;
		try {
			permissions = GoldenApple.getInstance().permissions = new PermissionManager();
			registerPermissions(permissions);
			registerEvents();
			registerCommands();
		} catch (Throwable e) {
			state = ModuleState.UNLOADED_ERROR;
			GoldenApple.getInstance().permissions = null;
			// TODO Add cleanup code to clean up after failed module start
		}
	}
	
	private void registerPermissions(PermissionManager permissions) {
		PermissionManager.goldenAppleNode = permissions.registerNode("goldenapple", permissions.getRootNode());
		PermissionManager.permissionNode = permissions.registerNode("permissions", PermissionManager.goldenAppleNode);
	}
	
	private void registerEvents() {
		PermissionListener.startListening();
	}
	
	private void registerCommands() {
		Bukkit.getPluginCommand("gapermissions").setExecutor(new PermissionsCommand());
	}

	@Override
	public void unloadModule() {
		PermissionListener.stopListening();
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

}
