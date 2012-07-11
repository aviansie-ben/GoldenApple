package com.bendude56.goldenapple.chat;

import java.util.HashMap;

import org.bukkit.ChatColor;

import com.bendude56.goldenapple.permissions.PermissionManager.Permission;
import com.bendude56.goldenapple.permissions.PermissionManager.PermissionNode;

public class ChatManager {
	public static PermissionNode channels;
	public static PermissionNode mute;
	public static PermissionNode censor;
	public static Permission channelCreate;
	public static Permission channelEdit;
	public static Permission channelDelete;
	public static Permission muteTimed;
	public static Permission mutePermanent;
	public static Permission muteUnmute;
	public static Permission muteImmune;
	public static Permission censorAddWord;
	public static Permission censorRemoveWord;
	public static Permission censorIgnore;

	private HashMap<Long, ChatChannel> chatChannels = new HashMap<Long, ChatChannel>();

	private static ChatChannel DEFAULT_CHANNEL = new ChatChannel(0, "Lobby", ChatColor.WHITE);
	
	//---------------CHAT CENSOR---------------
	public void blockWord(String word, boolean strict){
		Censor.blockWord(word, strict);
	}
	public void unblockWord(String word){
		Censor.unblockWord(word);
	}
	public boolean isBlocked(String word){
		return Censor.isBlocked(word);
	}
	
	public String censorString(String string){
		return Censor.censorString(string);
	}
	
	public boolean containsBlockedWord(String string){
		return Censor.containsBlockedWord(string);
	}
	
	
	//---------------CHAT CHANNELS---------------
	public ChatChannel getDefaultChannel(){
		return DEFAULT_CHANNEL;
	}
	
	public ChatChannel createChannel(String label){
		ChatChannel channel = new ChatChannel(generateId(), label, ChatColor.WHITE);
		chatChannels.put(channel.getId(), channel);
		return channel;
	}
	public ChatChannel getChannel(Long ID){
		if (chatChannels.containsKey(ID))
			return chatChannels.get(ID);
		else
			return null;
	}
	public ChatChannel getChannel(String label){
		for (ChatChannel channel : chatChannels.values())
			if (channel.getLabel().equals(label))
				return channel;
		for (ChatChannel channel : chatChannels.values())
			if (channel.getLabel().equalsIgnoreCase(label))
				return channel;
		return null;
	}
	public ChatChannel deleteChannel(Long ID){
		ChatChannel channel = chatChannels.get(ID);
		if (channel != null)
			chatChannels.remove(ID);
		return channel;
	}
	
	private Long generateId(){
		Long id = (long) 0;
		while(!chatChannels.containsKey(0))
			id++;
		return id;
	}
}