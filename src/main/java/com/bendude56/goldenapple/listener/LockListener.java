package com.bendude56.goldenapple.listener;

import java.util.logging.Level;

import org.bukkit.event.Event;
import org.bukkit.event.EventException;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.plugin.EventExecutor;
import org.bukkit.plugin.RegisteredListener;

import com.bendude56.goldenapple.GoldenApple;
import com.bendude56.goldenapple.User;
import com.bendude56.goldenapple.lock.LockedBlock;

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
		PlayerInteractEvent.getHandlerList().register(new RegisteredListener(this, this, EventPriority.NORMAL, GoldenApple.getInstance(), true));
	}

	private void unregisterEvents() {
		PlayerInteractEvent.getHandlerList().unregister(this);
	}

	@Override
	public void execute(Listener listener, Event event) throws EventException {
		if (event instanceof PlayerInteractEvent) {
			playerInteract((PlayerInteractEvent) event);
		} else {
			GoldenApple.log(Level.WARNING, "Unrecognized event in LockListener: " + event.getClass().getName());
		}
	}
	
	private void playerInteract(PlayerInteractEvent event) {
		LockedBlock lock = GoldenApple.getInstance().locks.getLock(event.getClickedBlock().getLocation());
		User u = User.getUser(event.getPlayer());
		if (lock == null)
			return;
		
		if (!lock.canUse(u)) {
			GoldenApple.getInstance().locale.sendMessage(u, "error.lock.noUse", false, GoldenApple.getInstance().permissions.getUser(lock.getOwner()).getName());
			event.setCancelled(true);
			return;
		}
	}
}
