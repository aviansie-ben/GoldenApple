package com.bendude56.goldenapple.commands;

import com.bendude56.goldenapple.GoldenApple;
import com.bendude56.goldenapple.User;

public class LangCommand extends GoldenAppleCommand {

	@Override
	public boolean onExecute(GoldenApple instance, User user, String commandLabel, String[] args) {
		if (args.length != 1)
			return false;
		
		if (user.isServer()) {
			user.sendLocalizedMessage("shared.noConsole");
		} else if (GoldenApple.getInstance().getLocalizationManager().languageExists(args[0])) {
			user.setPreferredLocale(args[0]);
			user.sendLocalizedMessage("general.lang.set", args[0]);
		} else {
			user.sendLocalizedMessage("error.lang.notFound", args[0]);
		}
		
		return true;
	}

}
