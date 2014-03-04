package com.bendude56.goldenapple.punish;

import java.util.HashMap;
import java.util.logging.Level;

import org.bukkit.Location;
import org.bukkit.event.Event;
import org.bukkit.event.EventException;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerLoginEvent;
import org.bukkit.event.player.PlayerLoginEvent.Result;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.plugin.EventExecutor;
import org.bukkit.plugin.RegisteredListener;

import com.bendude56.goldenapple.GoldenApple;
import com.bendude56.goldenapple.User;
import com.bendude56.goldenapple.PerformanceMonitor.PerformanceEvent;
import com.bendude56.goldenapple.punish.Punishment;
import com.bendude56.goldenapple.punish.PunishmentManager;

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
		PlayerQuitEvent.getHandlerList().register(new RegisteredListener(this, this, EventPriority.NORMAL, GoldenApple.getInstance(), true));
	}

	private void unregisterEvents() {
		PlayerLoginEvent.getHandlerList().unregister(this);
		PlayerQuitEvent.getHandlerList().unregister(this);
	}

	@Override
	public void execute(Listener listener, Event event) throws EventException {
		PerformanceEvent e = GoldenApple.getInstancePerformanceMonitor().createForEvent("Punish", event.getClass().getName());
		e.start();
		
		try {
			if (event instanceof PlayerLoginEvent) {
				playerLogin((PlayerLoginEvent)event);
			} else if (event instanceof PlayerQuitEvent) {
				playerQuit((PlayerQuitEvent)event);
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
				msg += "\n" + GoldenApple.getInstanceMainConfig().getString("banAppealMessage", "Contact an administrator to dispute this ban.");
				event.setResult(Result.KICK_BANNED);
				event.setKickMessage(msg);
			} else {
				String msg = instance.getLocalizationManager().processMessageDefaultLocale("general.ban.tempKick", ban.getRemainingDuration().toString(), ban.getAdmin().getName());
				msg += "\n" + ban.getReason();
				msg += "\n" + GoldenApple.getInstanceMainConfig().getString("banAppealMessage", "Contact an administrator to dispute this ban.");
				event.setResult(Result.KICK_BANNED);
				event.setKickMessage(msg);
			}
			User.unloadUser(u);
		}
	}
	
	private void playerQuit(PlayerQuitEvent event) {
		
	}
}