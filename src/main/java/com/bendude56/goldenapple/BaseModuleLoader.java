package com.bendude56.goldenapple;

import org.bukkit.Bukkit;

import com.bendude56.goldenapple.commands.VerifyCommand;

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
	}

	@Override
	public void unloadModule() {
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
