package com.bendude56.goldenapple.chat;

import java.util.HashMap;

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

	private static ChatChannel DEFAULT_CHANNEL;
	
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

}