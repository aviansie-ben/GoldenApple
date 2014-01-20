package com.bendude56.goldenapple.listener;

import java.lang.reflect.InvocationTargetException;
import java.util.List;
import java.util.logging.Level;

import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.entity.EntityType;
import org.bukkit.event.Event;
import org.bukkit.event.EventException;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.block.BlockBurnEvent;
import org.bukkit.event.entity.EntityChangeBlockEvent;
import org.bukkit.event.entity.EntityExplodeEvent;
import org.bukkit.event.entity.EntityTargetEvent;
import org.bukkit.event.entity.PotionSplashEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.plugin.EventExecutor;
import org.bukkit.plugin.RegisteredListener;
import com.bendude56.goldenapple.GoldenApple;
import com.bendude56.goldenapple.User;
import com.bendude56.goldenapple.antigrief.AntigriefModuleLoader;
import com.bendude56.goldenapple.permissions.PermissionManager;

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
		EntityTargetEvent.getHandlerList().register(new RegisteredListener(this, this, EventPriority.NORMAL, GoldenApple.getInstance(), true));
		EntityChangeBlockEvent.getHandlerList().register(new RegisteredListener(this, this, EventPriority.NORMAL, GoldenApple.getInstance(), true));
		try {
			registerTntBlock();
		} catch (Throwable e) {
			GoldenApple.log(Level.WARNING, "Failed to replace TNT block data. Are you running the right version of Bukkit?");
			GoldenApple.log(Level.WARNING, (e instanceof InvocationTargetException) ? e.getCause() : e);
			errorLoadingTntBlock = true;
		}
		try {
			registerPotionItem();
		} catch (Throwable e) {
			GoldenApple.log(Level.WARNING, "Failed to replace potion item data. Are you running the right version of Bukkit?");
			GoldenApple.log(Level.WARNING, (e instanceof InvocationTargetException) ? e.getCause() : e);
		}
	}

	private void unregisterEvents() {
		PlayerInteractEvent.getHandlerList().unregister(this);
		BlockBurnEvent.getHandlerList().unregister(this);
		EntityExplodeEvent.getHandlerList().unregister(this);
		EntityTargetEvent.getHandlerList().unregister(this);
		EntityChangeBlockEvent.getHandlerList().unregister(this);
		
		if (!errorLoadingTntBlock) {
			try {
				unregisterTntBlock();
			} catch (Throwable e) { }
		}
		try {
			unregisterPotionItem();
		} catch (Throwable e) { }
	}
	
	private void registerTntBlock() throws Throwable {
		Class.forName("com.bendude56.goldenapple.antigrief.BlockTNT").getMethod("registerBlock", new Class<?>[0]).invoke(null, new Object[0]);
	}
	
	private void unregisterTntBlock() throws Throwable {
		Class.forName("com.bendude56.goldenapple.antigrief.BlockTNT").getMethod("unregisterBlock", new Class<?>[0]).invoke(null, new Object[0]);
	}
	
	private void registerPotionItem() throws Throwable {
		Class.forName("com.bendude56.goldenapple.antigrief.ItemPotion").getMethod("registerItem", new Class<?>[0]).invoke(null, new Object[0]);
	}
	
	private void unregisterPotionItem() throws Throwable {
		Class.forName("com.bendude56.goldenapple.antigrief.ItemPotion").getMethod("unregisterItem", new Class<?>[0]).invoke(null, new Object[0]);
	}

	@Override
	public void execute(Listener listener, Event event) throws EventException {
		if (event instanceof PlayerInteractEvent) {
			playerInteract((PlayerInteractEvent)event);
		} else if (event instanceof BlockBurnEvent) {
			blockBurn((BlockBurnEvent)event);
		} else if (event instanceof EntityExplodeEvent) {
			entityExplode((EntityExplodeEvent)event);
		} else if (event instanceof EntityTargetEvent) {
			entityTarget((EntityTargetEvent)event);
		} else if (event instanceof EntityChangeBlockEvent) {
			entityChangeBlock((EntityChangeBlockEvent)event);
		} else if (event instanceof PotionSplashEvent) {
			potionSplash((PotionSplashEvent)event);
		} else {
			GoldenApple.log(Level.WARNING, "Unrecognized event in AntigriefListener: " + event.getClass().getName());
		}
	}

	private void playerInteract(PlayerInteractEvent event) {
		User u = User.getUser(event.getPlayer());
		
		if (event.getAction() == Action.RIGHT_CLICK_BLOCK && event.getClickedBlock().getType() == Material.TNT && event.getPlayer().getItemInHand().getType() == Material.FLINT_AND_STEEL) {
			Location l = event.getClickedBlock().getLocation();
			if (GoldenApple.getInstanceMainConfig().getBoolean("modules.antigrief.noLightTnt", true) && (PermissionManager.getInstance() == null || !u.hasPermission(AntigriefModuleLoader.tntPermission))) {
				event.setCancelled(true);
				u.sendLocalizedMessage("error.antigrief.tnt");
				GoldenApple.log(Level.WARNING, u.getName() + " attempted to ignite TNT at (" + l.getBlockX() + ", " + l.getBlockY() + ", " + l.getBlockZ() + ", " + l.getWorld().getName() + ")");
			} else {
				GoldenApple.log(Level.WARNING, u.getName() + " has ignited TNT at (" + l.getBlockX() + ", " + l.getBlockY() + ", " + l.getBlockZ() + ", " + l.getWorld().getName() + ")");
			}
		}
	}
	
	private void blockBurn(BlockBurnEvent event) {
		if (event.getBlock().getType() == Material.TNT) {
			if (GoldenApple.getInstanceMainConfig().getBoolean("modules.antigrief.noFireTnt", true))
				event.setCancelled(true);
		}
	}
	
	private void entityExplode(EntityExplodeEvent event) {
		if (event.getEntityType() == EntityType.PRIMED_TNT && errorLoadingTntBlock && GoldenApple.getInstanceMainConfig().getBoolean("modules.antigrief.blockTntOnLoadFail", true)) {
			event.setCancelled(true);
		} else if (event.getEntityType() == EntityType.CREEPER && GoldenApple.getInstanceMainConfig().getBoolean("modules.antigrief.noCreeperBlockDamage", true)) {
			List<Block> blockList = event.blockList();
			while (blockList.size() > 0)
				blockList.remove(0);
		} else if ((event.getEntityType() == EntityType.FIREBALL || event.getEntityType() == EntityType.SMALL_FIREBALL) && GoldenApple.getInstanceMainConfig().getBoolean("modules.antigrief.noFireballBlockDamage", true)) {
			List<Block> blockList = event.blockList();
			while (blockList.size() > 0)
				blockList.remove(0);
		} else if (event.getEntityType() == EntityType.MINECART_TNT && GoldenApple.getInstanceMainConfig().getBoolean("modules.antigrief.noMinecartTnt", true)) {
			List<Block> blockList = event.blockList();
			while (blockList.size() > 0)
				blockList.remove(0);
		}
	}
	
	private void entityTarget(EntityTargetEvent event) {
		if (event.getEntityType() == EntityType.CREEPER && GoldenApple.getInstanceMainConfig().getBoolean("modules.antigrief.noCreeperExplosion", true)) {
			event.setCancelled(true);
		}
	}
	
	private void entityChangeBlock(EntityChangeBlockEvent event) { 
		if (event.getEntityType() == EntityType.ENDERMAN && GoldenApple.getInstanceMainConfig().getBoolean("modules.antigrief.noEndermanMoveBlock", true)) {
			event.setCancelled(true);
		}
	}
	
	private void potionSplash(PotionSplashEvent event) {
		// TODO Implement this later
	}
}
