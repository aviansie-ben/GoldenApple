package com.bendude56.goldenapple.lock;

import org.bukkit.Bukkit;

import com.bendude56.goldenapple.GoldenApple;
import com.bendude56.goldenapple.IModuleLoader;
import com.bendude56.goldenapple.commands.AutoLockCommand;
import com.bendude56.goldenapple.commands.LockCommand;
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
	
	@Override
	public void registerPermissions(PermissionManager permissions) {
		LockManager.lockNode = permissions.registerNode("lock", PermissionManager.goldenAppleNode);
		LockManager.addPermission = permissions.registerPermission("add", LockManager.lockNode);
		
		LockManager.removeNode = permissions.registerNode("remove", LockManager.lockNode);
		LockManager.removeAllPermission = permissions.registerPermission("all", LockManager.removeNode);
		LockManager.removeOwnPermission = permissions.registerPermission("own", LockManager.removeNode);
		
		LockManager.guestNode = permissions.registerNode("guest", LockManager.lockNode);
		LockManager.guestAllPermission = permissions.registerPermission("all", LockManager.guestNode);
		LockManager.guestOwnPermission = permissions.registerPermission("own", LockManager.guestNode);
	}
	
	private void registerEvents() {
		LockListener.startListening();
	}
	
	private void registerCommands() {
		Bukkit.getPluginCommand("galock").setExecutor(new LockCommand());
		Bukkit.getPluginCommand("gaautolock").setExecutor(new AutoLockCommand());
	}

	@Override
	public void unloadModule(GoldenApple instance) {
		LockListener.stopListening();
		GoldenApple.getInstance().locks = null;
		Bukkit.getPluginCommand("galock").setExecutor(GoldenApple.defCmd);
		Bukkit.getPluginCommand("gaautolock").setExecutor(GoldenApple.defCmd);
		state = ModuleState.UNLOADED_USER;
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
