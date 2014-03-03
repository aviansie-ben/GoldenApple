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
	public SimplePerformanceMonitor monitor;

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
	public PerformanceMonitor getPerformanceMonitor() {
		return monitor;
	}

	@Override
	public void onEnable() {
		setInstance(this);
		
		if (!new File(this.getDataFolder() + "/config.yml").exists()) {
			// The config file wasn't found in the plugin directory
			try {
				// Create the proper directory structure
				this.getDataFolder().mkdirs();
				
				// Load the default config file from the resources folder
				InputStream i = getClassLoader().getResourceAsStream("config/config.yml");
				FileOutputStream o = new FileOutputStream(new File(this.getDataFolder() + "/config.yml"));
				
				// Copy all of the defaults into a new config file
				byte[] buffer = new byte[1024];
				int len;
				while ((len = i.read(buffer, 0, 1024)) > 0) {
					o.write(buffer, 0, len);
				}
				
				i.close();
				o.close();
			} catch (IOException e) {
				// We couldn't create a new config file
				GoldenApple.log(Level.SEVERE, "Failed to load default configuration!");
				GoldenApple.log(e);
				getServer().getPluginManager().disablePlugin(this);
			}
		}
		
		// Load in the main config
		mainConfig = YamlConfiguration.loadConfiguration(new File(this.getDataFolder() + "/config.yml"));
		
		
		try {
			if (new File(this.getDataFolder() + "/dbversion.yml").exists()) {
				// Load the existing database versioning configuration
				databaseVersion = YamlConfiguration.loadConfiguration(new File(this.getDataFolder() + "/dbversion.yml"));
			} else {
				// Create a new database versioning configuration
				databaseVersion = new YamlConfiguration();
				databaseVersion.createSection("tableVersions");
				((YamlConfiguration)databaseVersion).save(new File(this.getDataFolder() + "/dbversion.yml"));
			}
		} catch (IOException e) {
			// The database versioning config file couldn't be read or couldn't be created
			GoldenApple.log(Level.SEVERE, "Failed to load database version!");
			GoldenApple.log(e);
			getServer().getPluginManager().disablePlugin(this);
		}
		
		// Instantiates all of the managers necessary for base system functions
		commands = new SimpleCommandManager();
		database = new SimpleDatabaseManager();
		locale = new SimpleLocalizationManager(getClassLoader());
		modules = new SimpleModuleManager();
		monitor = new SimplePerformanceMonitor(this);
		
		// Verify that the database connected successfully
		if (!database.isConnected()) {
			// If the database could not be contacted, stop the server gracefully
			GoldenApple.log(Level.SEVERE, "Server shutting down due to failed database connection...");
			Bukkit.getServer().shutdown();
			return;
		}
		
		// Tell the module loader to load all default modules
		if (!modules.loadDefaults()) {
			// If a module load failed in a fatal manner, stop the server
			GoldenApple.log(Level.SEVERE, "Server shutting down due to failed module load...");
			Bukkit.getServer().shutdown();
			return;
		}
	}

	@Override
	public void onDisable() {
		// Unload all modules
		if (modules != null) {
			modules.unloadAll();
			modules = null;
		}

		// Unload the database
		if (database != null) {
			database.close();
			database = null;
		}
		
		monitor.close();

		mainConfig = null;
	}
}
