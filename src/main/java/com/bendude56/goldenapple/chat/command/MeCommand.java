package com.bendude56.goldenapple.chat.command;

import com.bendude56.goldenapple.GoldenApple;
import com.bendude56.goldenapple.User;
import com.bendude56.goldenapple.chat.ChatManager;
import com.bendude56.goldenapple.chat.IChatChannel;
import com.bendude56.goldenapple.command.GoldenAppleCommand;

public class MeCommand extends GoldenAppleCommand {
    @Override
    public boolean onExecute(GoldenApple instance, User user, String commandLabel, String[] args) {
        IChatChannel channel = ChatManager.getInstance().getActiveChannel(user);
        if (channel == null) {
            user.sendLocalizedMessage("module.chat.error.notInChannel.me");
        } else if (args.length > 0) {
            String msg = "";
            for (String arg : args) {
                msg += arg + " ";
            }
            msg = msg.substring(0, msg.length() - 1);
            synchronized (channel) {
                channel.sendMeMessage(user, msg);
            }
        }
        
        return true;
    }
}
