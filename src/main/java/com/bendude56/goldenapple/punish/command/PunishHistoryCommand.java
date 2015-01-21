package com.bendude56.goldenapple.punish.command;

import java.text.SimpleDateFormat;
import java.util.List;

import com.bendude56.goldenapple.GoldenApple;
import com.bendude56.goldenapple.User;
import com.bendude56.goldenapple.command.GoldenAppleCommand;
import com.bendude56.goldenapple.permissions.IPermissionUser;
import com.bendude56.goldenapple.permissions.PermissionManager;
import com.bendude56.goldenapple.punish.Punishment;
import com.bendude56.goldenapple.punish.PunishmentBan;
import com.bendude56.goldenapple.punish.PunishmentManager;
import com.bendude56.goldenapple.punish.PunishmentMute;

public class PunishHistoryCommand extends GoldenAppleCommand {
    public static final int PUNISHMENTS_PER_PAGE = 10;
    
    @Override
    public boolean onExecute(GoldenApple instance, User user, String commandLabel, String[] args) {
        if (args.length < 1) {
            return false;
        }
        
        if (!user.hasPermission(PunishmentManager.historyPermission)) {
            GoldenApple.logPermissionFail(user, commandLabel, args, true);
            return true;
        }
        
        user.sendLocalizedMessage("module.punish.header");
        
        IPermissionUser target = PermissionManager.getInstance().findUser(args[0], true);
        
        if (target == null) {
            user.sendLocalizedMessage("shared.parser.userNotFound.error", args[0]);
        } else if (args.length == 1 || args[1].equalsIgnoreCase("list")) {
            List<Punishment> punishments = PunishmentManager.getInstance().getPunishments(target, Punishment.class);
            
            if (punishments.size() > 0) {
                int maxPage = (int) Math.ceil(punishments.size() / (double)PUNISHMENTS_PER_PAGE);
                int page = 1;
                
                if (args.length > 2) {
                    try {
                        page = Integer.parseInt(args[2]);
                    } catch (NumberFormatException e) {
                        user.sendLocalizedMessage("shared.convertError.number", args[2]);
                        return true;
                    }
                }
                
                if (page <= maxPage) {
                    user.sendLocalizedMessage("module.punish.history.list.header", target.getName(), page, maxPage);
                    
                    for (int i = PUNISHMENTS_PER_PAGE * (page - 1); i < PUNISHMENTS_PER_PAGE * page && i < punishments.size(); i++) {
                        Punishment p = punishments.get(i);
                        String time = new SimpleDateFormat(GoldenApple.getInstance().getLocalizationManager().getLocale(user).getRawMessage("shared.format.dateTime")).format(p.getStartTime());
                        
                        if (p instanceof PunishmentBan) {
                            if (p.isPermanent()) {
                                user.sendLocalizedMessage("module.punish.history.list.entry.ban.permanent", punishments.size() - i, time, p.getReason());
                            } else {
                                user.sendLocalizedMessage("module.punish.history.list.entry.ban.temporary", punishments.size() - i, time, p.getReason(), p.getDuration().toString(user));
                            }
                        } else if (p instanceof PunishmentMute) {
                            PunishmentMute m = (PunishmentMute) p;
                            
                            if (m.isGlobal()) {
                                if (p.isPermanent()) {
                                    user.sendLocalizedMessage("module.punish.history.list.entry.globalMute.permanent", punishments.size() - i, time, p.getReason());
                                } else {
                                    user.sendLocalizedMessage("module.punish.history.list.entry.globalMute.temporary", punishments.size() - i, time, p.getReason(), p.getDuration().toString(user));
                                }
                            } else {
                                if (p.isPermanent()) {
                                    user.sendLocalizedMessage("module.punish.history.list.entry.mute.permanent", punishments.size() - i, time, m.getChannelIdentifier(), p.getReason());
                                } else {
                                    user.sendLocalizedMessage("module.punish.history.list.entry.mute.temporary", punishments.size() - i, time, m.getChannelIdentifier(), p.getReason(), p.getDuration().toString(user));
                                }
                            }
                        } else {
                            if (p.isPermanent()) {
                                user.sendLocalizedMessage("module.punish.history.list.entry.unknown.permanent", punishments.size() - i, time, p.getClass().getName(), p.getReason());
                            } else {
                                user.sendLocalizedMessage("module.punish.history.list.entry.unknown.temporary", punishments.size() - i, time, p.getClass().getName(), p.getReason(), p.getDuration().toString(user));
                            }
                        }
                        
                        if (p.isVoided()) {
                            user.sendLocalizedMessage("module.punish.history.list.voided");
                        }
                    }
                } else {
                    user.sendLocalizedMessage("module.punish.history.error.invalidPage", page, maxPage);
                }
            } else {
                user.sendLocalizedMessage("module.punish.history.list.empty", target.getName());
            }
        } else if (args[1].equalsIgnoreCase("info")) {
            int i;
            
            List<Punishment> punishments = PunishmentManager.getInstance().getPunishments(target, Punishment.class);
            
            if (args.length < 3) {
                user.sendLocalizedMessage("shared.paraser.parameterMissing", "info");
                return true;
            } else {
                try {
                    i = Integer.parseInt(args[2]);
                } catch (NumberFormatException e) {
                    user.sendLocalizedMessage("shared.convertError.number", args[2]);
                    return true;
                }
            }
            
            if (i > punishments.size() || i <= 0) {
                user.sendLocalizedMessage("module.punish.history.error.invalidEntry", punishments.size());
                return true;
            }
            
            Punishment p = punishments.get(punishments.size() - i);
            
            if (p instanceof PunishmentBan) {
                user.sendLocalizedMessage("module.punish.history.info.type.ban");
            } else if (p instanceof PunishmentMute) {
                PunishmentMute m = (PunishmentMute) p;
                
                if (m.isGlobal()) {
                    user.sendLocalizedMessage("module.punish.history.info.type.globalMute");
                } else {
                    user.sendLocalizedMessage("module.punish.history.info.type.mute", m.getChannelIdentifier());
                }
            } else {
                user.sendLocalizedMessage("module.punish.history.info.type.unknown", p.getClass().getName());
            }
            
            user.sendLocalizedMessage("module.punish.history.info.target", p.getTarget().getName());
            user.sendLocalizedMessage("module.punish.history.info.admin", p.getAdmin().getName());
            user.sendLocalizedMessage("module.punish.history.info.time", new SimpleDateFormat(GoldenApple.getInstance().getLocalizationManager().getLocale(user).getRawMessage("shared.format.dateTime")).format(p.getStartTime()));
            
            if (p.isPermanent()) {
                user.sendLocalizedMessage("module.punish.history.info.duration.permanent");
            } else {
                user.sendLocalizedMessage("module.punish.history.info.duration.temporary", p.getDuration().toString(user));
            }
            
            user.sendLocalizedMessage((p.isVoided()) ? "module.punish.history.info.voided.yes" : "module.punish.history.info.voided.no");
            user.sendLocalizedMessage("module.punish.history.info.reason", p.getReason());
        } else if (args[1].equalsIgnoreCase("purge")) {
            // TODO Implement this
        } else {
            user.sendLocalizedMessage("shared.parser.unknownOption", args[0]);
        }
        
        return true;
    }
    
}
