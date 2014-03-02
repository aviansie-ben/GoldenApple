package com.bendude56.goldenapple.lock.command;

import java.lang.reflect.InvocationTargetException;
import java.sql.SQLException;
import java.util.logging.Level;

import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;
import org.bukkit.util.BlockIterator;

import com.bendude56.goldenapple.GoldenApple;
import com.bendude56.goldenapple.User;
import com.bendude56.goldenapple.command.DualSyntaxCommand;
import com.bendude56.goldenapple.lock.LockManager;
import com.bendude56.goldenapple.lock.LockedBlock;
import com.bendude56.goldenapple.lock.LockedBlock.GuestLevel;
import com.bendude56.goldenapple.lock.LockedBlock.LockLevel;
import com.bendude56.goldenapple.permissions.IPermissionGroup;
import com.bendude56.goldenapple.permissions.IPermissionUser;
import com.bendude56.goldenapple.permissions.PermissionManager;
import com.bendude56.goldenapple.util.ComplexArgumentParser;
import com.bendude56.goldenapple.util.ComplexArgumentParser.ArgumentInfo;

public class LockCommand extends DualSyntaxCommand {

	@Override
	public void onExecuteComplex(GoldenApple instance, User user, String commandLabel, String[] args) {
		ComplexArgumentParser arg = new ComplexArgumentParser(getArguments());
		
		if (!arg.parse(user, args)) return;
		
		user.sendLocalizedMessage("header.lock");
		
		LockedBlock target = null;
		
		if (arg.isDefined("override-on")) {
			if (!LockManager.getInstance().canOverride(user)) {
				GoldenApple.logPermissionFail(user, commandLabel, args, true);
			} else {
				LockManager.getInstance().setOverrideOn(user, true);
				user.sendLocalizedMessage("general.lock.override.on");
			}
			return;
		} else if (arg.isDefined("override-off")) {
			if (!LockManager.getInstance().canOverride(user)) {
				GoldenApple.logPermissionFail(user, commandLabel, args, true);
			} else {
				LockManager.getInstance().setOverrideOn(user, false);
				user.sendLocalizedMessage("general.lock.override.off");
			}
			return;
		}
		
		if (arg.isDefined("create")) {
			Location lockLocation = findLockableBlock(user, 10);
			if (!user.hasPermission(LockManager.addPermission)) {
				GoldenApple.logPermissionFail(user, commandLabel, args, true);
			} else if (lockLocation == null) {
				user.sendLocalizedMessage("error.lock.invalidBlock");
			} else if (LockManager.getInstance().getLock(lockLocation) != null) {
				user.sendLocalizedMessage("error.lock.create.alreadyExists");
			} else {
				target = createLock(instance, user, (arg.isDefined("public")) ? LockLevel.PUBLIC : LockLevel.PRIVATE, lockLocation);
			}
		}
		
		if (target == null && arg.isDefined("select")) {
			target = LockManager.getInstance().getLock(arg.getLong("select"));
			
			if (target == null) {
				user.sendLocalizedMessage("error.lock.selectNotFound", arg.getLong("select") + "");
				return;
			}
		} else if (target == null) {
			target = findLock(user, 10);
			
			if (target == null) {
				user.sendLocalizedMessage("error.lock.notFound");
				return;
			}
		}
		
		if (arg.isDefined("delete")) {
			if (!target.canModifyBlock(user)) {
				GoldenApple.logPermissionFail(user, commandLabel, args, true);
				return;
			}
			
			deleteLock(instance, user, target.getLockId());
			return;
		}
		
		if (arg.isDefined("access")) {
			if (!target.canModifyBlock(user)) {
				GoldenApple.logPermissionFail(user, commandLabel, args, true);
				return;
			}
			
			changeAccess(instance, user, target, arg.getString("access"));
		}
		
		if (arg.isDefined("redstone-on")) {
			if (!target.canModifyBlock(user)) {
				GoldenApple.logPermissionFail(user, commandLabel, args, true);
				return;
			}
			
			setHopperAllow(user, target, true);
		} else if (arg.isDefined("redstone-off")) {
			if (!target.canModifyBlock(user)) {
				GoldenApple.logPermissionFail(user, commandLabel, args, true);
				return;
			}
			
			setHopperAllow(user, target, false);
		}
		
		if (arg.isDefined("invite-none")) {
			if (!target.canInvite(user)) {
				GoldenApple.logPermissionFail(user, commandLabel, args, true);
				return;
			}
			
			for (IPermissionUser u : arg.getUserList("invite-none")) {
				if (target.hasFullControl(user) || target.getActualLevel(u).levelId <= GuestLevel.ALLOW_INVITE.levelId) {
					removeUser(instance, user, target, u);
				}
			}
		}
		
		if (arg.isDefined("invite-use")) {
			if (!target.canInvite(user)) {
				GoldenApple.logPermissionFail(user, commandLabel, args, true);
				return;
			}
			
			for (IPermissionUser u : arg.getUserList("invite-use")) {
				if (target.hasFullControl(user) || target.getActualLevel(u) == GuestLevel.NONE) {
					addUser(instance, user, target, u, GuestLevel.USE);
				}
			}
		}
		
		if (arg.isDefined("invite-invite")) {
			if (!target.hasFullControl(user)) {
				GoldenApple.logPermissionFail(user, commandLabel, args, true);
				return;
			}
			
			for (IPermissionUser u : arg.getUserList("invite-invite")) {
				addUser(instance, user, target, u, GuestLevel.ALLOW_INVITE);
			}
		}
		
		if (arg.isDefined("invite-modify")) {
			if (!target.hasFullControl(user)) {
				GoldenApple.logPermissionFail(user, commandLabel, args, true);
				return;
			}
			
			for (IPermissionUser u : arg.getUserList("invite-modify")) {
				addUser(instance, user, target, u, GuestLevel.ALLOW_BLOCK_MODIFY);
			}
		}
		
		if (arg.isDefined("invite-full")) {
			if (!target.hasFullControl(user)) {
				GoldenApple.logPermissionFail(user, commandLabel, args, true);
				return;
			}
			
			for (IPermissionUser u : arg.getUserList("invite-full")) {
				addUser(instance, user, target, u, GuestLevel.FULL);
			}
		}
		
		if (arg.isDefined("group-invite-none")) {
			if (!target.hasFullControl(user)) {
				GoldenApple.logPermissionFail(user, commandLabel, args, true);
				return;
			}
			
			for (IPermissionGroup g : arg.getGroupList("group-invite-none")) {
				removeGroup(instance, user, target, g);
			}
		}
		
		if (arg.isDefined("group-invite-use")) {
			if (!target.hasFullControl(user)) {
				GoldenApple.logPermissionFail(user, commandLabel, args, true);
				return;
			}
			
			for (IPermissionGroup g : arg.getGroupList("group-invite-use")) {
				addGroup(instance, user, target, g, GuestLevel.USE);
			}
		}
		
		if (arg.isDefined("group-invite-invite")) {
			if (!target.hasFullControl(user)) {
				GoldenApple.logPermissionFail(user, commandLabel, args, true);
				return;
			}
			
			for (IPermissionGroup g : arg.getGroupList("group-invite-invite")) {
				addGroup(instance, user, target, g, GuestLevel.ALLOW_INVITE);
			}
		}
		
		if (arg.isDefined("group-invite-modify")) {
			if (!target.hasFullControl(user)) {
				GoldenApple.logPermissionFail(user, commandLabel, args, true);
				return;
			}
			
			for (IPermissionGroup g : arg.getGroupList("group-invite-modify")) {
				addGroup(instance, user, target, g, GuestLevel.ALLOW_BLOCK_MODIFY);
			}
		}
		
		if (arg.isDefined("group-invite-full")) {
			if (!target.hasFullControl(user)) {
				GoldenApple.logPermissionFail(user, commandLabel, args, true);
				return;
			}
			
			for (IPermissionGroup g : arg.getGroupList("group-invite-full")) {
				addGroup(instance, user, target, g, GuestLevel.FULL);
			}
		}
		
		if (arg.isDefined("info")) {
			getInfo(instance, user, target);
		}
	}

