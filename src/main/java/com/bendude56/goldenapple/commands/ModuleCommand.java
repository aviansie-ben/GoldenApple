package com.bendude56.goldenapple.commands;

import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Map.Entry;

import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;

import com.bendude56.goldenapple.GoldenApple;
import com.bendude56.goldenapple.IModuleLoader;
import com.bendude56.goldenapple.IModuleLoader.ModuleState;
import com.bendude56.goldenapple.User;
import com.bendude56.goldenapple.permissions.PermissionManager;

public class ModuleCommand implements CommandExecutor {
	@Override
	public boolean onCommand(CommandSender sender, Command command, String commandLabel, String[] args) {
		GoldenApple instance = GoldenApple.getInstance();
		User user = User.getUser(sender);
		
		if (user.getHandle().isOp() || user.hasPermission(PermissionManager.moduleQueryPermission)) {
			if (args.length == 0 || args[0].equalsIgnoreCase("-ls") || args[0].equalsIgnoreCase("--list")) {
				instance.locale.sendMessage(user, "header.module", false);
				instance.locale.sendMessage(user, "general.module.list", false);
				for (Entry<String, IModuleLoader> module : GoldenApple.modules.entrySet()) {
					String suffix = "";
					if (!module.getValue().canPolicyLoad() || (module.getValue().getCurrentState() == ModuleState.LOADED && !module.getValue().canPolicyUnload())) {
						suffix += ChatColor.DARK_GRAY + " [!]";
					}
					switch (module.getValue().getCurrentState()) {
						case BUSY:
							user.getHandle().sendMessage(ChatColor.YELLOW + module.getValue().getModuleName() + suffix);
							break;
						case LOADED:
							user.getHandle().sendMessage(ChatColor.GREEN + module.getValue().getModuleName() + suffix);
							break;
						case LOADING:
							user.getHandle().sendMessage(ChatColor.YELLOW + module.getValue().getModuleName() + suffix);
							break;
						case UNLOADED_USER:
							user.getHandle().sendMessage(ChatColor.GRAY + module.getValue().getModuleName() + suffix);
							break;
						case UNLOADED_ERROR:
						case UNLOADED_MISSING_DEPENDENCY:
							user.getHandle().sendMessage(ChatColor.RED + module.getValue().getModuleName() + suffix);
							break;
					}
				}
			} else if (args[0].equalsIgnoreCase("help") || args[0].equalsIgnoreCase("-?")) {
				// TODO Implement help
			} else if (GoldenApple.modules.containsKey(args[0])) {
				instance.locale.sendMessage(user, "header.module", false);
				IModuleLoader module = GoldenApple.modules.get(args[0]);
				if (args.length == 1 || args[1].equalsIgnoreCase("-q") || args[1].equalsIgnoreCase("--query")) {
					String status = "???";
					if (module.canPolicyLoad()) {
						switch (module.getCurrentState()) {
							case BUSY:
								status = instance.locale.getMessage(user, "general.module.query.busy");
								break;
							case LOADED:
								if (module.canPolicyUnload())
									status = instance.locale.getMessage(user, "general.module.query.loaded");
								else
									status = instance.locale.getMessage(user, "general.module.query.loadedLocked");
								break;
							case LOADING:
								status = instance.locale.getMessage(user, "general.module.query.loading");
								break;
							case UNLOADED_USER:
								status = instance.locale.getMessage(user, "general.module.query.unloadedUser");
								break;
							case UNLOADED_ERROR:
								status = instance.locale.getMessage(user, "general.module.query.unloadedError");
								break;
							case UNLOADED_MISSING_DEPENDENCY:
								status = instance.locale.getMessage(user, "general.module.query.unloadedDepend");
								break;
						}
					} else {
						status = instance.locale.getMessage(user, "general.module.query.unloadedLocked");
					}
					instance.locale.sendMessage(user, "general.module.query", true, module.getModuleName(), status);
				} else if (args[1].equalsIgnoreCase("-e") || args[1].equalsIgnoreCase("--enable")) {
					if (!user.hasPermission(PermissionManager.moduleLoadPermission)) {
						GoldenApple.logPermissionFail(user, commandLabel, args, true);
					} else if (module.getCurrentState() == ModuleState.LOADED || module.getCurrentState() == ModuleState.LOADING) {
						instance.locale.sendMessage(user, "error.module.load.alreadyLoaded", false, module.getModuleName());
					} else if (!module.canPolicyLoad()) {
						instance.locale.sendMessage(user, "error.module.load.policy", false, module.getModuleName());
					} else {
						ArrayDeque<IModuleLoader> depend = new ArrayDeque<IModuleLoader>();
						ArrayList<IModuleLoader> oldDepend = new ArrayList<IModuleLoader>();
						oldDepend.add(module);
						while (!oldDepend.isEmpty()) {
							ArrayList<IModuleLoader> newDepend = new ArrayList<IModuleLoader>();
							for (IModuleLoader m : oldDepend) {
								for (String d : m.getModuleDependencies()) {
									if (!GoldenApple.modules.containsKey(d)) {
										instance.locale.sendMessage(user, "error.module.load.dependFail", false, module.getModuleName(), d);
										return true;
									} else if (!GoldenApple.modules.get(d).canPolicyLoad()) {
										instance.locale.sendMessage(user, "error.module.load.dependFail", false, module.getModuleName(), d);
										return true;
									} else if (GoldenApple.modules.get(d).getCurrentState() != ModuleState.LOADED) {
										newDepend.add(GoldenApple.modules.get(d));
									}
								}
							}
							oldDepend.clear();
							oldDepend.addAll(newDepend);
							depend.addAll(newDepend);
						}
						if (!depend.isEmpty()) {
							if (args.length != 2 && args[2].equalsIgnoreCase("-v")) {
								for (IModuleLoader mLoad = depend.pollLast(); mLoad != null; mLoad = depend.pollLast()) {
									try {
										if (!instance.enableModule(mLoad, false)) {
											instance.locale.sendMessage(user, "error.module.load.dependFail", false, module.getModuleName(), mLoad.getModuleName());
											return true;
										} else {
											instance.locale.sendMessage(user, "general.module.load.success", false, mLoad.getModuleName());
										}
									} catch (Throwable t) {
										instance.locale.sendMessage(user, "error.module.load.dependFail", false, module.getModuleName(), mLoad.getModuleName());
										return true;
									}
								}
							} else {
								instance.locale.sendMessage(user, "general.module.load.warnStart", false);
								String dependStr = depend.pollLast().getModuleName();
								for (IModuleLoader mLoad = depend.pollLast(); mLoad != null; mLoad = depend.pollLast()) {
									dependStr += ", " + mLoad.getModuleName();
								}
								user.getHandle().sendMessage(dependStr);
								instance.locale.sendMessage(user, "general.module.load.warnEnd", false);
								String cmd = commandLabel;
								for (String arg : args)
									cmd += " " + arg;
								cmd += " -v";
								VerifyCommand.commands.put(user, cmd);
								return true;
							}
						}
						try {
							if (!instance.enableModule(module, false)) {
								instance.locale.sendMessage(user, "error.module.load.unknown", false, module.getModuleName());
								return true;
							} else {
								instance.locale.sendMessage(user, "general.module.load.success", false, module.getModuleName());
							}
						} catch (Throwable t) {
							instance.locale.sendMessage(user, "error.module.load.unknown", false, module.getModuleName());
							return true;
						}
					}
				} else if (args[1].equalsIgnoreCase("-d") || args[1].equalsIgnoreCase("--disable")) {
					if (!user.hasPermission(PermissionManager.moduleUnloadPermission)) {
						GoldenApple.logPermissionFail(user, commandLabel, args, true);
					} else if (module.getCurrentState() != ModuleState.LOADED) {
						instance.locale.sendMessage(user, "error.module.unload.notLoaded", false, module.getModuleName());
					} else if (!module.canPolicyUnload()) {
						instance.locale.sendMessage(user, "error.module.unload.policy", false, module.getModuleName());
					} else {
						ArrayDeque<IModuleLoader> depend = new ArrayDeque<IModuleLoader>();
						ArrayList<IModuleLoader> oldDepend = new ArrayList<IModuleLoader>();
						oldDepend.add(module);
						while (!oldDepend.isEmpty()) {
							ArrayList<IModuleLoader> newDepend = new ArrayList<IModuleLoader>();
							for (Entry<String, IModuleLoader> m : GoldenApple.modules.entrySet()) {
								if (m.getValue().getCurrentState() != ModuleState.LOADED) {
									continue;
								}
								for (IModuleLoader dis : oldDepend) {
									for (String d : m.getValue().getModuleDependencies()) {
										if (d.equals(dis.getModuleName())) {
											newDepend.add(m.getValue());
											depend.addFirst(m.getValue());
											if (!m.getValue().canPolicyUnload()) {
												instance.locale.sendMessage(user, "error.module.unload.dependFail", false, module.getModuleName(), m.getValue().getModuleName());
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
								for (IModuleLoader mUnload = depend.pollLast(); mUnload != null; mUnload = depend.pollLast()) {
									try {
										if (!instance.disableModule(mUnload, false)) {
											instance.locale.sendMessage(user, "error.module.unload.dependFail", false, module.getModuleName(), mUnload.getModuleName());
											return true;
										} else {
											instance.locale.sendMessage(user, "general.module.unload.success", false, mUnload.getModuleName());
										}
									} catch (Throwable t) {
										instance.locale.sendMessage(user, "error.module.unload.dependFail", false, module.getModuleName(), mUnload.getModuleName());
										return true;
									}
								}
							} else {
								instance.locale.sendMessage(user, "general.module.unload.warnStart", false);
								String dependStr = depend.pollLast().getModuleName();
								for (IModuleLoader mUnload = depend.pollLast(); mUnload != null; mUnload = depend.pollLast()) {
									dependStr += ", " + mUnload.getModuleName();
								}
								user.getHandle().sendMessage(dependStr);
								instance.locale.sendMessage(user, "general.module.unload.warnEnd", false);
								String cmd = commandLabel;
								for (String arg : args)
									cmd += " " + arg;
								cmd += " -v";
								VerifyCommand.commands.put(user, cmd);
								return true;
							}
						}
						try {
							if (!instance.disableModule(module, false)) {
								instance.locale.sendMessage(user, "error.module.unload.unknown", false, module.getModuleName());
								return true;
							} else {
								instance.locale.sendMessage(user, "general.module.unload.success", false, module.getModuleName());
							}
						} catch (Throwable t) {
							instance.locale.sendMessage(user, "error.module.unload.unknown", false, module.getModuleName());
							return true;
						}
					}
				} else {
					instance.locale.sendMessage(user, "shared.unknownOption", false, args[1]);
				}
			} else {
				instance.locale.sendMessage(user, "error.module.notFound", false, args[0]);
			}
		} else {
			GoldenApple.logPermissionFail(user, commandLabel, args, true);
		}
		
		return true;
	}

}
