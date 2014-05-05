package com.bendude56.goldenapple.chat;

import com.bendude56.goldenapple.CommandManager;
import com.bendude56.goldenapple.ModuleLoader;
import com.bendude56.goldenapple.User;
import com.bendude56.goldenapple.chat.command.AfkCommand;
import com.bendude56.goldenapple.chat.command.ChannelCommand;
import com.bendude56.goldenapple.chat.command.LemonPledgeCommand;
import com.bendude56.goldenapple.chat.command.MeCommand;
import com.bendude56.goldenapple.chat.command.ReplyCommand;
import com.bendude56.goldenapple.chat.command.TellCommand;
import com.bendude56.goldenapple.chat.command.TellSpyCommand;
import com.bendude56.goldenapple.permissions.PermissionManager;

public class ChatModuleLoader extends ModuleLoader {
	
	public ChatModuleLoader() {
		super("Chat", new String[] { "Permissions" }, "modules.chat.enabled", "securityPolicy.blockModules.chat", "securityPolicy.blockManualUnload.chat");
	}
	
	@Override
	protected void preregisterCommands(CommandManager commands) {
		commands.insertCommand("gachannel", "Chat", new ChannelCommand());
		commands.insertCommand("game", "Chat", new MeCommand());
		commands.insertCommand("galemonpledge", "Chat", new LemonPledgeCommand());
		commands.insertCommand("gatell", "Chat", new TellCommand());
		commands.insertCommand("gatellspy", "Chat", new TellSpyCommand());
		commands.insertCommand("gareply", "Chat", new ReplyCommand());
		commands.insertCommand("gaafk", "Chat", new AfkCommand());
	}

	@Override
	protected void registerPermissions(PermissionManager permissions) {
		ChatManager.chatNode = permissions.registerNode("chat", PermissionManager.goldenAppleNode);
		ChatManager.tellPermission = permissions.registerPermission("tell", ChatManager.chatNode);
		ChatManager.tellSpyPermission = permissions.registerPermission("tellSpy", ChatManager.chatNode);
		
		ChatManager.channelsNode = permissions.registerNode("channels", ChatManager.chatNode);
		ChatManager.channelAddPermission = permissions.registerPermission("add", ChatManager.channelsNode);
		ChatManager.channelModPermission = permissions.registerPermission("mod", ChatManager.channelsNode);
		ChatManager.channelAdminPermission = permissions.registerPermission("admin", ChatManager.channelsNode);
		
		User.setGlobalNegative("bukkit.command.tell");
		User.setGlobalNegative("bukkit.command.me");
		
		User.setGlobalNegative("minecraft.command.tell");
		User.setGlobalNegative("minecraft.command.me");
	}
	
	@Override
	protected void registerCommands(CommandManager commands) {
		commands.getCommand("gachannel").register();
		commands.getCommand("game").register();
		commands.getCommand("galemonpledge").register();
		commands.getCommand("gatell").register();
		commands.getCommand("gatellspy").register();
		commands.getCommand("gareply").register();
		commands.getCommand("gaafk").register();
	}

	@Override
	protected void registerListener() {
		ChatListener.startListening();
	}
	
	@Override
	protected void initializeManager() {
		SimpleChatCensor.loadCensors();
		ChatManager.instance = new SimpleChatManager();
	}
	
	@Override
	protected void postInit() {
	    ChatManager.getInstance().postInit();
	}
	
	@Override
	public void clearCache() {
		// TODO Reload chat channels from database
	}
	
	@Override
	protected void unregisterPermissions(PermissionManager permissions) {
		ChatManager.chatNode = null;
		ChatManager.tellPermission = null;
		ChatManager.tellSpyPermission = null;
		
		ChatManager.channelsNode = null;
		ChatManager.channelAddPermission = null;
		ChatManager.channelModPermission = null;
		ChatManager.channelAdminPermission = null;
		
		User.unsetGlobalNegative("bukkit.command.tell");
        User.unsetGlobalNegative("bukkit.command.me");
        
        User.unsetGlobalNegative("minecraft.command.tell");
        User.unsetGlobalNegative("minecraft.command.me");
	}
	
	@Override
	protected void unregisterCommands(CommandManager commands) {
		commands.getCommand("gachannel").unregister();
		commands.getCommand("game").unregister();
		commands.getCommand("galemonpledge").unregister();
		commands.getCommand("gatell").unregister();
		commands.getCommand("gatellspy").unregister();
		commands.getCommand("gareply").unregister();
		commands.getCommand("gaafk").unregister();
	}
	
	@Override
	protected void unregisterListener() {
		ChatListener.stopListening();
	}
	
	@Override
	protected void destroyManager() {
		SimpleChatCensor.unloadCensors();
		ChatManager.instance = null;
	}
}
