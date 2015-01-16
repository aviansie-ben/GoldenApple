package com.bendude56.goldenapple.command;

import java.util.ArrayDeque;
import java.util.ArrayList;

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
                user.sendLocalizedMessage("module.base.module.header");
                user.sendLocalizedMessage("module.base.module.list.header");
                for (ModuleLoader module : GoldenApple.getInstance().getModuleManager().getModules()) {
                    String suffix = "";
                    if (!module.canPolicyLoad() || (module.getCurrentState() == ModuleState.LOADED && !module.canPolicyUnload())) {
                        suffix += user.getLocalizedMessage("module.base.module.list.lockedSuffix");
                    }
                    switch (module.getCurrentState()) {
                        case BUSY:
                            user.sendLocalizedMessage("module.base.module.list.entry.busy", module.getModuleName() + suffix);
                            break;
                        case LOADED:
                            user.sendLocalizedMessage("module.base.module.list.entry.loaded", module.getModuleName() + suffix);
                            break;
                        case LOADING:
                            user.sendLocalizedMessage("module.base.module.list.entry.loading", module.getModuleName() + suffix);
                            break;
                        case UNLOADED_USER:
                            user.sendLocalizedMessage("module.base.module.list.entry.unloadedUser", module.getModuleName() + suffix);
                            break;
                        case UNLOADED_ERROR:
                        case UNLOADED_MISSING_DEPENDENCY:
                            user.sendLocalizedMessage("module.base.module.list.entry.unloadedError", module.getModuleName() + suffix);
                            break;
                        case UNLOADING:
                        	user.sendLocalizedMessage("module.base.module.list.entry.unloading", module.getModuleName() + suffix);
                        	break;
                    }
                }
            } else if (args[0].equalsIgnoreCase("help") || args[0].equalsIgnoreCase("-?")) {
                user.sendLocalizedMessage("module.base.module.header");
                user.sendLocalizedMessage("module.base.module.help");
            } else if (GoldenApple.getInstance().getModuleManager().getModule(args[0]) != null) {
                user.sendLocalizedMessage("module.base.module.header");
                ModuleLoader module = GoldenApple.getInstance().getModuleManager().getModule(args[0]);
                if (args.length == 1 || args[1].equalsIgnoreCase("-q") || args[1].equalsIgnoreCase("--query")) {
                    String status = "???";
                    switch (module.getCurrentState()) {
                        case BUSY:
                            status = user.getLocalizedMessage("module.base.module.query.state.busy");
                            break;
                        case LOADED:
                            if (module.canPolicyUnload()) {
                                status = user.getLocalizedMessage("module.base.module.query.state.loaded");
                            } else {
                                status = user.getLocalizedMessage("module.base.module.query.state.loadedLocked");
                            }
                            break;
                        case LOADING:
                            status = user.getLocalizedMessage("module.base.module.query.state.loading");
                            break;
                        case UNLOADED_USER:
                            if (module.canPolicyLoad()) {
                                status = user.getLocalizedMessage("module.base.module.query.state.unloadedUser");
                            } else {
                                status = user.getLocalizedMessage("module.base.module.query.state.unloadedUserLocked");
                            }
                            break;
                        case UNLOADED_ERROR:
                            status = user.getLocalizedMessage("module.base.module.query.state.unloadedError");
                            break;
                        case UNLOADED_MISSING_DEPENDENCY:
                            status = user.getLocalizedMessage("module.base.module.query.state.unloadedDepend");
                            break;
                        case UNLOADING:
                        	status = user.getLocalizedMessage("module.base.module.query.state.unloading");
                        	break;
                    }
                    user.sendLocalizedMessage("module.base.module.query.message", module.getModuleName(), status);
                } else if (args[1].equalsIgnoreCase("-e") || args[1].equalsIgnoreCase("--enable")) {
                    if (!user.hasPermission(PermissionManager.moduleLoadPermission)) {
                        GoldenApple.logPermissionFail(user, commandLabel, args, true);
                    } else if (module.getCurrentState() == ModuleState.LOADED || module.getCurrentState() == ModuleState.LOADING) {
                        user.sendLocalizedMessage("module.base.module.enable.alreadyLoaded", module.getModuleName());
                    } else if (!module.canPolicyLoad()) {
                        user.sendLocalizedMessage("module.base.module.enable.policyBlock", module.getModuleName());
                    } else {
                        ArrayDeque<ModuleLoader> depend = new ArrayDeque<ModuleLoader>();
                        ArrayList<ModuleLoader> oldDepend = new ArrayList<ModuleLoader>();
                        oldDepend.add(module);
                        while (!oldDepend.isEmpty()) {
                            ArrayList<ModuleLoader> newDepend = new ArrayList<ModuleLoader>();
                            for (ModuleLoader m : oldDepend) {
                                for (String d : m.getModuleDependencies()) {
                                    if (GoldenApple.getInstance().getModuleManager().getModule(d) == null) {
                                        user.sendLocalizedMessage("module.base.module.enable.loadError.dependency", module.getModuleName(), d);
                                        return true;
                                    } else if (!GoldenApple.getInstance().getModuleManager().getModule(d).canPolicyLoad()) {
                                        user.sendLocalizedMessage("module.base.module.enable.loadError.dependency", module.getModuleName(), d);
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
                                            user.sendLocalizedMessage("module.base.module.enable.loadError.dependency", module.getModuleName(), mLoad.getModuleName());
                                            return true;
                                        } else {
                                            user.sendLocalizedMessage("module.base.module.enable.success", mLoad.getModuleName());
                                        }
                                    } catch (Throwable t) {
                                        user.sendLocalizedMessage("module.base.module.enable.loadError.dependency", module.getModuleName(), mLoad.getModuleName());
                                        return true;
                                    }
                                }
                            } else {
                                user.sendLocalizedMessage("module.base.module.enable.warning.header");
                                for (ModuleLoader mLoad = depend.pollLast(); mLoad != null; mLoad = depend.pollLast()) {
                                    user.sendLocalizedMessage("module.base.module.enable.warning.entry", mLoad.getModuleName());
                                }
                                user.sendLocalizedMessage("module.base.module.enable.warning.footer");
                                String cmd = commandLabel;
                                for (String arg : args) {
                                    cmd += " " + arg;
                                }
                                cmd += " -v";
                                VerifyCommand.commands.put(user, cmd);
                                return true;
                            }
                        }
                        try {
                            if (!GoldenApple.getInstance().getModuleManager().enableModule(module.getModuleName(), false, user.getName())) {
                                user.sendLocalizedMessage("module.base.module.enable.loadError.self", module.getModuleName());
                                return true;
                            } else {
                                user.sendLocalizedMessage("module.base.module.enable.success", module.getModuleName());
                            }
                        } catch (Throwable t) {
                            user.sendLocalizedMessage("module.base.module.enable.loadError.self", module.getModuleName());
                            return true;
                        }
                    }
                } else if (args[1].equalsIgnoreCase("-d") || args[1].equalsIgnoreCase("--disable")) {
                    if (!user.hasPermission(PermissionManager.moduleUnloadPermission)) {
                        GoldenApple.logPermissionFail(user, commandLabel, args, true);
                    } else if (module.getCurrentState() == ModuleState.BUSY) {
                        user.sendLocalizedMessage("module.base.module.disable.busy", module.getModuleName());
                    } else if (module.getCurrentState() != ModuleState.LOADED) {
                        user.sendLocalizedMessage("module.base.module.disable.alreadyUnloaded", module.getModuleName());
                    } else if (!module.canPolicyUnload()) {
                        user.sendLocalizedMessage("module.base.module.disable.policyBlock", module.getModuleName());
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
                                                user.sendLocalizedMessage("module.base.module.disable.unloadError.dependency", module.getModuleName(), m.getModuleName());
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
                                            user.sendLocalizedMessage("module.base.module.disable.unloadError.dependency", module.getModuleName(), mUnload.getModuleName());
                                            return true;
                                        } else {
                                            user.sendLocalizedMessage("module.base.module.disable.success", mUnload.getModuleName());
                                        }
                                    } catch (Throwable t) {
                                        user.sendLocalizedMessage("module.base.module.disable.unloadError.dependency", module.getModuleName(), mUnload.getModuleName());
                                        return true;
                                    }
                                }
                            } else {
                                user.sendLocalizedMessage("module.base.module.disable.warning.header");
                                for (ModuleLoader mUnload = depend.pollLast(); mUnload != null; mUnload = depend.pollLast()) {
                                    user.sendLocalizedMessage("module.base.module.disable.warning.entry", mUnload.getModuleName());
                                }
                                user.sendLocalizedMessage("module.base.module.disable.warning.footer");
                                String cmd = commandLabel;
                                for (String arg : args) {
                                    cmd += " " + arg;
                                }
                                cmd += " -v";
                                VerifyCommand.commands.put(user, cmd);
                                return true;
                            }
                        }
                        try {
                            if (!GoldenApple.getInstance().getModuleManager().disableModule(module.getModuleName(), false, user.getName())) {
                                user.sendLocalizedMessage("module.base.module.disable.unloadError.self", module.getModuleName());
                                return true;
                            } else {
                                user.sendLocalizedMessage("module.base.module.disable.success", module.getModuleName());
                            }
                        } catch (Throwable t) {
                            user.sendLocalizedMessage("module.base.module.disable.unloadError.self", module.getModuleName());
                            return true;
                        }
                    }
                } else if (args[1].equalsIgnoreCase("-cc") || args[1].equalsIgnoreCase("--clear-cache")) {
                    if (!user.hasPermission(PermissionManager.moduleClearCachePermission)) {
                        GoldenApple.logPermissionFail(user, commandLabel, args, true);
                    } else if (module.getCurrentState() != ModuleState.LOADED) {
                        user.sendLocalizedMessage("module.base.module.clearCache.notLoaded", module.getModuleName());
                    } else {
                        module.clearCache();
                        user.sendLocalizedMessage("module.base.module.clearCache.success", module.getModuleName());
                    }
                } else {
                    user.sendLocalizedMessage("shared.parser.unknownOption", args[1]);
                }
            } else {
                user.sendLocalizedMessage("module.base.module.notFound", args[0]);
            }
        } else {
            GoldenApple.logPermissionFail(user, commandLabel, args, true);
        }
        
        return true;
    }
    
}
