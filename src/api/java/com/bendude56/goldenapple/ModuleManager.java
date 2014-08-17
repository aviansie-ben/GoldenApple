package com.bendude56.goldenapple;

import java.util.List;

/**
 * Interface for the ModuleManager. A ModuleManager's job is to maintain a list
 * of GoldenApple modules represented by {@link ModuleLoader}s and see that
 * modules are loaded and unloaded successfully and in the correct order.
 * Contains methods to register/unregister modules, as well as enable/disable
 * individual modules.
 * 
 * @author ben_dude56
 * 
 */
public interface ModuleManager {
    /**
     * Registers a module with the {@link ModuleManager}.
     * 
     * @param module The module to register.
     */
    void registerModule(ModuleLoader module);
    /**
     * Unregisters a module of the given name from the manager.
     * 
     * @param moduleName The name of the module to unregister.
     */
    void unregisterModule(String moduleName);
    
    /**
     * Gets a list of {@link ModuleLoader}s currently registered with this
     * manager.
     * 
     * @return ArrayList of {@link ModuleLoader}s registered with this manager.
     */
    List<ModuleLoader> getModules();
    /**
     * Searches through the registered modules for the module with a name
     * matching the one provided.
     * 
     * @param moduleName The name of the module to search for.
     * 
     * @return The {@link ModuleLoader} with the name matching the parameter or
     * null if no match is found.
     */
    ModuleLoader getModule(String moduleName);
    
    /**
     * Enables a registered module with the option to include any dependencies
     * that module may have.
     * 
     * @param moduleName The name of the module to load.
     * @param loadDependencies Whether or not to load dependencies as well. True
     * will load any of the module's unloaded dependencies, false will ignore
     * any dependencies and only load the module.
     * @return True if the module was enabled successfully, false if the module
     * did not load successfully, or if no module with the given name is
     * registered with the ModuleManager.
     */
    boolean enableModule(String moduleName, boolean loadDependencies, String authorizingUser);
    
    /**
     * Disables a currently running module and unloads it from memory. Includes
     * the option to force unload a module even if any other modules depend on
     * it or if the module is not ready.
     * 
     * @param moduleName The name of the module to disable.
     * @param forceUnload Disables the module regardless of any other dependent
     * modules. True will ignore dependent modules , false will only disable the
     * modules of no other modules depend on it.
     * @return True if the module was successfully disabled, false if it wasn't
     * successfully disabled or no registered module matches the given name.
     */
    boolean disableModule(String moduleName, boolean forceUnload, String authorizingUser);
    
    /**
     * Enables a registered module with the option to include any dependencies
     * that module may have.
     * 
     * @param moduleName The name of the module to load.
     * @param loadDependencies Whether or not to load dependencies as well. True
     * will load any of the module's unloaded dependencies, false will ignore
     * any dependencies and only load the module.
     * @return True if the module was enabled successfully, false if the module
     * did not load successfully, or if no module with the given name is
     * registered with the ModuleManager.
     * 
     * @deprecated An authorizing user should be specified when
     * enabling/disabling a module
     */
    @Deprecated
    boolean enableModule(String moduleName, boolean loadDependencies);
    
    /**
     * Disables a currently running module and unloads it from memory. Includes
     * the option to force unload a module even if any other modules depend on
     * it or if the module is not ready.
     * 
     * @param moduleName The name of the module to disable.
     * @param forceUnload Disables the module regardless of any other dependent
     * modules. True will ignore dependent modules , false will only disable the
     * modules of no other modules depend on it.
     * @return True if the module was successfully disabled, false if it wasn't
     * successfully disabled or no registered module matches the given name.
     * 
     * @deprecated An authorizing user should be specified when
     * enabling/disabling a module
     */
    @Deprecated
    boolean disableModule(String moduleName, boolean forceUnload);
}
