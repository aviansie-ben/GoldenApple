package com.bendude56.goldenapple.permissions;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.UUID;
import java.util.logging.Level;

import org.bukkit.ChatColor;

import com.bendude56.goldenapple.GoldenApple;
import com.bendude56.goldenapple.permissions.PermissionManager.Permission;

public class PermissionUser implements IPermissionUser {
    private long id;
    private String name;
    private UUID uuid;
    private String preferredLocale;
    private boolean complexCommands;
    private boolean autoLock;
    
    private ArrayList<Long> groups;
    private ArrayList<Permission> permissions;
    
    protected PermissionUser(long id, String name, UUID uuid, String preferredLocale, boolean complexCommands, boolean autoLock) {
        this.id = id;
        this.name = name;
        this.uuid = uuid;
        this.preferredLocale = preferredLocale;
        this.complexCommands = complexCommands;
        this.autoLock = autoLock;
        
        this.loadGroups();
        this.loadPermissions();
    }
    
    private void loadGroups() {
        try {
            groups = new ArrayList<Long>();
            ResultSet r = GoldenApple.getInstanceDatabaseManager().executeQuery("SELECT GroupID FROM GroupUserMembers WHERE MemberID=?", id);
            try {
                while (r.next()) {
                    groups.add(r.getLong("GroupID"));
                }
            } finally {
                GoldenApple.getInstanceDatabaseManager().closeResult(r);
            }
        } catch (SQLException e) {
            GoldenApple.log(Level.SEVERE, "An error occurred while calculating group inheritance for user '" + name + "':");
            GoldenApple.log(Level.SEVERE, e);
        }
    }
    
    private void loadPermissions() {
        try {
            permissions = new ArrayList<Permission>();
            ResultSet r = GoldenApple.getInstanceDatabaseManager().executeQuery("SELECT Permission FROM UserPermissions WHERE UserID=?", id);
            try {
                while (r.next()) {
                    permissions.add(PermissionManager.getInstance().getPermissionByName(r.getString("Permission"), true));
                }
            } finally {
                GoldenApple.getInstanceDatabaseManager().closeResult(r);
            }
        } catch (SQLException e) {
            GoldenApple.log(Level.SEVERE, "An error occurred while calculating permissions for user '" + name + "':");
            GoldenApple.log(Level.SEVERE, e);
        }
    }
    
    public void save() {
        try {
            GoldenApple.getInstanceDatabaseManager().execute("UPDATE Users SET Name=?, Locale=?, ComplexCommands=?, AutoLock=? WHERE ID=?", name, preferredLocale, complexCommands, autoLock, id);
        } catch (SQLException e) {
            GoldenApple.log(Level.SEVERE, "Failed to save changes to user '" + name + "':");
            GoldenApple.log(Level.SEVERE, e);
        }
    }
    
    @Override
    public String getName() {
        return name;
    }
    
    public void setName(String name) {
        this.name = name;
    }
    
    @Override
    public UUID getUuid() {
        return uuid;
    }
    
    @Override
    public long getId() {
        return id;
    }
    
    @Override
    public List<Permission> getPermissions(boolean inherited) {
        if (inherited) {
            ArrayList<Permission> permissions = new ArrayList<Permission>();
            permissions.addAll(getPermissions(false));
            
            for (Long g : groups) {
                permissions.addAll(PermissionManager.getInstance().getGroup(g).getPermissions(true));
            }
            
            return permissions;
        } else {
            return Collections.unmodifiableList(permissions);
        }
    }
    
    @Override
    public boolean hasPermission(String permission) {
        return hasPermission(permission, true);
    }
    
    @Override
    public boolean hasPermission(Permission permission) {
        return hasPermission(permission, true);
    }
    
    @Override
    public boolean hasPermission(String permission, boolean inherited) {
        return hasPermission(PermissionManager.getInstance().getPermissionByName(permission), inherited);
    }
    
    @Override
    public boolean hasPermission(Permission permission, boolean inherited) {
        List<Long> parentGroups = getParentGroups(true);
        if (hasPermissionSpecificInheritance(permission, parentGroups, inherited)) {
            return true;
        }
        for (Permission p : permission.getEquivalentPermissions()) {
            if (hasPermissionSpecificInheritance(p, parentGroups, inherited)) {
                return true;
            }
        }
        return false;
    }
    
