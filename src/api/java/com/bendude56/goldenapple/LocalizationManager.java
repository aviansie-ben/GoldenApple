package com.bendude56.goldenapple;

/**
 * Interface for a LocalizationManager, a class designed to provide an
 * method to accesses stored text from a localization file. Designed to allow
 * for users to select the local of their choice. 
 * 
 * @author ben_dude56
 */
public interface LocalizationManager {
	/**
	 * Gets a localization message based on a given user's preferences and a
	 * message key.
	 * 
	 * @param 	user The user to determine the language for. 
	 * @param 	message The message key of the message to send.
	 * 
	 * @return	The message identified by the given message key and the language
	 * 			specified by the user.
	 */
	String getMessage(User user, String message);
	
	/**
	 * Processes a message using the default locale. Retrieves the message from
	 * the default locale that matches the given message key and replaces all
	 * embedded placeholder text with that of the given String array.
	 * 
	 * @param	message The key of the message ot process.
	 * @param	args Array of replacement strings to  
	 * 
	 * @return	The requested message after all placeholder text has been
	 * 			replaced.
	 */
	String processMessageDefaultLocale(String message, String... args);
	/**
	 * Processes a message from a specified locale. Retrieves the message from
	 * the specified locale that matches the given message key and replaces all
	 * embedded placeholder text with that of the given String array.
	 * 
	 * @param	locale The locale to use.
	 * @param	message The key of the message to process.
	 * @param	args The array of strings to swap out placeholder text with.
	 * 
	 * @return	The processed message with all placeholders replaced.
	 */
	String processMessage(String locale, String message, String... args);
	
	/**
	 * Sends a localized message to a user. Uses the user's preferred locale to
	 * determined which locale to use.
	 * 
	 * @param	user The user to send the message to.
	 * @param	message The key of the message to send.
	 * @param	multiline Whether or not to treat this message as a multi-line
	 * 			message. True will include all parts of the message and
	 * 			individually send them to the user.
	 */
	void sendMessage(User user, String message, boolean multiline);
	/**
	 * Sends a localized message to a user. Uses the user's preferred locale to
	 * determined which locale to use. Replaces all placeholder text with the
	 * Strings given as the final arguments.
	 * 
	 * @param	user The user to send the message to.
	 * @param	message The key of the message to send.
	 * @param	multiline Whether or not to treat this message as a multi-line
	 * 			message. True will include all parts of the message and
	 * 			individually send them to the user.
	 * @param	args String array to use as replacement text for placeholders.
	 */
	void sendMessage(User user, String message, boolean multiline, String... args);
	
	/**
	 * Checks if the locale with the given name has been loaded.
	 * 
	 * @param	lang The name of the language to check for.
	 * 
	 * @return	True if the language exists and is loaded, false otherwise.
	 */
	boolean languageExists(String lang);
	/**
	 * Checks if a specific message has been loaded from the default locale.
	 * 
	 * @param	message The key of the message to check for.
	 * 
	 * @return	True if the message exists in the default locale, false if not.
	 */
	boolean messageExists(String message);
	/**
	 * Checks if a specific message has been loaded from a specific locale.
	 * 
	 * @param	lang The locale to check for the given message.
	 * @param	message The mesage key to search for.
	 * 
	 * @return	True if the message exists in the given locale, false if not.
	 */
	boolean messageExists(String lang, String message);
}
