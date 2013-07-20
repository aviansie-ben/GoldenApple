package com.bendude56.goldenapple.listener;

import java.util.logging.Level;

import org.bukkit.Bukkit;
import org.bukkit.event.Event;
import org.bukkit.event.EventException;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerLoginEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.event.player.PlayerLoginEvent.Result;
import org.bukkit.plugin.EventExecutor;
import org.bukkit.plugin.RegisteredListener;

import com.bendude56.goldenapple.GoldenApple;
import com.bendude56.goldenapple.User;
import com.bendude56.goldenapple.permissions.IPermissionGroup;
import com.bendude56.goldenapple.permissions.IPermissionUser;
import com.bendude56.goldenapple.permissions.PermissionManager;

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
		PlayerLoginEvent.getHandlerList().register(new RegisteredListener(this, this, EventPriority.LOWEST, GoldenApple.getInstance(), true));
		PlayerJoinEvent.getHandlerList().register(new RegisteredListener(this, this, EventPriority.LOWEST, GoldenApple.getInstance(), true));
		PlayerQuitEvent.getHandlerList().register(new RegisteredListener(this, this, EventPriority.HIGHEST, GoldenApple.getInstance(), true));
	}

	private void unregisterEvents() {
		PlayerLoginEvent.getHandlerList().unregister(this);
		PlayerQuitEvent.getHandlerList().unregister(this);
	}

	@Override
	public void execute(Listener listener, Event event) throws EventException {
		if (event instanceof PlayerJoinEvent) {
			playerJoin((PlayerJoinEvent)event);
		} else if (event instanceof PlayerLoginEvent) {
			playerLogin((PlayerLoginEvent)event);
		} else if (event instanceof PlayerQuitEvent) {
			playerQuit((PlayerQuitEvent)event);
		} else {
			GoldenApple.log(Level.WARNING, "Unrecognized event in PermissionListener: " + event.getClass().getName());
		}
	}
	
	private void playerJoin(PlayerJoinEvent event) {
		User.getUser(event.getPlayer()).setHandle(event.getPlayer());
	}

	private void playerLogin(PlayerLoginEvent event) {
		IPermissionUser u = PermissionManager.getInstance().createUser(event.getPlayer().getName());
		
		for (String defaultGroup : GoldenApple.getInstanceMainConfig().getStringList("modules.permissions.defaultGroups")) {
			IPermissionGroup g = PermissionManager.getInstance().getGroup(defaultGroup);
			if (g == null)
				continue;
			
			g.addUser(u);
		}
		
		if (event.getPlayer().isOp()) {
			for (String defaultGroup : GoldenApple.getInstanceMainConfig().getStringList("modules.permissions.opGroups")) {
				IPermissionGroup g = PermissionManager.getInstance().getGroup(defaultGroup);
				if (g == null)
					continue;
				
				if (!g.isMember(u, true)) {
					g.addUser(u);
				}
			}
		}
		
		for (String dev : PermissionManager.devs) {
			if (dev.equals(u.getName())) {
				for (String defaultGroup : GoldenApple.getInstanceMainConfig().getStringList("modules.permissions.devGroups")) {
					IPermissionGroup g = PermissionManager.getInstance().getGroup(defaultGroup);
					if (g == null)
						continue;
					
					if (!g.isMember(u, true)) {
						g.addUser(u);
					}
				}
				break;
			}
		}
		
		if (!GoldenApple.getInstanceMainConfig().getString("modules.permissions.reqGroup").equals("")) {
			IPermissionGroup reqGroup = PermissionManager.getInstance().getGroup(GoldenApple.getInstanceMainConfig().getString("modules.permissions.reqGroup"));
			if (reqGroup == null) {
				GoldenApple.log(Level.WARNING, "Failed to find required group '" + GoldenApple.getInstanceMainConfig().getString("modules.permissions.reqGroup") + "'. Only allowing ops to join...");
				if (!Bukkit.getOfflinePlayer(u.getName()).isOp()) {
					event.disallow(Result.KICK_WHITELIST, "You aren't allowed to connect. Contact an administrator for further details.");
				}
			} else {
				if (!reqGroup.isMember(u, false)) {
					event.disallow(Result.KICK_WHITELIST, "You aren't allowed to connect. Contact an administrator for further details.");
				}
			}
		}
		
		// Flush the user permissions to the Bukkit permissions interface
		if (event.getResult() == Result.ALLOWED)
			User.getUser(event.getPlayer());
	}
	
	private void playerQuit(PlayerQuitEvent event) {
		User.unloadUser(User.getUser(event.getPlayer()));
	}
}
