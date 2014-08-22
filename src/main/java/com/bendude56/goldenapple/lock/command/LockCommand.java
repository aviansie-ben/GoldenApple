package com.bendude56.goldenapple.lock.command;

import java.lang.reflect.InvocationTargetException;
import java.sql.SQLException;
import java.util.Map;
import java.util.logging.Level;

import org.bukkit.Location;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;
import org.bukkit.util.BlockIterator;

import com.bendude56.goldenapple.GoldenApple;
import com.bendude56.goldenapple.User;
import com.bendude56.goldenapple.audit.AuditLog;
import com.bendude56.goldenapple.command.DualSyntaxCommand;
import com.bendude56.goldenapple.lock.LockManager;
import com.bendude56.goldenapple.lock.LockedBlock;
import com.bendude56.goldenapple.lock.LockedBlock.GuestLevel;
import com.bendude56.goldenapple.lock.LockedBlock.LockLevel;
import com.bendude56.goldenapple.lock.audit.LockCreateEntry;
import com.bendude56.goldenapple.lock.audit.LockDeleteEntry;
import com.bendude56.goldenapple.permissions.IPermissionGroup;
import com.bendude56.goldenapple.permissions.IPermissionUser;
import com.bendude56.goldenapple.permissions.PermissionManager;
import com.bendude56.goldenapple.util.ComplexArgumentParser;
import com.bendude56.goldenapple.util.ComplexArgumentParser.ArgumentInfo;

public class LockCommand extends DualSyntaxCommand {
    
