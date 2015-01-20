package com.bendude56.goldenapple.command;

import com.bendude56.goldenapple.GoldenApple;
import com.bendude56.goldenapple.User;
import com.bendude56.goldenapple.permissions.PermissionManager;

public class ImportCommand extends GoldenAppleCommand {
    @Override
    public boolean onExecute(GoldenApple instance, User user, String commandLabel, String[] args) {
        if (args.length < 2) {
            return false;
        } else if (!user.hasPermission(PermissionManager.importPermission)) {
            GoldenApple.logPermissionFail(user, commandLabel, args, true);
        } else {
            // TODO If needed, this command should be reimplemented
        }
        
        return true;
    }
}
