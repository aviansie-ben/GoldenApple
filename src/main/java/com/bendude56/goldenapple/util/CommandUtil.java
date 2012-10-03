package com.bendude56.goldenapple.util;

import java.util.HashMap;

public class CommandUtil
{
	
	/**
	 * This function will accept an array of strings and parse
	 * it out into a HashMap with keys and values, which makes
	 * checking if a command contains certain arguments and what
	 * those arguments are. Uses ":", "-", and "=" as regexes.
	 * @param args The String array to be Parsed. Example:
	 * command arguments.
	 * @return The args in HashMap form.
	 */
	public static HashMap<String, String> parseArguments(String[] args)
	{
		HashMap<String, String> result = new HashMap<String, String>();
		
		for (String arg : args)
		{
			if (arg.startsWith("-"))
			{
				result.put(arg.substring(1), "true");
			}
			else if (arg.contains("="))
			{
				result.put(arg.substring(0, arg.indexOf("=")), arg.substring(arg.indexOf("=") + 1));
			}
			else if (arg.contains(":"))
			{
				result.put(arg.substring(0, arg.indexOf(":")), arg.substring(arg.indexOf(":") + 1));
			}
		}
		
		return result;
	}
}
