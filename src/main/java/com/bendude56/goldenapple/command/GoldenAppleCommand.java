package com.bendude56.goldenapple.command;

import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;

import com.bendude56.goldenapple.GoldenApple;
import com.bendude56.goldenapple.PerformanceMonitor.PerformanceEvent;
import com.bendude56.goldenapple.User;

public abstract class GoldenAppleCommand implements CommandExecutor {

	@Override
	public final boolean onCommand(CommandSender sender, Command command, String commandLabel, String[] args) {
		PerformanceEvent e = GoldenApple.getInstancePerformanceMonitor().createForCommand(GoldenApple.getInstance().getCommandManager().getCommand(command.getName()).getModule(), commandLabel, args);
		e.start();
		
		try {
			return onExecute(GoldenApple.getInstance(), User.getUser(sender), commandLabel, args);
		} finally {
			e.stop();
		}
	}
	
	public abstract boolean onExecute(GoldenApple instance, User user, String commandLabel, String[] args);

}
