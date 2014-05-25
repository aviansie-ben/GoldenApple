package com.bendude56.goldenapple.permissions;

import java.util.List;

import com.bendude56.goldenapple.permissions.PermissionManager.Permission;

/**
 * Represents an object in the GoldenApple permissions system. An object can be
 * assigned permissions, can be a member of groups, and can have variables set
 * on them.
 * 
 * @author ben_dude56
 */
public interface IPermissionObject {
    
    /**
     * Gets an ID number representing this permission object. This ID number is
     * guaranteed to be unique among objects <strong>of the same type</strong>.
     * It is possible for two objects of different types (e.g. a group and a
     * user) to have the same ID.
     * 
     * @return An ID number uniquely representing this permission object.
     */
    public long getId();
    
    /**
     * Returns a list of permissions that have been granted to this object. This
     * list should not be used for permission checking, since permissions may
     * have other equivalent permissions. (e.g. star permissions)
     * 
     * @param inherited If true, permissions inherited from groups that this
     * object is a member of will be included.
     * @return A list of permissions granted to this object.
     */
    public List<Permission> getPermissions(boolean inherited);
    
    /**
     * Checks whether or not this object has been granted a specified permission
     * or an equivalent permission. Permissions inherited from groups that this
     * object is a member of will be included in this search.
     * <p>
     * {@link #hasPermission(Permission)} is preferred and should be used in
     * place of this function wherever possible.
     * 
     * @param permission The name of the permission that this object should be
     * checked for.
     * @return True if the object has been granted the specified permission.
     * False otherwise.
     */
    public boolean hasPermission(String permission);
    
    /**
     * Checks whether or not this object has been granted a specified permission
     * or an equivalent permission. Permissions inherited from groups that this
     * object is a member of will be included in this search.
     * 
     * @param permission The permission that this object should be checked for.
     * @return True if the object has been granted the specified permission.
     * False otherwise.
     */
    public boolean hasPermission(Permission permission);
    
    /**
     * Checks whether or not this object has been granted a specified permission
     * or an equivalent permission.
     * <p>
     * {@link #hasPermission(Permission, boolean)} is preferred and should be
     * used in place of this function wherever possible.
     * 
     * @param permission The name of the permission that this object should be
     * checked for.
     * @param inherited If true, groups that this object inherits permissions
     * from will be considered.
     * @return True if the object has been granted the specified permission.
     * False otherwise.
     */
    public boolean hasPermission(String permission, boolean inherited);
    
    /**
     * Checks whether or not this object has been granted a specified permission
     * or an equivalent permission.
     * 
     * @param permission The permission that this object should be checked for.
     * @param inherited If true, groups that this object inherits permissions
     * from will be considered.
     * @return True if the object has been granted the specified permission.
     * False otherwise.
     */
    public boolean hasPermission(Permission permission, boolean inherited);
    
    /**
     * Checks whether this object has been explicitly granted the specified
     * permission. Equivalent permissions and group inheritance will be ignored.
     * This function should <strong>not</strong> be used to check whether a
     * object has permission to perform a specific action; in these cases,
     * {@link #hasPermission(Permission)} should be used instead.
     * 
     * @param permission The permission that this object should be checked for.
     * @return True if this object has been explicitly granted the specified
     * permission. False otherwise.
     */
    public boolean hasPermissionSpecific(Permission permission);
    
    /**
     * Explicitly grants the specified permission to this object. If this object
     * has already been granted the requested permission, nothing will occur.
     * 
     * @param permission The permission to be granted.
     */
    public void addPermission(Permission permission);
    
    /**
     * Explicitly grants the specified permission to this object. If this object
     * has already been granted the requested permission, nothing will occur.
     * <p>
     * {@link #addPermission(Permission)} is preferred and should be used in
     * place of this function wherever possible.
     * 
     * @param permission The name of the permission to be granted.
     */
    public void addPermission(String permission);
    
    /**
     * Removes the specified permission from the list of permissions that this
     * object has been explicitly granted. This will not work if the object is
     * either inheriting the specified permission from a group they are a member
     * of, or have been granted an equivalent permission. If this object has not
     * been granted the specified permission, nothing will occur.
     * 
     * @param permission The permission to be removed.
     */
    public void removePermission(Permission permission);
    
