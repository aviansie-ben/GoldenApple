package com.bendude56.goldenapple.punish.audit;

import java.util.Map;

public class WarnEntry extends PunishEntry {
    public String reason;
    
    public WarnEntry() {
        super(304, AuditEntryLevel.WARNING);
    }
    
    public WarnEntry(String authorizingUser, String target, String reason) {
        super(304, AuditEntryLevel.WARNING, authorizingUser, target);
        
        this.reason = reason;
    }
    
    @Override
    protected void loadMetadata(Map<String, AuditMetadata> metadata) {
        super.loadMetadata(metadata);
        
        this.reason = metadata.get("reason").valueString;
    }
    
    @Override
    protected Map<String, AuditMetadata> saveMetadata() {
        Map<String, AuditMetadata> metadata = super.saveMetadata();
        
        appendMetadata(metadata, "reason", reason);
        
        return metadata;
    }
    
    @Override
    public String formatMessage() {
        return this.authorizingUser + " has issued a warning to " + this.target + ": " + this.reason;
    }
    
}
