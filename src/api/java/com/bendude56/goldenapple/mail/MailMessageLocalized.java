package com.bendude56.goldenapple.mail;

public interface MailMessageLocalized extends MailMessageSent {
    public String getContentMessage();
    public String getSubjectMessage();
    
    public String[] getArguments();
}
