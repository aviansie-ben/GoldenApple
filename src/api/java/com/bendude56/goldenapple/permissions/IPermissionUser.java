package com.bendude56.goldenapple.permissions;

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
	 * Gets the locale preference of this user.
	 * 
	 * @return The name of the preferred locale for this user, if they have set
	 *         it. Otherwise, a null-value will be returned.
	 */
	public String getPreferredLocale();
	
	public void setPreferredLocale(String locale);

	/**
	 * Checks this user's preference for command syntax.
	 * 
	 * @return True if the user has complex commands enabled, false otherwise
	 */
	public boolean isUsingComplexCommands();

	/**
	 * Sets this user's preference for command syntax.
	 * 
	 * @param useComplex True to enable complex command syntax, false to enable
	 *            simple command syntax.
	 */
	public void setUsingComplexCommands(boolean useComplex);

	/**
	 * Checks whether this user has chosen to enable automatic locking of
	 * lockable blocks.
	 * 
	 * @return True if lockable blocks should be locked when placed, false
	 *         otherwise.
	 */
	public boolean isAutoLockEnabled();

	/**
	 * Sets whether blocks placed by this user should be automatically locked
	 * when they are placed if possible.
	 * 
	 * @param autoLock True to lock blocks when possible, false otherwise.
	 */
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
