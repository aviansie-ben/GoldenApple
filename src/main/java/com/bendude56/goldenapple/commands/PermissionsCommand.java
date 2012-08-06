package com.bendude56.goldenapple.commands;

import java.sql.SQLException;
import java.util.ArrayList;

import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;

import com.bendude56.goldenapple.GoldenApple;
import com.bendude56.goldenapple.User;
import com.bendude56.goldenapple.permissions.PermissionGroup;

public class PermissionsCommand implements CommandExecutor {
	@Override
	public boolean onCommand(CommandSender sender, Command command, String commandLabel, String[] args) {
		GoldenApple instance = GoldenApple.getInstance();
		User user = User.getUser(sender);

		/*if (!user.hasPermission("goldenapple.permissions")) {
			GoldenApple.logPermissionFail(user, commandLabel, args, true);
			return true;
		}*/

		if (args.length == 0 || args[0].equalsIgnoreCase("help")) {
			sendHelp(user, commandLabel);
			return true;
		}

		instance.locale.sendMessage(user, "header.permissions", false);

		ArrayList<String> changeUsers = new ArrayList<String>();
		ArrayList<String> changeGroups = new ArrayList<String>();

		ArrayList<String> addPermissions = new ArrayList<String>();
		ArrayList<String> remPermissions = new ArrayList<String>();
		ArrayList<String> addUsers = new ArrayList<String>();
		ArrayList<String> remUsers = new ArrayList<String>();
		ArrayList<String> addGroups = new ArrayList<String>();
		ArrayList<String> remGroups = new ArrayList<String>();

		boolean add = false;
		boolean remove = false;
		boolean verified = false;

		for (int i = 0; i < args.length; i++) {
			if (args[i].equalsIgnoreCase("-u")) {
				if (i == args.length - 1 || args[i + 1].startsWith("-")) {
					instance.locale.sendMessage(user, "shared.parameterMissing", false, args[i]);
				} else {
					changeUsers.add(args[i + 1]);
					i++;
				}
			} else if (args[i].equalsIgnoreCase("-g")) {
				if (i == args.length - 1 || args[i + 1].startsWith("-")) {
					instance.locale.sendMessage(user, "shared.parameterMissing", false, args[i]);
				} else {
					changeGroups.add(args[i + 1]);
					i++;
				}
			} else if (args[i].equalsIgnoreCase("-r")) {
				remove = true;
			} else if (args[i].equalsIgnoreCase("-a")) {
				add = true;
			} else if (args[i].equalsIgnoreCase("-pa")) {
				if (i == args.length - 1 || args[i + 1].startsWith("-")) {
					instance.locale.sendMessage(user, "shared.parameterMissing", false, args[i]);
				} else {
					addPermissions.add(args[i + 1]);
					i++;
				}
			} else if (args[i].equalsIgnoreCase("-pr")) {
				if (i == args.length - 1 || args[i + 1].startsWith("-")) {
					instance.locale.sendMessage(user, "shared.parameterMissing", false, args[i]);
				} else {
					remPermissions.add(args[i + 1]);
					i++;
				}
			} else if (args[i].equalsIgnoreCase("-ua")) {
				if (i == args.length - 1 || args[i + 1].startsWith("-")) {
					instance.locale.sendMessage(user, "shared.parameterMissing", false, args[i]);
				} else {
					addUsers.add(args[i + 1]);
					i++;
				}
			} else if (args[i].equalsIgnoreCase("-ur")) {
				if (i == args.length - 1 || args[i + 1].startsWith("-")) {
					instance.locale.sendMessage(user, "shared.parameterMissing", false, args[i]);
				} else {
					remUsers.add(args[i + 1]);
					i++;
				}
			} else if (args[i].equalsIgnoreCase("-ga")) {
				if (i == args.length - 1 || args[i + 1].startsWith("-")) {
					instance.locale.sendMessage(user, "shared.parameterMissing", false, args[i]);
				} else {
					addGroups.add(args[i + 1]);
					i++;
				}
			} else if (args[i].equalsIgnoreCase("-gr")) {
				if (i == args.length - 1 || args[i + 1].startsWith("-")) {
					instance.locale.sendMessage(user, "shared.parameterMissing", false, args[i]);
				} else {
					remGroups.add(args[i + 1]);
					i++;
				}
			} else if (args[i].equalsIgnoreCase("-v")) {
				verified = true;
			}
		}
		if (!remove && !add && addPermissions.isEmpty() && remPermissions.isEmpty() && addUsers.isEmpty() && remUsers.isEmpty() && addGroups.isEmpty() && remGroups.isEmpty()) {
			instance.locale.sendMessage(user, "error.permissions.noAction", false);
			return true;
		}
		if (remove) {
			if (add || !remPermissions.isEmpty() || !addPermissions.isEmpty() || !addUsers.isEmpty() || !remUsers.isEmpty() || !addGroups.isEmpty() || !remGroups.isEmpty()) {
				instance.locale.sendMessage(user, "error.permissions.conflict", false);
				return true;
			}
			if (changeUsers.isEmpty() && changeGroups.isEmpty()) {
				instance.locale.sendMessage(user, "error.permissions.noTarget", false, "-r");
				return true;
			}
			if (verified) {
				ArrayList<Long> users = new ArrayList<Long>();
				ArrayList<Long> groups = new ArrayList<Long>();
				for (String u : changeUsers) {
					long id = instance.permissions.getUserId(u);
					if (id != -1) {
						users.add(id);
					} else if (verified) {
						instance.locale.sendMessage(user, "error.permissions.remove.userNotFoundWarn", false, u);
					}
				}
				for (String g : changeGroups) {
					PermissionGroup group = instance.permissions.getGroup(g);
					if (group != null) {
						groups.add(group.getId());
					} else if (verified) {
						instance.locale.sendMessage(user, "error.permissions.remove.groupNotFoundWarn", false, g);
					}
				}
				for (long id : users) {
					if (instance.permissions.isSticky(id)) {
						instance.locale.sendMessage(user, "error.permissions.remove.userOnline", false, instance.permissions.getUser(id).getName());
					} else {
						String name = instance.permissions.getUser(id).getName();
						try {
							instance.permissions.deleteUser(id);
							instance.locale.sendMessage(user, "general.permissions.remove.user", false, name);
						} catch (SQLException e) {
							instance.locale.sendMessage(user, "error.permissions.remove.userUnknown", false, name);
						}
					}
				}
				for (long id : groups) {
					String name = instance.permissions.getGroup(id).getName();
					try {
						instance.permissions.deleteGroup(id);
						instance.locale.sendMessage(user, "general.permissions.remove.group", false, name);
					} catch (SQLException e) {
						instance.locale.sendMessage(user, "error.permissions.remove.groupUnknown", false, name);
					}
				}
			} else {
				instance.locale.sendMessage(user, "general.permissions.remove.warnStart", false);
				for (String u : changeUsers) {
					instance.locale.sendMessage(user, "general.permissions.remove.warnUser", false, u);
				}
				for (String g : changeGroups) {
					instance.locale.sendMessage(user, "general.permissions.remove.warnGroup", false, g);
				}
				instance.locale.sendMessage(user, "general.permissions.remove.warnEnd", false);
			}
			return true;
		} else if (add) {
			if (!remPermissions.isEmpty() || !addUsers.isEmpty() || !remUsers.isEmpty() || !addGroups.isEmpty() || !remGroups.isEmpty()) {
				instance.locale.sendMessage(user, "error.permissions.conflict", false);
				return true;
			} else if (changeUsers.isEmpty() && changeGroups.isEmpty()) {
				instance.locale.sendMessage(user, "error.permissions.noTarget", false, "-a");
				return true;
			}
			for (String u : changeUsers) {
				try {
					if (instance.permissions.userExists(u)) {
						instance.locale.sendMessage(user, "error.permissions.add.userExists", false, u);
					} else {
						instance.permissions.createUser(u);
						instance.locale.sendMessage(user, "general.permissions.add.user", false, u);
					}
				} catch (SQLException e) {
					instance.locale.sendMessage(user, "error.permissions.add.userUnknown", false, u);
				}
			}
			for (String g : changeGroups) {
				try {
					if (instance.permissions.groupExists(g)) {
						instance.locale.sendMessage(user, "error.permissions.add.groupExists", false, g);
					} else {
						instance.permissions.createGroup(g);
						instance.locale.sendMessage(user, "general.permissions.add.group", false, g);
					}
				} catch (SQLException e) {
					instance.locale.sendMessage(user, "error.permissions.add.groupUnknown", false, g);
				}
			}
		} else {
			if (!addUsers.isEmpty()) {
				if (changeGroups.isEmpty()) {
					instance.locale.sendMessage(user, "error.permissions.noTarget", false, "-ua");
					return true;
				}
				
			}
		}
		return true;
	}

	public void sendHelp(User user, String commandLabel) {
		GoldenApple.getInstance().locale.sendMessage(user, "header.help", false);
		GoldenApple.getInstance().locale.sendMessage(user, "help.permissions", true, commandLabel);
	}
}
