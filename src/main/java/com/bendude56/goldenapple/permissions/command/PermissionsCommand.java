package com.bendude56.goldenapple.permissions.command;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map.Entry;
import java.util.logging.Level;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;

import com.bendude56.goldenapple.GoldenApple;
import com.bendude56.goldenapple.User;
import com.bendude56.goldenapple.audit.AuditLog;
import com.bendude56.goldenapple.command.GoldenAppleCommand;
import com.bendude56.goldenapple.command.VerifyCommand;
import com.bendude56.goldenapple.permissions.DuplicateNameException;
import com.bendude56.goldenapple.permissions.IPermissionGroup;
import com.bendude56.goldenapple.permissions.IPermissionUser;
import com.bendude56.goldenapple.permissions.PermissionGroup;
import com.bendude56.goldenapple.permissions.PermissionManager;
import com.bendude56.goldenapple.permissions.PermissionManager.Permission;
import com.bendude56.goldenapple.permissions.UuidLookupException;
import com.bendude56.goldenapple.permissions.audit.GroupAddMemberEvent;
import com.bendude56.goldenapple.permissions.audit.GroupRemoveMemberEvent;
import com.bendude56.goldenapple.permissions.audit.ObjectCreateEvent;
import com.bendude56.goldenapple.permissions.audit.ObjectDeleteEvent;
import com.bendude56.goldenapple.permissions.audit.PermissionGrantEvent;
import com.bendude56.goldenapple.permissions.audit.PermissionRevokeEvent;

