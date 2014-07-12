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
        AreaManager.areaNode = PermissionManager.goldenAppleNode.createNode("area");
        AreaManager.areaListNode = AreaManager.areaNode.createNode("list");
        AreaManager.areaInfoNode = AreaManager.areaNode.createNode("info");
        AreaManager.areaEditNode = AreaManager.areaNode.createNode("edit");
        AreaManager.areaEditOwnNode = AreaManager.areaEditNode.createNode("own");
        AreaManager.addPermission = AreaManager.areaNode.createPermission("add");
        AreaManager.removePermission = AreaManager.areaNode.createPermission("remove");
        AreaManager.listAllPermission = AreaManager.areaListNode.createPermission("all");
        AreaManager.listLocationPermission = AreaManager.areaListNode.createPermission("location");
        AreaManager.listOwnPermission = AreaManager.areaListNode.createPermission("own");
        AreaManager.infoAllPermission = AreaManager.areaInfoNode.createPermission("all");
        AreaManager.infoOwnPermission = AreaManager.areaInfoNode.createPermission("own");
        AreaManager.overridePermission = AreaManager.areaNode.createPermission("override");
        AreaManager.editLabelPermission = AreaManager.areaEditNode.createPermission("label");
        AreaManager.editPriorityPermission = AreaManager.areaEditNode.createPermission("priority");
        AreaManager.editOwnersPermission = AreaManager.areaEditNode.createPermission("owners");
        AreaManager.editGroupOwnersPermission = AreaManager.areaEditNode.createPermission("groupOwners");
        AreaManager.editGuestsPermission = AreaManager.areaEditNode.createPermission("guests");
        AreaManager.editGroupGuestsPermission = AreaManager.areaEditNode.createPermission("groupGuests");
        AreaManager.editRegionsPermission = AreaManager.areaEditNode.createPermission("regions");
        AreaManager.editFlagsPermission = AreaManager.areaEditNode.createPermission("flags");
        AreaManager.editOwnLabelPermission = AreaManager.areaEditOwnNode.createPermission("label");
        AreaManager.editOwnPriorityPermission = AreaManager.areaEditOwnNode.createPermission("priority");
        AreaManager.editOwnOwnersPermission = AreaManager.areaEditOwnNode.createPermission("owners");
        AreaManager.editOwnGroupOwnersPermission = AreaManager.areaEditOwnNode.createPermission("groupOwners");
        AreaManager.editOwnGuestsPermission = AreaManager.areaEditOwnNode.createPermission("guests");
        AreaManager.editOwnGroupGuestsPermission = AreaManager.areaEditOwnNode.createPermission("groupGuests");
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
    protected void unregisterPermissions(PermissionManager permissions) {
        AreaManager.areaNode = null;
        AreaManager.areaListNode = null;
        AreaManager.areaInfoNode = null;
        AreaManager.areaEditNode = null;
        AreaManager.areaEditOwnNode = null;
        AreaManager.addPermission = null;
        AreaManager.removePermission = null;
        AreaManager.overridePermission = null;
        AreaManager.editLabelPermission = null;
        AreaManager.editPriorityPermission = null;
        AreaManager.editOwnersPermission = null;
        AreaManager.editGroupOwnersPermission = null;
        AreaManager.editGuestsPermission = null;
        AreaManager.editGroupGuestsPermission = null;
        AreaManager.editRegionsPermission = null;
        AreaManager.editFlagsPermission = null;
        AreaManager.editOwnLabelPermission = null;
        AreaManager.editOwnPriorityPermission = null;
        AreaManager.editOwnOwnersPermission = null;
        AreaManager.editOwnGroupOwnersPermission = null;
        AreaManager.editOwnGuestsPermission = null;
        AreaManager.editOwnGroupGuestsPermission = null;
        AreaManager.editOwnRegionsPermission = null;
        AreaManager.editOwnFlagsPermission = null;
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
