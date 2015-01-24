package com.bendude56.goldenapple.punish.command;

import com.bendude56.goldenapple.GoldenApple;
import com.bendude56.goldenapple.User;
import com.bendude56.goldenapple.audit.AuditLog;
import com.bendude56.goldenapple.command.GoldenAppleCommand;
import com.bendude56.goldenapple.mail.MailManager;
import com.bendude56.goldenapple.permissions.IPermissionUser;
import com.bendude56.goldenapple.permissions.PermissionManager;
import com.bendude56.goldenapple.punish.PunishmentManager;
import com.bendude56.goldenapple.punish.audit.WarnEntry;

public class WarnCommand extends GoldenAppleCommand {
    
    @Override
    public boolean onExecute(GoldenApple instance, User user, String commandLabel, String[] args) {
        if (args.length < 2) {
            return false;
        }
        
        if (!user.hasPermission(PunishmentManager.warnPermission)) {
            GoldenApple.logPermissionFail(user, commandLabel, args, true);
            return true;
        }
        
        user.sendLocalizedMessage("module.punish.header");
        
        IPermissionUser target = PermissionManager.getInstance().findUser(args[0], false);
        
        if (target == null) {
            user.sendLocalizedMessage("shared.parser.userNotFound.error", args[0]);
        } else {
            String reason = args[1];
            
            for (int i = 2; i < args.length; i++) {
                reason += " " + args[i];
            }
            
            PunishmentManager.getInstance().addWarning(target, user, reason);
            AuditLog.logEntry(new WarnEntry(user.getName(), target.getName(), reason));
            
            if (MailManager.getInstance() != null) {
                MailManager.getInstance().sendSystemMessage(target, "punish.warn", user.getName(), reason);
            }
            
            user.sendLocalizedMessage("module.punish.warn.success", target.getName());
        }
        
        return true;
    }
    
}
