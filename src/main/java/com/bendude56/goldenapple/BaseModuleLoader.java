package com.bendude56.goldenapple;

import org.bukkit.Bukkit;

import com.bendude56.goldenapple.commands.ComplexCommand;
import com.bendude56.goldenapple.commands.ModuleCommand;
import com.bendude56.goldenapple.commands.VerifyCommand;
import com.bendude56.goldenapple.permissions.PermissionManager;

public class BaseModuleLoader implements IModuleLoader {

	private static ModuleState	state	= ModuleState.UNLOADED_USER;

	@Override
	public void loadModule(GoldenApple instance) {
		state = ModuleState.LOADING;
		try {
			registerCommands();
			state = ModuleState.LOADED;
		} catch (Throwable e) {
			state = ModuleState.UNLOADED_ERROR;
			// TODO Add cleanup code to clean up after failed module start
		}
	}

	private void registerCommands() {
		Bukkit.getPluginCommand("gaverify").setExecutor(new VerifyCommand());
		Bukkit.getPluginCommand("gamodule").setExecutor(new ModuleCommand());
		Bukkit.getPluginCommand("gacomplex").setExecutor(new ComplexCommand());
	}
	
	@Override
	public void registerPermissions(PermissionManager permissions) {
		// Do nothing
	}

	@Override
	public void unloadModule(GoldenApple instance) {
		// This module should never be unloaded during normal operation
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
	public void setState(ModuleState state) {
		BaseModuleLoader.state = state;
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
