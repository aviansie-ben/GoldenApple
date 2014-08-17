package com.bendude56.goldenapple.request;

import java.util.logging.Level;

import org.bukkit.event.Event;
import org.bukkit.event.EventException;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.plugin.EventExecutor;
import org.bukkit.plugin.RegisteredListener;

import com.bendude56.goldenapple.GoldenApple;
import com.bendude56.goldenapple.PerformanceMonitor.PerformanceEvent;
import com.bendude56.goldenapple.User;
import com.bendude56.goldenapple.request.RequestManager.AutoAssignUserEvent;

public class RequestListener implements Listener, EventExecutor {
	private static RequestListener	listener;

	public static void startListening() {
		listener = new RequestListener();
		listener.registerEvents();
	}

	public static void stopListening() {
		if (listener != null) {
			listener.unregisterEvents();
			listener = null;
		}
	}

	private void registerEvents() {
	    PlayerJoinEvent.getHandlerList().register(new RegisteredListener(this, this, EventPriority.NORMAL, GoldenApple.getInstance(), true));
	    PlayerQuitEvent.getHandlerList().register(new RegisteredListener(this, this, EventPriority.NORMAL, GoldenApple.getInstance(), true));
	}

	private void unregisterEvents() {
	    PlayerJoinEvent.getHandlerList().unregister(this);
	    PlayerQuitEvent.getHandlerList().unregister(this);
	}

	@Override
	public void execute(Listener listener, Event event) throws EventException {
		PerformanceEvent e = GoldenApple.getInstancePerformanceMonitor().createForEvent("Request", event.getClass().getName());
		e.start();
		
		try {
			if (event instanceof PlayerJoinEvent) {
				onJoin((PlayerJoinEvent) event);
			} else if (event instanceof PlayerQuitEvent) {
			    onQuit((PlayerQuitEvent) event);
			} else {
				GoldenApple.log(Level.WARNING, "Unrecognized event in RequestListener: " + event.getClass().getName());
			}
		} finally {
			e.stop();
		}
	}

    private void onJoin(PlayerJoinEvent event) {
	    RequestManager manager = RequestManager.getInstance();
	    User user = User.getUser(event.getPlayer());
	    
	    for (RequestQueue queue : manager.getAllRequestQueues()) {
	        if (queue.canReceive(user) && queue.isReceiving(user)) {
	            queue.addToOnlineReceivers(user);
	            if (queue.getUnassignedRequests(false, false).size() > 0) {
	                user.sendLocalizedMessage("module.request.notify.unassignedLogin", queue.getName());
	            }
	            
	            if (queue.isAutoAssign(user)) {
	                queue.addToAutoAssignQueue(user);
	            }
	        }
	    }
	    
	    manager.notifyAutoAssignUserEvent(user, AutoAssignUserEvent.LOGIN);
	}
    
    private void onQuit(PlayerQuitEvent event) {
        RequestManager manager = RequestManager.getInstance();
        User user = User.getUser(event.getPlayer());
        
        for (RequestQueue queue : manager.getAllRequestQueues()) {
            if (queue.isReceiving(user)) {
                queue.removeFromOnlineReceivers(user);
            }
            
            if (queue.isAutoAssign(user)) {
                queue.removeFromAutoAssignQueue(user);
            }
        }
        
        manager.notifyAutoAssignUserEvent(user, AutoAssignUserEvent.LOGOUT);
    }
}
