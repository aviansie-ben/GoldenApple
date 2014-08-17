package com.bendude56.goldenapple;

import java.util.List;

import org.bukkit.command.CommandExecutor;
import org.bukkit.command.PluginCommand;

public abstract class CommandManager {
    public abstract void insertCommand(String commandName, String module, CommandExecutor executor);
    public abstract ICommandInformation getCommand(String command);
    public abstract CommandExecutor getDefaultCommand();
    
    public interface ICommandInformation {
        String getName();
        List<String> getAliases();
        String getModule();
        CommandExecutor getExecutor();
        PluginCommand getCommand();
        
        void register();
        void unregister();
    }
}
