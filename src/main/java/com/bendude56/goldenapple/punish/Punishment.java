package com.bendude56.goldenapple.punish;

import java.sql.Timestamp;

import com.bendude56.goldenapple.GoldenApple;

public class Punishment {
	protected long targetId, adminId;
	protected String reason;
	protected Timestamp startTime;
	
	public static class RemainingTime {
		private long secondsLeft;
		
		public RemainingTime(long seconds) {
			this.secondsLeft = seconds;
		}
		
		public RemainingTime(int days, int hours, int minutes, int seconds) {
			this.secondsLeft = seconds + (minutes * 60) + (hours * 3600) + (days * 86400);
		}
		
		public long getTotalSeconds() {
			return secondsLeft;
		}
		
		public int getDays() {
			return (int)Math.floor(secondsLeft / 86400D);
		}
		
		public int getHours() {
			return (int)((long)Math.floor(secondsLeft / 3600D) % 24);
		}
		
		public int getMinutes() {
			return (int)((long)Math.floor(secondsLeft / 60D) % 60);
		}
		
		public int getSeconds() {
			return (int)(secondsLeft % 60);
		}
		
		@Override
		public String toString() {
			String result = "";
			int days = getDays(), hours = getHours(), minutes = getMinutes(), seconds = getSeconds();
			
			if (days > 0)
				result += GoldenApple.getInstance().locale.processMessageDefaultLocale("time.days", days + "") + ", ";
			
			if (hours > 0)
				result += GoldenApple.getInstance().locale.processMessageDefaultLocale("time.hours", hours + "") + ", ";
			
			if (minutes > 0)
				result += GoldenApple.getInstance().locale.processMessageDefaultLocale("time.minutes", minutes + "") + ", ";
			
			if (seconds > 0)
				result += GoldenApple.getInstance().locale.processMessageDefaultLocale("time.seconds", seconds + "") + ", ";
			
			if (result.length() > 0) {
				result = result.substring(0, result.length() - 3);
			}
			
			return result;
		}
		
		public static RemainingTime parseTime(String input) throws NumberFormatException {
			long seconds = 0;
			
			while (input.length() > 0) {
				int i;
				for (i = 0; i <= input.length(); i++) {
					if (i == input.length()) {
						seconds += Integer.parseInt(input.substring(0, i - 1));
						input = "";
						break;
					} else if (!Character.isDigit((input.charAt(i)))) {
						Character c = Character.toLowerCase(input.charAt(i));
						if (c == 'd') {
							seconds += Integer.parseInt(input.substring(0, i - 1)) * 86400;
						} else if (c == 'h') {
							seconds += Integer.parseInt(input.substring(0, i - 1)) * 3600;
						} else if (c == 'm') {
							seconds += Integer.parseInt(input.substring(0, i - 1)) * 60;
						} else if (c == 's') {
							seconds += Integer.parseInt(input.substring(0, i - 1));
						} else {
							throw new NumberFormatException();
						}
						
						input = input.substring(i + 1);
						break;
					}
				}
			}
			
			return new RemainingTime(seconds);
		}
	}
	
	public static RemainingTime timeBetween(Timestamp time1, Timestamp time2) {
		return new RemainingTime((time1.getTime() - time2.getTime()) / 1000);
	}
	
	public static Timestamp add(Timestamp time, RemainingTime timeAdd) {
		return new Timestamp(time.getTime() + timeAdd.getTotalSeconds() * 1000);
	}
}
