package com.bendude56.goldenapple.audit;

import java.util.HashMap;
import java.util.Map;

public class ModuleDisableEntry extends AuditEntry {
    
    public String module;
    public String authorizingUser;
    
    public ModuleDisableEntry() {
        super(103, AuditEntryLevel.INFO, "Base");
    }
    
    @Deprecated
    public ModuleDisableEntry(String module) {
        this(module, "Unknown");
    }
    
    public ModuleDisableEntry(String module, String authorizingUser) {
        this();
        this.module = module;
        this.authorizingUser = authorizingUser;
    }
    
    @Override
    protected void loadMetadata(Map<String, AuditMetadata> metadata) {
        this.module = metadata.get("module").valueString;
        this.authorizingUser = (metadata.containsKey("authorizingUser")) ? metadata.get(authorizingUser).valueString : "Unknown";
    }
    
    @Override
    protected Map<String, AuditMetadata> saveMetadata() {
        Map<String, AuditMetadata> metadata = new HashMap<String, AuditMetadata>();
        
        appendMetadata(metadata, "module", module);
        appendMetadata(metadata, "authorizingUser", authorizingUser);
        
        return metadata;
    }
    
    @Override
    public String formatMessage() {
        return "Module '" + module + "' has been stopped by " + authorizingUser + ".";
    }
    
}
