package com.bendude56.goldenapple.commands;

import org.bukkit.Bukkit;

import com.bendude56.goldenapple.SimpleCommandManager;
import com.bendude56.goldenapple.GoldenApple;
import com.bendude56.goldenapple.ModuleLoader.ModuleState;
import com.bendude56.goldenapple.User;
import com.bendude56.goldenapple.chat.ChatChannel;
import com.bendude56.goldenapple.chat.ChatChannel.ChatChannelUserLevel;
import com.bendude56.goldenapple.chat.ChatManager;
import com.bendude56.goldenapple.permissions.IPermissionUser;
import com.bendude56.goldenapple.permissions.PermissionManager;
import com.bendude56.goldenapple.punish.PunishmentManager;
import com.bendude56.goldenapple.punish.PunishmentMute;
import com.bendude56.goldenapple.punish.Punishment.RemainingTime;
import com.bendude56.goldenapple.util.ComplexArgumentParser;
import com.bendude56.goldenapple.util.ComplexArgumentParser.ArgumentInfo;
import com.bendude56.goldenapple.util.ComplexArgumentParser.ArgumentInfo.ArgumentType;

public class MuteCommand extends DualSyntaxCommand {

	@Override
	public void onExecuteComplex(GoldenApple instance, User user, String commandLabel, String[] args) {
		if (GoldenApple.getInstance().getModuleManager().getModule("Chat").getCurrentState() != ModuleState.LOADED) {
			SimpleCommandManager.defaultCommand.onCommand(user.getHandle(), Bukkit.getPluginCommand("gamute"), commandLabel, args);
		} else if (args.length == 0 || args[0].equalsIgnoreCase("-?") || args[0].equalsIgnoreCase("help")) {
			sendHelp(user, commandLabel, true);
		} else {
			ComplexArgumentParser arg = new ComplexArgumentParser(new ArgumentInfo[] {
				new ArgumentInfo("target", "t", "target", ArgumentType.USER, false, true),
				new ArgumentInfo("duration", "d", "duration", ArgumentType.STRING, false, false),
				new ArgumentInfo("channel", "c", "channel", ArgumentType.STRING, false, false),
				new ArgumentInfo("reason", "r", "reason", ArgumentType.STRING_WITH_SPACES, false, false),
				new ArgumentInfo("void", "v", "void", ArgumentType.SWITCH, false, false),
				new ArgumentInfo("info", "i", "info", ArgumentType.SWITCH, false, false)
			});
			
			user.sendLocalizedMessage("header.punish");
			
			if (!arg.parse(user, args)) return;
			
			if (!arg.isDefined("target")) {
				user.sendLocalizedMessage("error.mute.noUserSelected");
				return;
			} else if (!arg.isDefined("channel") && ChatManager.getInstance().getActiveChannel(user) == null) {
				user.sendLocalizedMessage("error.mute.noChannelSelected");
				return;
			}
			
			IPermissionUser target = arg.getUser("target");
			ChatChannel c;
			
			if (arg.isDefined("channel")) {
				 c = ChatManager.getInstance().getChannel(arg.getString("channel"));
				 
				 if (c == null) {
					 user.sendLocalizedMessage("error.channel.notFound", arg.getString("channel"));
					 return;
				 }
			} else {
				c = ChatManager.getInstance().getActiveChannel(user);
			}
			
			if (c.isTemporary()) {
				user.sendLocalizedMessage("error.mute.tempChannel");
				return;
			} else if (c.getActiveLevel(user).id < ChatChannelUserLevel.MODERATOR.id) {
				GoldenApple.logPermissionFail(user, commandLabel, args, true);
				return;
			}
			
			if (arg.isDefined("info")) {
				PunishmentMute m = PunishmentManager.getInstance().getActiveMute(target, c);
				
				if (m == null) {
					user.sendLocalizedMessage("general.mute.info.notMuted", target.getName());
				} else if (m.isPermanent()) {
					user.sendLocalizedMessage("general.mute.info.permMuted", target.getName(), m.getAdmin().getName());
					if (m.isGlobal())
						user.sendLocalizedMessage("general.mute.info.global");
				} else {
					user.sendLocalizedMessage("general.mute.info.tempMuted", target.getName(), m.getRemainingDuration().toString(), m.getAdmin().getName());
					if (m.isGlobal())
						user.sendLocalizedMessage("general.mute.info.global");
				}
			} else if (arg.isDefined("void")) {
				PunishmentMute m = PunishmentManager.getInstance().getActiveMute(target, c);
				
				if (m == null) {
					user.sendLocalizedMessage("error.mute.notMuted");
				} else if (m.isGlobal()) {
					user.sendLocalizedMessage("error.mute.voidGlobal");
				} else {
					m.voidPunishment();
					user.sendLocalizedMessage("general.mute.voidMute", target.getName());
				}
			} else {
				PunishmentMute m = PunishmentManager.getInstance().getActiveMute(target, c);
				
				if (m == null) {
					try {
						User tUser;
						RemainingTime t = (arg.isDefined("duration")) ? RemainingTime.parseTime(arg.getString("duration")) : null;
						
						if (c.calculateLevel(user).id < ChatChannelUserLevel.SUPER_MODERATOR.id &&
								GoldenApple.getInstanceMainConfig().getInt("modules.punish.maxTempChannelMuteTime") > 0 &&
								t != null && t.getTotalSeconds() > GoldenApple.getInstanceMainConfig().getInt("modules.punish.maxTempChannelMuteTime")) {
							user.sendLocalizedMessage("error.mute.tooLong");
						} else {
							String reason = (arg.isDefined("reason")) ? arg.getString("reason") : null;
							
							if (reason == null)
								reason = (t == null) ? GoldenApple.getInstanceMainConfig().getString("modules.punish.defaultPermaChannelMuteReason", "You have been silenced from this channel.") :
									GoldenApple.getInstanceMainConfig().getString("modules.punish.defaultTempChannelMuteReason", "You have been temporarily silenced from this channel.");
							
							PunishmentManager.getInstance().addMute(target, user, reason, t, c.getName());
							
							if (t == null) {
								user.sendLocalizedMessage("general.mute.permaMute", target.getName());
							} else {
								user.sendLocalizedMessage("general.mute.tempMute", target.getName(), t.toString());
							}
							
							if ((tUser = User.getUser(target.getId())) != null && ChatManager.getInstance().getActiveChannel(tUser) == c) {
								if (t == null) {
									user.sendLocalizedMessage("general.mute.permaKick", user.getName());
									user.getHandle().sendMessage(reason);
								} else {
									user.sendLocalizedMessage("general.mute.tempKick", target.getName(), t.toString());
									user.getHandle().sendMessage(reason);
								}
							}
						}
					} catch (NumberFormatException e) {
						user.sendLocalizedMessage("error.mute.invalidDuration", arg.getString("duration"));
					}
				} else {
					user.sendLocalizedMessage("error.mute.alreadyMuted");
				}
			}
		}
	}

