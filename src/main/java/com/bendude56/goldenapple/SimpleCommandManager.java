package com.bendude56.goldenapple;

import java.util.Collections;
import java.util.HashMap;
import java.util.List;

import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.PluginCommand;

import com.bendude56.goldenapple.command.UnloadedCommand;

public class SimpleCommandManager extends CommandManager {
	public static final UnloadedCommand defaultCommand = new UnloadedCommand();
	
	private HashMap<String, CommandInformation> commands = new HashMap<String, CommandInformation>();
	
	public SimpleCommandManager() {
	}
	
	@Override
	public void insertCommand(String commandName, String module, CommandExecutor executor) {
		commands.put(commandName, new CommandInformation(Bukkit.getPluginCommand(commandName), module, executor));
	}
	
	@Override
	public ICommandInformation getCommand(String command) {
		return commands.get(command);
	}

	@Override
	public CommandExecutor getDefaultCommand() {
		return defaultCommand;
	}
	
	public class CommandInformation implements ICommandInformation {
		public String name;
		public List<String> aliases;
		public String module;
		
		public CommandExecutor executor;
		public PluginCommand command;
		
		public HashMap<String, Command> oldCommands;
		
		public CommandInformation(PluginCommand command, String module, CommandExecutor executor) {
			this.name = command.getName();
			this.aliases = command.getAliases();
			this.module = module;
			
			this.executor = executor;
			this.command = command;
			
			this.oldCommands = new HashMap<String, Command>();
			
			unregister();
		}
		
		// TODO Reimplement alias registration/unregistration
		
		@Override
		public void register() {
			command.setExecutor(executor);
		}
		
		@Override
		public void unregister() {
			command.setExecutor(getDefaultCommand());
		}

		@Override
		public String getName() {
			return name;
		}

		@Override
		public List<String> getAliases() {
			return Collections.unmodifiableList(aliases);
		}

		@Override
		public String getModule() {
			return module;
		}

		@Override
		public CommandExecutor getExecutor() {
			return executor;
		}

		@Override
		public PluginCommand getCommand() {
			return command;
		}
	}
}
