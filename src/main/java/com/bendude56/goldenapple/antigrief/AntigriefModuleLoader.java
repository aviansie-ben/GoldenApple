package com.bendude56.goldenapple.antigrief;

import com.bendude56.goldenapple.CommandManager;
import com.bendude56.goldenapple.ModuleLoader;
import com.bendude56.goldenapple.permissions.PermissionManager;
import com.bendude56.goldenapple.permissions.PermissionManager.Permission;
import com.bendude56.goldenapple.permissions.PermissionManager.PermissionNode;

public class AntigriefModuleLoader extends ModuleLoader {
    public static PermissionNode antigriefNode;
    public static Permission tntPermission;
    public static Permission lighterPermission;
    
    public AntigriefModuleLoader() {
        super("Antigrief", new String[] { "Base" }, "modules.antigrief.enabled", "securityPolicy.blockModules.antigrief", "securityPolicy.blockManualUnload.antigrief");
    }
    
    @Override
    protected void preregisterCommands(CommandManager commands) {}
    
    @Override
    public void preregisterPermissions() {
        antigriefNode = PermissionManager.goldenAppleNode.createNode("antigrief");
        tntPermission = antigriefNode.createPermission("tnt");
        lighterPermission = antigriefNode.createPermission("lighter");
    }
    
    @Override
    protected void registerCommands(CommandManager commands) {}
    
    @Override
    protected void registerListener() {
        AntigriefListener.startListening();
    }
    
    @Override
    protected void initializeManager() {}
    
    @Override
    public void clearCache() {}
    
    @Override
    protected void unregisterCommands(CommandManager commands) {}
    
    @Override
    protected void unregisterListener() {
        AntigriefListener.stopListening();
    }
    
    @Override
    protected void destroyManager() {}
}
