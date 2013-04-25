package com.bendude56.goldenapple.lock;

import com.bendude56.goldenapple.CommandManager;
import com.bendude56.goldenapple.GoldenApple;
import com.bendude56.goldenapple.ModuleLoader;
import com.bendude56.goldenapple.listener.LockListener;
import com.bendude56.goldenapple.permissions.PermissionManager;

public class LockModuleLoader implements ModuleLoader {

	private static ModuleState state = ModuleState.UNLOADED_USER;

	@Override
	public void loadModule(GoldenApple instance) {
		state = ModuleState.LOADING;
		try {
			LockManager.instance = new SimpleLockManager();
			registerPermissions(PermissionManager.getInstance());
			registerEvents();
			registerCommands(instance.getCommandManager());
			state = ModuleState.LOADED;
		} catch (Throwable e) {
			state = ModuleState.UNLOADED_ERROR;
			unregisterCommands(instance.getCommandManager());
		}
	}
	
	@Override
	public void registerPermissions(PermissionManager permissions) {
		LockManager.lockNode = permissions.registerNode("lock", PermissionManager.goldenAppleNode);
		LockManager.addPermission = permissions.registerPermission("add", LockManager.lockNode);
		LockManager.usePermission = permissions.registerPermission("use", LockManager.lockNode);
		LockManager.invitePermission = permissions.registerPermission("invite", LockManager.lockNode);
		LockManager.modifyBlockPermission = permissions.registerPermission("modifyBlock", LockManager.lockNode);
		LockManager.fullPermission = permissions.registerPermission("full", LockManager.lockNode);
	}
	
	private void registerEvents() {
		LockListener.startListening();
	}
	
	private void registerCommands(CommandManager commands) {
		commands.getCommand("galock").register();
		commands.getCommand("gaautolock").register();
	}

	@Override
	public void unloadModule(GoldenApple instance) {
		unregisterEvents();
		unregisterCommands(instance.getCommandManager());
		LockManager.instance = null;
		state = ModuleState.UNLOADED_USER;
	}
	
	private void unregisterCommands(CommandManager commands) {
		commands.getCommand("galock").unregister();
		commands.getCommand("gaautolock").unregister();
	}
	
	private void unregisterEvents() {
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
	public void setState(ModuleState state) {
		LockModuleLoader.state = state;
	}

	@Override
	public String[] getModuleDependencies() {
		return new String[] { "Permissions" };
	}

	@Override
	public boolean canLoadAuto() {
		return GoldenApple.getInstanceMainConfig().getBoolean("modules.lock.enabled", true);
	}

	@Override
	public boolean canPolicyLoad() {
		return !GoldenApple.getInstanceMainConfig().getBoolean("securityPolicy.blockModules.lock", false);
	}
	
	@Override
	public boolean canPolicyUnload() {
		return !GoldenApple.getInstanceMainConfig().getBoolean("securityPolicy.blockManualUnload.lock", false);
	}

}
