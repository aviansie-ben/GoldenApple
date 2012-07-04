package com.bendude56.goldenapple;

import org.bukkit.Bukkit;

import com.bendude56.goldenapple.commands.VerifyCommand;
import com.bendude56.goldenapple.permissions.PermissionManager;

public class BaseModuleLoader implements IModuleLoader {
	
	private static ModuleState state = ModuleState.UNLOADED_USER;

	@Override
	public void loadModule(PermissionManager permissions) {
		state = ModuleState.LOADING;
		try {
			registerCommands();
		} catch (Throwable e) {
			state = ModuleState.UNLOADED_ERROR;
			// TODO Add cleanup code to clean up after failed module start
		}
		state = ModuleState.LOADED;
	}
	
	private void registerCommands() {
		Bukkit.getPluginCommand("gaverify").setExecutor(new VerifyCommand());
	}

	@Override
	public void unloadModule() {
		// TODO Add unloading code
	}

	@Override
	public String getModuleName() {
		return "Base";
	}

	@Override
	public ModuleState getCurrentState() {
		return state;
	}

	@Override
	public String[] getModuleDependencies() {
		return new String[0];
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
