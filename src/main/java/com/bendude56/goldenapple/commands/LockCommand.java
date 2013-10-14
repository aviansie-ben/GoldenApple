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
import com.bendude56.goldenapple.permissions.IPermissionUser;
import com.bendude56.goldenapple.permissions.PermissionManager;

public class LockCommand extends DualSyntaxCommand {
	@SuppressWarnings("deprecation") // TODO Remove this when an alternative to getTargetBlock is released
	@Override
	public void onExecuteComplex(GoldenApple instance, User user, String commandLabel, String[] args) {
		Location lockLocation = null;
		long selectedId = -1;
		if (user.getHandle() instanceof Player)
			lockLocation = user.getPlayerHandle().getTargetBlock(null, 10).getLocation();

		if (args.length == 0 || args[0].equals("-?") || args[0].equals("help")) {
			sendHelp(user, commandLabel, true);
			return;
		}

		user.sendLocalizedMessage("header.lock");

		int arg = 0;
		if (args[0].equalsIgnoreCase("-s")) {
			try {
				selectedId = Long.parseLong(args[1]);
				arg = 2;
			} catch (NumberFormatException | ArrayIndexOutOfBoundsException e) {
				user.sendLocalizedMessage("shared.parameterMissing", "-s");
				return;
			}

			try {
				if (selectedId < 0 || !LockManager.getInstance().lockExists(selectedId)) {
					user.sendLocalizedMessage("error.lock.selectNotFound", String.valueOf(selectedId));
					return;
				} else if (args.length == 2) {
					user.sendLocalizedMessage("error.lock.selectNoAction");
					return;
				}
			} catch (SQLException e) {
				user.sendLocalizedMessage("error.lock.selectNotFound", String.valueOf(selectedId));
				return;
			}
		} else if (lockLocation == null || lockLocation.getBlock().getType() == Material.AIR) {
			user.sendLocalizedMessage("error.lock.invalidBlock");
			return;
		}

		LockedBlock lock = (selectedId >= 0) ? LockManager.getInstance().getLock(selectedId) : LockManager.getInstance().getLock(lockLocation);

		if (args[arg].equalsIgnoreCase("-c")) {
			arg++;
			if (selectedId >= 0) {
				user.sendLocalizedMessage("error.lock.selectCreate");
				return;
			} else if (!user.hasPermission(LockManager.addPermission)) {
				GoldenApple.logPermissionFail(user, commandLabel, args, true);
				return;
			} else if (LockManager.getInstance().getLock(lockLocation) != null) {
				user.sendLocalizedMessage("error.lock.create.alreadyExists");
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
				user.sendLocalizedMessage("error.lock.notFound");
			} else if (!lock.canModifyBlock(user)) {
				GoldenApple.logPermissionFail(user, commandLabel, args, true);
			} else {
				deleteLock(instance, user, lock.getLockId());
			}
			return;
		}

		if (lock == null) {
			user.sendLocalizedMessage("error.lock.notFound");
			return;
		}

		while (arg < args.length) {
			if (args[arg].equalsIgnoreCase("-in") || args[arg].equalsIgnoreCase("-in:u")) {
				arg++;
				if (arg == args.length) {
					user.sendLocalizedMessage("shared.parameterMissing", "-in:u");
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
					user.sendLocalizedMessage("shared.parameterMissing", "-in:i");
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
					user.sendLocalizedMessage("shared.parameterMissing", "-in:m");
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
					user.sendLocalizedMessage("shared.parameterMissing", "-in:f");
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
					user.sendLocalizedMessage("shared.parameterMissing", "-in:n");
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
					user.sendLocalizedMessage("shared.parameterMissing", "-gr");
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
			} else if (args[arg].equalsIgnoreCase("-r:on")) {
				if (!lock.canModifyBlock(user)) {
					GoldenApple.logPermissionFail(user, commandLabel, args, true);
					return;
				} else {
					setHopperAllow(user, lock, true);
					arg++;
				}
			} else if (args[arg].equalsIgnoreCase("-r:off")) {
				if (!lock.canModifyBlock(user)) {
					GoldenApple.logPermissionFail(user, commandLabel, args, true);
					return;
				} else {
					setHopperAllow(user, lock, false);
					arg++;
				}
			} else {
				user.sendLocalizedMessage("shared.unknownOption", args[arg]);
				arg++;
			}
		}
	}

	@SuppressWarnings("deprecation") // TODO Remove this when an alternative to getTargetBlock is released
	@Override
	public void onExecuteSimple(GoldenApple instance, User user, String commandLabel, String[] args) {
		if (!(user.getHandle() instanceof Player))
			user.sendLocalizedMessage("shared.noConsole");

		Location lockLocation = user.getPlayerHandle().getTargetBlock(null, 10).getLocation();

		if (args.length == 0 || args[0].equals("-?") || args[0].equals("help")) {
			sendHelp(user, commandLabel, false);
			return;
		}

		user.sendLocalizedMessage("header.lock");

		if (args[0].equalsIgnoreCase("create")) {
			if (!user.hasPermission(LockManager.addPermission)) {
				GoldenApple.logPermissionFail(user, commandLabel, args, true);
			} else if (LockManager.getInstance().getLock(lockLocation) != null) {
				user.sendLocalizedMessage("error.lock.create.alreadyExists");
			} else if (args.length > 2) {
				user.sendLocalizedMessage("shared.unknownOption", args[2]);
			} else if (args.length == 1) {
				createLock(instance, user, LockLevel.PRIVATE, lockLocation);
			} else if (args.length == 2 && args[1].equalsIgnoreCase("public")) {
				createLock(instance, user, LockLevel.PUBLIC, lockLocation);
			} else {
				user.sendLocalizedMessage("shared.unknownOption", args[1]);
			}
		} else {
			LockedBlock lock = LockManager.getInstance().getLock(lockLocation);

			if (lock == null) {
				user.sendLocalizedMessage("error.lock.notFound");
				return;
			}

			if (args[0].equalsIgnoreCase("delete")) {
				if (args.length > 1) {
					user.sendLocalizedMessage("shared.unknownOption", args[1]);
				} else if (!lock.canModifyBlock(user)) {
					GoldenApple.logPermissionFail(user, commandLabel, args, true);
				} else {
					deleteLock(instance, user, lock.getLockId());
				}
			} else if (args[0].equalsIgnoreCase("invite")) {
				if (args.length == 1) {
					user.sendLocalizedMessage("shared.parameterMissing", "invite");
				} else if (args.length > 2) {
					user.sendLocalizedMessage("shared.unknownOption", args[2]);
				} else if (!lock.canInvite(user)) {
					GoldenApple.logPermissionFail(user, commandLabel, args, true);
					return;
				} else {
					addUser(instance, user, lock, args[1], GuestLevel.USE);
				}
			} else if (args[0].equalsIgnoreCase("uninvite")) {
				if (args.length == 1) {
					user.sendLocalizedMessage("shared.parameterMissing", "uninvite");
				} else if (args.length > 2) {
					user.sendLocalizedMessage("shared.unknownOption", args[2]);
				} else if (!lock.hasFullControl(user)) {
					GoldenApple.logPermissionFail(user, commandLabel, args, true);
					return;
				} else {
					removeUser(instance, user, lock, args[1]);
				}
			} else if (args[0].equalsIgnoreCase("access")) {
				if (args.length == 1) {
					user.sendLocalizedMessage("shared.parameterMissing", "share");
				} else if (args.length > 2) {
					user.sendLocalizedMessage("shared.unknownOption", args[2]);
				} else if (!lock.canModifyBlock(user)) {
					GoldenApple.logPermissionFail(user, commandLabel, args, true);
					return;
				} else {
					changeAccess(instance, user, lock, args[1]);
				}
			} else if (args[0].equalsIgnoreCase("redstone")) {
				if (args.length == 1) {
					user.sendLocalizedMessage("shared.parameterMissing", "redstone");
				} else if (args.length > 2) {
					user.sendLocalizedMessage("shared.unknownOption", args[2]);
				} else if (!lock.canModifyBlock(user)) {
					GoldenApple.logPermissionFail(user, commandLabel, args, true);
					return;
				} else {
					setHopperAllow(user, lock, args[1].equalsIgnoreCase("on"));
				}
			} else if (args[0].equalsIgnoreCase("info")) {
				getInfo(instance, user, lock);
			} else {
				user.sendLocalizedMessage("shared.unknownOption", args[0]);
			}
		}
	}

	private LockedBlock createLock(GoldenApple instance, User user, LockLevel access, Location loc) {
		LockedBlock lock;
		try {
			lock = LockManager.getInstance().createLock(loc, access, user);
			getInfo(instance, user, lock);
			return lock;
		} catch (InvocationTargetException e) {
			user.sendLocalizedMessage("error.lock.create.invalidRegistered", LockedBlock.getBlock(loc.getBlock().getType()).plugin.getName());
			GoldenApple.log(Level.SEVERE, "Failed to lock block due to RegisteredBlock failure (Plugin: " + LockedBlock.getBlock(loc.getBlock().getType()).plugin.getName() + ")");
			GoldenApple.log(Level.SEVERE, e);
			return null;
		} catch (SQLException e) {
			user.sendLocalizedMessage("error.lock.create.ioError");
			return null;
		} catch (UnsupportedOperationException e) {
			user.sendLocalizedMessage("error.lock.invalidBlock");
			return null;
		}
	}

	private void deleteLock(GoldenApple instance, User user, long id) {
		try {
			LockManager.getInstance().deleteLock(id);
			user.sendLocalizedMessage("general.lock.delete.success");
		} catch (SQLException e) {
			user.sendLocalizedMessage("error.lock.delete.ioError");
		}
	}

	private void addUser(GoldenApple instance, User user, LockedBlock lock, String guest, GuestLevel level) {
		IPermissionUser gUser = PermissionManager.getInstance().getUser(guest);

		if (gUser == null) {
			user.sendLocalizedMessage("shared.userNotFoundWarning", guest);
		} else {
			lock.addUser(gUser, level);
			user.sendLocalizedMessage("general.lock.guest.add.success", gUser.getName());
		}
	}

	private void removeUser(GoldenApple instance, User user, LockedBlock lock, String guest) {
		IPermissionUser gUser = PermissionManager.getInstance().getUser(guest);

		if (gUser == null) {
			user.sendLocalizedMessage("shared.userNotFoundWarning", guest);
		} else {
			lock.remUser(gUser);
			user.sendLocalizedMessage("general.lock.guest.remove.success", gUser.getName());
		}
	}

	private void changeAccess(GoldenApple instance, User user, LockedBlock lock, String access) {
		LockLevel accessLevel;
		if (access.equalsIgnoreCase("private")) {
			accessLevel = LockLevel.PRIVATE;
			access = GoldenApple.getInstance().getLocalizationManager().getMessage(user, "general.lock.info.private");
		} else if (access.equalsIgnoreCase("public")) {
			accessLevel = LockLevel.PUBLIC;
			access = GoldenApple.getInstance().getLocalizationManager().getMessage(user, "general.lock.info.public");
		} else {
			user.sendLocalizedMessage("error.lock.access.unknown", access);
			return;
		}

		lock.setLevel(accessLevel);
		user.sendLocalizedMessage("general.lock.access.success", access);
	}

	private void getInfo(GoldenApple instance, User user, LockedBlock b) {
		String access = ChatColor.RED + "???";
		String redstone;
		
		switch (b.getLevel()) {
			case PUBLIC:
				access = GoldenApple.getInstance().getLocalizationManager().getMessage(user, "general.lock.info.public");
				break;
			case PRIVATE:
				access = GoldenApple.getInstance().getLocalizationManager().getMessage(user, "general.lock.info.private");
				break;
			default:
				break;
		}
		
		if (b.isRedstoneAccessApplicable())
			redstone = GoldenApple.getInstance().getLocalizationManager().getMessage(user, (b.getAllowExternal()) ? "general.lock.info.enabled" : "general.lock.info.disabled");
		else
			redstone = GoldenApple.getInstance().getLocalizationManager().getMessage(user, "general.lock.info.na");
		
		user.sendLocalizedMultilineMessage("general.lock.info", String.valueOf(b.getLockId()), PermissionManager.getInstance().getUser(b.getOwner()).getName(), access, b.getTypeIdentifier(), redstone);
	}
	
	private void setHopperAllow(User user, LockedBlock lock, boolean allow) {
		lock.setAllowExternal(allow);
		user.sendLocalizedMessage((allow) ? "general.lock.redstone.on" : "general.lock.redstone.off");
	}

	private void sendHelp(User user, String commandLabel, boolean complex) {
		user.sendLocalizedMessage("header.help");
		user.sendLocalizedMultilineMessage((complex) ? "help.lock.complex" : "help.lock.simple", commandLabel);
	}
}
