package com.bendude56.goldenapple;

import com.bendude56.goldenapple.permissions.PermissionManager;

public interface IModuleLoader {
	/**
	 * Loads the GoldenApple module into memory and prepares it for use. Should
	 * register any and all necessary events, permissions, etc.
	 * 
	 * @param instance The GoldenApple instance that is currently loading this
	 *            module.
	 */
	public void loadModule(GoldenApple instance) throws ModuleLoadException;

	/**
	 * Registers module permissions. This will get called without a call to
	 * {@link IModuleLoader#loadModule(GoldenApple)} if the permissions system
	 * recovers from an error.
	 * 
	 * @param permissions The {@link PermissionManager} that is currently
	 *            controlling permissions.
	 */
	public void registerPermissions(PermissionManager permissions);

	/**
	 * Unloads the GoldenApple module from memory and dumps any unsaved
	 * information into the database.
	 */
	public void unloadModule(GoldenApple instance);

	/**
	 * Gets the name of the module that this loader is designed to load into
	 * memory.
	 */
	public String getModuleName();

	/**
	 * Gets a {@link ModuleState} representing the current state of the module
	 * loaded by this loader.
	 */
	public ModuleState getCurrentState();

	/**
	 * Sets the module's current state. Used when loading a module to report
	 * errors to administrators using /gamodule.
	 */
	public void setState(ModuleState state);

	/**
	 * Gets a list of modules that this module depends on. The module will not
	 * be started if one or more of these modules has not been enabled.
	 */
	public String[] getModuleDependencies();

	/**
	 * Gets a value indicating whether this module is set to be loaded
	 * automatically.
	 */
	public boolean canLoadAuto();

	/**
	 * Gets a value indicating whether the security policy allows this module to
	 * be loaded.
	 */
	public boolean canPolicyLoad();

	/**
	 * Gets a value indicating whether the security policy allows this module to
	 * be unloaded manually
	 */
	public boolean canPolicyUnload();

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
