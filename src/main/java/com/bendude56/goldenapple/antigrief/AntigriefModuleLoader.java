package com.bendude56.goldenapple.antigrief;

import com.bendude56.goldenapple.GoldenApple;
import com.bendude56.goldenapple.ModuleLoader;
import com.bendude56.goldenapple.listener.AntigriefListener;
import com.bendude56.goldenapple.permissions.PermissionManager;
import com.bendude56.goldenapple.permissions.PermissionManager.Permission;
import com.bendude56.goldenapple.permissions.PermissionManager.PermissionNode;

public class AntigriefModuleLoader implements ModuleLoader {
	
	public static PermissionNode antigriefNode;
	public static Permission tntPermission;

	private static ModuleState state = ModuleState.UNLOADED_USER;

	@Override
	public void loadModule(GoldenApple instance) {
		state = ModuleState.LOADING;
		try {
			registerPermissions(PermissionManager.getInstance());
			registerEvents();
			registerCommands();
		} catch (Throwable e) {
			state = ModuleState.UNLOADED_ERROR;
			// TODO Add cleanup code to clean up after failed module start
		}
		state = ModuleState.LOADED;
	}
	
	@Override
	public void registerPermissions(PermissionManager permissions) {
		antigriefNode = permissions.registerNode("antigrief", PermissionManager.goldenAppleNode);
		tntPermission = permissions.registerPermission("tnt", antigriefNode);
	}
	
	private void registerEvents() {
		AntigriefListener.startListening();
	}
	
	private void registerCommands() {
	}

	@Override
	public void unloadModule(GoldenApple instance) {
		AntigriefListener.stopListening();
		state = ModuleState.UNLOADED_USER;
	}

	@Override
	public String getModuleName() {
		return "Antigrief";
	}

	@Override
	public ModuleState getCurrentState() {
		return state;
	}
	
	@Override
	public void setState(ModuleState state) {
		AntigriefModuleLoader.state = state;
	}

	@Override
	public String[] getModuleDependencies() {
		return new String[] { "Base" };
	}

	@Override
	public boolean canLoadAuto() {
		return GoldenApple.getInstanceMainConfig().getBoolean("modules.antigrief.enabled", true);
	}

	@Override
	public boolean canPolicyLoad() {
		return !GoldenApple.getInstanceMainConfig().getBoolean("securityPolicy.blockModules.antigrief", false);
	}
	
	@Override
	public boolean canPolicyUnload() {
		return !GoldenApple.getInstanceMainConfig().getBoolean("securityPolicy.blockManualUnload.antigrief", false);
	}

}
