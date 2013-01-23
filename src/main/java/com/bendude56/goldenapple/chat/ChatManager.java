package com.bendude56.goldenapple.chat;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import org.bukkit.ChatColor;
import org.bukkit.entity.Player;

import com.bendude56.goldenapple.User;
import com.bendude56.goldenapple.permissions.PermissionManager.Permission;
import com.bendude56.goldenapple.permissions.PermissionManager.PermissionNode;

public class ChatManager {

	// goldenapple.chat
	public static PermissionNode		chatNode;
	public static Permission			tellPermission;

	// goldenapple.chat.channel
	public static PermissionNode		channelsNode;
	public static Permission			channelAddPermission;
	public static Permission			channelModPermission;
	public static Permission			channelAdminPermission;

	private HashMap<Long, ChatChannel>	chatChannels	= new HashMap<Long, ChatChannel>();
	private HashMap<User, Long>			chatPlayers		= new HashMap<User, Long>();

	public ChatChannel createChannel(String label) {
		return null;
		/*ChatChannel channel = new ChatChannel(generateId(), label);
		chatChannels.put(channel.getId(), channel);
		return channel;*/
	}

	public ChatChannel getChannel(Long ID) {
		if (chatChannels.containsKey(ID))
			return chatChannels.get(ID);
		else
			return null;
	}

	public ChatChannel getChannel(String name) {
		return null;
	}

	public void deleteChannel(Long ID) {
		ChatChannel channel = chatChannels.get(ID);
		if (channel != null) {
			chatChannels.remove(ID);
			for (Player player : getPlayers(ID))
				;
				// chatPlayers.put(player, LOBBY.getId());
		}
	}

	private Long generateId() {
		Long id = (long)0;
		while (!chatChannels.containsKey(0))
			id++;
		return id;
	}

	/**
	 * Adds a player to the chat channel system. ONLY to be used when the player
	 * first logs on.
	 * 
	 * @param player
	 */
	public void addPlayer(Player player) {
		// chatPlayers.put(player, LOBBY.getId());
	}

	/**
	 * Sets what channel a player is assigned to.
	 * 
	 * @param player The player who's channel is being set.
	 * @param channel The channel to set the player to.
	 */
	public void setChannel(Player player, ChatChannel channel) {
		// chatPlayers.put(player, channel.getId());
	}

	/**
	 * Returns a list of all players assigned a certain channel
	 * 
	 * @param channel The channel to search for
	 */
	public List<Player> getPlayers(ChatChannel channel) {
		return null;
		// return getPlayers(channel.getId());
	}

	/**
	 * Returns a list of all players assigned a certain channel
	 * 
	 * @param ID The ID of the channel to search for
	 */
	public List<Player> getPlayers(Long ID) {
		List<Player> players = new ArrayList<Player>();
		/*for (Player player : chatPlayers.keySet())
			if (chatPlayers.get(player) == ID)
				players.add(player);*/
		return players;
	}

	/**
	 * Removes a player from all channels. Intended to only be used when the
	 * player goes offline.
	 * 
	 * @param player
	 */
	public void removePlayer(Player player) {
		for (ChatChannel channel : chatChannels.values()) {
			/*channel.removeSpy(player);
			LOBBY.removeSpy(player);*/
		}
		if (chatPlayers.containsKey(player))
			chatPlayers.remove(player);
	}

	public void startSpying(Player player, ChatChannel channel) {
		// channel.addSpy(player);
	}

	public void stopSpying(Player player, ChatChannel channel) {
		// channel.removeSpy(player);
	}
}
