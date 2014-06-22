package com.bendude56.goldenapple.area.command;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;

import org.bukkit.Location;
import org.bukkit.command.BlockCommandSender;
import org.bukkit.command.ConsoleCommandSender;
import org.bukkit.entity.Player;
import org.bukkit.entity.minecart.CommandMinecart;

import com.bendude56.goldenapple.GoldenApple;
import com.bendude56.goldenapple.User;
import com.bendude56.goldenapple.area.Area;
import com.bendude56.goldenapple.area.AreaAccessLevel;
import com.bendude56.goldenapple.area.AreaFlag;
import com.bendude56.goldenapple.area.AreaManager;
import com.bendude56.goldenapple.area.RegionShape;
import com.bendude56.goldenapple.command.DualSyntaxCommand;
import com.bendude56.goldenapple.permissions.IPermissionGroup;
import com.bendude56.goldenapple.permissions.IPermissionUser;
import com.bendude56.goldenapple.permissions.PermissionManager;
import com.bendude56.goldenapple.select.SelectManager;
import com.bendude56.goldenapple.util.ComplexArgumentParser;
import com.bendude56.goldenapple.util.ComplexArgumentParser.ArgumentInfo;

public class AreaCommand extends DualSyntaxCommand {

	@Override
	public void onExecuteComplex(GoldenApple instance, User user, String commandLabel, String[] args) {
		if (args.length==0 || args[0].equalsIgnoreCase("help")) {
			sendHelp(user, commandLabel, true);
			return;
		}
		
		ComplexArgumentParser arg = new ComplexArgumentParser(getArguments());
		if (!arg.parse(user, args)) return;
		user.sendLocalizedMessage("header.area");
		
		if (arg.isDefined("help")) {
			sendHelp(user, commandLabel, true);
			return;
		}
		
		if (arg.isDefined("override-on")) {
			if (!AreaManager.canOverride(user)) {
				GoldenApple.logPermissionFail(user, commandLabel, args, true);
			} else {
				AreaManager.getInstance().setOverride(user, true);
				user.sendLocalizedMessage("general.area.override.on");
			}
			return;
		} else if (arg.isDefined("override-off")) {
			if (!AreaManager.canOverride(user)) {
				GoldenApple.logPermissionFail(user, commandLabel, args, true);
			} else {
				AreaManager.getInstance().setOverride(user, false);
				user.sendLocalizedMessage("general.area.override.off");
			}
			return;
		} else if (arg.isDefined("list")) {
			int page = (arg.isDefined("page") ? arg.getInt("page") : 1);
			if (page <= 0) page = 1;
			sendAreaList(user, page, arg.isDefined("all"));
		}
		
		// Check for area creation/deletion. If both are defined, only execute
		// creation and ignore deletion.
		if (arg.isDefined("create")) {
			if (!onExecuteComplexCreate(instance, user, commandLabel, arg)) return;
		} else if (arg.isDefined("delete")) {
			if (!onExecuteComplexDelete(instance, user, commandLabel, arg)) return;
		} else {
		
			// Check for area modification. If multiple are defined, execute them
			// in order defined below.
			if (arg.isDefined("owner-add")) {
				onExecuteComplexAddOwner(instance, user, commandLabel, arg);
			}
			if (arg.isDefined("owner-remove")) {
				onExecuteComplexRemoveOwner(instance, user, commandLabel, arg);
			}
			if (arg.isDefined("group-owner-add")) {
				onExecuteComplexAddGroupOwner(instance, user, commandLabel, arg);
			}
			if (arg.isDefined("group-owner-remove")) {
				onExecuteComplexRemoveGroupOwner(instance, user, commandLabel, arg);
			}
			if (arg.isDefined("invite-full")) {
				onExecuteComplexAddGuest(instance, user, commandLabel, arg);
			}
			if (arg.isDefined("invite-none")) {
				onExecuteComplexRemoveGuest(instance, user, commandLabel, arg);
			}
			if (arg.isDefined("group-invite-full")) {
				onExecuteComplexAddGroupGuest(instance, user, commandLabel, arg);
			}
			if (arg.isDefined("group-invite-none")) {
				onExecuteComplexRemoveGroupGuest(instance, user, commandLabel, arg);
			}
			if (arg.isDefined("flags-add")) {
				onExecuteComplexAddFlags(instance, user, commandLabel, arg);
			}
			if (arg.isDefined("flags-remove")) {
				onExecuteComplexRemoveFlags(instance, user, commandLabel, arg);
			}
			if (arg.isDefined("priority")) {
				onExecuteComplexSetPriority(instance, user, commandLabel, arg);
			}
			if (arg.isDefined("label")) {
				onExecuteComplexSetLabel(instance, user, commandLabel, arg);
			}
			if (arg.isDefined("info")) {
				onExecuteComplexInfo(instance, user, commandLabel, arg);
			}
			
			// TODO Finish these (adding/removing regions)
		}
	}

	@Override
	public void onExecuteSimple(GoldenApple instance, User user, String commandLabel, String[] args) {
		// Redirect to complex temporarily
		onExecuteComplex(instance, user, commandLabel, args);
		
		/*
		if (args.length == 0 || args[0].equals("-?") || args[0].equalsIgnoreCase("help")) {
			sendHelp(user, commandLabel, false);
			return;
		}
		
		user.sendLocalizedMessage("header.area");
		
		switch (args[1].toLowerCase()) {
			case "create":
				onExecuteSimpleCreate(instance, user, commandLabel, args);
				break;
			case "delete":
				onExecuteSimpleDelete(instance, user, commandLabel, args);
				break;
				
				// TODO Create cases for each command
				
			default:
				// TODO Unknown argument
				onExecuteComplex(instance, user, commandLabel, args);
		
		}
		*/
	}

