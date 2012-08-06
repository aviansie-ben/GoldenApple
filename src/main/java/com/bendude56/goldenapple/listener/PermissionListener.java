package com.bendude56.goldenapple.listener;

import java.util.logging.Level;

import org.bukkit.Bukkit;
import org.bukkit.event.Event;
import org.bukkit.event.EventException;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerLoginEvent;
import org.bukkit.event.player.PlayerLoginEvent.Result;
import org.bukkit.plugin.EventExecutor;
import org.bukkit.plugin.RegisteredListener;

import com.bendude56.goldenapple.GoldenApple;
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
	}

	private void unregisterEvents() {
		PlayerLoginEvent.getHandlerList().unregister(this);
	}

	@Override
	public void execute(Listener listener, Event event) throws EventException {
		if (event instanceof PlayerLoginEvent) {
			playerLogin((PlayerLoginEvent)event);
		} else {
			GoldenApple.log(Level.WARNING, "Unrecognized event in PermissionListener: " + event.getClass().getName());
		}
	}

	private void playerLogin(PlayerLoginEvent event) {
		PermissionUser u = GoldenApple.getInstance().permissions.createUser(event.getPlayer().getName());
		if (GoldenApple.getInstance().mainConfig.getString("modules.permissions.reqGroup") != "") {
			PermissionGroup reqGroup = GoldenApple.getInstance().permissions.getGroup(GoldenApple.getInstance().mainConfig.getString("modules.permissions.reqGroup"));
			if (reqGroup == null) {
				GoldenApple.log(Level.WARNING, "Failed to find required group '" + GoldenApple.getInstance().mainConfig.getString("modules.permissions.reqGroup") + "'. Only allowing ops to join...");
				if (!Bukkit.getOfflinePlayer(u.getName()).isOp()) {
					event.disallow(Result.KICK_WHITELIST, "You aren't allowed to connect. Contact an administrator for further details.");
				}
			} else {
				if (!reqGroup.getMembers().contains(u.getId())) {
					event.disallow(Result.KICK_WHITELIST, "You aren't allowed to connect. Contact an administrator for further details.");
				}
			}
		}
	}
}
