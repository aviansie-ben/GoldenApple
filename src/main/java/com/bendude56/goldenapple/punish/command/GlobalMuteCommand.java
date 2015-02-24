package com.bendude56.goldenapple.punish.command;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;

import com.bendude56.goldenapple.GoldenApple;
import com.bendude56.goldenapple.ModuleLoader.ModuleState;
import com.bendude56.goldenapple.SimpleCommandManager;
import com.bendude56.goldenapple.User;
import com.bendude56.goldenapple.audit.AuditLog;
import com.bendude56.goldenapple.command.DualSyntaxCommand;
import com.bendude56.goldenapple.mail.MailManager;
import com.bendude56.goldenapple.permissions.IPermissionUser;
import com.bendude56.goldenapple.permissions.PermissionManager;
import com.bendude56.goldenapple.punish.Punishment.RemainingTime;
import com.bendude56.goldenapple.punish.PunishmentManager;
import com.bendude56.goldenapple.punish.PunishmentMute;
import com.bendude56.goldenapple.punish.audit.MuteEntry;
import com.bendude56.goldenapple.punish.audit.MuteVoidEntry;
import com.bendude56.goldenapple.util.ComplexArgumentParser;
import com.bendude56.goldenapple.util.ComplexArgumentParser.ArgumentInfo;

public class GlobalMuteCommand extends DualSyntaxCommand {
    
