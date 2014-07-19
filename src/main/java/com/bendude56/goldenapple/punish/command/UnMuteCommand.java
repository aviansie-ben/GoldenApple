package com.bendude56.goldenapple.punish.command;

import org.bukkit.Bukkit;

import com.bendude56.goldenapple.GoldenApple;
import com.bendude56.goldenapple.SimpleCommandManager;
import com.bendude56.goldenapple.User;
import com.bendude56.goldenapple.ModuleLoader.ModuleState;
import com.bendude56.goldenapple.chat.ChatManager;
import com.bendude56.goldenapple.chat.IChatChannel;
import com.bendude56.goldenapple.command.GoldenAppleCommand;
import com.bendude56.goldenapple.permissions.IPermissionUser;
import com.bendude56.goldenapple.permissions.PermissionManager;

public class UnMuteCommand extends GoldenAppleCommand {

	@Override
	public boolean onExecute(GoldenApple instance, User user, String commandLabel, String[] args) {
		if (GoldenApple.getInstance().getModuleManager().getModule("Chat").getCurrentState() != ModuleState.LOADED) {
			return SimpleCommandManager.defaultCommand.onCommand(user.getHandle(), Bukkit.getPluginCommand("gaunmute"), commandLabel, args);
		} else {
			if (args.length != 1) return false;
			
			user.sendLocalizedMessage("header.punish");
			
			IPermissionUser target = PermissionManager.getInstance().findUser(args[0], false);
			IChatChannel c = ChatManager.getInstance().getActiveChannel(user);
			
			if (c == null) {
				user.sendLocalizedMessage("error.channel.notInChannelCommand");
			} else if (target == null) {
				user.sendLocalizedMessage("shared.userNotFoundError", args[0]);
			} else {
				MuteCommand.muteVoid(target, c, user, commandLabel, args);
			}
			
			return true;
		}
	}

}
