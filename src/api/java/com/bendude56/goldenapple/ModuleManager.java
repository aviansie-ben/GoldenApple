package com.bendude56.goldenapple;

import java.util.List;

public interface ModuleManager {
	void registerModule(ModuleLoader module);
	void unregisterModule(String moduleName);
	
	List<ModuleLoader> getModules();
	ModuleLoader getModule(String moduleName);
	
	boolean enableModule(String moduleName, boolean loadDependencies);
	boolean disableModule(String moduleName, boolean forceUnload);
}
