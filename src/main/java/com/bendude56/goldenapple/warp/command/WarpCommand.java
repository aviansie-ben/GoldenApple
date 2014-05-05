package com.bendude56.goldenapple.warp.command;

import java.util.List;

import org.bukkit.ChatColor;

import com.bendude56.goldenapple.GoldenApple;
import com.bendude56.goldenapple.User;
import com.bendude56.goldenapple.command.GoldenAppleCommand;
import com.bendude56.goldenapple.warp.PermissibleWarp;
import com.bendude56.goldenapple.warp.WarpManager;

public class WarpCommand extends GoldenAppleCommand {
	@Override
	public boolean onExecute(GoldenApple instance, User user, String commandLabel, String[] args) {
		if (args.length > 2) return false;
		
		if (!user.hasPermission(WarpManager.warpPermission)) {
			GoldenApple.logPermissionFail(user, commandLabel, args, true);
			return true;
		} else if (args.length == 2 && !user.hasPermission(WarpManager.warpOtherPermission)) {
		    GoldenApple.logPermissionFail(user, commandLabel, args, true);
            return true;
		}
		
		if (args.length == 0 || args[0].equalsIgnoreCase("list")) {
		    List<PermissibleWarp> available = WarpManager.getInstance().getAvailableNamedWarps(user);
		    
		    user.sendLocalizedMessage("header.warps");
		    
		    if (available.size() > 0) {
		        user.sendLocalizedMessage("general.warp.list");
		        
		        for (PermissibleWarp w : available) {
		            user.getHandle().sendMessage(ChatColor.GRAY + "  " + w.getDisplayName());
		        }
		    } else {
		        user.sendLocalizedMessage("general.warp.listNone");
		    }
		} else if (args.length == 1) {
    		int deathCooldown = WarpManager.getInstance().getDeathCooldown(user), teleportCooldown = WarpManager.getInstance().getTeleportCooldown(user);
    		PermissibleWarp w = WarpManager.getInstance().getNamedWarp(args[0]);
    		
    		if (w == null) {
    			user.sendLocalizedMessage("error.warp.notFound", args[0]);
    		} else if (!w.canTeleport(user)) {
    			GoldenApple.logPermissionFail(user, commandLabel, args, true);
    		} else if (deathCooldown > 0) {
    			user.sendLocalizedMessage("error.warps.cooldownDeath", deathCooldown + "");
    		} else if (teleportCooldown > 0) {
    			user.sendLocalizedMessage("error.warps.cooldown", teleportCooldown + "");
    		} else {
    			w.teleport(user);
    			WarpManager.getInstance().startTeleportCooldown(user);
    		}
		} else if (args.length == 2) {
		    PermissibleWarp w = WarpManager.getInstance().getNamedWarp(args[0]);
		    User u = User.getUser(args[1]);
		    
		    if (w == null) {
		        user.sendLocalizedMessage("error.warp.notFound", args[0]);
		    } else if (!w.canTeleport(user)) {
		        GoldenApple.logPermissionFail(user, commandLabel, args, true);
		    } else if (u == null) {
		        user.sendLocalizedMessage("shared.userNotFoundError", args[1]);
		    } else {
		        w.teleport(u);
		        u.sendLocalizedMessage("general.warps.teleportBy", user.getDisplayName());
		    }
		}
		
		return true;
	}
}