    @Override
    public void onExecuteComplex(GoldenApple instance, User user, String commandLabel, String[] args) {
        ComplexArgumentParser arg = new ComplexArgumentParser(getArguments());
        
        if (!arg.parse(user, args)) {
            return;
        }
        
        user.sendLocalizedMessage("module.lock.header");
        
        LockedBlock target = null;
        
        if (arg.isDefined("override")) {
            String overrideMode = arg.getKeyValuePair("override").getKey();
            
            if (overrideMode.equalsIgnoreCase("alwayson")) {
                if (!LockManager.getInstance().canOverride(user)) {
                    GoldenApple.logPermissionFail(user, commandLabel, args, true);
                } else {
                    LockManager.getInstance().setOverrideOn(user, false);
                    user.setVariable("goldenapple.lock.alwaysOverride", true);
                    user.sendLocalizedMessage("module.lock.override.always");
                }
            } else if (overrideMode.equalsIgnoreCase("on")) {
                if (!LockManager.getInstance().canOverride(user)) {
                    GoldenApple.logPermissionFail(user, commandLabel, args, true);
                } else {
                    LockManager.getInstance().setOverrideOn(user, true);
                    user.deleteVariable("goldenapple.lock.alwaysOverride");
                    user.sendLocalizedMessage("module.lock.override.enabled");
                }
            } else if (overrideMode.equalsIgnoreCase("off")) {
                if (!LockManager.getInstance().canOverride(user)) {
                    GoldenApple.logPermissionFail(user, commandLabel, args, true);
                } else {
                    LockManager.getInstance().setOverrideOn(user, false);
                    user.deleteVariable("goldenapple.lock.alwaysOverride");
                    user.sendLocalizedMessage("module.lock.override.disabled");
                }
            } else {
                user.sendLocalizedMessage("module.lock.override.unknownMode", overrideMode);
            }
            
            return;
        }
        
        if (arg.isDefined("create")) {
            Location lockLocation = findLockableBlock(user, 10);
            if (!user.hasPermission(LockManager.addPermission)) {
                GoldenApple.logPermissionFail(user, commandLabel, args, true);
            } else if (lockLocation == null) {
                user.sendLocalizedMessage("module.lock.error.invalidBlock");
                return;
            } else if ((target = LockManager.getInstance().getLock(lockLocation)) != null) {
                user.sendLocalizedMessage("module.lock.error.alreadyLocked");
            } else {
                target = createLock(instance, user, (arg.isDefined("public")) ? LockLevel.PUBLIC : LockLevel.PRIVATE, lockLocation);
            }
        }
        
        if (target == null && arg.isDefined("select")) {
            target = LockManager.getInstance().getLock(arg.getLong("select"));
            
            if (target == null) {
                user.sendLocalizedMessage("module.lock.error.lockNotFound", arg.getLong("select"));
                return;
            }
        } else if (target == null) {
            target = findLock(user, 10);
            
            if (target == null) {
                user.sendLocalizedMessage("module.lock.error.notLocked");
                return;
            }
        }
        
        if (arg.isDefined("delete")) {
            if (!target.canModifyBlock(user)) {
                GoldenApple.logPermissionFail(user, commandLabel, args, true);
                return;
            }
            
            deleteLock(instance, user, target.getLockId());
            return;
        }
        
        if (arg.isDefined("access")) {
            if (!target.canModifyBlock(user)) {
                GoldenApple.logPermissionFail(user, commandLabel, args, true);
                return;
            }
            
            changeAccess(instance, user, target, arg.getString("access"));
        }
        
        if (arg.isDefined("redstone")) {
            String redstoneMode = arg.getKeyValuePair("redstone").getKey();
            
            if (redstoneMode.equalsIgnoreCase("on")) {
                if (!target.canModifyBlock(user)) {
                    GoldenApple.logPermissionFail(user, commandLabel, args, true);
                    return;
                }
                
                setHopperAllow(user, target, true);
            } else if (redstoneMode.equalsIgnoreCase("off")) {
                if (!target.canModifyBlock(user)) {
                    GoldenApple.logPermissionFail(user, commandLabel, args, true);
                    return;
                }
                
                setHopperAllow(user, target, false);
            } else {
                user.sendLocalizedMessage("module.lock.redstone.unknownMode", redstoneMode);
            }
        }
        
        if (arg.isDefined("invite")) {
            for (Map.Entry<String, Object> kvp : arg.getKeyValuePairList("invite")) {
                String accessLevel = kvp.getKey();
                IPermissionUser guest = (IPermissionUser) kvp.getValue();
                GuestLevel l;
                
                if (accessLevel.equalsIgnoreCase("n") || accessLevel.equalsIgnoreCase("none")) {
                    l = GuestLevel.NONE;
                } else if (accessLevel.equalsIgnoreCase("u") || accessLevel.equalsIgnoreCase("use")) {
                    l = GuestLevel.USE;
                } else if (accessLevel.equalsIgnoreCase("i") || accessLevel.equalsIgnoreCase("invite")) {
                    l = GuestLevel.ALLOW_INVITE;
                } else if (accessLevel.equalsIgnoreCase("m") || accessLevel.equalsIgnoreCase("modify")) {
                    l = GuestLevel.ALLOW_BLOCK_MODIFY;
                } else if (accessLevel.equalsIgnoreCase("f") || accessLevel.equalsIgnoreCase("full")) {
                    l = GuestLevel.FULL;
                } else {
                    user.sendLocalizedMessage("module.lock.share.unknownLevel", accessLevel);
                    continue;
                }
                
                if (!target.canInvite(user)) {
                    GoldenApple.logPermissionFail(user, commandLabel, args, true);
                    if (target.getOverrideLevel(user).levelId >= GuestLevel.ALLOW_INVITE.levelId) {
                        user.sendLocalizedMessage("module.lock.override.available.complex");
                    }
                    return;
                } else if (target.getOwner() == guest.getId()) {
                    user.sendLocalizedMessage("module.lock.share.ownerEdit");
                } else if (!target.hasFullControl(user) && ((l == GuestLevel.NONE && target.getActualLevel(guest).levelId >= GuestLevel.ALLOW_INVITE.levelId) || l.levelId >= GuestLevel.ALLOW_INVITE.levelId)) {
                    GoldenApple.logPermissionFail(user, commandLabel, args, true);
                    if (target.getOverrideLevel(user).levelId >= GuestLevel.FULL.levelId) {
                        user.sendLocalizedMessage("module.lock.override.available.complex");
                    }
                    return;
                } else if (l == GuestLevel.NONE) {
                    removeUser(instance, user, target, guest);
                } else {
                    addUser(instance, user, target, guest, l);
                }
            }
        }
        
        if (arg.isDefined("group-invite")) {
            for (Map.Entry<String, Object> kvp : arg.getKeyValuePairList("group-invite")) {
                String accessLevel = kvp.getKey();
                IPermissionGroup guest = (IPermissionGroup) kvp.getValue();
                GuestLevel l;
                
                if (accessLevel.equalsIgnoreCase("n") || accessLevel.equalsIgnoreCase("none")) {
                    l = GuestLevel.NONE;
                } else if (accessLevel.equalsIgnoreCase("u") || accessLevel.equalsIgnoreCase("use")) {
                    l = GuestLevel.USE;
                } else if (accessLevel.equalsIgnoreCase("i") || accessLevel.equalsIgnoreCase("invite")) {
                    l = GuestLevel.ALLOW_INVITE;
                } else if (accessLevel.equalsIgnoreCase("m") || accessLevel.equalsIgnoreCase("modify")) {
                    l = GuestLevel.ALLOW_BLOCK_MODIFY;
                } else if (accessLevel.equalsIgnoreCase("f") || accessLevel.equalsIgnoreCase("full")) {
                    l = GuestLevel.FULL;
                } else {
                    user.sendLocalizedMessage("module.lock.share.unknownLevel", accessLevel);
                    continue;
                }
                
                if (!target.hasFullControl(user)) {
                    GoldenApple.logPermissionFail(user, commandLabel, args, true);
                    if (target.getOverrideLevel(user).levelId >= GuestLevel.FULL.levelId) {
                        user.sendLocalizedMessage("module.lock.override.available.complex");
                    }
                    return;
                } else if (l == GuestLevel.NONE) {
                    removeGroup(instance, user, target, guest);
                } else {
                    addGroup(instance, user, target, guest, l);
                }
            }
        }
        
        if (arg.isDefined("info")) {
            getInfo(instance, user, target);
        }
    }
    
