package com.bendude56.goldenapple;

import java.util.HashMap;
import java.util.List;
import java.util.Map.Entry;

import org.bukkit.Bukkit;
import org.bukkit.command.CommandSender;
import org.bukkit.command.ConsoleCommandSender;
import org.bukkit.entity.Player;

import com.bendude56.goldenapple.permissions.IPermissionUser;
import com.bendude56.goldenapple.permissions.PermissionManager.Permission;
import com.bendude56.goldenapple.permissions.PermissionUser;

public class User implements IPermissionUser {
	private static HashMap<Long, User>	activeUsers	= new HashMap<Long, User>();
	private static User					consoleUser	= new User(-1, Bukkit.getConsoleSender(), false);

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
		} else if (GoldenApple.getInstance().permissions == null) {
			// Assign each user a temporary id in the event of a permissions
			// system failure
			long id = -1;
			for (Entry<Long, User> cached : activeUsers.entrySet()) {
				if (cached.getKey() > id)
					id = cached.getKey() + 1;
				if (cached.getValue().getHandle().equals(sender)) {
					return cached.getValue();
				}
			}
			User u;
			activeUsers.put(id, u = new User(id, sender, false));
			return u;
		}
		long id = GoldenApple.getInstance().permissions.getUserId(sender.getName());
		if (id == -1) {
			return null;
		} else if (activeUsers.containsKey(id)) {
			return activeUsers.get(id);
		} else {
			User u;
			activeUsers.put(id, u = new User(id, sender, true));
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
		if (GoldenApple.getInstance().permissions != null)
			GoldenApple.getInstance().permissions.setSticky(user.getId(), false);
	}

	/**
	 * Clears all User instances from memory. This should only be used if the
	 * status of the permissions module is changing. Clearing the cache at other
	 * times could have unintended side effects.
	 */
	public static void clearCache() {
		activeUsers.clear();
	}

	private PermissionUser	permissions;
	private CommandSender	handle;
	private long			id;

	private User(long id, CommandSender handle, boolean loadPermissions) {
		this.id = id;
		if (!loadPermissions) {
			permissions = null;
		} else {
			permissions = GoldenApple.getInstance().permissions.getUser(id);
			GoldenApple.getInstance().permissions.setSticky(id, true);
		}
		this.handle = handle;
	}

	@Override
	public String getName() {
		if (handle instanceof ConsoleCommandSender)
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
		if (permissions == null)
			throw new UnsupportedOperationException();
		else
			return permissions.getPermissions(inherited);
	}

	@Override
	public boolean hasPermission(String permission) {
		if (handle instanceof ConsoleCommandSender)
			return true;
		else if (permissions == null)
			return handle.isOp();
		else
			return permissions.hasPermission(permission);
	}

	@Override
	public boolean hasPermission(Permission permission) {
		if (handle instanceof ConsoleCommandSender)
			return true;
		else if (permissions == null)
			return handle.isOp();
		else
			return permissions.hasPermission(permission);
	}

	@Override
	public boolean hasPermission(String permission, boolean inherited) {
		if (handle instanceof ConsoleCommandSender)
			return !inherited;
		else if (permissions == null)
			return !inherited && handle.isOp();
		else
			return permissions.hasPermission(permission, inherited);
	}

	@Override
	public boolean hasPermission(Permission permission, boolean inherited) {
		if (handle instanceof ConsoleCommandSender)
			return !inherited;
		else if (permissions == null)
			return !inherited && handle.isOp();
		else
			return permissions.hasPermission(permission, inherited);
	}

	@Override
	public String getPreferredLocale() {
		if (permissions == null)
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

	@Override
	public void addPermission(Permission permission) {
		if (permissions == null)
			throw new UnsupportedOperationException();
		permissions.addPermission(permission);
	}

	@Override
	public void addPermission(String permission) {
		if (permissions == null)
			throw new UnsupportedOperationException();
		permissions.addPermission(permission);
	}

	@Override
	public void remPermission(Permission permission) {
		if (permissions == null)
			throw new UnsupportedOperationException();
		permissions.remPermission(permission);
	}

	@Override
	public void remPermission(String permission) {
		if (permissions == null)
			throw new UnsupportedOperationException();
		permissions.remPermission(permission);
	}
}
