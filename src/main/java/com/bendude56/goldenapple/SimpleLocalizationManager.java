package com.bendude56.goldenapple;

import java.io.IOException;
import java.util.HashMap;
import java.util.Properties;
import java.util.logging.Level;

import org.bukkit.ChatColor;

/**
 * A simple localization manager for GoldenApple that implements
 * {@link LocalizationManager}. Designed to load localization files from a
 * resources folder and to provide an interface for fetching and sending these
 * messages to users.
 * 
 * @author ben_dude56
 * 
 */
public class SimpleLocalizationManager implements LocalizationManager {
	/**
	 * The name of the default locale.
	 */
	public String									defaultLocale;
	/**
	 * The messages loaded from the default locale.
	 */
	public HashMap<String, String>					messages;
	/**
	 * All messages loaded from all locale
	 */
	public HashMap<String, HashMap<String, String>>	secondaryMessages;
	
	/**
	 * Constructor for the {@link SimpleLocalizationManager}. Loads up all
	 * available localization files from the default localization folder.
	 * 
	 * @param	loader The ClassLoader to use to access the resource files.
	 */
	public SimpleLocalizationManager(ClassLoader loader) {
		// Get the default language
		defaultLocale = GoldenApple.getInstanceMainConfig().getString("message.defaultLocale");
		// Initialize the double hashmap for all secondary languages
		secondaryMessages = new HashMap<String, HashMap<String, String>>();
		
		// Begin loading all available localization files
		for (String locale : GoldenApple.getInstanceMainConfig().getStringList("message.availableLocales")) {
			Properties p = new Properties();
			
			// Attempt to load the localization file
			try {
				p.load(loader.getResourceAsStream("locale/" + locale + ".lang"));
			} catch (IOException e) {
				GoldenApple.log(Level.WARNING, "Failed to load language from " + locale + ".lang:");
				GoldenApple.log(Level.WARNING, e);
				continue;
			}
			
			// Create a place to store the localization data
			secondaryMessages.put(locale, new HashMap<String, String>());
			
			// Start storing each localization string in memory
			for (String entry : p.stringPropertyNames()) {
				secondaryMessages.get(locale).put(entry, p.getProperty(entry).replace('&', ChatColor.COLOR_CHAR));
			}
		}
		
		// Create shortcut to primary language map
		if (secondaryMessages.containsKey(defaultLocale)) {
			messages = secondaryMessages.get(defaultLocale);
		}
		// If the preferred default locale is not loaded, make "en-US" default
		else if (secondaryMessages.containsKey("en-US")) {
			defaultLocale = "en-US";
			messages = secondaryMessages.get("en-US");
			GoldenApple.log(Level.WARNING, "Default locale not found. Reverting to en-US...");
		}
		// If "en-US" is not loaded, then attempt to make first language default
		else if (secondaryMessages.size() > 0) {
			defaultLocale = (String)secondaryMessages.keySet().toArray()[0];
			messages = secondaryMessages.get(defaultLocale);
			GoldenApple.log(Level.WARNING, "Default locale and en-US locale not found. Reverting to next available locale...");
		}
		// If no languages are loaded, then thrown an error
		else {
			throw new RuntimeException("Unable to find valid locale file to load from!");
		}
	}
	
	@Override
	public String getMessage(User user, String message) {
		// Get the user's preferred locale
		String lang = user.getPreferredLocale();
		// If the user's preferred locale doesn't exist, use the default
		if (!secondaryMessages.containsKey(lang))
			lang = defaultLocale;
		// Get the message string in the user's locale.
		return secondaryMessages.get(lang).get(message);
	}
	
	@Override
	public String processMessageDefaultLocale(String message, String... args) {
		// Get the message from the default locale based on the given key
		String msg = messages.get(message);
		
		// Replace all placeholder text with the text given in the array
		for (int i = 0; i < args.length; i++) {
			if (args[i] == null)
				throw new IllegalArgumentException("args[" + i + "] cannot be null");
			msg = msg.replace("%" + (i + 1), args[i]);
		}
		
		return msg;
	}
	
	@Override
	public String processMessage(String locale, String message, String... args) {
		// Gets the message from the specified locale based on the given key
		String msg = secondaryMessages.get(locale).get(message);
		
		// Replace all placeholder text with the text given in the array
		for (int i = 0; i < args.length; i++) {
			if (args[i] == null)
				throw new IllegalArgumentException("args[" + i + "] cannot be null");
			msg = msg.replace("%" + (i + 1), args[i]);
		}
		
		return msg;
	}

	@Override
	public void sendMessage(User user, String message, boolean multiline) {
		sendMessage(user, message, multiline, new String[0]);
	}

	@Override
	public void sendMessage(User user, String message, boolean multiline, String... args) {
		// Get the user's preferred locale
		String lang = user.getPreferredLocale();
		
		// If the user's preferred locale doesn't exist, use the default locale
		if (!secondaryMessages.containsKey(lang))
			lang = defaultLocale;
		
		// In case message spans multiple lines, send multiple messages 
		if (multiline) {
			for (int i = 1; secondaryMessages.get(lang).containsKey(message + "." + i); i++) {
				sendMessage(user, lang, message + "." + i, args);
			}
		} else {
			// In the case the message spans one line, send single message
			sendMessage(user, lang, message, args);
		}
	}
	
	/**
	 * Sends a localized message to a user, replacing placeholder text for
	 * that supplied in the string array.
	 * 
	 * @param	user The user to send the message to.
	 * @param	lang The language to use for the message.
	 * @param	message The key of the message to send.
	 * @param	args An array of Strings to swap out for placeholder text.
	 */
	private void sendMessage(User user, String lang, String message, String... args) {
		// Get the specified message from the specified locale
		String msg = secondaryMessages.get(lang).get(message);
		
		// Replace placeholders with given 
		for (int i = 0; i < args.length; i++) {
			if (args[i] == null)
				throw new IllegalArgumentException("args[" + i + "] cannot be null");
			msg = msg.replace("%" + (i + 1), args[i]);
		}
		
		// Send the message to the user
		if (msg != null && msg.length() > 0)
			user.getHandle().sendMessage(msg);
	}

	
	@Override
	public boolean languageExists(String lang) {
		return secondaryMessages.containsKey(lang);
	}

	@Override
	public boolean messageExists(String message) {
		return messages.containsKey(message);
	}

	@Override
	public boolean messageExists(String lang, String message) {
		return languageExists(lang) && secondaryMessages.get(lang).containsKey(lang);
	}
}
