package com.bendude56.goldenapple.chat;

import com.bendude56.goldenapple.GoldenApple;
import com.bendude56.goldenapple.IModuleLoader;
import com.bendude56.goldenapple.ModuleLoadException;
import com.bendude56.goldenapple.permissions.PermissionManager;

public class ChatModuleLoader implements IModuleLoader {

	private static ModuleState state = ModuleState.UNLOADED_USER;
	
	@Override
	public void loadModule(GoldenApple instance) throws ModuleLoadException
	{
		state = ModuleState.LOADING;
		try
		{
			instance.chat = new ChatManager();
			registerPermissions(instance.permissions);
			registerEvents();
			registerCommands();
		}
		catch (Throwable e)
		{
			state = ModuleState.UNLOADED_ERROR;
			// TODO Add cleanup code to clean up after failed module start
		}
		state = ModuleState.LOADED;
	}

	@Override
	public void registerPermissions(PermissionManager permissions)
	{
		ChatManager.chatNode = permissions.registerNode("chat", PermissionManager.goldenAppleNode);
		ChatManager.channelsNode = permissions.registerNode("channels", ChatManager.chatNode);
		ChatManager.muteNode = permissions.registerNode("mute", ChatManager.chatNode);
		ChatManager.censorNode = permissions.registerNode("censor", ChatManager.chatNode);
		
		ChatManager.channelCreatePermission = permissions.registerPermission("add", ChatManager.channelsNode);
		ChatManager.channelEditPermission = permissions.registerPermission("edit", ChatManager.channelsNode);
		ChatManager.channelDeletePermission = permissions.registerPermission("remove", ChatManager.channelsNode);
		
		ChatManager.muteTimedPermission = permissions.registerPermission("timed", ChatManager.muteNode);
		ChatManager.mutePermanentPermission = permissions.registerPermission("permanent", ChatManager.muteNode);
		ChatManager.muteUnmutePermission = permissions.registerPermission("unmute", ChatManager.muteNode);
		ChatManager.muteImmunePermission = permissions.registerPermission("immune", ChatManager.muteNode);
		
		ChatManager.censorAddWordPermission = permissions.registerPermission("add", ChatManager.censorNode);
		ChatManager.censorRemoveWordPermission = permissions.registerPermission("remove", ChatManager.censorNode);
		ChatManager.censorImmunePermission = permissions.registerPermission("immune", ChatManager.censorNode);
	}

	public void registerEvents()
	{
		// TODO Make ChatListener
		// TODO Register Events
	}

	public void registerCommands()
	{
		// TODO Register Commands
	}

	@Override
	public void unloadModule(GoldenApple instance) {
		// TODO Stop events listening
		// TODO Unregister commands
		instance.chat = null;
		
		state = ModuleState.UNLOADED_USER;
	}

	@Override
	public String getModuleName()
	{
		return "Chat";
	}

	@Override
	public ModuleState getCurrentState()
	{
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

}
