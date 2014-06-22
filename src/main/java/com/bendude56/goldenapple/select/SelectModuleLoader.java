package com.bendude56.goldenapple.select;

import com.bendude56.goldenapple.CommandManager;
import com.bendude56.goldenapple.ModuleLoader;
import com.bendude56.goldenapple.permissions.PermissionManager;
import com.bendude56.goldenapple.select.command.SelectionContractCommand;
import com.bendude56.goldenapple.select.command.SelectionExpandCommand;
import com.bendude56.goldenapple.select.command.SelectionShiftCommand;

public class SelectModuleLoader extends ModuleLoader {
    
    public SelectModuleLoader() {
        super("Select", new String[] { "Permissions" }, "modules.select.enabled", "securityPolicy.blockModules.select", "securityPolicy.blockManualUnload.select");
    }
    
    @Override
    protected void preregisterCommands(CommandManager commands) {
        commands.insertCommand("gaselcontract", "Select", new SelectionContractCommand());
        commands.insertCommand("gaselexpand", "Select", new SelectionExpandCommand());
        commands.insertCommand("gaselshift", "Select", new SelectionShiftCommand());
    }
    
    @Override
    protected void registerPermissions(PermissionManager permissions) {
        SelectManager.selectNode = PermissionManager.goldenAppleNode.createNode("select");
        SelectManager.builtinNode = SelectManager.selectNode.createNode("builtin");
        SelectManager.builtinSelectPermission = SelectManager.builtinNode.createPermission("select");
        SelectManager.builtinExpandPermission = SelectManager.builtinNode.createPermission("expand");
        SelectManager.builtinContractPermission = SelectManager.builtinNode.createPermission("contract");
        SelectManager.builtinShiftPermission = SelectManager.builtinNode.createPermission("shift");
    }
    
    @Override
    protected void registerCommands(CommandManager commands) {
        commands.getCommand("gaselcontract").register();
        commands.getCommand("gaselexpand").register();
        commands.getCommand("gaselshift").register();
    }
    
    @Override
    protected void registerListener() {
        SelectListener.startListening();
    }
    
    @Override
    protected void initializeManager() {
        SelectManager.instance = new SimpleSelectManager();
    }
    
    @Override
    public void clearCache() {
        // Do nothing
    }
    
    @Override
    protected void unregisterPermissions(PermissionManager permissions) {
        SelectManager.selectNode = null;
        SelectManager.builtinNode = null;
        SelectManager.builtinSelectPermission = null;
        SelectManager.builtinExpandPermission = null;
        SelectManager.builtinContractPermission = null;
        SelectManager.builtinShiftPermission = null;
    }
    
    @Override
    protected void unregisterCommands(CommandManager commands) {
        commands.getCommand("gaselcontract").unregister();
        commands.getCommand("gaselexpand").unregister();
        commands.getCommand("gaselshift").unregister();
    }
    
    @Override
    protected void unregisterListener() {
        SelectListener.stopListening();
    }
    
    @Override
    protected void destroyManager() {
        SelectManager.instance = null;
    }
    
}
