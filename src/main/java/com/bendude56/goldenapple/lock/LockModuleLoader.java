package com.bendude56.goldenapple.lock;

import com.bendude56.goldenapple.CommandManager;
import com.bendude56.goldenapple.ModuleLoader;
import com.bendude56.goldenapple.lock.command.AutoLockCommand;
import com.bendude56.goldenapple.lock.command.LockCommand;
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
    public void preregisterPermissions() {
        LockManager.lockNode = PermissionManager.goldenAppleNode.createNode("lock");
        LockManager.addPermission = LockManager.lockNode.createPermission("add");
        LockManager.usePermission = LockManager.lockNode.createPermission("use");
        LockManager.invitePermission = LockManager.lockNode.createPermission("invite");
        LockManager.modifyBlockPermission = LockManager.lockNode.createPermission("modifyBlock");
        LockManager.fullPermission = LockManager.lockNode.createPermission("full");
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
    public void clearCache() {
        LockManager.getInstance().clearCache();
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
