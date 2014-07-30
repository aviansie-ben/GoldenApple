package com.bendude56.goldenapple.permissions;

import java.util.Collection;
import java.util.UUID;

/**
 * Represents the controller in charge of managing all users and groups. This
 * class will automatically handle object caching and database querying.
 * 
 * @author ben_dude56
 */
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
    public static Permission userInfoPermission;
    
    // goldenapple.permissions.group
    public static PermissionNode groupNode;
    public static Permission groupAddPermission;
    public static Permission groupRemovePermission;
    public static Permission groupEditPermission;
    public static Permission groupInfoPermission;
    
    // goldenapple.module
    public static PermissionNode moduleNode;
    public static Permission moduleLoadPermission;
    public static Permission moduleUnloadPermission;
    public static Permission moduleClearCachePermission;
    public static Permission moduleQueryPermission;
    
    protected static PermissionManager instance;
    
    /**
     * Gets the active instance of this class that should be used for any
     * permissions-related queries.
     * 
     * @return The currently active {@link PermissionManager}.
     */
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
    
    /**
     * Gets the {@link Permission} object representing the permission with the
     * specified name.
     * 
     * @param name The name of the permission to be looked up.
     * @return The {@link Permission} object if this permission exists,
     * otherwise {@code null}.
     */
    public abstract Permission getPermissionByName(String name);
    
    /**
     * Gets the {@link Permission} object representing the permission with the
     * specified name. May also create non-existent permissions, however this
     * behaviour <strong>should not</strong> be used to register permissions;
     * instead, {@link PermissionNode#createPermission(String)} should be used
     * to register permissions.
     * 
     * @param name The name of the permission to be looked up.
     * @param create If true and the permission is not found, it will be
     * automatically created.
     * @return The {@link Permission} object if this permission exists, or if
     * was created, {@code null} otherwise.
     */
    public abstract Permission getPermissionByName(String name, boolean create);
    
    /**
     * Gets the {@link PermissionNode} object representing the node with the
     * specified name.
     * 
     * @param name The name of the node to be looked up.
     * @return The {@link PermissionNode} object if this node exists,
     * {@code null} otherwise.
     */
    public abstract PermissionNode getNodeByName(String name);
    
    /**
     * Gets the {@link Permission} object representing the node with the
     * specified name. May also create non-existent nodes, however this
     * behaviour <strong>should not</strong> be used to register nodes; instead,
     * {@link PermissionNode#createNode(String)} should be used to register
     * nodes.
     * 
     * @param name The name of the node to be looked up.
     * @param create If true and the node is not found, it will be automatically
     * created.
     * @return The {@link PermissionNode} object if this node exists, or if was
     * created, {@code null} otherwise.
     */
    public abstract PermissionNode getNodeByName(String name, boolean create);
    
    /**
     * Gets the {@link PermissionNode} representing the root of the permissions
     * system. All other permissions and nodes should be registered under this
     * node.
     * 
     * @return The {@link PermissionNode} representing the root of the
     * permission hierarchy.
     */
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
    
    /**
     * Looks up a user's internal ID by searching for them by UUID. A user
     * looked up via this function is <strong>not</strong> loaded into the
     * permissions cache.
     * 
     * @param uuid The UUID of the user to be looked up.
     * @return The internal ID of the user with the specified UUID.
     */
    public abstract long getUserId(UUID uuid);
    
    /**
     * @deprecated Old lookups by name are deprecated. Use
     * {@link PermissionManager#findUser(String, boolean)} when looking up users
     * by name for commands.
     */
    @Deprecated
    public abstract IPermissionUser getUser(String name);
    
    /**
     * Gets a user's {@link IPermissionUser} representation by their UUID. This
     * object may or may not be loaded from the internal cache.
     * 
     * @param uuid The UUID of the user that should be retrieved.
     * @return An {@link IPermissionUser} object representing the user with the
     * specified UUID.
     */
    public abstract IPermissionUser getUser(UUID uuid);
    
    /**
     * Gets a user's {@link IPermissionUser} representation by their internal ID
     * number. This object may or may not be loaded from the internal cache.
     * 
     * @param id The ID of the user that should be retrieved.
     * @return An {@link IPermissionUser} object representing the user with the
     * specified internal ID.
     */
    public abstract IPermissionUser getUser(long id);
    
    /**
     * Finds a user by their username. This function should
     * <strong>only</strong> be used when looking up users for commands! Users'
     * names may change without warning.
     * 
     * @param name The name of the user to look up.
     * @param allowPartialMatch If true, a partial match will be returned if
     * only one is found.
     * @return An {@link IPermissionUser} object representing the user with the
     * specified name.
     */
    public abstract IPermissionUser findUser(String name, boolean allowPartialMatch);
    
    /**
     * @deprecated Use {@link PermissionManager#findUser(String, boolean)} and
     * check for a null return instead!
     */
    @Deprecated
    public abstract boolean userExists(String name);
    
    /**
     * Adds a user to the database based only on their username. Mojang's
     * servers will be queried to retrieve the user's UUID. If the user already
     * exists, nothing will occur and the existing user will be returned.
     * 
     * @param name The username of the user to add.
     * @return The {@link IPermissionUser} object representing the newly created
     * user.
     * 
     * @throws UuidLookupException Mojang's servers could not be contacted for a
     * UUID lookup or the specified user does not exist.
     * @throws DuplicateNameException A user with this name already exists, but
     * the existing user's UUID does not match the response from Mojang's
     * servers.
     */
    public abstract IPermissionUser createUser(String name) throws UuidLookupException, DuplicateNameException;
    
    /**
     * Adds a user to the database based on their name and UUID. If the user
     * already exists, nothing will occur and the existing user will be
     * returned.
     * 
     * @param name The username of the user to add.
     * @param uuid The UUID of the user to add.
     * @return The {@link IPermissionUser} object representing the newly created
     * user.
     * 
     * @throws DuplicateNameException A user with this name already exists, but
     * the existing user's UUID does not match the provided UUID.
     */
    public abstract IPermissionUser createUser(String name, UUID uuid) throws DuplicateNameException;
    
    /**
     * Adds the specified user to the groups that the config states that ops
     * should be added to.
     * 
     * @param user The user to be added to the groups.
     */
    public abstract void addToOpGroups(IPermissionUser user);
    
    /**
     * Adds the specified user to the groups that the config states that devs
     * should be added to.
     * 
     * @param user The user to be added to the groups.
     */
    public abstract void addToDevGroups(IPermissionUser user);
    
    /**
     * Determines whether the user is in the group required for users to ba
     * allowed to play on this server.
     * 
     * @param user The user to be checked for login permissions.
     * @return True if the user should be allowed to login, false otherwise.
     */
    public abstract boolean canLogin(IPermissionUser user);
    
    /**
     * Determines whether the specified user is marked as a GoldenApple
     * developer.
     * 
     * @param user The user to be checked for developer status.
     * @return True if the user is marked as a GoldenApple developer, false
     * otherwise.
     */
    public abstract boolean isDev(IPermissionUser user);
    
    /**
     * Deletes the user with the specified internal ID. This user <strong>must
     * not</strong> be marked as sticky in the cache. Users can be checked for
     * stickyness using {@link #isUserSticky(long)}.
     * 
     * @param id The ID number of the user to delete.
     */
    public abstract void deleteUser(long id);
    
    /**
     * Sets the stickyness of the user with the specified ID. When a user is
     * marked as sticky, they will not be removed from the user cache and cannot
     * be deleted. Users should be marked as sticky when they are online, but
     * this behaviour <strong>should not</strong> be used to check whether a
     * user is online.
     * 
     * @param id The ID of the user to change the stickyness of.
     * @param sticky True if the user should be marked as sticky, false
     * otherwise.
     */
    public abstract void setUserSticky(long id, boolean sticky);
    
    /**
     * Checks whether a given user has been marked in the cache as sticky. When
     * a user is marked as sticky, they will not be removed from the cache and
     * cannot be deleted. Users should be marked as sticky when they are online,
     * but this behaviour <strong>should not</strong> be used to check whether a
     * user is online.
     * 
     * @param id The ID number of the user to check the stickyness of.
     * @return True if the user is marked as sticky, false otherwise.
     */
    public abstract boolean isUserSticky(long id);
    
    /**
     * Gets an {@link IPermissionGroup} representing the group with the
     * specified name. Group lookups by name should only be done when
     * interpreting command input. Otherwise, the group's ID number, as returned
     * by {@link IPermissionGroup#getId()}, should be stored and
     * {@link #getGroup(long)} used to retrieve the group object.
     * 
     * @param name The name of the group to look up.
     * @return An {@link IPermissionGroup} representing the specified group, or
     * {@code null} if the group was not found.
     */
    public abstract IPermissionGroup getGroup(String name);
    
    /**
     * Gets an {@link IPermissionGroup} representing the group with the
     * specified internal ID number.
     * 
     * @param id The ID number of the group to look up.
     * @return An {@link IPermissionGroup} representing the specified group, or
     * {@code null} if the group was not found.
     */
    public abstract IPermissionGroup getGroup(long id);
    
    /**
     * @deprecated Use {@link PermissionManager#getGroup(String)} and check for
     * a null return instead!
     */
    @Deprecated
    public abstract boolean groupExists(String name);
    
    /**
     * Creates a new group with the specified name. If a group with this name
     * already exists, nothing will occur and the existing group will be
     * returned.
     * 
     * @param name The name of the group that should be created.
     * @return The {@link IPermissionGroup} object representing the newly
     * created group.
     */
    public abstract IPermissionGroup createGroup(String name);
    
    /**
     * Deletes an existing group by its ID number. If no group with the
     * specified ID number exists, nothing will occur.
     * 
     * @param id The ID number of the group that should be deleted.
     */
    public abstract void deleteGroup(long id);
    
    /**
     * Checks whether a group is marked as protected. A protected group is
     * referenced in the configuration file and cannot be deleted unless the
     * configuration file is modified first.
     * 
     * @param group The group which will be checked for protection.
     * @return True if the group is protected, false otherwise.
     */
    public abstract boolean isGroupProtected(IPermissionGroup group);
    
    /**
     * Gets the default value of a specified variable. The default value will be
     * used if a user has no explicit value for the variable
     * <strong>and</strong> no group that they are a member of contains a value
     * for the variable.
     * 
     * @param variableName The name of the variable to get the default value
     * for.
     * @return The default value of the requested variable, as a string.
     */
    public abstract String getVariableDefaultValue(String variableName);
    
    /**
     * Sets the default value of a specified variable.
     * 
     * @param variableName The name of the variable on which the default value
     * should be set.
     * @param defaultValue The default value that should be applied to the
     * requested variable.
     * 
     * @see #getVariableDefaultValue(String)
     */
    public abstract void setVariableDefaultValue(String variableName, String defaultValue);
    
    /**
     * Sets the default value of a specified variable.
     * 
     * @param variableName The name of the variable on which the default value
     * should be set.
     * @param defaultValue The default value that should be applied to the
     * requested variable.
     * 
     * @see #getVariableDefaultValue(String)
     */
    public abstract void setVariableDefaultValue(String variableName, Boolean defaultValue);
    
    /**
     * Sets the default value of a specified variable.
     * 
     * @param variableName The name of the variable on which the default value
     * should be set.
     * @param defaultValue The default value that should be applied to the
     * requested variable.
     * 
     * @see #getVariableDefaultValue(String)
     */
    public abstract void setVariableDefaultValue(String variableName, Integer defaultValue);
    
    /**
     * Completely flushes all caches, destroys the existing permission hierarchy
     * and closes any references. This function should <strong>only</strong> be
     * used if the Permissions module is shutting down.
     * <p>
     * After calling this function, the result of calling any further functions
     * on this manager is undefined.
     */
    public abstract void close();
    
    /**
     * Clears any cached users and groups and reloads as much information as
     * possible directly from the database.
     */
    public abstract void clearCache();
    
    /**
     * Represents a permission in the GoldenApple permission hierarchy.
     * 
     * @author ben_dude56
     */
    public static interface Permission {
        /**
         * Gets the shortened name of this permission. This value is not unique,
         * however the combination of parent node and name should be unique.
         * 
         * @return The shortened name of this permission.
         */
        public String getName();
        
        /**
         * Gets the full name of this permission, which includes its position in
         * the permission hierarchy. This value is unique and should be used by
         * commands when referring to this permission.
         * 
         * @return The full name of this permission.
         */
        public String getFullName();
        
        /**
         * Gets the node on which this permission has been registered.
         * 
         * @return The node containing this permission.
         */
        public PermissionNode getParentNode();
        
        /**
         * Gets a list of permissions that are considered to be equivalent to
         * having this permission. Granting an object any of the permissions
         * from this list has the same effect as granting them this permission
         * directly.
         * 
         * @return A list of permissions equivalent to this one.
         */
        public Collection<Permission> getEquivalentPermissions();
    }
    
    /**
     * Represents a node in the GoldenApple permission hierarchy. A node can
     * contain child nodes as well as permissions, and is used to organize and
     * differentiate permissions.
     * 
     * @author ben_dude56
     */
    public static interface PermissionNode {
        
        /**
         * Gets the shortened name of this permission node. This value is not
         * unique, however the combination of parent node and name should be
         * unique.
         * 
         * @return The shortened name of this permission node, or an empty
         * string if this node is the root node.
         */
        public String getName();
        
        /**
         * Gets the full name of this node, which includes its position in the
         * permission hierarchy. This value is unique and should be used by
         * commands when referring to this node.
         * 
         * @return The full name of this node.
         */
        public String getFullName();
        
        /**
         * Gets the node on which this node has been registered.
         * 
         * @return The node containing this node, or {@code null} if this node
         * is the root node.
         */
        public PermissionNode getParentNode();
        
        /**
         * Gets a list of permissions that have been registered on this node.
         * This list cannot be directly modified; to add a child permission, use
         * {@link #createPermission(String)}.
         * 
         * @return A list of permissions registered under this node.
         */
        public Collection<Permission> getChildPermissions();
        
        /**
         * Gets a list of nodes that have been registered on this node. This
         * list cannot be directly modified; to add a child node, use
         * {@link #createNode(String)}.
         * 
         * @return A list of nodes registered under this node.
         */
        public Collection<PermissionNode> getChildNodes();
        
        /**
         * Gets the permission that is the equivalent of being given all
         * permissions that are children of this node.
         * 
         * @return A permission representing all permissions under this node.
         * 
         * @see Permission#getEquivalentPermissions()
         */
        public Permission getStarPermission();
        
        /**
         * Gets a specific permission that has been registered under this node
         * based on its shortened name.
         * 
         * @param name The name of the permission to retrieve.
         * @return A {@link Permission} representing the requested permission,
         * or {@code null} if the permission was not found.
         */
        public Permission getPermission(String name);
        
        /**
         * Gets a specific node that has been registered under this node based
         * on its shortened name.
         * 
         * @param name The name of the node to retrieve.
         * @return A {@link PermissionNode} representing the requested node, or
         * {@code null} if the node was not found.
         */
        public PermissionNode getNode(String name);
        
        /**
         * Creates a new permission with this node as the permission's parent.
         * If the permission already exists, the existing permission will be
         * returned and nothing will occur.
         * 
         * @param name The name of the permission to create.
         * @return The newly created permission, or {@code null} if a node
         * already exists with the same name.
         */
        public Permission createPermission(String name);
        
        /**
         * Creates a new node with this node as its parent. If the node already
         * exists, the existing node will be returned and nothing will occur.
         * 
         * @param name The name of the node to create.
         * @return The newly created node, or {@code null} if a permission
         * already exists with the same name.
         */
        public PermissionNode createNode(String name);
    }
}
