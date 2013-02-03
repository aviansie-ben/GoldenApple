package com.bendude56.goldenapple;

import org.bukkit.Bukkit;

import com.bendude56.goldenapple.audit.AuditLog;
import com.bendude56.goldenapple.commands.ComplexCommand;
import com.bendude56.goldenapple.commands.ImportCommand;
import com.bendude56.goldenapple.commands.ModuleCommand;
import com.bendude56.goldenapple.commands.VerifyCommand;
import com.bendude56.goldenapple.permissions.PermissionManager;

public class BaseModuleLoader implements IModuleLoader {

	private static ModuleState	state	= ModuleState.UNLOADED_USER;

	@Override
	public void loadModule(GoldenApple instance) {
		state = ModuleState.LOADING;
		try {
			AuditLog.initAuditLog();
			registerCommands();
			state = ModuleState.LOADED;
		} catch (Throwable e) {
			// This module should NEVER fail to load! This is a major problem.
			throw new ModuleLoadException("Base", e);
		}
	}

	private void registerCommands() {
		Bukkit.getPluginCommand("gaverify").setExecutor(new VerifyCommand());
		Bukkit.getPluginCommand("gamodule").setExecutor(new ModuleCommand());
		Bukkit.getPluginCommand("gacomplex").setExecutor(new ComplexCommand());
		Bukkit.getPluginCommand("gaimport").setExecutor(new ImportCommand());
	}

	@Override
	public void registerPermissions(PermissionManager permissions) {
		// Do nothing
	}

	@Override
	public void unloadModule(GoldenApple instance) {
		// This module should only be unloaded when GoldenApple is shutting down
		AuditLog.deinitAuditLog();
		state = ModuleState.UNLOADED_USER;
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
	
	@Override
	public boolean canPolicyUnload() {
		return false;
	}

}
