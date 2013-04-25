package com.bendude56.goldenapple;

public interface LocalizationManager {
	String getMessage(User user, String message);
	
	String processMessageDefaultLocale(String message, String... args);
	String processMessage(String locale, String message, String... args);
	
	void sendMessage(User user, String message, boolean multiline);
	void sendMessage(User user, String message, boolean multiline, String... args);
	
	boolean languageExists(String lang);
	boolean messageExists(String message);
	boolean messageExists(String lang, String message);
}
