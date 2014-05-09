package com.bendude56.goldenapple.permissions;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.logging.Level;

import org.bukkit.ChatColor;

import com.bendude56.goldenapple.GoldenApple;
import com.bendude56.goldenapple.User;
import com.bendude56.goldenapple.permissions.PermissionManager.Permission;

/**
 * Represents a group in the GoldenApple permissions database.
 * <p>
 * <em><strong>Note:</strong> Do not store direct references to this class. Store the
 * ID of the instance instead!</em>
 * 
 * @author Deaboy
 * @author ben_dude56
 */
public class PermissionGroup implements IPermissionGroup {
    private long id;
    private String name;
    private int priority;
    private boolean chatColorSelected;
    private ChatColor chatColor;
    private String prefix;
    
    private ArrayList<Long> users;
    private ArrayList<Long> groups;
    private ArrayList<Long> parentGroups;
    private ArrayList<Permission> permissions;
    
    protected PermissionGroup(ResultSet r) throws SQLException {
        this.id = r.getLong("ID");
        this.name = r.getString("Name");
        this.priority = r.getInt("Priority");
        this.chatColorSelected = r.getObject("ChatColor") != null;
        this.chatColor = (r.getObject("ChatColor") == null) ? ChatColor.WHITE : ChatColor.getByChar(r.getString("ChatColor"));
        this.prefix = r.getString("Prefix");
        
        this.loadUsersAndGroups();
        this.loadPermissions();
    }
    
    protected PermissionGroup(long id, String name, int priority) {
        this.id = id;
        this.name = name;
        this.priority = priority;
        this.chatColorSelected = false;
        this.chatColor = ChatColor.WHITE;
        this.prefix = null;
        
        this.loadUsersAndGroups();
        this.loadPermissions();
    }
    
    private void loadUsersAndGroups() {
        try {
            users = new ArrayList<Long>();
            ResultSet r = GoldenApple.getInstanceDatabaseManager().executeQuery("SELECT MemberID FROM GroupUserMembers WHERE GroupID=?", id);;
            try {
                while (r.next()) {
                    users.add(r.getLong("MemberID"));
                }
            } finally {
                r.close();
            }
        } catch (SQLException e) {
            GoldenApple.log(Level.SEVERE, "Failed to retrieve users for group '" + name + "':");
            GoldenApple.log(Level.SEVERE, e);
        }
        
        try {
            groups = new ArrayList<Long>();
            ResultSet r = GoldenApple.getInstanceDatabaseManager().executeQuery("SELECT MemberID FROM GroupGroupMembers WHERE GroupID=?", id);
            try {
                while (r.next()) {
                    groups.add(r.getLong("MemberID"));
                }
            } finally {
                GoldenApple.getInstanceDatabaseManager().closeResult(r);
            }
        } catch (SQLException e) {
            GoldenApple.log(Level.SEVERE, "Failed to retrieve sub-groups for group '" + name + "':");
            GoldenApple.log(Level.SEVERE, e);
        }
        
        try {
            parentGroups = new ArrayList<Long>();
            ResultSet r = GoldenApple.getInstanceDatabaseManager().executeQuery("SELECT GroupID FROM GroupGroupMembers WHERE MemberID=?", id);
            try {
                while (r.next()) {
                    parentGroups.add(r.getLong("GroupID"));
                }
            } finally {
                GoldenApple.getInstanceDatabaseManager().closeResult(r);
            }
        } catch (SQLException e) {
            GoldenApple.log(Level.SEVERE, "An error occurred while calculating group inheritance for group '" + name + "':");
            GoldenApple.log(Level.SEVERE, e);
        }
    }
    
    private void loadPermissions() {
        try {
            permissions = new ArrayList<Permission>();
            ResultSet r = GoldenApple.getInstanceDatabaseManager().executeQuery("SELECT Permission FROM GroupPermissions WHERE GroupID=?", id);
            try {
                while (r.next()) {
                    permissions.add(PermissionManager.getInstance().getPermissionByName(r.getString("Permission"), true));
                }
            } finally {
                GoldenApple.getInstanceDatabaseManager().closeResult(r);
            }
        } catch (SQLException e) {
            GoldenApple.log(Level.SEVERE, "An error occurred while calculating permissions for group '" + name + "':");
            GoldenApple.log(Level.SEVERE, e);
        }
    }
    
    /**
     * Pushes any changes made to this group to the SQL database
     */
    public void save() {
        try {
            GoldenApple.getInstanceDatabaseManager().execute("UPDATE Groups SET Name=?, Priority=?, ChatColor=?, Prefix=? WHERE ID=?", name, priority, (chatColorSelected) ? String.valueOf(chatColor.getChar()) : null, prefix, id);
        } catch (SQLException e) {
            GoldenApple.log(Level.SEVERE, "Failed to save changes to group '" + name + "':");
            GoldenApple.log(Level.SEVERE, e);
        }
    }
    
