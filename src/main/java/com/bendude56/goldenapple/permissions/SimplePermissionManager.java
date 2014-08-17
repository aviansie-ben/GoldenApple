package com.bendude56.goldenapple.permissions;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Deque;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import java.util.logging.Level;

import com.bendude56.goldenapple.GoldenApple;
import com.bendude56.goldenapple.SimpleLocalizationManager;
import com.bendude56.goldenapple.util.UUIDFetcher;

public class SimplePermissionManager extends PermissionManager {
    private int userCacheSize;
    private HashMap<Long, PermissionUser> userCache = new HashMap<Long, PermissionUser>();
    private Deque<Long> userCacheOut = new ArrayDeque<Long>();
    
    private HashMap<Long, PermissionGroup> groups = new HashMap<Long, PermissionGroup>();
    
    private PermissionNode rootNode;
    
    private PermissionGroup requiredGroup;
    private ArrayList<PermissionGroup> defaultGroups;
    private ArrayList<PermissionGroup> opGroups;
    private ArrayList<PermissionGroup> devGroups;
    private HashMap<String, String> variableDefaults = new HashMap<String, String>();
    
    public SimplePermissionManager() {
        rootNode = new SimplePermissionNode("", null);
        
        userCacheSize = Math.max(GoldenApple.getInstanceMainConfig().getInt("modules.permissions.userCacheSize", 20), 5);
        
        GoldenApple.getInstanceDatabaseManager().registerTableUpdater("Users", 4, new UserUuidTableUpdater());
        GoldenApple.getInstanceDatabaseManager().createOrUpdateTable("Users");
        GoldenApple.getInstanceDatabaseManager().createOrUpdateTable("UserPermissions");
        GoldenApple.getInstanceDatabaseManager().createOrUpdateTable("UserVariables");
        GoldenApple.getInstanceDatabaseManager().createOrUpdateTable("Groups");
        GoldenApple.getInstanceDatabaseManager().createOrUpdateTable("GroupPermissions");
        GoldenApple.getInstanceDatabaseManager().createOrUpdateTable("GroupGroupMembers");
        GoldenApple.getInstanceDatabaseManager().createOrUpdateTable("GroupUserMembers");
        GoldenApple.getInstanceDatabaseManager().createOrUpdateTable("GroupUserOwners");
        GoldenApple.getInstanceDatabaseManager().createOrUpdateTable("GroupVariables");
        
        setVariableDefaultValue("goldenapple.complexSyntax", GoldenApple.getInstanceMainConfig().getBoolean("modules.permissions.defaultComplexCommands", true));
        setVariableDefaultValue("goldenapple.locale", ((SimpleLocalizationManager) GoldenApple.getInstance().getLocalizationManager()).getDefaultLocale().getShortName());
    }
    
    public int getUserCacheCurrentSize() {
        return userCacheOut.size();
    }
    
    public int getUserCacheMaxSize() {
        return userCacheSize;
    }
    
    public int getUserCacheStickyCount() {
        return userCache.size() - userCacheOut.size();
    }
    
    private void popCache() {
        while (userCacheOut.size() > userCacheSize) {
            userCache.remove(userCacheOut.pop());
        }
    }
    
    public void loadGroups() {
        try {
            ResultSet r = GoldenApple.getInstanceDatabaseManager().executeQuery("SELECT * FROM Groups");
            try {
                while (r.next()) {
                    groups.put(r.getLong("ID"), new PermissionGroup(r));
                }
            } finally {
                GoldenApple.getInstanceDatabaseManager().closeResult(r);
            }
        } catch (SQLException e) {
            throw new RuntimeException("Failed to load groups!", e);
        }
    }
    
    public void checkDefaultGroups() {
        defaultGroups = new ArrayList<PermissionGroup>();
        opGroups = new ArrayList<PermissionGroup>();
        devGroups = new ArrayList<PermissionGroup>();
        
        if (!GoldenApple.getInstanceMainConfig().getString("modules.permissions.reqGroup").equals("")) {
            requiredGroup = createGroup(GoldenApple.getInstanceMainConfig().getString("modules.permissions.reqGroup"));
        }
        for (String g : GoldenApple.getInstanceMainConfig().getStringList("modules.permissions.defaultGroups")) {
            defaultGroups.add(createGroup(g));
        }
        for (String g : GoldenApple.getInstanceMainConfig().getStringList("modules.permissions.opGroups")) {
            opGroups.add(createGroup(g));
        }
        for (String g : GoldenApple.getInstanceMainConfig().getStringList("modules.permissions.devGroups")) {
            devGroups.add(createGroup(g));
        }
    }
    