	private boolean onExecuteSimpleCreate(GoldenApple instance, User user, String commandLabel, String[] args) {
		
		// Make sure the user is a player. Required for technical reasons. (i.e. console can't select a region in the world.)
		if (!(user.getHandle() instanceof Player)) {
			user.sendLocalizedMessage("shared.noConsole");
			return false;
		}
		
		// Make sure user has adequate permissions
		if (!user.hasPermission(AreaManager.addPermission)) {
			user.sendLocalizedMessage("shared.noPermission");
			return false;
		}
		
		// Handle optional arguments
		// Extract owner if argument is included
		IPermissionUser owner = user;
		if (args.length > 1) {
			owner = PermissionManager.getInstance().findUser(args[1], true);
			if (owner == null) {
				user.sendLocalizedMessage("shared.userNotFoundError", args[1]);
				return false;
			}
		}
		
		// Get selected area
		Location c1, c2;
		if (SelectManager.getInstance().isSelectionMade(user) && SelectManager.getInstance().getSelectionWorld(user) == user.getPlayerHandle().getWorld()) {
			c1 = SelectManager.getInstance().getSelectionMinimum(user);
			c2 = SelectManager.getInstance().getSelectionMaximum(user);
		} else {
			user.sendLocalizedMessage("error.area.noSelection");
			return false;
		}
		
		// Attempt to create the area
		return createArea(user, owner, RegionShape.CUBOID, c1, c2, false) != null;
	}

	private boolean onExecuteSimpleDelete(GoldenApple instance, User user, String commandLabel, String[] args) {

		// Make sure user has adequate permissions
		if (!user.hasPermission(AreaManager.removePermission)) {
			user.sendLocalizedMessage("shared.noPermission");
			return false;
		}
		
		// Get the selected area
		Area area = findArea(user, args[1]);
		if (area == null) {
			return false;
		}

		// Delete the area
		return deleteArea(user, area.getAreaId());
	}

	/**
	 * Sub-function of the onExecuteComplex function. Only handles area
	 * creation. Assumes that the user has requested a new area be
	 * created. Checks that the user has adequate permissions to create a new
	 * area.
	 * @param instance The current GoldenApple instance.
	 * @param user The user who executed the command.
	 * @param commandLabel The label used for the command.
	 * @param arg The set of arguments passed with the command.
	 * @return True if everything worked fine, false if an error occurred. If
	 * an error occurred that caused this function to return false, command
	 * execution should be halted immediately.
	 */
	private boolean onExecuteComplexCreate(GoldenApple instance, User user, String commandLabel, ComplexArgumentParser arg) {
		
		// Make sure the user is a player. Required for technical reasons. (i.e. console can't select a region in the world.)
		if (!(user.getHandle() instanceof Player)) {
			user.sendLocalizedMessage("shared.noConsole");
			return false;
		}
		
		// Make sure user has adequate permissions
		if (!user.hasPermission(AreaManager.addPermission)) {
			user.sendLocalizedMessage("shared.noPermission");
			return false;
		}
		
		// Acquire all needed properties
		String label = extractLabel(arg);
		RegionShape shape = extractShape(arg);
		Integer priority = extractPriority(arg);
		Boolean ignoreY = extractIgnoreY(arg);
		IPermissionUser owner = extractOwner(arg);
		List<AreaFlag> flags = extractFlags(arg, "flags");
		Location c1, c2;
		Area area;
		
		// Get selected area
		if (SelectManager.getInstance().isSelectionMade(user) && SelectManager.getInstance().getSelectionWorld(user) == user.getPlayerHandle().getWorld()) {
			c1 = SelectManager.getInstance().getSelectionMinimum(user);
			c2 = SelectManager.getInstance().getSelectionMaximum(user);
		} else {
			user.sendLocalizedMessage("error.area.noSelection");
			return false;
		}
		
		// Validate shape type or set to default value
		if (shape == null) {
			if (arg.isDefined("shape")) {
				user.sendLocalizedMessage("error.area.invalidShape", arg.getString("shape"));
				return false;
			} else {
				shape = RegionShape.CUBOID; // Default value
			}
		}
		
		// Validate priority or set to default value
		if (priority == null) {
			if (arg.isDefined("priority")) {
				return false;
			} else {
				priority = 0; // Default value
			}
		}
		
		// Validate owner or set to default value
		if (owner == null) {
			if (arg.isDefined("owner")) {
				return false;
			} else {
				owner = user; // Default value
			}
		}
		
		area = createArea(user, owner, shape, c1, c2, ignoreY);
		
		return (area != null
				&& setAreaLabel(user, area, label)
				&& addAreaFlags(user, area, flags));
	}

	/**
	 * Method to handle the deletion of an area via a command from a user.
	 * Assumes that the given user is the same one that passed the command,
	 * that the command indicates that an area is to be deleted. Performs the
	 * necessary permission checks before deleting the area and send the user
	 * an error if they do not have adequate permissions.
	 * @param instance The current GoldenApple instance.
	 * @param user The user who executed the command.
	 * @param commandLabel The label used for the command.
	 * @param arg The set of arguments passed with the command.
	 * @return True if everything worked fine, false if an error occurred. If
	 * an error occurred that caused this function to return false, command
	 * execution should be halted immediately.
	 */
	private boolean onExecuteComplexDelete(GoldenApple instance, User user, String commandLabel, ComplexArgumentParser arg) {
		
		// Make sure user has adequate permissions
		if (!user.hasPermission(AreaManager.removePermission)) {
			user.sendLocalizedMessage("shared.noPermission");
			return false;
		}
		
		// Get the selected area
		Area area = extractSelectedArea(arg, user, false);
		if (area == null) {
			return false;
		}
		
		// Delete the area
		return deleteArea(user, area.getAreaId());
	}

