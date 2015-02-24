package com.bendude56.goldenapple.punish.command;

import org.bukkit.ChatColor;

import com.bendude56.goldenapple.GoldenApple;
import com.bendude56.goldenapple.User;
import com.bendude56.goldenapple.audit.AuditLog;
import com.bendude56.goldenapple.command.DualSyntaxCommand;
import com.bendude56.goldenapple.command.VerifyCommand;
import com.bendude56.goldenapple.mail.MailManager;
import com.bendude56.goldenapple.permissions.IPermissionUser;
import com.bendude56.goldenapple.permissions.PermissionManager;
import com.bendude56.goldenapple.punish.Punishment.RemainingTime;
import com.bendude56.goldenapple.punish.PunishmentBan;
import com.bendude56.goldenapple.punish.PunishmentManager;
import com.bendude56.goldenapple.punish.audit.BanEntry;
import com.bendude56.goldenapple.punish.audit.BanVoidEntry;
import com.bendude56.goldenapple.util.ComplexArgumentParser;
import com.bendude56.goldenapple.util.ComplexArgumentParser.ArgumentInfo;

public class BanCommand extends DualSyntaxCommand {
    
    @Override
    public void onExecuteComplex(GoldenApple instance, User user, String commandLabel, String[] args) {
        if (args.length == 0 || args[0].equalsIgnoreCase("-?") || args[0].equalsIgnoreCase("help")) {
            sendHelp(user, commandLabel, true);
        } else {
            ComplexArgumentParser arg = new ComplexArgumentParser(new ArgumentInfo[] {
                ArgumentInfo.newUser("target", "u", "user", true, false),
                ArgumentInfo.newString("duration", "t", "time", false),
                ArgumentInfo.newString("reason", "r", "reason", true),
                ArgumentInfo.newSwitch("void", "v", "void"),
                ArgumentInfo.newSwitch("info", "i", "info"),
                ArgumentInfo.newSwitch("verify", null, "verify")
            });
            
            user.sendLocalizedMessage("module.punish.header");
            
            if (!arg.parse(user, args)) {
                return;
            }
            
            IPermissionUser target = arg.getUser("target");
            
            if (arg.isDefined("info")) {
                banInfo(target, user, commandLabel, args);
            } else if (arg.isDefined("void")) {
                banVoid(target, user, commandLabel, args, arg.isDefined("verify"));
            } else {
                banAdd(target, (arg.isDefined("duration")) ? arg.getString("duration") : null, (arg.isDefined("reason")) ? arg.getString("reason") : null, user, commandLabel, args);
            }
        }
    }
    
    public static void banInfo(IPermissionUser target, User user, String commandLabel, String[] args) {
        if (!user.hasPermission(PunishmentManager.banInfoPermission)) {
            GoldenApple.logPermissionFail(user, commandLabel, args, true);
            return;
        }
        
        PunishmentBan b = (PunishmentBan) PunishmentManager.getInstance().getActivePunishment(target, PunishmentBan.class);
        
        if (b == null) {
            user.sendLocalizedMessage("module.punish.ban.info.none", target.getName());
        } else if (b.isPermanent()) {
            user.sendLocalizedMessage("module.punish.ban.info.perm", target.getName(), b.getAdmin().getName());
            user.getHandle().sendMessage(ChatColor.GRAY + b.getReason());
        } else {
            user.sendLocalizedMessage("module.punish.ban.info.temp", target.getName(), b.getRemainingDuration().toString(user), b.getAdmin().getName());
            user.getHandle().sendMessage(ChatColor.GRAY + b.getReason());
        }
    }
    
    public static void banVoid(IPermissionUser target, User user, String commandLabel, String[] args, boolean verified) {
        if (!user.hasPermission(PunishmentManager.banVoidPermission)) {
            GoldenApple.logPermissionFail(user, commandLabel, args, true);
            return;
        }
        
        PunishmentBan b = (PunishmentBan) PunishmentManager.getInstance().getActivePunishment(target, PunishmentBan.class);
        
        if (b == null) {
            user.sendLocalizedMessage("module.punish.ban.error.notBanned");
        } else {
            if (b.getAdminId() != user.getId() && (!verified || !user.hasPermission(PunishmentManager.banVoidAllPermission))) {
                if (!user.hasPermission(PunishmentManager.banVoidAllPermission)) {
                    GoldenApple.logPermissionFail(user, commandLabel, args, true);
                } else {
                    user.sendLocalizedMessage("module.punish.ban.voidWarning", PermissionManager.getInstance().getUser(b.getAdminId()).getName());
                    
                    String cmd = commandLabel;
                    for (String a : args) {
                        cmd += " " + a;
                    }
                    cmd += " --verify";
                    VerifyCommand.commands.put(user, cmd);
                }
            } else {
                b.voidPunishment();
                b.update();
                
                AuditLog.logEntry(new BanVoidEntry(user.getLogName(), target.getLogName()));
                
                if (MailManager.getInstance() != null) {
                    MailManager.getInstance().sendSystemMessage(target, "punish.ban.void", user.getName());
                }
                
                user.sendLocalizedMessage("module.punish.ban.success.void", target.getName());
            }
        }
    }
    
