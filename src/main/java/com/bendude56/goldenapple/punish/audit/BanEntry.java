package com.bendude56.goldenapple.punish.audit;

import java.util.Map;

public class BanEntry extends PunishEntry {
    public String duration;
    public String reason;
    
    public BanEntry() {
        super(300, AuditEntryLevel.WARNING);
    }
    
    public BanEntry(String authorizingUser, String target, String duration, String reason) {
        super(300, AuditEntryLevel.WARNING, authorizingUser, target);
        
        this.duration = duration;
        this.reason = reason;
    }
    
    @Override
    protected void loadMetadata(Map<String, AuditMetadata> metadata) {
        super.loadMetadata(metadata);
        
        this.duration = metadata.get("duration").valueString;
        this.reason = metadata.get("reason").valueString;
    }
    
    @Override
    protected Map<String, AuditMetadata> saveMetadata() {
        Map<String, AuditMetadata> metadata = super.saveMetadata();
        
        appendMetadata(metadata, "duration", duration);
        appendMetadata(metadata, "reason", reason);
        
        return metadata;
    }
    
    @Override
    public String formatMessage() {
        return this.authorizingUser + " has banned " + this.target + " (Duration: " + this.duration + "): " + this.reason;
    }
    
}