    /**
     * Removes the specified permission from the list of permissions that this
     * object has been explicitly granted. This will not work if the object is
     * either inheriting the specified permission from a group they are a member
     * of, or have been granted an equivalent permission. If this object has not
     * been granted the specified permission, nothing will occur.
     * <p>
     * {@link #removePermission(Permission)} is preferred and should be used in
     * place of this function wherever possible.
     * 
     * @param permission The name of the permission to be removed.
     */
    public void removePermission(String permission);
    
    /**
     * Gets a list of IDs of groups from which this object is inheriting
     * permissions.
     * 
     * @param directOnly If true, only groups that this object is explicitly a
     * member of will be included.
     * @return A list of IDs representing groups this object inherits from.
     */
    public List<Long> getParentGroups(boolean directOnly);
    
    /**
     * Gets the value of the specified variable as a string. If this object has
     * not had the variable explicitly defined, this function will first check
     * for inherited variables and if the variable is still undefined, the
     * default value will be returned.
     * 
     * @param variableName The name of the variable to retrieve.
     * @return The string value of the variable.
     */
    public String getVariableString(String variableName);
    
    /**
     * Gets the value of the specified variable as a boolean. Inherited
     * variables will be taken into account.
     * 
     * @param variableName The name of the variable to retrieve.
     * @return True if the value of the variable is "true". False otherwise.
     * @see #getVariableString(String)
     */
    public Boolean getVariableBoolean(String variableName);
    
    /**
     * Gets the value of the specified variable as an integer. Inherited
     * variables will be taken into account.
     * 
     * @param variableName The name of the variable to retrieve.
     * @return The integer value of the variable, or {@code null} if the
     * specified variable cannot be converted to an integer.
     * @see #getVariableString(String)
     */
    public Integer getVariableInteger(String variableName);
    
    /**
     * Gets the explicitly defined value of the specified variable as a string.
     * This function will <strong>not</strong> check for inherited variables,
     * nor will it return the default value if undefined.
     * 
     * @param variableName The name of the variable to retrieve.
     * @return The explicitly defined value of the variable if defined.
     * {@code null} otherwise.
     */
    public String getVariableSpecificString(String variableName);
    
    /**
     * Gets the explicitly defined value of the specified variable as a boolean.
     * Inherited variables will not be taken into account.
     * 
     * @param variableName The name of the variable to retrieve.
     * @return True if the value of the variable is "true", false if the value
     * is something else, or {@code null} if the value of the variable is
     * undefined.
     * @see #getVariableSpecificString(String)
     */
    public Boolean getVariableSpecificBoolean(String variableName);
    
    /**
     * Gets the explicitly defined value of the specified variable as an
     * integer. Inherited variables will not be taken into account.
     * 
     * @param variableName The name of the variable to retrieve.
     * @return The integer value of the specified variable, or {@code null} if
     * the value is undefined or cannot be converted into an integer.
     * @see #getVariableSpecificString(String)
     */
    public Integer getVariableSpecificInteger(String variableName);
    
    /**
     * Deletes the explicitly defined value of the specified variable on this
     * object. This function has no effect if no explicitly defined value of the
     * specified variable exists.
     * 
     * @param variableName The name of the variable to delete.
     */
    public void deleteVariable(String variableName);
    
    /**
     * Sets an explicit value for the specified variable on this object.
     * 
     * @param variableName The name of the variable to which the value should be
     * set.
     * @param value The value to set the variable to.
     */
    public void setVariable(String variableName, String value);
    
    /**
     * Sets an explicit value for the specified variable on this object.
     * 
     * @param variableName The name of the variable to which the value should be
     * set.
     * @param value The value to set the variable to.
     */
    public void setVariable(String variableName, Boolean value);
    
    /**
     * Sets an explicit value for the specified variable on this object.
     * 
     * @param variableName The name of the variable to which the value should be
     * set.
     * @param value The value to set the variable to.
     */
    public void setVariable(String variableName, Integer value);
}
