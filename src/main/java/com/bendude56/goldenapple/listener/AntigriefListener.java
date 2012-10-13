package com.bendude56.goldenapple.listener;

import java.util.logging.Level;

import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.event.Event;
import org.bukkit.event.EventException;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.plugin.EventExecutor;
import org.bukkit.plugin.RegisteredListener;

import com.bendude56.goldenapple.GoldenApple;
import com.bendude56.goldenapple.User;
import com.bendude56.goldenapple.antigrief.AntigriefModuleLoader;

public class AntigriefListener implements Listener, EventExecutor {

	private static AntigriefListener	listener;

	public static void startListening() {
		listener = new AntigriefListener();
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
			playerInteract((PlayerInteractEvent)event);
		} else {
			GoldenApple.log(Level.WARNING, "Unrecognized event in AntigriefListener: " + event.getClass().getName());
		}
	}

	private void playerInteract(PlayerInteractEvent event) {
		User u = User.getUser(event.getPlayer());
		
		if (event.getAction() == Action.RIGHT_CLICK_BLOCK && event.getClickedBlock().getType() == Material.TNT && event.getPlayer().getItemInHand().getType() == Material.FLINT_AND_STEEL && GoldenApple.getInstance().mainConfig.getBoolean("modules.antigrief.noLightTnt")) {
			Location l = event.getClickedBlock().getLocation();
			if (GoldenApple.getInstance().mainConfig.getBoolean("modules.antigrief.noLightTnt", true) && (GoldenApple.getInstance().permissions == null || !u.hasPermission(AntigriefModuleLoader.tntPermission))) {
				event.setCancelled(true);
				GoldenApple.getInstance().locale.sendMessage(u, "error.antigrief.tnt", false);
				GoldenApple.log(Level.WARNING, u.getName() + " attempted to ignite TNT at (" + l.getBlockX() + ", " + l.getBlockY() + ", " + l.getBlockZ() + ", " + l.getWorld().getName() + ")");
			} else {
				GoldenApple.log(Level.WARNING, u.getName() + " has ignited TNT at (" + l.getBlockX() + ", " + l.getBlockY() + ", " + l.getBlockZ() + ", " + l.getWorld().getName() + ")");
			}
		}
	}
}
