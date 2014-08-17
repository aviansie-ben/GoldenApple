package com.bendude56.goldenapple.punish.audit;

import java.util.HashMap;

import com.bendude56.goldenapple.audit.AuditEvent;

public abstract class PunishEvent extends AuditEvent {
    
    public String authorizingUser;
    public String target;
    
    public PunishEvent(int eventId, AuditEventLevel severity, String authorizingUser, String target) {
        super(eventId, severity, "Punish");
        
        this.authorizingUser = authorizingUser;
        this.target = target;
    }
    
    @Override
    protected void loadMetadata(HashMap<String, AuditMetadata> metadata) {
        authorizingUser = metadata.get("authorizingUser").valueString;
        target = metadata.get("target").valueString;
    }
    
    @Override
    protected HashMap<String, AuditMetadata> saveMetadata() {
        HashMap<String, AuditMetadata> metadata = new HashMap<String, AuditMetadata>();
        
        metadata.put("authorizingUser", createMetadata("authorizingUser", authorizingUser));
        metadata.put("target", createMetadata("target", target));
        
        return metadata;
    }
    
}
