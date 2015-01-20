package com.bendude56.goldenapple.mail;

import java.util.List;

import com.bendude56.goldenapple.permissions.IPermissionUser;
import com.bendude56.goldenapple.permissions.PermissionManager.Permission;
import com.bendude56.goldenapple.permissions.PermissionManager.PermissionNode;

public abstract class MailManager {
    // goldenapple.mail
    public static PermissionNode mailNode;
    public static Permission mailSendPermission;
    public static Permission mailReplyPermission;
    
    protected static MailManager instance;
    
    public static MailManager getInstance() {
        return instance;
    }
    
    public abstract MailMessageSent getMessage(long id);
    public abstract MailMessageSent getByUserSpecificNumber(IPermissionUser receiver, int userNum);
    public abstract List<MailMessageSent> getMessages(IPermissionUser receiver, boolean unreadOnly);
    
    public abstract int getUserSpecificNumber(MailMessageSent message);
    
    public abstract void uncacheMessage(MailMessageSent message);
    
    public abstract MailMessageEditable createTemporaryMessage(IPermissionUser sender, IPermissionUser receiver, String subject);
    
    public abstract MailMessageLocalized sendSystemMessage(IPermissionUser receiver, String localeMessage, String... args);
    public abstract MailMessageSent sendMessage(IPermissionUser receiver, IPermissionUser sender, String subject, String contents);
    
    public abstract void clearCache();
}
