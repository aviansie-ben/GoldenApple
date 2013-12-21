package com.bendude56.goldenapple;

import com.bendude56.goldenapple.permissions.PermissionManager;

/**
 * Interface for module loaders, which are classes designed to load GoldenApple
 * modules into memory, register permissions and commands, and perform
 * other module start-up tasks.
 * 
 * @author ben_dude56
 */
public abstract class ModuleLoader {
	private final String name;
	private final String[] dependencies;
	private final String configAutoStart;
	private final String configDenyStart;
	private final String configDenyStop;
	
	private ModuleState state;
	
	public ModuleLoader(String name, String[] dependencies, String configAutoStart, String configDenyStart, String configDenyStop) {
		this.name = name;
		this.dependencies = dependencies;
		this.configAutoStart = configAutoStart;
		this.configDenyStart = configDenyStart;
		this.configDenyStop = configDenyStop;
		
		this.state = ModuleState.UNLOADED_USER;
	}
	
	protected abstract void preregisterCommands(CommandManager commands);
	
	protected abstract void registerPermissions(PermissionManager permissions);
	protected abstract void registerCommands(CommandManager commands);
	protected abstract void registerListener();
	protected abstract void initializeManager();

	protected abstract void destroyManager();
	protected abstract void unregisterListener();
	protected abstract void unregisterCommands(CommandManager commands);
	protected abstract void unregisterPermissions(PermissionManager permissions);
	
	private final void crashCleanup(int stage, GoldenApple instance) {
		if (stage >= 4) {
			try {
				unregisterListener();
			} catch (Exception e) { }
		}
		
		if (stage >= 3) {
			try {
				unregisterCommands(instance.getCommandManager());
			} catch (Exception e) { }
		}
		
		if (stage >= 2) {
			try {
				unregisterPermissions(PermissionManager.getInstance());
			} catch (Exception e) { }
		}
		
		if (stage >= 1) {
			try {
				destroyManager();
			} catch (Exception e) { }
		}
	}
	
	public final void loadModule(GoldenApple instance) throws ModuleLoadException {
		state = ModuleState.LOADING;
		
		try {
			initializeManager();
		} catch (ModuleLoadException e) {
			crashCleanup(1, instance);
			state = ModuleState.UNLOADED_ERROR;
			throw e;
		} catch (Exception e) {
			crashCleanup(1, instance);
			state = ModuleState.UNLOADED_ERROR;
			throw new ModuleLoadException(name, "Unhandled exception during manager initialization: " + e.getMessage(), e);
		}
		
		try {
			registerPermissions(PermissionManager.getInstance());
		} catch (ModuleLoadException e) {
			crashCleanup(2, instance);
			state = ModuleState.UNLOADED_ERROR;
			throw e;
		} catch (Exception e) {
			crashCleanup(2, instance);
			state = ModuleState.UNLOADED_ERROR;
			throw new ModuleLoadException(name, "Unhandled exception during permission registration: " + e.getMessage(), e);
		}
		
		try {
			registerCommands(instance.getCommandManager());
		} catch (ModuleLoadException e) {
			crashCleanup(3, instance);
			state = ModuleState.UNLOADED_ERROR;
			throw e;
		} catch (Exception e) {
			crashCleanup(3, instance);
			state = ModuleState.UNLOADED_ERROR;
			throw new ModuleLoadException(name, "Unhandled exception during command registration: " + e.getMessage(), e);
		}
		
		try {
			registerListener();
		} catch (ModuleLoadException e) {
			crashCleanup(4, instance);
			state = ModuleState.UNLOADED_ERROR;
			throw e;
		} catch (Exception e) {
			crashCleanup(4, instance);
			state = ModuleState.UNLOADED_ERROR;
			throw new ModuleLoadException(name, "Unhandled exception during listener registration: " + e.getMessage(), e);
		}
		
		state = ModuleState.LOADED;
	}
	
