package com.bendude56.goldenapple.warp;

import com.bendude56.goldenapple.GoldenApple;
import com.bendude56.goldenapple.IModuleLoader;
import com.bendude56.goldenapple.ModuleLoadException;
import com.bendude56.goldenapple.listener.WarpListener;
import com.bendude56.goldenapple.permissions.PermissionManager;

public class WarpModuleLoader implements IModuleLoader {

	private static ModuleState state = ModuleState.UNLOADED_USER;
	
	private boolean loadWarps;
	private boolean loadHomes;
	private boolean loadCheckpoints;
	
	@Override
	public void loadModule(GoldenApple instance) throws ModuleLoadException
	{
		state = ModuleState.LOADING;
		try
		{
			instance.warp = new WarpManager();
			
			registerPermissions(instance.permissions);
			registerEvents();
			registerCommands();
		}
		catch (Throwable e)
		{
			state = ModuleState.UNLOADED_ERROR;
			
			instance.warp = null;
			unregisterEvents();
			unregisterCommands();
		}
	}

	@Override
	public void registerPermissions(PermissionManager permissions)
	{
		if (loadWarps)
		{
			WarpManager.warpNode = permissions.registerNode("warp", PermissionManager.goldenAppleNode);
			WarpManager.warpAdd = permissions.registerPermission("add", WarpManager.warpNode);
			WarpManager.warpEdit = permissions.registerPermission("edit", WarpManager.warpNode);
			WarpManager.warpRemove = permissions.registerPermission("remove", WarpManager.warpNode);
			WarpManager.warpWarpAll = permissions.registerPermission("all", WarpManager.warpNode);
		}
		if (loadHomes)
		{
			WarpManager.homeNode = permissions.registerNode("home", PermissionManager.goldenAppleNode);
			WarpManager.homeNodeOwn = permissions.registerNode("own", WarpManager.homeNode);
			WarpManager.homeNodeAll = permissions.registerNode("all", WarpManager.homeNode);
			
			WarpManager.homeAddOwn = permissions.registerPermission("add", WarpManager.homeNodeOwn);
			WarpManager.homeEditOwn = permissions.registerPermission("edit", WarpManager.homeNodeOwn);
			WarpManager.homeRemoveOwn = permissions.registerPermission("remove", WarpManager.homeNodeOwn);
			WarpManager.homeWarpOwn = permissions.registerPermission("warp", WarpManager.homeNodeOwn);
			
			WarpManager.homeAddAll = permissions.registerPermission("add", WarpManager.homeNodeAll);
			WarpManager.homeEditAll = permissions.registerPermission("edit", WarpManager.homeNodeAll);
			WarpManager.homeRemoveAll = permissions.registerPermission("remove", WarpManager.homeNodeAll);
			WarpManager.homeWarpAll = permissions.registerPermission("warp", WarpManager.homeNodeAll);
		}
		if (loadCheckpoints)
		{
			WarpManager.checkpointNode = permissions.registerNode("checkpoints", PermissionManager.goldenAppleNode);
			WarpManager.checkpointNodeOwn = permissions.registerNode("own", WarpManager.checkpointNode);
			WarpManager.checkpointNodeAll = permissions.registerNode("all", WarpManager.checkpointNode);
			
			WarpManager.checkpointAddOwn = permissions.registerPermission("add", WarpManager.checkpointNodeOwn);
			WarpManager.checkpointRemoveOwn = permissions.registerPermission("remove", WarpManager.checkpointNodeOwn);
			WarpManager.checkpointWarpOwn = permissions.registerPermission("warp", WarpManager.checkpointNodeOwn);
			
			WarpManager.checkpointAddAll = permissions.registerPermission("add", WarpManager.checkpointNodeAll);
			WarpManager.checkpointRemoveAll = permissions.registerPermission("remove", WarpManager.checkpointNodeAll);
			WarpManager.checkpointWarpAll = permissions.registerPermission("warp", WarpManager.checkpointNodeAll);
			
		}
	}

	public void registerEvents()
	{
		WarpListener.registerEvents();
	}

	public void registerCommands()
	{
		// TODO Register Commands
	}

	public void unregisterEvents()
	{
		WarpListener.unregisterEvents();
	}

	public void unregisterCommands()
	{
		// TODO Unregister Commands
	}

	@Override
	public void unloadModule(GoldenApple instance)
	{
		unregisterEvents();
		unregisterCommands();
		instance.warp = null;

		state = ModuleState.UNLOADED_USER;
	}

	@Override
	public String getModuleName()
	{
		return "Warp";
	}

	@Override
	public ModuleState getCurrentState()
	{
		return WarpModuleLoader.state;
	}

	@Override
	public void setState(ModuleState state)
	{
		WarpModuleLoader.state = state;
	}

	@Override
	public String[] getModuleDependencies()
	{
		return new String[] { "Permissions" };
	}

	@Override
	public boolean canLoadAuto() {
		return GoldenApple.getInstance().mainConfig.getBoolean("modules.warp.enabled", true);
	}

	@Override
	public boolean canPolicyLoad() {
		return !GoldenApple.getInstance().mainConfig.getBoolean("securityPolicy.blockModules.warp", false);
	}

}
