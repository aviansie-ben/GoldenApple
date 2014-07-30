package com.bendude56.goldenapple.invisible;

import com.bendude56.goldenapple.CommandManager;
import com.bendude56.goldenapple.ModuleLoader;
import com.bendude56.goldenapple.invisible.command.PoofCommand;
import com.bendude56.goldenapple.permissions.PermissionManager;

public class InvisibleModuleLoader extends ModuleLoader {
    
    public InvisibleModuleLoader() {
        super("Invisible", new String[] { "Permissions" }, "modules.invisible.enabled", "securityPolicy.blockModules.invisible", "securityPolicy.blockManualUnload.invisible");
    }
    
    @Override
    protected void preregisterCommands(CommandManager commands) {
        commands.insertCommand("gapoof", "Invisible", new PoofCommand());
    }
    
    @Override
    public void preregisterPermissions() {
        InvisibilityManager.invisibleNode = PermissionManager.goldenAppleNode.createNode("invisible");
        InvisibilityManager.vanishPermission = InvisibilityManager.invisibleNode.createPermission("vanish");
        InvisibilityManager.vanishInteractPermission = InvisibilityManager.invisibleNode.createPermission("vanishInteract");
        InvisibilityManager.seeVanishedPermission = InvisibilityManager.invisibleNode.createPermission("seeVanished");
    }
    
    @Override
    protected void registerListener() {
        InvisibilityListener.startListening();
    }
    
    @Override
    protected void registerCommands(CommandManager commands) {
        commands.getCommand("gapoof").register();
    }
    
    @Override
    protected void initializeManager() {
        InvisibilityManager.instance = new SimpleInvisibilityManager();
    }
    
    @Override
    public void clearCache() {
        // There's no cache to clear!
    }
    
    @Override
    protected void unregisterCommands(CommandManager commands) {
        commands.getCommand("gapoof").unregister();
    }
    
    @Override
    protected void unregisterListener() {
        InvisibilityListener.stopListening();
    }
    
    @Override
    protected void destroyManager() {
        InvisibilityManager.instance = null;
    }
    
}
