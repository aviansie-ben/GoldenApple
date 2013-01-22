package com.bendude56.goldenapple.commands;

import java.lang.reflect.InvocationTargetException;
import java.sql.SQLException;
import java.util.logging.Level;

import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.entity.Player;

import com.bendude56.goldenapple.GoldenApple;
import com.bendude56.goldenapple.User;
import com.bendude56.goldenapple.lock.LockManager;
import com.bendude56.goldenapple.lock.LockedBlock;
import com.bendude56.goldenapple.lock.LockedBlock.GuestLevel;
import com.bendude56.goldenapple.lock.LockedBlock.LockLevel;
import com.bendude56.goldenapple.permissions.PermissionUser;

public class LockCommand extends DualSyntaxCommand {
	@Override
	public void onCommandComplex(GoldenApple instance, User user, String commandLabel, String[] args) {
		Location lockLocation = null;
		long selectedId = -1;
		if (user.getHandle() instanceof Player)
			lockLocation = user.getPlayerHandle().getTargetBlock(null, 10).getLocation();

		if (args.length == 0 || args[0].equals("-?") || args[0].equals("help")) {
			sendHelp(user, commandLabel, true);
			return;
		}

		instance.locale.sendMessage(user, "header.lock", false);

		int arg = 0;
		if (args[0].equalsIgnoreCase("-s")) {
			try {
				selectedId = Long.parseLong(args[1]);
				arg = 2;
			} catch (NumberFormatException | ArrayIndexOutOfBoundsException e) {
				instance.locale.sendMessage(user, "shared.parameterMissing", false, "-s");
				return;
			}

			try {
				if (selectedId < 0 || !instance.locks.lockExists(selectedId)) {
					instance.locale.sendMessage(user, "error.lock.selectNotFound", false, String.valueOf(selectedId));
					return;
				} else if (args.length == 2) {
					instance.locale.sendMessage(user, "error.lock.selectNoAction", false);
					return;
				}
			} catch (SQLException e) {
				instance.locale.sendMessage(user, "error.lock.selectNotFound", false, String.valueOf(selectedId));
				return;
			}
		} else if (lockLocation == null || lockLocation.getBlock().getType() == Material.AIR) {
			instance.locale.sendMessage(user, "error.lock.invalidBlock", false);
			return;
		}

		LockedBlock lock = (selectedId >= 0) ? instance.locks.getLock(selectedId) : instance.locks.getLock(lockLocation);

		if (args[arg].equalsIgnoreCase("-c")) {
			arg++;
			if (selectedId >= 0) {
				instance.locale.sendMessage(user, "error.lock.selectCreate", false);
				return;
			} else if (!user.hasPermission(LockManager.addPermission)) {
				GoldenApple.logPermissionFail(user, commandLabel, args, true);
				return;
			} else if (instance.locks.getLock(lockLocation) != null) {
				instance.locale.sendMessage(user, "error.lock.create.alreadyExists", false);
				return;
			}

			LockLevel accessLevel = LockLevel.PRIVATE;
			if (args.length > arg && args[arg].equalsIgnoreCase("-p")) {
				accessLevel = LockLevel.PUBLIC;
				arg++;
			}

			lock = createLock(instance, user, accessLevel, lockLocation);

			if (lock == null)
				return;
		} else if (args[arg].equalsIgnoreCase("-d")) {
			if (lock == null) {
				instance.locale.sendMessage(user, "error.lock.notFound", false);
			} else if (!lock.canModifyBlock(user)) {
				GoldenApple.logPermissionFail(user, commandLabel, args, true);
			} else {
				deleteLock(instance, user, lock.getLockId());
			}
			return;
		}

		if (lock == null) {
			instance.locale.sendMessage(user, "error.lock.notFound", false);
			return;
		}

		while (arg < args.length) {
			if (args[arg].equalsIgnoreCase("-in") || args[arg].equalsIgnoreCase("-in:u")) {
				arg++;
				if (arg == args.length) {
					instance.locale.sendMessage(user, "shared.parameterMissing", false, "-in:u");
				} else if (!lock.canInvite(user)) {
					GoldenApple.logPermissionFail(user, commandLabel, args, true);
					return;
				} else {
					addUser(instance, user, lock, args[arg], GuestLevel.USE);
					arg++;
				}
			} else if (args[arg].equalsIgnoreCase("-in:i")) {
				arg++;
				if (arg == args.length) {
					instance.locale.sendMessage(user, "shared.parameterMissing", false, "-in:i");
				} else if (!lock.hasFullControl(user)) {
					GoldenApple.logPermissionFail(user, commandLabel, args, true);
					return;
				} else {
					addUser(instance, user, lock, args[arg], GuestLevel.ALLOW_INVITE);
					arg++;
				}
			} else if (args[arg].equalsIgnoreCase("-in:m")) {
				arg++;
				if (arg == args.length) {
					instance.locale.sendMessage(user, "shared.parameterMissing", false, "-in:m");
				} else if (!lock.hasFullControl(user)) {
					GoldenApple.logPermissionFail(user, commandLabel, args, true);
					return;
				} else {
					addUser(instance, user, lock, args[arg], GuestLevel.ALLOW_BLOCK_MODIFY);
					arg++;
				}
			} else if (args[arg].equalsIgnoreCase("-in:f")) {
				arg++;
				if (arg == args.length) {
					instance.locale.sendMessage(user, "shared.parameterMissing", false, "-in:f");
				} else if (!lock.hasFullControl(user)) {
					GoldenApple.logPermissionFail(user, commandLabel, args, true);
					return;
				} else {
					addUser(instance, user, lock, args[arg], GuestLevel.FULL);
					arg++;
				}
			} else if (args[arg].equalsIgnoreCase("-in:n")) {
				arg++;
				if (arg == args.length) {
					instance.locale.sendMessage(user, "shared.parameterMissing", false, "-in:n");
				} else if (!lock.hasFullControl(user)) {
					GoldenApple.logPermissionFail(user, commandLabel, args, true);
					return;
				} else {
					removeUser(instance, user, lock, args[arg]);
					arg++;
				}
			} else if (args[arg].equalsIgnoreCase("-a")) {
				arg++;
				if (arg == args.length) {
					instance.locale.sendMessage(user, "shared.parameterMissing", false, "-gr");
				} else if (!lock.canModifyBlock(user)) {
					GoldenApple.logPermissionFail(user, commandLabel, args, true);
					return;
				} else {
					changeAccess(instance, user, lock, args[arg]);
					arg++;
				}
			} else if (args[arg].equalsIgnoreCase("-i")) {
				getInfo(instance, user, lock);
				arg++;
			} else {
				instance.locale.sendMessage(user, "shared.unknownOption", false, args[arg]);
				arg++;
			}
		}
	}

