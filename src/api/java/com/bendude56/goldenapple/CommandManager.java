package com.bendude56.goldenapple;

import java.util.List;

import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.PluginCommand;

import com.bendude56.goldenapple.command.GoldenAppleCommand;

public abstract class CommandManager {
    public abstract void insertCommand(String commandName, String module, CommandExecutor executor);
    public abstract ICommandInformation getCommand(String command);
    public abstract CommandExecutor getDefaultCommand();
    
    public abstract boolean isWorkerRunning();
    public abstract void startWorkers(int numWorkers);
    public abstract void stopWorkers();
    
    public abstract void queueWorkerCommand(GoldenAppleCommand command, CommandSender sender, Command bCommand, String commandLabel, String[] args);
    
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