    @Override
    @Deprecated
    public Permission registerPermission(String name, PermissionNode node) {
        return node.createPermission(name);
    }
    
    @Override
    @Deprecated
    public Permission registerPermission(String fullName) {
        String[] name = fullName.split("\\.");
        PermissionNode node = rootNode;
        for (int i = 0; i < name.length; i++) {
            if (i == name.length - 1) {
                return node.createPermission(name[i]);
            } else {
                node = node.createNode(name[i]);
            }
        }
        return null;
    }
    
    @Override
    @Deprecated
    public PermissionNode registerNode(String name, PermissionNode node) {
        return node.createNode(name);
    }
    
    @Override
    @Deprecated
    public Permission getRootStar() {
        return getRootNode().getStarPermission();
    }
    
    public HashMap<Long, PermissionUser> getUserCache() {
        return userCache;
    }
    
    @Override
    public Permission getPermissionByName(String name) {
        return getPermissionByName(name, false);
    }
    
    @Override
    public Permission getPermissionByName(String name, boolean create) {
        PermissionNode node = rootNode;
        String[] names = name.split("\\.");
        
        for (int i = 0; i < names.length - 1; i++) {
            node = (create) ? node.createNode(names[i]) : node.getNode(names[i]);
            
            if (node == null) {
                return null;
            }
        }
        
        return (create) ? node.createPermission(names[names.length - 1]) : node.getPermission(names[names.length - 1]);
    }
    
    @Override
    public PermissionNode getNodeByName(String name) {
        return getNodeByName(name, false);
    }
    
    @Override
    public PermissionNode getNodeByName(String name, boolean create) {
        PermissionNode node = rootNode;
        String[] names = name.split("\\.");
        
        for (int i = 0; i < names.length; i++) {
            node = (create) ? node.createNode(names[i]) : node.getNode(names[i]);
            
            if (node == null) {
                return null;
            }
        }
        
        return node;
    }
    
    @Override
    public PermissionNode getRootNode() {
        return rootNode;
    }
    
    @Override
    @Deprecated
    public long getUserId(String name) {
        return findUser(name, false).getId();
    }
    
    @Override
    public long getUserId(UUID uuid) {
        try {
            ResultSet r = GoldenApple.getInstanceDatabaseManager().executeQuery("SELECT ID FROM Users WHERE UUID=?", uuid.toString());
            try {
                if (r.next()) {
                    return r.getLong("ID");
                } else {
                    return -1;
                }
            } finally {
                GoldenApple.getInstanceDatabaseManager().closeResult(r);
            }
        } catch (SQLException e) {
            throw new RuntimeException("User lookup failed!", e);
        }
    }
    
    @Override
    public PermissionUser getUser(long id) {
        if (userCache.containsKey(id)) {
            return userCache.get(id);
        } else {
            try {
                ResultSet r = GoldenApple.getInstanceDatabaseManager().executeQuery("SELECT * FROM Users WHERE ID=?", id);
                try {
                    if (r.next()) {
                        PermissionUser u = new PermissionUser(r.getLong("ID"), r.getString("Name"), UUID.fromString(r.getString("UUID")));
                        userCache.put(u.getId(), u);
                        userCacheOut.addLast(u.getId());
                        popCache();
                        return u;
                    } else {
                        return null;
                    }
                } finally {
                    GoldenApple.getInstanceDatabaseManager().closeResult(r);
                }
            } catch (SQLException e) {
                GoldenApple.log(Level.WARNING, "Failed to load user " + id + ":");
                GoldenApple.log(Level.WARNING, e);
                return null;
            }
        }
    }
    
    @Override
    @Deprecated
    public PermissionUser getUser(String name) {
        return findUser(name, false);
    }
    
    @Override
    public PermissionUser getUser(UUID uuid) {
        for (Map.Entry<Long, PermissionUser> entry : userCache.entrySet()) {
            if (entry.getValue().getUuid().equals(uuid)) {
                return entry.getValue();
            }
        }
        try {
            ResultSet r = GoldenApple.getInstanceDatabaseManager().executeQuery("SELECT * FROM Users WHERE UUID=?", uuid.toString());
            try {
                if (r.next()) {
                    PermissionUser u = new PermissionUser(r.getLong("ID"), r.getString("Name"), UUID.fromString(r.getString("UUID")));
                    userCache.put(u.getId(), u);
                    userCacheOut.addLast(u.getId());
                    popCache();
                    return u;
                } else {
                    return null;
                }
            } finally {
                GoldenApple.getInstanceDatabaseManager().closeResult(r);
            }
        } catch (SQLException e) {
            GoldenApple.log(Level.WARNING, "Failed to load user '" + uuid + "':");
            GoldenApple.log(Level.WARNING, e);
            return null;
        }
    }
    
