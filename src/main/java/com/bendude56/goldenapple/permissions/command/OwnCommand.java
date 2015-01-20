package com.bendude56.goldenapple.permissions.command;

import com.bendude56.goldenapple.GoldenApple;
import com.bendude56.goldenapple.User;
import com.bendude56.goldenapple.command.GoldenAppleCommand;
import com.bendude56.goldenapple.command.VerifyCommand;
import com.bendude56.goldenapple.permissions.PermissionManager;

public class OwnCommand extends GoldenAppleCommand {
    @Override
    public boolean onExecute(GoldenApple instance, User user, String commandLabel, String[] args) {
        if (!user.getHandle().isOp()) {
            GoldenApple.logPermissionFail(user, commandLabel, args, true);
            return true;
        }
        
        if (GoldenApple.getInstanceMainConfig().getBoolean("securityPolicy.disableOwn")) {
            user.sendLocalizedMessage("module.permissions.own.disabled");
            GoldenApple.logPermissionFail(user, commandLabel, args, false);
            return true;
        }
        
        if (args.length == 0 || !args[0].equals("-v")) {
            user.sendLocalizedMessage("module.permissions.own.warning.before");
            VerifyCommand.commands.put(user, "gaown -v");
        } else {
            user.addPermission(PermissionManager.getInstance().getRootNode().getStarPermission());
            user.sendLocalizedMessage("module.permissions.own.success");
            user.sendLocalizedMessage("module.permissions.own.warning.after");
        }
        
        return true;
    }
}
