package com.bendude56.goldenapple.request;

import com.bendude56.goldenapple.CommandManager;
import com.bendude56.goldenapple.ModuleLoader;
import com.bendude56.goldenapple.permissions.PermissionManager;
import com.bendude56.goldenapple.request.command.RequestCommand;
import com.bendude56.goldenapple.request.command.RequestQueueCommand;

public class RequestModuleLoader extends ModuleLoader {
    
    public RequestModuleLoader() {
        super("Request", new String[] { "Permissions" }, "modules.request.enabled", "securityPolicy.blockModules.request", "securityPolicy.blockManualUnload.request");
    }
    
    @Override
    protected void preregisterCommands(CommandManager commands) {
        commands.insertCommand("garequest", "Request", new RequestCommand());
        commands.insertCommand("garequestqueue", "Request", new RequestQueueCommand());
    }
    
    @Override
    public void preregisterPermissions() {
        RequestManager.requestNode = PermissionManager.goldenAppleNode.createNode("request");
        RequestManager.statsPermission = RequestManager.requestNode.createPermission("stats");
        RequestManager.viewAllPermission = RequestManager.requestNode.createPermission("viewAll");
        RequestManager.reassignPermission = RequestManager.requestNode.createPermission("reassign");
        RequestManager.editQueuePermission = RequestManager.requestNode.createPermission("editQueue");
    }
    
    @Override
    protected void registerCommands(CommandManager commands) {
        commands.getCommand("garequest").register();
        commands.getCommand("garequestqueue").register();
    }
    
    @Override
    protected void registerListener() {
        RequestListener.startListening();
    }
    
    @Override
    protected void initializeManager() {
        RequestManager.instance = new SimpleRequestManager();
        ((SimpleRequestManager) RequestManager.instance).loadQueues();
        ((SimpleRequestManager) RequestManager.instance).scheduleTasks();
    }
    
    @Override
    public void clearCache() {}
    
    @Override
    protected void unregisterCommands(CommandManager commands) {
        commands.getCommand("garequest").unregister();
        commands.getCommand("garequestqueue").unregister();
    }
    
    @Override
    protected void unregisterListener() {
        RequestListener.stopListening();
    }
    
    @Override
    protected void destroyManager() {
        ((SimpleRequestManager) RequestManager.instance).close();
        RequestManager.instance = null;
    }
    
}