    @Override
    public PermissionUser findUser(String name, boolean allowPartialMatch) {
        if (allowPartialMatch) {
            PermissionUser u = findUser(name, false);
            
            if (u != null) {
                return u;
            } else {
                // TODO Add partial name matching
                return null;
            }
        } else {
            for (Map.Entry<Long, PermissionUser> entry : userCache.entrySet()) {
                if (entry.getValue().getName().equalsIgnoreCase(name)) {
                    return entry.getValue();
                }
            }
            try {
                ResultSet r = GoldenApple.getInstanceDatabaseManager().executeQuery("SELECT * FROM Users WHERE Name=?", name);
                try {
                    if (r.next()) {
                        PermissionUser u = new PermissionUser(r.getLong("ID"), r.getString("Name"), UUID.fromString(r.getString("UUID")));
                        userCache.put(u.getId(), u);
                        userCacheOut.addLast(u.getId());
                        popCache();
                        return u;
                    } else {
                        return null;
                    }
                } finally {
                    GoldenApple.getInstanceDatabaseManager().closeResult(r);
                }
            } catch (SQLException e) {
                throw new RuntimeException("User lookup failed!", e);
            }
        }
    }
    
    @Override
    public void setUserSticky(long id, boolean sticky) {
        if (userCache.containsKey(id)) {
            if (sticky && userCacheOut.contains(id)) {
                userCacheOut.remove(id);
            } else if (!sticky && !userCacheOut.contains(id)) {
                userCacheOut.addLast(id);
                if (userCacheOut.size() > userCacheSize) {
                    long id2 = userCacheOut.pop();
                    userCache.remove(id2);
                }
            }
        } else {
            throw new IllegalArgumentException("User must be in cache to change stickyness!");
        }
    }
    
    @Override
    public boolean isUserSticky(long id) {
        return userCache.containsKey(id) && !userCacheOut.contains(id);
    }
    
    @Override
    public PermissionGroup getGroup(long id) {
        return groups.get(id);
    }
    
    @Override
    public PermissionGroup getGroup(String name) {
        for (Map.Entry<Long, PermissionGroup> entry : groups.entrySet()) {
            if (entry.getValue().getName().equalsIgnoreCase(name)) {
                return entry.getValue();
            }
        }
        
        return null;
    }
    
    @Override
    @Deprecated
    public boolean userExists(String name) {
        return findUser(name, false) != null;
    }
    
    @Override
    public PermissionUser createUser(String name) throws UuidLookupException, DuplicateNameException {
        UUID uuid;
        try {
            uuid = UUIDFetcher.getUUIDOf(name);
        } catch (Exception e) {
            throw new UuidLookupException("Failed to lookup UUID for '" + name + "'", e);
        }
        
        if (uuid != null) {
            return createUser(name, uuid);
        } else {
            throw new UuidLookupException("No UUID mapping found for '" + name + "'");
        }
    }
    
    @Override
    public PermissionUser createUser(String name, UUID uuid) throws DuplicateNameException {
        PermissionUser user = findUser(name, false);
        
        if (user != null && user.getUuid().equals(uuid)) {
            return user;
        } else if (user != null) {
            throw new DuplicateNameException("Username '" + name + "' is already in use by another player!");
        }
        
        user = getUser(uuid);
        
        if (user != null) {
            user.setName(name);
            user.save();
            
            return user;
        } else {
            try {
                ResultSet r = GoldenApple.getInstanceDatabaseManager().executeReturnGenKeys("INSERT INTO Users (Name, UUID) VALUES (?, ?)", name, uuid.toString());
                
                try {
                    r.next();
                    user = getUser(r.getLong(1));
                    
                    for (PermissionGroup g : defaultGroups) {
                        g.addUser(user);
                    }
                    
                    return user;
                } finally {
                    GoldenApple.getInstanceDatabaseManager().closeResult(r);
                }
            } catch (SQLException e) {
                throw new RuntimeException("Failed to create user!", e);
            }
        }
    }
    
