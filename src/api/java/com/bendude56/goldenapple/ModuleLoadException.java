package com.bendude56.goldenapple;

import java.io.IOException;
import java.io.ObjectOutputStream;
import java.io.OutputStream;
import java.io.Serializable;
import java.util.HashMap;

public class ModuleLoadException extends RuntimeException {
    private static final long serialVersionUID = 1202984010081752893L;
    
    private final HashMap<String, Serializable> dumpInfo;
    private final String module;
    
    public ModuleLoadException(String module) {
        this(module, (Throwable) null, new HashMap<String, Serializable>());
    }
    
    public ModuleLoadException(String module, Throwable cause) {
        this(module, cause, new HashMap<String, Serializable>());
    }
    
    public ModuleLoadException(String module, HashMap<String, Serializable> dumpInfo) {
        this(module, (Throwable) null, dumpInfo);
    }
    
    public ModuleLoadException(String module, Throwable cause, HashMap<String, Serializable> dumpInfo) {
        super("Error loading module '" + module + "': " + ((cause == null) ? "Unknown exception" : cause.getMessage()), cause);
        
        this.module = module;
        this.dumpInfo = new HashMap<String, Serializable>(dumpInfo);
        
        populateDefaultDumpInfo();
    }
    
    public ModuleLoadException(String module, String message) {
        this(module, message, null, new HashMap<String, Serializable>());
    }
    
    public ModuleLoadException(String module, String message, Throwable cause) {
        this(module, message, cause, new HashMap<String, Serializable>());
    }
    
    public ModuleLoadException(String module, String message, HashMap<String, Serializable> dumpInfo) {
        this(module, message, null, dumpInfo);
    }
    
    public ModuleLoadException(String module, String message, Throwable cause, HashMap<String, Serializable> dumpInfo) {
        super("Error loading module '" + module + "': " + message, cause);
        
        this.module = module;
        this.dumpInfo = new HashMap<String, Serializable>(dumpInfo);
        
        populateDefaultDumpInfo();
    }
    
    private void populateDefaultDumpInfo() {
        dumpInfo.put("module", module);
        
        if (this.getCause() != null) {
            dumpInfo.put("cause", this.getCause());
        }
    }
    
    protected void addDumpInfo(String key, Serializable value) {
        dumpInfo.put(key, value);
    }
    
    public void dump(OutputStream s) throws IOException {
        ObjectOutputStream o = new ObjectOutputStream(s);
        o.writeObject(dumpInfo);
        o.close();
    }
    
    public enum LoadExceptionType {
        Sql, CommandRegister, EventRegister, PermissionRegister
    }
}
