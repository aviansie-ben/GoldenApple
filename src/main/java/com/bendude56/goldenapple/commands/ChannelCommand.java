package com.bendude56.goldenapple.commands;

import org.bukkit.ChatColor;

import com.bendude56.goldenapple.GoldenApple;
import com.bendude56.goldenapple.User;
import com.bendude56.goldenapple.chat.ChatChannel;
import com.bendude56.goldenapple.chat.ChatChannel.ChatChannelUserLevel;

public class ChannelCommand extends DualSyntaxCommand {
	@Override
	public void onCommandComplex(GoldenApple instance, User user, String commandLabel, String[] args) {
		if (args.length == 0 || args[0].equals("-?") || args[0].equals("help")) {
			sendHelp(user, commandLabel, true, instance.chat.getActiveChannelLevel(user));
		} else if (args[0].equals("--list")) {
			instance.locale.sendMessage(user, "header.chat", false);
			instance.locale.sendMessage(user, "general.channel.list", false);
			for (ChatChannel c : instance.chat.getActiveChannels()) {
				String name = (c.getName().equalsIgnoreCase(ChatColor.stripColor(c.getDisplayName()))) ? c.getDisplayName() : (c.getDisplayName() + " (" + c.getName() + ")");
				switch (c.getDisplayLevel(user)) {
					case CONNECTED:
						user.getHandle().sendMessage(ChatColor.GREEN + ChatColor.stripColor(name));
						break;
					case NORMAL:
						user.getHandle().sendMessage(ChatColor.WHITE + ChatColor.stripColor(name));
						break;
					case GRAYED_OUT:
						user.getHandle().sendMessage(ChatColor.DARK_GRAY + ChatColor.stripColor(name));
						break;
				}
			}
		} else if (args[0].equals("-l")) {
			instance.locale.sendMessage(user, "header.chat", false);
			if (instance.chat.getActiveChannel(user) == null) {
				instance.locale.sendMessage(user, "error.channel.notInChannelCommand", false);
			} else {
				instance.chat.leaveChannel(user, true);
			}
		} else if (args[0].equals("-k")) {
			ChatChannel c = instance.chat.getActiveChannel(user);
			instance.locale.sendMessage(user, "header.chat", false);
			if (args.length == 1) {
				instance.locale.sendMessage(user, "shared.parameterMissing", false, "-k");
			} else if (c == null) {
				instance.locale.sendMessage(user, "error.channel.notInChannelCommand", false);
			} else if (GoldenApple.getInstance().chat.getActiveChannelLevel(user).id < ChatChannelUserLevel.MODERATOR.id) {
				GoldenApple.logPermissionFail(user, commandLabel, args, true);
			} else {
				User u = User.getUser(args[1]);
				if (u == null) {
					instance.locale.sendMessage(user, "shared.userNotFoundError", false);
				} else if (c.isInChannel(u)) {
					instance.chat.kickFromChannel(u);
				} else {
					instance.locale.sendMessage(user, "error.channel.kick.notInChannel", false, u.getDisplayName());
				}
			}
		} else if (args[0].equals("--motd")) {
			ChatChannel c = instance.chat.getActiveChannel(user);
			instance.locale.sendMessage(user, "header.chat", false);
			if (c == null) {
				instance.locale.sendMessage(user, "error.channel.notInChannelCommand", false);
			} else if (GoldenApple.getInstance().chat.getActiveChannelLevel(user).id < ChatChannelUserLevel.MODERATOR.id) {
				GoldenApple.logPermissionFail(user, commandLabel, args, true);
			} else if (args.length == 1) {
				c.motd = null;
				c.save();
				instance.locale.sendMessage(user, "general.channel.motd.clear", false);
			} else {
				c.motd = "";
				for (int i = 1; i < args.length; i++) {
					if (i != 1) c.motd += " ";
					c.motd += args[i];
				}
				c.save();
				instance.locale.sendMessage(user, "general.channel.motd.set", false);
			}
		} else if (args[0].equals("-j")) {
			instance.locale.sendMessage(user, "header.chat", false);
			if (args.length == 1) {
				instance.locale.sendMessage(user, "shared.parameterMissing", false, "-j");
			} else {
				ChatChannel c = instance.chat.getChannel(args[1]);
				if (c == null) {
					instance.locale.sendMessage(user, "error.channel.notFound", false, args[1]);
				} else {
					instance.chat.tryJoinChannel(user, c, true);
				}
			}
		}
	}

	public void onCommandSimple(GoldenApple instance, User user, String commandLabel, String[] args) {
		
	}

	private void sendHelp(User user, String commandLabel, boolean complex, ChatChannelUserLevel level) {
		GoldenApple.getInstance().locale.sendMessage(user, "header.help", false);
		if (level == ChatChannelUserLevel.UNKNOWN) {
			showHelpSection(user, commandLabel, "out", complex);
		} else {
			showHelpSection(user, commandLabel, "inNormal", complex);
			if (level.id >= ChatChannelUserLevel.MODERATOR.id) showHelpSection(user, commandLabel, "inMod", complex);
			if (level.id >= ChatChannelUserLevel.SUPER_MODERATOR.id) showHelpSection(user, commandLabel, "inSuperMod", complex);
			if (level.id >= ChatChannelUserLevel.ADMINISTRATOR.id) showHelpSection(user, commandLabel, "inAdmin", complex);
		}
	}
	
	private void showHelpSection(User user, String commandLabel, String section, boolean complex) {
		GoldenApple.getInstance().locale.sendMessage(user, (complex) ? "help.channel." + section + ".complex" : "help.channel." + section + ".simple", true, commandLabel);
	}
}
