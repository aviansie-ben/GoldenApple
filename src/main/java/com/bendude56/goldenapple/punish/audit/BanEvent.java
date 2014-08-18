package com.bendude56.goldenapple.punish.audit;

import java.util.HashMap;

public class BanEvent extends PunishEvent {
    public String duration;
    public String reason;
    
    public BanEvent() {
        super(300, AuditEventLevel.WARNING);
    }
    
    public BanEvent(String authorizingUser, String target, String duration, String reason) {
        super(300, AuditEventLevel.WARNING, authorizingUser, target);
        
        this.duration = duration;
        this.reason = reason;
    }
    
    @Override
    protected void loadMetadata(HashMap<String, AuditMetadata> metadata) {
        super.loadMetadata(metadata);
        
        this.duration = metadata.get("duration").valueString;
        this.reason = metadata.get("reason").valueString;
    }
    
    @Override
    protected HashMap<String, AuditMetadata> saveMetadata() {
        HashMap<String, AuditMetadata> metadata = super.saveMetadata();
        
        metadata.put("duration", createMetadata("duration", duration));
        metadata.put("reason", createMetadata("reason", reason));
        
        return metadata;
    }
    
    @Override
    public String formatMessage() {
        return this.authorizingUser + " has banned " + this.target + " (Duration: " + this.duration + "): " + this.reason;
    }
    
}
