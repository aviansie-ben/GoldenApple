package com.bendude56.goldenapple.invisible;

import com.bendude56.goldenapple.User;
import com.bendude56.goldenapple.permissions.PermissionManager.Permission;
import com.bendude56.goldenapple.permissions.PermissionManager.PermissionNode;

public abstract class InvisibilityManager {
    // goldenapple.invisible
    public static PermissionNode invisibleNode;
    public static Permission vanishPermission;
    public static Permission seeVanishedPermission;
    
    protected static InvisibilityManager instance;
    
    public static InvisibilityManager getInstance() {
        return instance;
    }
    
    public abstract void setInvisible(User user, boolean invisible);
    public abstract boolean isInvisible(User user);
    
    public abstract void setInteractionEnabled(User user, boolean interact);
    public abstract boolean isInteractionEnabled(User user);
    
    public abstract void setAllSeeing(User user, boolean allSeeing);
    public abstract boolean isAllSeeing(User user);
}
