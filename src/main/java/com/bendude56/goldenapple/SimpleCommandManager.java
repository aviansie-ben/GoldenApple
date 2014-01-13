package com.bendude56.goldenapple;

import java.util.HashMap;

import org.bukkit.Bukkit;
import org.bukkit.command.CommandExecutor;

import com.bendude56.goldenapple.commands.UnloadedCommand;

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
	public CommandInformation getCommand(String command) {
		return commands.get(command);
	}

	@Override
	public CommandExecutor getDefaultCommand() {
		return defaultCommand;
	}
}
