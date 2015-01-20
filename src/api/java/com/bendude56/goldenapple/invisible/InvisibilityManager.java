package com.bendude56.goldenapple.invisible;

import com.bendude56.goldenapple.User;
import com.bendude56.goldenapple.permissions.PermissionManager.Permission;
import com.bendude56.goldenapple.permissions.PermissionManager.PermissionNode;

public abstract class InvisibilityManager {
    // goldenapple.invisible
    public static PermissionNode invisibleNode;
    public static Permission vanishPermission;
    public static Permission vanishInteractPermission;
    public static Permission seeVanishedPermission;
    
    protected static InvisibilityManager instance;
    
    public static InvisibilityManager getInstance() {
        return instance;
    }
    
    public abstract void setInvisible(User user, boolean invisible);
    public abstract boolean isInvisible(User user);
    
    public abstract void setInvisibilityFlag(User user, String flag, boolean value);
    public abstract boolean isInvisibilityFlagSet(User user, String flag);
    
    public abstract void setAllSeeing(User user, boolean allSeeing);
    public abstract boolean isAllSeeing(User user);
}
