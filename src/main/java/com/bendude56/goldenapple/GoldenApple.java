package com.bendude56.goldenapple;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.ObjectOutputStream;
import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.ChatColor;
import org.bukkit.configuration.Configuration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;

import com.bendude56.goldenapple.IModuleLoader.ModuleState;
import com.bendude56.goldenapple.antigrief.AntigriefModuleLoader;
import com.bendude56.goldenapple.area.AreaManager;
import com.bendude56.goldenapple.audit.AuditLog;
import com.bendude56.goldenapple.audit.ModuleDisableEvent;
import com.bendude56.goldenapple.audit.ModuleEnableEvent;
import com.bendude56.goldenapple.chat.ChatManager;
import com.bendude56.goldenapple.chat.ChatModuleLoader;
import com.bendude56.goldenapple.commands.UnloadedCommand;
import com.bendude56.goldenapple.lock.LockManager;
import com.bendude56.goldenapple.lock.LockModuleLoader;
import com.bendude56.goldenapple.permissions.PermissionManager;
import com.bendude56.goldenapple.permissions.PermissionsModuleLoader;
import com.bendude56.goldenapple.warp.WarpModuleLoader;

public class GoldenApple extends JavaPlugin {
	private static Logger								log			= Logger.getLogger("Minecraft");

	public static final HashMap<String, IModuleLoader>	modules		= new HashMap<String, IModuleLoader>();
	public static final String[]						loadOrder	= new String[] { "Base", "Permissions", "Lock", "Antigrief", "Chat", "Warp" };
	public static final String[]						commands	= new String[] { "gamodule", "gaverify", "gaown", "gapermissions", "galock", "gacomplex", "gaautolock", "gaspawn", "gatp", "gatphere", "gachannel", "game" };
	public static final String[]						devs		= new String[] { "ben_dude56", "Deaboy" };
	public static final UnloadedCommand					defCmd		= new UnloadedCommand();

	static {
		modules.put("Base", new BaseModuleLoader());
		modules.put("Permissions", new PermissionsModuleLoader());
		modules.put("Lock", new LockModuleLoader());
		modules.put("Antigrief", new AntigriefModuleLoader());
		modules.put("Chat", new ChatModuleLoader());
		modules.put("Warp", new WarpModuleLoader());
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
			getInstance().locale.sendMessage(u, "shared.noPermission", false);
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

	public static GoldenApple getInstance() {
		return (GoldenApple)Bukkit.getServer().getPluginManager().getPlugin("GoldenApple");
	}

	public Database				database;
	public Configuration		mainConfig;
	public PermissionManager	permissions;
	public LockManager			locks;
	public AreaManager			areas;
	public ChatManager			chat;
	public LocalizationHandler	locale;

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
		for (String cmd : commands) {
			getCommand(cmd).setExecutor(defCmd);
		}
		for (String m : loadOrder) {
			IModuleLoader module = modules.get(m);
			if (module != null && module.canLoadAuto() && module.canPolicyLoad() && module.getCurrentState() == ModuleState.UNLOADED_USER) {
				try {
					if (!enableModule(module, true)) {
						if (mainConfig.getBoolean("securityPolicy.shutdownOnFailedModuleLoad", true)) {
							GoldenApple.log(Level.SEVERE, "Server shutting down due to failed module load...");
							Bukkit.getServer().shutdown();
							return;
						}
					}
				} catch (Exception e) {
					GoldenApple.log(Level.SEVERE, "An error occured while loading module " + m + ":");
					GoldenApple.log(Level.SEVERE, e);
					if (mainConfig.getBoolean("securityPolicy.shutdownOnFailedModuleLoad", true)) {
						GoldenApple.log(Level.SEVERE, "Server shutting down due to failed module load...");
						Bukkit.getServer().shutdown();
						return;
					}
				}
			}
		}
	}