    @Override
    public void onExecuteSimple(GoldenApple instance, User user, String commandLabel, String[] args) {
        if (!(user.getHandle() instanceof Player)) {
            user.sendLocalizedMessage("shared.consoleNotAllowed");
        }
        
        if (args.length == 0 || args[0].equals("-?") || args[0].equals("help")) {
            sendHelp(user, commandLabel, false);
            return;
        }
        
        user.sendLocalizedMessage("module.lock.header");
        
        if (args[0].equalsIgnoreCase("create")) {
            Location lockLocation = findLockableBlock(user, 10);
            if (!user.hasPermission(LockManager.addPermission)) {
                GoldenApple.logPermissionFail(user, commandLabel, args, true);
            } else if (lockLocation == null) {
                user.sendLocalizedMessage("module.lock.error.invalidBlock");
            } else if (LockManager.getInstance().getLock(lockLocation) != null) {
                user.sendLocalizedMessage("module.lock.error.alreadyLocked");
            } else if (args.length > 2) {
                user.sendLocalizedMessage("shared.parser.unknownOption", args[2]);
            } else if (args.length == 1) {
                createLock(instance, user, LockLevel.PRIVATE, lockLocation);
            } else if (args.length == 2 && args[1].equalsIgnoreCase("public")) {
                createLock(instance, user, LockLevel.PUBLIC, lockLocation);
            } else {
                user.sendLocalizedMessage("shared.parser.unknownOption", args[1]);
            }
        } else if (args[0].equalsIgnoreCase("override")) {
            if (args.length == 1) {
                if (!LockManager.getInstance().canOverride(user)) {
                    GoldenApple.logPermissionFail(user, commandLabel, args, true);
                } else {
                    if (LockManager.getInstance().isOverrideOn(user)) {
                        if (!LockManager.getInstance().canOverride(user)) {
                            GoldenApple.logPermissionFail(user, commandLabel, args, true);
                        } else {
                            LockManager.getInstance().setOverrideOn(user, false);
                            user.sendLocalizedMessage("module.lock.override.disabled");
                        }
                    } else {
                        if (!LockManager.getInstance().canOverride(user)) {
                            GoldenApple.logPermissionFail(user, commandLabel, args, true);
                        } else {
                            LockManager.getInstance().setOverrideOn(user, true);
                            user.sendLocalizedMessage("module.lock.override.enabled");
                        }
                    }
                }
            } else if (args[1].equalsIgnoreCase("on")) {
                if (!LockManager.getInstance().canOverride(user)) {
                    GoldenApple.logPermissionFail(user, commandLabel, args, true);
                } else {
                    LockManager.getInstance().setOverrideOn(user, true);
                    user.sendLocalizedMessage("module.lock.override.enabled");
                }
            } else if (args[1].equalsIgnoreCase("off")) {
                if (!LockManager.getInstance().canOverride(user)) {
                    GoldenApple.logPermissionFail(user, commandLabel, args, true);
                } else {
                    LockManager.getInstance().setOverrideOn(user, false);
                    user.sendLocalizedMessage("module.lock.override.disabled");
                }
            } else {
                user.sendLocalizedMessage("module.lock.override.unknownMode", args[1]);
            }
        } else {
            LockedBlock lock = findLock(user, 10);
            
            if (lock == null) {
                user.sendLocalizedMessage("module.lock.error.notLocked");
                return;
            }
            
            if (args[0].equalsIgnoreCase("delete")) {
                if (args.length > 1) {
                    user.sendLocalizedMessage("shared.parser.unknownOption", args[1]);
                } else if (!lock.canModifyBlock(user)) {
                    GoldenApple.logPermissionFail(user, commandLabel, args, true);
                    if (lock.getOverrideLevel(user).levelId >= GuestLevel.ALLOW_BLOCK_MODIFY.levelId) {
                        user.sendLocalizedMessage("module.lock.override.available.simple");
                    }
                } else {
                    deleteLock(instance, user, lock.getLockId());
                }
            } else if (args[0].equalsIgnoreCase("invite")) {
                if (args.length == 1) {
                    user.sendLocalizedMessage("shared.parser.parameterMissing", "invite");
                } else if (args.length > 2) {
                    user.sendLocalizedMessage("shared.parser.unknownOption", args[2]);
                } else if (!lock.canInvite(user)) {
                    GoldenApple.logPermissionFail(user, commandLabel, args, true);
                    if (lock.getOverrideLevel(user).levelId >= GuestLevel.ALLOW_INVITE.levelId) {
                        user.sendLocalizedMessage("module.lock.override.available.simple");
                    }
                    return;
                } else {
                    addUser(instance, user, lock, args[1], GuestLevel.USE);
                }
            } else if (args[0].equalsIgnoreCase("uninvite")) {
                if (args.length == 1) {
                    user.sendLocalizedMessage("shared.parser.parameterMissing", "uninvite");
                } else if (args.length > 2) {
                    user.sendLocalizedMessage("shared.parser.unknownOption", args[2]);
                } else if (!lock.canInvite(user)) {
                    GoldenApple.logPermissionFail(user, commandLabel, args, true);
                    if (lock.getOverrideLevel(user).levelId >= GuestLevel.FULL.levelId) {
                        user.sendLocalizedMessage("module.lock.override.available.simple");
                    }
                    return;
                } else {
                    removeUser(instance, user, lock, args[1], commandLabel, args);
                }
            } else if (args[0].equalsIgnoreCase("access")) {
                if (args.length == 1) {
                    user.sendLocalizedMessage("shared.parser.parameterMissing", "access");
                } else if (args.length > 2) {
                    user.sendLocalizedMessage("shared.parser.unknownOption", args[2]);
                } else if (!lock.canModifyBlock(user)) {
                    GoldenApple.logPermissionFail(user, commandLabel, args, true);
                    if (lock.getOverrideLevel(user).levelId >= GuestLevel.ALLOW_BLOCK_MODIFY.levelId) {
                        user.sendLocalizedMessage("module.lock.override.available.simple");
                    }
                    return;
                } else {
                    changeAccess(instance, user, lock, args[1]);
                }
            } else if (args[0].equalsIgnoreCase("redstone")) {
                if (args.length == 1) {
                    user.sendLocalizedMessage("shared.parser.parameterMissing", "redstone");
                } else if (args.length > 2) {
                    user.sendLocalizedMessage("shared.parser.unknownOption", args[2]);
                } else if (!lock.canModifyBlock(user)) {
                    GoldenApple.logPermissionFail(user, commandLabel, args, true);
                    if (lock.getOverrideLevel(user).levelId >= GuestLevel.ALLOW_BLOCK_MODIFY.levelId) {
                        user.sendLocalizedMessage("module.lock.override.available.simple");
                    }
                    return;
                } else {
                    setHopperAllow(user, lock, args[1].equalsIgnoreCase("on"));
                }
            } else if (args[0].equalsIgnoreCase("info")) {
                getInfo(instance, user, lock);
            } else {
                user.sendLocalizedMessage("shared.parser.unknownOption", args[0]);
            }
        }
    }
    
