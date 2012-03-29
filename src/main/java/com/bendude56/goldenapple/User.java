package com.bendude56.goldenapple;

import java.util.HashMap;
import java.util.List;

import org.bukkit.command.CommandSender;
import org.bukkit.command.ConsoleCommandSender;

import com.bendude56.goldenapple.permissions.IPermissionUser;
import com.bendude56.goldenapple.permissions.PermissionManager.Permission;
import com.bendude56.goldenapple.permissions.PermissionUser;

public class User implements IPermissionUser {
	
	private static HashMap<Long, User> activeUsers = new HashMap<Long, User>();
	private static User consoleUser = new User(-1);
	
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
			activeUsers.put(id, u = new User(id));
			return u;
		}
	}
	
	private PermissionUser permissions;
	private long id;
	
	private User(long id) {
		this.id = id;
		if (id == -1) {
			permissions = null;
		} else {
			permissions = GoldenApple.getInstance().permissions.getUser(id);
			GoldenApple.getInstance().permissions.setSticky(id, true);
		}
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
}
