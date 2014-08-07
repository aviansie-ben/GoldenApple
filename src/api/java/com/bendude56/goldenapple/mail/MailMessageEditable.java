package com.bendude56.goldenapple.mail;

import com.bendude56.goldenapple.permissions.IPermissionUser;

public interface MailMessageEditable extends MailMessage {
    public void setSubject(String subject);
    public void setContents(String contents);
    
    public void setReceiver(IPermissionUser receiver);
    
    public MailMessageSent send();
}
