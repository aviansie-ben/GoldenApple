package com.bendude56.goldenapple.warp;

import java.io.File;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Map;
import java.util.logging.Level;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.OfflinePlayer;
import org.bukkit.configuration.Configuration;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.YamlConfiguration;
import com.bendude56.goldenapple.GoldenApple;
import com.bendude56.goldenapple.ModuleLoader.ModuleState;
import com.bendude56.goldenapple.User;
import com.bendude56.goldenapple.permissions.IPermissionUser;
import com.bendude56.goldenapple.permissions.PermissionManager;

public class SimpleWarpManager extends WarpManager {
	private static int maxHomes;
	
	private boolean homeBusy, warpBusy;
	
	public SimpleWarpManager() {
		GoldenApple.getInstanceDatabaseManager().createOrUpdateTable("homes");
		maxHomes = GoldenApple.getInstanceMainConfig().getInt("modules.warps.maxHomes", 5);
	}
	
	public int getMaxHomes() {
		return maxHomes;
	}
	
	public boolean isHomeBusy() {
		return homeBusy;
	}
	
	public boolean isWarpBusy() {
		return warpBusy;
	}
	
	private void updateBusy() {
		 GoldenApple.getInstance().getModuleManager().getModule("Warp").setState((homeBusy || warpBusy) ? ModuleState.BUSY : ModuleState.LOADED);
	}
	
	public BaseWarp getHome(IPermissionUser user, int homeNum) {
		try {
			ResultSet r = GoldenApple.getInstanceDatabaseManager().executeQuery("SELECT * FROM Homes WHERE UserID=? AND Home=?", user.getId(), homeNum);
			try {
				if (r.next()) {
					return new HomeWarp(r);
				} else {
					return null;
				}
			} finally {
				GoldenApple.getInstanceDatabaseManager().closeResult(r);
			}
		} catch (SQLException e) {
			GoldenApple.log(Level.WARNING, "Error while attempting to retrieve a home from the database:");
			GoldenApple.log(Level.WARNING, e);
			return null;
		}
	}
	
	public BaseWarp getHome(IPermissionUser user, String alias) {
		try {
			ResultSet r = GoldenApple.getInstanceDatabaseManager().executeQuery("SELECT * FROM Homes WHERE UserID=? AND Alias=?", user.getId(), alias);
			try {
				if (r.next()) {
					return new HomeWarp(r);
				} else {
					return null;
				}
			} finally {
				GoldenApple.getInstanceDatabaseManager().closeResult(r);
			}
		} catch (SQLException e) {
			GoldenApple.log(Level.WARNING, "Error while attempting to retrieve a home from the database:");
			GoldenApple.log(Level.WARNING, e);
			return null;
		}
	}
	
	public void importHomesFromEssentials(User sender) {
		Bukkit.getScheduler().runTaskAsynchronously(GoldenApple.getInstance(), new EssentialsImportHomes(sender));
	}
	
	private class EssentialsImportHomes implements Runnable {
		
		private User sender;
		
		public EssentialsImportHomes(User sender) {
			this.sender = sender;
		}

		@Override
		public void run() {
			int numHomes = 0, numUsers = 0, newUsers = 0;
			
			homeBusy = true;
			updateBusy();
			
			File userData = new File("plugins/Essentials/userdata");
			
			if (!userData.exists() || !userData.isDirectory()) {
				sender.sendLocalizedMessage("error.import.dataNotFound", "Essentials");
			} else {
				for (File f : userData.listFiles()) {
					if (f.getName().endsWith(".yml")) {
						OfflinePlayer player = Bukkit.getOfflinePlayer(f.getName().split("\\.")[0]);
						try {
							Configuration yml = YamlConfiguration.loadConfiguration(f);
							
							if (yml.contains("homes")) {
								int userHome = 1;
								
								numUsers++;
								if (!PermissionManager.getInstance().userExists(player.getName())) {
									newUsers++;
									PermissionManager.getInstance().createUser(player.getName());
								}
								
								IPermissionUser u = PermissionManager.getInstance().getUser(player.getName());
								
								for (Map.Entry<String, Object> s : yml.getConfigurationSection("homes").getValues(false).entrySet()) {
									if (s.getValue() instanceof ConfigurationSection) {
										ConfigurationSection section = (ConfigurationSection)s.getValue();
										Location l = new Location(Bukkit.getWorld(section.getString("world")), section.getDouble("x"), section.getDouble("y"), section.getDouble("z"),
												(float)section.getDouble("yaw"), (float)section.getDouble("pitch"));
										if (l.getWorld() != null) { // Ignore homes in missing worlds
											HomeWarp h = new HomeWarp(u.getId(), userHome, l, s.getKey(), false);
											h.delete();
											h.insert();
											userHome++;
											numHomes++;
										}
									}
								}
							}
						} catch (Throwable e) {
							GoldenApple.log(Level.WARNING, "Failed to import Essentials data for user " + player.getName() + ":");
							GoldenApple.log(Level.WARNING, e);
						}
					}
				}
			}
			
			homeBusy = false;
			updateBusy();
			
			if (sender != null)
				sender.sendLocalizedMessage("general.import.homesSuccess", numHomes + "", numUsers + "", newUsers + "");
		}
			
	}
}
