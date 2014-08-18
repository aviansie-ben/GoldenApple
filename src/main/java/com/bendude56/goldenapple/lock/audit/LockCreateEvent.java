package com.bendude56.goldenapple.lock.audit;

import java.util.HashMap;

import org.bukkit.Location;

import com.bendude56.goldenapple.permissions.IPermissionUser;

public class LockCreateEvent extends LockEvent {
    public long lock;
    public String type;
    
    public long x, y, z;
    public String world;
    
    public LockCreateEvent() {
        super(203, AuditEventLevel.INFO);
    }
    
    public LockCreateEvent(IPermissionUser user, long lock, String type, Location location) {
        super(203, AuditEventLevel.INFO, user);
        
        this.lock = lock;
        this.type = type;
        
        this.x = location.getBlockX();
        this.y = location.getBlockY();
        this.z = location.getBlockZ();
        this.world = location.getWorld().getName();
    }
    
    @Override
    protected void loadMetadata(HashMap<String, AuditMetadata> metadata) {
        super.loadMetadata(metadata);
        
        lock = metadata.get("lock").valueInt;
        type = metadata.get("type").valueString;
        
        x = metadata.get("x").valueInt;
        y = metadata.get("y").valueInt;
        z = metadata.get("z").valueInt;
        world = metadata.get("world").valueString;
    }
    
    @Override
    protected HashMap<String, AuditMetadata> saveMetadata() {
        HashMap<String, AuditMetadata> metadata = super.saveMetadata();
        
        metadata.put("lock", createMetadata("lock", lock));
        metadata.put("type", createMetadata("type", type));
        
        metadata.put("x", createMetadata("x", x));
        metadata.put("y", createMetadata("y", y));
        metadata.put("z", createMetadata("z", z));
        metadata.put("world", createMetadata("world", world));
        
        return metadata;
    }
    
    @Override
    public String formatMessage() {
        return user + " has created lock " + lock + " of type " + type + " at (" + x + ", " + y + ", " + z + ", " + world + ")";
    }
}
