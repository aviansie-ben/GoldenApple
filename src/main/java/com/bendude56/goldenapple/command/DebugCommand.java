package com.bendude56.goldenapple.command;

import java.io.File;

import org.bukkit.Location;
import org.bukkit.command.PluginCommand;
import org.bukkit.configuration.Configuration;
import org.bukkit.configuration.file.YamlConfiguration;

import com.bendude56.goldenapple.CommandManager.ICommandInformation;
import com.bendude56.goldenapple.GoldenApple;
import com.bendude56.goldenapple.GoldenApplePlugin;
import com.bendude56.goldenapple.ModuleLoader;
import com.bendude56.goldenapple.ModuleLoader.ModuleState;
import com.bendude56.goldenapple.antigrief.AntigriefListener;
import com.bendude56.goldenapple.lock.LockManager;
import com.bendude56.goldenapple.lock.SimpleLockManager;
import com.bendude56.goldenapple.permissions.PermissionManager;
import com.bendude56.goldenapple.permissions.SimplePermissionManager;
import com.bendude56.goldenapple.punish.PunishmentManager;
import com.bendude56.goldenapple.punish.SimplePunishmentManager;
import com.bendude56.goldenapple.select.SelectManager;
import com.bendude56.goldenapple.SimpleLocalizationManager;
import com.bendude56.goldenapple.User;

