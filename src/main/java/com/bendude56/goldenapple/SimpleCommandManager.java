package com.bendude56.goldenapple;

import java.lang.reflect.Field;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;

import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.PluginCommand;
import org.bukkit.command.SimpleCommandMap;
import org.bukkit.plugin.SimplePluginManager;

import com.bendude56.goldenapple.command.UnloadedCommand;

public class SimpleCommandManager extends CommandManager {
    public static final UnloadedCommand defaultCommand = new UnloadedCommand();
    
    private HashMap<String, CommandInformation> commands = new HashMap<String, CommandInformation>();
    
    public SimpleCommandManager() {}
    
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
        
        // TODO Fix unloading of modules not properly removing aliases
        
        @Override
        public void register() {
            command.setExecutor(executor);
            
            try {
                Field f = SimplePluginManager.class.getDeclaredField("commandMap");
                f.setAccessible(true);
                
                SimpleCommandMap commandMap = (SimpleCommandMap) f.get(Bukkit.getPluginManager());
                
                f = SimpleCommandMap.class.getDeclaredField("knownCommands");
                f.setAccessible(true);
                
                @SuppressWarnings("unchecked")
				Map<String, Command> knownCommands = (Map<String, Command>) f.get(commandMap);
                
                for (String alias : aliases) {
                    knownCommands.put(alias, command);
                }
            } catch (NoSuchFieldException | SecurityException | IllegalArgumentException | IllegalAccessException e) {
                GoldenApple.log(Level.SEVERE, "Failed to register command '/" + name + "':");
                GoldenApple.log(Level.SEVERE, e);
            }
        }
        
        @Override
        public void unregister() {
            command.setExecutor(getDefaultCommand());
            
            try {
                Field f = SimplePluginManager.class.getDeclaredField("commandMap");
                f.setAccessible(true);
                
                SimpleCommandMap commandMap = (SimpleCommandMap) f.get(Bukkit.getPluginManager());
                
                f = SimpleCommandMap.class.getDeclaredField("knownCommands");
                f.setAccessible(true);
                
                @SuppressWarnings("unchecked")
				Map<String, Command> knownCommands = (Map<String, Command>) f.get(commandMap);
                
                for (String alias : command.getAliases()) {
                    if (knownCommands.get(alias) == command) {
                        knownCommands.remove(alias);
                    }
                }
            } catch (NoSuchFieldException | SecurityException | IllegalArgumentException | IllegalAccessException e) {
                GoldenApple.log(Level.SEVERE, "Failed to register command '/" + name + "':");
                GoldenApple.log(Level.SEVERE, e);
            }
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
