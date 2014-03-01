package com.bendude56.goldenapple.chat;

import com.bendude56.goldenapple.CommandManager;
import com.bendude56.goldenapple.ModuleLoader;
import com.bendude56.goldenapple.commands.ChannelCommand;
import com.bendude56.goldenapple.commands.LemonPledgeCommand;
import com.bendude56.goldenapple.commands.MeCommand;
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
	}

	@Override
	protected void registerPermissions(PermissionManager permissions) {
		ChatManager.chatNode = permissions.registerNode("chat", PermissionManager.goldenAppleNode);
		
		ChatManager.channelsNode = permissions.registerNode("channels", ChatManager.chatNode);
		ChatManager.channelAddPermission = permissions.registerPermission("add", ChatManager.channelsNode);
		ChatManager.channelModPermission = permissions.registerPermission("mod", ChatManager.channelsNode);
		ChatManager.channelAdminPermission = permissions.registerPermission("admin", ChatManager.channelsNode);
	}
	
	@Override
	protected void registerCommands(CommandManager commands) {
		commands.getCommand("gachannel").register();
		commands.getCommand("game").register();
		commands.getCommand("galemonpledge").register();
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
	public void clearCache() {
		// TODO Reload chat channels from database
	}
	
	@Override
	protected void unregisterPermissions(PermissionManager permissions) {
		ChatManager.chatNode = null;
		
		ChatManager.channelsNode = null;
		ChatManager.channelAddPermission = null;
		ChatManager.channelModPermission = null;
		ChatManager.channelAdminPermission = null;
	}
	
	@Override
	protected void unregisterCommands(CommandManager commands) {
		commands.getCommand("gachannel").unregister();
		commands.getCommand("game").unregister();
		commands.getCommand("galemonpledge").unregister();
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