public class PermissionsCommand extends GoldenAppleCommand {
    @Override
    public boolean onExecute(GoldenApple instance, User user, String commandLabel, String[] args) {
        if (args.length == 0 || args[0].equalsIgnoreCase("-?")) {
            sendHelp(user, commandLabel);
            return true;
        }
        
        user.sendLocalizedMessage("header.permissions");
        
        ArrayList<String> changeUsers = new ArrayList<String>();
        ArrayList<String> changeGroups = new ArrayList<String>();
        
        ArrayList<String> addPermissions = new ArrayList<String>();
        ArrayList<String> remPermissions = new ArrayList<String>();
        ArrayList<String> addUsers = new ArrayList<String>();
        ArrayList<String> remUsers = new ArrayList<String>();
        ArrayList<String> addGroups = new ArrayList<String>();
        ArrayList<String> remGroups = new ArrayList<String>();
        HashMap<String, String> setVars = new HashMap<String, String>();
        
        String chatPrefix = null;
        Character chatColor = null;
        
        boolean add = false;
        boolean remove = false;
        boolean verified = false;
        
        for (int i = 0; i < args.length; i++) {
            if (args[i].equalsIgnoreCase("-u")) {
                if (i == args.length - 1 || args[i + 1].startsWith("-")) {
                    user.sendLocalizedMessage("shared.parameterMissing", args[i]);
                } else {
                    changeUsers.add(args[i + 1]);
                    i++;
                }
            } else if (args[i].equalsIgnoreCase("-g")) {
                if (i == args.length - 1 || args[i + 1].startsWith("-")) {
                    user.sendLocalizedMessage("shared.parameterMissing", args[i]);
                } else {
                    changeGroups.add(args[i + 1]);
                    i++;
                }
            } else if (args[i].equalsIgnoreCase("-r")) {
                remove = true;
            } else if (args[i].equalsIgnoreCase("-a")) {
                add = true;
            } else if (args[i].equalsIgnoreCase("-pa")) {
                if (i == args.length - 1 || args[i + 1].startsWith("-")) {
                    user.sendLocalizedMessage("shared.parameterMissing", args[i]);
                } else {
                    addPermissions.add(args[i + 1]);
                    i++;
                }
            } else if (args[i].equalsIgnoreCase("-pr")) {
                if (i == args.length - 1 || args[i + 1].startsWith("-")) {
                    user.sendLocalizedMessage("shared.parameterMissing", args[i]);
                } else {
                    remPermissions.add(args[i + 1]);
                    i++;
                }
            } else if (args[i].equalsIgnoreCase("-ua")) {
                if (i == args.length - 1 || args[i + 1].startsWith("-")) {
                    user.sendLocalizedMessage("shared.parameterMissing", args[i]);
                } else {
                    addUsers.add(args[i + 1]);
                    i++;
                }
            } else if (args[i].equalsIgnoreCase("-ur")) {
                if (i == args.length - 1 || args[i + 1].startsWith("-")) {
                    user.sendLocalizedMessage("shared.parameterMissing", args[i]);
                } else {
                    remUsers.add(args[i + 1]);
                    i++;
                }
            } else if (args[i].equalsIgnoreCase("-ga")) {
                if (i == args.length - 1 || args[i + 1].startsWith("-")) {
                    user.sendLocalizedMessage("shared.parameterMissing", args[i]);
                } else {
                    addGroups.add(args[i + 1]);
                    i++;
                }
            } else if (args[i].equalsIgnoreCase("-gr")) {
                if (i == args.length - 1 || args[i + 1].startsWith("-")) {
                    user.sendLocalizedMessage("shared.parameterMissing", args[i]);
                } else {
                    remGroups.add(args[i + 1]);
                    i++;
                }
            } else if (args[i].equalsIgnoreCase("-cp")) {
                if (i == args.length - 1 || args[i + 1].startsWith("-")) {
                    user.sendLocalizedMessage("shared.parameterMissing", args[i]);
                } else {
                    chatPrefix = args[i + 1];
                    i++;
                }
            } else if (args[i].equalsIgnoreCase("-cc")) {
                if (i == args.length - 1 || args[i + 1].startsWith("-")) {
                    user.sendLocalizedMessage("shared.parameterMissing", args[i]);
                } else {
                    chatColor = args[i + 1].charAt(0);
                    i++;
                }
            } else if (args[i].toLowerCase().startsWith("-var:") && args[i].length() > 5) {
                if (i == args.length - 1 || args[i + 1].startsWith("-")) {
                    user.sendLocalizedMessage("shared.parameterMissing", args[i]);
                } else {
                    setVars.put(args[i].substring(5), args[i + 1]);
                    i++;
                }
            } else if (args[i].equalsIgnoreCase("-v")) {
                verified = true;
            } else {
                user.sendLocalizedMessage("shared.unknownOption", args[i]);
            }
        }
        if (chatPrefix == null && chatColor == null && !remove && !add && addPermissions.isEmpty() && remPermissions.isEmpty() && addUsers.isEmpty() && remUsers.isEmpty() && addGroups.isEmpty() && remGroups.isEmpty() && setVars.isEmpty()) {
            user.sendLocalizedMessage("error.permissions.noAction");
            return true;
        }
        if (remove) {
            if (add || !remPermissions.isEmpty() || !addPermissions.isEmpty() || !addUsers.isEmpty() || !remUsers.isEmpty() || !addGroups.isEmpty() || !remGroups.isEmpty() || !setVars.isEmpty()) {
                user.sendLocalizedMessage("error.permissions.conflict");
                return true;
            }
            if (!changeUsers.isEmpty() && !user.hasPermission(PermissionManager.userRemovePermission)) {
                GoldenApple.logPermissionFail(user, commandLabel, args, true);
                return true;
            } else if (!changeGroups.isEmpty() && !user.hasPermission(PermissionManager.groupRemovePermission)) {
                GoldenApple.logPermissionFail(user, commandLabel, args, true);
                return true;
            } else if (changeUsers.isEmpty() && changeGroups.isEmpty()) {
                user.sendLocalizedMessage("error.permissions.noTarget", "-r");
                return true;
            }
            if (verified) {
                ArrayList<Long> users = new ArrayList<Long>();
                ArrayList<Long> groups = new ArrayList<Long>();
                for (String u : changeUsers) {
                    long id = PermissionManager.getInstance().findUser(u, false).getId();
                    if (id != -1) {
                        users.add(id);
                    } else if (verified) {
                        user.sendLocalizedMessage("shared.userNotFoundWarn", u);
                    }
                }
                for (String g : changeGroups) {
                    IPermissionGroup group = PermissionManager.getInstance().getGroup(g);
                    if (group != null) {
                        groups.add(group.getId());
                    } else if (verified) {
                        user.sendLocalizedMessage("shared.groupNotFoundWarn", g);
                    }
                }
                for (long id : users) {
                    if (PermissionManager.getInstance().isUserSticky(id)) {
                        user.sendLocalizedMessage("error.permissions.remove.userOnline", PermissionManager.getInstance().getUser(id).getName());
                    } else {
                        IPermissionUser u = PermissionManager.getInstance().getUser(id);
                        AuditLog.logEvent(new ObjectDeleteEvent(user.getName(), u));
                        PermissionManager.getInstance().deleteUser(id);
                        GoldenApple.log(Level.INFO, "User " + u.getName() + " (PU" + id + ") has been deleted by " + user.getName());
                        user.sendLocalizedMessage("general.permissions.remove.user", u.getName());
                    }
                }
                for (long id : groups) {
                    IPermissionGroup g = PermissionManager.getInstance().getGroup(id);
                    AuditLog.logEvent(new ObjectDeleteEvent(user.getName(), g));
                    PermissionManager.getInstance().deleteGroup(id);
                    GoldenApple.log(Level.INFO, "Group " + g.getName() + " (PG" + id + ") has been deleted by " + user.getName());
                    user.sendLocalizedMessage("general.permissions.remove.group", g.getName());
                }
            } else {
                user.sendLocalizedMessage("general.permissions.remove.warnStart");
                for (String u : changeUsers) {
                    user.sendLocalizedMessage("general.permissions.remove.warnUser", u);
                }
                for (String g : changeGroups) {
                    user.sendLocalizedMessage("general.permissions.remove.warnGroup", g);
                }
                user.sendLocalizedMessage("general.permissions.remove.warnEnd");
                String cmd = commandLabel;
                for (String a : args) {
                    cmd += " " + a;
                }
                cmd += " -v";
                VerifyCommand.commands.put(user, cmd);
            }
            return true;
        } else if (add) {
            if (!remPermissions.isEmpty() || !addUsers.isEmpty() || !remUsers.isEmpty() || !addGroups.isEmpty() || !remGroups.isEmpty() || !setVars.isEmpty()) {
                user.sendLocalizedMessage("error.permissions.conflict");
                return true;
            } else if (!changeUsers.isEmpty() && !user.hasPermission(PermissionManager.userAddPermission)) {
                GoldenApple.logPermissionFail(user, commandLabel, args, true);
                return true;
            } else if (!changeGroups.isEmpty() && !user.hasPermission(PermissionManager.groupAddPermission)) {
                GoldenApple.logPermissionFail(user, commandLabel, args, true);
                return true;
            } else if (changeUsers.isEmpty() && changeGroups.isEmpty()) {
                user.sendLocalizedMessage("error.permissions.noTarget", "-a");
                return true;
            }
            for (String u : changeUsers) {
                try {
                    IPermissionUser newUser = PermissionManager.getInstance().createUser(u);
                    AuditLog.logEvent(new ObjectCreateEvent(user.getName(), newUser));
                    GoldenApple.log(Level.INFO, "User " + newUser.getName() + " (PU" + newUser.getId() + ") has been created by " + user.getName());
                    user.sendLocalizedMessage("general.permissions.add.user", u);
                } catch (UuidLookupException e) {
                    user.sendLocalizedMessage("error.permissions.add.userNoUuid", u);
                } catch (DuplicateNameException e) {
                    user.sendLocalizedMessage("error.permissions.add.userExists", u);
                }
            }
            for (String g : changeGroups) {
                if (PermissionManager.getInstance().getGroup(g) != null) {
                    user.sendLocalizedMessage("error.permissions.add.groupExists", g);
                } else {
                    IPermissionGroup newGroup = PermissionManager.getInstance().createGroup(g);
                    AuditLog.logEvent(new ObjectCreateEvent(user.getName(), newGroup));
                    GoldenApple.log(Level.INFO, "Group " + newGroup.getName() + " (PG" + newGroup.getId() + ") has been created by " + user.getName());
                    user.sendLocalizedMessage("general.permissions.add.group", g);
                }
            }
        } else {
            ArrayList<IPermissionUser> ul = new ArrayList<IPermissionUser>();
            ArrayList<IPermissionGroup> gl = new ArrayList<IPermissionGroup>();
            
            resolveUsers(instance, user, changeUsers, ul);
            resolveGroups(instance, user, changeGroups, gl);
            
            if (!addUsers.isEmpty() || !remUsers.isEmpty()) {
                if (!user.hasPermission(PermissionManager.groupEditPermission)) {
                    GoldenApple.logPermissionFail(user, commandLabel, args, true);
                    return true;
                } else if (gl.isEmpty()) {
                    user.sendLocalizedMessage("error.permissions.noTarget", "-ua/-ur");
                    return true;
                }
                
                ArrayList<IPermissionUser> ua = new ArrayList<IPermissionUser>();
                ArrayList<IPermissionUser> ur = new ArrayList<IPermissionUser>();
                
                resolveUsers(instance, user, addUsers, ua);
                resolveUsers(instance, user, remUsers, ur);
                
                modifyGroupMembershipUsers(instance, user, gl, ua, ur);
            }
            
            if (!addGroups.isEmpty() || !remGroups.isEmpty()) {
                if (!user.hasPermission(PermissionManager.groupEditPermission)) {
                    GoldenApple.logPermissionFail(user, commandLabel, args, true);
                    return true;
                } else if (gl.isEmpty()) {
                    user.sendLocalizedMessage("error.permissions.noTarget", "-ua/-ur");
                    return true;
                }
                
                ArrayList<IPermissionGroup> ga = new ArrayList<IPermissionGroup>();
                ArrayList<IPermissionGroup> gr = new ArrayList<IPermissionGroup>();
                
                resolveGroups(instance, user, addGroups, ga);
                resolveGroups(instance, user, remGroups, gr);
                
                modifyGroupMembershipGroups(instance, user, gl, ga, gr);
            }
            
            if (!addPermissions.isEmpty() || !remPermissions.isEmpty()) {
                if (!ul.isEmpty() && !user.hasPermission(PermissionManager.userEditPermission)) {
                    GoldenApple.logPermissionFail(user, commandLabel, args, true);
                    return true;
                } else if (!gl.isEmpty() && !user.hasPermission(PermissionManager.groupEditPermission)) {
                    GoldenApple.logPermissionFail(user, commandLabel, args, true);
                    return true;
                } else if (gl.isEmpty() && ul.isEmpty()) {
                    user.sendLocalizedMessage("error.permissions.noTarget", "-pa");
                    return true;
                }
                ArrayList<Permission> addPerm = new ArrayList<Permission>();
                ArrayList<Permission> remPerm = new ArrayList<Permission>();
                
                resolvePermissions(instance, user, addPermissions, addPerm);
                resolvePermissions(instance, user, remPermissions, remPerm);
                
                for (IPermissionUser u : ul) {
                    updatePermissions(instance, user, u, addPerm, remPerm);
                }
                for (IPermissionGroup g : gl) {
                    updatePermissions(instance, user, g, addPerm, remPerm);
                }
            }
            
            if (chatColor != null) {
                if (gl.isEmpty()) {
                    user.sendLocalizedMessage("error.permissions.noTarget", "-cc");
                    return true;
                }
                
                for (IPermissionGroup g : gl) {
                    g.setChatColor(chatColor != null, (chatColor == null) ? ChatColor.WHITE : ChatColor.getByChar(chatColor));
                    ((PermissionGroup)g).save();
                }
            }
            
            if (chatPrefix != null) {
                if (gl.isEmpty()) {
                    user.sendLocalizedMessage("error.permissions.noTarget", "-cp");
                    return true;
                }
                
                for (IPermissionGroup g : gl) {
                    g.setPrefix(chatPrefix);
                    ((PermissionGroup)g).save();
                }
            }
            
            if (!setVars.isEmpty()) {
                if (gl.isEmpty() && ul.isEmpty()) {
                    user.sendLocalizedMessage("error.permissions.noTarget", "-var");
                    return true;
                }
                
                for (Entry<String, String> var : setVars.entrySet()) {
                    for (IPermissionUser u : ul) {
                        if (var.getValue().equals("<null>")) {
                            u.deleteVariable(var.getKey());
                        } else {
                            u.setVariable(var.getKey(), var.getValue());
                        }
                        
                        user.sendLocalizedMessage("general.permissions.varSet.user", u.getName(), var.getKey(), var.getValue());
                    }
                    
                    for (IPermissionGroup g : gl) {
                        if (var.getValue().equals("<null>")) {
                            g.deleteVariable(var.getKey());
                        } else {
                            g.setVariable(var.getKey(), var.getValue());
                        }
                        
                        user.sendLocalizedMessage("general.permissions.varSet.group", g.getName(), var.getKey(), var.getValue());
                    }
                }
            }
        }
        return true;
    }
    
