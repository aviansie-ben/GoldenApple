package com.bendude56.goldenapple.audit;

import java.util.HashMap;

public class AuditStartEvent extends AuditEvent {
    
    public AuditStartEvent() {
        super(100, AuditEventLevel.INFO, "Base");
    }
    
    @Override
    protected void loadMetadata(HashMap<String, AuditMetadata> metadata) {}
    
    @Override
    protected HashMap<String, AuditMetadata> saveMetadata() {
        return new HashMap<String, AuditMetadata>();
    }
    
    @Override
    public String formatMessage() {
        return "Auditing services have been started.";
    }
    
}
