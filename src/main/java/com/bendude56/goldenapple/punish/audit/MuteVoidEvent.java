package com.bendude56.goldenapple.punish.audit;

import java.util.HashMap;

public class MuteVoidEvent extends PunishEvent {
    public String channel;
    
    public MuteVoidEvent(String authorizingUser, String target, String channel) {
        super(303, AuditEventLevel.WARNING, authorizingUser, target);
        
        this.channel = channel;
    }
    
    @Override
    protected void loadMetadata(HashMap<String, AuditMetadata> metadata) {
        super.loadMetadata(metadata);
        
        this.channel = metadata.get("channel").valueString;
    }
    
    @Override
    protected HashMap<String, AuditMetadata> saveMetadata() {
        HashMap<String, AuditMetadata> metadata = super.saveMetadata();
        
        metadata.put("channel", createMetadata("channel", channel));
        
        return metadata;
    }
    
    @Override
    public String formatMessage() {
        return this.authorizingUser + " has voided the mute on " + this.target + " from " + channel;
    }
    
}
