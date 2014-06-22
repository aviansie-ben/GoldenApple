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
    protected void registerPermissions(PermissionManager permissions) {
        AreaManager.areaNode = permissions.getRootNode().createNode("area");
        AreaManager.areaEditNode = AreaManager.areaNode.createNode("edit");
        AreaManager.addPermission = AreaManager.areaNode.createPermission("add");
        AreaManager.removePermission = AreaManager.areaNode.createPermission("remove");
        AreaManager.overridePermission = AreaManager.areaNode.createPermission("override");
        AreaManager.editLabelPermission = AreaManager.areaEditNode.createPermission("label");
        AreaManager.editPriorityPermission = AreaManager.areaEditNode.createPermission("priority");
        AreaManager.editOwnersPermission = AreaManager.areaEditNode.createPermission("owners");
        AreaManager.editGuestsPermission = AreaManager.areaEditNode.createPermission("guests");
        AreaManager.editRegionsPermission = AreaManager.areaEditNode.createPermission("regions");
        AreaManager.editFlagsPermission = AreaManager.areaEditNode.createPermission("flags");
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
    protected void unregisterPermissions(PermissionManager permissions) {
        AreaManager.areaNode = null;
        AreaManager.areaEditNode = null;
        AreaManager.addPermission = null;
        AreaManager.removePermission = null;
        AreaManager.overridePermission = null;
        AreaManager.editPriorityPermission = null;
        AreaManager.editOwnersPermission = null;
        AreaManager.editGuestsPermission = null;
        AreaManager.editRegionsPermission = null;
        AreaManager.editFlagsPermission = null;
        
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
        // TODO Auto-generated method stub
        
    }
    
}