	public void onCommandSimple(GoldenApple instance, User user, String commandLabel, String[] args) {
		if (!(user.getHandle() instanceof Player))
			instance.locale.sendMessage(user, "shared.noConsole", false);

		Location lockLocation = user.getPlayerHandle().getTargetBlock(null, 10).getLocation();

		if (args.length == 0 || args[0].equals("-?") || args[0].equals("help")) {
			sendHelp(user, commandLabel, false);
			return;
		}

		instance.locale.sendMessage(user, "header.lock", false);

		if (args[0].equalsIgnoreCase("create")) {
			if (!user.hasPermission(LockManager.addPermission)) {
				GoldenApple.logPermissionFail(user, commandLabel, args, true);
			} else if (instance.locks.getLock(lockLocation) != null) {
				instance.locale.sendMessage(user, "error.lock.create.alreadyExists", false);
			} else if (args.length > 2) {
				instance.locale.sendMessage(user, "shared.unknownOption", false, args[2]);
			} else if (args.length == 1) {
				createLock(instance, user, LockLevel.PRIVATE, lockLocation);
			} else if (args.length == 2 && args[1].equalsIgnoreCase("public")) {
				createLock(instance, user, LockLevel.PUBLIC, lockLocation);
			} else {
				instance.locale.sendMessage(user, "shared.unknownOption", false, args[1]);
			}
		} else {
			LockedBlock lock = instance.locks.getLock(lockLocation);

			if (lock == null) {
				instance.locale.sendMessage(user, "error.lock.notFound", false);
				return;
			}

			if (args[0].equalsIgnoreCase("delete")) {
				if (args.length > 1) {
					instance.locale.sendMessage(user, "shared.unknownOption", false, args[1]);
				} else if (!lock.canModifyBlock(user)) {
					GoldenApple.logPermissionFail(user, commandLabel, args, true);
				} else {
					deleteLock(instance, user, lock.getLockId());
				}
			} else if (args[0].equalsIgnoreCase("invite")) {
				if (args.length == 1) {
					instance.locale.sendMessage(user, "shared.parameterMissing", false, "invite");
				} else if (args.length > 2) {
					instance.locale.sendMessage(user, "shared.unknownOption", false, args[2]);
				} else if (!lock.canInvite(user)) {
					GoldenApple.logPermissionFail(user, commandLabel, args, true);
					return;
				} else {
					addUser(instance, user, lock, args[1], GuestLevel.USE);
				}
			} else if (args[0].equalsIgnoreCase("uninvite")) {
				if (args.length == 1) {
					instance.locale.sendMessage(user, "shared.parameterMissing", false, "uninvite");
				} else if (args.length > 2) {
					instance.locale.sendMessage(user, "shared.unknownOption", false, args[2]);
				} else if (!lock.hasFullControl(user)) {
					GoldenApple.logPermissionFail(user, commandLabel, args, true);
					return;
				} else {
					removeUser(instance, user, lock, args[1]);
				}
			} else if (args[0].equalsIgnoreCase("access")) {
				if (args.length == 1) {
					instance.locale.sendMessage(user, "shared.parameterMissing", false, "share");
				} else if (args.length > 2) {
					instance.locale.sendMessage(user, "shared.unknownOption", false, args[2]);
				} else if (!lock.canModifyBlock(user)) {
					GoldenApple.logPermissionFail(user, commandLabel, args, true);
					return;
				} else {
					changeAccess(instance, user, lock, args[1]);
				}
			} else if (args[0].equalsIgnoreCase("info")) {
				getInfo(instance, user, lock);
			} else {
				instance.locale.sendMessage(user, "shared.unknownOption", false, args[0]);
			}
		}
	}

