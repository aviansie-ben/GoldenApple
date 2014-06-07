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
        
        if (!arg.parse(user, args)) return;
        
        if (arg.isDefined("turn-on")) {
            turnOn = true;
        } else if (arg.isDefined("turn-off")) {
            turnOn = false;
        }
        
        if (turnOn) {
            InvisibilityManager.getInstance().setInvisible(user, true);
            InvisibilityManager.getInstance().setInteractionEnabled(user, arg.isDefined("allow-interact"));
            user.sendLocalizedMessage("general.poof.on");
        } else {
            InvisibilityManager.getInstance().setInvisible(user, false);
            InvisibilityManager.getInstance().setInteractionEnabled(user, true);
            user.sendLocalizedMessage("general.poof.off");
        }
    }
    
    @Override
    public void onExecuteSimple(GoldenApple instance, User user, String commandLabel, String[] args) {
        onExecuteComplex(instance, user, commandLabel, args);
    }
    
    private ArgumentInfo[] getArguments() {
        return new ArgumentInfo[] {
            ArgumentInfo.newSwitch("allow-interact", null, "allow-interact"),
            ArgumentInfo.newSwitch("turn-on", null, "on"),
            ArgumentInfo.newSwitch("turn-off", null, "off")
        };
    }
}
