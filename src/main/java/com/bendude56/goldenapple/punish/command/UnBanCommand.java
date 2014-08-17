package com.bendude56.goldenapple.punish.command;

import com.bendude56.goldenapple.GoldenApple;
import com.bendude56.goldenapple.User;
import com.bendude56.goldenapple.command.GoldenAppleCommand;
import com.bendude56.goldenapple.permissions.IPermissionUser;
import com.bendude56.goldenapple.permissions.PermissionManager;

public class UnBanCommand extends GoldenAppleCommand {
    
    @Override
    public boolean onExecute(GoldenApple instance, User user, String commandLabel, String[] args) {
        if (args.length != 1 && (args.length != 2 || (!args[1].equalsIgnoreCase("-v") && !args[1].equalsIgnoreCase("--verify")))) {
            return false;
        }
        
        user.sendLocalizedMessage("module.punish.header");
        
        IPermissionUser target = PermissionManager.getInstance().findUser(args[0], false);
        
        if (target == null) {
            user.sendLocalizedMessage("shared.parser.userNotFound.error", args[0]);
        } else {
            if (user.getVariableBoolean("goldenapple.complexSyntax")) {
                BanCommand.banVoid(target, user, "gaban", new String[] { "-u", target.getName(), "-v" }, args.length >= 2 && (args[1].equalsIgnoreCase("-v") || args[1].equalsIgnoreCase("--verify")));
            } else {
                BanCommand.banVoid(target, user, "gaban", new String[] { target.getName(), "void" }, args.length >= 2 && (args[1].equalsIgnoreCase("-v") || args[1].equalsIgnoreCase("--verify")));
            }
        }
        
        return true;
    }
    
}