    private void resolveUsers(GoldenApple instance, User user, ArrayList<String> usersInput, ArrayList<IPermissionUser> usersOutput) {
        usersOutput.clear();
        for (String u : usersInput) {
            try {
                IPermissionUser pUser = PermissionManager.getInstance().findUser(u, false);
                
                if (pUser == null) {
                    user.sendLocalizedMessage("shared.userNotFoundWarning", u);
                } else {
                    usersOutput.add((User.hasUserInstance(pUser.getId())) ? User.getUser(pUser.getId()) : pUser);
                }
            } catch (Exception e) {
                user.sendLocalizedMessage("shared.userNotFoundWarning", u);
            }
        }
    }
    
    private void resolveGroups(GoldenApple instance, User user, ArrayList<String> groupsInput, ArrayList<IPermissionGroup> groupsOutput) {
        groupsOutput.clear();
        for (String g : groupsInput) {
            try {
                IPermissionGroup pGroup = PermissionManager.getInstance().getGroup(g);
                
                if (pGroup == null) {
                    user.sendLocalizedMessage("shared.groupNotFoundWarning", g);
                } else {
                    groupsOutput.add(pGroup);
                }
            } catch (Exception e) {
                user.sendLocalizedMessage("shared.groupNotFoundWarning", g);
            }
        }
    }
    
