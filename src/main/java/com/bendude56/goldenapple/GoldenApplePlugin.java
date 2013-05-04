package com.bendude56.goldenapple;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.logging.Level;

import org.bukkit.Bukkit;
import org.bukkit.configuration.Configuration;
import org.bukkit.configuration.file.YamlConfiguration;

public class GoldenApplePlugin extends GoldenApple {
	public static GoldenApplePlugin getInstance() {
		return (GoldenApplePlugin)Bukkit.getServer().getPluginManager().getPlugin("GoldenApple");
	}

	private Configuration			mainConfig;
	private Configuration			databaseVersion;

	private SimpleLocalizationManager		locale;
	public SimpleDatabaseManager	database;
	public SimpleCommandManager			commands;
	public SimpleModuleManager		modules;

	@Override
	public SimpleLocalizationManager getLocalizationManager() {
		return locale;
	}

	@Override
	public DatabaseManager getDatabaseManager() {
		return database;
	}

	@Override
	public CommandManager getCommandManager() {
		return commands;
	}
	
	@Override
	public ModuleManager getModuleManager() {
		return modules;
	}

	@Override
	public Configuration getMainConfig() {
		return mainConfig;
	}

	@Override
	public Configuration getDatabaseVersionConfig() {
		return databaseVersion;
	}

	@Override
	public void onEnable() {
		setInstance(this);
		
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
				GoldenApplePlugin.log(Level.SEVERE, "Failed to load default configuration!");
				GoldenApplePlugin.log(e);
				getServer().getPluginManager().disablePlugin(this);
			}
		}
		mainConfig = YamlConfiguration.loadConfiguration(new File(this.getDataFolder() + "/config.yml"));
		try {
			if (new File(this.getDataFolder() + "/dbversion.yml").exists()) {
				databaseVersion = YamlConfiguration.loadConfiguration(new File(this.getDataFolder() + "/dbversion.yml"));
			} else {
				databaseVersion = new YamlConfiguration();
				databaseVersion.createSection("tableVersions");
				((YamlConfiguration)databaseVersion).save(new File(this.getDataFolder() + "/dbversion.yml"));
			}
		} catch (IOException e) {
			GoldenApplePlugin.log(Level.SEVERE, "Failed to load database version!");
			GoldenApplePlugin.log(e);
			getServer().getPluginManager().disablePlugin(this);
		}
		commands = new SimpleCommandManager();
		database = new SimpleDatabaseManager();
		locale = new SimpleLocalizationManager(getClassLoader());
		modules = new SimpleModuleManager();
		
		if (!modules.loadDefaults()) {
			GoldenApplePlugin.log(Level.SEVERE, "Server shutting down due to failed module load...");
			Bukkit.getServer().shutdown();
			return;
		}
	}

	@Override
	public void onDisable() {
		// Unload all modules
		modules.unloadAll();

		// Unload the database
		if (database != null) {
			database.close();
			database = null;
		}

		mainConfig = null;
	}
}