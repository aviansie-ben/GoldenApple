package com.bendude56.goldenapple.chat;

public class ChatUtil {
	static private int LINE_WIDTH = 324;

	public String justifyCenter(String text) {
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

	public String justifyRight(String text) {
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

	public int getStringWidth(String text) {
		int length = 0;
		for (int c = 0; c < text.length(); c++) {
			if (((Character) text.charAt(c)).toString() == "§") {
				c += 2;
			} else {
				if (length > 0)
					length += 1;
				length += getCharacterWidth(text.charAt(c));
			}
		}
		return length;
	}

	public int getCharacterWidth(Character c) {
		String letter = c.toString();
		switch (letter) {
		// Upper case
		case "A":
			return 5;
		case "B":
			return 5;
		case "C":
			return 5;
		case "D":
			return 5;
		case "E":
			return 5;
		case "F":
			return 5;
		case "G":
			return 5;
		case "H":
			return 5;
		case "I":
			return 3;
		case "J":
			return 5;
		case "K":
			return 5;
		case "L":
			return 5;
		case "M":
			return 5;
		case "N":
			return 5;
		case "O":
			return 5;
		case "P":
			return 5;
		case "Q":
			return 5;
		case "R":
			return 5;
		case "S":
			return 5;
		case "T":
			return 5;
		case "U":
			return 5;
		case "V":
			return 5;
		case "W":
			return 5;
		case "X":
			return 5;
		case "Y":
			return 5;
		case "Z":
			return 5;
			// Lower case
		case "a":
			return 5;
		case "b":
			return 5;
		case "c":
			return 5;
		case "d":
			return 5;
		case "e":
			return 5;
		case "f":
			return 4;
		case "g":
			return 5;
		case "h":
			return 5;
		case "i":
			return 1;
		case "j":
			return 5;
		case "k":
			return 4;
		case "l":
			return 2;
		case "m":
			return 5;
		case "n":
			return 5;
		case "o":
			return 5;
		case "p":
			return 5;
		case "q":
			return 5;
		case "r":
			return 5;
		case "s":
			return 5;
		case "t":
			return 3;
		case "u":
			return 5;
		case "v":
			return 5;
		case "w":
			return 5;
		case "x":
			return 5;
		case "y":
			return 5;
		case "z":
			return 5;
			// Number
		case "0":
			return 5;
		case "1":
			return 5;
		case "2":
			return 5;
		case "3":
			return 5;
		case "4":
			return 5;
		case "5":
			return 5;
		case "6":
			return 5;
		case "7":
			return 5;
		case "8":
			return 5;
		case "9":
			return 5;
			// Punctuation
		case " ":
			return 3;
		case "!":
			return 1;
		case "\"":
			return 3;
		case "#":
			return 5;
		case "$":
			return 5;
		case "%":
			return 5;
		case "&":
			return 5;
		case "'":
			return 1;
		case "(":
			return 4;
		case ")":
			return 4;
		case "*":
			return 4;
		case "+":
			return 5;
		case ",":
			return 1;
		case "-":
			return 5;
		case ".":
			return 1;
		case "/":
			return 5;
		case ":":
			return 1;
		case ";":
			return 1;
		case "<":
			return 4;
		case "=":
			return 5;
		case ">":
			return 4;
		case "?":
			return 5;
		case "@":
			return 6;
		case "[":
			return 3;
		case "\\":
			return 5;
		case "]":
			return 3;
		case "^":
			return 5;
		case "_":
			return 5;
		case "`":
			return 2;
		case "{":
			return 4;
		case "|":
			return 1;
		case "}":
			return 4;
		case "~":
			return 6;
			// ChatColors
		case "§":
			return 0;
		}
		return 5;
	}
}