    private void resolvePermissions(GoldenApple instance, User user, ArrayList<String> permsInput, ArrayList<Permission> permsOutput) {
        permsOutput.clear();
        for (String ps : permsInput) {
            Permission p = PermissionManager.getInstance().getPermissionByName(ps);
            if (p == null && Bukkit.getPluginManager().getPermission(ps) != null) {
                p = PermissionManager.getInstance().getPermissionByName(ps, true);
            }
            
            if (p == null) {
                user.sendLocalizedMessage("error.permissions.perm.notFound", ps);
            } else {
                permsOutput.add(p);
            }
        }
    }
    
    private void modifyGroupMembershipUsers(GoldenApple instance, User user, ArrayList<IPermissionGroup> groups, ArrayList<IPermissionUser> addUsers, ArrayList<IPermissionUser> remUsers) {
        try {
            for (IPermissionUser u : addUsers) {
                for (IPermissionGroup ch : groups) {
                    ch.addUser(u);
                    AuditLog.logEvent(new GroupAddMemberEvent(user.getName(), u, ch));
                    GoldenApple.log(Level.INFO, "User " + u.getName() + " (PU" + u.getId() + ") has been added to group " + ch.getName() + " (PG" + ch.getId() + ") by " + user.getName());
                    user.sendLocalizedMessage("general.permissions.member.addUser", u.getName(), ch.getName());
                }
            }
            for (IPermissionUser u : remUsers) {
                for (IPermissionGroup ch : groups) {
                    ch.removeUser(u);
                    AuditLog.logEvent(new GroupRemoveMemberEvent(user.getName(), u, ch));
                    GoldenApple.log(Level.INFO, "User " + u.getName() + " (PU" + u.getId() + ") has been removed from group " + ch.getName() + " (PG" + ch.getId() + ") by " + user.getName());
                    user.sendLocalizedMessage("general.permissions.member.remUser", u.getName(), ch.getName());
                }
            }
        } catch (Exception e) {
            user.sendLocalizedMessage("error.permissions.member.unknown");
        }
    }
    