	/**
	 * Method to add owners to an existing area.
	 * @param instance The current GoldenApple instance.
	 * @param user The user who executed the command.
	 * @param commandLabel The label used for the command.
	 * @param arg The set of arguments passed with the command.
	 * @return True if everything worked fine, false if an error occurred. If
	 * an error occurred that caused this function to return false, command
	 * execution should be halted immediately.
	 */
	private boolean onExecuteComplexAddOwner(GoldenApple instance, User user, String commandLabel, ComplexArgumentParser arg) {
		int count = 0;
		
		// Make sure user has adequate permissions
		if (!user.hasPermission(AreaManager.editOwnersPermission)) {
			user.sendLocalizedMessage("shared.noPermission");
			return false;
		}
		
		// Get the selected area
		Area area = extractSelectedArea(arg, user, true);
		if (area == null) {
			return false;
		}
		
		// Add all of the owners specified
		for (IPermissionUser u : arg.getUserList("owner-add")) {
			if (area.getUserAccessLevel(u.getId()).getComparableValue() < AreaAccessLevel.OWNER.getComparableValue()) {
				area.setUserAccessLevel(u.getId(), AreaAccessLevel.OWNER);
				user.sendLocalizedMessage("general.area.edit.owner.add", area.getAreaId()+"", u.getName());
				++count;
			} else {
				user.sendLocalizedMessage("error.area.edit.owner.add", area.getAreaId()+"", u.getName(), area.getUserAccessLevel(u.getId()).toString());
			}
		}
		
		// check if no changes were made
		if (count == 0) {
			user.sendLocalizedMessage("general.area.edit.owner.unmodified", area.getAreaId()+"");
		}
		
		return true;
	}

	/**
	 * Method to remove owners from an existing area.
	 * @param instance The current GoldenApple instance.
	 * @param user The user who executed the command.
	 * @param commandLabel The label used for the command.
	 * @param arg The set of arguments passed with the command.
	 * @return True if everything worked fine, false if an error occurred. If
	 * an error occurred that caused this function to return false, command
	 * execution should be halted immediately.
	 */
	private boolean onExecuteComplexRemoveOwner(GoldenApple instance, User user, String commandLabel, ComplexArgumentParser arg) {
		int count = 0;
		
		// Make sure user has adequate permissions
		if (!user.hasPermission(AreaManager.editOwnersPermission)) {
			user.sendLocalizedMessage("shared.noPermission");
			return false;
		}

		// Get the selected area
		Area area = extractSelectedArea(arg, user, true);
		if (area == null) {
			return false;
		}
		
		// Attempt to remove users form owner list
		for (IPermissionUser u : arg.getUserList("owner-remove")) {
			if (area.getUserAccessLevel(u.getId()) == AreaAccessLevel.OWNER) {
				area.setUserAccessLevel(u.getId(), AreaAccessLevel.NONE);
				user.sendLocalizedMessage("general.area.edit.owner.remove", area.getAreaId()+"", u.getName());
				++count;
			} else {
				user.sendLocalizedMessage("error.area.edit.owner.remove", area.getAreaId()+"", u.getName());
			}
		}
		
		// Check if no modifications were made
		if (count == 0) {
			user.sendLocalizedMessage("general.area.edit.owner.unmodified", area.getAreaId()+"");
		}
		
		return true;
	}

	/**
	 * Method to add group owners to an existing area.
	 * @param instance The current GoldenApple instance.
	 * @param user The user who executed the command.
	 * @param commandLabel The label used for the command.
	 * @param arg The set of arguments passed with the command.
	 * @return True if everything worked fine, false if an error occurred. If
	 * an error occurred that caused this function to return false, command
	 * execution should be halted immediately.
	 */
	private boolean onExecuteComplexAddGroupOwner(GoldenApple instance, User user, String commandLabel, ComplexArgumentParser arg) {
		int count = 0;
		
		// Make sure user has adequate permissions
		if (!user.hasPermission(AreaManager.editOwnersPermission)) {
			user.sendLocalizedMessage("shared.noPermission");
			return false;
		}

		// Get the selected area
		Area area = extractSelectedArea(arg, user, true);
		if (area == null) {
			return false;
		}
		
		// Attempt to add group owners to area
		for (IPermissionGroup g : arg.getGroupList("group-owner-add")) {
			if (area.getGroupAccessLevel(g.getId()).getComparableValue() < AreaAccessLevel.OWNER.getComparableValue()) {
				area.setGroupAccessLevel(g.getId(), AreaAccessLevel.OWNER);
				user.sendLocalizedMessage("general.area.edit.groupOwner.add", area.getAreaId()+"", g.getName());
				++count;
			} else {
				user.sendLocalizedMessage("error.area.edit.groupOwner.add", area.getAreaId()+"", g.getName(), area.getGroupAccessLevel(g.getId()).toString());
			}
		}
		
		// Check if no modifications were made
		if (count == 0) {
			user.sendLocalizedMessage("general.area.edit.groupOwner.unmodified", area.getAreaId()+"");
		}
		
		return true;
	}
	
	/**
	 * Method to remove group owners from an existing area.
	 * @param instance The current GoldenApple instance.
	 * @param user The user who executed the command.
	 * @param commandLabel The label used for the command.
	 * @param arg The set of arguments passed with the command.
	 * @return True if everything worked fine, false if an error occurred. If
	 * an error occurred that caused this function to return false, command
	 * execution should be halted immediately.
	 */
	private boolean onExecuteComplexRemoveGroupOwner(GoldenApple instance, User user, String commandLabel, ComplexArgumentParser arg) {
		int count = 0;
		
		// Make sure user has adequate permissions
		if (!user.hasPermission(AreaManager.editOwnersPermission)) {
			user.sendLocalizedMessage("shared.noPermission");
			return false;
		}

		// Get the selected area
		Area area = extractSelectedArea(arg, user, true);
		if (area == null) {
			return false;
		}
		
		// Attempt to remove group owners from area
		for (IPermissionGroup g : arg.getGroupList("group-owner-remove")) {
			if (area.getGroupAccessLevel(g.getId()) == AreaAccessLevel.OWNER) {
				area.setGroupAccessLevel(g.getId(), AreaAccessLevel.NONE);
				user.sendLocalizedMessage("general.area.edit.groupOwner.remove", area.getAreaId()+"", g.getName());
				++count;
			} else {
				user.sendLocalizedMessage("error.area.edit.groupOwner.remove", area.getAreaId()+"", g.getName());
			}
		}
		
		// Check if no changes were made
		if (count == 0) {
			user.sendLocalizedMessage("general.area.edit.groupOwner.unmodified", area.getAreaId()+"");
		}
		
		return true;
	}

