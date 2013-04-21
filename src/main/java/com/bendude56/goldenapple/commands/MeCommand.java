package com.bendude56.goldenapple.commands;

import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import com.bendude56.goldenapple.GoldenApple;
import com.bendude56.goldenapple.User;
import com.bendude56.goldenapple.chat.ChatChannel;

public class MeCommand implements CommandExecutor {
	@Override
	public boolean onCommand(CommandSender sender, Command command, String commandLabel, String[] args) {
		GoldenApple instance = GoldenApple.getInstance();
		User user = User.getUser(sender);
		
		ChatChannel channel = instance.chat.getActiveChannel(user);
		if (channel == null) {
			instance.locale.sendMessage(user, "error.channel.notInChannel", false);
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
