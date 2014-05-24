package com.bendude56.goldenapple.permissions.command;

import com.bendude56.goldenapple.GoldenApple;
import com.bendude56.goldenapple.User;
import com.bendude56.goldenapple.command.GoldenAppleCommand;

public class LangCommand extends GoldenAppleCommand {

	@Override
	public boolean onExecute(GoldenApple instance, User user, String commandLabel, String[] args) {
		if (args.length != 1)
			return false;
		
		if (user.isServer()) {
			user.sendLocalizedMessage("shared.noConsole");
		} else if (GoldenApple.getInstance().getLocalizationManager().languageExists(args[0])) {
			user.setVariable("goldenapple.locale", args[0]);
			user.sendLocalizedMessage("general.lang.set", args[0]);
		} else {
			user.sendLocalizedMessage("error.lang.notFound", args[0]);
		}
		
		return true;
	}

}
