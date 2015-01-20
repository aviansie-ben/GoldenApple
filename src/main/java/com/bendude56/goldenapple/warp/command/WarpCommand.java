package com.bendude56.goldenapple.warp.command;

import java.util.List;

import com.bendude56.goldenapple.GoldenApple;
import com.bendude56.goldenapple.User;
import com.bendude56.goldenapple.command.GoldenAppleCommand;
import com.bendude56.goldenapple.warp.PermissibleWarp;
import com.bendude56.goldenapple.warp.WarpManager;

public class WarpCommand extends GoldenAppleCommand {
    @Override
    public boolean onExecute(GoldenApple instance, User user, String commandLabel, String[] args) {
        if (args.length > 2) {
            return false;
        }
        
        if (!user.hasPermission(WarpManager.warpPermission)) {
            GoldenApple.logPermissionFail(user, commandLabel, args, true);
            return true;
        } else if (args.length == 2 && !user.hasPermission(WarpManager.warpOtherPermission)) {
            GoldenApple.logPermissionFail(user, commandLabel, args, true);
            return true;
        }
        
        if (args.length == 0 || args[0].equalsIgnoreCase("list")) {
            List<PermissibleWarp> available = WarpManager.getInstance().getAvailableNamedWarps(user);
            
            user.sendLocalizedMessage("module.warp.header");
            
            if (available.size() > 0) {
                user.sendLocalizedMessage("module.warp.list.header");
                
                for (PermissibleWarp w : available) {
                    user.sendLocalizedMessage("module.warp.list.entry", w.getDisplayName());
                }
            } else {
                user.sendLocalizedMessage("module.warp.list.none");
            }
        } else if (args.length == 1) {
            int deathCooldown = WarpManager.getInstance().getDeathCooldown(user), teleportCooldown = WarpManager.getInstance().getTeleportCooldown(user);
            PermissibleWarp w = WarpManager.getInstance().getNamedWarp(args[0]);
            
            if (w == null) {
                user.sendLocalizedMessage("module.warp.error.notFound", args[0]);
            } else if (!w.canTeleport(user)) {
                GoldenApple.logPermissionFail(user, commandLabel, args, true);
            } else if (deathCooldown > 0) {
                user.sendLocalizedMessage("module.warp.error.cooldown.death", deathCooldown);
            } else if (teleportCooldown > 0) {
                user.sendLocalizedMessage("module.warp.error.cooldown.normal", teleportCooldown);
            } else {
                w.teleport(user);
                WarpManager.getInstance().startTeleportCooldown(user);
            }
        } else if (args.length == 2) {
            PermissibleWarp w = WarpManager.getInstance().getNamedWarp(args[0]);
            User u = User.findUser(args[1]);
            
            if (w == null) {
                user.sendLocalizedMessage("module.warp.error.notFound", args[0]);
            } else if (!w.canTeleport(user)) {
                GoldenApple.logPermissionFail(user, commandLabel, args, true);
            } else if (u == null) {
                user.sendLocalizedMessage("shared.parser.userNotFound.error", args[1]);
            } else {
                w.teleport(u);
                u.sendLocalizedMessage("module.warp.teleportedBy", user.getDisplayName());
            }
        }
        
        return true;
    }
}
