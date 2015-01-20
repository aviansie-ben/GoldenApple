package com.bendude56.goldenapple.chat.command;

import com.bendude56.goldenapple.GoldenApple;
import com.bendude56.goldenapple.User;
import com.bendude56.goldenapple.chat.ChatManager;
import com.bendude56.goldenapple.command.GoldenAppleCommand;

public class AfkCommand extends GoldenAppleCommand {
    
    @Override
    public boolean onExecute(GoldenApple instance, User user, String commandLabel, String[] args) {
        if (args.length != 0) {
            return false;
        }
        
        boolean alreadyAfk = ChatManager.getInstance().getAfkStatus(user);
        
        ChatManager.getInstance().setAfkStatus(user, !alreadyAfk, true);
        
        if (!alreadyAfk) {
            user.sendLocalizedMessage("module.chat.afk.success.on");
        } else {
            user.sendLocalizedMessage("module.chat.afk.success.off");
        }
        
        return true;
    }
    
}
