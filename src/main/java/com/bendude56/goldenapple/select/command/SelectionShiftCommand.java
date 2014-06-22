package com.bendude56.goldenapple.select.command;

import com.bendude56.goldenapple.GoldenApple;
import com.bendude56.goldenapple.User;
import com.bendude56.goldenapple.command.GoldenAppleCommand;
import com.bendude56.goldenapple.select.SelectManager;
import com.bendude56.goldenapple.select.SimpleSelectionProvider;
import com.bendude56.goldenapple.select.SimpleSelectionProvider.Direction;

public class SelectionShiftCommand extends GoldenAppleCommand {
	@Override
	public boolean onExecute(GoldenApple instance, User user, String commandLabel, String[] args) {

		// Permission and prerequisite checks.
		if (args.length < 1 || args.length > 2) {
		    return false;
		} else if (!user.hasPermission(SelectManager.builtinShiftPermission)) {
		    GoldenApple.logPermissionFail(user, commandLabel, args, true);
		    return true;
		} else if (!(SelectManager.getInstance().getSelectionProvider() instanceof SimpleSelectionProvider)) {
		    user.sendLocalizedMessage("error.select.notBuiltin");
		    return true;
		} else if (!SelectManager.getInstance().isSelectionMade(user)) {
		    user.sendLocalizedMessage("error.select.noSelection");
		    return true;
		}

		int amount;
		Direction d;

		// Extract number of blocks
		try {
		    amount = Integer.parseInt(args[0]);
		} catch (NumberFormatException e) {
		    user.sendLocalizedMessage("shared.notANumber", args[0]);
		    return true;
		}

		// Extract direction
		if (args.length > 1) {
			d = Direction.fromString(args[1], user);
		} else {
			d = Direction.fromYawPitch(user.getPlayerHandle().getLocation().getYaw(), user.getPlayerHandle().getLocation().getPitch());
		}
		if (d == null) {
		    user.sendLocalizedMessage("error.select.unknownDirection", args[1]);
		    return true;
		}

		// If blocks is negative, translate shift direction
		if (amount < 0) {
			amount = -amount;
			d = d.getOpposite();
		}

		// Perform operation and provide feedback
		amount = ((SimpleSelectionProvider) SelectManager.getInstance().getSelectionProvider()).shiftSelection(user, amount, d);
		user.sendLocalizedMessage("general.select.shift", amount + "", d.getFriendlyName(user));
		return true;
	}
}
