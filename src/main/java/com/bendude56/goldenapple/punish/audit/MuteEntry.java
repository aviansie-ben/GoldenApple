package com.bendude56.goldenapple.punish.audit;

import java.util.Map;

public class MuteEntry extends PunishEntry {
    public String duration;
    public String reason;
    public String channel;
    
    public MuteEntry() {
        super(301, AuditEntryLevel.WARNING);
    }
    
    public MuteEntry(String authorizingUser, String target, String duration, String reason, String channel) {
        super(301, AuditEntryLevel.WARNING, authorizingUser, target);
        
        this.duration = duration;
        this.reason = reason;
        this.channel = channel;
    }
    
    @Override
    protected void loadMetadata(Map<String, AuditMetadata> metadata) {
        super.loadMetadata(metadata);
        
        this.duration = metadata.get("duration").valueString;
        this.reason = metadata.get("reason").valueString;
        this.channel = metadata.get("channel").valueString;
    }
    
    @Override
    protected Map<String, AuditMetadata> saveMetadata() {
        Map<String, AuditMetadata> metadata = super.saveMetadata();
        
        appendMetadata(metadata, "duration", duration);
        appendMetadata(metadata, "reason", reason);
        
        appendMetadata(metadata, "channel", channel);
        
        return metadata;
    }
    
    @Override
    public String formatMessage() {
        return this.authorizingUser + " has muted " + this.target + " from " + channel + " (Duration: " + this.duration + "): " + this.reason;
    }
    
}
