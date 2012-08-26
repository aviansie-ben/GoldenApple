package com.bendude56.goldenapple.commands;

import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.sql.SQLException;
import java.util.logging.Level;

import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import com.bendude56.goldenapple.GoldenApple;
import com.bendude56.goldenapple.User;
import com.bendude56.goldenapple.lock.LockManager;
import com.bendude56.goldenapple.lock.LockedBlock;
import com.bendude56.goldenapple.lock.LockedBlock.LockLevel;
import com.bendude56.goldenapple.util.Constants;

public class LockCommand implements CommandExecutor {
	@Override
	public boolean onCommand(CommandSender sender, Command command, String commandLabel, String[] args) {
		GoldenApple instance = GoldenApple.getInstance();
		User user = User.getUser(sender);

		Location lockLocation = null;
		int selectedId = -1;
		if (user.getHandle() instanceof Player)
			lockLocation = user.getPlayerHandle().getTargetBlock(Constants.getTransparentBlocks(), 10).getLocation();

		if (args.length == 0 || args[0].equals("-?")) {
			sendHelp(user, commandLabel);
			return true;
		}

		instance.locale.sendMessage(user, "header.lock", false);

		int arg = 0;
		if (args[0].equalsIgnoreCase("-s")) {
			try {
				selectedId = Integer.parseInt(args[1]);
				arg = 2;
			} catch (NumberFormatException | ArrayIndexOutOfBoundsException e) {
				instance.locale.sendMessage(user, "shared.parameterMissing", false, "-s");
				return true;
			}

			try {
				if (selectedId < 0 || !instance.locks.lockExists(selectedId)) {
					instance.locale.sendMessage(user, "error.lock.selectNotFound", false, String.valueOf(selectedId));
					return true;
				} else if (args.length == 2) {
					instance.locale.sendMessage(user, "error.lock.selectNoAction", false);
					return true;
				}
			} catch (SQLException e) {
				instance.locale.sendMessage(user, "error.lock.selectNotFound", false, String.valueOf(selectedId));
				return true;
			}
		} else if (lockLocation == null || Constants.getTransparentBlocks().contains((byte)lockLocation.getBlock().getTypeId())) {
			instance.locale.sendMessage(user, "error.lock.invalidBlock", false);
			return true;
		}

		if (args[arg].equalsIgnoreCase("-c")) {
			arg++;
			if (selectedId >= 0) {
				instance.locale.sendMessage(user, "error.lock.selectCreate", false);
				return true;
			} else if (!user.hasPermission(LockManager.addPermission)) {
				GoldenApple.logPermissionFail(user, commandLabel, args, true);
				return true;
			}

			LockLevel accessLevel = LockLevel.PRIVATE;
			if (args.length > arg && args[arg].equalsIgnoreCase("-p")) {
				accessLevel = LockLevel.PUBLIC;
				arg++;
			}

			try {
				LockedBlock b = instance.locks.createLock(lockLocation, accessLevel, user);
				getInfo(instance, user, b);
			} catch (InvocationTargetException e) {
				instance.locale.sendMessage(user, "error.lock.create.invalidRegistered", false, LockedBlock.getBlock(lockLocation.getBlock().getType()).plugin.getName());
				GoldenApple.log(Level.SEVERE, "Failed to lock block due to RegisteredBlock failure (Plugin: " + LockedBlock.getBlock(lockLocation.getBlock().getType()).plugin.getName() + ")");
				GoldenApple.log(Level.SEVERE, e);
				return true;
			} catch (IOException e) {
				instance.locale.sendMessage(user, "error.lock.create.ioError", false);
				return true;
			} catch (UnsupportedOperationException e) {
				instance.locale.sendMessage(user, "error.lock.invalidBlock", false);
				return true;
			}
		} else if (args[arg].equalsIgnoreCase("-d")) {
			
		}

		return true;
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
		}
		GoldenApple.getInstance().locale.sendMessage(user, "general.lock.info", true, String.valueOf(b.getLockId()), instance.permissions.getUser(b.getOwner()).getName(), access, b.getTypeIdentifier());
	}

	private void sendHelp(User user, String commandLabel) {
		GoldenApple.getInstance().locale.sendMessage(user, "header.help", false);
		GoldenApple.getInstance().locale.sendMessage(user, "help.lock", true, commandLabel);
	}
}
