package com.bendude56.goldenapple.invisible.command;

import com.bendude56.goldenapple.GoldenApple;
import com.bendude56.goldenapple.User;
import com.bendude56.goldenapple.command.DualSyntaxCommand;
import com.bendude56.goldenapple.invisible.InvisibilityManager;
import com.bendude56.goldenapple.util.ComplexArgumentParser;
import com.bendude56.goldenapple.util.ComplexArgumentParser.ArgumentInfo;

public class PoofCommand extends DualSyntaxCommand {
    
    @Override
    public void onExecuteComplex(GoldenApple instance, User user, String commandLabel, String[] args) {
        ComplexArgumentParser arg = new ComplexArgumentParser(getArguments());
        boolean turnOn = !InvisibilityManager.getInstance().isInvisible(user);
        
        if (!user.hasPermission(InvisibilityManager.vanishPermission)) {
            GoldenApple.logPermissionFail(user, commandLabel, args, true);
            return;
        }
        
        if (!arg.parse(user, args)) return;
        
        if ((arg.isDefined("allow-interact") || arg.isDefined("allow-pickup")) && !user.hasPermission(InvisibilityManager.vanishInteractPermission)) {
            GoldenApple.logPermissionFail(user, commandLabel, args, true);
            return;
        }
        
        if (arg.isDefined("turn-on")) {
            turnOn = true;
        } else if (arg.isDefined("turn-off")) {
            turnOn = false;
        }
        
        if (turnOn) {
            InvisibilityManager.getInstance().setInvisible(user, true);
            InvisibilityManager.getInstance().setInvisibilityFlag(user, "interact", arg.isDefined("allow-interact"));
            InvisibilityManager.getInstance().setInvisibilityFlag(user, "damage", arg.isDefined("allow-damage"));
            InvisibilityManager.getInstance().setInvisibilityFlag(user, "target", arg.isDefined("allow-target"));
            InvisibilityManager.getInstance().setInvisibilityFlag(user, "pickup", arg.isDefined("allow-pickup"));
            user.sendLocalizedMessage("module.invisible.poof.enable");
        } else {
            InvisibilityManager.getInstance().setInvisible(user, false);
            user.sendLocalizedMessage("module.invisible.poof.disable");
        }
    }
    
    @Override
    public void onExecuteSimple(GoldenApple instance, User user, String commandLabel, String[] args) {
        onExecuteComplex(instance, user, commandLabel, args);
    }
    
    private ArgumentInfo[] getArguments() {
        return new ArgumentInfo[] {
            ArgumentInfo.newSwitch("allow-interact", null, "allow-interact"),
            ArgumentInfo.newSwitch("allow-damage", null, "allow-damage"),
            ArgumentInfo.newSwitch("allow-target", null, "allow-target"),
            ArgumentInfo.newSwitch("allow-pickup", null, "allow-pickup"),
            ArgumentInfo.newSwitch("turn-on", null, "on"),
            ArgumentInfo.newSwitch("turn-off", null, "off")
        };
    }
}
