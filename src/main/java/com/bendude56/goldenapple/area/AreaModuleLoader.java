package com.bendude56.goldenapple.area;

import com.bendude56.goldenapple.CommandManager;
import com.bendude56.goldenapple.ModuleLoader;
import com.bendude56.goldenapple.area.command.AreaCommand;
import com.bendude56.goldenapple.permissions.PermissionManager;

public class AreaModuleLoader extends ModuleLoader {
    
    public AreaModuleLoader() {
        super("Area", new String[] { "Permissions", "Select" }, "modules.area.enabled", "securityPolicy.blockModules.area", "securityPolicy.blockManualUnload.area");
    }
    
    @Override
    protected void preregisterCommands(CommandManager commands) {
        commands.insertCommand("gaarea", "Area", new AreaCommand());
    }
    
    @Override
    public void preregisterPermissions() {
        // Generic area permissions
        AreaManager.areaNode = PermissionManager.goldenAppleNode.createNode("area");
        AreaManager.addPermission = AreaManager.areaNode.createPermission("add");
        AreaManager.removePermission = AreaManager.areaNode.createPermission("remove");
        AreaManager.overridePermission = AreaManager.areaNode.createPermission("override");
        
        // Area listing permissions
        AreaManager.areaListNode = AreaManager.areaNode.createNode("list");
        AreaManager.listAllPermission = AreaManager.areaListNode.createPermission("all");
        AreaManager.listLocationPermission = AreaManager.areaListNode.createPermission("location");
        AreaManager.listOwnPermission = AreaManager.areaListNode.createPermission("own");
        
        // Area information permissions
        AreaManager.areaInfoNode = AreaManager.areaNode.createNode("info");
        AreaManager.infoAllPermission = AreaManager.areaInfoNode.createPermission("all");
        AreaManager.infoOwnPermission = AreaManager.areaInfoNode.createPermission("own");
        
        // Global area editing permissions
        AreaManager.areaEditNode = AreaManager.areaNode.createNode("edit");
        AreaManager.areaEditAllNode = AreaManager.areaEditNode.createNode("all");
        AreaManager.editAllLabelPermission = AreaManager.areaEditAllNode.createPermission("label");
        AreaManager.editAllPriorityPermission = AreaManager.areaEditAllNode.createPermission("priority");
        AreaManager.editAllOwnersPermission = AreaManager.areaEditAllNode.createPermission("owners");
        AreaManager.editAllGuestsPermission = AreaManager.areaEditAllNode.createPermission("guests");
        AreaManager.editAllRegionsPermission = AreaManager.areaEditAllNode.createPermission("regions");
        AreaManager.editAllFlagsPermission = AreaManager.areaEditAllNode.createPermission("flags");
        
        // Own area editing permissions
        AreaManager.areaEditOwnNode = AreaManager.areaEditNode.createNode("own");
        AreaManager.editOwnLabelPermission = AreaManager.areaEditOwnNode.createPermission("label");
        AreaManager.editOwnPriorityPermission = AreaManager.areaEditOwnNode.createPermission("priority");
        AreaManager.editOwnOwnersPermission = AreaManager.areaEditOwnNode.createPermission("owners");
        AreaManager.editOwnGuestsPermission = AreaManager.areaEditOwnNode.createPermission("guests");
        AreaManager.editOwnRegionsPermission = AreaManager.areaEditOwnNode.createPermission("regions");
        AreaManager.editOwnFlagsPermission = AreaManager.areaEditOwnNode.createPermission("flags");
    }
    
    @Override
    protected void registerCommands(CommandManager commands) {
        commands.getCommand("gaarea").register();
    }
    
    @Override
    protected void registerListener() {
        AreaListener.startListening();
    }
    
    @Override
    protected void initializeManager() {
        AreaManager.instance = new SimpleAreaManager();
    }
    
    @Override
    protected void unregisterCommands(CommandManager commands) {
        commands.getCommand("gaarea").unregister();
    }
    
    @Override
    protected void unregisterListener() {
        AreaListener.stopListening();
    }
    
    @Override
    protected void destroyManager() {
        AreaManager.instance = null;
    }
    
    @Override
    public void clearCache() {
        AreaManager.getInstance().clearCache();
    }
    
}
