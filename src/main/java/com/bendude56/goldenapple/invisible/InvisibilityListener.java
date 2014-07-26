package com.bendude56.goldenapple.invisible;

import java.util.logging.Level;

import org.bukkit.entity.Player;
import org.bukkit.event.Cancellable;
import org.bukkit.event.Event;
import org.bukkit.event.EventException;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.block.BlockExpEvent;
import org.bukkit.event.block.BlockPlaceEvent;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.entity.EntityTargetEvent;
import org.bukkit.event.entity.ProjectileLaunchEvent;
import org.bukkit.event.hanging.HangingBreakByEntityEvent;
import org.bukkit.event.hanging.HangingBreakEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerPickupItemEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.plugin.EventExecutor;
import org.bukkit.plugin.RegisteredListener;

import com.bendude56.goldenapple.GoldenApple;
import com.bendude56.goldenapple.User;
import com.bendude56.goldenapple.PerformanceMonitor.PerformanceEvent;

public class InvisibilityListener implements Listener, EventExecutor {
    
    private static InvisibilityListener listener;
    
    public static void startListening() {
        listener = new InvisibilityListener();
        listener.registerEvents();
    }
    
    public static void stopListening() {
        if (listener != null) {
            listener.unregisterEvents();
            listener = null;
        }
    }
    
    private void registerEvents() {
        PlayerJoinEvent.getHandlerList().register(new RegisteredListener(this, this, EventPriority.LOWEST, GoldenApple.getInstance(), true));
        PlayerQuitEvent.getHandlerList().register(new RegisteredListener(this, this, EventPriority.LOWEST, GoldenApple.getInstance(), true));
        PlayerInteractEvent.getHandlerList().register(new RegisteredListener(this, this, EventPriority.LOWEST, GoldenApple.getInstance(), true));
        BlockBreakEvent.getHandlerList().register(new RegisteredListener(this, this, EventPriority.LOWEST, GoldenApple.getInstance(), true));
        BlockPlaceEvent.getHandlerList().register(new RegisteredListener(this, this, EventPriority.LOWEST, GoldenApple.getInstance(), true));
        EntityTargetEvent.getHandlerList().register(new RegisteredListener(this, this, EventPriority.LOWEST, GoldenApple.getInstance(), true));
        EntityDamageEvent.getHandlerList().register(new RegisteredListener(this, this, EventPriority.LOWEST, GoldenApple.getInstance(), true));
        HangingBreakEvent.getHandlerList().register(new RegisteredListener(this, this, EventPriority.LOWEST, GoldenApple.getInstance(), true));
        PlayerPickupItemEvent.getHandlerList().register(new RegisteredListener(this, this, EventPriority.LOWEST, GoldenApple.getInstance(), true));
        ProjectileLaunchEvent.getHandlerList().register(new RegisteredListener(this, this, EventPriority.LOWEST, GoldenApple.getInstance(), true));
    }
    
    private void unregisterEvents() {
        PlayerJoinEvent.getHandlerList().unregister(this);
        PlayerQuitEvent.getHandlerList().unregister(this);
        PlayerInteractEvent.getHandlerList().unregister(this);
        BlockBreakEvent.getHandlerList().unregister(this);
        BlockPlaceEvent.getHandlerList().unregister(this);
        EntityTargetEvent.getHandlerList().unregister(this);
        EntityDamageEvent.getHandlerList().unregister(this);
        HangingBreakEvent.getHandlerList().unregister(this);
        PlayerPickupItemEvent.getHandlerList().unregister(this);
        ProjectileLaunchEvent.getHandlerList().unregister(this);
    }
    
