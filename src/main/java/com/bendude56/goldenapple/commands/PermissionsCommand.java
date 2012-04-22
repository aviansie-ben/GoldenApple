package com.bendude56.goldenapple.commands;

import java.util.ArrayList;

import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;

import com.bendude56.goldenapple.GoldenApple;
import com.bendude56.goldenapple.User;
import com.bendude56.goldenapple.permissions.PermissionGroup;

public class PermissionsCommand implements CommandExecutor {
	public boolean onCommand(CommandSender sender, Command command, String commandLabel, String[] args) {
		GoldenApple instance = GoldenApple.getInstance();

		User user = User.getUser(sender);

		if (!user.hasPermission("goldenapple.permissions")) {
			GoldenApple.logPermissionFail(user, commandLabel, args, true);
			return true;
		}

		if (args.length == 0 || args[0] == "help") {
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
			} else if (args[i].equalsIgnoreCase("-d")) {
				if (add) {
					instance.locale.sendMessage(user, "error.permissions.conflict", false);
					return true;
				} else if (!addPermissions.isEmpty() || !remPermissions.isEmpty()) {
					instance.locale.sendMessage(user, "error.permissions.conflict", false);
					return true;
				} else {
					remove = true;
				}
			} else if (args[i].equalsIgnoreCase("-a")) {
				if (remove) {
					instance.locale.sendMessage(user, "error.permissions.conflict", false);
					return true;
				} else {
					add = true;
				}
			} else if (args[i].equalsIgnoreCase("-pa")) {
				if (i == args.length - 1 || args[i + 1].startsWith("-")) {
					instance.locale.sendMessage(user, "shared.parameterMissing", false, args[i]);
				} else if (remove) {
					instance.locale.sendMessage(user, "error.permissions.conflict", false);
					return true;
				} else {
					addPermissions.add(args[i + 1]);
					i++;
				}
			} else if (args[i].equalsIgnoreCase("-pr")) {
				if (i == args.length - 1 || args[i + 1].startsWith("-")) {
					instance.locale.sendMessage(user, "shared.parameterMissing", false, args[i]);
				} else if (remove) {
					instance.locale.sendMessage(user, "error.permissions.conflict", false);
					return true;
				} else {
					remPermissions.add(args[i + 1]);
					i++;
				}
			} else if (args[i].equalsIgnoreCase("-ua")) {
				if (i == args.length - 1 || args[i + 1].startsWith("-")) {
					instance.locale.sendMessage(user, "shared.parameterMissing", false, args[i]);
				} else if (remove) {
					instance.locale.sendMessage(user, "error.permissions.conflict", false);
					return true;
				} else {
					addUsers.add(args[i + 1]);
					i++;
				}
			} else if (args[i].equalsIgnoreCase("-ur")) {
				if (i == args.length - 1 || args[i + 1].startsWith("-")) {
					instance.locale.sendMessage(user, "shared.parameterMissing", false, args[i]);
				} else if (remove) {
					instance.locale.sendMessage(user, "error.permissions.conflict", false);
					return true;
				} else {
					remUsers.add(args[i + 1]);
					i++;
				}
			} else if (args[i].equalsIgnoreCase("-ga")) {
				if (i == args.length - 1 || args[i + 1].startsWith("-")) {
					instance.locale.sendMessage(user, "shared.parameterMissing", false, args[i]);
				} else if (remove) {
					instance.locale.sendMessage(user, "error.permissions.conflict", false);
					return true;
				} else {
					addGroups.add(args[i + 1]);
					i++;
				}
			} else if (args[i].equalsIgnoreCase("-gr")) {
				if (i == args.length - 1 || args[i + 1].startsWith("-")) {
					instance.locale.sendMessage(user, "shared.parameterMissing", false, args[i]);
				} else if (remove) {
					instance.locale.sendMessage(user, "error.permissions.conflict", false);
					return true;
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
			ArrayList<Long> users = new ArrayList<Long>();
			ArrayList<Long> groups = new ArrayList<Long>();
			for (String u : changeUsers) {
				long id = instance.permissions.getUserId(u);
				if (id != -1) {
					users.add(id);
				}
			}
			for (String g : changeGroups) {
				PermissionGroup group = instance.permissions.getGroup(g);
				if (group != null) {
					groups.add(group.getId());
				}
			}
			if (users.isEmpty() && groups.isEmpty()) {
				instance.locale.sendMessage(user, "error.permissions.noTarget", false, "-d");
				return true;
			}
			if (verified) {
				for (long id : users) {
					if (instance.permissions.isSticky(id)) {

					}
				}
			} else {

			}
			return true;
		}
		return true;
	}

	public void sendHelp(User user, String commandLabel) {
		GoldenApple.getInstance().locale.sendMessage(user, "header.help", false);
		GoldenApple.getInstance().locale.sendMessage(user, "help.permissions", true, commandLabel);
	}
}
