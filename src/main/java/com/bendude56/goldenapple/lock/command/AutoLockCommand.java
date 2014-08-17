package com.bendude56.goldenapple.lock.command;

import org.bukkit.entity.Player;

import com.bendude56.goldenapple.GoldenApple;
import com.bendude56.goldenapple.User;
import com.bendude56.goldenapple.command.GoldenAppleCommand;

public class AutoLockCommand extends GoldenAppleCommand {
    @Override
    public boolean onExecute(GoldenApple instance, User user, String commandLabel, String[] args) {
        if (user.getHandle() instanceof Player) {
            if (user.getVariableBoolean("goldenapple.lock.autoLock")) {
                user.setVariable("goldenapple.lock.autoLock", false);
                user.sendLocalizedMessage("module.lock.autoLock.disabled");
            } else {
                user.setVariable("goldenapple.lock.autoLock", true);
                user.sendLocalizedMessage("module.lock.autoLock.enabled");
            }
        } else {
            user.sendLocalizedMessage("shared.consoleNotAllowed");
        }
        
        return true;
    }
}
