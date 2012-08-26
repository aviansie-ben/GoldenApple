package com.bendude56.goldenapple.listener;

import org.bukkit.Bukkit;
import org.bukkit.event.EventHandler;
import org.bukkit.event.HandlerList;
import org.bukkit.event.Listener;
import org.bukkit.event.player.AsyncPlayerChatEvent;

import com.bendude56.goldenapple.GoldenApple;

public class ChatListener implements Listener
{
	private static ChatListener instance = null;
	
	public static void registerEvents()
	{
		if (instance == null)
		{
			instance = new ChatListener();
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
	public void onAsyncPlayerChat(AsyncPlayerChatEvent e)
	{
		
	}

}
