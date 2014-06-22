package com.bendude56.goldenapple.select;

import org.bukkit.Location;
import org.bukkit.World;

import com.bendude56.goldenapple.User;
import com.bendude56.goldenapple.permissions.PermissionManager.Permission;
import com.bendude56.goldenapple.permissions.PermissionManager.PermissionNode;

public abstract class SelectManager {
    // goldenapple.select
    public static PermissionNode selectNode;
    
    // goldenapple.select.builtin
    public static PermissionNode builtinNode;
    public static Permission builtinSelectPermission;
    public static Permission builtinExpandPermission;
    public static Permission builtinContractPermission;
    public static Permission builtinShiftPermission;
    
    protected static SelectManager instance;
    
    public static SelectManager getInstance() {
        return instance;
    }
    
    public abstract ISelectionProvider getSelectionProvider();
    public abstract void setSelectionProvider(ISelectionProvider provider);
    
    public abstract boolean isSelectionMade(User user);
    public abstract Location getSelectionMinimum(User user);
    public abstract Location getSelectionMaximum(User user);
    public abstract World getSelectionWorld(User user);
}