	/**
	 * Method to add guests to an existing area.
	 * @param instance The current GoldenApple intance.
	 * @param user The user who executed the command.
	 * @param commandLabel The label used for the command.
	 * @param arg The set of arguments passed with the command.
	 * @return True if everything worked fine, false if an error occurred. If
	 * an error occurred that caused this function to return false, command
	 * execution should be halted immediately.
	 */
	private boolean onExecuteComplexAddGuest(GoldenApple instance, User user, String commandLabel, ComplexArgumentParser arg) {
		int count = 0;
		
		// Get the selected area
		Area area = extractSelectedArea(arg, user, true);
		if (area == null) {
			return false;
		}
		
		// Make sure user has adequate permissions
		if (!user.hasPermission(AreaManager.editGuestsPermission) && area.getUserAccessLevel(user.getId()).getComparableValue() < AreaAccessLevel.OWNER.getComparableValue()) {
			user.sendLocalizedMessage("shared.noPermission");
			return false;
		}
		
		// Attempt to add guests to area
		for (IPermissionUser u : arg.getUserList("invite-full")) {
			if (area.getUserAccessLevel(u.getId()).getComparableValue() < AreaAccessLevel.GUEST.getComparableValue()) {
				area.setUserAccessLevel(u.getId(), AreaAccessLevel.GUEST);
				user.sendLocalizedMessage("general.area.edit.guest.add", area.getAreaId()+"", u.getName());
				++count;
			} else {
				user.sendLocalizedMessage("error.area.edit.guest.add", area.getAreaId()+"", u.getName(), area.getUserAccessLevel(u.getId()).toString());
			}
		}
		
		// Check if no changes were made
		if (count == 0) {
			user.sendLocalizedMessage("general.area.edit.guest.unmodified", area.getAreaId()+"");
		}
		
		return true;
	}

	/**
	 * Method to remove guests from an existing area.
	 * @param instance The current GoldenApple instance.
	 * @param user The user who executed the command.
	 * @param commandLabel The label used for the command.
	 * @param arg The set of arguments passed with the command.
	 * @return True if everything worked fine, false if an error occurred. If
	 * an error occurred that caused this function to return false, command
	 * execution should be halted immediately.
	 */
	private boolean onExecuteComplexRemoveGuest(GoldenApple instance, User user, String commandLabel, ComplexArgumentParser arg) {
		int count = 0;
		
		// Get the selected area
		Area area = extractSelectedArea(arg, user, true);
		if (area == null) {
			return false;
		}

		// Make sure user has adequate permissions
		if (!user.hasPermission(AreaManager.editGuestsPermission) && area.getUserAccessLevel(user.getId()).getComparableValue() < AreaAccessLevel.OWNER.getComparableValue()) {
			user.sendLocalizedMessage("shared.noPermission");
			return false;
		}
		
		// Attempt to remove guests from area
		for (IPermissionUser u : arg.getUserList("invite-none")) {
			if (area.getUserAccessLevel(u.getId()) == AreaAccessLevel.GUEST) {
				area.setUserAccessLevel(u.getId(), AreaAccessLevel.NONE);
				user.sendLocalizedMessage("general.area.edit.guest.remove", area.getAreaId()+"", u.getName());
				++count;
			} else {
				user.sendLocalizedMessage("error.area.edit.guest.remove", area.getAreaId()+"", u.getName());
			}
		}
		
		// Check if no changes were made
		if (count == 0) {
			user.sendLocalizedMessage("general.area.edit.guest.unmodified", area.getAreaId()+"");
		}
		
		return true;
	}

	/**
	 * Method to add group guests to an existing area.
	 * @param instance The current GoldenApple instance.
	 * @param user The user who executed the command.
	 * @param commandLabel The label used for the command.
	 * @param arg The set of arguments passed with the command.
	 * @return True if everything worked fine, false if an error occurred. If
	 * an error occurred that caused this function to return false, command
	 * execution should be halted immediately.
	 */
	private boolean onExecuteComplexAddGroupGuest(GoldenApple instance, User user, String commandLabel, ComplexArgumentParser arg) {
		int count = 0;
		
		// Get the selected area
		Area area = extractSelectedArea(arg, user, true);
		if (area == null) {
			return false;
		}

		// Make sure user has adequate permissions
		if (!user.hasPermission(AreaManager.editGuestsPermission) && area.getUserAccessLevel(user.getId()).getComparableValue() < AreaAccessLevel.OWNER.getComparableValue()) {
			user.sendLocalizedMessage("shared.noPermission");
			return false;
		}
		
		// Attempt to add group guests to area
		for (IPermissionGroup g : arg.getGroupList("group-invite-full")) {
			if (area.getGroupAccessLevel(g.getId()).getComparableValue() < AreaAccessLevel.GUEST.getComparableValue()) {
				area.setGroupAccessLevel(g.getId(), AreaAccessLevel.GUEST);
				user.sendLocalizedMessage("general.area.edit.groupGuest.add", area.getAreaId()+"", g.getName());
				++count;
			} else {
				user.sendLocalizedMessage("error.area.edit.groupGuest.add", area.getAreaId()+"", g.getName(), area.getGroupAccessLevel(g.getId()).toString());
			}
		}
		
		// Check if no changes were made
		if (count == 0) {
			user.sendLocalizedMessage("general.area.edit.groupGuest.unmodified", area.getAreaId()+"");
		}
		
		return true;
	}

