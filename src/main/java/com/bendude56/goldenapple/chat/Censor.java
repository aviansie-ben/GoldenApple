package com.bendude56.goldenapple.chat;

import java.util.ArrayList;
import java.util.List;

public class Censor {
	private List<String> censored = new ArrayList<String>();

	public void censorWord(String word) {
		if (!censored.contains(word))
			censored.add(word);
	}

	public void uncensorWord(String word) {
		if (censored.contains(word))
			censored.remove(word);
	}

	public String removeAllCaps(String string) {
		String value = "";
		for (String word : string.split(" ")) {
			int caps = 0;
			for (int c=0; c < word.length(); c++) {
				Character character = word.charAt(c);
				if (Character.isUpperCase(character)) {
					caps++;
				}
			}
			if (value.equalsIgnoreCase("")) {
				value += "";
			}
			if (caps > 2) {
				value += word.toLowerCase();
			} else {
				value += word;
			}
		}
		return value;
	}

	public String censorString(String string) {
		String[] original = formatForCensorship(string).split(" ");
		String word;
		boolean wordIsBlocked = false;
		int i = 0, ii = 0;

		for (i = 0; i < original.length; i++) {
			word = original[i];
			wordIsBlocked = false;
			for (String censored : this.censored) {
				censored = formatForCensorship(censored).replaceAll(" ", "");
				if (!wordIsBlocked)
					ii = 0;
				while (compareWords(original[i], censored) && !wordIsBlocked) {
					if (word == censored) {
						int pos = 0;
						for (int c = 0; c < i; c++) {
							pos += 1;
							pos += original[c].length();
						}
						string = string.substring(0, pos + 1)
								+ generateBlockedWord(word.length() + ii)
								+ string.substring(
										pos + word.length() + ii + 1,
										string.length());
						i += ii;
						wordIsBlocked = true;
					} else {
						ii++;
						word = word + original[i + ii];
					}
				}
			}
		}
		return string;
	}

	public boolean mustBeCensored(String string) {
		String[] original = formatForCensorship(string).split(" ");
		String word = "";
		int i = 0, ii = 0;

		for (i = 0; i < original.length; i++) {
			word.equalsIgnoreCase(original[i]);
			for (String censored : this.censored) {
				censored = formatForCensorship(censored).replaceAll(" ", "");
				ii = 0;
				while (compareWords(original[i], censored)) {
					if (word.equalsIgnoreCase(censored)) {
						return true;
					} else {
						ii++;
						word = word + original[i + ii];
					}
				}
			}
		}
		return false;
	}

	public boolean compareWords(String word, String censored) {
		int c = 0;
		for (int w = 0; w < word.length(); w++) {
			if (c < censored.length() && word.charAt(w) == censored.charAt(c)) {
				c++;
			} else if (w > 0 && word.charAt(w) == word.charAt(w - 1)) {
				// Do nothing, just to take repeating characters into account.
				// Example: "FFFFUUUUUUUUUUU"
			} else {
				return false;
			}
		}
		return true;
	}

	public String formatForCensorship(String string) {
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

	public String generateBlockedWord(int i) {
		String string = "";
		while (i > 0)
			string += "*";
		return string;
	}
}
