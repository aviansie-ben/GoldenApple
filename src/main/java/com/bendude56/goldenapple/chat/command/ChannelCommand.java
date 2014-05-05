package com.bendude56.goldenapple.chat.command;

import org.bukkit.ChatColor;

import com.bendude56.goldenapple.GoldenApple;
import com.bendude56.goldenapple.User;
import com.bendude56.goldenapple.chat.ChatChannel;
import com.bendude56.goldenapple.chat.ChatChannel.ChatChannelUserLevel;
import com.bendude56.goldenapple.chat.ChatManager;
import com.bendude56.goldenapple.command.DualSyntaxCommand;
import com.bendude56.goldenapple.command.VerifyCommand;
import com.bendude56.goldenapple.permissions.IPermissionGroup;
import com.bendude56.goldenapple.permissions.IPermissionUser;
import com.bendude56.goldenapple.permissions.PermissionManager;

public class ChannelCommand extends DualSyntaxCommand {
	@Override
	public void onExecuteComplex(GoldenApple instance, User user, String commandLabel, String[] args) {
		if (args.length == 0 || args[0].equals("-?") || args[0].equals("help")) {
			sendHelp(user, commandLabel, true, ChatManager.getInstance().getActiveChannelLevel(user));
		} else if (args[0].equals("-l")) {
			user.sendLocalizedMessage("header.chat");
			if (ChatManager.getInstance().getActiveChannel(user) == null) {
				user.sendLocalizedMessage("error.channel.notInChannelCommand");
			} else {
				ChatManager.getInstance().leaveChannel(user, true);
			}
		} else if (args[0].equals("-k")) {
			ChatChannel c = ChatManager.getInstance().getActiveChannel(user);
			user.sendLocalizedMessage("header.chat");
			if (args.length == 1) {
				user.sendLocalizedMessage("shared.parameterMissing", "-k");
			} else if (c == null) {
				user.sendLocalizedMessage("error.channel.notInChannelCommand");
			} else if (ChatManager.getInstance().getActiveChannelLevel(user).id < ChatChannelUserLevel.MODERATOR.id) {
				GoldenApple.logPermissionFail(user, commandLabel, args, true);
			} else {
				User u = User.findUser(args[1]);
				if (u == null) {
					user.sendLocalizedMessage("shared.userNotFoundError");
				} else if (c.isInChannel(u)) {
					ChatManager.getInstance().kickFromChannel(u);
				} else {
					user.sendLocalizedMessage("error.channel.kick.notInChannel", u.getDisplayName());
				}
			}
		} else if (args[0].equals("--motd")) {
			ChatChannel c = ChatManager.getInstance().getActiveChannel(user);
			user.sendLocalizedMessage("header.chat");
			if (c == null) {
				user.sendLocalizedMessage("error.channel.notInChannelCommand");
			} else if (ChatManager.getInstance().getActiveChannelLevel(user).id < ChatChannelUserLevel.MODERATOR.id) {
				GoldenApple.logPermissionFail(user, commandLabel, args, true);
			} else if (args.length == 1) {
				c.motd = null;
				c.save();
				user.sendLocalizedMessage("general.channel.motd.clear");
			} else {
				c.motd = "";
				for (int i = 1; i < args.length; i++) {
					if (i != 1) c.motd += " ";
					c.motd += args[i];
				}
				c.save();
				user.sendLocalizedMessage("general.channel.motd.set");
			}
		} else if (args[0].startsWith("-lvl:")) {
			ChatChannel c = ChatManager.getInstance().getActiveChannel(user);
			user.sendLocalizedMessage("header.chat");
			if (args.length < 2) {
				user.sendLocalizedMessage("shared.parameterMissing", args[0]);
			} else if (c == null) {
				user.sendLocalizedMessage("error.channel.notInChannelCommand");
			} else if (ChatManager.getInstance().getActiveChannelLevel(user).id < ChatChannelUserLevel.SUPER_MODERATOR.id) {
				GoldenApple.logPermissionFail(user, commandLabel, args, true);
			} else {
				ChatChannelUserLevel l = ChatChannelUserLevel.fromCmdComplex(args[0].substring(5));
				if (args[0].substring(5).equals("r")) {
					IPermissionUser u = PermissionManager.getInstance().getUser(args[1]);
					if (u == null) {
						user.sendLocalizedMessage("shared.userNotFoundError", args[1]);
					} else {
						c.setUserLevel(u.getId(), ChatChannelUserLevel.UNKNOWN);
						user.sendLocalizedMessage("general.channel.lvlRemoveUser", u.getName(), l.display);
					}
				} else if (l == ChatChannelUserLevel.UNKNOWN) {
					user.sendLocalizedMessage("error.channel.invalidLevel", args[0].substring(5));
				} else {
					IPermissionUser u = PermissionManager.getInstance().getUser(args[1]);
					if (u == null) {
						user.sendLocalizedMessage("shared.userNotFoundError", args[1]);
					} else {
						c.setUserLevel(u.getId(), l);
						user.sendLocalizedMessage("general.channel.lvlSetUser", u.getName(), l.display);
					}
				}
			}
		} else if (args[0].startsWith("-glvl:")) {
			ChatChannel c = ChatManager.getInstance().getActiveChannel(user);
			user.sendLocalizedMessage("header.chat");
			if (args.length < 2) {
				user.sendLocalizedMessage("shared.parameterMissing", args[0]);
			} else if (c == null) {
				user.sendLocalizedMessage("error.channel.notInChannelCommand");
			} else if (ChatManager.getInstance().getActiveChannelLevel(user).id < ChatChannelUserLevel.SUPER_MODERATOR.id) {
				GoldenApple.logPermissionFail(user, commandLabel, args, true);
			} else {
				ChatChannelUserLevel l = ChatChannelUserLevel.fromCmdComplex(args[0].substring(6));
				if (args[0].substring(6).equals("r")) {
					IPermissionGroup g = PermissionManager.getInstance().getGroup(args[1]);
					if (g == null) {
						user.sendLocalizedMessage("shared.groupNotFoundError", args[1]);
					} else {
						c.setGroupLevel(g.getId(), ChatChannelUserLevel.UNKNOWN);
						user.sendLocalizedMessage("general.channel.lvlRemoveGroup", g.getName(), l.display);
					}
				} else if (l == ChatChannelUserLevel.UNKNOWN) {
					user.sendLocalizedMessage("error.channel.invalidLevel", args[0].substring(6));
				} else {
					IPermissionGroup g = PermissionManager.getInstance().getGroup(args[1]);
					if (g == null) {
						user.sendLocalizedMessage("shared.groupNotFoundError", args[1]);
					} else {
						c.setGroupLevel(g.getId(), l);
						user.sendLocalizedMessage("general.channel.lvlSetGroup", g.getName(), l.display);
					}
				}
			}
		} else if (args[0].startsWith("-dlvl:")) {
			ChatChannel c = ChatManager.getInstance().getActiveChannel(user);
			user.sendLocalizedMessage("header.chat");
			if (c == null) {
				user.sendLocalizedMessage("error.channel.notInChannelCommand");
			} else if (ChatManager.getInstance().getActiveChannelLevel(user).id < ChatChannelUserLevel.SUPER_MODERATOR.id) {
				GoldenApple.logPermissionFail(user, commandLabel, args, true);
			} else {
				ChatChannelUserLevel l = ChatChannelUserLevel.fromCmdComplex(args[0].substring(6));
				if (l == ChatChannelUserLevel.UNKNOWN) {
					user.sendLocalizedMessage("error.channel.invalidLevel", args[0].substring(6));
				} else if (l.id >= ChatChannelUserLevel.VIP.id) {
					user.sendLocalizedMessage("error.channel.defaultLevelTooHigh");
				} else {
					c.setDefaultLevel(l);
					user.sendLocalizedMessage("general.channel.lvlSetDefault", l.display);
				}
			}
		} else if (args[0].equals("-d")) {
			ChatChannel c = ChatManager.getInstance().getActiveChannel(user);
			user.sendLocalizedMessage("header.chat");
			if (c == null) {
				user.sendLocalizedMessage("error.channel.notInChannelCommand");
			} else if (ChatManager.getInstance().getActiveChannelLevel(user).id < ChatChannelUserLevel.ADMINISTRATOR.id) {
				GoldenApple.logPermissionFail(user, commandLabel, args, true);
			} else {
				if (args.length == 2 && args[1].equals("-v")) {
					ChatManager.getInstance().deleteChannel(c.getName());
				} else {
					VerifyCommand.commands.put(user, "gachannel -d -v");
					user.sendLocalizedMessage("general.channel.deleteWarn");
				}
			}
		} else if (args[0].equals("--strict")) {
			ChatChannel c = ChatManager.getInstance().getActiveChannel(user);
			user.sendLocalizedMessage("header.chat");
			if (c == null) {
				user.sendLocalizedMessage("error.channel.notInChannelCommand");
			} else if (ChatManager.getInstance().getActiveChannelLevel(user).id < ChatChannelUserLevel.ADMINISTRATOR.id) {
				GoldenApple.logPermissionFail(user, commandLabel, args, true);
			} else {
				if (c.isStrictCensorOn()) {
					c.setStrictCensorOn(false);
					c.save();
					user.sendLocalizedMessage("general.channel.strictOff");
				} else {
					c.setStrictCensorOn(true);
					c.save();
					user.sendLocalizedMessage("general.channel.strictOn");
				}
			}
		} else if (args[0].equals("-j")) {
			user.sendLocalizedMessage("header.chat");
			if (args.length == 1) {
				user.sendLocalizedMessage("shared.parameterMissing", "-j");
			} else {
				ChatChannel c = ChatManager.getInstance().getChannel(args[1]);
				if (c == null) {
					user.sendLocalizedMessage("error.channel.notFound", args[1]);
				} else {
					ChatManager.getInstance().tryJoinChannel(user, c, true);
				}
			}
		} else if (args[0].equals("--list")) {
			user.sendLocalizedMessage("header.chat");
			user.sendLocalizedMessage("general.channel.list");
			for (ChatChannel c : ChatManager.getInstance().getActiveChannels()) {
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
		} else if (args[0].equals("-a")) {
			if (!user.hasPermission(ChatManager.channelAddPermission)) {
				GoldenApple.logPermissionFail(user, commandLabel, args, true);
				return;
			}
			
			user.sendLocalizedMessage("header.chat");
			if (args.length < 2) {
				user.sendLocalizedMessage("shared.parameterMissing", "-a");
			} else {
				if (ChatManager.getInstance().channelExists(args[1])) {
					user.sendLocalizedMessage("error.channel.alreadyExists");
				} else {
					ChatChannel c = ChatManager.getInstance().createChannel(args[1]);
					ChatManager.getInstance().tryJoinChannel(user, c, true);
				}
			}
		}
	}

	@Override
	public void onExecuteSimple(GoldenApple instance, User user, String commandLabel, String[] args) {
		if (args.length == 0 || args[0].equals("-?") || args[0].equals("help")) {
			sendHelp(user, commandLabel, false, ChatManager.getInstance().getActiveChannelLevel(user));
		} else if (args[0].equals("leave")) {
			user.sendLocalizedMessage("header.chat");
			if (ChatManager.getInstance().getActiveChannel(user) == null) {
				user.sendLocalizedMessage("error.channel.notInChannelCommand");
			} else {
				ChatManager.getInstance().leaveChannel(user, true);
			}
		} else if (args[0].equals("kick")) {
			ChatChannel c = ChatManager.getInstance().getActiveChannel(user);
			user.sendLocalizedMessage("header.chat");
			if (args.length == 1) {
				user.sendLocalizedMessage("shared.parameterMissing", "kick");
			} else if (c == null) {
				user.sendLocalizedMessage("error.channel.notInChannelCommand");
			} else if (ChatManager.getInstance().getActiveChannelLevel(user).id < ChatChannelUserLevel.MODERATOR.id) {
				GoldenApple.logPermissionFail(user, commandLabel, args, true);
			} else {
				User u = User.findUser(args[1]);
				if (u == null) {
					user.sendLocalizedMessage("shared.userNotFoundError");
				} else if (c.isInChannel(u)) {
					ChatManager.getInstance().kickFromChannel(u);
				} else {
					user.sendLocalizedMessage("error.channel.kick.notInChannel", u.getDisplayName());
				}
			}
		} else if (args[0].equals("motd")) {
			ChatChannel c = ChatManager.getInstance().getActiveChannel(user);
			user.sendLocalizedMessage("header.chat");
			if (c == null) {
				user.sendLocalizedMessage("error.channel.notInChannelCommand");
			} else if (ChatManager.getInstance().getActiveChannelLevel(user).id < ChatChannelUserLevel.MODERATOR.id) {
				GoldenApple.logPermissionFail(user, commandLabel, args, true);
			} else if (args.length == 1) {
				c.motd = null;
				c.save();
				user.sendLocalizedMessage("general.channel.motd.clear");
			} else {
				c.motd = "";
				for (int i = 1; i < args.length; i++) {
					if (i != 1) c.motd += " ";
					c.motd += args[i];
				}
				c.save();
				user.sendLocalizedMessage("general.channel.motd.set");
			}
		} else if (args[0].startsWith("level")) {
			ChatChannel c = ChatManager.getInstance().getActiveChannel(user);
			user.sendLocalizedMessage("header.chat");
			if (args.length < 3) {
				user.sendLocalizedMessage("shared.parameterMissing", "level");
			} else if (c == null) {
				user.sendLocalizedMessage("error.channel.notInChannelCommand");
			} else if (ChatManager.getInstance().getActiveChannelLevel(user).id < ChatChannelUserLevel.SUPER_MODERATOR.id) {
				GoldenApple.logPermissionFail(user, commandLabel, args, true);
			} else {
				ChatChannelUserLevel l = ChatChannelUserLevel.fromCmdSimple(args[1]);
				if (args[1].equals("remove")) {
					IPermissionUser u = PermissionManager.getInstance().getUser(args[2]);
					if (u == null) {
						user.sendLocalizedMessage("shared.userNotFoundError", args[2]);
					} else {
						c.setUserLevel(u.getId(), ChatChannelUserLevel.UNKNOWN);
						user.sendLocalizedMessage("general.channel.lvlRemoveUser", u.getName(), l.display);
					}
				} else if (l == ChatChannelUserLevel.UNKNOWN) {
					user.sendLocalizedMessage("error.channel.invalidLevel", args[1]);
				} else {
					if (args[2].equals("default")) {
						if (l.id >= ChatChannelUserLevel.VIP.id) {
							user.sendLocalizedMessage("error.channel.defaultLevelTooHigh");
						} else {
							c.setDefaultLevel(l);
							user.sendLocalizedMessage("general.channel.lvlSetDefault", l.display);
						}
					} else {
						IPermissionUser u = PermissionManager.getInstance().getUser(args[2]);
						if (u == null) {
							user.sendLocalizedMessage("shared.userNotFoundError", args[2]);
						} else {
							c.setUserLevel(u.getId(), l);
							user.sendLocalizedMessage("general.channel.lvlSetUser", u.getName(), l.display);
						}
					}
				}
			}
		} else if (args[0].equals("delete")) {
			ChatChannel c = ChatManager.getInstance().getActiveChannel(user);
			user.sendLocalizedMessage("header.chat");
			if (c == null) {
				user.sendLocalizedMessage("error.channel.notInChannelCommand");
			} else if (ChatManager.getInstance().getActiveChannelLevel(user).id < ChatChannelUserLevel.ADMINISTRATOR.id) {
				GoldenApple.logPermissionFail(user, commandLabel, args, true);
			} else {
				if (args.length == 2 && args[1].equals("-v")) {
					ChatManager.getInstance().deleteChannel(c.getName());
				} else {
					VerifyCommand.commands.put(user, "gachannel delete -v");
					user.sendLocalizedMessage("general.channel.deleteWarn");
				}
			}
		} else if (args[0].equals("strict")) {
			ChatChannel c = ChatManager.getInstance().getActiveChannel(user);
			user.sendLocalizedMessage("header.chat");
			if (c == null) {
				user.sendLocalizedMessage("error.channel.notInChannelCommand");
			} else if (ChatManager.getInstance().getActiveChannelLevel(user).id < ChatChannelUserLevel.ADMINISTRATOR.id) {
				GoldenApple.logPermissionFail(user, commandLabel, args, true);
			} else {
				if (c.isStrictCensorOn()) {
					c.setStrictCensorOn(false);
					c.save();
					user.sendLocalizedMessage("general.channel.strictOff");
				} else {
					c.setStrictCensorOn(true);
					c.save();
					user.sendLocalizedMessage("general.channel.strictOn");
				}
			}
		} else if (args[0].equals("join")) {
			user.sendLocalizedMessage("header.chat");
			if (args.length == 1) {
				user.sendLocalizedMessage("shared.parameterMissing", "-j");
			} else {
				ChatChannel c = ChatManager.getInstance().getChannel(args[1]);
				if (c == null) {
					user.sendLocalizedMessage("error.channel.notFound", args[1]);
				} else {
					ChatManager.getInstance().tryJoinChannel(user, c, true);
				}
			}
		} else if (args[0].equals("list")) {
			user.sendLocalizedMessage("header.chat");
			user.sendLocalizedMessage("general.channel.list");
			for (ChatChannel c : ChatManager.getInstance().getActiveChannels()) {
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
		} else if (args[0].equals("add")) {
			if (!user.hasPermission(ChatManager.channelAddPermission)) {
				GoldenApple.logPermissionFail(user, commandLabel, args, true);
				return;
			}
			
			user.sendLocalizedMessage("header.chat");
			if (args.length < 2) {
				user.sendLocalizedMessage("shared.parameterMissing", "-a");
			} else {
				if (ChatManager.getInstance().channelExists(args[1])) {
					user.sendLocalizedMessage("error.channel.alreadyExists");
				} else {
					ChatChannel c = ChatManager.getInstance().createChannel(args[1]);
					ChatManager.getInstance().tryJoinChannel(user, c, true);
				}
			}
		}
	}

	private void sendHelp(User user, String commandLabel, boolean complex, ChatChannelUserLevel level) {
		user.sendLocalizedMessage("header.help");
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
		user.sendLocalizedMultilineMessage((complex) ? "help.channel." + section + ".complex" : "help.channel." + section + ".simple", commandLabel);
	}
}
