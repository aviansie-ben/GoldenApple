package com.bendude56.goldenapple;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectOutputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.logging.Level;

import com.bendude56.goldenapple.ModuleLoader.ModuleState;
import com.bendude56.goldenapple.antigrief.AntigriefModuleLoader;
import com.bendude56.goldenapple.area.AreaModuleLoader;
import com.bendude56.goldenapple.audit.AuditLog;
import com.bendude56.goldenapple.audit.ModuleDisableEvent;
import com.bendude56.goldenapple.audit.ModuleEnableEvent;
import com.bendude56.goldenapple.chat.ChatModuleLoader;
import com.bendude56.goldenapple.invisible.InvisibleModuleLoader;
import com.bendude56.goldenapple.lock.LockModuleLoader;
import com.bendude56.goldenapple.mail.MailModuleLoader;
import com.bendude56.goldenapple.permissions.PermissionsModuleLoader;
import com.bendude56.goldenapple.punish.PunishModuleLoader;
import com.bendude56.goldenapple.request.RequestModuleLoader;
import com.bendude56.goldenapple.select.SelectModuleLoader;
import com.bendude56.goldenapple.warp.WarpModuleLoader;

/**
 * Designed to provide an interface to manage and control GoldenApple's various
 * modules, this class will keep track of each module's {@link ModuleLoader} and
 * provide methods to enable/disable individual modules. It will also handle
 * module dependencies, crashes, and failures.
 * 
 * @author ben_dude56
 * 
 */
public class SimpleModuleManager implements ModuleManager {
    /**
     * Preprogrammed order in which to load the various GoldenApple modules.
     */
    private static final String[] loadOrder = new String[] {
        "Base", "Permissions", "Lock", "Antigrief", "Chat", "Warp", "Punish", "Request", "Invisible", "Select", "Area", "Mail" };
    
    /**
     * Hashmap to store registered {@ModuleLoader}s by module
     * name.
     */
    private HashMap<String, ModuleLoader> modules =
        new HashMap<String, ModuleLoader>();
    
    /**
     * Constructor for the class. Registers each GoldenApple module's
     * {@link ModuleLoader}.
     */
    public SimpleModuleManager() {
        registerModule(new BaseModuleLoader());
        registerModule(new PermissionsModuleLoader());
        registerModule(new LockModuleLoader());
        registerModule(new AntigriefModuleLoader());
        registerModule(new ChatModuleLoader());
        registerModule(new WarpModuleLoader());
        registerModule(new PunishModuleLoader());
        registerModule(new RequestModuleLoader());
        registerModule(new InvisibleModuleLoader());
        registerModule(new SelectModuleLoader());
        registerModule(new AreaModuleLoader());
        registerModule(new MailModuleLoader());
    }
    
    /**
     * Enables all modules configured to run on startup using GoldenApple's
     * configuration file.
     * 
     * @return True if no errors occurred and GoldenApple is not configured to
     * shutdown on error, false if the entire program should abort.
     */
    public boolean loadDefaults() {
        for (String m : loadOrder) {
            ModuleLoader module = modules.get(m);
            // Check if the module is supposed to be loaded automatically
            if (module != null && module.canLoadAuto() && module.canPolicyLoad() && module.getCurrentState() == ModuleState.UNLOADED_USER) {
                try {
                    // Try to enable the module
                    if (!enableModule(m, true, "GoldenApple Startup Automation")) {
                        if (GoldenApple.getInstanceMainConfig().getBoolean("securityPolicy.shutdownOnFailedModuleLoad", true)) {
                            return false;
                        }
                    }
                } catch (Exception e) {
                    // An error occurred while enabling the module
                    GoldenApple.log(Level.SEVERE, "An error occured while loading module " + m + ":");
                    GoldenApple.log(Level.SEVERE, e);
                    if (GoldenApple.getInstanceMainConfig().getBoolean("securityPolicy.shutdownOnFailedModuleLoad", true)) {
                        return false;
                    }
                }
            }
        }
        
        return true;
    }
    
