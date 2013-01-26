package com.bendude56.goldenapple.commands;

import com.bendude56.goldenapple.GoldenApple;
import com.bendude56.goldenapple.User;

public class ChannelCommand extends DualSyntaxCommand {
	@Override
	public void onCommandComplex(GoldenApple instance, User user, String commandLabel, String[] args) {
		
	}

	public void onCommandSimple(GoldenApple instance, User user, String commandLabel, String[] args) {
		
	}

	private void sendHelp(User user, String commandLabel, boolean complex) {
		GoldenApple.getInstance().locale.sendMessage(user, "header.help", false);
		GoldenApple.getInstance().locale.sendMessage(user, (complex) ? "help.lock.complex" : "help.lock.simple", true, commandLabel);
	}
}