    public static void banAdd(IPermissionUser target, String duration, String reason, User user, String commandLabel, String[] args) {
        if (!user.hasPermission(PunishmentManager.banTempPermission)) {
            GoldenApple.logPermissionFail(user, commandLabel, args, true);
            return;
        }
        
        PunishmentBan b = (PunishmentBan) PunishmentManager.getInstance().getActivePunishment(target, PunishmentBan.class);
        
        if (b == null) {
            try {
                User tUser;
                RemainingTime t = (duration != null) ? RemainingTime.parseTime(duration) : null;
                
                if (!user.hasPermission(PunishmentManager.banTempOverridePermission) &&
                    GoldenApple.getInstanceMainConfig().getInt("modules.punish.maxTempBanTime") > 0 &&
                    t != null && t.getTotalSeconds() > GoldenApple.getInstanceMainConfig().getInt("modules.punish.maxTempBanTime")) {
                    user.sendLocalizedMessage("module.punish.ban.error.tooLong", new RemainingTime(GoldenApple.getInstanceMainConfig().getInt("modules.punish.maxTempBanTime")).toString(user));
                } else if (!user.hasPermission(PunishmentManager.banPermPermission) && t == null) {
                    GoldenApple.logPermissionFail(user, commandLabel, args, true);
                } else {
                    if (reason == null) {
                        reason = (t == null) ? GoldenApple.getInstanceMainConfig().getString("modules.punish.defaultPermaBanReason", "You have been banished from this server!") :
                            GoldenApple.getInstanceMainConfig().getString("modules.punish.defaultTempBanReason", "You have been temporarily banished from this server!");
                    }
                    
                    PunishmentManager.getInstance().addBan(target, user, reason, t);
                    AuditLog.logEntry(new BanEntry(user.getLogName(), target.getLogName(), (t == null) ? "PERMANENT" : t.toStringDefault(), reason));
                    
                    if (MailManager.getInstance() != null) {
                        if (t == null) {
                            MailManager.getInstance().sendSystemMessage(target, "punish.ban.perm", user.getName(), reason);
                        } else {
                            MailManager.getInstance().sendSystemMessage(target, "punish.ban.temp", user.getName(), reason, t.toString(user));
                        }
                    }
                    
                    if (t == null) {
                        user.sendLocalizedMessage("module.punish.ban.success.perm", target.getName());
                    } else {
                        user.sendLocalizedMessage("module.punish.ban.success.temp", target.getName(), t.toString(user));
                    }
                    
                    if ((tUser = User.getUser(target.getId())) != null) {
                        if (t == null) {
                            tUser.getPlayerHandle().kickPlayer(tUser.getLocalizedMessage("module.punish.ban.kick.perm", user.getName()) +
                                "\n" + reason +
                                "\n" + GoldenApple.getInstanceMainConfig().getString("banAppealMessage", "Contact an administrator to dispute this ban."));
                        } else {
                            tUser.getPlayerHandle().kickPlayer(tUser.getLocalizedMessage("module.punish.ban.kick.temp", t.toString(tUser), user.getName()) +
                                "\n" + reason +
                                "\n" + GoldenApple.getInstanceMainConfig().getString("banAppealMessage", "Contact an administrator to dispute this ban."));
                        }
                    }
                }
            } catch (NumberFormatException e) {
                user.sendLocalizedMessage("module.punish.error.invalidDuration", duration);
            }
        } else {
            user.sendLocalizedMessage("module.punish.ban.error.alreadyBanned");
        }
    }
    
    @Override
    public void onExecuteSimple(GoldenApple instance, User user, String commandLabel, String[] args) {
        if (args.length == 0 || args[0].equalsIgnoreCase("-?") || args[0].equalsIgnoreCase("help")) {
            sendHelp(user, commandLabel, false);
        } else {
            user.sendLocalizedMessage("module.punish.header");
            
            IPermissionUser target = PermissionManager.getInstance().findUser(args[0], false);
            
            if (target == null) {
                user.sendLocalizedMessage("shared.parser.userNotFound.error", args[0]);
                return;
            }
            
            if (args.length == 1) {
                banAdd(target, null, null, user, commandLabel, args);
            } else if (args[1].equalsIgnoreCase("info")) {
                banInfo(target, user, commandLabel, args);
            } else if (args[1].equalsIgnoreCase("void")) {
                banVoid(target, user, commandLabel, args, args.length >= 3 && args[2].equalsIgnoreCase("--verify"));
            } else {
                String reason = null;
                
                if (args.length > 2) {
                    reason = args[2];
                    for (int i = 3; i < args.length; i++) {
                        reason += " " + args[i];
                    }
                }
                
                banAdd(target, (args[1].equalsIgnoreCase("permanent") || args[1].equalsIgnoreCase("perm")) ? null : args[1], reason, user, commandLabel, args);
            }
        }
    }
    
    private void sendHelp(User user, String commandLabel, boolean complex) {
        user.sendLocalizedMessage("module.punish.header");
        user.sendLocalizedMessage((complex) ? "module.punish.ban.help.complex" : "module.punish.ban.help.simple", commandLabel);
    }
    
}
