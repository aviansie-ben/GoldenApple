package com.bendude56.goldenapple.permissions;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
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
    
    private ArrayList<Long> groups;
    private ArrayList<Permission> permissions;
    private HashMap<String, String> variables;
    
    protected PermissionUser(long id, String name, UUID uuid) {
        this.id = id;
        this.name = name;
        this.uuid = uuid;
        
        this.loadGroups();
        this.loadPermissions();
        this.loadVariables();
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
    
    private void loadVariables() {
        try {
            variables = new HashMap<String, String>();
            ResultSet r = GoldenApple.getInstanceDatabaseManager().executeQuery("SELECT VariableName, Value FROM UserVariables WHERE UserID=?", id);
            try {
                while (r.next()) {
                    variables.put(r.getString("VariableName"), r.getString("Value"));
                }
            } finally {
                GoldenApple.getInstanceDatabaseManager().closeResult(r);
            }
        } catch (SQLException e) {
            GoldenApple.log(Level.SEVERE, "An error occurred while loading variables for user '" + name + "':");
            GoldenApple.log(Level.SEVERE, e);
        }
    }
    
    public void save() {
        try {
            GoldenApple.getInstanceDatabaseManager().execute("UPDATE Users SET Name=? WHERE ID=?", name, id);
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
        List<Long> parentGroups = getParentGroups(false);
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
    @Deprecated
    public String getPreferredLocale() {
        return getVariableString("goldenapple.locale");
    }
    
    @Override
    @Deprecated
    public void setPreferredLocale(String locale) {
        setVariable("goldenapple.locale", locale);
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
    @Deprecated
    public boolean isUsingComplexCommands() {
        return getVariableBoolean("goldenapple.complexSyntax");
    }
    
    @Override
    @Deprecated
    public void setUsingComplexCommands(boolean useComplex) {
        setVariable("goldenapple.complexSyntax", useComplex);
    }
    
    @Override
    @Deprecated
    public boolean isAutoLockEnabled() {
        return getVariableBoolean("goldenapple.lock.autoLock");
    }
    
    @Override
    @Deprecated
    public void setAutoLockEnabled(boolean autoLock) {
        setVariable("goldenapple.lock.autoLock", autoLock);
    }
    
    @Override
    public ChatColor getChatColor() {
        int priority = Integer.MIN_VALUE;
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
        int priority = Integer.MIN_VALUE;
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
        this.loadVariables();
    }

    @Override
    public String getVariableString(String variableName) {
        if (variables.containsKey(variableName)) {
            return getVariableSpecificString(variableName);
        } else {
            int priority = Integer.MIN_VALUE;
            String value = PermissionManager.getInstance().getVariableDefaultValue(variableName);
            
            for (Long parent : getParentGroups(false)) {
                IPermissionGroup parentGroup = PermissionManager.getInstance().getGroup(parent);
                String groupValue = parentGroup.getVariableSpecificString(variableName);
                
                if (groupValue != null && parentGroup.getPriority() > priority) {
                    value = groupValue;
                    priority = parentGroup.getPriority();
                }
            }
            
            return value;
        }
    }

    @Override
    public Boolean getVariableBoolean(String variableName) {
        if (variables.containsKey(variableName)) {
            return getVariableSpecificBoolean(variableName);
        } else {
            int priority = Integer.MIN_VALUE;
            Boolean value;
            
            if (PermissionManager.getInstance().getVariableDefaultValue(variableName) != null) {
                value = PermissionManager.getInstance().getVariableDefaultValue(variableName).equalsIgnoreCase("true");
            } else {
                value = null;
            }
            
            for (Long parent : getParentGroups(false)) {
                IPermissionGroup parentGroup = PermissionManager.getInstance().getGroup(parent);
                Boolean groupValue = parentGroup.getVariableSpecificBoolean(variableName);
                
                if (groupValue != null && parentGroup.getPriority() > priority) {
                    value = groupValue;
                    priority = parentGroup.getPriority();
                }
            }
            
            return value;
        }
    }

    @Override
    public Integer getVariableInteger(String variableName) {
        if (variables.containsKey(variableName)) {
            return getVariableSpecificInteger(variableName);
        } else {
            int priority = Integer.MIN_VALUE;
            Integer value;
            
            try {
                if (PermissionManager.getInstance().getVariableDefaultValue(variableName) != null) {
                    value = Integer.parseInt(PermissionManager.getInstance().getVariableDefaultValue(variableName));
                } else {
                    value = null;
                }
            } catch (NumberFormatException e) {
                value = null;
            }
            
            for (Long parent : getParentGroups(false)) {
                IPermissionGroup parentGroup = PermissionManager.getInstance().getGroup(parent);
                Integer groupValue = parentGroup.getVariableSpecificInteger(variableName);
                
                if (groupValue != null && parentGroup.getPriority() > priority) {
                    value = groupValue;
                    priority = parentGroup.getPriority();
                }
            }
            
            return value;
        }
    }

    @Override
    public String getVariableSpecificString(String variableName) {
        return variables.get(variableName);
    }

    @Override
    public Boolean getVariableSpecificBoolean(String variableName) {
        if (variables.containsKey(variableName)) {
            return variables.get(variableName).equalsIgnoreCase("true");
        } else {
            return null;
        }
    }

    @Override
    public Integer getVariableSpecificInteger(String variableName) {
        if (variables.containsKey(variableName)) {
            try {
                return Integer.parseInt(variables.get(variableName));
            } catch (NumberFormatException e) {
                return null;
            }
        } else {
            return null;
        }
    }
    
    @Override
    public void deleteVariable(String variableName) {
        variables.remove(variableName);
        
        try {
            GoldenApple.getInstanceDatabaseManager().execute("DELETE FROM UserVariables WHERE UserID=? AND VariableName=?", id, variableName);
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public void setVariable(String variableName, String value) {
        if (value != null) {
            deleteVariable(variableName);
            variables.put(variableName, value);
            
            try {
                GoldenApple.getInstanceDatabaseManager().execute("INSERT INTO UserVariables (UserID, VariableName, Value) VALUES (?, ?, ?)", id, variableName, value);
            } catch (SQLException e) {
                throw new RuntimeException(e);
            }
        } else {
            deleteVariable(variableName);
        }
    }

    @Override
    public void setVariable(String variableName, Boolean value) {
        if (value != null) {
            setVariable(variableName, (value) ? "true" : "false");
        } else {
            deleteVariable(variableName);
        }
    }

    @Override
    public void setVariable(String variableName, Integer value) {
        if (variableName != null) {
            setVariable(variableName, value.toString());
        } else {
            deleteVariable(variableName);
        }
    }
}
