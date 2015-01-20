package com.bendude56.goldenapple.mail;

import java.util.Date;

public interface MailMessageSent extends MailMessage {
    public long getId();
    public Date getSentTime();
    
    public MailStatus getStatus();
    public void setStatus(MailStatus status);
    
    public void insert();
    public void delete();
}