    /**
     * Disables all registered modules and unloads them from memory.
     */
    public void unloadAll() {
        for (Map.Entry<String, ModuleLoader> module : modules.entrySet()) {
            if (module.getValue().getCurrentState() == ModuleState.LOADED) {
                disableModule(module.getKey(), true, "GoldenApple Shutdown Automation");
            }
        }
    }
    
    @Override
    public void registerModule(ModuleLoader module) {
        modules.put(module.getModuleName(), module);
        
        // Pre-register the module's commands
        module.preregisterCommands(GoldenApple.getInstance().getCommandManager());
    }
    
    @Override
    public void unregisterModule(String moduleName) {
        modules.remove(moduleName);
    }
    
    @Override
    public List<ModuleLoader> getModules() {
        List<ModuleLoader> modules = new ArrayList<ModuleLoader>();
        
        for (String module : loadOrder) {
            modules.add(this.modules.get(module));
        }
        
        for (Map.Entry<String, ModuleLoader> module : this.modules.entrySet()) {
            if (!modules.contains(module.getValue())) {
                modules.add(module.getValue());
            }
        }
        
        return modules;
    }
    
    @Override
    public ModuleLoader getModule(String moduleName) {
        return modules.get(moduleName);
    }
    
    /**
     * Performs all pre-enabling checks, making sure that all required
     * conditions are met before enabling the module. Will throw an
     * {@link IllegalStateException} if an error is encountered. If dependencies
     * are not enabled, and the "loadDependencies" parameter is set to true,
     * will enable all required dependencies.
     * 
     * @param module The module to enable.
     * @param loadDependancies If set to *true*, will enable all of the given
     * module's dependencies.
     */
    private void verifyModuleLoad(ModuleLoader module, boolean loadDependancies) {
        // Check if the module is already loaded or is currently loading
        if (module.getCurrentState() == ModuleLoader.ModuleState.LOADED || module.getCurrentState() == ModuleLoader.ModuleState.LOADING) {
            throw new IllegalStateException("0: Module '" + module.getModuleName() + "' already loaded");
        }
        // Check if the security policy will permit the module to load
        else if (!module.canPolicyLoad()) {
            throw new IllegalStateException("1: Module '" + module.getModuleName() + "' blocked by policy");
        }
        // Preliminary checks passed; continue to check dependencies
        else {
            for (String dependency : module.getModuleDependencies()) {
                // Check if dependencies are registered
                if (!modules.containsKey(dependency)) {
                    throw new IllegalStateException("2: Dependency error in module '" + module.getModuleName() + "': Dependency '" + dependency + "' not found");
                }
                // Check if dependencies are still loading
                else if (modules.get(dependency).getCurrentState() == ModuleState.LOADING) {
                    throw new IllegalStateException("2: Dependency error in module '" + module.getModuleName() + "': Dependancy '" + dependency + "' is currently loading");
                }
                // Check if dependencies are fully loaded
                else if (modules.get(dependency).getCurrentState() != ModuleState.LOADED) {
                    // Load dependencies if parameter is set
                    if (loadDependancies) {
                        // Attempt to enable the module
                        try {
                            if (!enableModule(dependency, true)) {
                                throw new IllegalStateException("2: Dependency error in module '" + module.getModuleName() + "': Dependency '" + dependency + "' failed to load");
                            }
                        } catch (Throwable e) {
                            throw new IllegalStateException("2: Dependency error in module '" + module.getModuleName() + "': Dependency '" + dependency + "' failed to load");
                        }
                    } else {
                        throw new IllegalStateException("2: Dependency error in module '" + module.getModuleName() + "': Dependency '" + dependency + "' not loaded");
                    }
                }
            }
        }
    }
    
    @Override
    @Deprecated
    public boolean enableModule(String moduleName, boolean loadDependencies) {
        return enableModule(moduleName, loadDependencies, "Unknown");
    }
    
    @Override
    @Deprecated
    public boolean disableModule(String moduleName, boolean forceUnload) {
        return disableModule(moduleName, forceUnload, "Unknown");
    }
    