    private LockedBlock findLock(User user, int range) {
        BlockIterator i = new BlockIterator(user.getPlayerHandle(), range);
        
        while (i.hasNext()) {
            Block b = i.next();
            
            if (LockManager.getInstance().isLockable(b.getType())) {
                return LockManager.getInstance().getLock(b.getLocation());
            }
        }
        
        return null;
    }
    
    private Location findLockableBlock(User user, int range) {
        BlockIterator i = new BlockIterator(user.getPlayerHandle(), range);
        
        while (i.hasNext()) {
            Block b = i.next();
            
            if (LockManager.getInstance().isLockable(b.getType())) {
                return b.getLocation();
            }
        }
        
        return null;
    }
    
    private LockedBlock createLock(GoldenApple instance, User user, LockLevel access, Location loc) {
        LockedBlock lock;
        try {
            lock = LockManager.getInstance().createLock(loc, access, user);
            AuditLog.logEntry(new LockCreateEntry(user, lock.getLockId(), lock.getTypeIdentifier(), lock.getLocation()));
            getInfo(instance, user, lock);
            return lock;
        } catch (InvocationTargetException e) {
            user.sendLocalizedMessage("module.lock.error.invalidRegisteredBlock", LockedBlock.getBlock(loc.getBlock().getType()).plugin.getName());
            GoldenApple.log(Level.SEVERE, "Failed to lock block due to RegisteredBlock failure (Plugin: " + LockedBlock.getBlock(loc.getBlock().getType()).plugin.getName() + ")");
            GoldenApple.log(Level.SEVERE, e);
            return null;
        } catch (SQLException e) {
            user.sendLocalizedMessage("module.lock.error.sqlError");
            GoldenApple.log(Level.SEVERE, "Failed to lock block due to SQL error:");
            GoldenApple.log(Level.SEVERE, e);
            return null;
        }
    }
    