	private LockedBlock createLock(GoldenApple instance, User user, LockLevel access, Location loc) {
		LockedBlock lock;
		try {
			lock = instance.locks.createLock(loc, access, user);
			getInfo(instance, user, lock);
			return lock;
		} catch (InvocationTargetException e) {
			instance.locale.sendMessage(user, "error.lock.create.invalidRegistered", false, LockedBlock.getBlock(loc.getBlock().getType()).plugin.getName());
			GoldenApple.log(Level.SEVERE, "Failed to lock block due to RegisteredBlock failure (Plugin: " + LockedBlock.getBlock(loc.getBlock().getType()).plugin.getName() + ")");
			GoldenApple.log(Level.SEVERE, e);
			return null;
		} catch (SQLException e) {
			instance.locale.sendMessage(user, "error.lock.create.ioError", false);
			return null;
		} catch (UnsupportedOperationException e) {
			instance.locale.sendMessage(user, "error.lock.invalidBlock", false);
			return null;
		}
	}

	private void deleteLock(GoldenApple instance, User user, long id) {
		try {
			instance.locks.deleteLock(id);
			instance.locale.sendMessage(user, "general.lock.delete.success", false);
		} catch (SQLException e) {
			instance.locale.sendMessage(user, "error.lock.delete.ioError", false);
		}
	}

	private void addUser(GoldenApple instance, User user, LockedBlock lock, String guest, GuestLevel level) {
		PermissionUser gUser = instance.permissions.getUser(guest);

		if (gUser == null) {
			instance.locale.sendMessage(user, "shared.userNotFoundWarning", false, guest);
		} else {
			lock.addUser(gUser, level);
			instance.locale.sendMessage(user, "general.lock.guest.add.success", false, gUser.getName());
		}
	}

	private void removeUser(GoldenApple instance, User user, LockedBlock lock, String guest) {
		PermissionUser gUser = instance.permissions.getUser(guest);

		if (gUser == null) {
			instance.locale.sendMessage(user, "shared.userNotFoundWarning", false, guest);
		} else {
			lock.remUser(gUser);
			instance.locale.sendMessage(user, "general.lock.guest.remove.success", false, gUser.getName());
		}
	}

	private void changeAccess(GoldenApple instance, User user, LockedBlock lock, String access) {
		LockLevel accessLevel;
		if (access.equalsIgnoreCase("private")) {
			accessLevel = LockLevel.PRIVATE;
			access = GoldenApple.getInstance().locale.getMessage(user, "general.lock.info.private");
		} else if (access.equalsIgnoreCase("public")) {
			accessLevel = LockLevel.PUBLIC;
			access = GoldenApple.getInstance().locale.getMessage(user, "general.lock.info.public");
		} else {
			instance.locale.sendMessage(user, "error.lock.access.unknown", false, access);
			return;
		}

		lock.setLevel(accessLevel);
		instance.locale.sendMessage(user, "general.lock.access.success", false, access);
	}

	private void getInfo(GoldenApple instance, User user, LockedBlock b) {
		String access = ChatColor.RED + "???";
		switch (b.getLevel()) {
			case PUBLIC:
				access = GoldenApple.getInstance().locale.getMessage(user, "general.lock.info.public");
				break;
			case PRIVATE:
				access = GoldenApple.getInstance().locale.getMessage(user, "general.lock.info.private");
				break;
			default:
				break;
		}
		GoldenApple.getInstance().locale.sendMessage(user, "general.lock.info", true, String.valueOf(b.getLockId()), instance.permissions.getUser(b.getOwner()).getName(), access, b.getTypeIdentifier());
	}

	private void sendHelp(User user, String commandLabel, boolean complex) {
		GoldenApple.getInstance().locale.sendMessage(user, "header.help", false);
		GoldenApple.getInstance().locale.sendMessage(user, (complex) ? "help.lock.complex" : "help.lock.simple", true, commandLabel);
	}
}
