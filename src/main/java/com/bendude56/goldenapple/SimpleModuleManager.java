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
import com.bendude56.goldenapple.audit.AuditLog;
import com.bendude56.goldenapple.audit.ModuleDisableEvent;
import com.bendude56.goldenapple.audit.ModuleEnableEvent;
import com.bendude56.goldenapple.chat.ChatModuleLoader;
import com.bendude56.goldenapple.lock.LockModuleLoader;
import com.bendude56.goldenapple.permissions.PermissionsModuleLoader;
import com.bendude56.goldenapple.punish.PunishModuleLoader;
import com.bendude56.goldenapple.warp.WarpModuleLoader;

public class SimpleModuleManager implements ModuleManager {
	private static final String[]			loadOrder	= new String[] { "Base", "Permissions", "Lock", "Antigrief", "Chat", "Warp", "Punish" };

	private HashMap<String, ModuleLoader>	modules = new HashMap<String, ModuleLoader>();

	public SimpleModuleManager() {
		registerModule(new BaseModuleLoader());
		registerModule(new PermissionsModuleLoader());
		registerModule(new LockModuleLoader());
		registerModule(new AntigriefModuleLoader());
		registerModule(new ChatModuleLoader());
		registerModule(new WarpModuleLoader());
		registerModule(new PunishModuleLoader());
	}
	
	public boolean loadDefaults() {
		for (String m : loadOrder) {
			ModuleLoader module = modules.get(m);
			if (module != null && module.canLoadAuto() && module.canPolicyLoad() && module.getCurrentState() == ModuleState.UNLOADED_USER) {
				try {
					if (!enableModule(m, true)) {
						if (GoldenApple.getInstanceMainConfig().getBoolean("securityPolicy.shutdownOnFailedModuleLoad", true)) {
							return false;
						}
					}
				} catch (Exception e) {
					GoldenApplePlugin.log(Level.SEVERE, "An error occured while loading module " + m + ":");
					GoldenApplePlugin.log(Level.SEVERE, e);
					if (GoldenApple.getInstanceMainConfig().getBoolean("securityPolicy.shutdownOnFailedModuleLoad", true)) {
						return false;
					}
				}
			}
		}
		
		return true;
	}
	
	public void unloadAll() {
		for (Map.Entry<String, ModuleLoader> module : modules.entrySet()) {
			if (module.getValue().getCurrentState() == ModuleState.LOADED) {
				disableModule(module.getKey(), true);
			}
		}
	}

	@Override
	public void registerModule(ModuleLoader module) {
		modules.put(module.getModuleName(), module);
	}

	@Override
	public void unregisterModule(String moduleName) {
		modules.remove(moduleName);
	}

	@Override
	public List<ModuleLoader> getModules() {
		List<ModuleLoader> modules = new ArrayList<ModuleLoader>();

		for (Map.Entry<String, ModuleLoader> module : this.modules.entrySet()) {
			modules.add(module.getValue());
		}

		return modules;
	}

	@Override
	public ModuleLoader getModule(String moduleName) {
		return modules.get(moduleName);
	}
	
	private void verifyModuleLoad(ModuleLoader module, boolean loadDependancies) {
		if (module.getCurrentState() == ModuleLoader.ModuleState.LOADED || module.getCurrentState() == ModuleLoader.ModuleState.LOADING) {
			throw new IllegalStateException("0: Module '" + module.getModuleName() + "' already loaded");
		} else if (!module.canPolicyLoad()) {
			throw new IllegalStateException("1: Module '" + module.getModuleName() + "' blocked by policy");
		} else {
			for (String dependency : module.getModuleDependencies()) {
				if (!modules.containsKey(dependency)) {
					throw new IllegalStateException("2: Dependency error in module '" + module.getModuleName() + "': Dependency '" + dependency + "' not found");
				} else if (modules.get(dependency).getCurrentState() == ModuleState.LOADING) {
					throw new IllegalStateException("2: Dependency error in module '" + module.getModuleName() + "': Dependancy '" + dependency + "' is currently loading");
				} else if (modules.get(dependency).getCurrentState() != ModuleState.LOADED) {
					if (loadDependancies) {
						try {
							if (!enableModule(dependency, true))
								throw new IllegalStateException("2: Dependency error in module '" + module.getModuleName() + "': Dependency '" + dependency + "' failed to load");
						} catch (Throwable e) {
							throw new IllegalStateException("2: Dependency error in module '" + module.getModuleName() + "': Dependency '" + dependency + "' failed to load");
						}
					} else {
						throw new IllegalStateException("2: Dependency error in module '" + module.getModuleName() + "': Dependency '" + dependency + "'not loaded");
					}
				}
			}
		}
	}

	@Override
	public boolean enableModule(String moduleName, boolean loadDependencies) {
		ModuleLoader module = modules.get(moduleName);
		
		verifyModuleLoad(module, loadDependencies);
		try {
			module.loadModule(GoldenApple.getInstance());
			AuditLog.logEvent(new ModuleEnableEvent(module.getModuleName()));
			return true;
		} catch (Throwable e) {
			GoldenApple.log(Level.SEVERE, "Encountered an unrecoverable error while enabling module '" + module.getModuleName() + "'");
			GoldenApple.log(Level.SEVERE, e);
			ModuleLoadException eDump = (e instanceof ModuleLoadException) ? (ModuleLoadException)e : new ModuleLoadException(module.getModuleName(), e);
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
	
	private File nextDumpFile(String module) throws IOException {
		if (!new File(GoldenApple.getInstance().getDataFolder() + "/dumps").exists()) {
			new File(GoldenApple.getInstance().getDataFolder() + "/dumps").mkdirs();
		}
		
		for (int i = 1;; i++) {
			File f = new File(GoldenApple.getInstance().getDataFolder() + "/dumps/mcrash-" + module + "-" + i + ".dmp");
			if (!f.exists()) {
				f.createNewFile();
				return f;
			}
		}
	}

	@Override
	public boolean disableModule(String moduleName, boolean forceUnload) {
		ModuleLoader module = modules.get(moduleName);
		
		if (!forceUnload && module.getCurrentState() != ModuleState.LOADED)
			throw new IllegalStateException("Module '" + module.getModuleName() + "' was not in an expected state to be disabled");
		for (Entry<String, ModuleLoader> checkDepend : modules.entrySet()) {
			if (checkDepend.getValue().getCurrentState() != ModuleState.LOADED)
				continue;
			for (String depend : checkDepend.getValue().getModuleDependencies()) {
				if (depend.equals(module.getModuleName())) {
					disableModule(checkDepend.getKey(), forceUnload);
				}
			}
		}
		try {
			module.unloadModule(GoldenApple.getInstance());
		} catch (Throwable e) {
			GoldenApple.log(Level.WARNING, "Module '" + module.getModuleName() + "' threw an exception while unloading:");
			GoldenApple.log(Level.WARNING, e);
		}
		if (module.getCurrentState() != ModuleState.UNLOADED_USER) {
			if (forceUnload) {
				GoldenApple.log(Level.SEVERE, "Module '" + module.getModuleName() + "' is not in an expected state after unloading. Forcing shutdown...");
				module.setState(ModuleState.UNLOADED_USER);
			} else {
				GoldenApple.log(Level.SEVERE, "Module '" + module.getModuleName() + "' is not in an expected state after unloading.");
				return false;
			}
		}
		AuditLog.logEvent(new ModuleDisableEvent(module.getModuleName()));
		return true;
	}

}
