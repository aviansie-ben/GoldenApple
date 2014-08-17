package com.bendude56.goldenapple.select;

import java.util.logging.Level;

import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.event.Event;
import org.bukkit.event.EventException;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.plugin.EventExecutor;
import org.bukkit.plugin.RegisteredListener;

import com.bendude56.goldenapple.GoldenApple;
import com.bendude56.goldenapple.PerformanceMonitor.PerformanceEvent;
import com.bendude56.goldenapple.User;

public class SelectListener implements Listener, EventExecutor {
    
    private static SelectListener listener;
    
    public static void startListening() {
        listener = new SelectListener();
        listener.registerEvents();
    }
    
    public static void stopListening() {
        if (listener != null) {
            listener.unregisterEvents();
            listener = null;
        }
    }
    
    private void registerEvents() {
        PlayerInteractEvent.getHandlerList().register(new RegisteredListener(this, this, EventPriority.LOWEST, GoldenApple.getInstance(), true));
        PlayerQuitEvent.getHandlerList().register(new RegisteredListener(this, this, EventPriority.LOWEST, GoldenApple.getInstance(), true));
    }
    
    private void unregisterEvents() {
        PlayerInteractEvent.getHandlerList().unregister(this);
        PlayerQuitEvent.getHandlerList().unregister(this);
    }
    
    @Override
    public void execute(Listener listener, Event event) throws EventException {
        PerformanceEvent e = GoldenApple.getInstancePerformanceMonitor().createForEvent("Warp", event.getClass().getName());
        e.start();
        
        try {
            if (event instanceof PlayerInteractEvent) {
                playerInteract((PlayerInteractEvent) event);
            } else if (event instanceof PlayerQuitEvent) {
                playerQuit((PlayerQuitEvent) event);
            } else {
                GoldenApple.log(Level.WARNING, "Unrecognized event in SelectListener: " + event.getClass().getName());
            }
        } finally {
            e.stop();
        }
    }
    
    private void playerInteract(PlayerInteractEvent event) {
        if (SelectManager.getInstance().getSelectionProvider() instanceof SimpleSelectionProvider) {
            if (event.getItem() != null && event.getItem().getType() == Material.WOOD_AXE) {
                Location l = event.getClickedBlock().getLocation();
                User user = User.getUser(event.getPlayer());
                if (event.getAction() == Action.LEFT_CLICK_BLOCK) {
                    ((SimpleSelectionProvider) SelectManager.getInstance().getSelectionProvider()).setSelection1(user, l);
                    user.sendLocalizedMessage("module.select.update.set1", l.getBlockX() , l.getBlockY() , l.getBlockZ() );
                    event.setCancelled(true);
                } else if (event.getAction() == Action.RIGHT_CLICK_BLOCK) {
                    ((SimpleSelectionProvider) SelectManager.getInstance().getSelectionProvider()).setSelection2(user, event.getClickedBlock().getLocation());
                    user.sendLocalizedMessage("module.select.update.set2", l.getBlockX() , l.getBlockY() , l.getBlockZ() );
                    event.setCancelled(true);
                }
            }
        }
    }
    
    private void playerQuit(PlayerQuitEvent event) {
        if (SelectManager.getInstance().getSelectionProvider() instanceof SimpleSelectionProvider) {
            ((SimpleSelectionProvider) SelectManager.getInstance().getSelectionProvider()).clearSelection(User.getUser(event.getPlayer()));
        }
    }
}