	@Override
	public void onExecuteSimple(GoldenApple instance, User user, String commandLabel, String[] args) {
		if (!(user.getHandle() instanceof Player))
			user.sendLocalizedMessage("shared.noConsole");

		if (args.length == 0 || args[0].equals("-?") || args[0].equals("help")) {
			sendHelp(user, commandLabel, false);
			return;
		}

		user.sendLocalizedMessage("header.lock");

		if (args[0].equalsIgnoreCase("create")) {
			Location lockLocation = findLockableBlock(user, 10);
			if (!user.hasPermission(LockManager.addPermission)) {
				GoldenApple.logPermissionFail(user, commandLabel, args, true);
			} else if (lockLocation == null) {
				user.sendLocalizedMessage("error.lock.invalidBlock");
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
		} else if (args[0].equalsIgnoreCase("override")) {
			if (args.length == 1) {
				if (!LockManager.getInstance().canOverride(user)) {
					GoldenApple.logPermissionFail(user, commandLabel, args, true);
				} else {
					if (LockManager.getInstance().isOverrideOn(user)) {
						if (!LockManager.getInstance().canOverride(user)) {
							GoldenApple.logPermissionFail(user, commandLabel, args, true);
						} else {
							LockManager.getInstance().setOverrideOn(user, false);
							user.sendLocalizedMessage("general.lock.override.off");
						}
					} else {
						if (!LockManager.getInstance().canOverride(user)) {
							GoldenApple.logPermissionFail(user, commandLabel, args, true);
						} else {
							LockManager.getInstance().setOverrideOn(user, true);
							user.sendLocalizedMessage("general.lock.override.on");
						}
					}
				}
			} else if (args[1].equalsIgnoreCase("on")) {
				if (!LockManager.getInstance().canOverride(user)) {
					GoldenApple.logPermissionFail(user, commandLabel, args, true);
				} else {
					LockManager.getInstance().setOverrideOn(user, true);
					user.sendLocalizedMessage("general.lock.override.on");
				}
			} else if (args[1].equalsIgnoreCase("off")) {
				if (!LockManager.getInstance().canOverride(user)) {
					GoldenApple.logPermissionFail(user, commandLabel, args, true);
				} else {
					LockManager.getInstance().setOverrideOn(user, false);
					user.sendLocalizedMessage("general.lock.override.off");
				}
			} else {
				user.sendLocalizedMessage("shared.unknownOption", args[1]);
			}
		} else {
			LockedBlock lock = findLock(user, 10);

			if (lock == null) {
				user.sendLocalizedMessage("error.lock.notFound");
				return;
			}

			if (args[0].equalsIgnoreCase("delete")) {
				if (args.length > 1) {
					user.sendLocalizedMessage("shared.unknownOption", args[1]);
				} else if (!lock.canModifyBlock(user)) {
					GoldenApple.logPermissionFail(user, commandLabel, args, true);
					if (lock.getOverrideLevel(user).levelId >= GuestLevel.ALLOW_BLOCK_MODIFY.levelId) {
						user.sendLocalizedMessage("general.lock.overrideAvailable.simple");
					}
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
					if (lock.getOverrideLevel(user).levelId >= GuestLevel.ALLOW_INVITE.levelId) {
						user.sendLocalizedMessage("general.lock.overrideAvailable.simple");
					}
					return;
				} else {
					addUser(instance, user, lock, args[1], GuestLevel.USE);
				}
			} else if (args[0].equalsIgnoreCase("uninvite")) {
				if (args.length == 1) {
					user.sendLocalizedMessage("shared.parameterMissing", "uninvite");
				} else if (args.length > 2) {
					user.sendLocalizedMessage("shared.unknownOption", args[2]);
				} else if (!lock.canInvite(user)) {
					GoldenApple.logPermissionFail(user, commandLabel, args, true);
					if (lock.getOverrideLevel(user).levelId >= GuestLevel.FULL.levelId) {
						user.sendLocalizedMessage("general.lock.overrideAvailable.simple");
					}
					return;
				} else {
					removeUser(instance, user, lock, args[1], commandLabel, args);
				}
			} else if (args[0].equalsIgnoreCase("access")) {
				if (args.length == 1) {
					user.sendLocalizedMessage("shared.parameterMissing", "share");
				} else if (args.length > 2) {
					user.sendLocalizedMessage("shared.unknownOption", args[2]);
				} else if (!lock.canModifyBlock(user)) {
					GoldenApple.logPermissionFail(user, commandLabel, args, true);
					if (lock.getOverrideLevel(user).levelId >= GuestLevel.ALLOW_BLOCK_MODIFY.levelId) {
						user.sendLocalizedMessage("general.lock.overrideAvailable.simple");
					}
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
					if (lock.getOverrideLevel(user).levelId >= GuestLevel.ALLOW_BLOCK_MODIFY.levelId) {
						user.sendLocalizedMessage("general.lock.overrideAvailable.simple");
					}
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
	
	private LockedBlock findLock(User user, int range) {
		BlockIterator i = new BlockIterator(user.getPlayerHandle(), range);
		
		while (i.hasNext()) {
			Block b = i.next();
			
			if (LockManager.getInstance().isLockable(b.getType()))
				return LockManager.getInstance().getLock(b.getLocation());
		}
		
		return null;
	}
	
	private Location findLockableBlock(User user, int range) {
		BlockIterator i = new BlockIterator(user.getPlayerHandle(), range);
		
		while (i.hasNext()) {
			Block b = i.next();
			
			if (LockManager.getInstance().isLockable(b.getType()))
				return b.getLocation();
		}
		
		return null;
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
			addUser(instance, user, lock, gUser, level);
		}
	}
	
	private void addUser(GoldenApple instance, User user, LockedBlock lock, IPermissionUser guest, GuestLevel level) {
		lock.addUser(guest, level);
		user.sendLocalizedMessage("general.lock.guest.add.success", guest.getName());
	}

	private void removeUser(GoldenApple instance, User user, LockedBlock lock, String guest, String commandLabel, String[] args) {
		IPermissionUser gUser = PermissionManager.getInstance().getUser(guest);

		if (gUser == null) {
			user.sendLocalizedMessage("shared.userNotFoundWarning", guest);
		} else if (lock.getActualLevel(gUser).levelId > GuestLevel.USE.levelId && !lock.hasFullControl(user)) {
			GoldenApple.logPermissionFail(user, commandLabel, args, true);
		} else {
			removeUser(instance, user, lock, gUser);
		}
	}
	
	private void removeUser(GoldenApple instance, User user, LockedBlock lock, IPermissionUser guest) {
		lock.remUser(guest);
		user.sendLocalizedMessage("general.lock.guest.remove.success", guest.getName());
	}
	
	private void addGroup(GoldenApple instance, User user, LockedBlock lock, IPermissionGroup guest, GuestLevel level) {
		lock.addGroup(guest, level);
		user.sendLocalizedMessage("general.lock.guest.add.success", guest.getName());
	}
	
	private void removeGroup(GoldenApple instance, User user, LockedBlock lock, IPermissionGroup guest) {
		lock.remGroup(guest);
		user.sendLocalizedMessage("general.lock.guest.remove.success", guest.getName());
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
		
		if (b.isRedstoneAccessApplicable() || b.isHopperAccessApplicable())
			redstone = GoldenApple.getInstance().getLocalizationManager().getMessage(user, (b.getAllowExternal()) ? "general.lock.info.enabled" : "general.lock.info.disabled");
		else
			redstone = GoldenApple.getInstance().getLocalizationManager().getMessage(user, "general.lock.info.na");
		
		user.sendLocalizedMultilineMessage("general.lock.info", String.valueOf(b.getLockId()), PermissionManager.getInstance().getUser(b.getOwner()).getName(), access, b.getTypeIdentifier(), redstone);
	}
	
	private void setHopperAllow(User user, LockedBlock lock, boolean allow) {
		lock.setAllowExternal(allow);
		lock.save();
		user.sendLocalizedMessage((allow) ? "general.lock.redstone.on" : "general.lock.redstone.off");
	}
	
	private void sendHelp(User user, String commandLabel, boolean complex) {
		user.sendLocalizedMessage("header.help");
		user.sendLocalizedMultilineMessage((complex) ? "help.lock.complex" : "help.lock.simple", commandLabel);
	}
	
	private ArgumentInfo[] getArguments() {
		return new ArgumentInfo[] {
			ArgumentInfo.newInt("select", "s", "select"),
			
			ArgumentInfo.newSwitch("override-on", "o:on", "override:on"),
			ArgumentInfo.newSwitch("override-off", "o:off", "override:off"),
			
			ArgumentInfo.newSwitch("create", "c", "create"),
			ArgumentInfo.newSwitch("public", "p", "public"),
			ArgumentInfo.newSwitch("delete", "d", "delete"),
			
			ArgumentInfo.newSwitch("info", "i", "info"),
			
			ArgumentInfo.newString("access", "a", "access", false),
			ArgumentInfo.newSwitch("redstone-on", "r:on", "redstone:on"),
			ArgumentInfo.newSwitch("redstone-off", "r:off", "redstone:off"),
			
			ArgumentInfo.newUserList("invite-none", "in:n", "invite:none", false, false),
			ArgumentInfo.newUserList("invite-use", "in:u", "invite:use", false, false),
			ArgumentInfo.newUserList("invite-invite", "in:i", "invite:invite", false, false),
			ArgumentInfo.newUserList("invite-modify", "in:m", "invite:modify", false, false),
			ArgumentInfo.newUserList("invite-full", "in:f", "invite:full", false, false),
			
			ArgumentInfo.newGroupList("group-invite-none", "gin:n", "groupinvite:none", false),
			ArgumentInfo.newGroupList("group-invite-use", "gin:u", "groupinvite:use", false),
			ArgumentInfo.newGroupList("group-invite-invite", "gin:i", "groupinvite:invite", false),
			ArgumentInfo.newGroupList("group-invite-modify", "gin:m", "groupinvite:modify", false),
			ArgumentInfo.newGroupList("group-invite-full", "gin:f", "groupinvite:full", false),
		};
	}
}
