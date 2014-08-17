package com.bendude56.goldenapple.warp.command;

import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.bukkit.event.player.PlayerTeleportEvent.TeleportCause;

import com.bendude56.goldenapple.GoldenApple;
import com.bendude56.goldenapple.User;
import com.bendude56.goldenapple.command.GoldenAppleCommand;
import com.bendude56.goldenapple.warp.WarpManager;

public class TpCommand extends GoldenAppleCommand {
    @Override
    public boolean onExecute(GoldenApple instance, User user, String commandLabel, String[] args) {
        if (args.length == 0) {
            return false;
        } else if (args.length == 1) {
            User user2 = User.findUser(args[0]);
            if (!(user.getHandle() instanceof Player)) {
                user.sendLocalizedMessage("shared.consoleNotAllowed");
            } else if (!user.hasPermission(WarpManager.tpSelfToPlayerPermission)) {
                GoldenApple.logPermissionFail(user, commandLabel, args, true);
            } else if (user2 == null) {
                user.sendLocalizedMessage("shared.parser.userNotFound.error", args[0]);
            } else if (user.getPlayerHandle().teleport(user2.getPlayerHandle(), TeleportCause.COMMAND)) {} else {
                user.sendLocalizedMessage("module.warp.error.pluginCancel");
            }
        } else if (args.length == 2) {
            User user1 = User.findUser(args[0]);
            User user2 = User.findUser(args[1]);
            if (!user.hasPermission(WarpManager.tpOtherToPlayerPermission)) {
                GoldenApple.logPermissionFail(user, commandLabel, args, true);
            } else if (user1 == null) {
                user.sendLocalizedMessage("shared.parser.userNotFound.error", args[0]);
            } else if (user2 == null) {
                user.sendLocalizedMessage("shared.parser.userNotFound.error", args[1]);
            } else if (user1.getPlayerHandle().teleport(user2.getPlayerHandle(), TeleportCause.COMMAND)) {
                user1.sendLocalizedMessage("module.warp.teleportedBy", user.getName());
            } else {
                user.sendLocalizedMessage("module.warp.error.pluginCancel");
            }
        } else if (args.length == 3) {
            if (!(user.getHandle() instanceof Player)) {
                user.sendLocalizedMessage("shared.consoleNotAllowed");
            } else if (!user.hasPermission(WarpManager.tpSelfToCoordPermission)) {
                GoldenApple.logPermissionFail(user, commandLabel, args, true);
            } else {
                Location loc = user.getPlayerHandle().getLocation();
                
                try {
                    loc.setX(Double.parseDouble(args[0]));
                    loc.setY(Double.parseDouble(args[1]));
                    loc.setZ(Double.parseDouble(args[2]));
                } catch (NumberFormatException e) {
                    user.sendLocalizedMessage("shared.convertError.location", args[0], args[1], args[2]);
                    return true;
                }
                
                if (!user.getPlayerHandle().teleport(loc, TeleportCause.COMMAND)) {
                    user.sendLocalizedMessage("module.warp.error.pluginCancel");
                }
            }
        } else if (args.length == 4) {
            if (!user.hasPermission(WarpManager.tpOtherToCoordPermission)) {
                GoldenApple.logPermissionFail(user, commandLabel, args, true);
            } else {
                User user1 = User.findUser(args[0]);
                Location loc = user1.getPlayerHandle().getLocation();
                
                try {
                    loc.setX(Double.parseDouble(args[1]));
                    loc.setY(Double.parseDouble(args[2]));
                    loc.setZ(Double.parseDouble(args[3]));
                } catch (NumberFormatException e) {
                    user.sendLocalizedMessage("shared.convertError.location", args[1], args[2], args[3]);
                    return true;
                }
                
                if (user.getPlayerHandle().teleport(loc, TeleportCause.COMMAND)) {
                    user1.sendLocalizedMessage("module.warp.teleportedBy", user.getName());
                } else {
                    user.sendLocalizedMessage("module.warp.error.pluginCancel");
                }
            }
        } else {
            return false;
        }
        
        return true;
    }
}
