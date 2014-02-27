package com.bendude56.goldenapple.punish;

import com.bendude56.goldenapple.CommandManager;
import com.bendude56.goldenapple.ModuleLoader;
import com.bendude56.goldenapple.commands.BanCommand;
import com.bendude56.goldenapple.commands.GlobalMuteCommand;
import com.bendude56.goldenapple.commands.MuteCommand;
import com.bendude56.goldenapple.commands.UnBanCommand;
import com.bendude56.goldenapple.commands.UnGlobalMuteCommand;
import com.bendude56.goldenapple.commands.UnMuteCommand;
import com.bendude56.goldenapple.listener.PunishmentListener;
import com.bendude56.goldenapple.permissions.PermissionManager;

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
		commands.insertCommand("gawhois", "Punish", null);
	}
	
	@Override
	protected void registerPermissions(PermissionManager permissions) {
		PunishmentManager.punishNode = permissions.registerNode("punish", PermissionManager.goldenAppleNode);
		
		PunishmentManager.globalMuteNode = permissions.registerNode("globalmute", PunishmentManager.punishNode);
		PunishmentManager.globalMuteInfoPermission = permissions.registerPermission("info", PunishmentManager.globalMuteNode);
		PunishmentManager.globalMuteTempPermission = permissions.registerPermission("temp", PunishmentManager.globalMuteNode);
		PunishmentManager.globalMuteTempOverridePermission = permissions.registerPermission("tempOverride", PunishmentManager.globalMuteNode);
		PunishmentManager.globalMutePermPermission = permissions.registerPermission("perm", PunishmentManager.globalMuteNode);
		PunishmentManager.globalMuteVoidPermission = permissions.registerPermission("void", PunishmentManager.globalMuteNode);
		PunishmentManager.globalMuteVoidAllPermission = permissions.registerPermission("voidAll", PunishmentManager.globalMuteNode);
		
		PunishmentManager.banNode = permissions.registerNode("ban", PunishmentManager.punishNode);
		PunishmentManager.banInfoPermission = permissions.registerPermission("info", PunishmentManager.banNode);
		PunishmentManager.banTempPermission = permissions.registerPermission("temp", PunishmentManager.banNode);
		PunishmentManager.banTempOverridePermission = permissions.registerPermission("tempOverride", PunishmentManager.banNode);
		PunishmentManager.banPermPermission = permissions.registerPermission("perm", PunishmentManager.banNode);
		PunishmentManager.banVoidPermission = permissions.registerPermission("void", PunishmentManager.banNode);
		PunishmentManager.banVoidAllPermission = permissions.registerPermission("voidAll", PunishmentManager.banNode);
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
	protected void unregisterPermissions(PermissionManager permissions) {
		PunishmentManager.punishNode = null;
		
		PunishmentManager.banNode = null;
		PunishmentManager.banTempPermission = null;
		PunishmentManager.banTempOverridePermission = null;
		PunishmentManager.banPermPermission = null;
		PunishmentManager.banVoidPermission = null;
		PunishmentManager.banVoidAllPermission = null;
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
