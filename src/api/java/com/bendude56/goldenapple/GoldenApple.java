package com.bendude56.goldenapple;

import java.util.logging.Level;
import java.util.logging.Logger;

import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.configuration.Configuration;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;

public abstract class GoldenApple extends JavaPlugin {
	private static Logger		log	= Logger.getLogger("Minecraft");
	private static GoldenApple	instance;

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
			u.sendLocalizedMessage("shared.noPermission");
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

	public abstract LocalizationManager getLocalizationManager();
	public abstract DatabaseManager getDatabaseManager();
	public abstract CommandManager getCommandManager();
	public abstract ModuleManager getModuleManager();

	public abstract Configuration getMainConfig();
	public abstract Configuration getDatabaseVersionConfig();
}
