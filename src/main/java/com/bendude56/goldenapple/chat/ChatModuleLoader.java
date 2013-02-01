package com.bendude56.goldenapple.chat;

import org.bukkit.Bukkit;

import com.bendude56.goldenapple.GoldenApple;
import com.bendude56.goldenapple.IModuleLoader;
import com.bendude56.goldenapple.ModuleLoadException;
import com.bendude56.goldenapple.commands.ChannelCommand;
import com.bendude56.goldenapple.listener.ChatListener;
import com.bendude56.goldenapple.permissions.PermissionManager;

public class ChatModuleLoader implements IModuleLoader {

	private static ModuleState	state	= ModuleState.UNLOADED_USER;

	@Override
	public void loadModule(GoldenApple instance) throws ModuleLoadException {
		state = ModuleState.LOADING;
		try {
			registerPermissions(instance.permissions);
			
			ChatCensor.loadCensors();
			
			instance.chat = new ChatManager();
			registerEvents();
			registerCommands();
			
			state = ModuleState.LOADED;
		} catch (Throwable e) {
			state = ModuleState.UNLOADED_ERROR;

			instance.chat = null;
			unregisterEvents();
			unregisterCommands();
			
			ChatCensor.unloadCensors();
			
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

	public void registerCommands() {
		Bukkit.getPluginCommand("gachannel").setExecutor(new ChannelCommand());
	}

	public void unregisterEvents() {
		ChatListener.stopListening();
	}

	public void unregisterCommands() {
		Bukkit.getPluginCommand("gachannel").setExecutor(GoldenApple.defCmd);
	}

	@Override
	public void unloadModule(GoldenApple instance) {
		unregisterEvents();
		unregisterCommands();
		instance.chat = null;
		
		ChatCensor.unloadCensors();

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
		return GoldenApple.getInstance().mainConfig.getBoolean("modules.chat.enabled", true);
	}

	@Override
	public boolean canPolicyLoad() {
		return !GoldenApple.getInstance().mainConfig.getBoolean("securityPolicy.blockModules.chat", false);
	}
	
	@Override
	public boolean canPolicyUnload() {
		return !GoldenApple.getInstance().mainConfig.getBoolean("securityPolicy.blockManualUnload.chat", false);
	}

}
