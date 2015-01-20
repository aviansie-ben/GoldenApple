package com.bendude56.goldenapple.mail;

import com.bendude56.goldenapple.CommandManager;
import com.bendude56.goldenapple.ModuleLoader;
import com.bendude56.goldenapple.mail.command.MailCommand;
import com.bendude56.goldenapple.permissions.PermissionManager;

public class MailModuleLoader extends ModuleLoader {
    
    public MailModuleLoader() {
        super("Mail", new String[] { "Permissions" }, "modules.mail.enabled", "securityPolicy.blockModules.mail", "securityPolicy.blockManualUnload.mail");
    }
    
    @Override
    protected void preregisterCommands(CommandManager commands) {
        commands.insertCommand("gamail", "Mail", new MailCommand());
    }
    
    @Override
    public void preregisterPermissions() {
        MailManager.mailNode = PermissionManager.goldenAppleNode.createNode("mail");
        MailManager.mailSendPermission = MailManager.mailNode.createPermission("send");
        MailManager.mailReplyPermission = MailManager.mailNode.createPermission("reply");
    }
    
    @Override
    protected void registerCommands(CommandManager commands) {
        commands.getCommand("gamail").register();
    }
    
    @Override
    protected void registerListener() {
        MailListener.startListening();
    }
    
    @Override
    protected void initializeManager() {
        MailManager.instance = new SimpleMailManager();
    }
    
    @Override
    public void clearCache() {
        MailManager.getInstance().clearCache();
    }
    
    @Override
    protected void destroyManager() {
        MailManager.instance = null;
    }
    
    @Override
    protected void unregisterListener() {
        MailListener.stopListening();
    }
    
    @Override
    protected void unregisterCommands(CommandManager commands) {
        commands.getCommand("gamail").unregister();
    }
    
}
