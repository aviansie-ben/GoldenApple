package com.bendude56.goldenapple.punish;

import java.util.HashMap;
import java.util.logging.Level;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.event.Event;
import org.bukkit.event.EventException;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.AsyncPlayerChatEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerKickEvent;
import org.bukkit.event.player.PlayerLoginEvent;
import org.bukkit.event.player.PlayerMoveEvent;
import org.bukkit.event.player.PlayerTeleportEvent;
import org.bukkit.event.player.PlayerLoginEvent.Result;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.plugin.EventExecutor;
import org.bukkit.plugin.RegisteredListener;

import com.bendude56.goldenapple.GoldenApple;
import com.bendude56.goldenapple.User;
import com.bendude56.goldenapple.PerformanceMonitor.PerformanceEvent;
import com.bendude56.goldenapple.punish.Punishment;
import com.bendude56.goldenapple.punish.PunishmentManager;
import com.bendude56.goldenapple.warp.WarpListener;

public class PunishmentListener implements Listener, EventExecutor {
	public static HashMap<User, Location> backLocation = new HashMap<User, Location>();
	
	private static PunishmentListener	listener;

	public static void startListening() {
		listener = new PunishmentListener();
		listener.registerEvents();
	}

	public static void stopListening() {
		if (listener != null) {
			listener.unregisterEvents();
			listener = null;
		}
	}

	private void registerEvents() {
		PlayerLoginEvent.getHandlerList().register(new RegisteredListener(this, this, EventPriority.NORMAL, GoldenApple.getInstance(), true));
		PlayerJoinEvent.getHandlerList().register(new RegisteredListener(this, this, EventPriority.NORMAL, GoldenApple.getInstance(), true));
		PlayerQuitEvent.getHandlerList().register(new RegisteredListener(this, this, EventPriority.NORMAL, GoldenApple.getInstance(), true));
		PlayerKickEvent.getHandlerList().register(new RegisteredListener(this, this, EventPriority.NORMAL, GoldenApple.getInstance(), true));
		AsyncPlayerChatEvent.getHandlerList().register(new RegisteredListener(this, this, EventPriority.LOWEST, GoldenApple.getInstance(), true));
		PlayerTeleportEvent.getHandlerList().register(new RegisteredListener(this, this, EventPriority.LOWEST, GoldenApple.getInstance(), true));
	}

	private void unregisterEvents() {
		PlayerLoginEvent.getHandlerList().unregister(this);
		PlayerJoinEvent.getHandlerList().unregister(this);
		PlayerQuitEvent.getHandlerList().unregister(this);
		PlayerKickEvent.getHandlerList().unregister(this);
		AsyncPlayerChatEvent.getHandlerList().unregister(this);
		PlayerTeleportEvent.getHandlerList().unregister(this);
	}
	
	private HashMap<User, Location> antiMineChat = new HashMap<User, Location>();

	@Override
	public void execute(Listener listener, Event event) throws EventException {
		PerformanceEvent e = GoldenApple.getInstancePerformanceMonitor().createForEvent("Punish", event.getClass().getName());
		e.start();
		
		try {
			if (event instanceof PlayerLoginEvent) {
				playerLogin((PlayerLoginEvent)event);
			} else if (event instanceof PlayerJoinEvent) {
                playerJoin((PlayerJoinEvent)event);
			} else if (event instanceof PlayerQuitEvent) {
				playerQuit((PlayerQuitEvent)event);
			} else if (event instanceof PlayerKickEvent) {
                playerKick((PlayerKickEvent)event);
			} else if (event instanceof AsyncPlayerChatEvent) {
                asyncPlayerChat((AsyncPlayerChatEvent)event);
			} else if (event instanceof PlayerTeleportEvent) {
			    playerTeleport((PlayerTeleportEvent)event);
			} else {
				GoldenApple.log(Level.WARNING, "Unrecognized event in PunishmentListener: " + event.getClass().getName());
			}
		} finally {
			e.stop();
		}
	}