    @Override
    public void execute(Listener listener, Event event) throws EventException {
        PerformanceEvent e = GoldenApple.getInstancePerformanceMonitor().createForEvent("Invisible", event.getClass().getName());
        e.start();
        
        try {
            if (event instanceof PlayerJoinEvent) {
                playerJoin((PlayerJoinEvent) event);
            } else if (event instanceof PlayerQuitEvent) {
                playerQuit((PlayerQuitEvent) event);
            } else if (event instanceof PlayerInteractEvent) {
                playerInteract((PlayerInteractEvent) event);
            } else if (event instanceof BlockBreakEvent) {
                maybeCancel((Cancellable) event, User.getUser(((BlockBreakEvent) event).getPlayer()), true);
            } else if (event instanceof BlockPlaceEvent) {
                maybeCancel((Cancellable) event, User.getUser(((BlockPlaceEvent) event).getPlayer()), true);
            } else if (event instanceof EntityTargetEvent) {
                entityTarget((EntityTargetEvent) event);
            } else if (event instanceof EntityDamageEvent) {
                entityDamage((EntityDamageEvent) event);
            } else if (event instanceof HangingBreakEvent) {
                hangingBreak((HangingBreakEvent) event);
            } else if (event instanceof PlayerPickupItemEvent) {
                maybeCancel((Cancellable) event, User.getUser(((PlayerPickupItemEvent) event).getPlayer()), false);
            } else if (event instanceof ProjectileLaunchEvent) {
                projectileLaunch((ProjectileLaunchEvent) event);
            } else if (event instanceof BlockExpEvent) {
                // Do nothing
            } else {
                GoldenApple.log(Level.WARNING, "Unrecognized event in InvisibilityListener: " + event.getClass().getName());
            }
        } finally {
            e.stop();
        }
    }
    
    private void playerJoin(PlayerJoinEvent event) {
        User user = User.getUser(event.getPlayer());
        
        InvisibilityManager.getInstance().setAllSeeing(user, user.hasPermission(InvisibilityManager.seeVanishedPermission));
    }
    
    private void playerQuit(PlayerQuitEvent event) {
        User user = User.getUser(event.getPlayer());
        
        InvisibilityManager.getInstance().setInvisible(user, false);
        InvisibilityManager.getInstance().setInteractionEnabled(user, true);
        InvisibilityManager.getInstance().setAllSeeing(user, false);
    }
    
    private void playerInteract(PlayerInteractEvent event) {
        maybeCancel((Cancellable) event, User.getUser(((PlayerInteractEvent) event).getPlayer()), event.getAction() != Action.PHYSICAL);
    }
    
    private void entityTarget(EntityTargetEvent event) {
        if (event.getTarget() instanceof Player) {
            maybeCancel(event, User.getUser((Player) event.getTarget()), false);
        }
    }
    
    private void entityDamage(EntityDamageEvent event) {
        if (event.getEntity() instanceof Player) {
            maybeCancel(event, User.getUser((Player) event.getEntity()), false);
        }
        
        if (event instanceof EntityDamageByEntityEvent && ((EntityDamageByEntityEvent) event).getDamager() instanceof Player) {
            maybeCancel(event, User.getUser((Player) ((EntityDamageByEntityEvent) event).getDamager()), true);
        }
    }
    
    private void hangingBreak(HangingBreakEvent event) {
        if (event instanceof HangingBreakByEntityEvent && ((HangingBreakByEntityEvent) event).getRemover() instanceof Player) {
            maybeCancel(event, User.getUser((Player) ((HangingBreakByEntityEvent) event).getRemover()), true);
        }
    }
    
    private void projectileLaunch(ProjectileLaunchEvent event) {
        if (event.getEntity().getShooter() instanceof Player) {
            maybeCancel(event, User.getUser((Player) event.getEntity().getShooter()), true);
        }
    }
    
    private void maybeCancel(Cancellable event, User user, boolean loud) {
        if (!InvisibilityManager.getInstance().isInteractionEnabled(user)) {
            event.setCancelled(true);
            
            if (loud) {
                user.sendLocalizedMessage("error.poof.noInteract");
            }
        }
    }
}
