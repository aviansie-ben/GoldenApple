package com.bendude56.goldenapple.permissions;

import org.bukkit.ChatColor;

public interface IPermissionUser extends IPermissionObject {
	/**
	 * Gets the name of the user represented by this instance.
	 */
	public String getName();

	/**
	 * Gets the user's preferred locale, if they have specifically set one.
	 * 
	 * @return A string representing the user's preferred locale. If default,
	 *         will return an empty string.
	 */
	public String getPreferredLocale();

	/**
	 * Gets a boolean representing whether or not the user should have complex
	 * syntax enabled for commands.
	 */
	public boolean isUsingComplexCommands();

	/**
	 * Sets whether or not the user will have complex syntax enabled for
	 * commands.
	 * 
	 * @param useComplex True for complex syntax, false for basic syntax
	 */
	public void setUsingComplexCommands(boolean useComplex);

	/**
	 * Gets a boolean showing whether the user has auto-lock enabled. When
	 * auto-lock is enabled, certain blocks, when placed, will be automatically
	 * locked.
	 */
	public boolean isAutoLockEnabled();

	/**
	 * Sets whether certain blocks that the user places will be automatically
	 * locked.
	 * 
	 * @param autoLock Whether or not blocks listed in config.yml should be
	 *            locked
	 */
	public void setAutoLockEnabled(boolean autoLock);
	
	public ChatColor getChatColor();
	
	public String getPrefix();
}
