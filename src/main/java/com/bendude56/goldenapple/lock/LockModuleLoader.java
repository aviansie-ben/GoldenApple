package com.bendude56.goldenapple.lock;

import com.bendude56.goldenapple.GoldenApple;
import com.bendude56.goldenapple.IModuleLoader;
import com.bendude56.goldenapple.listener.LockListener;
import com.bendude56.goldenapple.permissions.PermissionManager;

public class LockModuleLoader implements IModuleLoader {

	private static ModuleState state = ModuleState.UNLOADED_USER;

	@Override
	public void loadModule(GoldenApple instance) {
		state = ModuleState.LOADING;
		try {
			instance.locks = new LockManager();
			registerPermissions(instance.permissions);
			registerEvents();
			registerCommands();
		} catch (Throwable e) {
			state = ModuleState.UNLOADED_ERROR;
			// TODO Add cleanup code to clean up after failed module start
		}
		state = ModuleState.LOADED;
	}
	
	private void registerPermissions(PermissionManager permissions) {
		LockManager.lockNode = permissions.registerNode("lock", PermissionManager.goldenAppleNode);
		LockManager.createPermission = permissions.registerPermission("create", LockManager.lockNode);
		
		LockManager.deleteNode = permissions.registerNode("delete", LockManager.lockNode);
		LockManager.deleteAllPermission = permissions.registerPermission("all", LockManager.deleteNode);
		LockManager.deleteOwnPermission = permissions.registerPermission("own", LockManager.deleteNode);
		
		LockManager.guestNode = permissions.registerNode("guest", LockManager.lockNode);
		LockManager.guestAllPermission = permissions.registerPermission("all", LockManager.guestNode);
		LockManager.guestOwnPermission = permissions.registerPermission("own", LockManager.guestNode);
	}
	
	private void registerEvents() {
		LockListener.startListening();
	}
	
	private void registerCommands() {
		// TODO Register commands
	}

	@Override
	public void unloadModule() {
		LockListener.stopListening();
	}

	@Override
	public String getModuleName() {
		return "Lock";
	}

	@Override
	public ModuleState getCurrentState() {
		return state;
	}

	@Override
	public String[] getModuleDependencies() {
		return new String[] { "Permissions" };
	}

	@Override
	public boolean canLoadAuto() {
		return GoldenApple.getInstance().mainConfig.getBoolean("modules.lock.enabled", true);
	}

	@Override
	public boolean canPolicyLoad() {
		return !GoldenApple.getInstance().mainConfig.getBoolean("securityPolicy.blockModules.lock", false);
	}

}
