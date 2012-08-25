package com.bendude56.goldenapple.listener;

import org.bukkit.Bukkit;
import org.bukkit.event.EventHandler;
import org.bukkit.event.HandlerList;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerRespawnEvent;

import com.bendude56.goldenapple.GoldenApple;

public class WarpListener implements Listener
{
	private static WarpListener instance;
	
	public static void registerEvents()
	{
		if (instance == null)
		{
			instance = new WarpListener();
			Bukkit.getServer().getPluginManager().registerEvents(instance, GoldenApple.getInstance());
		}
	}
	
	public static void unregisterEvents()
	{
		if (instance != null)
		{
			HandlerList.unregisterAll(instance);
			instance = null;
		}
	}
	
	@EventHandler
	public void onPlayerRespawn(PlayerRespawnEvent e)
	{
		// TODO Option to send player to their home 1
	}
	
}