    private void modifyGroupMembershipGroups(GoldenApple instance, User user, ArrayList<IPermissionGroup> groups, ArrayList<IPermissionGroup> addGroups, ArrayList<IPermissionGroup> remGroups) {
        try {
            for (IPermissionGroup g : addGroups) {
                for (IPermissionGroup ch : groups) {
                    ch.addGroup(g);
                    AuditLog.logEvent(new GroupAddMemberEvent(user.getName(), g, ch));
                    GoldenApple.log(Level.INFO, "Group " + g.getName() + " (PG" + g.getId() + ") has been added to group " + ch.getName() + " (PG" + ch.getId() + ") by " + user.getName());
                    user.sendLocalizedMessage("general.permissions.member.addGroup", g.getName(), ch.getName());
                }
            }
            for (IPermissionGroup g : remGroups) {
                for (IPermissionGroup ch : groups) {
                    ch.removeGroup(g);
                    AuditLog.logEvent(new GroupRemoveMemberEvent(user.getName(), g, ch));
                    GoldenApple.log(Level.INFO, "Group " + g.getName() + " (PG" + g.getId() + ") has been removed from group " + ch.getName() + " (PG" + ch.getId() + ") by " + user.getName());
                    user.sendLocalizedMessage("general.permissions.member.remUser", g.getName(), ch.getName());
                }
            }
        } catch (Exception e) {
            user.sendLocalizedMessage("error.permissions.member.unknown");
        }
    }
    
