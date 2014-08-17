package com.bendude56.goldenapple.punish.command;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;

import com.bendude56.goldenapple.GoldenApple;
import com.bendude56.goldenapple.ModuleLoader.ModuleState;
import com.bendude56.goldenapple.SimpleCommandManager;
import com.bendude56.goldenapple.User;
import com.bendude56.goldenapple.audit.AuditLog;
import com.bendude56.goldenapple.chat.ChatManager;
import com.bendude56.goldenapple.chat.DatabaseChatChannel;
import com.bendude56.goldenapple.chat.IChatChannel;
import com.bendude56.goldenapple.chat.IChatChannel.ChatChannelFeature;
import com.bendude56.goldenapple.command.DualSyntaxCommand;
import com.bendude56.goldenapple.mail.MailManager;
import com.bendude56.goldenapple.permissions.IPermissionUser;
import com.bendude56.goldenapple.permissions.PermissionManager;
import com.bendude56.goldenapple.punish.Punishment.RemainingTime;
import com.bendude56.goldenapple.punish.PunishmentManager;
import com.bendude56.goldenapple.punish.PunishmentMute;
import com.bendude56.goldenapple.punish.audit.MuteEvent;
import com.bendude56.goldenapple.punish.audit.MuteVoidEvent;
import com.bendude56.goldenapple.util.ComplexArgumentParser;
import com.bendude56.goldenapple.util.ComplexArgumentParser.ArgumentInfo;

public class MuteCommand extends DualSyntaxCommand {
    
    @Override
    public void onExecuteComplex(GoldenApple instance, User user, String commandLabel, String[] args) {
        if (GoldenApple.getInstance().getModuleManager().getModule("Chat").getCurrentState() != ModuleState.LOADED) {
            SimpleCommandManager.defaultCommand.onCommand(user.getHandle(), Bukkit.getPluginCommand("gamute"), commandLabel, args);
        } else if (args.length == 0 || args[0].equalsIgnoreCase("-?") || args[0].equalsIgnoreCase("help")) {
            sendHelp(user, commandLabel, true);
        } else {
            ComplexArgumentParser arg = new ComplexArgumentParser(new ArgumentInfo[] {
                ArgumentInfo.newUser("target", "u", "user", true, false),
                ArgumentInfo.newString("duration", "t", "time", false),
                ArgumentInfo.newString("channel", "c", "channel", false),
                ArgumentInfo.newString("reason", "r", "reason", true),
                ArgumentInfo.newSwitch("void", "v", "void"),
                ArgumentInfo.newSwitch("info", "i", "info")
            });
            
            user.sendLocalizedMessage("module.punish.header");
            
            if (!arg.parse(user, args)) {
                return;
            }
            
            if (!arg.isDefined("target")) {
                user.sendLocalizedMessage("module.punish.error.noUserSelected");
                return;
            } else if (!arg.isDefined("channel") && ChatManager.getInstance().getActiveChannel(user) == null) {
                user.sendLocalizedMessage("module.punish.mute.error.noChannel");
                return;
            }
            
            IPermissionUser target = arg.getUser("target");
            IChatChannel c;
            
            if (arg.isDefined("channel")) {
                c = ChatManager.getInstance().getChannel(arg.getString("channel"));
                
                if (c == null) {
                    user.sendLocalizedMessage("module.chat.error.channelNotFound", arg.getString("channel"));
                    return;
                }
            } else {
                c = ChatManager.getInstance().getActiveChannel(user);
            }
            
            if (!c.isFeatureAccessible(user, ChatChannelFeature.MUTE_USER) || !(c instanceof DatabaseChatChannel)) {
                user.sendLocalizedMessage("module.punish.mute.error.notAllowed");
                return;
            }
            
            if (arg.isDefined("info")) {
                muteInfo(target, c, user, commandLabel, args);
            } else if (arg.isDefined("void")) {
                muteVoid(target, c, user, commandLabel, args);
            } else {
                muteAdd(target, c, (arg.isDefined("duration")) ? arg.getString("duration") : null, (arg.isDefined("reason")) ? arg.getString("reason") : null, user, commandLabel, args);
            }
        }
    }
    
