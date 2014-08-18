package com.bendude56.goldenapple.lock.audit;

import java.util.HashMap;

import org.bukkit.Location;

import com.bendude56.goldenapple.permissions.IPermissionUser;

public class LockMoveEvent extends LockEvent {
    public long lock;
    
    public long fromX, fromY, fromZ;
    public long toX, toY, toZ;
    public String fromWorld, toWorld;
    
    public LockMoveEvent() {
        super(205, AuditEventLevel.INFO);
    }
    
    public LockMoveEvent(IPermissionUser user, long lock, Location from, Location to) {
        super(205, AuditEventLevel.INFO, user);
        
        this.lock = lock;
        
        this.fromX = from.getBlockX();
        this.fromY = from.getBlockY();
        this.fromZ = from.getBlockZ();
        this.fromWorld = from.getWorld().getName();
        
        this.toX = to.getBlockX();
        this.toY = to.getBlockY();
        this.toZ = to.getBlockZ();
        this.toWorld = to.getWorld().getName();
    }
    
    @Override
    protected void loadMetadata(HashMap<String, AuditMetadata> metadata) {
        super.loadMetadata(metadata);
        
        lock = metadata.get("lock").valueInt;
        
        fromX = metadata.get("fromX").valueInt;
        fromY = metadata.get("fromY").valueInt;
        fromZ = metadata.get("fromZ").valueInt;
        fromWorld = metadata.get("fromWorld").valueString;
        
        toX = metadata.get("toX").valueInt;
        toY = metadata.get("toY").valueInt;
        toZ = metadata.get("toZ").valueInt;
        toWorld = metadata.get("toWorld").valueString;
    }
    
    @Override
    protected HashMap<String, AuditMetadata> saveMetadata() {
        HashMap<String, AuditMetadata> metadata = super.saveMetadata();
        
        metadata.put("lock", createMetadata("lock", lock));
        
        metadata.put("fromX", createMetadata("fromX", fromX));
        metadata.put("fromY", createMetadata("fromY", fromY));
        metadata.put("fromZ", createMetadata("fromZ", fromZ));
        metadata.put("fromWorld", createMetadata("fromWorld", fromWorld));
        
        metadata.put("toX", createMetadata("toX", toX));
        metadata.put("toY", createMetadata("toY", toY));
        metadata.put("toZ", createMetadata("toZ", toZ));
        metadata.put("toWorld", createMetadata("toWorld", toWorld));
        
        return metadata;
    }
    
    @Override
    public String formatMessage() {
        return user + " has moved lock " + lock + " from (" + fromX + ", " + fromY + ", " + fromZ + ", " + fromWorld + ") to (" + toX + ", " + toY + ", " + toZ + ", " + toWorld + ")";
    }
}
