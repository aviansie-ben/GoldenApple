package com.bendude56.goldenapple.permissions;

import java.util.Collection;
import java.util.UUID;

public abstract class PermissionManager {
    // goldenapple
    public static PermissionNode goldenAppleNode;
    public static Permission importPermission;
    
    // goldenapple.permissions
    public static PermissionNode permissionNode;
    
    // goldenapple.permissions.user
    public static PermissionNode userNode;
    public static Permission userAddPermission;
    public static Permission userRemovePermission;
    public static Permission userEditPermission;
    
    // goldenapple.permissions.group
    public static PermissionNode groupNode;
    public static Permission groupAddPermission;
    public static Permission groupRemovePermission;
    public static Permission groupEditPermission;
    
    // goldenapple.module
    public static PermissionNode moduleNode;
    public static Permission moduleLoadPermission;
    public static Permission moduleUnloadPermission;
    public static Permission moduleClearCachePermission;
    public static Permission moduleQueryPermission;
    
    protected static PermissionManager instance;
    
    public static PermissionManager getInstance() {
        return PermissionManager.instance;
    }
    
    /**
     * @deprecated Register the permission directly from the node you wish to
     * register it on.
     */
    @Deprecated
    public abstract Permission registerPermission(String name, PermissionNode node);
    
    /**
     * @deprecated Register the permission directly from the node you wish to
     * register it on.
     */
    @Deprecated
    public abstract Permission registerPermission(String fullName);
    
    /**
     * @deprecated Register the node directly from the node you wish to register
     * it on.
     */
    @Deprecated
    public abstract PermissionNode registerNode(String name, PermissionNode node);
    
    public abstract Permission getPermissionByName(String name);
    public abstract Permission getPermissionByName(String name, boolean create);
    
    public abstract PermissionNode getNodeByName(String name);
    public abstract PermissionNode getNodeByName(String name, boolean create);
    public abstract PermissionNode getRootNode();
    
    /**
     * @deprecated Use {@link PermissionNode#getStarPermission()} on the node
     * retrieved from {@link PermissionManager#getRootNode()} instead.
     */
    @Deprecated
    public abstract Permission getRootStar();
    
    /**
     * @deprecated Old lookups by name are deprecated. Use
     * {@link PermissionManager#findUser(String, boolean)} when looking up users
     * by name for commands.
     */
    @Deprecated
    public abstract long getUserId(String name);
    public abstract long getUserId(UUID uuid);
    
    /**
     * @deprecated Old lookups by name are deprecated. Use
     * {@link PermissionManager#findUser(String, boolean)} when looking up users
     * by name for commands.
     */
    @Deprecated
    public abstract IPermissionUser getUser(String name);
    public abstract IPermissionUser getUser(UUID uuid);
    public abstract IPermissionUser getUser(long id);
    
    public abstract IPermissionUser findUser(String name, boolean allowPartialMatch);
    
    /**
     * @deprecated Use {@link PermissionManager#findUser(String, boolean)} and
     * check for a null return instead!
     */
    @Deprecated
    public abstract boolean userExists(String name);
    
    public abstract IPermissionUser createUser(String name) throws UuidLookupException, DuplicateNameException;
    public abstract IPermissionUser createUser(String name, UUID uuid) throws DuplicateNameException;
    
    public abstract void addToOpGroups(IPermissionUser user);
    public abstract void addToDevGroups(IPermissionUser user);
    
    public abstract boolean canLogin(IPermissionUser user);
    public abstract boolean isDev(IPermissionUser user);
    
    public abstract void deleteUser(long id);
    
    public abstract void setUserSticky(long id, boolean sticky);
    public abstract boolean isUserSticky(long id);
    
    public abstract IPermissionGroup getGroup(String name);
    public abstract IPermissionGroup getGroup(long id);
    
    /**
     * @deprecated Use {@link PermissionManager#getGroup(String)} and check for
     * a null return instead!
     */
    @Deprecated
    public abstract boolean groupExists(String name);
    
    public abstract IPermissionGroup createGroup(String name);
    public abstract void deleteGroup(long id);
    
    public abstract String getVariableDefaultValue(String variableName);
    public abstract void setVariableDefaultValue(String variableName, String defaultValue);
    public abstract void setVariableDefaultValue(String variableName, Boolean defaultValue);
    public abstract void setVariableDefaultValue(String variableName, Integer defaultValue);
    
    public abstract void close();
    public abstract void clearCache();
    
    public static interface Permission {
        public String getName();
        public String getFullName();
        
        public PermissionNode getParentNode();
        public Collection<Permission> getEquivalentPermissions();
    }
    
    public static interface PermissionNode {
        public String getName();
        public String getFullName();
        
        public PermissionNode getParentNode();
        
        public Collection<Permission> getChildPermissions();
        public Collection<PermissionNode> getChildNodes();
        public Permission getStarPermission();
        
        public Permission getPermission(String name);
        public PermissionNode getNode(String name);
        
        public Permission createPermission(String name);
        public PermissionNode createNode(String name);
    }
}
