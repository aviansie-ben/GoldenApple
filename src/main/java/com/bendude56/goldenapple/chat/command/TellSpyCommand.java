package com.bendude56.goldenapple.chat.command;

import com.bendude56.goldenapple.GoldenApple;
import com.bendude56.goldenapple.User;
import com.bendude56.goldenapple.chat.ChatManager;
import com.bendude56.goldenapple.command.GoldenAppleCommand;

public class TellSpyCommand extends GoldenAppleCommand {
    
    @Override
    public boolean onExecute(GoldenApple instance, User user, String commandLabel, String[] args) {
        if (args.length != 1) {
            return false;
        }
        
        if (!user.hasPermission(ChatManager.tellSpyPermission)) {
            GoldenApple.logPermissionFail(user, commandLabel, args, true);
            return true;
        }
        
        boolean spy = ChatManager.getInstance().getTellSpyStatus(user);
        
        if (args[0].equalsIgnoreCase("on")) {
            if (!spy) {
                ChatManager.getInstance().setTellSpyStatus(user, true);
                user.sendLocalizedMessage("module.chat.tell.spy.enabled");
            } else {
                user.sendLocalizedMessage("module.chat.tell.spy.alreadyEnabled");
            }
        } else if (args[0].equalsIgnoreCase("off")) {
            if (spy) {
                ChatManager.getInstance().setTellSpyStatus(user, false);
                user.sendLocalizedMessage("module.chat.tell.spy.disabled");
            } else {
                user.sendLocalizedMessage("module.chat.tell.spy.alreadyDisabled");
            }
        } else {
            return false;
        }
        
        return true;
    }
    
}
