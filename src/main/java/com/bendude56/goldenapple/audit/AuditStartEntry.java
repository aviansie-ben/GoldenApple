package com.bendude56.goldenapple.audit;

import java.util.HashMap;
import java.util.Map;

public class AuditStartEntry extends AuditEntry {
    
    public AuditStartEntry() {
        super(100, AuditEntryLevel.INFO, "Base");
    }
    
    @Override
    protected void loadMetadata(Map<String, AuditMetadata> metadata) {}
    
    @Override
    protected Map<String, AuditMetadata> saveMetadata() {
        return new HashMap<String, AuditMetadata>();
    }
    
    @Override
    public String formatMessage() {
        return "Auditing services have been started.";
    }
    
}
