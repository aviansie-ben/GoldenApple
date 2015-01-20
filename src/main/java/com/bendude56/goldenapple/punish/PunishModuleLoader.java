package com.bendude56.goldenapple.punish;

import com.bendude56.goldenapple.CommandManager;
import com.bendude56.goldenapple.ModuleLoader;
import com.bendude56.goldenapple.permissions.PermissionManager;
import com.bendude56.goldenapple.punish.command.BanCommand;
import com.bendude56.goldenapple.punish.command.GlobalMuteCommand;
import com.bendude56.goldenapple.punish.command.MuteCommand;
import com.bendude56.goldenapple.punish.command.UnBanCommand;
import com.bendude56.goldenapple.punish.command.UnGlobalMuteCommand;
import com.bendude56.goldenapple.punish.command.UnMuteCommand;
import com.bendude56.goldenapple.punish.command.WhoisCommand;

public class PunishModuleLoader extends ModuleLoader {
    
    public PunishModuleLoader() {
        super("Punish", new String[] { "Permissions" }, "modules.punish.enabled", "securityPolicy.blockModules.punish", "securityPolicy.blockManualUnload.punish");
    }
    
    @Override
    protected void preregisterCommands(CommandManager commands) {
        commands.insertCommand("gaban", "Punish", new BanCommand());
        commands.insertCommand("gaunban", "Punish", new UnBanCommand());
        commands.insertCommand("gamute", "Punish", new MuteCommand());
        commands.insertCommand("gaunmute", "Punish", new UnMuteCommand());
        commands.insertCommand("gaglobalmute", "Punish", new GlobalMuteCommand());
        commands.insertCommand("gaunglobalmute", "Punish", new UnGlobalMuteCommand());
        commands.insertCommand("gawhois", "Punish", new WhoisCommand());
    }
    
    @Override
    public void preregisterPermissions() {
        PunishmentManager.punishNode = PermissionManager.goldenAppleNode.createNode("punish");
        PunishmentManager.whoisPermission = PunishmentManager.punishNode.createPermission("whois");
        
        PunishmentManager.globalMuteNode = PunishmentManager.punishNode.createNode("globalmute");
        PunishmentManager.globalMuteInfoPermission = PunishmentManager.globalMuteNode.createPermission("info");
        PunishmentManager.globalMuteTempPermission = PunishmentManager.globalMuteNode.createPermission("temp");
        PunishmentManager.globalMuteTempOverridePermission = PunishmentManager.globalMuteNode.createPermission("tempOverride");
        PunishmentManager.globalMutePermPermission = PunishmentManager.globalMuteNode.createPermission("perm");
        PunishmentManager.globalMuteVoidPermission = PunishmentManager.globalMuteNode.createPermission("void");
        PunishmentManager.globalMuteVoidAllPermission = PunishmentManager.globalMuteNode.createPermission("voidAll");
        
        PunishmentManager.banNode = PunishmentManager.punishNode.createNode("ban");
        PunishmentManager.banInfoPermission = PunishmentManager.banNode.createPermission("info");
        PunishmentManager.banTempPermission = PunishmentManager.banNode.createPermission("temp");
        PunishmentManager.banTempOverridePermission = PunishmentManager.banNode.createPermission("tempOverride");
        PunishmentManager.banPermPermission = PunishmentManager.banNode.createPermission("perm");
        PunishmentManager.banVoidPermission = PunishmentManager.banNode.createPermission("void");
        PunishmentManager.banVoidAllPermission = PunishmentManager.banNode.createPermission("voidAll");
    }
    
    @Override
    protected void registerListener() {
        PunishmentListener.startListening();
    }
    
    @Override
    protected void registerCommands(CommandManager commands) {
        commands.getCommand("gaban").register();
        commands.getCommand("gaunban").register();
        commands.getCommand("gamute").register();
        commands.getCommand("gaunmute").register();
        commands.getCommand("gaglobalmute").register();
        commands.getCommand("gaunglobalmute").register();
        commands.getCommand("gawhois").register();
    }
    
    @Override
    protected void initializeManager() {
        PunishmentManager.instance = new SimplePunishmentManager();
    }
    
    @Override
    public void clearCache() {
        PunishmentManager.getInstance().clearCache();
    }
    
    @Override
    protected void unregisterCommands(CommandManager commands) {
        commands.getCommand("gaban").unregister();
        commands.getCommand("gaunban").unregister();
        commands.getCommand("gamute").unregister();
        commands.getCommand("gaunmute").unregister();
        commands.getCommand("gaglobalmute").unregister();
        commands.getCommand("gaunglobalmute").unregister();
        commands.getCommand("gawhois").unregister();
    }
    
    @Override
    protected void unregisterListener() {
        PunishmentListener.stopListening();
    }
    
    @Override
    protected void destroyManager() {
        PunishmentManager.instance = null;
    }
    
}
