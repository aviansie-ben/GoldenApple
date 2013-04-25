package com.bendude56.goldenapple.chat;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Pattern;

import com.bendude56.goldenapple.GoldenApple;

public class SimpleChatCensor implements ChatCensor {
	public static SimpleChatCensor noCensor = new SimpleChatCensor(new ArrayList<String>(), "***");
	public static SimpleChatCensor defaultChatCensor = noCensor;
	public static SimpleChatCensor strictChatCensor = noCensor;
	
	public static void loadCensors() {
		List<String> defaultCensor = GoldenApple.getInstanceMainConfig().getStringList("modules.chat.censorList");
		List<String> strictCensor = GoldenApple.getInstanceMainConfig().getStringList("modules.chat.strictCensorList");
		strictCensor.addAll(defaultCensor);
		
		defaultChatCensor = new SimpleChatCensor(defaultCensor, "***");
		strictChatCensor = new SimpleChatCensor(strictCensor, "***");
	}
	
	public static void unloadCensors() {
		defaultChatCensor = noCensor;
		strictChatCensor = noCensor;
	}
	
	public List<String> censoredRegex;
	public String censorString;
	
	public SimpleChatCensor(List<String> censoredRegex, String censorString) {
		this.censoredRegex = censoredRegex;
		this.censorString = censorString;
	}
	
	public String censorMessage(String message) {
		for (String regex : censoredRegex) {
			message = Pattern.compile(regex, Pattern.CASE_INSENSITIVE).matcher(message).replaceAll(censorString);
		}
		return message;
	}
}
