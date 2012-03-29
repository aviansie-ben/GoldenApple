package com.bendude56.goldenapple;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.bukkit.Bukkit;
import org.bukkit.configuration.Configuration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.plugin.java.JavaPlugin;

import com.bendude56.goldenapple.commands.PermissionsCommand;
import com.bendude56.goldenapple.permissions.PermissionManager;

public class GoldenApple extends JavaPlugin {
	private static Logger	log	= Logger.getLogger("Minecraft");

	public static void log(Exception e) {
		log(Level.SEVERE, e.toString());
	}
	
	public static void log(Level level, Exception e) {
		log(level, e.toString());
	}

	public static void log(String message) {
		log(Level.INFO, message);
	}

	public static void log(Level level, String message) {
		log.log(level, "[" + getInstance().getDescription().getName() + "] " + message);
	}
	
	public static GoldenApple getInstance() {
		return (GoldenApple) Bukkit.getServer().getPluginManager().getPlugin("GoldenApple");
	}
	
	public Database database;
	public Configuration mainConfig;
	public PermissionManager permissions;
	public LocalizationHandler locale;

	@Override
	public void onEnable() {
		if (!new File(this.getDataFolder() + "/config.yml").exists()) {
			try {
				this.getDataFolder().mkdirs();
				InputStream i = getClassLoader().getResourceAsStream("config/config.yml");
				FileOutputStream o = new FileOutputStream(new File(this.getDataFolder() + "/config.yml"));
				byte[] buffer = new byte[1024];
				int len;
				while ((len = i.read(buffer, 0, 1024)) > 0) {
					o.write(buffer, 0, len);
				}
				i.close();
				o.close();
			} catch (IOException e) {
				GoldenApple.log(Level.SEVERE, "Failed to load default configuration!");
				GoldenApple.log(e);
				getServer().getPluginManager().disablePlugin(this);
			}
		}
		mainConfig = YamlConfiguration.loadConfiguration(new File(this.getDataFolder() + "/config.yml"));
		database = new Database();
		permissions = new PermissionManager();
		locale = new LocalizationHandler(getClassLoader());
		registerCommands();
	}
	
	private void registerCommands() {
		getCommand("permissions").setExecutor(new PermissionsCommand());
	}

	@Override
	public void onDisable() {
		permissions.close();
		permissions = null;
		database.close();
		database = null;
		mainConfig = null;
	}
}