    public static void muteInfo(IPermissionUser target, IChatChannel c, User user, String commandLabel, String[] args) {
        if (!c.getAccessLevel(user).isModerator()) {
            GoldenApple.logPermissionFail(user, commandLabel, args, true);
            return;
        }
        
        PunishmentMute m = PunishmentManager.getInstance().getActiveMute(target, c);
        
        if (m == null) {
            user.sendLocalizedMessage("module.punish.mute.info.none", target.getName());
        } else if (m.isPermanent()) {
            user.sendLocalizedMessage("module.punish.mute.info.perm", target.getName(), m.getAdmin().getName());
            user.getHandle().sendMessage(ChatColor.GRAY + m.getReason());
            if (m.isGlobal()) {
                user.sendLocalizedMessage("module.punish.mute.info.global");
            }
        } else {
            user.sendLocalizedMessage("module.punish.mute.info.temp", target.getName(), m.getRemainingDuration().toString(user), m.getAdmin().getName());
            user.getHandle().sendMessage(ChatColor.GRAY + m.getReason());
            if (m.isGlobal()) {
                user.sendLocalizedMessage("module.punish.mute.info.global");
            }
        }
    }
    
    public static void muteVoid(IPermissionUser target, IChatChannel c, User user, String commandLabel, String[] args) {
        if (!c.getAccessLevel(user).isModerator()) {
            GoldenApple.logPermissionFail(user, commandLabel, args, true);
            return;
        }
        
        PunishmentMute m = PunishmentManager.getInstance().getActiveMute(target, c);
        
        if (m == null) {
            user.sendLocalizedMessage("module.punish.mute.error.notMuted");
        } else if (m.isGlobal()) {
            user.sendLocalizedMessage("module.punish.mute.error.voidGlobal");
        } else {
            if (m.getAdminId() != user.getId() && c.getAccessLevel(user).isSuperModerator()) {
                GoldenApple.logPermissionFail(user, commandLabel, args, true);
                return;
            } else {
                m.voidPunishment();
                m.update();
                
                AuditLog.logEvent(new MuteVoidEvent(user.getName(), target.getName(), c.getName()));
                
                if (MailManager.getInstance() != null) {
                    MailManager.getInstance().sendSystemMessage(target, "punish.mute.void", user.getName(), c.getName());
                }
                
                user.sendLocalizedMessage("module.punish.mute.success.void", target.getName());
            }
        }
    }
    
