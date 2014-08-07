package com.bendude56.goldenapple.mail;

import com.bendude56.goldenapple.User;
import com.bendude56.goldenapple.permissions.IPermissionUser;

public interface MailMessage {
    public long getReceiverId();
    public IPermissionUser getReceiver();
    
    public long getSenderId();
    public IPermissionUser getSender();
    
    public String getSubject(User user);
    public String getContents(User user);
    
    public enum MailStatus {
        UNREAD("u"), READ("r"), REPLIED("R");
        
        public String identifier;
        
        private MailStatus(String identifier) {
            this.identifier = identifier;
        }
        
        public static MailStatus fromIdentifier(String identifier) {
            for (MailStatus s : MailStatus.values()) {
                if (s.identifier.equals(identifier)) {
                    return s;
                }
            }
            
            return MailStatus.UNREAD;
        }
    }
}
