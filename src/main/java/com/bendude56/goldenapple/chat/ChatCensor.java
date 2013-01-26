package com.bendude56.goldenapple.chat;

import java.util.ArrayList;
import java.util.List;

public class ChatCensor {
	public static ChatCensor noCensor = new ChatCensor(new ArrayList<String>(), "***");
	public static ChatCensor defaultChatCensor = noCensor;
	public static ChatCensor strictChatCensor = noCensor;
	
	public List<String> censoredRegex;
	public String censorString;
	
	public ChatCensor(ArrayList<String> censoredRegex, String censorString) {
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
