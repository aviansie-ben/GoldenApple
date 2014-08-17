package com.bendude56.goldenapple.audit;

import java.util.HashMap;

public class AuditStopEvent extends AuditEvent {
    
    public AuditStopEvent() {
        super(101, AuditEventLevel.INFO, "Base");
    }
    
    @Override
    protected void loadMetadata(HashMap<String, AuditMetadata> metadata) {}
    
    @Override
    protected HashMap<String, AuditMetadata> saveMetadata() {
        return new HashMap<String, AuditMetadata>();
    }
    
    @Override
    public String formatMessage() {
        return "Auditing services have been stopped.";
    }
    
}
