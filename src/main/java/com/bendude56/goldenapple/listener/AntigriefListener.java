package com.bendude56.goldenapple.listener;

import java.lang.reflect.InvocationTargetException;
import java.util.logging.Level;

import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.entity.EntityType;
import org.bukkit.event.Event;
import org.bukkit.event.EventException;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.block.BlockBurnEvent;
import org.bukkit.event.entity.EntityExplodeEvent;
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
	
	private boolean errorLoadingTntBlock = false;

	private void registerEvents() {
		errorLoadingTntBlock = false;
		PlayerInteractEvent.getHandlerList().register(new RegisteredListener(this, this, EventPriority.NORMAL, GoldenApple.getInstance(), true));
		BlockBurnEvent.getHandlerList().register(new RegisteredListener(this, this, EventPriority.NORMAL, GoldenApple.getInstance(), true));
		EntityExplodeEvent.getHandlerList().register(new RegisteredListener(this, this, EventPriority.NORMAL, GoldenApple.getInstance(), true));
		try {
			registerTntBlock();
		} catch (Throwable e) {
			GoldenApple.log(Level.WARNING, "Failed to replace TNT block data. Are you running the right version of Bukkit?");
			GoldenApple.log(Level.WARNING, (e instanceof InvocationTargetException) ? e.getCause() : e);
			errorLoadingTntBlock = true;
		}
	}

	private void unregisterEvents() {
		PlayerInteractEvent.getHandlerList().unregister(this);
		BlockBurnEvent.getHandlerList().unregister(this);
		EntityExplodeEvent.getHandlerList().unregister(this);
		if (!errorLoadingTntBlock) {
			try {
				unregisterTntBlock();
			} catch (Throwable e) { }
		}
	}
	
	private void registerTntBlock() throws Throwable {
		Class.forName("com.bendude56.goldenapple.antigrief.BlockTNT").getMethod("registerBlock", new Class<?>[0]).invoke(null, new Object[0]);
	}
	
	private void unregisterTntBlock() throws Throwable {
		Class.forName("com.bendude56.goldenapple.antigrief.BlockTNT").getMethod("unregisterBlock", new Class<?>[0]).invoke(null, new Object[0]);
	}

	@Override
	public void execute(Listener listener, Event event) throws EventException {
		if (event instanceof PlayerInteractEvent) {
			playerInteract((PlayerInteractEvent)event);
		} else if (event instanceof BlockBurnEvent) {
			blockBurn((BlockBurnEvent)event);
		} else if (event instanceof EntityExplodeEvent) {
			entityExplode((EntityExplodeEvent)event);
		} else {
			GoldenApple.log(Level.WARNING, "Unrecognized event in AntigriefListener: " + event.getClass().getName());
		}
	}

	private void playerInteract(PlayerInteractEvent event) {
		User u = User.getUser(event.getPlayer());
		
		if (event.getAction() == Action.RIGHT_CLICK_BLOCK && event.getClickedBlock().getType() == Material.TNT && event.getPlayer().getItemInHand().getType() == Material.FLINT_AND_STEEL) {
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
	
	private void blockBurn(BlockBurnEvent event) {
		if (event.getBlock().getType() == Material.TNT) {
			if (GoldenApple.getInstance().mainConfig.getBoolean("modules.antigrief.noFireTnt", true))
				event.setCancelled(true);
		}
	}
	
	private void entityExplode(EntityExplodeEvent event) {
		if (event.getEntityType() == EntityType.PRIMED_TNT && errorLoadingTntBlock && GoldenApple.getInstance().mainConfig.getBoolean("modules.antigrief.blockTntOnLoadFail", true)) {
			event.setCancelled(true);
		}
	}
}
