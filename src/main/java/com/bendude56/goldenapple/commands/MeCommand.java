package com.bendude56.goldenapple.commands;

import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import com.bendude56.goldenapple.User;
import com.bendude56.goldenapple.chat.ChatChannel;
import com.bendude56.goldenapple.chat.ChatManager;

public class MeCommand implements CommandExecutor {
	@Override
	public boolean onCommand(CommandSender sender, Command command, String commandLabel, String[] args) {
		User user = User.getUser(sender);
		
		ChatChannel channel = ChatManager.getInstance().getActiveChannel(user);
		if (channel == null) {
			user.sendLocalizedMessage("error.channel.notInChannel");
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
