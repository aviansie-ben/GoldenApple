package com.bendude56.goldenapple.commands;

import java.util.ArrayDeque;
import java.util.ArrayList;

import org.bukkit.ChatColor;

import com.bendude56.goldenapple.GoldenApple;
import com.bendude56.goldenapple.ModuleLoader;
import com.bendude56.goldenapple.ModuleLoader.ModuleState;
import com.bendude56.goldenapple.User;
import com.bendude56.goldenapple.permissions.PermissionManager;

public class ModuleCommand extends GoldenAppleCommand {
	@Override
	public boolean onExecute(GoldenApple instance, User user, String commandLabel, String[] args) {
		if (user.getHandle().isOp() || user.hasPermission(PermissionManager.moduleQueryPermission)) {
			if (args.length == 0 || args[0].equalsIgnoreCase("-ls") || args[0].equalsIgnoreCase("--list")) {
				user.sendLocalizedMessage("header.module");
				user.sendLocalizedMessage("general.module.list");
				for (ModuleLoader module : GoldenApple.getInstance().getModuleManager().getModules()) {
					String suffix = "";
					if (!module.canPolicyLoad() || (module.getCurrentState() == ModuleState.LOADED && !module.canPolicyUnload())) {
						suffix += ChatColor.DARK_GRAY + " [!]";
					}
					switch (module.getCurrentState()) {
						case BUSY:
							user.getHandle().sendMessage(ChatColor.YELLOW + module.getModuleName() + suffix);
							break;
						case LOADED:
							user.getHandle().sendMessage(ChatColor.GREEN + module.getModuleName() + suffix);
							break;
						case LOADING:
							user.getHandle().sendMessage(ChatColor.YELLOW + module.getModuleName() + suffix);
							break;
						case UNLOADED_USER:
							user.getHandle().sendMessage(ChatColor.GRAY + module.getModuleName() + suffix);
							break;
						case UNLOADED_ERROR:
						case UNLOADED_MISSING_DEPENDENCY:
							user.getHandle().sendMessage(ChatColor.RED + module.getModuleName() + suffix);
							break;
					}
				}
			} else if (args[0].equalsIgnoreCase("help") || args[0].equalsIgnoreCase("-?")) {
				// TODO Implement help
			} else if (GoldenApple.getInstance().getModuleManager().getModule(args[0]) != null) {
				user.sendLocalizedMessage("header.module");
				ModuleLoader module = GoldenApple.getInstance().getModuleManager().getModule(args[0]);
				if (args.length == 1 || args[1].equalsIgnoreCase("-q") || args[1].equalsIgnoreCase("--query")) {
					String status = "???";
					if (module.canPolicyLoad()) {
						switch (module.getCurrentState()) {
							case BUSY:
								status = GoldenApple.getInstance().getLocalizationManager().getMessage(user, "general.module.query.busy");
								break;
							case LOADED:
								if (module.canPolicyUnload())
									status = GoldenApple.getInstance().getLocalizationManager().getMessage(user, "general.module.query.loaded");
								else
									status = GoldenApple.getInstance().getLocalizationManager().getMessage(user, "general.module.query.loadedLocked");
								break;
							case LOADING:
								status = GoldenApple.getInstance().getLocalizationManager().getMessage(user, "general.module.query.loading");
								break;
							case UNLOADED_USER:
								status = GoldenApple.getInstance().getLocalizationManager().getMessage(user, "general.module.query.unloadedUser");
								break;
							case UNLOADED_ERROR:
								status = GoldenApple.getInstance().getLocalizationManager().getMessage(user, "general.module.query.unloadedError");
								break;
							case UNLOADED_MISSING_DEPENDENCY:
								status = GoldenApple.getInstance().getLocalizationManager().getMessage(user, "general.module.query.unloadedDepend");
								break;
						}
					} else {
						status = GoldenApple.getInstance().getLocalizationManager().getMessage(user, "general.module.query.unloadedLocked");
					}
					user.sendLocalizedMultilineMessage("general.module.query", module.getModuleName(), status);
				} else if (args[1].equalsIgnoreCase("-e") || args[1].equalsIgnoreCase("--enable")) {
					if (!user.hasPermission(PermissionManager.moduleLoadPermission)) {
						GoldenApple.logPermissionFail(user, commandLabel, args, true);
					} else if (module.getCurrentState() == ModuleState.LOADED || module.getCurrentState() == ModuleState.LOADING) {
						user.sendLocalizedMessage("error.module.load.alreadyLoaded", module.getModuleName());
					} else if (!module.canPolicyLoad()) {
						user.sendLocalizedMessage("error.module.load.policy", module.getModuleName());
					} else {
						ArrayDeque<ModuleLoader> depend = new ArrayDeque<ModuleLoader>();
						ArrayList<ModuleLoader> oldDepend = new ArrayList<ModuleLoader>();
						oldDepend.add(module);
						while (!oldDepend.isEmpty()) {
							ArrayList<ModuleLoader> newDepend = new ArrayList<ModuleLoader>();
							for (ModuleLoader m : oldDepend) {
								for (String d : m.getModuleDependencies()) {
									if (GoldenApple.getInstance().getModuleManager().getModule(d) == null) {
										user.sendLocalizedMessage("error.module.load.dependFail", module.getModuleName(), d);
										return true;
									} else if (!GoldenApple.getInstance().getModuleManager().getModule(d).canPolicyLoad()) {
										user.sendLocalizedMessage("error.module.load.dependFail", module.getModuleName(), d);
										return true;
									} else if (GoldenApple.getInstance().getModuleManager().getModule(d).getCurrentState() != ModuleState.LOADED) {
										newDepend.add(GoldenApple.getInstance().getModuleManager().getModule(d));
									}
								}
							}
							oldDepend.clear();
							oldDepend.addAll(newDepend);
							depend.addAll(newDepend);
						}
						if (!depend.isEmpty()) {
							if (args.length != 2 && args[2].equalsIgnoreCase("-v")) {
								for (ModuleLoader mLoad = depend.pollLast(); mLoad != null; mLoad = depend.pollLast()) {
									try {
										if (!GoldenApple.getInstance().getModuleManager().enableModule(mLoad.getModuleName(), false, user.getName())) {
											user.sendLocalizedMessage("error.module.load.dependFail", module.getModuleName(), mLoad.getModuleName());
											return true;
										} else {
											user.sendLocalizedMessage("general.module.load.success", mLoad.getModuleName());
										}
									} catch (Throwable t) {
										user.sendLocalizedMessage("error.module.load.dependFail", module.getModuleName(), mLoad.getModuleName());
										return true;
									}
								}
							} else {
								user.sendLocalizedMessage("general.module.load.warnStart");
								String dependStr = depend.pollLast().getModuleName();
								for (ModuleLoader mLoad = depend.pollLast(); mLoad != null; mLoad = depend.pollLast()) {
									dependStr += ", " + mLoad.getModuleName();
								}
								user.getHandle().sendMessage(dependStr);
								user.sendLocalizedMessage("general.module.load.warnEnd");
								String cmd = commandLabel;
								for (String arg : args)
									cmd += " " + arg;
								cmd += " -v";
								VerifyCommand.commands.put(user, cmd);
								return true;
							}
						}
						try {
							if (!GoldenApple.getInstance().getModuleManager().enableModule(module.getModuleName(), false, user.getName())) {
								user.sendLocalizedMessage("error.module.load.unknown", module.getModuleName());
								return true;
							} else {
								user.sendLocalizedMessage("general.module.load.success", module.getModuleName());
							}
						} catch (Throwable t) {
							user.sendLocalizedMessage("error.module.load.unknown", module.getModuleName());
							return true;
						}
					}
				} else if (args[1].equalsIgnoreCase("-d") || args[1].equalsIgnoreCase("--disable")) {
					if (!user.hasPermission(PermissionManager.moduleUnloadPermission)) {
						GoldenApple.logPermissionFail(user, commandLabel, args, true);
					} else if (module.getCurrentState() != ModuleState.LOADED) {
						user.sendLocalizedMessage("error.module.unload.notLoaded", module.getModuleName());
					} else if (!module.canPolicyUnload()) {
						user.sendLocalizedMessage("error.module.unload.policy", module.getModuleName());
					} else {
						ArrayDeque<ModuleLoader> depend = new ArrayDeque<ModuleLoader>();
						ArrayList<ModuleLoader> oldDepend = new ArrayList<ModuleLoader>();
						oldDepend.add(module);
						while (!oldDepend.isEmpty()) {
							ArrayList<ModuleLoader> newDepend = new ArrayList<ModuleLoader>();
							for (ModuleLoader m : GoldenApple.getInstance().getModuleManager().getModules()) {
								if (m.getCurrentState() != ModuleState.LOADED) {
									continue;
								}
								for (ModuleLoader dis : oldDepend) {
									for (String d : m.getModuleDependencies()) {
										if (d.equals(dis.getModuleName())) {
											newDepend.add(m);
											depend.addFirst(m);
											if (!m.canPolicyUnload()) {
												user.sendLocalizedMessage("error.module.unload.dependFail", module.getModuleName(), m.getModuleName());
												return true;
											}
										}
									}
								}
							}
							oldDepend.clear();
							oldDepend.addAll(newDepend);
						}
						if (!depend.isEmpty()) {
							if (args.length != 2 && args[2].equalsIgnoreCase("-v")) {
								for (ModuleLoader mUnload = depend.pollLast(); mUnload != null; mUnload = depend.pollLast()) {
									try {
										if (!GoldenApple.getInstance().getModuleManager().disableModule(mUnload.getModuleName(), false, user.getName())) {
											user.sendLocalizedMessage("error.module.unload.dependFail", module.getModuleName(), mUnload.getModuleName());
											return true;
										} else {
											user.sendLocalizedMessage("general.module.unload.success", mUnload.getModuleName());
										}
									} catch (Throwable t) {
										user.sendLocalizedMessage("error.module.unload.dependFail", module.getModuleName(), mUnload.getModuleName());
										return true;
									}
								}
							} else {
								user.sendLocalizedMessage("general.module.unload.warnStart");
								String dependStr = depend.pollLast().getModuleName();
								for (ModuleLoader mUnload = depend.pollLast(); mUnload != null; mUnload = depend.pollLast()) {
									dependStr += ", " + mUnload.getModuleName();
								}
								user.getHandle().sendMessage(dependStr);
								user.sendLocalizedMessage("general.module.unload.warnEnd");
								String cmd = commandLabel;
								for (String arg : args)
									cmd += " " + arg;
								cmd += " -v";
								VerifyCommand.commands.put(user, cmd);
								return true;
							}
						}
						try {
							if (!GoldenApple.getInstance().getModuleManager().disableModule(module.getModuleName(), false, user.getName())) {
								user.sendLocalizedMessage("error.module.unload.unknown", module.getModuleName());
								return true;
							} else {
								user.sendLocalizedMessage("general.module.unload.success", module.getModuleName());
							}
						} catch (Throwable t) {
							user.sendLocalizedMessage("error.module.unload.unknown", module.getModuleName());
							return true;
						}
					}
				} else {
					user.sendLocalizedMessage("shared.unknownOption", args[1]);
				}
			} else {
				user.sendLocalizedMessage("error.module.notFound", args[0]);
			}
		} else {
			GoldenApple.logPermissionFail(user, commandLabel, args, true);
		}
		
		return true;
	}

}
