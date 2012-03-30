package com.bendude56.goldenapple;

import java.util.HashMap;
import java.util.List;

import org.bukkit.Bukkit;
import org.bukkit.command.CommandSender;
import org.bukkit.command.ConsoleCommandSender;
import org.bukkit.entity.Player;

import com.bendude56.goldenapple.permissions.IPermissionUser;
import com.bendude56.goldenapple.permissions.PermissionManager.Permission;
import com.bendude56.goldenapple.permissions.PermissionUser;

public class User implements IPermissionUser {
	private static HashMap<Long, User>	activeUsers	= new HashMap<Long, User>();
	private static User					consoleUser	= new User(-1, Bukkit.getConsoleSender());

	/**
	 * Gets a user instance from a
	 * {@link org.bukkit.craftbukkit.command.CommandSender} for use with other
	 * GoldenApple functions. If that
	 * {@link org.bukkit.craftbukkit.command.CommandSender} doesn't have a user
	 * instance already associated with it, one will be automatically created.
	 * 
	 * @param sender The instance that the returned user should be based upon
	 */
	public static User getUser(CommandSender sender) {
		if (sender instanceof ConsoleCommandSender) {
			return consoleUser;
		}
		long id = GoldenApple.getInstance().permissions.getUserId(sender.getName());
		if (id == -1) {
			return null;
		} else if (activeUsers.containsKey(id)) {
			return activeUsers.get(id);
		} else {
			User u;
			activeUsers.put(id, u = new User(id, sender));
			GoldenApple.getInstance().permissions.setSticky(u.getId(), true);
			return u;
		}
	}

	/**
	 * Unloads an instance of a User from memory. This method is designed for
	 * use when a player is logging off <strong>only</strong>. If used
	 * otherwise, unwanted side effects may occur.
	 * 
	 * @param user The user that should be unloaded
	 */
	public static void unloadUser(User user) {
		activeUsers.remove(user.getId());
		GoldenApple.getInstance().permissions.setSticky(user.getId(), false);
	}

	private PermissionUser	permissions;
	private CommandSender	handle;
	private long			id;

	private User(long id, CommandSender handle) {
		this.id = id;
		if (id == -1) {
			permissions = null;
		} else {
			permissions = GoldenApple.getInstance().permissions.getUser(id);
			GoldenApple.getInstance().permissions.setSticky(id, true);
		}
		this.handle = handle;
	}

	@Override
	public String getName() {
		if (id == -1)
			return "Server";
		else
			return permissions.getName();
	}

	@Override
	public long getId() {
		return id;
	}

	@Override
	public List<Permission> getPermissions(boolean inherited) {
		if (id == -1)
			throw new UnsupportedOperationException();
		else
			return permissions.getPermissions(inherited);
	}

	@Override
	public boolean hasPermission(String permission) {
		if (id == -1)
			return true;
		else
			return permissions.hasPermission(permission);
	}

	@Override
	public boolean hasPermission(Permission permission) {
		if (id == -1)
			return true;
		else
			return permissions.hasPermission(permission);
	}

	@Override
	public boolean hasPermission(String permission, boolean specific) {
		if (id == -1)
			return !specific;
		else
			return permissions.hasPermission(permission, specific);
	}

	@Override
	public boolean hasPermission(Permission permission, boolean specific) {
		if (id == -1)
			return !specific;
		else
			return permissions.hasPermission(permission, specific);
	}

	@Override
	public String getPreferredLocale() {
		if (id == -1)
			return "";
		else
			return permissions.getPreferredLocale();
	}

	/**
	 * Gets the {@link org.bukkit.command.CommandSender} that is represented by
	 * this instance.
	 */
	public CommandSender getHandle() {
		return handle;
	}

	/**
	 * Gets the {@link org.bukkit.entity.Player} that is represented by this
	 * instance, if this instance represents a player who is currently online.
	 * 
	 * @throws UnsupportedOperationException Thrown if the user represented by
	 *             this instance is not a Player.
	 */
	public Player getPlayerHandle() {
		if (handle instanceof Player) {
			return (Player)handle;
		} else {
			throw new UnsupportedOperationException();
		}
	}
}
