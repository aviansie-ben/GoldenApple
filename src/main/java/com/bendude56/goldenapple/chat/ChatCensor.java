package com.bendude56.goldenapple.chat;

import java.util.List;

public class ChatCensor {
	public List<String> censoredRegex;
	public String censorString = "***";
	
	public ChatCensor(boolean strict) {
		
	}
	
	public String censorMessage(String message) {
		return message;
	}
}
