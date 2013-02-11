package com.bendude56.goldenapple;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.PluginCommand;
import org.bukkit.command.SimpleCommandMap;
import org.bukkit.plugin.SimplePluginManager;

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
import com.bendude56.goldenapple.commands.OwnCommand;
import com.bendude56.goldenapple.commands.PermissionsCommand;
import com.bendude56.goldenapple.commands.SetHomeCommand;
import com.bendude56.goldenapple.commands.SpawnCommand;
import com.bendude56.goldenapple.commands.TpCommand;
import com.bendude56.goldenapple.commands.TpHereCommand;
import com.bendude56.goldenapple.commands.UnloadedCommand;
import com.bendude56.goldenapple.commands.VerifyCommand;

public class CommandManager {
	public static final UnloadedCommand defaultCommand = new UnloadedCommand();
	
	private HashMap<String, CommandInformation> commands = new HashMap<String, CommandInformation>();
	
	public CommandManager() {
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
		insertCommand("gamute", "Punish", null);
		insertCommand("gaglobalmute", "Punish", null);
		insertCommand("gawhois", "Punish", null);
	}
	
	public void insertCommand(String commandName, String module, CommandExecutor executor) {
		commands.put(commandName, new CommandInformation(Bukkit.getPluginCommand(commandName), module, executor));
	}
	
	public CommandInformation getCommand(String command) {
		return commands.get(command);
	}
	
	public class CommandInformation {
		public String name;
		public List<String> aliases;
		public String module;
		
		public CommandExecutor executor;
		public PluginCommand command;
		
		public CommandInformation(PluginCommand command, String module, CommandExecutor executor) {
			this.name = command.getName();
			this.aliases = command.getAliases();
			this.module = module;
			
			this.executor = executor;
			this.command = command;
			
			unregister();
		}
		
		@SuppressWarnings("unchecked")
		private void setAliases(List<String> aliases) {
			command.setAliases(aliases);
			try {
				Field f = SimplePluginManager.class.getDeclaredField("commandMap");
				f.setAccessible(true);
				SimpleCommandMap commandMap = (SimpleCommandMap)f.get(Bukkit.getPluginManager());
				
				f = SimpleCommandMap.class.getDeclaredField("knownCommands");
				f.setAccessible(true);
				Map<String, Command> cmd = (Map<String, Command>)f.get(commandMap);
				
				f = SimpleCommandMap.class.getDeclaredField("aliases");
				f.setAccessible(true);
				Set<String> mapAliases = (Set<String>)f.get(commandMap);
				
				for (String alias : command.getAliases()) {
					if (cmd.containsKey(alias)) {
						cmd.remove(alias);
						if (mapAliases.contains(alias))
							mapAliases.remove(alias);
					}
				}
				
				for (String alias : aliases) {
					if (!cmd.containsKey(alias)) {
						cmd.put(alias, command);
						if (!mapAliases.contains(alias))
							mapAliases.add(alias);
					}
				}
				
				f = Command.class.getDeclaredField("activeAliases");
				f.setAccessible(true);
				f.set(command, aliases);
			} catch (Throwable e) {
				e.printStackTrace();
			}
		}
		
		public void register() {
			setAliases(aliases);
			command.setExecutor(executor);
		}
		
		public void unregister() {
			setAliases(new ArrayList<String>());			
			command.setExecutor(CommandManager.defaultCommand);
		}
	}
}
