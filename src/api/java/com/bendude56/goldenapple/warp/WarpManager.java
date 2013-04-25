package com.bendude56.goldenapple.warp;

import com.bendude56.goldenapple.User;
import com.bendude56.goldenapple.permissions.IPermissionUser;
import com.bendude56.goldenapple.permissions.PermissionManager.Permission;
import com.bendude56.goldenapple.permissions.PermissionManager.PermissionNode;

public abstract class WarpManager {
	// goldenapple.warp
	public static PermissionNode warpNode;
	public static Permission backPermission;
	
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
	
	public abstract BaseWarp getHome(IPermissionUser user, int homeNum);
	public abstract BaseWarp getHome(IPermissionUser user, String alias);
	
	public abstract void importHomesFromEssentials(User sender);
}