    public static void muteAdd(IPermissionUser target, IChatChannel c, String duration, String reason, User user, String commandLabel, String[] args) {
        if (!c.getAccessLevel(user).isModerator()) {
            GoldenApple.logPermissionFail(user, commandLabel, args, true);
            return;
        }
        
        PunishmentMute m = PunishmentManager.getInstance().getActiveMute(target, c);
        
        if (m == null) {
            try {
                User tUser;
                RemainingTime t = (duration != null) ? RemainingTime.parseTime(duration) : null;
                
                if (c.getAccessLevel(user).isSuperModerator() &&
                    GoldenApple.getInstanceMainConfig().getInt("modules.punish.maxTempChannelMuteTime") > 0 &&
                    t != null && t.getTotalSeconds() > GoldenApple.getInstanceMainConfig().getInt("modules.punish.maxTempChannelMuteTime")) {
                    user.sendLocalizedMessage("module.punish.mute.error.tooLong", new RemainingTime(GoldenApple.getInstanceMainConfig().getInt("modules.punish.maxTempChannelMuteTime")).toString(user));
                } else if (!c.getAccessLevel(user).canPunish(c.getAccessLevel(target))) {
                    GoldenApple.logPermissionFail(user, commandLabel, args, true);
                    return;
                } else {
                    if (reason == null) {
                        reason = (t == null) ? GoldenApple.getInstanceMainConfig().getString("modules.punish.defaultPermaChannelMuteReason", "You have been silenced from this channel.") :
                            GoldenApple.getInstanceMainConfig().getString("modules.punish.defaultTempChannelMuteReason", "You have been temporarily silenced from this channel.");
                    }
                    
                    PunishmentManager.getInstance().addMute(target, user, reason, t, c.getName());
                    AuditLog.logEvent(new MuteEvent(user.getName(), target.getName(), (t == null) ? "PERMANENT" : t.toStringDefault(), reason, c.getName()));
                    
                    if (MailManager.getInstance() != null) {
                        if (t == null) {
                            MailManager.getInstance().sendSystemMessage(target, "punish.mute.perm", user.getName(), c.getName(), reason);
                        } else {
                            MailManager.getInstance().sendSystemMessage(target, "punish.mute.temp", user.getName(), c.getName(), reason, t.toString(target));
                        }
                    }
                    
                    if (t == null) {
                        user.sendLocalizedMessage("module.punish.mute.success.perm", target.getName());
                    } else {
                        user.sendLocalizedMessage("module.punish.mute.success.temp", target.getName(), t.toString(user));
                    }
                    
                    if ((tUser = User.getUser(target.getId())) != null && ChatManager.getInstance().getActiveChannel(tUser) == c) {
                        if (t == null) {
                            tUser.sendLocalizedMessage("module.punish.mute.notify.perm", user.getName());
                            tUser.getHandle().sendMessage(reason);
                        } else {
                            tUser.sendLocalizedMessage("module.punish.mute.notify.temp", user.toString(), t.toString(tUser));
                            tUser.getHandle().sendMessage(reason);
                        }
                    }
                }
            } catch (NumberFormatException e) {
                user.sendLocalizedMessage("module.punish.error.invalidDuration", duration);
            }
        } else {
            user.sendLocalizedMessage("module.punish.mute.error.alreadyMuted");
        }
    }
    
    @Override
    public void onExecuteSimple(GoldenApple instance, User user, String commandLabel, String[] args) {
        if (GoldenApple.getInstance().getModuleManager().getModule("Chat").getCurrentState() != ModuleState.LOADED) {
            SimpleCommandManager.defaultCommand.onCommand(user.getHandle(), Bukkit.getPluginCommand("gamute"), commandLabel, args);
        } else if (args.length == 0 || args[0].equalsIgnoreCase("-?") || args[0].equalsIgnoreCase("help")) {
            sendHelp(user, commandLabel, false);
        } else {
            user.sendLocalizedMessage("module.punish.header");
            
            IPermissionUser target = PermissionManager.getInstance().findUser(args[0], false);
            IChatChannel c;
            
            if (target == null) {
                user.sendLocalizedMessage("shared.parser.userNotFound.error", args[0]);
                return;
            } else if ((c = ChatManager.getInstance().getActiveChannel(user)) == null) {
                user.sendLocalizedMessage("module.chat.error.notInChannel.command");
            }
            
            if (args.length == 1) {
                muteAdd(target, c, null, null, user, commandLabel, args);
            } else if (args[1].equalsIgnoreCase("info")) {
                muteInfo(target, c, user, commandLabel, args);
            } else if (args[1].equalsIgnoreCase("void")) {
                muteVoid(target, c, user, commandLabel, args);
            } else {
                String reason = null;
                
                if (args.length > 2) {
                    reason = args[2];
                    for (int i = 3; i < args.length; i++) {
                        reason += " " + args[i];
                    }
                }
                
                muteAdd(target, c, (args[1].equalsIgnoreCase("permanent") || args[1].equalsIgnoreCase("perm")) ? null : args[1], reason, user, commandLabel, args);
            }
        }
    }
    
    private void sendHelp(User user, String commandLabel, boolean complex) {
        user.sendLocalizedMessage("module.punish.header");
        user.sendLocalizedMessage((complex) ? "module.punish.mute.help.complex" : "module.punish.mute.help.simple", commandLabel);
    }
    
}