	/**
	 * Method to remove group guests from an existing area.
	 * @param instance The current GoldenApple instance.
	 * @param user The user who executed the command.
	 * @param commandLabel The label used for the command.
	 * @param arg The set of arguments passed with the command.
	 * @return True if everything worked fine, false if an error occurred. If
	 * an error occurred that caused this function to return false, command
	 * execution should be halted immediately.
	 */
	private boolean onExecuteComplexRemoveGroupGuest(GoldenApple instance, User user, String commandLabel, ComplexArgumentParser arg) {
		int count = 0;
		
		// Get the selected area
		Area area = extractSelectedArea(arg, user, true);
		if (area == null) {
			return false;
		}

		// Make sure user has adequate permissions
		if (!user.hasPermission(AreaManager.editGuestsPermission) && area.getUserAccessLevel(user.getId()).getComparableValue() < AreaAccessLevel.OWNER.getComparableValue()) {
			user.sendLocalizedMessage("shared.noPermission");
			return false;
		}
		
		// Attempt to remove group guests from area
		for (IPermissionGroup g : arg.getGroupList("group-invite-none")) {
			if (area.getGroupAccessLevel(g.getId()) == AreaAccessLevel.GUEST) {
				area.setGroupAccessLevel(g.getId(), AreaAccessLevel.NONE);
				user.sendLocalizedMessage("general.area.edit.groupGuest.remove", area.getAreaId()+"", g.getName());
				++count;
			} else {
				user.sendLocalizedMessage("error.area.edit.groupGuest.remove", area.getAreaId()+"", g.getName());
			}
		}
		
		// Check if no changes were made
		if (count == 0) {
			user.sendLocalizedMessage("general.area.edit.groupGuest.unmodified", area.getAreaId()+"");
		}
		
		return true;
	}

	/**
	 * Method to set flags on an area.
	 * @param instance The current GoldenApple instance.
	 * @param user The user who executed the command.
	 * @param commandLabel The label used for the command.
	 * @param arg The set of arguments passed with the command.
	 * @return True if everything worked fine, false if an error occurred. If
	 * an error occurred that caused this function to return false, command
	 * execution should be halted immediately.
	 */
	private boolean onExecuteComplexAddFlags(GoldenApple instance, User user, String commandLabel, ComplexArgumentParser arg) {

		// Make sure user has adequate permissions
		if (!user.hasPermission(AreaManager.editFlagsPermission)) {
			user.sendLocalizedMessage("shared.noPermission");
			return false;
		}

		// Get the selected area
		Area area = extractSelectedArea(arg, user, true);
		if (area == null) {
			return false;
		}
		
		// Extract flags from arguments
		List<AreaFlag> flags = extractFlags(arg, "flags-add");
		
		// Set the flags
		return addAreaFlags(user, area, flags);
	}

	/**
	 * Method to reset flags of an area.
	 * @param instance The current GoldenApple instance.
	 * @param user The user who executed the command.
	 * @param commandLabel The label used for the command.
	 * @param arg The set of arguments passed with the command.
	 * @return True if everything worked fine, false if an error occurred. If
	 * an error occurred that caused this function to return false, command
	 * execution should be halted immediately.
	 */
	private boolean onExecuteComplexRemoveFlags(GoldenApple instance, User user, String commandLabel, ComplexArgumentParser arg) {

		// Make sure user has adequate permissions
		if (!user.hasPermission(AreaManager.editFlagsPermission)) {
			user.sendLocalizedMessage("shared.noPermission");
			return false;
		}

		// Get the selected area
		Area area = extractSelectedArea(arg, user, true);
		if (area == null) {
			return false;
		}
		
		// Extract flags from arguments
		List<AreaFlag> flags = extractFlags(arg, "flags-remove");
		
		// Reset the flags
		return removeAreaFlags(user, area, flags);
	}

	/**
	 * Method to set the priority of an existing area.
	 * @param instance The current GoldenApple instance.
	 * @param user The user who executed the command.
	 * @param commandLabel The label used for the command.
	 * @param arg The set of arguments passed with the command.
	 * @return True if everything worked fine, false if an error occurred. If
	 * an error occurred that caused this function to return false, command
	 * execution should be halted immediately.
	 */
	private boolean onExecuteComplexSetPriority(GoldenApple instance, User user, String commandLabel, ComplexArgumentParser arg) {
		Integer priority;
		
		// Make sure user has adequate permissions
		if (!user.hasPermission(AreaManager.editPriorityPermission)) {
			user.sendLocalizedMessage("shared.noPermission");
			return false;
		}
		
		// Get the selected area
		Area area = extractSelectedArea(arg, user, true);
		if (area == null) {
			return false;
		}
		
		// Extract priority from arguments
		priority = extractPriority(arg);
		if (priority == null) {
			return false;
		}
		
		// Attempt to set the priority of the area
		area.setPriority(priority);
		user.sendLocalizedMessage("general.area.edit.priority", area.getAreaId()+"", priority+"");
		
		return true;
	}

	/**
	 * Method to set the label of an existing area.
	 * @param instance The current GoldenApple instance.
	 * @param user The user who executed the command.
	 * @param commandLabel The label used for the command.
	 * @param arg The set of arguments passed with the command.
	 * @return True if everything worked fine, false if an error occurred. If
	 * an error occurred that caused this function to return false, command
	 * execution should be halted immediately.
	 */
	private boolean onExecuteComplexSetLabel(GoldenApple instance, User user, String commandLabel, ComplexArgumentParser arg) {
		String label;
		
		// Make sure user has adequate permissions
		if (!user.hasPermission(AreaManager.editLabelPermission)) {
			user.sendLocalizedMessage("shared.noPermission");
			return false;
		}
		
		// Get the selected area
		Area area = extractSelectedArea(arg, user, true);
		if (area == null) {
			return false;
		}
		
		// Extract label from arguments
		label = extractLabel(arg);
		
		// Attempt to set the label of the area
		return setAreaLabel(user, area, label);
	}

