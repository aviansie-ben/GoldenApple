package com.bendude56.goldenapple.util;

import java.util.HashMap;

import org.bukkit.ChatColor;

public class ChatUtil {
	private static final int LINE_WIDTH = 324;
	private static final HashMap<Character, Integer> charWidth;
	
	static {
		charWidth = new HashMap<Character, Integer>();
		
		// Uppercase
		setWidth(5, 'A', 'B', 'C', 'D', 'E', 'F', 'G', 'H', 'J', 'K', 'L', 'M', 'N', 'O', 'P', 'Q', 'R', 'S', 'T', 'U', 'V', 'W', 'X', 'Y', 'Z');
		setWidth(3, 'I');
		
		// Lowercase
		setWidth(5, 'a', 'b', 'c', 'd', 'e', 'g', 'h', 'j', 'm', 'n', 'o', 'p', 'q', 'r', 's', 'u', 'v', 'w', 'x', 'y', 'z');
		setWidth(4, 'f', 'k');
		setWidth(3, 't');
		setWidth(2, 'l');
		setWidth(1, 'i');
		
		// Numbers
		setWidth(5, '0', '1', '2', '3', '4', '5', '6', '7', '8', '9');
		
		// Symbols
		setWidth(6, '@', '~');
		setWidth(5, '#', '$', '%', '&', '+', '-', '/', '=', '?', '\\', '^', '_');
		setWidth(4, '(', ')', '*', '<', '>', '{', '}');
		setWidth(3, ' ', '\"', '[', ']');
		setWidth(2, '`');
		setWidth(1, '!', '\'', ',', '.', ':', ';', '|');
	}
	
	private static void setWidth(int width, char... chars) {
		for (char c : chars) {
			charWidth.put(c, width);
		}
	}

	public static String alignCenter(String text) {
		int width = getStringWidth(text);
		if (LINE_WIDTH - width > 0) {
			double i = Math.floor(((LINE_WIDTH - width) / 2) / 4);
			while (i > 0) {
				text = " " + text;
				i--;
			}
		}
		return text;
	}

	public static String alignRight(String text) {
		int width = getStringWidth(text);
		if (LINE_WIDTH - width > 0) {
			double i = Math.floor((LINE_WIDTH - width) / 4);
			while (i > 0) {
				text = " " + text;
				i--;
			}
		}
		return text;
	}

	public static int getStringWidth(String text) {
		int length = 0;
		text = ChatColor.stripColor(text);
		for (int c = 0; c < text.length(); c++) {
			if (length > 0)
				length += 1;
			length += getCharacterWidth(text.charAt(c));
		}
		return length;
	}

	public static int getCharacterWidth(Character c) {
		if (charWidth.containsKey(c))
			return charWidth.get(c);
		else
			return 5;
	}
}
