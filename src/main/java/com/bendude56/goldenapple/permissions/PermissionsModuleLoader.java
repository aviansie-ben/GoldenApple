package com.bendude56.goldenapple.permissions;

import com.bendude56.goldenapple.CommandManager;
import com.bendude56.goldenapple.ModuleLoader;
import com.bendude56.goldenapple.User;
import com.bendude56.goldenapple.permissions.command.LangCommand;
import com.bendude56.goldenapple.permissions.command.OwnCommand;
import com.bendude56.goldenapple.permissions.command.PermissionsCommand;

public class PermissionsModuleLoader extends ModuleLoader {
    
    public PermissionsModuleLoader() {
        super("Permissions", new String[] { "Base" }, null, null, "securityPolicy.blockManualUnload.permissions");
    }
    
    @Override
    protected void preregisterCommands(CommandManager commands) {
        commands.insertCommand("gapermissions", "Permissions", new PermissionsCommand());
        commands.insertCommand("gaown", "Permissions", new OwnCommand());
        commands.insertCommand("galang", "Permissions", new LangCommand());
    }
    
    @Override
    protected void registerPermissions(PermissionManager permissions) {
        PermissionManager.goldenAppleNode = permissions.getRootNode().createNode("goldenapple");
        PermissionManager.importPermission = PermissionManager.goldenAppleNode.createPermission("import");
        
        PermissionManager.permissionNode = PermissionManager.goldenAppleNode.createNode("permissions");
        
        PermissionManager.userNode = PermissionManager.permissionNode.createNode("user");
        PermissionManager.userAddPermission = PermissionManager.userNode.createPermission("add");
        PermissionManager.userRemovePermission = PermissionManager.userNode.createPermission("remove");
        PermissionManager.userEditPermission = PermissionManager.userNode.createPermission("edit");
        
        PermissionManager.groupNode = PermissionManager.permissionNode.createNode("group");
        PermissionManager.groupAddPermission = PermissionManager.groupNode.createPermission("add");
        PermissionManager.groupRemovePermission = PermissionManager.groupNode.createPermission("remove");
        PermissionManager.groupEditPermission = PermissionManager.groupNode.createPermission("edit");
        
        PermissionManager.moduleNode = PermissionManager.goldenAppleNode.createNode("module");
        PermissionManager.moduleLoadPermission = PermissionManager.moduleNode.createPermission("load");
        PermissionManager.moduleUnloadPermission = PermissionManager.moduleNode.createPermission("unload");
        PermissionManager.moduleClearCachePermission = PermissionManager.moduleNode.createPermission("clearCache");
        PermissionManager.moduleQueryPermission = PermissionManager.moduleNode.createPermission("query");
    }
    
    @Override
    protected void registerCommands(CommandManager commands) {
        commands.getCommand("gapermissions").register();
        commands.getCommand("gaown").register();
        commands.getCommand("galang").register();
    }
    
    @Override
    protected void registerListener() {
        PermissionListener.startListening();
    }
    
    @Override
    protected void initializeManager() {
        PermissionManager.instance = new SimplePermissionManager();
        
        ((SimplePermissionManager)PermissionManager.instance).loadGroups();
        ((SimplePermissionManager)PermissionManager.instance).checkDefaultGroups();
        
        User.clearCache();
    }
    
    @Override
    public void clearCache() {
        PermissionManager.getInstance().clearCache();
    }
    
    @Override
    protected void unregisterPermissions(PermissionManager permissions) {
        PermissionManager.goldenAppleNode = null;
        PermissionManager.importPermission = null;
        
        PermissionManager.permissionNode = null;
        
        PermissionManager.userNode = null;
        PermissionManager.userAddPermission = null;
        PermissionManager.userRemovePermission = null;
        PermissionManager.userEditPermission = null;
        
        PermissionManager.groupNode = null;
        PermissionManager.groupAddPermission = null;
        PermissionManager.groupRemovePermission = null;
        PermissionManager.groupEditPermission = null;
        
        PermissionManager.moduleNode = null;
        PermissionManager.moduleLoadPermission = null;
        PermissionManager.moduleUnloadPermission = null;
        PermissionManager.moduleQueryPermission = null;
    }
    
    @Override
    protected void unregisterCommands(CommandManager commands) {
        commands.getCommand("gapermissions").unregister();
        commands.getCommand("gaown").unregister();
        commands.getCommand("galang").unregister();
    }
    
    @Override
    protected void unregisterListener() {
        PermissionListener.stopListening();
    }
    
    @Override
    protected void destroyManager() {
        User.clearCache();
        PermissionManager.getInstance().close();
        PermissionManager.instance = null;
    }
    
}
