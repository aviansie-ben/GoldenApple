package com.bendude56.goldenapple.lock.audit;

import java.util.Map;

import org.bukkit.Location;

import com.bendude56.goldenapple.permissions.IPermissionUser;

public class LockCreateEntry extends LockEntry {
    public long lock;
    public String type;
    
    public long x, y, z;
    public String world;
    
    public LockCreateEntry() {
        super(203, AuditEntryLevel.INFO);
    }
    
    public LockCreateEntry(IPermissionUser user, long lock, String type, Location location) {
        super(203, AuditEntryLevel.INFO, user);
        
        this.lock = lock;
        this.type = type;
        
        this.x = location.getBlockX();
        this.y = location.getBlockY();
        this.z = location.getBlockZ();
        this.world = location.getWorld().getName();
    }
    
    @Override
    protected void loadMetadata(Map<String, AuditMetadata> metadata) {
        super.loadMetadata(metadata);
        
        lock = metadata.get("lock").valueInt;
        type = metadata.get("type").valueString;
        
        x = metadata.get("x").valueInt;
        y = metadata.get("y").valueInt;
        z = metadata.get("z").valueInt;
        world = metadata.get("world").valueString;
    }
    
    @Override
    protected Map<String, AuditMetadata> saveMetadata() {
        Map<String, AuditMetadata> metadata = super.saveMetadata();
        
        appendMetadata(metadata, "lock", lock);
        appendMetadata(metadata, "type", type);
        
        appendMetadata(metadata, "x", x);
        appendMetadata(metadata, "y", y);
        appendMetadata(metadata, "z", z);
        appendMetadata(metadata, "world", world);
        
        return metadata;
    }
    
    @Override
    public String formatMessage() {
        return user + " has created lock " + lock + " of type " + type + " at (" + x + ", " + y + ", " + z + ", " + world + ")";
    }
}
