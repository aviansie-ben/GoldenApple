package com.bendude56.goldenapple.warp;

import org.bukkit.Location;

import com.bendude56.goldenapple.permissions.IPermissionUser;
import com.bendude56.goldenapple.permissions.PermissionManager;

public abstract class PlayerBoundWarp extends BaseWarp {
    protected long owner;
    private boolean isPublic;
    
    public PlayerBoundWarp(long owner, boolean isPublic, Location loc) {
        setLocation(loc);
        
        this.owner = owner;
        this.isPublic = isPublic;
    }
    
    public IPermissionUser getOwner() {
        return PermissionManager.getInstance().getUser(owner);
    }
    
    @Override
    public boolean canTeleport(IPermissionUser u) {
        if (u.getId() == owner && u.hasPermission(WarpManager.homeTpOwn)) {
            return true;
        } else if (isPublic && u.hasPermission(WarpManager.homeTpPublic)) {
            return true;
        } else if (u.hasPermission(WarpManager.homeTpAll)) {
            return true;
        } else {
            return false;
        }
    }
    
    @Override
    public boolean canEdit(IPermissionUser u) {
        if (u.getId() == owner && u.hasPermission(WarpManager.homeEditOwn)) {
            return true;
        } else if (isPublic && u.hasPermission(WarpManager.homeEditPublic)) {
            return true;
        } else if (u.hasPermission(WarpManager.homeEditAll)) {
            return true;
        } else {
            return false;
        }
    }
    
    public boolean isPublic() {
        return isPublic;
    }
    
    public void setPublic(boolean isPublic) {
        this.isPublic = isPublic;
    }
    
}
