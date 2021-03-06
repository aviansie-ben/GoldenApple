package com.bendude56.goldenapple.chat.command;

import com.bendude56.goldenapple.GoldenApple;
import com.bendude56.goldenapple.User;
import com.bendude56.goldenapple.chat.ChatManager;
import com.bendude56.goldenapple.command.GoldenAppleCommand;

public class ReplyCommand extends GoldenAppleCommand {
    
    @Override
    public boolean onExecute(GoldenApple instance, User user, String commandLabel, String[] args) {
        if (args.length < 1) {
            return false;
        }
        
        if (!user.hasPermission(ChatManager.tellPermission)) {
            GoldenApple.logPermissionFail(user, commandLabel, args, true);
            return true;
        }
        
        String message = args[0];
        for (int i = 1; i < args.length; i++) {
            message += " " + args[i];
        }
        
        if (!ChatManager.getInstance().sendReplyMessage(user, message)) {
            user.sendLocalizedMessage("module.chat.tell.noReply");
        }
        
        return true;
    }
    
}
