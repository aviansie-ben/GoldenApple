package com.bendude56.goldenapple.permissions;

import java.util.UUID;

import org.bukkit.ChatColor;

import com.bendude56.goldenapple.User;

/**
 * Represents a user in the GoldenApple permissions system. A user is a direct
 * one-to-one representation of a player who plays on the server. This may
 * represent either an online user or an offline user. This user is online if
 * one of the following conditions is met:
 * <ul>
 * <li>This object is an instance of {@link User}</li>
 * <li>{@link User#getUser(long)} returns a {@link User} when provided with the
 * ID number of this user as provided by {@link #getId()}</li>
 * </ul>
 * 
 * @author ben_dude56
 */
public interface IPermissionUser extends IPermissionObject {
    
    /**
     * Gets the username of this user. This name should be used when referring
     * to this user in a command's output or input. This name <strong>should
     * never</strong> be used internally when referring to a user, since it may
     * be changed; instead, the internal ID number of this user retrieved from
     * {@link #getId()} should be used.
     * 
     * @return This user's username.
     */
    public String getName();
    
    /**
     * Gets the UUID associated with this user as set by Mojang's servers. While
     * it is acceptable to store references to a user internally using this, it
     * is discouraged; instead, the internal ID number of this user retrieved
     * from {@link #getId()} should be used.
     * 
     * @return The Mojang UUID of this user.
     */
    public UUID getUuid();
    
    /**
     * Gets the chat color that this user's name should be displayed in when
     * shown in chat. If none of the user's groups specify a color,
     * {@code ChatColor.WHITE} will be returned.
     * 
     * @return The color to display the user's name in.
     */
    public ChatColor getChatColor();
    
    /**
     * Gets the prefix that should be shown alongside the user's name when they
     * speak in chat.
     * 
     * @return The prefix to display alongside the user's name when they talk,
     * or {@code null} if no prefix should be displayed.
     */
    public String getPrefix();
    
    /**
     * Causes the properties of this user to be reloaded from the database. Any
     * changes made directly to the database will then be reflected in this
     * object.
     */
    public void reloadFromDatabase();
    
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
}