    private void deleteLock(GoldenApple instance, User user, long id) {
        try {
            LockManager.getInstance().deleteLock(id);
            AuditLog.logEntry(new LockDeleteEntry(user, id));
            user.sendLocalizedMessage("module.lock.delete.success");
        } catch (SQLException e) {
            user.sendLocalizedMessage("module.lock.error.sqlError");
            GoldenApple.log(Level.SEVERE, "Failed to unlock block due to SQL error:");
            GoldenApple.log(Level.SEVERE, e);
        }
    }
    
    private void addUser(GoldenApple instance, User user, LockedBlock lock, String guest, GuestLevel level) {
        IPermissionUser gUser = PermissionManager.getInstance().findUser(guest, false);
        
        if (gUser == null) {
            user.sendLocalizedMessage("shared.parser.userNotFound.warning", guest);
        } else {
            addUser(instance, user, lock, gUser, level);
        }
    }
    
    private void addUser(GoldenApple instance, User user, LockedBlock lock, IPermissionUser guest, GuestLevel level) {
        lock.addUser(guest, level);
        user.sendLocalizedMessage("module.lock.share.successAdd", guest.getName());
    }
    
    private void removeUser(GoldenApple instance, User user, LockedBlock lock, String guest, String commandLabel, String[] args) {
        IPermissionUser gUser = PermissionManager.getInstance().findUser(guest, false);
        
        if (gUser == null) {
            user.sendLocalizedMessage("shared.parser.userNotFound.warning", guest);
        } else if (lock.getActualLevel(gUser).levelId > GuestLevel.USE.levelId && !lock.hasFullControl(user)) {
            GoldenApple.logPermissionFail(user, commandLabel, args, true);
        } else {
            removeUser(instance, user, lock, gUser);
        }
    }
    
    private void removeUser(GoldenApple instance, User user, LockedBlock lock, IPermissionUser guest) {
        lock.remUser(guest);
        user.sendLocalizedMessage("module.lock.share.successRemove", guest.getName());
    }
    
