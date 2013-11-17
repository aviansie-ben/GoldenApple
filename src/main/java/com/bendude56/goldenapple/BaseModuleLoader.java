package com.bendude56.goldenapple;

import com.bendude56.goldenapple.audit.AuditLog;
import com.bendude56.goldenapple.permissions.PermissionManager;

/**
 * Class that implements the {@link ModuleLoader} interface to minimal extend,
 * designed to be extended on a per-module basis.
 * 
 * @author ben_dude56
 */
public class BaseModuleLoader implements ModuleLoader {
	
	/**
	 * The current state of the module.
	 */
	private static ModuleState	state	= ModuleState.UNLOADED_USER;

	@Override
	public void loadModule(GoldenApple instance) {
		// Set the module's state to "loading" until loading is complete.
		state = ModuleState.LOADING;
		try {
			// Initialize the audit log
			AuditLog.initAuditLog();
			// Register the module's commands
			registerCommands(instance.getCommandManager());
			// Operation is complete; set module state to "loaded"
			state = ModuleState.LOADED;
		} catch (Throwable e) {
			// This module should NEVER fail to load! This is a major problem.
			// I dare not think of the consequences for failure.
			throw new ModuleLoadException("Base", e);
		}
	}
	
	/**
	 * Registers this module's commands with Bukkit.
	 * 
	 * @param 	commands The {@link CommandManager} that will handle incoming
	 * 			commands.
	 */
	private void registerCommands(CommandManager commands) {
		commands.getCommand("gaverify").register();
		commands.getCommand("gamodule").register();
		commands.getCommand("gacomplex").register();
		commands.getCommand("gaimport").register();
	}

	@Override
	public void registerPermissions(PermissionManager permissions) {
		// Do nothing since the base module has no permissions to register.
	}

	@Override
	public void unloadModule(GoldenApple instance) {
		// This module should only be unloaded when GoldenApple is shutting down
		
		// Deinitialize the audit log
		AuditLog.deinitAuditLog();
		// Set state to "unloaded by user"
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
		// The base module has no dependencies, thus return an empty array
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
