package com.bendude56.goldenapple;

import java.util.logging.Level;
import java.util.logging.Logger;

import org.bukkit.plugin.java.JavaPlugin;

public class GoldenApple extends JavaPlugin {
	
	private Logger log = Logger.getLogger("Minecraft");
	
	@Override
	public void onEnable() {
		
	}
	
	@Override
	public void onDisable() {
		
	}
	
	public void log(Level level, String message) {
		log.log(level, "[" + this.getDescription().getName() + "] " + message);
	}
}