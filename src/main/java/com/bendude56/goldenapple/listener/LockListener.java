package com.bendude56.goldenapple.listener;

import java.util.logging.Level;

import org.bukkit.event.Event;
import org.bukkit.event.EventException;
import org.bukkit.event.Listener;
import org.bukkit.plugin.EventExecutor;
import com.bendude56.goldenapple.GoldenApple;

public class LockListener implements Listener, EventExecutor {

	private static LockListener	listener;

	public static void startListening() {
		listener = new LockListener();
		listener.registerEvents();
	}

	public static void stopListening() {
		if (listener != null) {
			listener.unregisterEvents();
			listener = null;
		}
	}

	private void registerEvents() {
		
	}

	private void unregisterEvents() {
		
	}

	@Override
	public void execute(Listener listener, Event event) throws EventException {
		if (false) {
		} else {
			GoldenApple.log(Level.WARNING, "Unrecognized event in LockListener: " + event.getClass().getName());
		}
	}
}
