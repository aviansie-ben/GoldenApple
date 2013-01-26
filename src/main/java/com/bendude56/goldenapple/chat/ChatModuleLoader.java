package com.bendude56.goldenapple.chat;

import com.bendude56.goldenapple.GoldenApple;
import com.bendude56.goldenapple.IModuleLoader;
import com.bendude56.goldenapple.ModuleLoadException;
import com.bendude56.goldenapple.listener.ChatListener;
import com.bendude56.goldenapple.permissions.PermissionManager;

public class ChatModuleLoader implements IModuleLoader {

	private static ModuleState	state	= ModuleState.UNLOADED_USER;

	@Override
	public void loadModule(GoldenApple instance) throws ModuleLoadException {
		state = ModuleState.LOADING;
		try {
			instance.chat = new ChatManager();
			registerPermissions(instance.permissions);
			registerEvents();
			registerCommands();
			state = ModuleState.LOADED;
		} catch (Throwable e) {
			state = ModuleState.UNLOADED_ERROR;

			instance.chat = null;
			unregisterEvents();
			unregisterCommands();
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
		// TODO Register Commands
	}

	public void unregisterEvents() {
		ChatListener.stopListening();
	}

	public void unregisterCommands() {
		// TODO Unregister Commands
	}

	@Override
	public void unloadModule(GoldenApple instance) {
		unregisterEvents();
		unregisterCommands();
		instance.chat = null;

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
