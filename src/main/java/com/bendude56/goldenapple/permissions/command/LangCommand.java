package com.bendude56.goldenapple.permissions.command;

import com.bendude56.goldenapple.GoldenApple;
import com.bendude56.goldenapple.User;
import com.bendude56.goldenapple.LocalizationManager.Locale;
import com.bendude56.goldenapple.command.GoldenAppleCommand;

public class LangCommand extends GoldenAppleCommand {

	@Override
	public boolean onExecute(GoldenApple instance, User user, String commandLabel, String[] args) {
		if (args.length != 1)
			return false;
		
		if (user.isServer()) {
			user.sendLocalizedMessage("shared.consoleNotAllowed");
		} else if (GoldenApple.getInstance().getLocalizationManager().isLocalePresent(args[0])) {
		    Locale l = GoldenApple.getInstance().getLocalizationManager().getLocale(args[0]);
			
		    user.setVariable("goldenapple.locale", l.getShortName());
			user.sendLocalizedMessage("module.permissions.locale.set", l.getLongName());
		} else {
			user.sendLocalizedMessage("module.permissions.locale.notFound", args[0]);
		}
		
		return true;
	}

}
