package com.bendude56.goldenapple.mail;

import java.util.List;
import java.util.logging.Level;

import org.bukkit.event.Event;
import org.bukkit.event.EventException;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.plugin.EventExecutor;
import org.bukkit.plugin.RegisteredListener;

import com.bendude56.goldenapple.GoldenApple;
import com.bendude56.goldenapple.PerformanceMonitor.PerformanceEvent;
import com.bendude56.goldenapple.User;

public class MailListener implements Listener, EventExecutor {
    private static MailListener listener;
    
    public static void startListening() {
        listener = new MailListener();
        listener.registerEvents();
    }
    
    public static void stopListening() {
        if (listener != null) {
            listener.unregisterEvents();
            listener = null;
        }
    }
    
    private void registerEvents() {
        PlayerJoinEvent.getHandlerList().register(new RegisteredListener(this, this, EventPriority.MONITOR, GoldenApple.getInstance(), true));
    }
    
    private void unregisterEvents() {
        PlayerJoinEvent.getHandlerList().unregister(this);
    }
    
    @Override
    public void execute(Listener listener, Event event) throws EventException {
        PerformanceEvent e = GoldenApple.getInstancePerformanceMonitor().createForEvent("Warp", event.getClass().getName());
        e.start();
        
        try {
            if (event instanceof PlayerJoinEvent) {
                playerJoin((PlayerJoinEvent) event);
            } else {
                GoldenApple.log(Level.WARNING, "Unrecognized event in MailListener: " + event.getClass().getName());
            }
        } finally {
            e.stop();
        }
    }
    
    private void playerJoin(PlayerJoinEvent event) {
        User user = User.getUser(event.getPlayer());
        List<MailMessageSent> messages = MailManager.getInstance().getMessages(user, true);
        
        if (messages.size() > 0) {
            user.sendLocalizedMessage("module.mail.notify.joinUnread", messages.size());
        }
    }
}
