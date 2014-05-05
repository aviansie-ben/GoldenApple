package com.bendude56.goldenapple.chat;

import java.util.logging.Level;

import org.bukkit.event.Event;
import org.bukkit.event.EventException;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.AsyncPlayerChatEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.plugin.EventExecutor;
import org.bukkit.plugin.RegisteredListener;

import com.bendude56.goldenapple.GoldenApple;
import com.bendude56.goldenapple.User;
import com.bendude56.goldenapple.PerformanceMonitor.PerformanceEvent;
import com.bendude56.goldenapple.chat.ChatChannel;
import com.bendude56.goldenapple.chat.ChatManager;

public class ChatListener implements Listener, EventExecutor {
	private static ChatListener listener = null;
	
	public static void startListening() {
		listener = new ChatListener();
		listener.registerEvents();
	}
	
	public static void stopListening() {
		if (listener != null) {
			listener.unregisterEvents();
			listener = null;
		}
	}
	
	public void registerEvents() {
		AsyncPlayerChatEvent.getHandlerList().register(new RegisteredListener(this, this, EventPriority.HIGH, GoldenApple.getInstance(), true));
		PlayerJoinEvent.getHandlerList().register(new RegisteredListener(this, this, EventPriority.HIGH, GoldenApple.getInstance(), true));
		PlayerQuitEvent.getHandlerList().register(new RegisteredListener(this, this, EventPriority.HIGH, GoldenApple.getInstance(), true));
	}
	
	public void unregisterEvents() {
		AsyncPlayerChatEvent.getHandlerList().unregister(this);
		PlayerJoinEvent.getHandlerList().unregister(this);
		PlayerQuitEvent.getHandlerList().unregister(this);
	}
	
	@Override
	public void execute(Listener listener, Event event) throws EventException {
		PerformanceEvent e = GoldenApple.getInstancePerformanceMonitor().createForEvent("Chat", event.getClass().getName());
		e.start();
		
		try {
			if (event instanceof AsyncPlayerChatEvent) {
				asyncPlayerChat((AsyncPlayerChatEvent) event);
			} else if (event instanceof PlayerJoinEvent) {
				playerJoin((PlayerJoinEvent)event);
			} else if (event instanceof PlayerQuitEvent) {
				playerQuit((PlayerQuitEvent)event);
			} else {
				GoldenApple.log(Level.WARNING, "Unrecognized event in ChatListener: " + event.getClass().getName());
			}
		} finally {
			e.stop();
		}
	}
	
	private void asyncPlayerChat(AsyncPlayerChatEvent event) {
		User u = User.getUser(event.getPlayer());
		ChatChannel channel = ChatManager.getInstance().getActiveChannel(u);
		if (channel == null) {
			u.sendLocalizedMessage("error.channel.notInChannel");
		} else {
			synchronized (channel) {
				channel.sendMessage(u, event.getMessage());
			}
		}
		event.setCancelled(true);
	}
	
	private void playerJoin(PlayerJoinEvent event) {
		User user = User.getUser(event.getPlayer());
		
		ChatManager.getInstance().tryJoinChannel(user, ChatManager.getInstance().getDefaultChannel(), false);
		
		if (user.hasPermission(ChatManager.tellSpyPermission)) {
		    ChatManager.getInstance().setTellSpyStatus(user, true);
		}
	}
	
	private void playerQuit(PlayerQuitEvent event) {
	    User user = User.getUser(event.getPlayer());
	    
		ChatManager.getInstance().leaveChannel(user, false);
		ChatManager.getInstance().setTellSpyStatus(user, false);
		ChatManager.getInstance().removeReplyEntry(user);
		ChatManager.getInstance().setAfkStatus(user, false, false);
	}
}
