package com.bendude56.goldenapple;

import com.bendude56.goldenapple.audit.AuditLog;
import com.bendude56.goldenapple.permissions.PermissionManager;

public class BaseModuleLoader implements IModuleLoader {

	private static ModuleState	state	= ModuleState.UNLOADED_USER;

	@Override
	public void loadModule(GoldenApple instance) {
		state = ModuleState.LOADING;
		try {
			AuditLog.initAuditLog();
			registerCommands(instance.commands);
			state = ModuleState.LOADED;
		} catch (Throwable e) {
			// This module should NEVER fail to load! This is a major problem.
			throw new ModuleLoadException("Base", e);
		}
	}

	private void registerCommands(CommandManager commands) {
		commands.getCommand("gaverify").register();
		commands.getCommand("gamodule").register();
		commands.getCommand("gacomplex").register();
		commands.getCommand("gaimport").register();
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
