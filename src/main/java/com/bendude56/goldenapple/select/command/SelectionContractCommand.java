package com.bendude56.goldenapple.select.command;

import com.bendude56.goldenapple.GoldenApple;
import com.bendude56.goldenapple.User;
import com.bendude56.goldenapple.command.GoldenAppleCommand;
import com.bendude56.goldenapple.select.SelectManager;
import com.bendude56.goldenapple.select.SimpleSelectionProvider;
import com.bendude56.goldenapple.select.SimpleSelectionProvider.Direction;

public class SelectionContractCommand extends GoldenAppleCommand {
	@Override
	public boolean onExecute(GoldenApple instance, User user, String commandLabel, String[] args) {

		// Permission and prerequisite checks.
		if (args.length < 1 || args.length > 3) {
		    return false;
		} else if (!user.hasPermission(SelectManager.builtinContractPermission)) {
		    GoldenApple.logPermissionFail(user, commandLabel, args, true);
		    user.sendLocalizedMessage("shared.noPermission");
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
		boolean reverse = false;
		int reverseAmount = 0;

		// Extract number of blocks
		try {
		    amount = Integer.parseInt(args[0]);
		} catch (NumberFormatException e) {
		    user.sendLocalizedMessage("shared.notANumber", args[0]);
		    return true;
		}
		if (args.length > 1) {
			try {
				reverseAmount = Integer.parseInt(args[1]);
				reverse = true;
			} catch (NumberFormatException e) {
				if (args.length > 2) {
					user.sendLocalizedMessage("shared.notNumber", args[1]);
					return true;
				}
			}
		}

		// Make sure user has permission to use negative numbers
		if ((amount < 0 || reverseAmount < 0) && !user.hasPermission(SelectManager.builtinExpandPermission)) {
			GoldenApple.logPermissionFail(user, commandLabel, args, true);
			user.sendLocalizedMessage("shared.noPermission");
			return true;
		}

		// Extract direction
		if (args.length > (reverse ? 2 : 1)) {
			d = Direction.fromString(args[reverse ? 2 : 1], user);			
		} else {
			d = Direction.fromYawPitch(user.getPlayerHandle().getLocation().getYaw(), user.getPlayerHandle().getLocation().getPitch());
		}
		if (d == null) {
		    user.sendLocalizedMessage("error.select.unknownDirection", args[reverse ? 2 : 1]);
		    return true;
		}

		// Perform operation and provide feedback
		if (amount >= 0) {
			amount = ((SimpleSelectionProvider) SelectManager.getInstance().getSelectionProvider()).contractSelection(user, amount, d);
			user.sendLocalizedMessage("general.select.contract", amount + "", d.getFriendlyName(user));
		} else {
			amount = -amount;
			amount = ((SimpleSelectionProvider) SelectManager.getInstance().getSelectionProvider()).expandSelection(user, amount, d);
			user.sendLocalizedMessage("general.select.expand", amount + "", d.getFriendlyName(user));
		}
		if (reverse) {
			if (reverseAmount >= 0) {
				reverseAmount = ((SimpleSelectionProvider) SelectManager.getInstance().getSelectionProvider()).contractSelection(user, reverseAmount, d.getOpposite());
				user.sendLocalizedMessage("general.select.contract", reverseAmount + "", d.getOpposite().getFriendlyName(user));
			} else {
				reverseAmount = -reverseAmount;
				reverseAmount = ((SimpleSelectionProvider) SelectManager.getInstance().getSelectionProvider()).expandSelection(user, reverseAmount, d.getOpposite());
				user.sendLocalizedMessage("general.select.expand", reverseAmount + "", d.getOpposite().getFriendlyName(user));
			}
		}
		return true;
	}
}
