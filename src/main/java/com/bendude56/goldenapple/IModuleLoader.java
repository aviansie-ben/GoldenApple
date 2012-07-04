package com.bendude56.goldenapple;

public interface IModuleLoader {
	/**
	 * Loads the GoldenApple module into memory and prepares it for use. Should
	 * register any and all necessary events, permissions, etc.
	 * 
	 * @param permissions The currently loaded permissions module manager. May
	 *            be null if the permissions module has not yet been loaded. If
	 *            the module loader relies on this value being non-null, the
	 *            permissions module should be included in the dependency list.
	 */
	public void loadModule(GoldenApple instance) throws ModuleLoadException;

	/**
	 * Unloads the GoldenApple module from memory and dumps any unsaved
	 * information into the database.
	 */
	public void unloadModule();

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
	 * Represents the state of a module at a given moment in time.
	 * 
	 * @author ben_dude56
	 */
	public enum ModuleState {
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
