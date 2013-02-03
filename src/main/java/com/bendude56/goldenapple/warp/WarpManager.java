package com.bendude56.goldenapple.warp;

import java.io.File;
import java.io.IOException;
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
import com.bendude56.goldenapple.IModuleLoader.ModuleState;
import com.bendude56.goldenapple.User;
import com.bendude56.goldenapple.permissions.IPermissionUser;
import com.bendude56.goldenapple.permissions.PermissionManager.Permission;
import com.bendude56.goldenapple.permissions.PermissionManager.PermissionNode;
import com.bendude56.goldenapple.permissions.PermissionUser;

public class WarpManager {
	// goldenapple.warp
	public static PermissionNode warpNode;
	public static Permission backPermission;
	public static Permission importPermission;
	
	// goldenapple.warp.tp
	public static PermissionNode tpNode;
	public static Permission tpSelfToOtherPermission;
	public static Permission tpOtherToSelfPermission;
	public static Permission tpOtherToOtherPermission;
	
	// goldenapple.warp.spawn
	public static PermissionNode spawnNode;
	public static Permission spawnCurrentPermission;
	public static Permission spawnAllPermission;
	
	// goldenapple.warp.home
	public static PermissionNode homeNode;
	
	// goldenapple.warp.home.teleport
	public static PermissionNode homeTpNode;
	public static Permission homeTpOwn;
	public static Permission homeTpPublic;
	public static Permission homeTpAll;
	
	// goldenapple.warp.home.edit
	public static PermissionNode homeEditNode;
	public static Permission homeEditOwn;
	public static Permission homeEditPublic;
	public static Permission homeEditAll;
	
	public static int maxHomes;
	
	private boolean homeBusy, warpBusy;
	
	public WarpManager() {
		tryCreateTable("homes");
		maxHomes = GoldenApple.getInstance().mainConfig.getInt("modules.warps.maxHomes", 5);
	}
	
	private void tryCreateTable(String tableName) {
		try {
			GoldenApple.getInstance().database.executeFromResource(tableName.toLowerCase() + "_create");
		} catch (SQLException | IOException e) {
			GoldenApple.log(Level.SEVERE, "Failed to create table '" + tableName + "':");
			GoldenApple.log(Level.SEVERE, e);
		}
	}
	
	public boolean isHomeBusy() {
		return homeBusy;
	}
	
	public boolean isWarpBusy() {
		return warpBusy;
	}
	
	private void updateBusy() {
		 GoldenApple.modules.get("Warp").setState((homeBusy || warpBusy) ? ModuleState.BUSY : ModuleState.LOADED);
	}
	
	public HomeWarp getHome(IPermissionUser user, int homeNum) {
		try {
			ResultSet r = GoldenApple.getInstance().database.executeQuery("SELECT * FROM Homes WHERE UserID=? AND Home=?", user.getId(), homeNum);
			if (r.next()) {
				return new HomeWarp(r);
			} else {
				return null;
			}
		} catch (SQLException e) {
			GoldenApple.log(Level.WARNING, "Error while attempting to retrieve a home from the database:");
			GoldenApple.log(Level.WARNING, e);
			return null;
		}
	}
	
	public HomeWarp getHome(IPermissionUser user, String alias) {
		try {
			ResultSet r = GoldenApple.getInstance().database.executeQuery("SELECT * FROM Homes WHERE UserID=? AND Alias=?", user.getId(), alias);
			if (r.next()) {
				return new HomeWarp(r);
			} else {
				return null;
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
				GoldenApple.getInstance().locale.sendMessage(sender, "error.import.dataNotFound", false, "Essentials");
			} else {
				for (File f : userData.listFiles()) {
					if (f.getName().endsWith(".yml")) {
						OfflinePlayer player = Bukkit.getOfflinePlayer(f.getName().split("\\.")[0]);
						try {
							Configuration yml = YamlConfiguration.loadConfiguration(f);
							
							if (yml.contains("homes")) {
								int userHome = 1;
								
								numUsers++;
								if (!GoldenApple.getInstance().permissions.userExists(player.getName())) {
									newUsers++;
									GoldenApple.getInstance().permissions.createUser(player.getName());
								}
								
								PermissionUser u = GoldenApple.getInstance().permissions.getUser(player.getName());
								
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
				GoldenApple.getInstance().locale.sendMessage(sender, "general.import.homesSuccess", false, numHomes + "", numUsers + "", newUsers + "");
		}
			
	}
}