	private void playerLogin(PlayerLoginEvent event) {
		GoldenApple instance = GoldenApple.getInstance();
		User u = User.getUser(event.getPlayer());
		
		PunishmentManager.getInstance().loadIntoCache(u);
		Punishment ban = PunishmentManager.getInstance().getActivePunishment(u, SimplePunishmentBan.class);
		
		if (ban != null) {
			if (ban.isPermanent()) {
				String msg = instance.getLocalizationManager().processMessageDefaultLocale("general.ban.permaKick", ban.getAdmin().getName());
				msg += "\n" + ban.getReason();
				msg += "\n" + GoldenApple.getInstanceMainConfig().getString("modules.punish.banAppealMessage", "Contact an administrator to dispute this ban.");
				event.setResult(Result.KICK_BANNED);
				event.setKickMessage(msg);
			} else {
				String msg = instance.getLocalizationManager().processMessageDefaultLocale("general.ban.tempKick", ban.getRemainingDuration().toString(), ban.getAdmin().getName());
				msg += "\n" + ban.getReason();
				msg += "\n" + GoldenApple.getInstanceMainConfig().getString("modules.punish.banAppealMessage", "Contact an administrator to dispute this ban.");
				event.setResult(Result.KICK_BANNED);
				event.setKickMessage(msg);
			}
			User.unloadUser(u);
		}
	}
	
	private void playerJoin(PlayerJoinEvent event) {
	    final User user = User.getUser(event.getPlayer());
	    final boolean flying = user.getPlayerHandle().isFlying();
	    
	    if (GoldenApple.getInstanceMainConfig().getBoolean("modules.punish.blockMinechat", true)) {
	        antiMineChat.put(user, user.getPlayerHandle().getLocation());
	        
	        if (flying) user.getPlayerHandle().setFlying(false);
	        
	        user.getPlayerHandle().teleport(new Location(user.getPlayerHandle().getWorld(), 0, 1000, 0));
	        WarpListener.backLocation.remove(user);
	        
	        if (Bukkit.getScheduler().scheduleSyncDelayedTask(GoldenApple.getInstance(), new Runnable() {
	            private int retries = 0;
	            
                @Override
                public void run() {
                    if (!antiMineChat.containsKey(user)) return;
                        
                    if (user.getPlayerHandle().getLocation().getY() >= 1000) {
                        retries++;
                        
                        if (retries <= 12) {
                            if (Bukkit.getScheduler().scheduleSyncDelayedTask(GoldenApple.getInstance(), this, 5) == -1) {
                                if (flying) user.getPlayerHandle().setFlying(true);
                                user.getPlayerHandle().teleport(antiMineChat.remove(user));
                                user.getPlayerHandle().kickPlayer("Chat-only clients are not allowed!");
                            }
                        } else {
                            if (flying) user.getPlayerHandle().setFlying(true);
                            user.getPlayerHandle().teleport(antiMineChat.remove(user));
                            user.getPlayerHandle().kickPlayer("Chat-only clients are not allowed!");
                        }
                    } else {
                        if (flying) user.getPlayerHandle().setFlying(true);
                        user.getPlayerHandle().teleport(antiMineChat.remove(user));
                        WarpListener.backLocation.remove(user);
                    }
                }
	        }, 1) == -1) {
	            if (flying) user.getPlayerHandle().setFlying(true);
	            user.getPlayerHandle().teleport(antiMineChat.remove(user));
	            WarpListener.backLocation.remove(user);
	        }
	    }
	}
	
	private void playerQuit(PlayerQuitEvent event) {
	    User user = User.getUser(event.getPlayer());
	    
		if (antiMineChat.containsKey(user)) {
		    user.getPlayerHandle().teleport(antiMineChat.get(user));
		    antiMineChat.remove(user);
		}
	}
	
	private void playerKick(PlayerKickEvent event) {
	    User user = User.getUser(event.getPlayer());
        
        if (antiMineChat.containsKey(user)) {
            user.getPlayerHandle().teleport(antiMineChat.get(user));
            antiMineChat.remove(user);
        }
	}
	
	private void asyncPlayerChat(AsyncPlayerChatEvent event) {
	    if (antiMineChat.containsKey(User.getUser(event.getPlayer()))) {
	        event.setCancelled(true);
	    }
	}
	
	private void playerTeleport(PlayerTeleportEvent event) {
	    if (antiMineChat.containsKey(User.getUser(event.getPlayer())) && event.getTo().getY() != 1000) {
	        event.setCancelled(true);
	    }
	}
}
