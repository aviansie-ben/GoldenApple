package com.bendude56.goldenapple.lock;

import com.bendude56.goldenapple.CommandManager;
import com.bendude56.goldenapple.ModuleLoader;
import com.bendude56.goldenapple.commands.AutoLockCommand;
import com.bendude56.goldenapple.commands.LockCommand;
import com.bendude56.goldenapple.listener.LockListener;
import com.bendude56.goldenapple.permissions.PermissionManager;

public class LockModuleLoader extends ModuleLoader {
	
	public LockModuleLoader() {
		super("Lock", new String[] { "Permissions" }, "modules.lock.enabled", "securityPolicy.blockModules.lock", "securityPolicy.blockManualUnload.lock");
	}
	
	@Override
	protected void preregisterCommands(CommandManager commands) {
		commands.insertCommand("galock", "Lock", new LockCommand());
		commands.insertCommand("gaautolock", "Lock", new AutoLockCommand());
	}
	
	@Override
	protected void registerPermissions(PermissionManager permissions) {
		LockManager.lockNode = permissions.registerNode("lock", PermissionManager.goldenAppleNode);
		LockManager.addPermission = permissions.registerPermission("add", LockManager.lockNode);
		LockManager.usePermission = permissions.registerPermission("use", LockManager.lockNode);
		LockManager.invitePermission = permissions.registerPermission("invite", LockManager.lockNode);
		LockManager.modifyBlockPermission = permissions.registerPermission("modifyBlock", LockManager.lockNode);
		LockManager.fullPermission = permissions.registerPermission("full", LockManager.lockNode);
	}
	
	@Override
	protected void registerCommands(CommandManager commands) {
		commands.getCommand("galock").register();
		commands.getCommand("gaautolock").register();
	}
	
	@Override
	protected void registerListener() {
		LockListener.startListening();
	}
	
	@Override
	protected void initializeManager() {
		LockManager.instance = new SimpleLockManager();
	}
	
	@Override
	protected void unregisterPermissions(PermissionManager permissions) {
		LockManager.lockNode = null;
		LockManager.addPermission = null;
		LockManager.usePermission = null;
		LockManager.invitePermission = null;
		LockManager.modifyBlockPermission = null;
		LockManager.fullPermission = null;
	}
	
	@Override
	protected void unregisterCommands(CommandManager commands) {
		commands.getCommand("galock").unregister();
		commands.getCommand("gaautolock").unregister();
	}
	
	@Override
	protected void unregisterListener() {
		LockListener.stopListening();
	}
	
	@Override
	protected void destroyManager() {
		LockManager.instance = null;
	}

}
