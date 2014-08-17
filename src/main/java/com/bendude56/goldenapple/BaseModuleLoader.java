package com.bendude56.goldenapple;

import com.bendude56.goldenapple.audit.AuditLog;
import com.bendude56.goldenapple.command.ComplexCommand;
import com.bendude56.goldenapple.command.DebugCommand;
import com.bendude56.goldenapple.command.ImportCommand;
import com.bendude56.goldenapple.command.ModuleCommand;
import com.bendude56.goldenapple.command.VerifyCommand;

public class BaseModuleLoader extends ModuleLoader {
	
	public BaseModuleLoader() {
		super("Base", new String[0], null, null, null);
	}
	
	@Override
	protected void preregisterCommands(CommandManager commands) {
		commands.insertCommand("gamodule", "Base", new ModuleCommand());
		commands.insertCommand("gaverify", "Base", new VerifyCommand());
		commands.insertCommand("gaimport", "Base", new ImportCommand());
		commands.insertCommand("gacomplex", "Base", new ComplexCommand());
		commands.insertCommand("gadebug", "Base", new DebugCommand());
	}
	
	@Override
	public void preregisterPermissions() {
		// Do nothing since the base module has no permissions to register
	}
	
	@Override
	protected void registerCommands(CommandManager commands) {
		commands.getCommand("gaverify").register();
		commands.getCommand("gamodule").register();
		commands.getCommand("gacomplex").register();
		commands.getCommand("gadebug").register();
	}
	
	@Override
	protected void registerListener() {
		// Do nothing since the base module has no listener to register
	}
	
	@Override
	protected void initializeManager() {
		AuditLog.initAuditLog();
	}
	
	@Override
	public void clearCache() {
		// TODO Reload config
	}
	
	@Override
	protected void unregisterCommands(CommandManager commands) {
		commands.getCommand("gaverify").unregister();
		commands.getCommand("gamodule").unregister();
		commands.getCommand("gacomplex").unregister();
		commands.getCommand("gadebug").unregister();
	}
	
	@Override
	protected void unregisterListener() {
		// Do nothing since the base module has no listener to unregister
	}
	
	@Override
	protected void destroyManager() {
		AuditLog.deinitAuditLog();
	}
}
