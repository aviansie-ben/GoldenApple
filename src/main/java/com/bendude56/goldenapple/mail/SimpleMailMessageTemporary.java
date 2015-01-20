package com.bendude56.goldenapple.mail;

import com.bendude56.goldenapple.User;
import com.bendude56.goldenapple.permissions.IPermissionUser;
import com.bendude56.goldenapple.permissions.PermissionManager;

public class SimpleMailMessageTemporary implements MailMessageEditable {
    private long receiver;
    private long sender;
    
    private String subject;
    private String contents;
    
    public SimpleMailMessageTemporary(IPermissionUser sender) {
        this.receiver = -1;
        this.sender = (sender == null) ? -1 : sender.getId();
        
        this.subject = "";
        this.contents = "";
    }
    
    @Override
    public long getReceiverId() {
        return receiver;
    }
    
    @Override
    public IPermissionUser getReceiver() {
        return PermissionManager.getInstance().getUser(receiver);
    }
    
    @Override
    public long getSenderId() {
        return sender;
    }
    
    @Override
    public IPermissionUser getSender() {
        return PermissionManager.getInstance().getUser(sender);
    }
    
    @Override
    public String getSubject(User user) {
        return subject;
    }
    
    @Override
    public String getContents(User user) {
        return contents;
    }
    
    @Override
    public void setSubject(String subject) {
        this.subject = subject;
    }
    
    @Override
    public void setContents(String contents) {
        this.contents = contents;
    }
    
    @Override
    public void setReceiver(IPermissionUser receiver) {
        this.receiver = receiver.getId();
    }
    
    @Override
    public MailMessageSent send() {
        if (this.receiver == -1) {
            throw new UnsupportedOperationException("Cannot send message with no receiver!");
        } else if (this.subject.isEmpty()) {
            throw new UnsupportedOperationException("Cannot send message with no subject!");
        } else if (this.contents.isEmpty()) {
            throw new UnsupportedOperationException("Cannot send message with no contents!");
        }
        
        return MailManager.getInstance().sendMessage(getReceiver(), getSender(), subject, contents);
    }
    
}