	/**
	 * Method to a user info on an existing area.
	 * @param instance The current GoldenApple instance.
	 * @param user The user who executed the command.
	 * @param commandLabel The label used for the command.
	 * @param arg The set of arguments passed with the command.
	 * @return True if everything worked fine, false if an error occurred. If
	 * an error occurred that caused this function to return false, command
	 * execution should be halted immediately.
	 */
	private boolean onExecuteComplexInfo(GoldenApple instance, User user, String commandLabel, ComplexArgumentParser arg) {
		
		// Get the selected area
		Area area = extractSelectedArea(arg, user, true);
		if (area == null) {
			return false;
		}

		// Generate lists for info
		String flags;
		String owners;
		String guests;
		String gowners;
		String gguests;
		String world;
		
		// Generate flag list
		List<AreaFlag> flaglist = area.getFlags();
		if (flaglist.isEmpty()) {
			flags = GoldenApple.getInstance().getLocalizationManager().getMessage(user, "shared.none");
		} else {
			flags = "";
			for (AreaFlag f : flaglist) {
				if (!flags.isEmpty()) flags += ", ";
				flags += f.toString();
			}
		}
		
		// Generate owner list
		List<IPermissionUser> users = area.getUsers(AreaAccessLevel.OWNER);
		if (users.isEmpty()) {
			owners = GoldenApple.getInstance().getLocalizationManager().getMessage(user, "shared.none");
		} else {
			owners = "";
			for (IPermissionUser u : users) {
				if (!owners.isEmpty()) owners += ", ";
				owners += u.getName();
			}
		}
		
		// Generate guest list
		users = area.getUsers(AreaAccessLevel.GUEST);
		if (users.isEmpty()) {
			guests = GoldenApple.getInstance().getLocalizationManager().getMessage(user, "shared.none");
		} else {
			guests = "";
			for (IPermissionUser u : users) {
				if (!guests.isEmpty()) guests += ", ";
				guests += u.getName();
			}
		}
		
		// Generate group owner list
		List<IPermissionGroup> groups = area.getGroups(AreaAccessLevel.OWNER);
		if (groups.isEmpty()) {
			gowners = GoldenApple.getInstance().getLocalizationManager().getMessage(user, "shared.none");
		} else {
			gowners = "";
			for (IPermissionGroup g : groups) {
				if (!gowners.isEmpty()) gowners += ", ";
				gowners += g.getName();
			}
		}
		
		// Generate group guest list
		groups = area.getGroups(AreaAccessLevel.GUEST);
		if (groups.isEmpty()) {
			gguests = GoldenApple.getInstance().getLocalizationManager().getMessage(user, "shared.none");
		} else {
			gguests = "";
			for (IPermissionGroup g : groups) {
				if (!gguests.isEmpty()) gguests += ", ";
				gguests += g.getName();
			}
		}
		
		if (area.getRegionIds().size() > 0) {
			world = AreaManager.getInstance().getRegion(area.getRegionIds().get(0)).getWorld().getName();
		} else {
			world = GoldenApple.getInstance().getLocalizationManager().getMessage(user, "shared.none");
		}
		
		// Send user info about area
		user.sendLocalizedMultilineMessage("general.area.info",
				area.getAreaId()+"",
				area.getLabel(),
				area.getPriority()+"",
				flags,
				owners,
				guests,
				gowners,
				gguests,
				area.getRegionIds().size()+"",
				world);
		return true;
		
	}

	/**
	 * Gets the area selected by the user or, if no area is explicitly
	 * selected, then the area in which the user is currently standing if the
	 * user is a player, command block, or command block minecart.
	 * @param arg The arguments to extract from.
	 * @param user The user to check the location of
	 * @param useLocation True to check the user's location, false if not
	 * @return The area explicitly selected by the user, the area of highest
	 * priority at the location of the player, or null if no area exists
	 * matching these criteria.
	 */
	private Area extractSelectedArea(ComplexArgumentParser arg, User user, boolean useLocation) {
		
		if (arg.isDefined("select")) {
			return findArea(user, arg.getString("select"));
		} else if (useLocation && user != null) {
			return findArea(user);
		} else {
			if (user != null) {
				user.sendLocalizedMessage("error.area.select.missing");
			}
			return null;
		}
	}

	/**
	 * Gets the area label from a ComplexArgumentParser. Returns null if it is
	 * not defined. 
	 * @param arg The ComplexArgumentParser to search through.
	 * @return The label in the arguments or null if there is none.
	 */
	private String extractLabel(ComplexArgumentParser arg) {
		if (arg.isDefined("label")) {
			return arg.getString("label");
		} else {
			return null;
		}
	}

	/**
	 * Gets the region shape from a ComplexArgumentParser. Returns null if it
	 * not defined or does not match any existing enums.
	 * @param arg The ComplexArgumentParser to search through.
	 * @return The RegionShape matching the string in the arguments or null
	 * if no RegionShape matches or the shape argument is not defined.
	 */
	private RegionShape extractShape(ComplexArgumentParser arg) {
		if (arg.isDefined("shape")) {
			switch (arg.getString("shape").toLowerCase()) {
			case "rectangle":
			case "square":
			case "cuboid":
			case "box":
			case "block":
				return RegionShape.CUBOID;
			case "cylindar":
			case "pipe":
			case "pole":
			case "column":
				return RegionShape.CYLINDER;
			case "sphere":
			case "ellipsoid":
			case "circle":
			case "ball":
				return RegionShape.ELLIPSOID;
			default:
				return null;
			}
		} else {
			return null;		}
	}