    @Override
    public void addToOpGroups(IPermissionUser user) {
        for (PermissionGroup g : opGroups) {
            g.addUser(user);
        }
    }
    
    @Override
    public void addToDevGroups(IPermissionUser user) {
        for (PermissionGroup g : devGroups) {
            g.addUser(user);
        }
    }
    
    @Override
    public boolean canLogin(IPermissionUser user) {
        return requiredGroup == null || requiredGroup.isMember(user, false);
    }
    
    @Override
    public boolean isDev(IPermissionUser user) {
        // TODO Auto-generated method stub
        return false;
    }
    
    @Override
    public boolean groupExists(String name) {
        for (Map.Entry<Long, PermissionGroup> entry : groups.entrySet()) {
            if (entry.getValue().getName().equalsIgnoreCase(name)) {
                return true;
            }
        }
        
        return false;
    }
    
    @Override
    public PermissionGroup createGroup(String name) {
        if (groupExists(name)) {
            return getGroup(name);
        }
        try {
            ResultSet r = GoldenApple.getInstanceDatabaseManager().executeReturnGenKeys("INSERT INTO Groups (Name, Priority) VALUES (?, 1)", name);
            try {
                r.next();
                PermissionGroup g = new PermissionGroup(r.getLong(1), name, 1);
                groups.put(r.getLong(1), g);
                return g;
            } finally {
                GoldenApple.getInstanceDatabaseManager().closeResult(r);
            }
        } catch (SQLException e) {
            throw new RuntimeException("Failed to create group!", e);
        }
    }
    
    @Override
    public boolean isGroupProtected(IPermissionGroup group) {
        return defaultGroups.contains(group) || opGroups.contains(group) || devGroups.contains(group) || requiredGroup == group;
    }
    
    @Override
    public void deleteUser(long id) {
        if (id < 0) {
            throw new IllegalArgumentException("id cannot be < 0");
        } else if (isUserSticky(id)) {
            throw new UnsupportedOperationException("Cannot delete a sticky user");
        } else {
            IPermissionUser target = getUser(id);
            
            for (Long g : target.getParentGroups(true)) {
                getGroup(g).removeUser(target);
            }
            
            userCache.remove(id);
            userCacheOut.remove(id);
            
            try {
                GoldenApple.getInstanceDatabaseManager().execute("DELETE FROM Users WHERE ID=?", id);
            } catch (SQLException e) {
                throw new RuntimeException("Failed to remove user!", e);
            }
        }
    }
    
    @Override
    public void deleteGroup(long id) {
        if (id < 0) {
            throw new IllegalArgumentException("id cannot be < 0");
        } else {
            IPermissionGroup target = groups.remove(id);
            
            if (isGroupProtected(target)) {
                throw new UnsupportedOperationException("Cannot delete a group which is referenced in the config file!");
            }
            
            try {
                GoldenApple.getInstanceDatabaseManager().execute("DELETE FROM Groups WHERE ID=?", id);
                
                for (Long u : target.getUsers()) {
                    if (userCache.containsKey(u)) {
                        userCache.get(u).reloadFromDatabase();
                    }
                }
                
                for (Long g : target.getGroups()) {
                    groups.get(g).reloadFromDatabase();
                }
            } catch (SQLException e) {
                throw new RuntimeException("Failed to delete group!", e);
            }
        }
    }
    
    @Override
    public String getVariableDefaultValue(String variableName) {
        return variableDefaults.get(variableName);
    }
    
    @Override
    public void setVariableDefaultValue(String variableName, String defaultValue) {
        variableDefaults.put(variableName, defaultValue);
    }
    
    @Override
    public void setVariableDefaultValue(String variableName, Boolean defaultValue) {
        variableDefaults.put(variableName, (defaultValue) ? "true" : "false");
    }
    
    @Override
    public void setVariableDefaultValue(String variableName, Integer defaultValue) {
        variableDefaults.put(variableName, defaultValue.toString());
    }
    
    @Override
    public void close() {
        userCache = null;
        groups = null;
        rootNode = null;
    }
    
    @Override
    public void clearCache() {
        while (!userCacheOut.isEmpty()) {
            userCache.remove(userCacheOut.pop());
        }
        
        for (PermissionUser user : userCache.values()) {
            user.reloadFromDatabase();
        }
        
        for (PermissionGroup group : groups.values()) {
            group.reloadFromDatabase();
        }
    }
    
    private static class SimplePermissionNode implements PermissionNode {
        private String name;
        private SimplePermissionNode parent;
        
