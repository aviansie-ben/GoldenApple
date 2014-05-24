package com.bendude56.goldenapple.permissions;

import java.util.UUID;

import org.bukkit.ChatColor;

/**
 * Represents a user in the GoldenApple permission database. A player
 * represented by an instance of this type may or may not be online. If this
 * player is online, one of the following conditions will be true:
 * <ol>
 * <li>This object is an instance of {@link com.bendude56.goldenapple.User}.</li>
 * <li>{@link com.bendude56.goldenapple.User#getUser(long)} returns non-null
 * when provided with the ID of this user instance.</li>
 * </ol>
 * 
 * @author ben_dude56
 */
public interface IPermissionUser extends IPermissionObject {
    /**
     * Gets the name of the user represented by this object. Where possible, the
     * ID number returned from {@link IPermissionObject#getId()} should be
     * stored in place of this name.
     */
    public String getName();
    
    /**
     * Gets the UUID of the user represented by this object. While this is
     * guaranteed to be unique, it is preferred to store the GoldenApple ID of
     * this user instead for performance reasons;
     */
    public UUID getUuid();
    
    /**
     * @deprecated Replaced by the new user variable system. Use
     * {@link IPermissionObject#getVariableString(String)} with the
     * "goldenapple.locale" variable.
     */
    @Deprecated
    public String getPreferredLocale();
    
    /**
     * @deprecated Replaced by the new user variable system. Use
     * {@link IPermissionObject#setVariable(String, String)} with the
     * "goldenapple.locale" variable.
     */
    @Deprecated
    public void setPreferredLocale(String locale);
    
    /**
     * @deprecated Replaced by the new user variable system. Use
     * {@link IPermissionObject#getVariableBoolean(String)} with the
     * "goldenapple.complexSyntax" variable.
     */
    @Deprecated
    public boolean isUsingComplexCommands();
    
    /**
     * @deprecated Replaced by the new user variable system. Use
     * {@link IPermissionObject#setVariable(String, Boolean)} with the
     * "goldenapple.complexSyntax" variable.
     */
    @Deprecated
    public void setUsingComplexCommands(boolean useComplex);
    
    /**
     * @deprecated Replaced by the new user variable system. Use
     * {@link IPermissionObject#getVariableBoolean(String)} with the
     * "goldenapple.lock.autoLock" variable.
     */
    @Deprecated
    public boolean isAutoLockEnabled();
    
    /**
     * @deprecated Replaced by the new user variable system. Use
     * {@link IPermissionObject#setVariable(String, Boolean)} with the
     * "goldenapple.lock.autoLock" variable.
     */
    @Deprecated
    public void setAutoLockEnabled(boolean autoLock);
    
    /**
     * Gets the color that this user's name should appear in chat.
     */
    public ChatColor getChatColor();
    
    /**
     * Gets the prefix that should preceed this user's name in chat.
     */
    public String getPrefix();
    
    public void reloadFromDatabase();
}
