package com.bendude56.goldenapple;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.PluginCommand;
import org.bukkit.command.SimpleCommandMap;
import org.bukkit.plugin.SimplePluginManager;

public abstract class CommandManager {
	public abstract void insertCommand(String commandName, String module, CommandExecutor executor);
	public abstract CommandInformation getCommand(String command);
	public abstract CommandExecutor getDefaultCommand();
	
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
			command.setExecutor(getDefaultCommand());
		}
	}
}
