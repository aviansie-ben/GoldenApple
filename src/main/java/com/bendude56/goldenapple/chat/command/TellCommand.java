package com.bendude56.goldenapple.chat.command;

import com.bendude56.goldenapple.GoldenApple;
import com.bendude56.goldenapple.User;
import com.bendude56.goldenapple.chat.ChatManager;
import com.bendude56.goldenapple.command.GoldenAppleCommand;

public class TellCommand extends GoldenAppleCommand {
    
    @Override
    public boolean onExecute(GoldenApple instance, User user, String commandLabel, String[] args) {
        if (args.length < 2) {
            return false;
        }
        
        User receiver = (args[0].equalsIgnoreCase("server")) ? User.getConsoleUser() : User.findUser(args[0]);
        
        if (receiver == null) {
            user.sendLocalizedMessage("shared.parser.userNotFound.error", args[0]);
        } else if (receiver == user) {
            user.sendLocalizedMessage("module.chat.tell.self");
        } else {
            String message = args[1];
            
            for (int i = 2; i < args.length; i++) {
                message += " " + args[i];
            }
            
            ChatManager.getInstance().sendTellMessage(user, receiver, message);
        }
        
        return true;
    }
    
}