    @Override
    public long getId() {
        return id;
    }
    
    /**
     * Gets the name of the group represented by this instance.
     */
    @Override
    public String getName() {
        return name;
    }
    
    @Override
    public int getPriority() {
        return priority;
    }
    
    @Override
    public boolean isChatColorSet() {
        return chatColorSelected;
    }
    
    @Override
    public ChatColor getChatColor() {
        return chatColor;
    }
    
    @Override
    public String getPrefix() {
        return prefix;
    }
    
    @Override
    public void setPriority(int priority) {
        this.priority = priority;
    }
    
    @Override
    public void setChatColor(boolean isSet, ChatColor color) {
        chatColor = (isSet) ? color : ChatColor.WHITE;
        chatColorSelected = isSet;
    }
    
    @Override
    public void setPrefix(String prefix) {
        this.prefix = prefix;
    }
    
    /**
     * Gets a list of user IDs for users that inherit this group's permissions.
     */
    @Override
    public List<Long> getUsers() {
        return Collections.unmodifiableList(users);
    }
    
    @Override
    public List<Long> getAllUsers() {
        ArrayList<Long> users = new ArrayList<Long>();
        users.addAll(getUsers());
        for (long g : getAllGroups()) {
            users.addAll(PermissionManager.getInstance().getGroup(g).getUsers());
        }
        return users;
    }
    
    @Override
    public void addUser(IPermissionUser user) {
        if (!isMember(user, true)) {
            try {
                users.add(user.getId());
                GoldenApple.getInstanceDatabaseManager().execute("INSERT INTO GroupUserMembers (GroupID, MemberID) VALUES (?, ?)", id, user.getId());
                user.reloadFromDatabase();
                if (user instanceof User) {
                    ((User)user).registerBukkitPermissions();
                }
            } catch (SQLException e) {
                GoldenApple.log(Level.SEVERE, "Failed to add user '" + user.getName() + "' to group '" + name + "':");
                GoldenApple.log(Level.SEVERE, e);
            }
        }
    }
    
    @Override
    public void removeUser(IPermissionUser user) {
        if (isMember(user, true)) {
            try {
                users.remove(user.getId());
                GoldenApple.getInstanceDatabaseManager().execute("DELETE FROM GroupUserMembers WHERE GroupID=? AND MemberID=?", id, user.getId());
                user.reloadFromDatabase();
                if (user instanceof User) {
                    ((User)user).registerBukkitPermissions();
                }
            } catch (SQLException e) {
                GoldenApple.log(Level.SEVERE, "Failed to remove user '" + user.getName() + "' from group '" + name + "':");
                GoldenApple.log(Level.SEVERE, e);
            }
        }
    }
    
    @Override
    public boolean isMember(IPermissionUser user, boolean directOnly) {
        if (users.contains(user.getId())) {
            return true;
        }
        
        if (!directOnly) {
            for (Long g : getParentGroups(true)) {
                if (PermissionManager.getInstance().getGroup(g).isMember(user, true)) {
                    return true;
                }
            }
        }
        
        return false;
    }
    
    /**
     * Gets a list of group IDs for groups that inherit this group's
     * permissions.
     */
    @Override
    public List<Long> getGroups() {
        return Collections.unmodifiableList(groups);
    }
    
    @Override
    public List<Long> getAllGroups() {
        ArrayList<Long> groups = new ArrayList<Long>();
        groups.addAll(getGroups());
        for (int i = 0; i < groups.size(); i++) {
            groups.addAll(PermissionManager.getInstance().getGroup(groups.get(i)).getGroups());
        }
        return groups;
    }
    
    @Override
    public void addGroup(IPermissionGroup group) {
        if (!isMember(group, true)) {
            try {
                groups.add(group.getId());
                GoldenApple.getInstanceDatabaseManager().execute("INSERT INTO GroupGroupMembers (GroupID, MemberID) VALUES (?, ?)", id, group.getId());
                group.reloadFromDatabase();
                for (long id : group.getAllUsers()) {
                    User.refreshPermissions(id);
                }
            } catch (SQLException e) {
                GoldenApple.log(Level.SEVERE, "Failed to add group '" + group.getName() + "' to group '" + name + "':");
                GoldenApple.log(Level.SEVERE, e);
            }
        }
    }
    