    private void addGroup(GoldenApple instance, User user, LockedBlock lock, IPermissionGroup guest, GuestLevel level) {
        lock.addGroup(guest, level);
        user.sendLocalizedMessage("module.lock.share.successAdd", guest.getName());
    }
    
    private void removeGroup(GoldenApple instance, User user, LockedBlock lock, IPermissionGroup guest) {
        lock.remGroup(guest);
        user.sendLocalizedMessage("module.lock.share.successRemove", guest.getName());
    }
    
    private void changeAccess(GoldenApple instance, User user, LockedBlock lock, String access) {
        LockLevel accessLevel;
        if (access.equalsIgnoreCase("private")) {
            accessLevel = LockLevel.PRIVATE;
        } else if (access.equalsIgnoreCase("public")) {
            accessLevel = LockLevel.PUBLIC;
        } else {
            user.sendLocalizedMessage("module.lock.access.unknownLevel", access);
            return;
        }
        
        lock.setLevel(accessLevel);
        
        if (accessLevel == LockLevel.PRIVATE) {
            user.sendLocalizedMessage("module.lock.access.private");
        } else if (accessLevel == LockLevel.PUBLIC) {
            user.sendLocalizedMessage("module.lock.access.public");
        }
    }
    
    private void getInfo(GoldenApple instance, User user, LockedBlock b) {
        user.sendLocalizedMessage("module.lock.info.id", b.getLockId());
        user.sendLocalizedMessage("module.lock.info.owner", PermissionManager.getInstance().getUser(b.getOwner()).getName());
        
        switch (b.getLevel()) {
            case PUBLIC:
                user.sendLocalizedMessage("module.lock.info.access.public");
                break;
            case PRIVATE:
                user.sendLocalizedMessage("module.lock.info.access.private");
                break;
        }
        
        if (b.isRedstoneAccessApplicable() && b.isHopperAccessApplicable()) {
            user.sendLocalizedMessage((b.getAllowExternal()) ? "module.lock.info.external.both.enabled" : "module.lock.info.external.both.disabled");
        } else if (b.isHopperAccessApplicable()) {
            user.sendLocalizedMessage((b.getAllowExternal()) ? "module.lock.info.external.hopper.enabled" : "module.lock.info.external.hopper.disabled");
        } else if (b.isRedstoneAccessApplicable()) {
            user.sendLocalizedMessage((b.getAllowExternal()) ? "module.lock.info.external.redstone.enabled" : "module.lock.info.external.redstone.disabled");
        }
        
        // TODO Only display when enabled in config
        user.sendLocalizedMessage("module.lock.info.type", b.getTypeIdentifier());
    }
    
    private void setHopperAllow(User user, LockedBlock lock, boolean allow) {
        lock.setAllowExternal(allow);
        lock.save();
        user.sendLocalizedMessage((allow) ? "module.lock.redstone.enabled" : "module.lock.redstone.disabled");
    }
    
    private void sendHelp(User user, String commandLabel, boolean complex) {
        user.sendLocalizedMessage("module.lock.header");
        user.sendLocalizedMessage((complex) ? "module.lock.help.complex" : "module.lock.help.simple", commandLabel);
    }
    
    private ArgumentInfo[] getArguments() {
        return new ArgumentInfo[] {
            ArgumentInfo.newInt("select", "s", "select"),
            
            ArgumentInfo.newKeyValuePair(ArgumentInfo.newSwitch("override", "o", "override")),
            
            ArgumentInfo.newSwitch("create", "c", "create"),
            ArgumentInfo.newSwitch("public", "p", "public"),
            ArgumentInfo.newSwitch("delete", "d", "delete"),
            
            ArgumentInfo.newSwitch("info", "i", "info"),
            
            ArgumentInfo.newString("access", "a", "access", false),
            ArgumentInfo.newKeyValuePair(ArgumentInfo.newSwitch("redstone", "r", "redstone")),
            
            ArgumentInfo.newKeyValuePair(ArgumentInfo.newUserList("invite", "in", "invite", false, false)),
            ArgumentInfo.newKeyValuePair(ArgumentInfo.newGroupList("group-invite", "gin", "groupinvite", false)),
        };
    }
}
