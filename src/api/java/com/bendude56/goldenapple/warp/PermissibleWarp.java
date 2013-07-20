package com.bendude56.goldenapple.warp;

import com.bendude56.goldenapple.permissions.PermissionGroup;

public abstract class PermissibleWarp extends BaseWarp {

	public abstract boolean canEverybodyTeleport();
	public abstract boolean canTeleport(PermissionGroup g);
	
	public abstract void addGroup(PermissionGroup g);
	public abstract void removeGroup(PermissionGroup g);
}
