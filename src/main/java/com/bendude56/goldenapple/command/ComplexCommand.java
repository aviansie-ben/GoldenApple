package com.bendude56.goldenapple.command;

import org.bukkit.entity.Player;

import com.bendude56.goldenapple.GoldenApple;
import com.bendude56.goldenapple.User;

public class ComplexCommand extends GoldenAppleCommand {
    @Override
    public boolean onExecute(GoldenApple instance, User user, String commandLabel, String[] args) {
        if (user.getHandle() instanceof Player) {
            if (args.length == 1 && args[0].equals("-v")) {
                if (user.getVariableBoolean("goldenapple.complexSyntax")) {
                    user.setVariable("goldenapple.complexSyntax", false);
                    user.sendLocalizedMessage("module.base.complex.success.off");
                } else {
                    user.setVariable("goldenapple.complexSyntax", true);
                    user.sendLocalizedMessage("module.base.complex.success.on");
                }
            } else {
                user.sendLocalizedMessage((user.getVariableBoolean("goldenapple.complexSyntax")) ? "module.base.complex.warning.off" : "module.base.complex.warning.on");
                VerifyCommand.commands.put(user, commandLabel + " -v");
            }
        } else {
            user.sendLocalizedMessage("shared.consoleNotAllowed");
        }
        
        return true;
    }
}