	@Override
	public void onExecuteSimple(GoldenApple instance, User user, String commandLabel, String[] args) {
		if (args.length == 0 || args[0].equalsIgnoreCase("-?") || args[0].equalsIgnoreCase("help")) {
			sendHelp(user, commandLabel, false);
		} else {
			ChatChannel c;
			
			user.sendLocalizedMessage("header.punish");
			
			if ((c = ChatManager.getInstance().getActiveChannel(user)) == null) {
				user.sendLocalizedMessage("error.channel.notInChannelCommand");
			} else if (c.getActiveLevel(user).id < ChatChannelUserLevel.MODERATOR.id) {
				
			} else {
				IPermissionUser target = PermissionManager.getInstance().getUser(args[0]);
				User tUser;
				
				if (target == null) {
					user.sendLocalizedMessage("shared.userNotFoundError", args[0]);
				} else {
					String reason = null;
					
					if (args.length > 2) {
						reason = "";
						for (int i = 2; i < args.length; i++) {
							reason += (reason.equals("")) ? args[i] : (" " + args[i]);
						}
					}
					
					if (args.length < 2 || args[1].equalsIgnoreCase("permanent")) {
						if (reason == null) reason = GoldenApple.getInstanceMainConfig().getString("modules.punish.defaultPermaChannelMuteReason", "You have been silenced from this channel.");
						
						user.sendLocalizedMessage("general.mute.permaMute", target.getName());
						
						if ((tUser = User.getUser(target.getId())) != null && ChatManager.getInstance().getActiveChannel(tUser) == c) {
							user.sendLocalizedMessage("general.mute.permaKick", user.getName());
							user.getHandle().sendMessage(reason);
						}
					} else if (args[1].equalsIgnoreCase("void")) {
						
					} else {
						if (reason == null) reason = GoldenApple.getInstanceMainConfig().getString("modules.punish.defaultTempChannelMuteReason", "You have been temporarily silenced from this channel.");
						
						try {
							RemainingTime t = RemainingTime.parseTime(args[1]);
							
							user.sendLocalizedMessage("general.mute.tempMute", target.getName(), t.toString());
							
							if ((tUser = User.getUser(target.getId())) != null && ChatManager.getInstance().getActiveChannel(tUser) == c) {
								user.sendLocalizedMessage("general.mute.tempKick", t.toString(), user.getName());
								user.getHandle().sendMessage(reason);
							}
						} catch (NumberFormatException e) {
							user.sendLocalizedMessage("error.mute.invalidDuration", args[1]);
						}
					}
				}
			}
		}
	}
	
	private void sendHelp(User user, String commandLabel, boolean complex) {
		user.sendLocalizedMessage("header.help");
		user.sendLocalizedMultilineMessage((complex) ? "help.mute.complex" : "help.mute.simple", commandLabel);
	}

}