    private boolean hasPermissionSpecificInheritance(Permission permission, List<Long> groups, boolean inherited) {
        if (hasPermissionSpecific(permission)) {
            return true;
        } else if (inherited) {
            for (Long g : groups) {
                IPermissionGroup gr = PermissionManager.getInstance().getGroup(g);
                if (gr.hasPermissionSpecific(permission)) {
                    return true;
                }
            }
            return false;
        } else {
            return false;
        }
    }
    
    @Override
    public boolean hasPermissionSpecific(Permission permission) {
        return permissions.contains(permission);
    }
    
    @Override
    public String getPreferredLocale() {
        return preferredLocale;
    }
    
    @Override
    public void setPreferredLocale(String locale) {
        preferredLocale = locale;
        save();
    }
    
    @Override
    public void addPermission(Permission permission) {
        if (!hasPermissionSpecific(permission)) {
            try {
                GoldenApple.getInstanceDatabaseManager().execute("INSERT INTO UserPermissions (UserID, Permission) VALUES (?, ?)", id, permission.getFullName());
                permissions.add(permission);
            } catch (SQLException e) {
                GoldenApple.log(Level.SEVERE, "Error while adding permission '" + permission.getFullName() + "' to user '" + name + "':");
                GoldenApple.log(Level.SEVERE, e);
            }
        }
    }
    
    @Override
    public void addPermission(String permission) {
        addPermission(PermissionManager.getInstance().getPermissionByName(permission));
    }
    
    @Override
    public void removePermission(Permission permission) {
        if (hasPermissionSpecific(permission)) {
            try {
                GoldenApple.getInstanceDatabaseManager().execute("DELETE FROM UserPermissions WHERE UserID=? AND Permission=?", id, permission.getFullName());
                permissions.remove(permission);
            } catch (SQLException e) {
                GoldenApple.log(Level.SEVERE, "Error while removing permission '" + permission.getFullName() + "' from user '" + name + "':");
                GoldenApple.log(Level.SEVERE, e);
            }
        }
    }
    
    @Override
    public void removePermission(String permission) {
        removePermission(PermissionManager.getInstance().getPermissionByName(permission));
    }
    
    @Override
    public List<Long> getParentGroups(boolean directOnly) {
        if (directOnly) {
            return Collections.unmodifiableList(groups);
        } else {
            ArrayList<Long> parents = new ArrayList<Long>();
            parents.addAll(groups);
            
            for (Long g : groups) {
                parents.addAll(PermissionManager.getInstance().getGroup(g).getParentGroups(false));
            }
            
            return parents;
        }
    }
    
    @Override
    public boolean isUsingComplexCommands() {
        return complexCommands;
    }
    
    @Override
    public void setUsingComplexCommands(boolean useComplex) {
        complexCommands = useComplex;
        save();
    }
    
    @Override
    public boolean isAutoLockEnabled() {
        return autoLock;
    }
    
    @Override
    public void setAutoLockEnabled(boolean autoLock) {
        this.autoLock = autoLock;
        save();
    }
    
    @Override
    public ChatColor getChatColor() {
        int priority = -1;
        ChatColor color = ChatColor.WHITE;
        
        for (Long gid : getParentGroups(false)) {
            IPermissionGroup g = PermissionManager.getInstance().getGroup(gid);
            if (g.isChatColorSet() && g.getPriority() > priority) {
                priority = g.getPriority();
                color = g.getChatColor();
            }
        }
        
        return color;
    }
    
    @Override
    public String getPrefix() {
        int priority = -1;
        String prefix = null;
        
        for (Long gid : getParentGroups(false)) {
            IPermissionGroup g = PermissionManager.getInstance().getGroup(gid);
            if (g.getPrefix() != null && g.getPriority() > priority) {
                priority = g.getPriority();
                prefix = g.getPrefix();
            }
        }
        
        return prefix;
    }
    
    @Override
    public void reloadFromDatabase() {
        this.loadGroups();
        this.loadPermissions();
    }
}