    @Override
    public void removeGroup(IPermissionGroup group) {
        if (isMember(group, true)) {
            try {
                groups.remove(group.getId());
                GoldenApple.getInstanceDatabaseManager().execute("DELETE FROM GroupGroupMembers WHERE GroupID=? AND MemberID=?", id, group.getId());
                group.reloadFromDatabase();
                for (long id : group.getAllUsers()) {
                    User.refreshPermissions(id);
                }
            } catch (SQLException e) {
                GoldenApple.log(Level.SEVERE, "Failed to remove group '" + group.getName() + "' from group '" + name + "':");
                GoldenApple.log(Level.SEVERE, e);
            }
        }
    }
    
    @Override
    public boolean isMember(IPermissionGroup group, boolean directOnly) {
        if (groups.contains(group.getId())) {
            return true;
        }
        
        if (!directOnly) {
            for (Long g : getParentGroups(true)) {
                if (PermissionManager.getInstance().getGroup(g).isMember(group, true)) {
                    return true;
                }
            }
        }
        
        return false;
    }
    
    @Override
    public List<Permission> getPermissions(boolean inherited) {
        if (inherited) {
            ArrayList<Permission> permissions = new ArrayList<Permission>();
            permissions.addAll(getPermissions(false));
            
            for (Long g : getParentGroups(true)) {
                permissions.addAll(PermissionManager.getInstance().getGroup(g).getPermissions(false));
            }
            
            return Collections.unmodifiableList(permissions);
        } else {
            return Collections.unmodifiableList(permissions);
        }
    }
    
    @Override
    public void addPermission(Permission permission) {
        if (!hasPermissionSpecific(permission)) {
            try {
                permissions.add(permission);
                GoldenApple.getInstanceDatabaseManager().execute("INSERT INTO GroupPermissions (GroupID, Permission) VALUES (?, ?)", id, permission.getFullName());
                for (long id : getAllUsers()) {
                    User u = User.getUser(id);
                    if (u != null) {
                        u.getPermissionAttachment().setPermission(permission.getFullName(), true);
                    }
                }
            } catch (SQLException e) {
                GoldenApple.log(Level.SEVERE, "Error while adding permission '" + permission.getFullName() + "' to group '" + name + "':");
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
                permissions.remove(permission);
                GoldenApple.getInstanceDatabaseManager().execute("DELETE FROM GroupPermissions WHERE GroupID=? AND Permission=?", id, permission.getFullName());
                for (long id : getAllUsers()) {
                    User u = User.getUser(id);
                    if (u != null) {
                        u.getPermissionAttachment().unsetPermission(permission.getFullName());
                    }
                }
            } catch (SQLException e) {
                GoldenApple.log(Level.SEVERE, "Error while removing permission '" + permission.getFullName() + "' from group '" + name + "':");
                GoldenApple.log(Level.SEVERE, e);
            }
        }
    }
    
    @Override
    public void removePermission(String permission) {
        removePermission(PermissionManager.getInstance().getPermissionByName(permission));
    }
    
    /**
     * Checks whether this group has a given permission.
     * 
     * @param permission The permission to check for.
     * @return True if the group has the specified permission, false otherwise.
     */
    @Override
    public boolean hasPermission(String permission) {
        return hasPermission(permission, false);
    }
    
    /**
     * Checks whether this group has a given permission.
     * 
     * @param permission The permission to check for.
     * @return True if the group has the specified permission, false otherwise.
     */
    @Override
    public boolean hasPermission(Permission permission) {
        return hasPermission(permission, false);
    }
    
    /**
     * Checks whether this group has a given permission.
     * 
     * @param permission The permission to check for.
     * @param specific Determines whether or not indirect permissions should be
     * considered. If true, only permissions given specifically to this group
     * will be checked. If false, all permissions (including indirect
     * permissions) will be considered.
     * @return True if the group has the specified permission, false otherwise.
     */
    @Override
    public boolean hasPermission(String permission, boolean specific) {
        return hasPermission(PermissionManager.getInstance().getPermissionByName(permission), specific);
    }
    
    /**
     * Checks whether this group has a given permission.
     * 
     * @param permission The permission to check for.
     * @param specific Determines whether or not indirect permissions should be
     * considered. If true, only permissions given specifically to this group
     * will be checked. If false, all permissions (including indirect
     * permissions) will be considered.
     * @return True if the group has the specified permission, false otherwise.
     */
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
    public List<Long> getParentGroups(boolean directOnly) {
        if (directOnly) {
            return Collections.unmodifiableList(parentGroups);
        } else {
            ArrayList<Long> parents = new ArrayList<Long>();
            parents.addAll(parentGroups);
            
            for (int i = 0; i < parents.size(); i++) {
                parents.addAll(PermissionManager.getInstance().getGroup(parents.get(i)).getParentGroups(false));
            }
            
            return parents;
        }
    }
    
    @Override
    public void reloadFromDatabase() {
        this.loadPermissions();
        this.loadUsersAndGroups();
    }
}