public class DebugCommand extends GoldenAppleCommand {
	@Override
	public boolean onExecute(GoldenApple instance, User user, String commandLabel, String[] args) {
		if (!instance.getMainConfig().getBoolean("global.debugCommand", false)) {
			user.sendLocalizedMessage("error.debug.disabled");
			return true;
		} else if (!user.getHandle().isOp()) {
			GoldenApple.logPermissionFail(user, commandLabel, args, true);
			return true;
		} else if (args.length == 0) {
			return false;
		}
		
		if (args[0].equalsIgnoreCase("localelist")) {
			for (String locale : ((SimpleLocalizationManager)instance.getLocalizationManager()).secondaryMessages.keySet()) {
				user.getHandle().sendMessage(locale);
			}
		} else if (args[0].equalsIgnoreCase("cachepurge")) {
			for (ModuleLoader l : instance.getModuleManager().getModules()) {
				l.clearCache();
			}
			user.getHandle().sendMessage("All caches purged");
		} else if (args[0].equalsIgnoreCase("reloadconfig")) {
			Configuration newConfig = YamlConfiguration.loadConfiguration(new File(instance.getDataFolder() + "/config.yml"));
			
			if (newConfig != null) {
				((GoldenApplePlugin) instance).mainConfig = newConfig;
				user.getHandle().sendMessage("Config reloaded");
			} else {
				user.getHandle().sendMessage("Error reloading config");
			}
		} else if (args[0].equalsIgnoreCase("forceown")) {
			if (instance.getModuleManager().getModule("Permissions").getCurrentState() == ModuleState.LOADED) {
				user.addPermission(PermissionManager.getInstance().getRootNode().getStarPermission());
				user.getHandle().sendMessage("All permissions given");
			} else {
				user.getHandle().sendMessage("Permissions module not loaded");
			}
		} else if (args[0].equalsIgnoreCase("bprefresh")) {
			if (instance.getModuleManager().getModule("Permissions").getCurrentState() == ModuleState.LOADED) {
				User target = user;
				
				if (args.length >= 2) {
					target = User.findUser(args[1]);
					
					if (target == null) {
						user.getHandle().sendMessage("User not online: " + args[1]);
					}
				}
				
				if (target != null) {
					target.registerBukkitPermissions();
					user.getHandle().sendMessage("Bukkit permissions refreshed for " + target.getName());
				}
			} else {
				user.getHandle().sendMessage("Permissions module not loaded");
			}
		} else if (args[0].equalsIgnoreCase("ptest")) {
			if (args.length == 2 || args.length == 3) {
				User target = user;
				
				if (args.length >= 3) {
					target = User.findUser(args[2]);
					
					if (target == null) {
						user.getHandle().sendMessage("User not online: " + args[2]);
					}
				}
				
				if (target != null) {
					if (target.hasPermission(args[1]))
						user.getHandle().sendMessage("User " + target.getName() + " has permission " + args[1]);
					else
						user.getHandle().sendMessage("User " + target.getName() + " does not have permission " + args[1]);
				}
			} else {
				user.getHandle().sendMessage("Missing permission");
			}
		} else if (args[0].equalsIgnoreCase("agtnt")) {
			if (instance.getModuleManager().getModule("Antigrief").getCurrentState() != ModuleState.LOADED) {
				user.getHandle().sendMessage("Antigrief not loaded");
			} else if (AntigriefListener.isTntLoaded()) {
				user.getHandle().sendMessage("Antigrief loaded and TNT replaced");
			} else {
				user.getHandle().sendMessage("Antigrief loaded, but TNT not replaced");
			}
		} else if (args[0].equalsIgnoreCase("cachesize")) {
			if (instance.getModuleManager().getModule("Permissions").getCurrentState() == ModuleState.LOADED) {
				SimplePermissionManager m = ((SimplePermissionManager) PermissionManager.getInstance());
				user.getHandle().sendMessage("Offline Users: " + m.getUserCacheCurrentSize() + "/" + m.getUserCacheMaxSize());
				user.getHandle().sendMessage("Sticky Users: " + m.getUserCacheStickyCount());
			}
			
			if (instance.getModuleManager().getModule("Lock").getCurrentState() == ModuleState.LOADED) {
				SimpleLockManager m = ((SimpleLockManager) LockManager.getInstance());
				user.getHandle().sendMessage("Locks: " + m.getLockCacheCurrentSize() + "/" + m.getLockCacheMaxSize());
			}
			
			if (instance.getModuleManager().getModule("Punish").getCurrentState() == ModuleState.LOADED) {
				SimplePunishmentManager m = ((SimplePunishmentManager) PunishmentManager.getInstance());
				user.getHandle().sendMessage("Punishment Lookup: " + m.getLookupCacheCurrentSize());
			}
		} else if (args[0].equalsIgnoreCase("commandinfo")) {
			if (args.length == 2) {
				String cmd = args[1].toLowerCase();
				PluginCommand pcmd = instance.getCommand(cmd);
				ICommandInformation i = (pcmd == null) ? null : instance.getCommandManager().getCommand(pcmd.getName());
				
				if (i == null && instance.getCommand(cmd) != null) {
					user.getHandle().sendMessage("GoldenApple command not registered with command manager");
				} else if (i == null) {
					user.getHandle().sendMessage("Command not found or not a GoldenApple command");
				} else {
					user.getHandle().sendMessage("Command: /" + i.getName());
					user.getHandle().sendMessage("Module: " + i.getModule());
					if (i.getCommand().getExecutor() == i.getExecutor())
						user.getHandle().sendMessage("This command is active");
					else
						user.getHandle().sendMessage("This command is inactive");
				}
			} else {
				user.getHandle().sendMessage("Missing command");
			}
		} else if (args[0].equalsIgnoreCase("getvar")) {
		    if (args.length == 2 || args.length == 3) {
		        User target = user;
                
                if (args.length >= 3) {
                    target = User.findUser(args[2]);
                    
                    if (target == null) {
                        user.getHandle().sendMessage("User not online: " + args[2]);
                    }
                }
                
                if (target != null) {
                    String effective = target.getVariableString(args[1]);
                    String userVal = target.getVariableSpecificString(args[1]);
                    String serverDefault = PermissionManager.getInstance().getVariableDefaultValue(args[1]);
                    
                    user.getHandle().sendMessage("Testing variable " + args[1] + " on user " + target.getName());
                    
                    if (effective == null) {
                        user.getHandle().sendMessage("Effective Value: <null>");
                    } else {
                        user.getHandle().sendMessage("Effective Value: " + effective);
                    }
                    
                    if (userVal == null) {
                        user.getHandle().sendMessage("User-Specific Value: <null>");
                    } else {
                        user.getHandle().sendMessage("User-Specific Value: " + userVal);
                    }
                    
                    if (serverDefault == null) {
                        user.getHandle().sendMessage("Default Value: <null>");
                    } else {
                        user.getHandle().sendMessage("Default Value: " + serverDefault);
                    }
                }
		    } else {
		        user.getHandle().sendMessage("Missing variable");
		    }
		} else if (args[0].equalsIgnoreCase("selection")) {
            if (GoldenApple.getInstance().getModuleManager().getModule("Select").getCurrentState() != ModuleState.LOADED) {
                user.getHandle().sendMessage("The 'Select' module has not been loaded!");
            } else if (!SelectManager.getInstance().isSelectionMade(user)) {
                user.getHandle().sendMessage("You have not yet made a selection!");
            } else {
                Location min = SelectManager.getInstance().getSelectionMinimum(user);
                Location max = SelectManager.getInstance().getSelectionMaximum(user);
                
                user.getHandle().sendMessage("You have selected the following region:");
                user.getHandle().sendMessage("  Min: (" + min.getBlockX() + ", " + min.getBlockY() + ", " + min.getBlockZ() + ")");
                user.getHandle().sendMessage("  Max: (" + max.getBlockX() + ", " + max.getBlockY() + ", " + max.getBlockZ() + ")");
                user.getHandle().sendMessage("  World: " + min.getWorld().getName());
            }
		} else {
			user.getHandle().sendMessage("Bad command: " + args[0]);
		}
		
		return true;
	}
}