	private void verifyModuleLoad(IModuleLoader module, boolean loadDependancies) {
		if (module.getCurrentState() == IModuleLoader.ModuleState.LOADED || module.getCurrentState() == IModuleLoader.ModuleState.LOADING) {
			throw new IllegalStateException("0: Module '" + module.getModuleName() + "' already loaded");
		} else if (!module.canPolicyLoad()) {
			throw new IllegalStateException("1: Module '" + module.getModuleName() + "' blocked by policy");
		} else {
			for (String dependancy : module.getModuleDependencies()) {
				if (!modules.containsKey(dependancy)) {
					throw new IllegalStateException("2: Dependency error in module '" + module.getModuleName() + "': Dependency '" + dependancy + "' not found");
				} else if (modules.get(dependancy).getCurrentState() == ModuleState.LOADING) {
					throw new IllegalStateException("2: Dependency error in module '" + module.getModuleName() + "': Dependancy '" + dependancy + "' is currently loading");
				} else if (modules.get(dependancy).getCurrentState() != ModuleState.LOADED) {
					if (loadDependancies) {
						try {
							if (!enableModule(modules.get(dependancy), true))
								throw new IllegalStateException("2: Dependency error in module '" + module.getModuleName() + "': Dependency '" + dependancy + "' failed to load");
						} catch (Throwable e) {
							throw new IllegalStateException("2: Dependency error in module '" + module.getModuleName() + "': Dependency '" + dependancy + "' failed to load");
						}
					} else {
						throw new IllegalStateException("2: Dependency error in module '" + module.getModuleName() + "': Dependency '" + dependancy + "'not loaded");
					}
				}
			}
		}
	}

	public boolean enableModule(IModuleLoader module, boolean loadDependancies) {
		verifyModuleLoad(module, loadDependancies);
		try {
			module.loadModule(this);
			AuditLog.logEvent(new ModuleEnableEvent(module.getModuleName()));
			return true;
		} catch (Throwable e) {
			log(Level.SEVERE, "Encountered an unrecoverable error while enabling module '" + module.getModuleName() + "'");
			log(Level.SEVERE, e);
			ModuleLoadException eDump = (e instanceof ModuleLoadException) ? (ModuleLoadException)e : new ModuleLoadException(module.getModuleName(), e);
			if (mainConfig.getBoolean("securityPolicy.dumpExtendedInfo", true)) {
				try {
					File dumpFile = nextDumpFile(module.getModuleName());
					ObjectOutputStream o = new ObjectOutputStream(new FileOutputStream(dumpFile));
					try {
						o.writeObject(eDump);
					} finally {
						o.close();
					}
					log(Level.SEVERE, "Technical information dumped to " + dumpFile.getCanonicalPath());
				} catch (Throwable e2) {
					log(Level.SEVERE, "An error occured while dumping technical information:");
					log(Level.SEVERE, e2);
				}
			}
			return false;
		}
	}

	public boolean disableModule(IModuleLoader module, boolean force) {
		if (!force && module.getCurrentState() != ModuleState.LOADED)
			throw new IllegalStateException("Module '" + module.getModuleName() + "' was not in an expected state to be disabled");
		for (Entry<String, IModuleLoader> checkDepend : modules.entrySet()) {
			if (checkDepend.getValue().getCurrentState() != ModuleState.LOADED)
				continue;
			for (String depend : checkDepend.getValue().getModuleDependencies()) {
				if (depend.equals(module.getModuleName())) {
					disableModule(checkDepend.getValue(), force);
				}
			}
		}
		try {
			module.unloadModule(this);
		} catch (Throwable e) {
			log(Level.WARNING, "Module '" + module.getModuleName() + "' threw an exception while unloading:");
			log(Level.WARNING, e);
		}
		if (module.getCurrentState() != ModuleState.UNLOADED_USER) {
			if (force) {
				log(Level.SEVERE, "Module '" + module.getModuleName() + "' is not in an expected state after unloading. Forcing shutdown...");
				module.setState(ModuleState.UNLOADED_USER);
			} else {
				log(Level.SEVERE, "Module '" + module.getModuleName() + "' is not in an expected state after unloading.");
				return false;
			}
		}
		AuditLog.logEvent(new ModuleDisableEvent(module.getModuleName()));
		return true;
	}

	private File nextDumpFile(String module) throws IOException {
		if (!new File(this.getDataFolder() + "/dumps").exists()) {
			new File(this.getDataFolder() + "/dumps").mkdirs();
		}
		for (int i = 1;; i++) {
			File f = new File(this.getDataFolder() + "/dumps/mcrash-" + module + "-" + i + ".dmp");
			if (!f.exists()) {
				f.createNewFile();
				return f;
			}
		}
	}

	@Override
	public void onDisable() {
		// Unload all modules
		for (Map.Entry<String, IModuleLoader> module : modules.entrySet()) {
			if (module.getValue().getCurrentState() == ModuleState.LOADED) {
				disableModule(module.getValue(), true);
			}
		}
		
		// Unload the database
		if (database != null) {
			database.close();
			database = null;
		}
		
		mainConfig = null;
	}
}
