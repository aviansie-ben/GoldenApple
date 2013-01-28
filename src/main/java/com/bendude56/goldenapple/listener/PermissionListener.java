package com.bendude56.goldenapple.listener;

import java.util.logging.Level;

import org.bukkit.Bukkit;
import org.bukkit.event.Event;
import org.bukkit.event.EventException;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerLoginEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.event.player.PlayerLoginEvent.Result;
import org.bukkit.plugin.EventExecutor;
import org.bukkit.plugin.RegisteredListener;

import com.bendude56.goldenapple.GoldenApple;
import com.bendude56.goldenapple.User;
import com.bendude56.goldenapple.permissions.PermissionGroup;
import com.bendude56.goldenapple.permissions.PermissionUser;

public class PermissionListener implements Listener, EventExecutor {

	private static PermissionListener	listener;

	public static void startListening() {
		listener = new PermissionListener();
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
		PlayerQuitEvent.getHandlerList().register(new RegisteredListener(this, this, EventPriority.HIGHEST, GoldenApple.getInstance(), true));
	}

	private void unregisterEvents() {
		PlayerLoginEvent.getHandlerList().unregister(this);
		PlayerQuitEvent.getHandlerList().unregister(this);
	}

	@Override
	public void execute(Listener listener, Event event) throws EventException {
		if (event instanceof PlayerLoginEvent) {
			playerLogin((PlayerLoginEvent)event);
		} else if (event instanceof PlayerQuitEvent) {
			playerQuit((PlayerQuitEvent)event);
		} else {
			GoldenApple.log(Level.WARNING, "Unrecognized event in PermissionListener: " + event.getClass().getName());
		}
	}

	private void playerLogin(PlayerLoginEvent event) {
		GoldenApple instance = GoldenApple.getInstance();
		PermissionUser u = GoldenApple.getInstance().permissions.createUser(event.getPlayer().getName());
		
		for (String defaultGroup : instance.mainConfig.getStringList("modules.permissions.defaultGroups")) {
			PermissionGroup g = instance.permissions.getGroup(defaultGroup);
			if (g == null)
				continue;
			
			g.addUser(u);
		}
		
		if (event.getPlayer().isOp()) {
			for (String defaultGroup : instance.mainConfig.getStringList("modules.permissions.opGroups")) {
				PermissionGroup g = instance.permissions.getGroup(defaultGroup);
				if (g == null)
					continue;
				
				if (!g.isMember(u, true)) {
					g.addUser(u);
				}
			}
		}
		
		for (String dev : GoldenApple.devs) {
			if (dev.equals(u.getName())) {
				for (String defaultGroup : instance.mainConfig.getStringList("modules.permissions.devGroups")) {
					PermissionGroup g = instance.permissions.getGroup(defaultGroup);
					if (g == null)
						continue;
					
					if (!g.isMember(u, true)) {
						g.addUser(u);
					}
				}
				break;
			}
		}
		
		if (!instance.mainConfig.getString("modules.permissions.reqGroup").equals("")) {
			PermissionGroup reqGroup = instance.permissions.getGroup(GoldenApple.getInstance().mainConfig.getString("modules.permissions.reqGroup"));
			if (reqGroup == null) {
				GoldenApple.log(Level.WARNING, "Failed to find required group '" + instance.mainConfig.getString("modules.permissions.reqGroup") + "'. Only allowing ops to join...");
				if (!Bukkit.getOfflinePlayer(u.getName()).isOp()) {
					event.disallow(Result.KICK_WHITELIST, "You aren't allowed to connect. Contact an administrator for further details.");
				}
			} else {
				if (!reqGroup.isMember(u, false)) {
					event.disallow(Result.KICK_WHITELIST, "You aren't allowed to connect. Contact an administrator for further details.");
				}
			}
		}
	}
	
	private void playerQuit(PlayerQuitEvent event) {
		User.unloadUser(User.getUser(event.getPlayer()));
	}
}
