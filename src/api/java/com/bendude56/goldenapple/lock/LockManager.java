package com.bendude56.goldenapple.lock;

import java.lang.reflect.InvocationTargetException;
import java.sql.SQLException;

import org.bukkit.Location;
import org.bukkit.Material;

import com.bendude56.goldenapple.User;
import com.bendude56.goldenapple.lock.LockedBlock.LockLevel;
import com.bendude56.goldenapple.permissions.IPermissionUser;
import com.bendude56.goldenapple.permissions.PermissionManager.Permission;
import com.bendude56.goldenapple.permissions.PermissionManager.PermissionNode;

public abstract class LockManager {
    // goldenapple.lock
    public static PermissionNode lockNode;
    public static Permission addPermission;
    public static Permission usePermission;
    public static Permission invitePermission;
    public static Permission modifyBlockPermission;
    public static Permission fullPermission;
    
    protected static LockManager instance;
    
    public static LockManager getInstance() {
        return instance;
    }
    
    public abstract LockedBlock getLock(long id);
    public abstract LockedBlock getLock(Location l);
    public abstract LockedBlock getLockSpecific(Location l);
    public abstract boolean lockExists(long id) throws SQLException;
    
    public abstract boolean isLockable(Material m);
    
    public abstract LockedBlock createLock(Location loc, LockLevel access, IPermissionUser owner) throws SQLException, InvocationTargetException;
    public abstract void deleteLock(long id) throws SQLException;
    
    public abstract boolean isOverrideOn(User u);
    public abstract void setOverrideOn(User u, boolean override);
    public abstract boolean canOverride(User u);
    
    public abstract void clearCache();
}