	public final void unloadModule(GoldenApple instance) {
		state = ModuleState.UNLOADING;
		
		try {
			unregisterListener();
		} catch (Exception e) { }
		
		try {
			unregisterCommands(instance.getCommandManager());
		} catch (Exception e) { }
		
		try {
			unregisterPermissions(PermissionManager.getInstance());
		} catch (Exception e) { }
		
		try {
			destroyManager();
		} catch (Exception e) { }
		
		state = ModuleState.UNLOADED_USER;
	}

	/**
	 * Gets the name of the module that this loader is designed to load into
	 * memory.
	 * 
	 * @return The name of the module.
	 */
	public final String getModuleName() {
		return name;
	}

	/**
	 * Gets a {@link ModuleState} representing the current state of the module
	 * loaded by this loader.
	 * 
	 * @return The current {@link ModuleState} of the module.
	 */
	public final ModuleState getCurrentState() {
		return state;
	}

	public final void setBusy(boolean busy) {
		if (state != ModuleState.LOADED && state != ModuleState.BUSY)
			throw new IllegalStateException("Module must be loaded successfully to set its busy state");
		
		state = (busy) ? ModuleState.BUSY : ModuleState.LOADED;
	}
	
	protected final void forceSetState(ModuleState state) {
		this.state = state;
	}

	/**
	 * Gets a list of modules that this module depends on. The module will not
	 * be started if one or more of its dependencies have not been enabled.
	 * 
	 * @return	String array containing names of other modules that this one
	 * 			depends on.
	 */
	public final String[] getModuleDependencies() {
		return dependencies;
	}

	/**
	 * Gets a value indicating whether this module is set to be loaded
	 * automatically.
	 * 
	 * @return	True if the module will be automatically loaded, false
	 * 			if the module must be manually loaded.
	 */
	public final boolean canLoadAuto() {
		if (configAutoStart == null) {
			return true;
		} else {
			return GoldenApple.getInstanceMainConfig().getBoolean(configAutoStart);
		}
	}

	/**
	 * Gets a value indicating whether the security policy allows this module to
	 * be loaded.
	 * 
	 * @return	True if GoldenApple's current security policy will allow this
	 * 			module to load, false if the security policy prevents this
	 * 			module from loading. 
	 */
	public final boolean canPolicyLoad() {
		if (configDenyStart == null) {
			return true;
		} else {
			return !GoldenApple.getInstanceMainConfig().getBoolean(configDenyStart);
		}
	}

	/**
	 * Gets a value indicating whether the security policy allows this module to
	 * be unloaded manually
	 */
	public final boolean canPolicyUnload() {
		if (configDenyStop == null) {
			return false;
		} else {
			return !GoldenApple.getInstanceMainConfig().getBoolean(configDenyStop);
		}
	}

	/**
	 * Represents the state of a module at a given moment in time.
	 * 
	 * @author ben_dude56
	 */
	public enum ModuleState {
		/**
		 * The module is performing a background operation that is stopping
		 * users from accessing one or more features.
		 */
		BUSY,
		/**
		 * The module has been successfully loaded into memory and is currently
		 * working as intended.
		 */
		LOADED,
		/**
		 * The module is in the process of loading itself into memory and its
		 * functions are not yet ready for use.
		 */
		LOADING,
		/**
		 * The module is in the process of unloading itself and its functions
		 * are in the process of being removed.
		 */
		UNLOADING,
		/**
		 * The module has been unloaded at the request of the user, or the
		 * configuration file specifies not to load this specific module.
		 */
		UNLOADED_USER,
		/**
		 * The module has been unloaded at its own request due to an error
		 * during or after loading.
		 */
		UNLOADED_ERROR,
		/**
		 * The module attempted to load, but was missing one or more module
		 * dependencies and failed to load.
		 */
		UNLOADED_MISSING_DEPENDENCY
	}
}