	/**
	 * Gets the priority from a ComplexArgumentParser. Returns null if argument
	 * is not defined or not a valid integer.  
	 * @param arg The ComplexArgumentParser to search through.
	 * @return The value of the priority argument or null if it is not defined
	 * or not a valid integer.
	 */
	private Integer extractPriority(ComplexArgumentParser arg) {
		if (arg.isDefined("priority")) {
			try {
				return arg.getInt("priority");
			} catch (NumberFormatException e) {
				return null;
			}
		} else {
			return null;
		}
	}

	/**
	 * Checks if the user specified that the region ignore the Y coordinate.
	 * @param arg The ComplexArgumentParser to search through.
	 * @return True if the user specified that the region ignore the Y
	 * coordinate, false if not. Never returns null.
	 */
	private Boolean extractIgnoreY(ComplexArgumentParser arg) {
		return (arg.isDefined("ignore-y"));
	}

	/**
	 * Checks if the given arguments have an owner defined. If not, returns
	 * null.
	 * @param arg The ComplexArgumentParser to search through.
	 * @return The user specified by the owner argument, or null if either the
	 * argument or the user does not exist.
	 */
	private IPermissionUser extractOwner(ComplexArgumentParser arg) {
		if (arg.isDefined("owner")) {
			return arg.getUser("owner");
		} else {
			return null;
		}
	}

	/**
	 * Extracts any AreaFlags defined in a ComplexArgumentParser object. Only
	 * extracts flags from the specified argument. Valid values for argument
	 * area:
	 * <ul>
	 * <li>"flags"</li>
	 * <li>"flags-add"</li>
	 * <li>"flags-remove"</li>
	 * </ul>
	 * Will throw an IllegalArgumentException if another value is given.
	 * @param arg The ComplexArgumentParser to search through.
	 * @param argument The specific command argument to search through.
	 * @return An ArrayList of AreaFlags defined in the function. List may be
	 * empty. Never returns null.
	 */
	private List<AreaFlag> extractFlags(ComplexArgumentParser arg, String argument) {
		if (arg == null || argument == null) {
			throw new IllegalArgumentException("Argument can not be null.");
		}
		if (!argument.equals("flags") && !argument.equals("flags-add") && !argument.equals("flags-remove")) {
			throw new IllegalArgumentException("Invalid argument '" + argument + ".' Must be 'flags', 'flags-add', or 'flags-remove.'");
		}
		
		List<AreaFlag> flags = new ArrayList<AreaFlag>();
		List<String> strings = null;
		AreaFlag f;
		
		if (arg.isDefined(argument)) {
			strings = arg.getStringList(argument);
		}
		
		if (strings != null) {
			f = AreaFlag.fromString(strings.get(0));
			if (f != null && !flags.contains(f)) {
				flags.add(f);
			}
		}
		
		return flags;
	}

	private Area findArea(User user, String query) {
		Area area;
		
		// User is explicitly selecting an area
		try {
			
			// If query is a number, search for by ID
			area = AreaManager.getInstance().getArea(Long.parseLong(query));
			if (area == null) {
				user.sendLocalizedMessage("error.area.select.id", query);
			}
		} catch (NumberFormatException e) {
			
			// If query is not a number, search by label
			area = AreaManager.getInstance().getArea(query);
			if (area == null) {
				user.sendLocalizedMessage("error.area.select.label", query);
			}
		}
		
		return area;
	}

	private Area findArea(User user) {
		// Search by user's location, return area with highest priority
		Location location;
		
		// Verify that command sender is a location-bearing object
		if (user.getHandle() instanceof Player) {
			location = ((Player) user.getHandle()).getLocation();
		} else if (user.getHandle() instanceof BlockCommandSender) {
			location = ((BlockCommandSender) user.getHandle()).getBlock().getLocation();
		} else if (user.getHandle() instanceof CommandMinecart) {
			location = ((CommandMinecart) user.getHandle()).getLocation();
		} else {
			user.sendLocalizedMessage("shared.noConsole");
			return null;
		}
		
		// Return a list of areas at current location
		List<Area> areas = AreaManager.getInstance().getAreas(location);
		
		if (areas.isEmpty()) {
			user.sendLocalizedMessage("error.area.select.location");
			return null; // No areas at current location
		}
		return areas.get(0);
	}

	private Area createArea(User user, IPermissionUser owner, RegionShape shape, Location c1, Location c2, boolean ignoreY) {
		try {
			Area area;
			area = AreaManager.getInstance().createArea(owner, shape, c1, c2, ignoreY);
			if (area == null) {
				user.sendLocalizedMessage("error.area.create");
				return null;
			}
			user.sendLocalizedMessage("general.area.create", area.getAreaId()+"");
			return area;
		} catch (Exception e) {

			// An error has occurred. Notify the user and log the error.
			user.sendLocalizedMessage("error.area.create");
			GoldenApple.log(Level.SEVERE, "An error occured while attempting to create a new area.");
			GoldenApple.log(Level.SEVERE, "Please send the following information to the GoldenApple developers:");
			GoldenApple.log(Level.SEVERE, "User:" + user.getName() + " ID:" + user.getId() + ")");
			GoldenApple.log(Level.SEVERE, e);
			return null;
		}
	}

	private boolean deleteArea(User user, long areaId) {

		try {
			// Attempt to delete the area
			AreaManager.getInstance().deleteArea(areaId);
			user.sendLocalizedMessage("general.area.delete", areaId+"");
			return true;
		} catch (SQLException e) {
			
			// An error has occurred. Notify the user and log the error.
			user.sendLocalizedMessage("error.area.delete");
			GoldenApple.log(Level.SEVERE, "An error occured while attempting to delete area " + areaId);
			GoldenApple.log(Level.SEVERE, e);
			return false;
		}
	}

