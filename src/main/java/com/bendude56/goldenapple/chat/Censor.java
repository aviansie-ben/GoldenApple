package com.bendude56.goldenapple.chat;

import java.util.ArrayList;
import java.util.List;

public class Censor {
	private static List<String> blocked = new ArrayList<String>();
	private static List<String> blockedStrict = new ArrayList<String>();

	private static final String CENSOR_STRING = "***";

	public static void blockWord(String string, boolean strict) {
		if (strict) {
			if (!blockedStrict.contains(string.toLowerCase()))
				blockedStrict.add(string.toLowerCase());
			if (blocked.contains(string.toLowerCase()))
				blocked.remove(string.toLowerCase());
		} else {
			if (!blocked.contains(string.toLowerCase()))
				blocked.add(string.toLowerCase());
			if (blockedStrict.contains(string.toLowerCase()))
				blockedStrict.remove(string.toLowerCase());
		}
	}
	public static boolean isBlocked(String string) {
		return (blocked.contains(string.toLowerCase())
				|| blockedStrict.contains(string.toLowerCase()));
	}
	public static void unblockWord(String string) {
		if (blocked.contains(string.toLowerCase()))
			blocked.remove(string.toLowerCase());
		if (blockedStrict.contains(string.toLowerCase()))
			blockedStrict.remove(string.toLowerCase());
	}

	public static String censorString(String string) {
		List<String> wordsToBlock = new ArrayList<String>();
		for (String blockedWord : blockedStrict) {
			String word = "";
			int char1 = 0;
			int char2 = 0;
			for (char1 = 0; char1 < string.length(); char1++) {
				if (char2 < blockedWord.length()
						&& format(string.substring(char1, char1)).equals(
								format(blockedWord.substring(char2, char2)))) {
					char2++;
					word += string.substring(char1, char1);
					if (char2 == blockedWord.length()) {
						if (char1 + 1 >= string.length()
								|| (char1 + 1 < string.length() && !format(
										string.substring(char1, char1)).equals(
										format(string.substring(char1 + 1,
												char1 + 1))))) {
							wordsToBlock.add(word);
							char2 = 0;
							word = "";
						}
					}
				} else if (char1 > 0
						&& format(string.substring(char1, char1)).equals(
								format(string.substring(char1 - 1, char1 - 1)))) {
					word += string.substring(char1, char1);
				} else if (char2 == blockedWord.length()) {
					wordsToBlock.add(word);
					char2 = 0;
					word = "";
				} else if (char2 < blockedWord.length()
						&& format(string.substring(char1, char1)).equals(" ")) {
					word += string.substring(char1, char1);
				} else {
					char2 = 0;
					word = "";
				}
			}
		}
		for (String blockedWord : blocked) {
			int char1 = 0;
			int char2 = 0;
			String word = "";
			for (char1 = 0; char1 < string.length(); char1++) {

				if (char2 < blockedWord.length()
						&& format(string.substring(char1, char1)).equals(
								format(blockedWord.substring(char2, char2)))) {
					char2++;
					word += string.substring(char1, char1);
					if (char2 == blockedWord.length()) {
						if (char1 + 1 >= string.length()
								|| (char1 + 1 < string.length() && !format(
										string.substring(char1, char1)).equals(
										format(string.substring(char1 + 1,
												char1 + 1))))) {
							wordsToBlock.add(word);
							char2 = 0;
							word = "";
						}
					}
				} else if (char1 > 0
						&& format(string.substring(char1, char1)).equals(
								format(string.substring(char1 - 1, char1 - 1)))) {
					word += string.substring(char1, char1);
				} else if (char2 == blockedWord.length()
						&& (char1 + 1 == string.length() || format(
								string.substring(char1 + 1, char1 + 1)).equals(
								" "))) {
					wordsToBlock.add(word);
					char2 = 0;
					word = "";
				} else if (char2 < blockedWord.length()
						&& format(string.substring(char1, char1)).equals(" ")) {
					word += string.substring(char1, char1);
				} else {
					char2 = 0;
					word = "";
				}
			}
		}
		for (String word : wordsToBlock) {
			string.replaceAll(word, CENSOR_STRING);
		}
		return string;
	}

	public static boolean containsBlockedWord(String string) {
		for (String blockedWord : blockedStrict) {
			int char1 = 0;
			int char2 = 0;
			for (char1 = 0; char1 < string.length(); char1++) {
				if (format(string.substring(char1, char1)).equals(
						format(blockedWord.substring(char2, char2)))) {
					char2++;
					if (char2 >= blockedWord.length()) {
						return true;
					}
				} else if (char1 > 0
						&& format(string.substring(char1, char1)).equals(
								format(string.substring(char1 - 1, char1 - 1)))) {
				} else if (char2 < blockedWord.length()
						&& format(string.substring(char1, char1)).equals(" ")) {
				} else {
					char2 = 0;
				}
			}
		}
		for (String blockedWord : blocked) {
			int char1 = 0;
			int char2 = 0;
			for (char1 = 0; char1 < string.length(); char1++) {
				if (format(string.substring(char1, char1)).equals(
						format(blockedWord.substring(char2, char2)))
						&& (char2 > 0 || (char1 > 0 && format(
								string.substring(char1 - 1, char1 - 1)).equals(
								" ")))) {
					char2++;
					if (char2 >= blockedWord.length()) {
						if (char1 + 1 >= string.length()
								|| string.substring(char1 + 1, char1 + 1)
										.equals(" "))
							return true;
					}
				} else if (char1 > 0
						&& format(string.substring(char1, char1)).equals(
								format(string.substring(char1 - 1, char1 - 1)))) {
				} else if (char2 < blockedWord.length()
						&& format(string.substring(char1, char1)).equals(" ")) {
				} else {
					char2 = 0;
				}
			}
		}
		return false;
	}

	public static String format(String string) {
		string = string.toLowerCase();
		// Letters
		string.replaceAll("@", "a");
		string.replaceAll("$", "s");
		string.replaceAll("1", "l");
		string.replaceAll("!", "i");
		string.replaceAll("|", "i");
		string.replaceAll("3", "e");
		string.replaceAll("4", "a");
		string.replaceAll("5", "s");
		string.replaceAll("0", "o");
		// Punctuation
		string.replaceAll(".", " ");
		string.replaceAll(",", " ");
		string.replaceAll("/", " ");
		string.replaceAll("?", " ");
		string.replaceAll(";", " ");
		string.replaceAll(":", " ");
		string.replaceAll("'", " ");
		string.replaceAll("\"", " ");
		string.replaceAll("[", " ");
		string.replaceAll("{", " ");
		string.replaceAll("]", " ");
		string.replaceAll("}", " ");
		string.replaceAll("\\", " ");
		string.replaceAll("|", " ");
		string.replaceAll("`", " ");
		string.replaceAll("~", " ");
		string.replaceAll("*", " ");
		string.replaceAll("(", " ");
		string.replaceAll(")", " ");
		string.replaceAll("-", " ");
		string.replaceAll("_", " ");
		string.replaceAll("+", " ");
		string.replaceAll("=", " ");

		return string;
	}
}
