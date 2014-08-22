package com.bendude56.goldenapple.lock.audit;

import java.util.Map;

import org.bukkit.Location;

import com.bendude56.goldenapple.permissions.IPermissionUser;

public class LockMoveEntry extends LockEntry {
    public long lock;
    
    public long fromX, fromY, fromZ;
    public long toX, toY, toZ;
    public String fromWorld, toWorld;
    
    public LockMoveEntry() {
        super(205, AuditEntryLevel.INFO);
    }
    
    public LockMoveEntry(IPermissionUser user, long lock, Location from, Location to) {
        super(205, AuditEntryLevel.INFO, user);
        
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
    protected void loadMetadata(Map<String, AuditMetadata> metadata) {
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
    protected Map<String, AuditMetadata> saveMetadata() {
        Map<String, AuditMetadata> metadata = super.saveMetadata();
        
        appendMetadata(metadata, "lock", lock);
        
        appendMetadata(metadata, "fromX", fromX);
        appendMetadata(metadata, "fromY", fromY);
        appendMetadata(metadata, "fromZ", fromZ);
        appendMetadata(metadata, "fromWorld", fromWorld);
        
        appendMetadata(metadata, "toX", toX);
        appendMetadata(metadata, "toY", toY);
        appendMetadata(metadata, "toZ", toZ);
        appendMetadata(metadata, "toWorld", toWorld);
        
        return metadata;
    }
    
    @Override
    public String formatMessage() {
        return user + " has moved lock " + lock + " from (" + fromX + ", " + fromY + ", " + fromZ + ", " + fromWorld + ") to (" + toX + ", " + toY + ", " + toZ + ", " + toWorld + ")";
    }
}
