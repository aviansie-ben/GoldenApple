package com.bendude56.goldenapple.lock;

import java.lang.reflect.InvocationTargetException;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayDeque;
import java.util.Deque;
import java.util.HashMap;
import java.util.Map;
import java.util.logging.Level;

import org.bukkit.Location;

import com.bendude56.goldenapple.GoldenApple;
import com.bendude56.goldenapple.lock.LockedBlock.RegisteredBlock;
import com.bendude56.goldenapple.permissions.PermissionManager.Permission;
import com.bendude56.goldenapple.permissions.PermissionManager.PermissionNode;

public class LockManager {
	
	// goldenapple.lock
	public static PermissionNode lockNode;
	public static Permission addPermission;
	
	// goldenapple.lock.delete
	public static PermissionNode removeNode;
	public static Permission removeOwnPermission;
	public static Permission removeAllPermission;
	
	// goldenapple.lock.guest
	public static PermissionNode guestNode;
	public static Permission guestOwnPermission;
	public static Permission guestAllPermission;
	
	private HashMap<Long, LockedBlock> lockCache;
	private Deque<Long> cacheOut;
	
	public LockManager() {
		lockCache = new HashMap<Long, LockedBlock>();
		cacheOut = new ArrayDeque<Long>();
		try {
			GoldenApple.getInstance().database.execute("CREATE TABLE IF NOT EXISTS Locks (ID BIGINT PRIMARY KEY, X BIGINT, Y BIGINT, Z BIGINT, World VARCHAR(128), Type VARCHAR(32), AccessLevel INT, Owner BIGINT, Guests TEXT)");
		} catch (SQLException e) {
			GoldenApple.log(Level.SEVERE, "Failed to create table 'Locks':");
			GoldenApple.log(Level.SEVERE, e);
		}
	}
	
	private LockedBlock checkCache(Location l) {
		for (Map.Entry<Long, LockedBlock> b : lockCache.entrySet()) {
			if (b.getValue().getLocation().equals(l)) {
				return b.getValue();
			}
		}
		return null;
	}
	
	private LockedBlock loadIntoCache(ResultSet r) throws SQLException {
		RegisteredBlock lockClass = LockedBlock.getBlock(r.getString("Type"));
		if (lockClass == null) {
			GoldenApple.log(Level.WARNING, "The specified lock type is not loaded: " + r.getString("Type"));
			GoldenApple.log(Level.WARNING, "This lock will be ignored...");
			return null;
		}
		try {
			LockedBlock b = lockClass.blockClass.getConstructor(new Class<?>[] { ResultSet.class }).newInstance(r);
			lockCache.put(b.getLockId(), b);
			cacheOut.add(b.getLockId());
			
			return b;
		} catch (InstantiationException | IllegalAccessException | IllegalArgumentException | InvocationTargetException | NoSuchMethodException | SecurityException e) {
			GoldenApple.log(Level.SEVERE, "There was an error while loading a lock of type '" + lockClass.identifier + "'");
			GoldenApple.log(Level.SEVERE, "Please report this error to the creator of '" + lockClass.plugin.getName() + "'. Please include the following stack trace:");
			GoldenApple.log(Level.SEVERE, e);
			return null;
		}
	}
	
	public LockedBlock getLock(Location l) {
		LockedBlock.correctLocation(l);
		LockedBlock b = checkCache(l);
		if (b != null)
			return b;
		try {
			ResultSet r = GoldenApple.getInstance().database.executeQuery("SELECT ID, X, Y, Z, World, Type, AccessLevel, Owner, Guests FROM Locks WHERE X=?, Y=?, Z=?, World=?", l.getBlockX(), l.getBlockY(), l.getBlockZ(), l.getWorld().getName());
			if (r.next()) {
				return loadIntoCache(r);
			} else {
				return null;
			}
		} catch (SQLException e) {
			GoldenApple.log(Level.WARNING, "Error while attempting to retrieve a lock from the database:");
			GoldenApple.log(Level.WARNING, e);
			return null;
		}
	}

}
