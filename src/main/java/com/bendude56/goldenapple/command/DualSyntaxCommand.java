package com.bendude56.goldenapple.command;

import com.bendude56.goldenapple.GoldenApple;
import com.bendude56.goldenapple.User;

public abstract class DualSyntaxCommand extends GoldenAppleCommand {

	@Override
	public final boolean onExecute(GoldenApple instance, User user, String commandLabel, String[] args) {
		if (user.getVariableBoolean("goldenapple.complexSyntax"))
			onExecuteComplex(instance, user, commandLabel, args);
		else
			onExecuteSimple(instance, user, commandLabel, args);
		
		return true;
	}
	
	public abstract void onExecuteComplex(GoldenApple instance, User user, String commandLabel, String[] args);
	public abstract void onExecuteSimple(GoldenApple instance, User user, String commandLabel, String[] args);

}
