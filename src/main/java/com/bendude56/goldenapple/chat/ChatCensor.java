package com.bendude56.goldenapple.chat;

import java.util.ArrayList;
import java.util.List;

import com.bendude56.goldenapple.GoldenApple;

public class ChatCensor {
	public static ChatCensor noCensor = new ChatCensor(new ArrayList<String>(), "***");
	public static ChatCensor defaultChatCensor = noCensor;
	public static ChatCensor strictChatCensor = noCensor;
	
	public static void loadCensors() {
		List<String> defaultCensor = GoldenApple.getInstance().mainConfig.getStringList("modules.chat.censorList");
		List<String> strictCensor = GoldenApple.getInstance().mainConfig.getStringList("modules.chat.strictCensorList");
		strictCensor.addAll(defaultCensor);
		
		defaultChatCensor = new ChatCensor(defaultCensor, "***");
		strictChatCensor = new ChatCensor(strictCensor, "***");
	}
	
	public static void unloadCensors() {
		defaultChatCensor = noCensor;
		strictChatCensor = noCensor;
	}
	
	public List<String> censoredRegex;
	public String censorString;
	
	public ChatCensor(List<String> censoredRegex, String censorString) {
		this.censoredRegex = censoredRegex;
		this.censorString = censorString;
	}
	
	public String censorMessage(String message) {
		for (String regex : censoredRegex) {
			message = message.replaceAll(regex, censorString);
		}
		return message;
	}
}
