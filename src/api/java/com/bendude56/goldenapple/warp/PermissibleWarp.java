package com.bendude56.goldenapple.warp;

import com.bendude56.goldenapple.permissions.IPermissionGroup;

public abstract class PermissibleWarp extends BaseWarp {
    
    public abstract boolean canEverybodyTeleport();
    public abstract boolean canTeleport(IPermissionGroup g);
    
    public abstract void addGroup(IPermissionGroup g);
    public abstract void removeGroup(IPermissionGroup g);
}
