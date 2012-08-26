package com.bendude56.goldenapple.commands;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.logging.Level;

import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;

import com.bendude56.goldenapple.GoldenApple;
import com.bendude56.goldenapple.User;
import com.bendude56.goldenapple.permissions.PermissionGroup;
import com.bendude56.goldenapple.permissions.PermissionManager;
import com.bendude56.goldenapple.permissions.PermissionManager.Permission;
import com.bendude56.goldenapple.permissions.PermissionUser;

public class PermissionsCommand implements CommandExecutor {
	@Override
	public boolean onCommand(CommandSender sender, Command command, String commandLabel, String[] args) {
		GoldenApple instance = GoldenApple.getInstance();
		User user = User.getUser(sender);

		if (args.length == 0 || args[0].equalsIgnoreCase("-?")) {
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
			if (!changeUsers.isEmpty() && !user.hasPermission(PermissionManager.userRemovePermission)) {
				GoldenApple.logPermissionFail(user, commandLabel, args, true);
				return true;
			} else if (!changeGroups.isEmpty() && !user.hasPermission(PermissionManager.groupRemovePermission)) {
				GoldenApple.logPermissionFail(user, commandLabel, args, true);
				return true;
			} else if (changeUsers.isEmpty() && changeGroups.isEmpty()) {
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
						instance.locale.sendMessage(user, "shared.userNotFoundWarn", false, u);
					}
				}
				for (String g : changeGroups) {
					PermissionGroup group = instance.permissions.getGroup(g);
					if (group != null) {
						groups.add(group.getId());
					} else if (verified) {
						instance.locale.sendMessage(user, "shared.groupNotFoundWarn", false, g);
					}
				}
				for (long id : users) {
					if (instance.permissions.isSticky(id)) {
						instance.locale.sendMessage(user, "error.permissions.remove.userOnline", false, instance.permissions.getUser(id).getName());
					} else {
						String name = instance.permissions.getUser(id).getName();
						try {
							instance.permissions.deleteUser(id);
							GoldenApple.log(Level.INFO, "User " + name + " (PU" + id + ") has been deleted by " + user.getName());
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
						GoldenApple.log(Level.INFO, "Group " + name + " (PG" + id + ") has been deleted by " + user.getName());
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
				String cmd = commandLabel;
				for (String a : args) {
					cmd += " " + a;
				}
				cmd += " -v";
				VerifyCommand.commands.put(user, cmd);
			}
			return true;
		} else if (add) {
			if (!remPermissions.isEmpty() || !addUsers.isEmpty() || !remUsers.isEmpty() || !addGroups.isEmpty() || !remGroups.isEmpty()) {
				instance.locale.sendMessage(user, "error.permissions.conflict", false);
				return true;
			} else if (!changeUsers.isEmpty() && !user.hasPermission(PermissionManager.userAddPermission)) {
				GoldenApple.logPermissionFail(user, commandLabel, args, true);
				return true;
			} else if (!changeGroups.isEmpty() && !user.hasPermission(PermissionManager.groupAddPermission)) {
				GoldenApple.logPermissionFail(user, commandLabel, args, true);
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
						PermissionUser newUser = instance.permissions.createUser(u);
						GoldenApple.log(Level.INFO, "User " + newUser.getName() + " (PU" + newUser.getId() + ") has been created by " + user.getName());
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
						PermissionGroup newGroup = instance.permissions.createGroup(g);
						GoldenApple.log(Level.INFO, "Group " + newGroup.getName() + " (PG" + newGroup.getId() + ") has been created by " + user.getName());
						instance.locale.sendMessage(user, "general.permissions.add.group", false, g);
					}
				} catch (SQLException e) {
					instance.locale.sendMessage(user, "error.permissions.add.groupUnknown", false, g);
				}
			}
		} else {
			ArrayList<PermissionUser> ul = new ArrayList<PermissionUser>();
			ArrayList<PermissionGroup> gl = new ArrayList<PermissionGroup>();
			
			resolveUsers(instance, user, changeUsers, ul);
			resolveGroups(instance, user, changeGroups, gl);
			
			if (!addUsers.isEmpty() || !remUsers.isEmpty()) {
				if (!user.hasPermission(PermissionManager.groupEditPermission)) {
					GoldenApple.logPermissionFail(user, commandLabel, args, true);
					return true;
				} else if (gl.isEmpty()) {
					instance.locale.sendMessage(user, "error.permissions.noTarget", false, "-ua/-ur");
					return true;
				}
				
				ArrayList<PermissionUser> ua = new ArrayList<PermissionUser>();
				ArrayList<PermissionUser> ur = new ArrayList<PermissionUser>();
				
				resolveUsers(instance, user, addUsers, ua);
				resolveUsers(instance, user, remUsers, ur);
				
				modifyGroupMembershipUsers(instance, user, gl, ua, ur);
			}
			
			if (!addGroups.isEmpty() || !remGroups.isEmpty()) {
				if (!user.hasPermission(PermissionManager.groupEditPermission)) {
					GoldenApple.logPermissionFail(user, commandLabel, args, true);
					return true;
				} else if (gl.isEmpty()) {
					instance.locale.sendMessage(user, "error.permissions.noTarget", false, "-ua/-ur");
					return true;
				}
				
				ArrayList<PermissionGroup> ga = new ArrayList<PermissionGroup>();
				ArrayList<PermissionGroup> gr = new ArrayList<PermissionGroup>();
				
				resolveGroups(instance, user, addGroups, ga);
				resolveGroups(instance, user, remGroups, gr);
				
				modifyGroupMembershipGroups(instance, user, gl, ga, gr);
			}
			
			if (!addPermissions.isEmpty() || !remPermissions.isEmpty()) {
				if (!ul.isEmpty() && !user.hasPermission(PermissionManager.userEditPermission)) {
					GoldenApple.logPermissionFail(user, commandLabel, args, true);
					return true;
				} else if (!gl.isEmpty() && !user.hasPermission(PermissionManager.groupEditPermission)) {
					GoldenApple.logPermissionFail(user, commandLabel, args, true);
					return true;
				} else if (gl.isEmpty() && ul.isEmpty()) {
					instance.locale.sendMessage(user, "error.permissions.noTarget", false, "-pa");
					return true;
				}
				ArrayList<Permission> addPerm = new ArrayList<Permission>();
				ArrayList<Permission> remPerm = new ArrayList<Permission>();
				
				resolvePermissions(instance, user, addPermissions, addPerm);
				resolvePermissions(instance, user, remPermissions, remPerm);
				
				for (PermissionUser u : ul) {
					updatePermissions(instance, user, u, addPerm, remPerm);
				}
				for (PermissionGroup g : gl) {
					updatePermissions(instance, user, g, addPerm, remPerm);
				}
			}
		}
		return true;
	}
	
	private void resolveUsers(GoldenApple instance, User user, ArrayList<String> usersInput, ArrayList<PermissionUser> usersOutput) {
		usersOutput.clear();
		for (String u : usersInput) {
			try {
				if (instance.permissions.userExists(u)) {
					usersOutput.add(instance.permissions.getUser(u));
				} else {
					instance.locale.sendMessage(user, "shared.userNotFoundWarning", false, u);
				}
			} catch (Exception e) {
				instance.locale.sendMessage(user, "shared.userNotFoundWarning", false, u);
			}
		}
	}
	
	private void resolveGroups(GoldenApple instance, User user, ArrayList<String> groupsInput, ArrayList<PermissionGroup> groupsOutput) {
		groupsOutput.clear();
		for (String g : groupsInput) {
			try {
				if (instance.permissions.groupExists(g)) {
					groupsOutput.add(instance.permissions.getGroup(g));
				} else {
					instance.locale.sendMessage(user, "shared.groupNotFoundWarning", false, g);
				}
			} catch (Exception e) {
				instance.locale.sendMessage(user, "shared.groupNotFoundWarning", false, g);
			}
		}
	}
	
	private void resolvePermissions(GoldenApple instance, User user, ArrayList<String> permsInput, ArrayList<Permission> permsOutput) {
		permsOutput.clear();
		for (String ps : permsInput) {
			Permission p = instance.permissions.getPermissionByName(ps);
			if (p == null) {
				instance.locale.sendMessage(user, "error.permissions.perm.notFound", false, ps);
			} else {
				permsOutput.add(p);
			}
		}
	}
	
	private void modifyGroupMembershipUsers(GoldenApple instance, User user, ArrayList<PermissionGroup> groups, ArrayList<PermissionUser> addUsers, ArrayList<PermissionUser> remUsers) {
		try {
			for (PermissionUser u : addUsers) {
				for (PermissionGroup ch : groups) {
					ch.addMember(u);
					GoldenApple.log(Level.INFO, "User " + u.getName() + " (PU" + u.getId() + ") has been added to group " + ch.getName() + " (PG" + ch.getId() + ") by " + user.getName());
					instance.locale.sendMessage(user, "general.permissions.member.addUser", false, u.getName(), ch.getName());
				}
			}
			for (PermissionUser u : remUsers) {
				for (PermissionGroup ch : groups) {
					ch.removeMember(u);
					GoldenApple.log(Level.INFO, "User " + u.getName() + " (PU" + u.getId() + ") has been removed from group " + ch.getName() + " (PG" + ch.getId() + ") by " + user.getName());
					instance.locale.sendMessage(user, "general.permissions.member.remUser", false, u.getName(), ch.getName());
				}
			}
		} catch (Exception e) {
			instance.locale.sendMessage(user, "error.permissions.member.unknown", false);
		}
	}
	
	private void modifyGroupMembershipGroups(GoldenApple instance, User user, ArrayList<PermissionGroup> groups, ArrayList<PermissionGroup> addGroups, ArrayList<PermissionGroup> remGroups) {
		try {
			for (PermissionGroup g : addGroups) {
				for (PermissionGroup ch : groups) {
					ch.addSubGroup(g);
					GoldenApple.log(Level.INFO, "Group " + g.getName() + " (PG" + g.getId() + ") has been added to group " + ch.getName() + " (PG" + ch.getId() + ") by " + user.getName());
					instance.locale.sendMessage(user, "general.permissions.member.addGroup", false, g.getName(), ch.getName());
				}
			}
			for (PermissionGroup g : remGroups) {
				for (PermissionGroup ch : groups) {
					ch.removeSubGroup(g);
					GoldenApple.log(Level.INFO, "Group " + g.getName() + " (PG" + g.getId() + ") has been removed from group " + ch.getName() + " (PG" + ch.getId() + ") by " + user.getName());
					instance.locale.sendMessage(user, "general.permissions.member.remUser", false, g.getName(), ch.getName());
				}
			}
		} catch (Exception e) {
			instance.locale.sendMessage(user, "error.permissions.member.unknown", false);
		}
	}
	
	private void updatePermissions(GoldenApple instance, User user, PermissionUser u, ArrayList<Permission> add, ArrayList<Permission> remove) {
		for (Permission p : add) {
			if (!u.hasPermission(p, false)) {
				u.addPermission(p);
				GoldenApple.log(Level.INFO, "User " + u.getName() + " (PU" + u.getId() + ") has been granted permission '" + p.getFullName() + "' by " + user.getName());
				instance.locale.sendMessage(user, "general.permissions.perm.add", false, p.getFullName(), u.getName());
			}
		}
		for (Permission p : remove) {
			if (u.hasPermission(p, false)) {
				u.removePermission(p);
				GoldenApple.log(Level.INFO, "User " + u.getName() + " (PU" + u.getId() + ") has had permission '" + p.getFullName() + "' revoked by " + user.getName());
				instance.locale.sendMessage(user, "general.permissions.perm.rem", false, p.getFullName(), u.getName());
			}
		}
	}
	
	private void updatePermissions(GoldenApple instance, User user, PermissionGroup g, ArrayList<Permission> add, ArrayList<Permission> remove) {
		for (Permission p : add) {
			if (!g.hasPermission(p, false)) {
				g.addPermission(p);
				GoldenApple.log(Level.INFO, "Group " + g.getName() + " (PG" + g.getId() + ") has been granted permission '" + p.getFullName() + "' by " + user.getName());
				instance.locale.sendMessage(user, "general.permissions.perm.add", false, p.getFullName(), g.getName());
			}
		}
		for (Permission p : remove) {
			if (g.hasPermission(p, false)) {
				g.removePermission(p);
				GoldenApple.log(Level.INFO, "Group " + g.getName() + " (PG" + g.getId() + ") has had permission '" + p.getFullName() + "' revoked by " + user.getName());
				instance.locale.sendMessage(user, "general.permissions.perm.rem", false, p.getFullName(), g.getName());
			}
		}
	}

	private void sendHelp(User user, String commandLabel) {
		GoldenApple.getInstance().locale.sendMessage(user, "header.help", false);
		GoldenApple.getInstance().locale.sendMessage(user, "help.permissions", true, commandLabel);
	}
}
