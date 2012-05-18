package com.bendude56.goldenapple.lock;

import java.sql.SQLException;
import java.util.ArrayDeque;
import java.util.Deque;
import java.util.HashMap;
import java.util.logging.Level;

import org.bukkit.Location;

import com.bendude56.goldenapple.GoldenApple;

public class LockManager {
	private HashMap<Long, LockedBlock> lockCache;
	private Deque<Long> cacheOut;
	
	public LockManager() {
		lockCache = new HashMap<Long, LockedBlock>();
		cacheOut = new ArrayDeque<Long>();
		try {
			GoldenApple.getInstance().database.execute("CREATE TABLE IF NOT EXISTS Locks (ID BIGINT PRIMARY KEY, X BIGINT, Y BIGINT, Z BIGINT, World VARCHAR(128), Type VARCHAR(32))");
		} catch (SQLException e) {
			GoldenApple.log(Level.SEVERE, "Failed to create table 'Locks':");
			GoldenApple.log(Level.SEVERE, e);
		}
	}

}