        private HashMap<String, Permission> permissions = new HashMap<String, Permission>();
        private HashMap<String, PermissionNode> nodes = new HashMap<String, PermissionNode>();
        
        private SimplePermissionNode(String name, SimplePermissionNode parent) {
            this.name = name;
            this.parent = parent;
            
            createPermission("*");
        }
        
        @Override
        public String getName() {
            return name;
        }
        
        @Override
        public String getFullName() {
            if (parent == null || parent.getName().isEmpty()) {
                return name;
            } else {
                return parent.getFullName() + "." + name;
            }
        }
        
        @Override
        public PermissionNode getParentNode() {
            return parent;
        }
        
        @Override
        public Collection<Permission> getChildPermissions() {
            return Collections.unmodifiableCollection(permissions.values());
        }
        
        @Override
        public Collection<PermissionNode> getChildNodes() {
            return Collections.unmodifiableCollection(nodes.values());
        }
        
        @Override
        public Permission getStarPermission() {
            return getPermission("*");
        }
        
        @Override
        public Permission getPermission(String name) {
            return permissions.get(name);
        }
        
        @Override
        public PermissionNode getNode(String name) {
            return nodes.get(name);
        }
        
        @Override
        public Permission createPermission(String name) {
            if (permissions.containsKey(name)) {
                return permissions.get(name);
            } else if (nodes.containsKey(name)) {
                return null;
            } else {
                SimplePermission p = new SimplePermission(name, this);
                permissions.put(name, p);
                return p;
            }
        }
        
        @Override
        public PermissionAlias createPermissionAlias(String name, Permission aliasOf) {
            if (permissions.containsKey(name)) {
                return (permissions.get(name) instanceof PermissionAlias) ? (PermissionAlias) permissions.get(name) : null;
            } else if (nodes.containsKey(name)) {
                return null;
            } else {
                SimplePermissionAlias p = new SimplePermissionAlias(aliasOf, name, this);
                permissions.put(name, p);
                return p;
            }
        }
        
        @Override
        public PermissionNode createNode(String name) {
            if (nodes.containsKey(name)) {
                return nodes.get(name);
            } else if (permissions.containsKey(name)) {
                return null;
            } else {
                SimplePermissionNode n = new SimplePermissionNode(name, this);
                nodes.put(name, n);
                return n;
            }
        }
    }
    
    private static class SimplePermission implements Permission {
        private String name;
        private SimplePermissionNode parent;
        
        private SimplePermission(String name, SimplePermissionNode parent) {
            this.name = name;
            this.parent = parent;
        }
        
        @Override
        public String getName() {
            return name;
        }
        
        @Override
        public String getFullName() {
            if (parent == null || parent.getName().isEmpty()) {
                return name;
            } else {
                return parent.getFullName() + "." + name;
            }
        }
        
        @Override
        public PermissionNode getParentNode() {
            return parent;
        }
        
        @Override
        public Collection<Permission> getEquivalentPermissions() {
            ArrayList<Permission> permissions = new ArrayList<Permission>();
            PermissionNode node = parent;
            
            while (node != null) {
                permissions.add(node.getStarPermission());
                node = node.getParentNode();
            }
            
            return Collections.unmodifiableCollection(permissions);
        }
    }
    
    private static class SimplePermissionAlias implements PermissionAlias {
        private Permission aliasOf;
        private String name;
        private SimplePermissionNode parent;
        
        private SimplePermissionAlias(Permission aliasOf, String name, SimplePermissionNode parent) {
            this.aliasOf = aliasOf;
            this.name = name;
            this.parent = parent;
        }
        
        @Override
        public String getName() {
            return aliasOf.getName();
        }
        
        @Override
        public String getFullName() {
            return aliasOf.getFullName();
        }
        
        @Override
        public PermissionNode getParentNode() {
            return aliasOf.getParentNode();
        }
        
        @Override
        public Collection<Permission> getEquivalentPermissions() {
            return aliasOf.getEquivalentPermissions();
        }
        
        @Override
        public Permission getAliasOf() {
            return aliasOf;
        }
        
        @Override
        public String getAliasName() {
            return name;
        }
        
        @Override
        public String getAliasFullName() {
            if (parent == null || parent.getName().isEmpty()) {
                return name;
            } else {
                return parent.getFullName() + "." + name;
            }
        }
        
        @Override
        public PermissionNode getAliasParentNode() {
            return parent;
        }
    }
}