    @Override
    public boolean enableModule(String moduleName, boolean loadDependencies, String authorizingUser) {
        // Fetch the actual ModuleLoader object
        ModuleLoader module = modules.get(moduleName);
        
        // Perform pre-enable checks
        verifyModuleLoad(module, loadDependencies);
        try {
            // Load the module and initialize the audit log
            module.loadModule(GoldenApple.getInstance());
            AuditLog.logEvent(new ModuleEnableEvent(module.getModuleName(), authorizingUser));
            return true;
        } catch (Throwable e) {
            // Handle any exceptions
            GoldenApple.log(Level.SEVERE, "Encountered an unrecoverable error while enabling module '" + module.getModuleName() + "'");
            GoldenApple.log(Level.SEVERE, e);
            ModuleLoadException eDump = (e instanceof ModuleLoadException) ? (ModuleLoadException) e : new ModuleLoadException(module.getModuleName(), e);
            if (GoldenApple.getInstanceMainConfig().getBoolean("securityPolicy.dumpExtendedInfo", true)) {
                try {
                    File dumpFile = nextDumpFile(module.getModuleName());
                    ObjectOutputStream o = new ObjectOutputStream(new FileOutputStream(dumpFile));
                    try {
                        o.writeObject(eDump);
                    } finally {
                        o.close();
                    }
                    GoldenApple.log(Level.SEVERE, "Technical information dumped to " + dumpFile.getCanonicalPath());
                } catch (Throwable e2) {
                    GoldenApple.log(Level.SEVERE, "An error occured while dumping technical information:");
                    GoldenApple.log(Level.SEVERE, e2);
                }
            }
            return false;
        }
    }
    
    /**
     * Gets a file pointing to the next valid location for a dump file.
     * 
     * @param module The name of the module to create a dump file for.
     * 
     * @return A File object for a new dump file.
     * 
     * @throws IOException
     */
    private File nextDumpFile(String module) throws IOException {
        // Make directories if they don't already exist
        if (!new File(GoldenApple.getInstance().getDataFolder() + "/dumps").exists()) {
            new File(GoldenApple.getInstance().getDataFolder() + "/dumps").mkdirs();
        }
        
        // Create new file and return pointer to it.
        for (int i = 1;; i++) {
            File f = new File(GoldenApple.getInstance().getDataFolder() + "/dumps/mcrash-" + module + "-" + i + ".dmp");
            if (!f.exists()) {
                f.createNewFile();
                return f;
            }
        }
    }
    
    @Override
    public boolean disableModule(String moduleName, boolean forceUnload, String authorizingUser) {
        // Get the module's {@link ModuleLoader} object
        ModuleLoader module = modules.get(moduleName);
        
        // If module's not ready and forceUnload is not set, throw an error
        if (!forceUnload && module.getCurrentState() != ModuleState.LOADED) {
            throw new IllegalStateException("Module '" + module.getModuleName() + "' was not in an expected state to be disabled");
        }
        // Check for and unload all modules that depend on the given module
        for (Entry<String, ModuleLoader> checkDepend : modules.entrySet()) {
            if (checkDepend.getValue().getCurrentState() != ModuleState.LOADED) {
                continue;
            }
            for (String depend : checkDepend.getValue().getModuleDependencies()) {
                if (depend.equals(module.getModuleName())) {
                    disableModule(checkDepend.getKey(), forceUnload, authorizingUser);
                }
            }
        }
        // Attempt to unload the current module
        try {
            module.unloadModule(GoldenApple.getInstance());
        } catch (Throwable e) {
            GoldenApple.log(Level.WARNING, "Module '" + module.getModuleName() + "' threw an exception while unloading:");
            GoldenApple.log(Level.WARNING, e);
        }
        // Make sure the module has unloaded completely
        if (module.getCurrentState() != ModuleState.UNLOADED_USER) {
            if (forceUnload) {
                GoldenApple.log(Level.SEVERE, "Module '" + module.getModuleName() + "' is not in an expected state after unloading. Forcing shutdown...");
                module.forceSetState(ModuleState.UNLOADED_USER);
            } else {
                GoldenApple.log(Level.SEVERE, "Module '" + module.getModuleName() + "' is not in an expected state after unloading.");
                return false;
            }
        }
        // Log the event
        AuditLog.logEvent(new ModuleDisableEvent(module.getModuleName(), authorizingUser));
        return true;
    }
    
}