    private void updatePermissions(GoldenApple instance, User user, IPermissionUser u, ArrayList<Permission> add, ArrayList<Permission> remove) {
        for (Permission p : add) {
            if (!u.hasPermissionSpecific(p)) {
                u.addPermission(p);
                AuditLog.logEvent(new PermissionGrantEvent(user.getName(), u, p.getFullName()));
                GoldenApple.log(Level.INFO, "User " + u.getName() + " (PU" + u.getId() + ") has been granted permission '" + p.getFullName() + "' by " + user.getName());
                user.sendLocalizedMessage("general.permissions.perm.add", p.getFullName(), u.getName());
            }
        }
        for (Permission p : remove) {
            if (u.hasPermissionSpecific(p)) {
                u.removePermission(p);
                AuditLog.logEvent(new PermissionRevokeEvent(user.getName(), u, p.getFullName()));
                GoldenApple.log(Level.INFO, "User " + u.getName() + " (PU" + u.getId() + ") has had permission '" + p.getFullName() + "' revoked by " + user.getName());
                user.sendLocalizedMessage("general.permissions.perm.rem", p.getFullName(), u.getName());
            }
        }
    }
    
    private void updatePermissions(GoldenApple instance, User user, IPermissionGroup g, ArrayList<Permission> add, ArrayList<Permission> remove) {
        for (Permission p : add) {
            if (!g.hasPermission(p, false)) {
                g.addPermission(p);
                AuditLog.logEvent(new PermissionGrantEvent(user.getName(), g, p.getFullName()));
                GoldenApple.log(Level.INFO, "Group " + g.getName() + " (PG" + g.getId() + ") has been granted permission '" + p.getFullName() + "' by " + user.getName());
                user.sendLocalizedMessage("general.permissions.perm.add", p.getFullName(), g.getName());
            }
        }
        for (Permission p : remove) {
            if (g.hasPermission(p, false)) {
                g.removePermission(p);
                AuditLog.logEvent(new PermissionRevokeEvent(user.getName(), g, p.getFullName()));
                GoldenApple.log(Level.INFO, "Group " + g.getName() + " (PG" + g.getId() + ") has had permission '" + p.getFullName() + "' revoked by " + user.getName());
                user.sendLocalizedMessage("general.permissions.perm.rem", p.getFullName(), g.getName());
            }
        }
    }
    
    private void sendHelp(User user, String commandLabel) {
        user.sendLocalizedMessage("header.help");
        user.sendLocalizedMultilineMessage("help.permissions", commandLabel);
    }
}
