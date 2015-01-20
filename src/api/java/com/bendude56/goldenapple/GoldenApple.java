package com.bendude56.goldenapple;

import java.util.logging.Level;
import java.util.logging.Logger;

import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.configuration.Configuration;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;

public abstract class GoldenApple extends JavaPlugin {
    private static Logger log = Logger.getLogger("Minecraft");
    private static GoldenApple instance;
    
    protected static void setInstance(GoldenApple instance) {
        GoldenApple.instance = instance;
    }
    
    public static GoldenApple getInstance() {
        return instance;
    }
    
    public static void logPermissionFail(User u, String command, String[] args, boolean sendMessage) {
        for (String arg : args) {
            command += " " + arg;
        }
        log(Level.WARNING, u.getName() + " attempted to perform a command (/" + command + ") but doesn't have permission!");
        if (u.getHandle() instanceof Player) {
            Location l = u.getPlayerHandle().getLocation();
            log(Level.WARNING, "Command performed at: (" + l.getX() + ", " + l.getY() + ", " + l.getZ() + ", " + l.getWorld().getName() + ")");
        }
        if (sendMessage) {
            u.sendLocalizedMessage("shared.permissionFailure");
        }
    }
    
    public static void log(Throwable e) {
        log(Level.SEVERE, e);
    }
    
    public static void log(Level level, Throwable e) {
        log.log(level, "", e);
    }
    
    public static void log(String message) {
        log(Level.INFO, message);
    }
    
    public static void log(Level level, String message) {
        log.log(level, ChatColor.stripColor("[" + getInstance().getDescription().getName() + "] " + message));
    }
    
    public static DatabaseManager getInstanceDatabaseManager() {
        return instance.getDatabaseManager();
    }
    
    public static Configuration getInstanceMainConfig() {
        return instance.getMainConfig();
    }
    
    public static PerformanceMonitor getInstancePerformanceMonitor() {
        return instance.getPerformanceMonitor();
    }
    
    /**
     * Retrieves the active localization manager for this instance of
     * GoldenApple. The localization manager handles sending localized messages
     * to users and should be used wherever possible in place of sending
     * hardcoded messages.
     * 
     * @return The active localization manager
     */
    public abstract LocalizationManager getLocalizationManager();
    
    /**
     * Retrieves the active database manager for this instance of GoldenApple.
     * The database manager handles executing queries on the database and
     * ensuring that all tables are up-to-date before they are used.
     * 
     * @return The active database manager
     */
    public abstract DatabaseManager getDatabaseManager();
    
    /**
     * Retrieves the active command manager for this instance of GoldenApple.
     * The command manager handles registration and unregistration of command
     * aliases with the Bukkit interfaces.
     * 
     * @return The active command manager
     */
    public abstract CommandManager getCommandManager();
    
    /**
     * Retrieves the active module manager for this instance of GoldenApple. The
     * module manager handles loading and unloading modules as well as managing
     * any loaded modules.
     * 
     * @return The active module manager
     */
    public abstract ModuleManager getModuleManager();
    
    public abstract PerformanceMonitor getPerformanceMonitor();
    
    /**
     * Retrieves an active reference to the main configuration file located at
     * "plugins/GoldenApple/config.yml"
     * 
     * @return A reference to the main configuration file
     */
    public abstract Configuration getMainConfig();
    
    /**
     * Retrieves an active reference to the database version configuration file
     * located at "plugins/GoldenApple/dbversion.yml". This should not be used
     * by modules, as the {@link DatabaseManager} is capable of handling
     * versioning.
     * 
     * @return A reference to the database version configuration file
     */
    public abstract Configuration getDatabaseVersionConfig();
}
