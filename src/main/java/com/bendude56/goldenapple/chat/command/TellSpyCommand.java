package com.bendude56.goldenapple.chat.command;

import com.bendude56.goldenapple.GoldenApple;
import com.bendude56.goldenapple.User;
import com.bendude56.goldenapple.chat.ChatManager;
import com.bendude56.goldenapple.command.GoldenAppleCommand;

public class TellSpyCommand extends GoldenAppleCommand {

    @Override
    public boolean onExecute(GoldenApple instance, User user, String commandLabel, String[] args) {
        if (args.length != 1) return false;
        
        if (!user.hasPermission(ChatManager.tellSpyPermission)) {
            GoldenApple.logPermissionFail(user, commandLabel, args, true);
        }
        
        boolean spy = ChatManager.getInstance().getTellSpyStatus(user);
        
        if (args[0].equalsIgnoreCase("on")) {
            if (!spy) {
                ChatManager.getInstance().setTellSpyStatus(user, true);
                user.sendLocalizedMessage("general.tellspy.on");
            } else {
                user.sendLocalizedMessage("error.tellspy.alreadyOn");
            }
        } else if (args[0].equalsIgnoreCase("off")) {
            if (spy) {
                ChatManager.getInstance().setTellSpyStatus(user, false);
                user.sendLocalizedMessage("general.tellspy.off");
            } else {
                user.sendLocalizedMessage("error.tellspy.alreadyOff");
            }
        } else {
            return false;
        }
        
        return true;
    }

}
