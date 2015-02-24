package com.bendude56.goldenapple;

import java.lang.reflect.Field;
import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Deque;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;

import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.PluginCommand;
import org.bukkit.command.SimpleCommandMap;
import org.bukkit.plugin.SimplePluginManager;

import com.bendude56.goldenapple.command.GoldenAppleCommand;
import com.bendude56.goldenapple.command.UnloadedCommand;

public class SimpleCommandManager extends CommandManager {
    public static final UnloadedCommand defaultCommand = new UnloadedCommand();
    
    private HashMap<String, CommandInformation> commands = new HashMap<String, CommandInformation>();
    
    private List<CommandWorker> workers = new ArrayList<CommandWorker>();
    private Deque<QueuedCommand> commandQueue = new ArrayDeque<QueuedCommand>();
    
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
    
    private void removeDeadWorkers() {
        Iterator<CommandWorker> workerIterator = workers.iterator();
        
        while (workerIterator.hasNext()) {
            CommandWorker worker = workerIterator.next();
            
            if (!worker.isAlive()) {
                workerIterator.remove();
            }
        }
    }
    
    @Override
    public boolean isWorkerRunning() {
        this.removeDeadWorkers();
        return this.workers.size() > 0;
    }

    @Override
    public void startWorkers(int numWorkers) {
        for (int i = 0; i < numWorkers; i++) {
            CommandWorker w = new CommandWorker("GoldenApple Command Worker #" + (this.workers.size() + 1));
            
            this.workers.add(w);
            w.start();
        }
    }

    @Override
    public void stopWorkers() {
        for (CommandWorker worker : this.workers) {
            try {
                worker.interrupt();
                worker.join(5000);
                
                if (worker.isAlive()) {
                    GoldenApple.log(Level.SEVERE, "Could not terminate '" + worker.getName() + "'! Command '" + worker.currentCommand + "' is deadlocked!");
                }
            } catch (InterruptedException e) {
                throw new RuntimeException(e);
            }
        }
        
        this.workers.clear();
    }

    @Override
    public void queueWorkerCommand(GoldenAppleCommand command, CommandSender sender, Command bCommand, String commandLabel, String[] args) {
        synchronized (this.commandQueue) {
            this.commandQueue.addLast(new QueuedCommand(command, sender, bCommand, commandLabel, args));
            this.commandQueue.notify();
        }
    }
    
    public void sendWorkerDebugInformation(User u) {
        this.removeDeadWorkers();
        
        u.sendMessage("Command queue size: " + this.commandQueue.size());
        u.sendMessage("Number of command workers: " + this.workers.size());
        
        for (CommandWorker worker : this.workers) {
            String command = worker.currentCommand;
            u.sendMessage("  " + worker.getName() + ": " + ((command == null) ? "Idle" : command));
        }
    }
    
    private class QueuedCommand {
        public final GoldenAppleCommand command;
        public final CommandSender sender;
        public final Command bCommand;
        public final String commandLabel;
        public final String[] args;
        
        public QueuedCommand(GoldenAppleCommand command, CommandSender sender, Command bCommand, String commandLabel, String[] args) {
            super();
            this.command = command;
            this.sender = sender;
            this.bCommand = bCommand;
            this.commandLabel = commandLabel;
            this.args = args;
        }
        
        public void execute() {
            command.execute(sender, bCommand, commandLabel, args);
        }
        
    }
    
    private class CommandWorker extends Thread {
        private volatile String currentCommand = null;
        
        public CommandWorker(String name) {
            super(name);
        }

        @Override
        public void run() {
            while (true) {
                try {
                    QueuedCommand command = null;
                    synchronized (SimpleCommandManager.this.commandQueue) {
                        while (SimpleCommandManager.this.commandQueue.isEmpty()) {
                            SimpleCommandManager.this.commandQueue.wait(1000);
                        }
                        
                        command = SimpleCommandManager.this.commandQueue.removeFirst();
                    }
                    
                    this.currentCommand = this.getCurrentCommand(command.commandLabel, command.args);
                    command.execute();
                    this.currentCommand = null;
                } catch (InterruptedException e) {
                    return;
                }
            }
        }
        
        private String getCurrentCommand(String commandLabel, String[] args) {
            StringBuilder b = new StringBuilder("/");
            b.append(commandLabel);
            
            for (String a : args) {
                b.append(" " + a);
            }
            
            return b.toString();
        }
        
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
