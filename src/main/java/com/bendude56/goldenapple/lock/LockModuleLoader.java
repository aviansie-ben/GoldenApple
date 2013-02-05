package com.bendude56.goldenapple.lock;

import com.bendude56.goldenapple.CommandManager;
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
			registerCommands(instance.commands);
			state = ModuleState.LOADED;
		} catch (Throwable e) {
			state = ModuleState.UNLOADED_ERROR;
			unregisterCommands(instance.commands);
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
		unregisterCommands(instance.commands);
		GoldenApple.getInstance().locks = null;
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
		return GoldenApple.getInstance().mainConfig.getBoolean("modules.lock.enabled", true);
	}

	@Override
	public boolean canPolicyLoad() {
		return !GoldenApple.getInstance().mainConfig.getBoolean("securityPolicy.blockModules.lock", false);
	}
	
	@Override
	public boolean canPolicyUnload() {
		return !GoldenApple.getInstance().mainConfig.getBoolean("securityPolicy.blockManualUnload.lock", false);
	}

}
