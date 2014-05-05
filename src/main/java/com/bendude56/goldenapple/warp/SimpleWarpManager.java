package com.bendude56.goldenapple.warp;

import java.io.File;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.OfflinePlayer;
import org.bukkit.configuration.Configuration;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.YamlConfiguration;

import com.bendude56.goldenapple.GoldenApple;
import com.bendude56.goldenapple.User;
import com.bendude56.goldenapple.permissions.IPermissionUser;
import com.bendude56.goldenapple.permissions.PermissionManager;

public class SimpleWarpManager extends WarpManager {
	private static int maxHomes;
	private static int teleportCooldownTime, deathCooldownTime;
	
	private boolean homeBusy, warpBusy;
	private HashMap<Long, Integer> teleportCooldown, deathCooldown;
	private int cooldownTimer;
	
	public SimpleWarpManager() {
		GoldenApple.getInstanceDatabaseManager().createOrUpdateTable("homes");
		GoldenApple.getInstanceDatabaseManager().createOrUpdateTable("warps");
		GoldenApple.getInstanceDatabaseManager().createOrUpdateTable("warpgroups");
		
		maxHomes = GoldenApple.getInstanceMainConfig().getInt("modules.warps.maxHomes", 5);
		teleportCooldownTime = GoldenApple.getInstanceMainConfig().getInt("modules.warps.teleportCooldown", 60);
		deathCooldownTime = GoldenApple.getInstanceMainConfig().getInt("modules.warps.deathCooldown", 60);
		
		teleportCooldown = new HashMap<Long, Integer>();
		deathCooldown = new HashMap<Long, Integer>();
		
		startCooldownTimer();
	}
	
	@Override
	public int getMaxHomes() {
		return maxHomes;
	}
	
	@Override
	public boolean isHomeBusy() {
		return homeBusy;
	}
	
	@Override
	public boolean isWarpBusy() {
		return warpBusy;
	}
	
	private void updateBusy() {
		 GoldenApple.getInstance().getModuleManager().getModule("Warp").setBusy(homeBusy || warpBusy);
	}
	
	@Override
	public PlayerBoundWarp getHome(IPermissionUser user, int homeNum) {
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
	
	@Override
	public PlayerBoundWarp getHome(IPermissionUser user, String alias) {
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
	
	@Override
	public PlayerBoundWarp setHome(IPermissionUser user, int homeNumber, Location loc) throws SQLException {
		return setHome(user, homeNumber, loc, null, false);
	}
	
	@Override
	public PlayerBoundWarp setHome(IPermissionUser user, int homeNumber, Location loc, String alias, boolean isPublic) throws SQLException {
		HomeWarp h = new HomeWarp(user.getId(), homeNumber, loc, alias, isPublic);
		h.delete();
		h.insert();
		
		return h;
	}
	
	@Override
	public PermissibleWarp getNamedWarp(String name) {
		try {
			ResultSet r = GoldenApple.getInstanceDatabaseManager().executeQuery("SELECT * FROM Warps WHERE Name=?", name);
			try {
				if (r.next()) {
					return new NamedWarp(r);
				} else {
					return null;
				}
			} finally {
				GoldenApple.getInstanceDatabaseManager().closeResult(r);
			}
		} catch (SQLException e) {
			GoldenApple.log(Level.WARNING, "Error while attempting to retrieve a warp from the database:");
			GoldenApple.log(Level.WARNING, e);
			return null;
		}
	}
	
	@Override
	public PermissibleWarp setNamedWarp(String name, Location loc) throws SQLException {
		NamedWarp w = new NamedWarp(name, loc);
		w.delete();
		w.insert();
		
		return w;
	}
	
	@Override
	public List<PermissibleWarp> getAvailableNamedWarps(IPermissionUser u) {
	    List<PermissibleWarp> available = new ArrayList<PermissibleWarp>();
	    
	    for (PermissibleWarp w : getAllNamedWarps()) {
	        if (w.canTeleport(u)) available.add(w);
	    }
	    
	    return available;
	}
	
	@Override
	public List<PermissibleWarp> getAllNamedWarps() {
	    try {
	        List<PermissibleWarp> warps = new ArrayList<PermissibleWarp>();
	        ResultSet r = GoldenApple.getInstanceDatabaseManager().executeQuery("SELECT * FROM Warps");
	        
	        try {
	            while (r.next()) {
	                warps.add(new NamedWarp(r));
	            }
	        } finally {
	            GoldenApple.getInstanceDatabaseManager().closeResult(r);
	        }
	        
	        return warps;
	    } catch (SQLException e) {
	        GoldenApple.log(Level.WARNING, "Error while attempting to retrieve a warp from the database:");
	        GoldenApple.log(Level.WARNING, e);
	        
	        return new ArrayList<PermissibleWarp>();
	    }
	}
	
	@Override
	public int getTeleportCooldown(IPermissionUser user) {
		if (teleportCooldown.containsKey(user.getId())) return teleportCooldown.get(user.getId());
		else return 0;
	}

	@Override
	public int getDeathCooldown(IPermissionUser user) {
		if (deathCooldown.containsKey(user.getId())) return deathCooldown.get(user.getId());
		else return 0;
	}

	@Override
	public int startTeleportCooldown(IPermissionUser user) {
		if (teleportCooldownTime <= 0) return 0;
		if (user.hasPermission(WarpManager.overrideCooldownPermission)) return 0;
		
		teleportCooldown.put(user.getId(), teleportCooldownTime);
		return teleportCooldownTime;
	}

	@Override
	public int startDeathCooldown(IPermissionUser user) {
		if (deathCooldownTime <= 0) return 0;
		if (user.hasPermission(WarpManager.overrideCooldownPermission)) return 0;
		
		deathCooldown.put(user.getId(), deathCooldownTime);
		return deathCooldownTime;
	}

	@Override
	public void clearCooldownTimer(IPermissionUser user) {
		teleportCooldown.remove(user.getId());
		deathCooldown.remove(user.getId());
	}

	@Override
	public void startCooldownTimer() {
		cooldownTimer = Bukkit.getScheduler().scheduleSyncRepeatingTask(GoldenApple.getInstance(), new Runnable() {
			@Override
			public void run() {
				cooldownTimerTick();
			}
		}, 20, 20);
	}

	@Override
	public void stopCooldownTimer() {
		Bukkit.getScheduler().cancelTask(cooldownTimer);
	}
	
	private void cooldownTimerTick() {
		List<Long> toRemove = new ArrayList<Long>();
		
		for (Map.Entry<Long, Integer> timer : teleportCooldown.entrySet()) {
			if (timer.getValue() == 1) {
				toRemove.add(timer.getKey());
			} else {
				timer.setValue(timer.getValue() - 1);
			}
		}
		
		for (Long user : toRemove) {
			teleportCooldown.remove(user);
		}
		
		toRemove.clear();
		
		for (Map.Entry<Long, Integer> timer : deathCooldown.entrySet()) {
			if (timer.getValue() <= 1) {
				toRemove.add(timer.getKey());
			} else {
				timer.setValue(timer.getValue() - 1);
			}
		}
		
		for (Long user : toRemove) {
			deathCooldown.remove(user);
		}
	}

	@Override
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
