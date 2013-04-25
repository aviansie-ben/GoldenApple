package com.bendude56.goldenapple.chat;

import com.bendude56.goldenapple.CommandManager;
import com.bendude56.goldenapple.GoldenApple;
import com.bendude56.goldenapple.ModuleLoader;
import com.bendude56.goldenapple.ModuleLoadException;
import com.bendude56.goldenapple.listener.ChatListener;
import com.bendude56.goldenapple.permissions.PermissionManager;

public class ChatModuleLoader implements ModuleLoader {

	private static ModuleState	state	= ModuleState.UNLOADED_USER;

	@Override
	public void loadModule(GoldenApple instance) throws ModuleLoadException {
		state = ModuleState.LOADING;
		try {
			registerPermissions(PermissionManager.getInstance());
			
			SimpleChatCensor.loadCensors();
			
			ChatManager.instance = new SimpleChatManager();
			registerEvents();
			registerCommands(instance.getCommandManager());
			
			state = ModuleState.LOADED;
		} catch (Throwable e) {
			state = ModuleState.UNLOADED_ERROR;

			ChatManager.instance = null;
			unregisterEvents();
			unregisterCommands(instance.getCommandManager());
			
			SimpleChatCensor.unloadCensors();
			
			throw new ModuleLoadException("Chat", e);
		}
	}

	@Override
	public void registerPermissions(PermissionManager permissions) {
		ChatManager.chatNode = permissions.registerNode("chat", PermissionManager.goldenAppleNode);
		
		ChatManager.channelsNode = permissions.registerNode("channels", ChatManager.chatNode);
		ChatManager.channelAddPermission = permissions.registerPermission("add", ChatManager.channelsNode);
		ChatManager.channelModPermission = permissions.registerPermission("mod", ChatManager.channelsNode);
		ChatManager.channelAdminPermission = permissions.registerPermission("admin", ChatManager.channelsNode);
	}

	public void registerEvents() {
		ChatListener.startListening();
	}

	public void registerCommands(CommandManager commands) {
		commands.getCommand("gachannel").register();
		commands.getCommand("game").register();
		commands.getCommand("galemonpledge").register();
	}

	public void unregisterEvents() {
		ChatListener.stopListening();
	}

	public void unregisterCommands(CommandManager commands) {
		commands.getCommand("gachannel").unregister();
		commands.getCommand("game").unregister();
		commands.getCommand("galemonpledge").unregister();
	}

	@Override
	public void unloadModule(GoldenApple instance) {
		unregisterEvents();
		unregisterCommands(instance.getCommandManager());
		ChatManager.instance = null;
		
		SimpleChatCensor.unloadCensors();

		state = ModuleState.UNLOADED_USER;
	}

	@Override
	public String getModuleName() {
		return "Chat";
	}

	@Override
	public ModuleState getCurrentState() {
		return state;
	}

	@Override
	public void setState(ModuleState state) {
		ChatModuleLoader.state = state;
	}

	@Override
	public String[] getModuleDependencies() {
		return new String[] { "Permissions" };
	}

	@Override
	public boolean canLoadAuto() {
		return GoldenApple.getInstanceMainConfig().getBoolean("modules.chat.enabled", true);
	}

	@Override
	public boolean canPolicyLoad() {
		return !GoldenApple.getInstanceMainConfig().getBoolean("securityPolicy.blockModules.chat", false);
	}
	
	@Override
	public boolean canPolicyUnload() {
		return !GoldenApple.getInstanceMainConfig().getBoolean("securityPolicy.blockManualUnload.chat", false);
	}

}
