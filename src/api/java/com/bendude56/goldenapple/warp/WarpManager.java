package com.bendude56.goldenapple.warp;

import java.sql.SQLException;

import org.bukkit.Location;

import com.bendude56.goldenapple.User;
import com.bendude56.goldenapple.permissions.IPermissionUser;
import com.bendude56.goldenapple.permissions.PermissionManager.Permission;
import com.bendude56.goldenapple.permissions.PermissionManager.PermissionNode;

public abstract class WarpManager {
	// goldenapple.warp
	public static PermissionNode warpNode;
	public static Permission backPermission;
	public static Permission editPermission;
	
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
	
	protected static WarpManager instance;
	
	public static WarpManager getInstance() {
		return instance;
	}
	
	public abstract int getMaxHomes();
	public abstract boolean isHomeBusy();
	public abstract boolean isWarpBusy();
	
	public abstract PlayerBoundWarp getHome(IPermissionUser user, int homeNum);
	public abstract PlayerBoundWarp getHome(IPermissionUser user, String alias);
	
	public abstract PlayerBoundWarp setHome(IPermissionUser user, int nomeNumber, Location loc) throws SQLException;
	public abstract PlayerBoundWarp setHome(IPermissionUser user, int homeNumber, Location loc, String alias, boolean isPublic) throws SQLException;
	
	public abstract PermissibleWarp getNamedWarp(String name);
	
	public abstract PermissibleWarp setNamedWarp(String name, Location loc) throws SQLException;
	
	public abstract int getTeleportCooldown(IPermissionUser user);
	public abstract int getDeathCooldown(IPermissionUser user);
	
	public abstract int startTeleportCooldown(IPermissionUser user);
	public abstract int startDeathCooldown(IPermissionUser user);
	
	public abstract void clearCooldownTimer(IPermissionUser user);
	
	public abstract void startCooldownTimer();
	public abstract void stopCooldownTimer();
	
	public abstract void importHomesFromEssentials(User sender);
}
