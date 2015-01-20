package com.bendude56.goldenapple.antigrief;

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
import org.bukkit.event.block.BlockIgniteEvent;
import org.bukkit.event.block.BlockIgniteEvent.IgniteCause;
import org.bukkit.event.entity.CreatureSpawnEvent;
import org.bukkit.event.entity.EntityChangeBlockEvent;
import org.bukkit.event.entity.EntityExplodeEvent;
import org.bukkit.event.entity.EntityTargetEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.plugin.EventExecutor;
import org.bukkit.plugin.RegisteredListener;

import com.bendude56.goldenapple.GoldenApple;
import com.bendude56.goldenapple.PerformanceMonitor.PerformanceEvent;
import com.bendude56.goldenapple.User;
import com.bendude56.goldenapple.permissions.PermissionManager;

public class AntigriefListener implements Listener, EventExecutor {
    
    private static AntigriefListener listener;
    
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
        BlockIgniteEvent.getHandlerList().register(new RegisteredListener(this, this, EventPriority.NORMAL, GoldenApple.getInstance(), true));
        BlockBurnEvent.getHandlerList().register(new RegisteredListener(this, this, EventPriority.NORMAL, GoldenApple.getInstance(), true));
        CreatureSpawnEvent.getHandlerList().register(new RegisteredListener(this, this, EventPriority.NORMAL, GoldenApple.getInstance(), true));
        EntityExplodeEvent.getHandlerList().register(new RegisteredListener(this, this, EventPriority.NORMAL, GoldenApple.getInstance(), true));
        EntityTargetEvent.getHandlerList().register(new RegisteredListener(this, this, EventPriority.NORMAL, GoldenApple.getInstance(), true));
        EntityChangeBlockEvent.getHandlerList().register(new RegisteredListener(this, this, EventPriority.NORMAL, GoldenApple.getInstance(), true));
    }
    
    private void unregisterEvents() {
        PlayerInteractEvent.getHandlerList().unregister(this);
        BlockIgniteEvent.getHandlerList().unregister(this);
        BlockBurnEvent.getHandlerList().unregister(this);
        CreatureSpawnEvent.getHandlerList().unregister(this);
        EntityExplodeEvent.getHandlerList().unregister(this);
        EntityTargetEvent.getHandlerList().unregister(this);
        EntityChangeBlockEvent.getHandlerList().unregister(this);
    }
    
    @Override
    public void execute(Listener listener, Event event) throws EventException {
        PerformanceEvent e = GoldenApple.getInstancePerformanceMonitor().createForEvent("Antigrief", event.getClass().getName());
        e.start();
        
        try {
            if (event instanceof PlayerInteractEvent) {
                playerInteract((PlayerInteractEvent) event);
            } else if (event instanceof BlockIgniteEvent) {
                blockIgnite((BlockIgniteEvent) event);
            } else if (event instanceof BlockBurnEvent) {
                blockBurn((BlockBurnEvent) event);
            } else if (event instanceof CreatureSpawnEvent) {
                creatureSpawn((CreatureSpawnEvent) event);
            } else if (event instanceof EntityExplodeEvent) {
                entityExplode((EntityExplodeEvent) event);
            } else if (event instanceof EntityTargetEvent) {
                entityTarget((EntityTargetEvent) event);
            } else if (event instanceof EntityChangeBlockEvent) {
                entityChangeBlock((EntityChangeBlockEvent) event);
            } else {
                GoldenApple.log(Level.WARNING, "Unrecognized event in AntigriefListener: " + event.getClass().getName());
            }
        } finally {
            e.stop();
        }
    }
    
    private void playerInteract(PlayerInteractEvent event) {
        User u = User.getUser(event.getPlayer());
        
        if (event.getAction() == Action.RIGHT_CLICK_BLOCK && event.getClickedBlock().getType() == Material.TNT && event.getPlayer().getItemInHand().getType() == Material.FLINT_AND_STEEL) {
            Location l = event.getClickedBlock().getLocation();
            if (GoldenApple.getInstanceMainConfig().getBoolean("modules.antigrief.noLightTnt", true) && (PermissionManager.getInstance() == null || !u.hasPermission(AntigriefModuleLoader.tntPermission))) {
                event.setCancelled(true);
                u.sendLocalizedMessage("module.antigrief.tnt.blocked");
                GoldenApple.log(Level.WARNING, u.getName() + " attempted to ignite TNT at (" + l.getBlockX() + ", " + l.getBlockY() + ", " + l.getBlockZ() + ", " + l.getWorld().getName() + ")");
            } else {
                GoldenApple.log(Level.WARNING, u.getName() + " has ignited TNT at (" + l.getBlockX() + ", " + l.getBlockY() + ", " + l.getBlockZ() + ", " + l.getWorld().getName() + ")");
            }
        }
    }
    
    private void blockIgnite(BlockIgniteEvent event) {
        if (event.getCause() == IgniteCause.SPREAD) {
            if (GoldenApple.getInstanceMainConfig().getBoolean("modules.antigrief.noFireSpread", true)) {
                event.setCancelled(true);
            }
        } else if (event.getCause() == IgniteCause.FIREBALL) {
            if (GoldenApple.getInstanceMainConfig().getBoolean("modules.antigrief.noFireballFireLight", true)) {
                event.setCancelled(true);
            }
        } else if (event.getCause() == IgniteCause.LAVA) {
            if (GoldenApple.getInstanceMainConfig().getBoolean("modules.antigrief.noFireLava", true)) {
                event.setCancelled(true);
            }
        } else if (event.getCause() == IgniteCause.LIGHTNING) {
            if (GoldenApple.getInstanceMainConfig().getBoolean("modules.antigrief.noFireLightning", true)) {
                event.setCancelled(true);
            }
        } else if (event.getCause() == IgniteCause.FLINT_AND_STEEL) {
            User u = User.getUser(event.getPlayer());
            Location l = event.getBlock().getLocation();
            if (GoldenApple.getInstanceMainConfig().getBoolean("modules.antigrief.noFireLight", true)) {
                if (u.hasPermission(AntigriefModuleLoader.lighterPermission)) {
                    GoldenApple.log(Level.WARNING, u.getName() + " has lit a fire at (" + l.getBlockX() + ", " + l.getBlockY() + ", " + l.getBlockZ() + ", " + l.getWorld().getName() + ")");
                } else {
                    GoldenApple.log(Level.WARNING, u.getName() + " attempted to light fire at (" + l.getBlockX() + ", " + l.getBlockY() + ", " + l.getBlockZ() + ", " + l.getWorld().getName() + ")");
                    event.setCancelled(true);
                }
            }
        } else {
            if (GoldenApple.getInstanceMainConfig().getBoolean("modules.antigrief.noFireUnknown", true)) {
                event.setCancelled(true);
            }
        }
    }
    
    private void blockBurn(BlockBurnEvent event) {
        if (event.getBlock().getType() == Material.TNT) {
            if (GoldenApple.getInstanceMainConfig().getBoolean("modules.antigrief.noFireTnt", true)) {
                event.setCancelled(true);
            }
        } else if (GoldenApple.getInstanceMainConfig().getBoolean("modules.antigrief.noFireDamage", true)) {
            event.setCancelled(true);
        }
    }
    
    private void creatureSpawn(CreatureSpawnEvent event) {
        if (event.getEntityType() == EntityType.WITHER && GoldenApple.getInstanceMainConfig().getBoolean("modules.antigrief.noWither", true)) {
            event.setCancelled(true);
        }
    }
    
    private void entityExplode(EntityExplodeEvent event) {
        if (event.getEntityType() == EntityType.PRIMED_TNT && GoldenApple.getInstanceMainConfig().getBoolean("modules.antigrief.noTntBlockDamage", true)) {
            List<Block> blockList = event.blockList();
            while (blockList.size() > 0) {
                blockList.remove(0);
            }
        } else if (event.getEntityType() == EntityType.CREEPER && GoldenApple.getInstanceMainConfig().getBoolean("modules.antigrief.noCreeperBlockDamage", true)) {
            List<Block> blockList = event.blockList();
            while (blockList.size() > 0) {
                blockList.remove(0);
            }
        } else if ((event.getEntityType() == EntityType.FIREBALL || event.getEntityType() == EntityType.SMALL_FIREBALL) && GoldenApple.getInstanceMainConfig().getBoolean("modules.antigrief.noFireballBlockDamage", true)) {
            List<Block> blockList = event.blockList();
            while (blockList.size() > 0) {
                blockList.remove(0);
            }
        } else if (event.getEntityType() == EntityType.MINECART_TNT && GoldenApple.getInstanceMainConfig().getBoolean("modules.antigrief.noMinecartTnt", true)) {
            List<Block> blockList = event.blockList();
            while (blockList.size() > 0) {
                blockList.remove(0);
            }
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
}
