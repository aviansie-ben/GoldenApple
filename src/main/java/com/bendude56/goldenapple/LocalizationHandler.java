package com.bendude56.goldenapple;

import java.io.IOException;
import java.util.HashMap;
import java.util.Properties;
import java.util.logging.Level;

public class LocalizationHandler {
	public String defaultLocale;
	public HashMap<String, String> messages;
	public HashMap<String, HashMap<String, String>> secondaryMessages;
	
	public LocalizationHandler(ClassLoader loader) {
		defaultLocale = GoldenApple.getInstance().mainConfig.getString("message.defaultLocale");
		secondaryMessages = new HashMap<String, HashMap<String, String>>();
		for (String locale : GoldenApple.getInstance().mainConfig.getStringList("message.availableLocales"))
		{
			Properties p = new Properties();
			try {
				p.load(loader.getResourceAsStream("locale/" + locale + ".lang"));
			} catch (IOException e) {
				GoldenApple.log(Level.WARNING, "Failed to load language from " + locale + ".lang:");
				GoldenApple.log(Level.WARNING, e);
			}
			secondaryMessages.put(locale, new HashMap<String, String>());
			for (String entry : p.stringPropertyNames()) {
				secondaryMessages.get(locale).put(entry, p.getProperty(entry).replace('&', '§'));
			}
		}
		if (secondaryMessages.containsKey(defaultLocale)) {
			messages = secondaryMessages.get(defaultLocale);
		} else if (secondaryMessages.containsKey("en-US")) {
			defaultLocale = "en-US";
			messages = secondaryMessages.get("en-US");
			GoldenApple.log(Level.WARNING, "Default locale not found. Reverting to en-US...");
		} else if (secondaryMessages.size() > 0) {
			defaultLocale = (String) secondaryMessages.keySet().toArray()[0];
			messages = secondaryMessages.get(defaultLocale);
			GoldenApple.log(Level.WARNING, "Default locale and en-US locale not found. Reverting to next available locale...");
		} else {
			throw new RuntimeException("Unable to find valid locale file to load from!");
		}
	}
	
	public void sendMessage(User user, String message, boolean multiline) {
		sendMessage(user, message, multiline, new String[0]);
	}
	
	public void sendMessage(User user, String message, boolean multiline, String... args) {
		String lang = user.getPreferredLocale();
		if (!secondaryMessages.containsKey(lang))
			lang = defaultLocale;
		if (multiline) {
			for (int i = 1; secondaryMessages.get(lang).containsKey(message + "." + i); i++) {
				sendMessage(user, lang, message + "." + i, args);
			}
		} else {
			sendMessage(user, lang, message, args);
		}
	}
	
	private void sendMessage(User user, String lang, String message, String... args) {
		String msg = secondaryMessages.get(lang).get(message);
		for (int i = 0; i < args.length; i++) {
			msg.replace("%" + i, message);
		}
		user.getHandle().sendMessage(msg);
	}
}