    @Override
    public void onExecuteComplex(GoldenApple instance, User user, String commandLabel, String[] args) {
        if (GoldenApple.getInstance().getModuleManager().getModule("Chat").getCurrentState() != ModuleState.LOADED) {
            SimpleCommandManager.defaultCommand.onCommand(user.getHandle(), Bukkit.getPluginCommand("gaglobalmute"), commandLabel, args);
        } else if (args.length == 0 || args[0].equalsIgnoreCase("-?") || args[0].equalsIgnoreCase("help")) {
            sendHelp(user, commandLabel, true);
        } else {
            ComplexArgumentParser arg = new ComplexArgumentParser(new ArgumentInfo[] {
                ArgumentInfo.newUser("target", "u", "user", true, false),
                ArgumentInfo.newString("duration", "t", "time", false),
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
            }
            
            IPermissionUser target = arg.getUser("target");
            
            if (arg.isDefined("info")) {
                muteInfo(target, user, commandLabel, args);
            } else if (arg.isDefined("void")) {
                muteVoid(target, user, commandLabel, args);
            } else {
                muteAdd(target, (arg.isDefined("duration")) ? arg.getString("duration") : null, (arg.isDefined("reason")) ? arg.getString("reason") : null, user, commandLabel, args);
            }
        }
    }
    
    public static void muteInfo(IPermissionUser target, User user, String commandLabel, String[] args) {
        if (!user.hasPermission(PunishmentManager.globalMuteInfoPermission)) {
            GoldenApple.logPermissionFail(user, commandLabel, args, true);
            return;
        }
        
        PunishmentMute m = PunishmentManager.getInstance().getActiveMute(target, null);
        
        if (m == null) {
            user.sendLocalizedMessage("module.punish.globalMute.info.none", target.getName());
        } else if (m.isPermanent()) {
            user.sendLocalizedMessage("module.punish.globalMute.info.perm", target.getName(), m.getAdmin().getName());
            user.getHandle().sendMessage(ChatColor.GRAY + m.getReason());
        } else {
            user.sendLocalizedMessage("module.punish.globalMute.info.temp", target.getName(), m.getRemainingDuration().toString(user), m.getAdmin().getName());
            user.getHandle().sendMessage(ChatColor.GRAY + m.getReason());
        }
    }
    
    public static void muteVoid(IPermissionUser target, User user, String commandLabel, String[] args) {
        if (!user.hasPermission(PunishmentManager.globalMuteVoidPermission)) {
            GoldenApple.logPermissionFail(user, commandLabel, args, true);
            return;
        }
        
        PunishmentMute m = PunishmentManager.getInstance().getActiveMute(target, null);
        
        if (m == null) {
            user.sendLocalizedMessage("module.punish.globalMute.error.notMuted");
        } else {
            if (m.getAdminId() != user.getId() && !user.hasPermission(PunishmentManager.globalMuteVoidAllPermission)) {
                GoldenApple.logPermissionFail(user, commandLabel, args, true);
                return;
            } else {
                m.voidPunishment();
                m.update();
                
                AuditLog.logEntry(new MuteVoidEntry(user.getLogName(), target.getLogName(), "GLOBAL"));
                
                if (MailManager.getInstance() != null) {
                    MailManager.getInstance().sendSystemMessage(target, "punish.globalMute.void", user.getName());
                }
                
                user.sendLocalizedMessage("module.punish.globalMute.success.void", target.getName());
            }
        }
    }
    
    public static void muteAdd(IPermissionUser target, String duration, String reason, User user, String commandLabel, String[] args) {
        if (!user.hasPermission(PunishmentManager.globalMuteTempPermission)) {
            GoldenApple.logPermissionFail(user, commandLabel, args, true);
            return;
        }
        
        PunishmentMute m = PunishmentManager.getInstance().getActiveMute(target, null);
        
        if (m == null) {
            try {
                User tUser;
                RemainingTime t = (duration != null) ? RemainingTime.parseTime(duration) : null;
                
                if (!user.hasPermission(PunishmentManager.globalMuteTempOverridePermission) &&
                    GoldenApple.getInstanceMainConfig().getInt("modules.punish.maxTempChannelMuteTime") > 0 &&
                    t != null && t.getTotalSeconds() > GoldenApple.getInstanceMainConfig().getInt("modules.punish.maxTempGlobalMuteTime")) {
                    user.sendLocalizedMessage("module.punish.globalMute.error.tooLong", new RemainingTime(GoldenApple.getInstanceMainConfig().getInt("modules.punish.maxTempGlobalMuteTime")).toString(user));
                } else {
                    if (reason == null) {
                        reason = (t == null) ? GoldenApple.getInstanceMainConfig().getString("modules.punish.defaultPermaGlobalMuteReason", "You have been silenced!") :
                            GoldenApple.getInstanceMainConfig().getString("modules.punish.defaultTempGlobalMuteReason", "An administrator has temporarily silenced you!");
                    }
                    
                    PunishmentManager.getInstance().addMute(target, user, reason, t, null);
                    AuditLog.logEntry(new MuteEntry(user.getLogName(), target.getLogName(), (t == null) ? "PERMANENT" : t.toStringDefault(), reason, "GLOBAL"));
                    
                    if (MailManager.getInstance() != null) {
                        if (t == null) {
                            MailManager.getInstance().sendSystemMessage(target, "punish.globalMute.perm", user.getName(), reason);
                        } else {
                            MailManager.getInstance().sendSystemMessage(target, "punish.globalMute.temp", user.getName(), reason, t.toString(target));
                        }
                    }
                    
                    if (t == null) {
                        user.sendLocalizedMessage("module.punish.globalMute.success.perm", target.getName());
                    } else {
                        user.sendLocalizedMessage("module.punish.globalMute.success.temp", target.getName(), t.toString(target));
                    }
                    
                    if ((tUser = User.getUser(target.getId())) != null) {
                        if (t == null) {
                            tUser.sendLocalizedMessage("module.punish.globalMute.notify.perm", user.getName());
                            tUser.getHandle().sendMessage(reason);
                        } else {
                            tUser.sendLocalizedMessage("module.punish.globalMute.notify.temp", user.getName(), t.toString(tUser));
                            tUser.getHandle().sendMessage(reason);
                        }
                    }
                }
            } catch (NumberFormatException e) {
                user.sendLocalizedMessage("module.punish.error.invalidDuration", duration);
            }
        } else {
            user.sendLocalizedMessage("module.punish.globalMute.error.alreadyMuted");
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
            
            if (target == null) {
                user.sendLocalizedMessage("shared.parser.userNotFound.error", args[0]);
                return;
            }
            
            if (args.length == 1) {
                muteAdd(target, null, null, user, commandLabel, args);
            } else if (args[1].equalsIgnoreCase("info")) {
                muteInfo(target, user, commandLabel, args);
            } else if (args[1].equalsIgnoreCase("void")) {
                muteVoid(target, user, commandLabel, args);
            } else {
                String reason = null;
                
                if (args.length > 2) {
                    reason = args[2];
                    for (int i = 3; i < args.length; i++) {
                        reason += " " + args[i];
                    }
                }
                
                muteAdd(target, (args[1].equalsIgnoreCase("permanent") || args[1].equalsIgnoreCase("perm")) ? null : args[1], reason, user, commandLabel, args);
            }
        }
    }
    
    private void sendHelp(User user, String commandLabel, boolean complex) {
        user.sendLocalizedMessage("module.punish.header");
        user.sendLocalizedMessage((complex) ? "module.punish.globalMute.help.complex" : "module.punish.globalMute.help.simple", commandLabel);
    }
    
}
