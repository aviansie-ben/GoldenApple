package com.bendude56.goldenapple.warp;

import java.util.HashMap;
import java.util.logging.Level;

import org.bukkit.Location;
import org.bukkit.event.Event;
import org.bukkit.event.EventException;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDeathEvent;
import org.bukkit.event.entity.PlayerDeathEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.event.player.PlayerTeleportEvent;
import org.bukkit.event.player.PlayerTeleportEvent.TeleportCause;
import org.bukkit.plugin.EventExecutor;
import org.bukkit.plugin.RegisteredListener;

import com.bendude56.goldenapple.GoldenApple;
import com.bendude56.goldenapple.PerformanceMonitor.PerformanceEvent;
import com.bendude56.goldenapple.User;

public class WarpListener implements Listener, EventExecutor {
    public static HashMap<User, Location> backLocation = new HashMap<User, Location>();
    
    private static WarpListener listener;
    
    public static void startListening() {
        listener = new WarpListener();
        listener.registerEvents();
    }
    
    public static void stopListening() {
        if (listener != null) {
            listener.unregisterEvents();
            listener = null;
        }
    }
    
    private void registerEvents() {
        PlayerTeleportEvent.getHandlerList().register(new RegisteredListener(this, this, EventPriority.MONITOR, GoldenApple.getInstance(), true));
        EntityDeathEvent.getHandlerList().register(new RegisteredListener(this, this, EventPriority.MONITOR, GoldenApple.getInstance(), true));
        PlayerQuitEvent.getHandlerList().register(new RegisteredListener(this, this, EventPriority.LOWEST, GoldenApple.getInstance(), true));
    }
    
    private void unregisterEvents() {
        PlayerTeleportEvent.getHandlerList().unregister(this);
        EntityDeathEvent.getHandlerList().unregister(this);
        PlayerQuitEvent.getHandlerList().unregister(this);
    }
    
    @Override
    public void execute(Listener listener, Event event) throws EventException {
        PerformanceEvent e = GoldenApple.getInstancePerformanceMonitor().createForEvent("Warp", event.getClass().getName());
        e.start();
        
        try {
            if (event instanceof PlayerTeleportEvent) {
                playerTeleport((PlayerTeleportEvent) event);
            } else if (event instanceof PlayerDeathEvent) {
                playerDeath((PlayerDeathEvent) event);
            } else if (event instanceof EntityDeathEvent) {
                // Do nothing
            } else if (event instanceof PlayerQuitEvent) {
                backLocation.remove(User.getUser(((PlayerQuitEvent) event).getPlayer()));
            } else {
                GoldenApple.log(Level.WARNING, "Unrecognized event in WarpListener: " + event.getClass().getName());
            }
        } finally {
            e.stop();
        }
    }
    
    private void playerTeleport(PlayerTeleportEvent event) {
        if (event.getCause() != TeleportCause.UNKNOWN) {
            backLocation.put(User.getUser(event.getPlayer()), event.getFrom());
        }
    }
    
    private void playerDeath(PlayerDeathEvent event) {
        User user = User.getUser(event.getEntity());
        
        backLocation.put(user, event.getEntity().getLocation());
        
        int deathCooldown = WarpManager.getInstance().startDeathCooldown(user);
        
        if (deathCooldown > 0) {
            user.sendLocalizedMessage("module.warp.diedCooldown", deathCooldown);
        }
    }
}
