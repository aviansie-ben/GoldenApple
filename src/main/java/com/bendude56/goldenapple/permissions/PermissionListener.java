package com.bendude56.goldenapple.permissions;

import java.util.logging.Level;

import org.bukkit.event.Event;
import org.bukkit.event.EventException;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerLoginEvent;
import org.bukkit.event.player.PlayerLoginEvent.Result;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.plugin.EventExecutor;
import org.bukkit.plugin.RegisteredListener;

import com.bendude56.goldenapple.GoldenApple;
import com.bendude56.goldenapple.PerformanceMonitor.PerformanceEvent;
import com.bendude56.goldenapple.User;

public class PermissionListener implements Listener, EventExecutor {
    
    private static PermissionListener listener;
    
    public static void startListening() {
        PermissionListener.listener = new PermissionListener();
        PermissionListener.listener.registerEvents();
    }
    
    public static void stopListening() {
        if (PermissionListener.listener != null) {
            PermissionListener.listener.unregisterEvents();
            PermissionListener.listener = null;
        }
    }
    
    private void registerEvents() {
        PlayerLoginEvent.getHandlerList().register(new RegisteredListener(this, this, EventPriority.LOWEST, GoldenApple.getInstance(), true));
        PlayerJoinEvent.getHandlerList().register(new RegisteredListener(this, this, EventPriority.LOWEST, GoldenApple.getInstance(), true));
        PlayerQuitEvent.getHandlerList().register(new RegisteredListener(this, this, EventPriority.HIGHEST, GoldenApple.getInstance(), true));
    }
    
    private void unregisterEvents() {
        PlayerLoginEvent.getHandlerList().unregister(this);
        PlayerJoinEvent.getHandlerList().unregister(this);
        PlayerQuitEvent.getHandlerList().unregister(this);
    }
    
    @Override
    public void execute(Listener listener, Event event) throws EventException {
        PerformanceEvent e = GoldenApple.getInstancePerformanceMonitor().createForEvent("Permissions", event.getClass().getName());
        e.start();
        
        try {
            if (event instanceof PlayerJoinEvent) {
                playerJoin((PlayerJoinEvent) event);
            } else if (event instanceof PlayerLoginEvent) {
                playerLogin((PlayerLoginEvent) event);
            } else if (event instanceof PlayerQuitEvent) {
                playerQuit((PlayerQuitEvent) event);
            } else {
                GoldenApple.log(Level.WARNING, "Unrecognized event in PermissionListener: " + event.getClass().getName());
            }
        } finally {
            e.stop();
        }
    }
    
    private void playerJoin(PlayerJoinEvent event) {
        User.getUser(event.getPlayer()).setHandle(event.getPlayer());
    }
    
    private void playerLogin(PlayerLoginEvent event) {
        IPermissionUser u;
        
        try {
            u = PermissionManager.getInstance().createUser(event.getPlayer().getName(), event.getPlayer().getUniqueId());
        } catch (DuplicateNameException e) {
            event.disallow(Result.KICK_OTHER, "You have the same name as another player! Change your name or contact an administrator!");
            return;
        } catch (Exception e) {
            GoldenApple.log(Level.SEVERE, "Failed to create user profile for user " + event.getPlayer().getName() + ":");
            GoldenApple.log(Level.SEVERE, e);
            
            event.disallow(Result.KICK_OTHER, "Failed to create user profile! Contact an administrator!");
            return;
        }
        
        if (event.getPlayer().isOp()) {
            PermissionManager.getInstance().addToOpGroups(u);
        }
        
        if (PermissionManager.getInstance().isDev(u)) {
            PermissionManager.getInstance().addToDevGroups(u);
        }
        
        if (!PermissionManager.getInstance().canLogin(u)) {
            event.disallow(Result.KICK_WHITELIST, "You aren't allowed to connect. Contact an administrator for further details.");
        }
        
        // Flush the user permissions to the Bukkit permissions interface
        if (event.getResult() == Result.ALLOWED) {
            User.getUser(event.getPlayer());
        }
    }
    
    private void playerQuit(PlayerQuitEvent event) {
        User.unloadUser(User.getUser(event.getPlayer()));
    }
}
