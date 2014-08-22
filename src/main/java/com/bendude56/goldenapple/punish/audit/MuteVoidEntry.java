package com.bendude56.goldenapple.punish.audit;

import java.util.Map;

public class MuteVoidEntry extends PunishEntry {
    public String channel;
    
    public MuteVoidEntry() {
        super(303, AuditEntryLevel.WARNING);
    }
    
    public MuteVoidEntry(String authorizingUser, String target, String channel) {
        super(303, AuditEntryLevel.WARNING, authorizingUser, target);
        
        this.channel = channel;
    }
    
    @Override
    protected void loadMetadata(Map<String, AuditMetadata> metadata) {
        super.loadMetadata(metadata);
        
        this.channel = metadata.get("channel").valueString;
    }
    
    @Override
    protected Map<String, AuditMetadata> saveMetadata() {
        Map<String, AuditMetadata> metadata = super.saveMetadata();
        
        appendMetadata(metadata, "channel", channel);
        
        return metadata;
    }
    
    @Override
    public String formatMessage() {
        return this.authorizingUser + " has voided the mute on " + this.target + " from " + channel;
    }
    
}