	private boolean setAreaLabel(User user, Area area, String label) {
		area.setLabel(label);
		user.sendLocalizedMessage("general.area.edit.label", area.getAreaId()+"", label);
		return true;
	}

	private boolean addAreaFlags(User user, Area area, List<AreaFlag> flags) {
		int count = 0;
		
		// Attempt to set the flags
		for (AreaFlag flag : flags) {
			area.setFlag(flag, true);
			user.sendLocalizedMessage("general.area.edit.flag.add", area.getAreaId()+"", flag.toString());
			++count;
		}
		
		// Check if no modifications were made
		if (count == 0) {
			user.sendLocalizedMessage("general.area.edit.flag.unmodified", area.getAreaId()+"");
		}
		
		return true;
	}

	private boolean removeAreaFlags(User user, Area area, List<AreaFlag> flags) {
		int count = 0;
		
		// Attempt to reset the flags
		for (AreaFlag flag : flags) {
			area.setFlag(flag, false);
			user.sendLocalizedMessage("general.area.edit.flag.remove", area.getAreaId()+"", flag.toString());
			++count;
		}
		
		// Check if no modifications were made
		if (count == 0) {
			user.sendLocalizedMessage("general.area.edit.flag.unmodified", area.getAreaId()+"");
		}
		
		return true;
	}

	/**
	 * Sends a user a list of areas in which they're currently standing, 6 at a
	 * time,
	 * @param user
	 * @param page
	 * @param all Indicates whether or not to list all areas on the server, or
	 * to only include areas at the user's current location.
	 */
	private void sendAreaList(User user, int page, boolean all) {
		if (!(user.getHandle() instanceof Player) && !all) {
			user.sendLocalizedMessage("shared.noConsole");
			return;
		}
		int per = 6;		// Areas per page
		int total;
		if (user.getHandle() instanceof ConsoleCommandSender) {
			per = 10;
		}
		
		// Fetch the list of areas at the user's current location
		List<Area> areas;
		if (all) {
			areas = AreaManager.getInstance().getAreas(page, per);
			total = AreaManager.getInstance().getTotalAreas();
		} else {
			areas = AreaManager.getInstance().getAreas(((Player) user.getHandle()).getLocation());
			total = areas.size();
		}
		
		// Adjust page number
		if (page > (total + per - 1) / per) {
			page = (total + per -1) / per;
		}
		if (page < 1) {
			page = 1;
		}
		if (!all) {
			areas = areas.subList((page - 1) * per, ((page * per) > total ? total : (page * per)));
		}
		
		// Check if no areas are there
		if (areas.isEmpty()) {
			user.sendLocalizedMessage(all ? "error.area.list.none" : "error.area.list.noneLocation");
			return;
		}
		
		// Print listing header
		user.sendLocalizedMessage((all ? "general.area.list.header" : "general.area.list.headerLocation"), page+"", (total+per-1)/6+"");
		
		for (Area area : areas) {
			user.sendLocalizedMessage("general.area.list.item", area.getAreaId()+"", (area.getLabel() == null || area.getLabel().isEmpty()) ? "[No label]" : area.getLabel(), area.getPriority()+"");
		}
		
	}

	private void sendHelp(User user, String commandLabel, boolean complex) {
		user.sendLocalizedMessage("header.help");
		user.sendLocalizedMultilineMessage((complex) ? "help.area.complex" : "help.area.simple", commandLabel);
	}

	private ArgumentInfo[] getArguments() {
		return new ArgumentInfo[] {
			ArgumentInfo.newString("select", "s", "select", true),
			ArgumentInfo.newSwitch("help", "?", "help"),
			
			ArgumentInfo.newSwitch("override-on", "o:on", "override:on"),
			ArgumentInfo.newSwitch("override-off", "o:off", "override:off"),
			
			ArgumentInfo.newSwitch("create", "c", "create"),
			ArgumentInfo.newSwitch("delete", "d", "delete"),
			ArgumentInfo.newSwitch("list", "ls", "list"),
			ArgumentInfo.newSwitch("all", "a", "all"),
			ArgumentInfo.newInt("page", "pg", "page"),
			
			ArgumentInfo.newSwitch("info", "i", "info"),
			
			// For new areas/regions
			ArgumentInfo.newString("label", "l", "label", true),
			ArgumentInfo.newInt("priority", "p", "priority"),
			ArgumentInfo.newUser("owner", "o", "owner", false, false),
			ArgumentInfo.newString("shape", "sh", "shape", false),
			ArgumentInfo.newSwitch("ignore-y", "y", "ignorey"),
			ArgumentInfo.newStringList("flags", "f", "flags", false),
			
			// For existing areas
			ArgumentInfo.newUserList("owner-add", "o:a", "owner:add", false, false),
			ArgumentInfo.newUserList("owner-remove", "o:r", "owner:remove", false, false),
			ArgumentInfo.newGroupList("group-owner-add", "go:a", "groupowner:add", false),
			ArgumentInfo.newGroupList("group-owner-remove", "go:r", "groupowner:remove", false),
			
			ArgumentInfo.newUserList("invite-full", "in:f", "invite:full", false, false),
			ArgumentInfo.newUserList("invite-none", "in:n", "invite:none", false, false),
			ArgumentInfo.newGroupList("group-invite-full", "gin:f", "groupinvite:full", false),
			ArgumentInfo.newGroupList("group-invite-none", "gin:n", "groupinvite:none", false),
			
			ArgumentInfo.newStringList("flags-add", "f:a", "flags:add", false),
			ArgumentInfo.newStringList("flags-remove", "f:r", "flags:remove", false),
			
			ArgumentInfo.newLong("region-add", "r:a", "region:add"),
			ArgumentInfo.newLongList("region-remove", "r:r", "region:remove")
		};
	}

}
