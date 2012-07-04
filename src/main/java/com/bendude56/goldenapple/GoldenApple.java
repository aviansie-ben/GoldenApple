package com.bendude56.goldenapple;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.HashMap;
import java.util.Map.Entry;
import java.util.HashSet;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.configuration.Configuration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;

import com.bendude56.goldenapple.area.AreaManager;
import com.bendude56.goldenapple.lock.LockManager;
import com.bendude56.goldenapple.lock.LockModuleLoader;
import com.bendude56.goldenapple.permissions.PermissionManager;
import com.bendude56.goldenapple.permissions.PermissionsModuleLoader;
import com.bendude56.goldenapple.warps.WarpManager;

public class GoldenApple extends JavaPlugin {
	private static Logger	log	= Logger.getLogger("Minecraft");
	
	public static final HashMap<String, IModuleLoader> modules = new HashMap<String, IModuleLoader>();
	
	static {
		modules.put("Base", new BaseModuleLoader());
		modules.put("Permissions", new PermissionsModuleLoader());
		modules.put("Lock", new LockModuleLoader());
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
			getInstance().locale.sendMessage(u, "basic.noPermission", false);
		}
	}

	public static void log(Throwable e) {
		log(Level.SEVERE, e.toString());
	}

	public static void log(Level level, Throwable e) {
		log(level, e.toString());
	}

	public static void log(String message) {
		log(Level.INFO, message);
	}

	public static void log(Level level, String message) {
		log.log(level, ChatColor.stripColor("[" + getInstance().getDescription().getName() + "] " + message));
	}

	public static GoldenApple getInstance() {
		return (GoldenApple)Bukkit.getServer().getPluginManager().getPlugin("GoldenApple");
	}

	public Database				database;
	public Configuration		mainConfig;
	public PermissionManager	permissions;
	public LockManager			locks;
	public AreaManager 			areas;
	public WarpManager			warps;
	public LocalizationHandler	locale;

	public HashSet<Byte> getTransparentBlocks() {
		HashSet<Byte> blockList = new HashSet<Byte>(32);
				blockList.add(((Integer) Material.AIR.getId()).byteValue());
				blockList.add(((Integer) Material.BREWING_STAND.getId()).byteValue());
				blockList.add(((Integer) Material.BROWN_MUSHROOM.getId()).byteValue());
				blockList.add(((Integer) Material.CAKE.getId()).byteValue());
				blockList.add(((Integer) Material.CROPS.getId()).byteValue());
				blockList.add(((Integer) Material.DETECTOR_RAIL.getId()).byteValue());
				blockList.add(((Integer) Material.DIODE_BLOCK_ON.getId()).byteValue());
				blockList.add(((Integer) Material.DIODE_BLOCK_OFF.getId()).byteValue());
				blockList.add(((Integer) Material.LADDER.getId()).byteValue());
				blockList.add(((Integer) Material.LAVA.getId()).byteValue());
				blockList.add(((Integer) Material.LEVER.getId()).byteValue());
				blockList.add(((Integer) Material.LONG_GRASS.getId()).byteValue());
				blockList.add(((Integer) Material.MELON_STEM.getId()).byteValue());
				blockList.add(((Integer) Material.NETHER_STALK.getId()).byteValue());
				blockList.add(((Integer) Material.PAINTING.getId()).byteValue());
				blockList.add(((Integer) Material.PORTAL.getId()).byteValue());
				blockList.add(((Integer) Material.POWERED_RAIL.getId()).byteValue());
				blockList.add(((Integer) Material.PUMPKIN_STEM.getId()).byteValue());
				blockList.add(((Integer) Material.RAILS.getId()).byteValue());
				blockList.add(((Integer) Material.RED_MUSHROOM.getId()).byteValue());
				blockList.add(((Integer) Material.RED_ROSE.getId()).byteValue());
				blockList.add(((Integer) Material.REDSTONE_TORCH_ON.getId()).byteValue());
				blockList.add(((Integer) Material.REDSTONE_TORCH_OFF.getId()).byteValue());
				blockList.add(((Integer) Material.REDSTONE_WIRE.getId()).byteValue());
				blockList.add(((Integer) Material.SAPLING.getId()).byteValue());
				blockList.add(((Integer) Material.SIGN_POST.getId()).byteValue());
				blockList.add(((Integer) Material.SNOW.getId()).byteValue());
				blockList.add(((Integer) Material.TORCH.getId()).byteValue());
				blockList.add(((Integer) Material.VINE.getId()).byteValue());
				blockList.add(((Integer) Material.WALL_SIGN.getId()).byteValue());
				blockList.add(((Integer) Material.WATER.getId()).byteValue());
				blockList.add(((Integer) Material.YELLOW_FLOWER.getId()).byteValue());
		return blockList;
	}

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
		locale = new LocalizationHandler(getClassLoader());
		for (Entry<String, IModuleLoader> module : modules.entrySet()) {
			enableModule(module.getValue());
		}
	}
	
	private void verifyModuleLoad(IModuleLoader module) {
		if (module.getCurrentState() == IModuleLoader.ModuleState.LOADED || module.getCurrentState() == IModuleLoader.ModuleState.LOADING)
			throw new IllegalStateException("Module '" + module.getModuleName() + "' is already loading/loaded!");
		if (!module.canPolicyLoad())
			throw new IllegalStateException("Module '" + module.getModuleName() + "' is not allowed to load due to the security policy!");
	}

	private void enableModule(IModuleLoader module) {
		verifyModuleLoad(module);
		try {
			
		} catch (Throwable e) {
			log(Level.SEVERE, "Encountered an unrecoverable error while enabling module '" + module.getModuleName() + "'");
			log(Level.SEVERE, e);
			ModuleLoadException eDump = (e instanceof ModuleLoadException) ? (ModuleLoadException) e : new ModuleLoadException(module.getModuleName(), e);
			if (mainConfig.getBoolean("securityPolicy.dumpExtendedInfo", true)) {
				try {
					File dumpFile = nextDumpFile(module.getModuleName());
					log(Level.SEVERE, "In order to prevent security breaches, the server will now shut down. Technical information is stored in dump file at '" + dumpFile.getCanonicalPath() + "'");
				} catch (Throwable e2) {
					log(Level.SEVERE, "In order to prevent security breaches, the server will now shut down. Technical information dump failed...");
					log(Level.SEVERE, e2);
				}
			} else {
				
			}
		}
	}
	
	private File nextDumpFile(String module) throws IOException {
		if (!new File(this.getDataFolder() + "/dumps").exists()) {
			new File(this.getDataFolder() + "/dumps").mkdirs();
		}
		for (int i = 1; ; i++) {
			File f = new File(this.getDataFolder() + "/dumps/mcrash-" + module + "-" + i + ".dmp");
			if (!f.exists()) {
				f.createNewFile();
				return f;
			}
		}
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
