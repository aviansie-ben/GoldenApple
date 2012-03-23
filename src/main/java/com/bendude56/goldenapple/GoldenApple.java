package com.bendude56.goldenapple;

import java.util.logging.Level;
import java.util.logging.Logger;

import org.bukkit.Bukkit;
import org.bukkit.plugin.java.JavaPlugin;

public class GoldenApple extends JavaPlugin {
	private static Logger	log	= Logger.getLogger("Minecraft");

	public static void log(Exception e) {
		log(Level.SEVERE, e.toString());
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

	@Override
	public void onEnable() {}

	@Override
	public void onDisable() {}
}
