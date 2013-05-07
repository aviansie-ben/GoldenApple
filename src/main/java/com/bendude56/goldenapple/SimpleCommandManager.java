package com.bendude56.goldenapple;

import java.util.HashMap;
import org.bukkit.Bukkit;
import org.bukkit.command.CommandExecutor;
import com.bendude56.goldenapple.commands.AutoLockCommand;
import com.bendude56.goldenapple.commands.BackCommand;
import com.bendude56.goldenapple.commands.BanCommand;
import com.bendude56.goldenapple.commands.ChannelCommand;
import com.bendude56.goldenapple.commands.ComplexCommand;
import com.bendude56.goldenapple.commands.DelHomeCommand;
import com.bendude56.goldenapple.commands.HomeCommand;
import com.bendude56.goldenapple.commands.ImportCommand;
import com.bendude56.goldenapple.commands.LemonPledgeCommand;
import com.bendude56.goldenapple.commands.LockCommand;
import com.bendude56.goldenapple.commands.MeCommand;
import com.bendude56.goldenapple.commands.ModuleCommand;
import com.bendude56.goldenapple.commands.MuteCommand;
import com.bendude56.goldenapple.commands.OwnCommand;
import com.bendude56.goldenapple.commands.PermissionsCommand;
import com.bendude56.goldenapple.commands.SetHomeCommand;
import com.bendude56.goldenapple.commands.SpawnCommand;
import com.bendude56.goldenapple.commands.TpCommand;
import com.bendude56.goldenapple.commands.TpHereCommand;
import com.bendude56.goldenapple.commands.UnloadedCommand;
import com.bendude56.goldenapple.commands.VerifyCommand;

public class SimpleCommandManager extends CommandManager {
	public static final UnloadedCommand defaultCommand = new UnloadedCommand();
	
	private HashMap<String, CommandInformation> commands = new HashMap<String, CommandInformation>();
	
	public SimpleCommandManager() {
		// Module - Base
		insertCommand("gamodule", "Base", new ModuleCommand());
		insertCommand("gaverify", "Base", new VerifyCommand());
		insertCommand("gaimport", "Base", new ImportCommand());
		insertCommand("gacomplex", "Base", new ComplexCommand());
		
		// Module - Permissions
		insertCommand("gapermissions", "Permissions", new PermissionsCommand());
		insertCommand("gaown", "Permissions", new OwnCommand());
		
		// Module - Chat
		insertCommand("gachannel", "Chat", new ChannelCommand());
		insertCommand("game", "Chat", new MeCommand());
		insertCommand("galemonpledge", "Chat", new LemonPledgeCommand());
		
		// Module - Lock
		insertCommand("galock", "Lock", new LockCommand());
		insertCommand("gaautolock", "Lock", new AutoLockCommand());
		
		// Module - Warp
		insertCommand("gahome", "Warp", new HomeCommand());
		insertCommand("gasethome", "Warp", new SetHomeCommand());
		insertCommand("gadelhome", "Warp", new DelHomeCommand());
		insertCommand("gaspawn", "Warp", new SpawnCommand());
		insertCommand("gaback" ,"Warp", new BackCommand());
		insertCommand("gatp", "Warp", new TpCommand());
		insertCommand("gatphere", "Warp", new TpHereCommand());
		
		// Module - Punish
		insertCommand("gaban", "Punish", new BanCommand());
		insertCommand("gamute", "Punish", new MuteCommand());
		insertCommand("gaglobalmute", "Punish", null);
		insertCommand("gawhois", "Punish", null);
	}
	
	public void insertCommand(String commandName, String module, CommandExecutor executor) {
		commands.put(commandName, new CommandInformation(Bukkit.getPluginCommand(commandName), module, executor));
	}
	
	public CommandInformation getCommand(String command) {
		return commands.get(command);
	}

	@Override
	public CommandExecutor getDefaultCommand() {
		return defaultCommand;
	}
}
