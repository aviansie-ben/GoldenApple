package com.bendude56.goldenapple.permissions.command;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import org.bukkit.ChatColor;

import com.bendude56.goldenapple.GoldenApple;
import com.bendude56.goldenapple.User;
import com.bendude56.goldenapple.audit.AuditLog;
import com.bendude56.goldenapple.command.DualSyntaxCommand;
import com.bendude56.goldenapple.command.VerifyCommand;
import com.bendude56.goldenapple.permissions.DuplicateNameException;
import com.bendude56.goldenapple.permissions.IPermissionGroup;
import com.bendude56.goldenapple.permissions.IPermissionObject;
import com.bendude56.goldenapple.permissions.IPermissionUser;
import com.bendude56.goldenapple.permissions.PermissionManager;
import com.bendude56.goldenapple.permissions.PermissionManager.Permission;
import com.bendude56.goldenapple.permissions.UuidLookupException;
import com.bendude56.goldenapple.permissions.audit.GroupAddMemberEvent;
import com.bendude56.goldenapple.permissions.audit.GroupAddOwnerEvent;
import com.bendude56.goldenapple.permissions.audit.GroupRemoveMemberEvent;
import com.bendude56.goldenapple.permissions.audit.GroupRemoveOwnerEvent;
import com.bendude56.goldenapple.permissions.audit.PermissionGrantEvent;
import com.bendude56.goldenapple.permissions.audit.PermissionRevokeEvent;
import com.bendude56.goldenapple.util.ComplexArgumentParser;
import com.bendude56.goldenapple.util.ComplexArgumentParser.ArgumentInfo;

public class PermissionsCommand extends DualSyntaxCommand {
    
    @Override
    public void onExecuteComplex(GoldenApple instance, User user, String commandLabel, String[] args) {
        ComplexArgumentParser arg = new ComplexArgumentParser(getArguments());
        ArrayList<IPermissionUser> targetUsers = new ArrayList<IPermissionUser>();
        ArrayList<IPermissionGroup> targetGroups = new ArrayList<IPermissionGroup>();
        List<PermissionAction> actions = new ArrayList<PermissionAction>();
        
        if (args.length == 0 || args[0].equalsIgnoreCase("-?") || args[0].equalsIgnoreCase("help")) {
            sendHelp(user, commandLabel, true);
            return;
        }
        
        user.sendLocalizedMessage("header.permissions");
        
        if (!arg.parse(user, args)) {
            return;
        }
        
        if (arg.isDefined("add") && arg.isDefined("user-target") && !user.hasPermission(PermissionManager.userAddPermission)) {
            GoldenApple.logPermissionFail(user, commandLabel, args, true);
            return;
        } else if (arg.isDefined("add") && arg.isDefined("group-target") && !user.hasPermission(PermissionManager.groupRemovePermission)) {
            GoldenApple.logPermissionFail(user, commandLabel, args, true);
            return;
        } else if (arg.isDefined("remove") && arg.isDefined("user-target") && !user.hasPermission(PermissionManager.userRemovePermission)) {
            GoldenApple.logPermissionFail(user, commandLabel, args, true);
            return;
        } else if (arg.isDefined("remove") && arg.isDefined("group-target") && !user.hasPermission(PermissionManager.groupRemovePermission)) {
            GoldenApple.logPermissionFail(user, commandLabel, args, true);
            return;
        } else if (arg.isDefined("add") && arg.isDefined("remove")) {
            user.sendLocalizedMessage("error.permissions.conflict");
            return;
        }
        
        parseTargets(user, arg, targetUsers, targetGroups);
        if (!arg.isDefined("remove")) {
            parseActions(user, arg, targetUsers, targetGroups, actions);
            
            if (checkPermissions(user, actions)) {
                performActions(user, actions, !arg.isDefined("add"));
            } else {
                GoldenApple.logPermissionFail(user, commandLabel, args, true);
                return;
            }
        } else {
            if (targetUsers.isEmpty() && targetGroups.isEmpty()) {
                user.sendLocalizedMessage("error.permissions.noTarget", "-r");
                user.sendLocalizedMessage("error.permissions.noAction");
                return;
            }
            
            if (arg.isDefined("verify")) {
                for (IPermissionUser target : targetUsers) {
                    if (!PermissionManager.getInstance().isUserSticky(target.getId())) {
                        PermissionManager.getInstance().deleteUser(target.getId());
                        user.sendLocalizedMessage("general.permissions.remove.user", target.getName());
                    } else {
                        user.sendLocalizedMessage("error.permissions.remove.userOnline", target.getName());
                    }
                }
                
                for (IPermissionGroup target : targetGroups) {
                    if (!PermissionManager.getInstance().isGroupProtected(target)) {
                        PermissionManager.getInstance().deleteGroup(target.getId());
                        user.sendLocalizedMessage("general.permissions.remove.group", target.getName());
                    } else {
                        user.sendLocalizedMessage("error.permissions.remove.groupProtected", target.getName());
                    }
                }
            } else {
                user.sendLocalizedMessage("general.permissions.remove.warnStart");
                
                for (IPermissionUser target : targetUsers) {
                    user.sendLocalizedMessage("general.permissions.remove.warnUser", target.getName());
                }
                
                for (IPermissionGroup target : targetGroups) {
                    user.sendLocalizedMessage("general.permissions.remove.warnGroup", target.getName());
                }
                
                user.sendLocalizedMessage("general.permissions.remove.warnEnd");
                
                String cmd = commandLabel;
                for (String a : args) {
                    cmd += " " + a;
                }
                cmd += " -v";
                
                VerifyCommand.commands.put(user, cmd);
            }
        }
    }
    
    private void parseTargets(User user, ComplexArgumentParser arg, List<IPermissionUser> targetUsers, List<IPermissionGroup> targetGroups) {
        if (arg.isDefined("add")) {
            if (arg.isDefined("user-target")) {
                for (String targetName : arg.getStringList("user-target")) {
                    IPermissionUser target = PermissionManager.getInstance().findUser(targetName, false);
                    
                    if (target == null) {
                        try {
                            target = PermissionManager.getInstance().createUser(targetName);
                            user.sendLocalizedMessage("general.permissions.add.user", target.getName());
                        } catch (UuidLookupException e) {
                            user.sendLocalizedMessage("error.permissions.add.userNoUuid", targetName);
                        } catch (DuplicateNameException e) {
                            user.sendLocalizedMessage("error.permissions.add.userUnknown", targetName);
                        }
                    }
                    
                    if (target != null) {
                        targetUsers.add(target);
                    }
                }
            }
            
            if (arg.isDefined("group-target")) {
                for (String targetName : arg.getStringList("group-target")) {
                    IPermissionGroup target = PermissionManager.getInstance().getGroup(targetName);
                    
                    if (target == null) {
                        target = PermissionManager.getInstance().createGroup(targetName);
                        user.sendLocalizedMessage("general.permissions.add.group", target.getName());
                    }
                    
                    if (target != null) {
                        targetGroups.add(target);
                    }
                }
            }
        } else {
            if (arg.isDefined("user-target")) {
                for (String targetName : arg.getStringList("user-target")) {
                    IPermissionUser target = PermissionManager.getInstance().findUser(targetName, false);
                    
                    if (target != null) {
                        targetUsers.add(target);
                    } else {
                        user.sendLocalizedMessage("shared.userNotFoundWarning", targetName);
                    }
                }
            }
            
            if (arg.isDefined("group-target")) {
                for (String targetName : arg.getStringList("group-target")) {
                    IPermissionGroup target = PermissionManager.getInstance().getGroup(targetName);
                    
                    if (target != null) {
                        targetGroups.add(target);
                    } else {
                        user.sendLocalizedMessage("shared.groupNotFoundWarning", targetName);
                    }
                }
            }
        }
    }
    
    private void parseActions(User user, ComplexArgumentParser arg, List<IPermissionUser> targetUsers, List<IPermissionGroup> targetGroups, List<PermissionAction> actions) {
        PermissionManager instance = PermissionManager.getInstance();
        
        if (arg.isDefined("permission-add")) {
            for (String permissionName : arg.getStringList("permission-add")) {
                Permission permission = instance.getPermissionByName(permissionName, arg.isDefined("force"));
                
                if (permission != null) {
                    PermissionAction action = new PermissionAddAction(permission);
                    
                    if (action.addValidTargets(targetUsers, targetGroups) > 0) {
                        actions.add(action);
                    } else {
                        user.sendLocalizedMessage("error.permissions.noTarget", "-pa");
                    }
                } else {
                    user.sendLocalizedMessage("error.permissions.perm.notFound", permissionName);
                }
            }
        }
        
        if (arg.isDefined("permission-remove")) {
            for (String permissionName : arg.getStringList("permission-remove")) {
                Permission permission = instance.getPermissionByName(permissionName, arg.isDefined("force"));
                
                if (permission != null) {
                    PermissionAction action = new PermissionRemoveAction(permission);
                    
                    if (action.addValidTargets(targetUsers, targetGroups) > 0) {
                        actions.add(action);
                    } else {
                        user.sendLocalizedMessage("error.permissions.noTarget", "-pr");
                    }
                } else {
                    user.sendLocalizedMessage("error.permissions.perm.notFound", permissionName);
                }
            }
        }
        
        if (arg.isDefined("variable-set")) {
            for (Entry<String, Object> variable : arg.getKeyValuePairList("variable-set")) {
                PermissionAction action = new VariableSetAction(variable.getKey(), (String) variable.getValue());
                
                if (action.addValidTargets(targetUsers, targetGroups) > 0) {
                    actions.add(action);
                } else {
                    user.sendLocalizedMessage("error.permissions.noTarget", "-var:" + variable.getKey());
                }
            }
        }
        
        if (arg.isDefined("member-user-add")) {
            for (IPermissionUser member : arg.getUserList("member-user-add")) {
                PermissionAction action = new MemberAddAction(member);
                
                if (action.addValidTargets(targetUsers, targetGroups) > 0) {
                    actions.add(action);
                } else {
                    user.sendLocalizedMessage("error.permissions.noTarget", "-ua");
                }
            }
        }
        
        if (arg.isDefined("member-user-remove")) {
            for (IPermissionUser member : arg.getUserList("member-user-remove")) {
                PermissionAction action = new MemberRemoveAction(member);
                
                if (action.addValidTargets(targetUsers, targetGroups) > 0) {
                    actions.add(action);
                } else {
                    user.sendLocalizedMessage("error.permissions.noTarget", "-ur");
                }
            }
        }
        
        if (arg.isDefined("member-group-add")) {
            for (IPermissionGroup member : arg.getGroupList("member-group-add")) {
                PermissionAction action = new MemberAddAction(member);
                
                if (action.addValidTargets(targetUsers, targetGroups) > 0) {
                    actions.add(action);
                } else {
                    user.sendLocalizedMessage("error.permissions.noTarget", "-ga");
                }
            }
        }
        
        if (arg.isDefined("member-group-remove")) {
            for (IPermissionGroup member : arg.getGroupList("member-group-remove")) {
                PermissionAction action = new MemberRemoveAction(member);
                
                if (action.addValidTargets(targetUsers, targetGroups) > 0) {
                    actions.add(action);
                } else {
                    user.sendLocalizedMessage("error.permissions.noTarget", "-gr");
                }
            }
        }
        
        if (arg.isDefined("owner-add")) {
            for (IPermissionUser member : arg.getUserList("owner-add")) {
                PermissionAction action = new OwnerAddAction(member);
                
                if (action.addValidTargets(targetUsers, targetGroups) > 0) {
                    actions.add(action);
                } else {
                    user.sendLocalizedMessage("error.permissions.noTarget", "-oa");
                }
            }
        }
        
        if (arg.isDefined("owner-remove")) {
            for (IPermissionUser member : arg.getUserList("owner-remove")) {
                PermissionAction action = new OwnerRemoveAction(member);
                
                if (action.addValidTargets(targetUsers, targetGroups) > 0) {
                    actions.add(action);
                } else {
                    user.sendLocalizedMessage("error.permissions.noTarget", "-or");
                }
            }
        }
        
        if (arg.isDefined("chat-prefix")) {
            PermissionAction action = new ChatPrefixAction(arg.getString("chat-prefix"));
            
            if (action.addValidTargets(targetUsers, targetGroups) > 0) {
                actions.add(action);
            } else {
                user.sendLocalizedMessage("error.permissions.noTarget", "-cp");
            }
        }
        
        if (arg.isDefined("chat-color")) {
            PermissionAction action = new ChatColorAction(ChatColor.getByChar(arg.getString("chat-color")));
            
            if (action.addValidTargets(targetUsers, targetGroups) > 0) {
                actions.add(action);
            } else {
                user.sendLocalizedMessage("error.permissions.noTarget", "-cc");
            }
        }
        
        if (arg.isDefined("group-priority")) {
            PermissionAction action = new GroupPriorityAction(arg.getInt("group-priority"));
            
            if (action.addValidTargets(targetUsers, targetGroups) > 0) {
                actions.add(action);
            } else {
                user.sendLocalizedMessage("error.permissions.noTarget", "-gp");
            }
        }
        
        if (arg.isDefined("info")) {
            InfoAction action = new InfoAction();
            
            if (action.addValidTargets(targetUsers, targetGroups) > 0) {
                actions.add(action);
            } else {
                user.sendLocalizedMessage("error.permissions.noTarget", "-i");
            }
        }
    }
    
    private boolean checkPermissions(User user, List<PermissionAction> actions) {
        for (PermissionAction action : actions) {
            if (!action.isAllowedToPerformAction(user)) {
                return false;
            }
        }
        
        return true;
    }
    
    private void performActions(User user, List<PermissionAction> actions, boolean errorOnNoActions) {
        if (actions.size() > 0) {
            for (PermissionAction action : actions) {
                action.performAction(user);
            }
        } else if (errorOnNoActions) {
            user.sendLocalizedMessage("error.permissions.noAction");
        }
    }
    
    @Override
    public void onExecuteSimple(GoldenApple instance, User user, String commandLabel, String[] args) {
        // TODO Implement a simple /permissions command
        onExecuteComplex(instance, user, commandLabel, args);
    }
    
    private void sendHelp(User user, String commandLabel, boolean complex) {
        user.sendLocalizedMessage("header.help");
        user.sendLocalizedMultilineMessage((complex) ? "help.permissions.complex" : "help.permissions.simple", commandLabel);
    }
    
    private ArgumentInfo[] getArguments() {
        return new ArgumentInfo[] {
            ArgumentInfo.newStringList("user-target", "u", null, false),
            ArgumentInfo.newStringList("group-target", "g", null, false),
            
            ArgumentInfo.newSwitch("remove", "r", "remove"),
            ArgumentInfo.newSwitch("add", "a", "add"),
            ArgumentInfo.newSwitch("info", "i", "info"),
            ArgumentInfo.newSwitch("verify", "v", "verify"),
            ArgumentInfo.newSwitch("force", null, "force"),
            
            ArgumentInfo.newStringList("permission-add", "pa", null, false),
            ArgumentInfo.newStringList("permission-remove", "pr", null, false),
            ArgumentInfo.newKeyValuePair(ArgumentInfo.newStringList("variable-set", "var", null, true)),
            
            ArgumentInfo.newUserList("member-user-add", "ua", null, false, false),
            ArgumentInfo.newUserList("member-user-remove", "ur", null, false, false),
            ArgumentInfo.newGroupList("member-group-add", "ga", null, false),
            ArgumentInfo.newGroupList("member-group-remove", "gr", null, false),
            
            ArgumentInfo.newUserList("owner-add", "oa", null, false, false),
            ArgumentInfo.newUserList("owner-remove", "or", null, false, false),
            
            ArgumentInfo.newString("chat-prefix", "cp", null, true),
            ArgumentInfo.newString("chat-color", "cc", null, false),
            ArgumentInfo.newInt("group-priority", "gp", null)
        };
    }
    
    private String getName(IPermissionObject o) {
        if (o instanceof IPermissionUser) {
            return ((IPermissionUser) o).getName();
        } else if (o instanceof IPermissionGroup) {
            return ((IPermissionGroup) o).getName();
        } else {
            throw new UnsupportedOperationException();
        }
    }
    
    private abstract class PermissionAction {
        private final List<IPermissionObject> targets = new ArrayList<IPermissionObject>();
        
        public int addValidTargets(List<IPermissionUser> targetUsers, List<IPermissionGroup> targetGroups) {
            for (IPermissionUser target : targetUsers) {
                if (this.isActionValid(target)) {
                    targets.add(target);
                }
            }
            
            for (IPermissionGroup target : targetGroups) {
                if (this.isActionValid(target)) {
                    targets.add(target);
                }
            }
            
            return targets.size();
        }
        
        public boolean isAllowedToPerformAction(User user) {
            for (IPermissionObject target : targets) {
                if (!isAllowedToPerformAction(user, target)) {
                    return false;
                }
            }
            
            return true;
        }
        
        public void performAction(User user) {
            for (IPermissionObject target : targets) {
                this.performAction(user, target);
            }
        }
        
        public abstract boolean isActionValid(IPermissionObject target);
        public abstract boolean isAllowedToPerformAction(User user, IPermissionObject target);
        
        public abstract void performAction(User user, IPermissionObject target);
    }
    
    private class PermissionAddAction extends PermissionAction {
        private final Permission permission;
        
        public PermissionAddAction(Permission permission) {
            this.permission = permission;
        }

        @Override
        public boolean isActionValid(IPermissionObject target) {
            return !target.hasPermissionSpecific(permission);
        }

        @Override
        public boolean isAllowedToPerformAction(User user, IPermissionObject target) {
            if (target instanceof IPermissionUser) {
                return user.hasPermission(PermissionManager.userEditPermission);
            } else if (target instanceof IPermissionGroup) {
                return user.hasPermission(PermissionManager.groupEditPermission);
            } else {
                throw new UnsupportedOperationException();
            }
        }

        @Override
        public void performAction(User user, IPermissionObject target) {
            AuditLog.logEvent(new PermissionGrantEvent(user.getName(), target, permission.getFullName()));
            target.addPermission(permission);
            user.sendLocalizedMessage("general.permissions.perm.add", permission.getFullName(), getName(target));
        }
    }
    
    private class PermissionRemoveAction extends PermissionAction {
        private final Permission permission;
        
        public PermissionRemoveAction(Permission permission) {
            this.permission = permission;
        }

        @Override
        public boolean isActionValid(IPermissionObject target) {
            return target.hasPermissionSpecific(permission);
        }

        @Override
        public boolean isAllowedToPerformAction(User user, IPermissionObject target) {
            if (target instanceof IPermissionUser) {
                return user.hasPermission(PermissionManager.userEditPermission);
            } else if (target instanceof IPermissionGroup) {
                return user.hasPermission(PermissionManager.groupEditPermission);
            } else {
                throw new UnsupportedOperationException();
            }
        }

        @Override
        public void performAction(User user, IPermissionObject target) {
            AuditLog.logEvent(new PermissionRevokeEvent(user.getName(), target, permission.getFullName()));
            target.removePermission(permission);
            user.sendLocalizedMessage("general.permissions.perm.rem", permission.getFullName(), getName(target));
        }
    }
    
    private class VariableSetAction extends PermissionAction {
        private final String variableName;
        private final String variableValue;
        
        public VariableSetAction(String variableName, String variableValue) {
            this.variableName = variableName;
            this.variableValue = (variableValue.equals("<null>")) ? null : variableValue;
        }

        @Override
        public boolean isActionValid(IPermissionObject target) {
            return true;
        }

        @Override
        public boolean isAllowedToPerformAction(User user, IPermissionObject target) {
            if (target instanceof IPermissionUser) {
                return user.hasPermission(PermissionManager.userEditPermission);
            } else if (target instanceof IPermissionGroup) {
                return user.hasPermission(PermissionManager.groupEditPermission);
            } else {
                throw new UnsupportedOperationException();
            }
        }

        @Override
        public void performAction(User user, IPermissionObject target) {
            if (variableValue != null) {
                target.setVariable(variableName, variableValue);
            } else {
                target.deleteVariable(variableName);
            }
            
            if (target instanceof IPermissionUser) {
                user.sendLocalizedMessage("general.permissions.varSet.user", getName(target), variableName, (variableValue == null) ? "<null>" : variableValue);
            } else if (target instanceof IPermissionGroup) {
                user.sendLocalizedMessage("general.permissions.varSet.group", getName(target), variableName, (variableValue == null) ? "<null>" : variableValue);
            } else {
                throw new UnsupportedOperationException();
            }
        }
    }
    
    private class MemberAddAction extends PermissionAction {
        private final IPermissionObject member;
        
        public MemberAddAction(IPermissionObject member) {
            this.member = member;
        }

        @Override
        public boolean isActionValid(IPermissionObject target) {
            if (member instanceof IPermissionGroup) {
                return (target instanceof IPermissionGroup) && !((IPermissionGroup) target).isMember((IPermissionGroup) member, true);
            } else if (member instanceof IPermissionUser) {
                return (target instanceof IPermissionGroup) && !((IPermissionGroup) target).isMember((IPermissionUser) member, true);
            } else {
                throw new UnsupportedOperationException();
            }
        }

        @Override
        public boolean isAllowedToPerformAction(User user, IPermissionObject target) {
            return user.hasPermission(PermissionManager.groupEditPermission) || (member instanceof IPermissionUser && ((IPermissionGroup) target).isOwner(user));
        }

        @Override
        public void performAction(User user, IPermissionObject target) {
            AuditLog.logEvent(new GroupAddMemberEvent(user.getName(), member, (IPermissionGroup) target));
            
            if (member instanceof IPermissionGroup) {
                ((IPermissionGroup) target).addGroup((IPermissionGroup) member);
                user.sendLocalizedMessage("general.permissions.member.addGroup", getName(member), getName(target));
            } else if (member instanceof IPermissionUser) {
                ((IPermissionGroup) target).addUser((IPermissionUser) member);
                user.sendLocalizedMessage("general.permissions.member.addUser", getName(member), getName(target));
            } else {
                throw new UnsupportedOperationException();
            }
        }
    }
    
    private class MemberRemoveAction extends PermissionAction {
        private final IPermissionObject member;
        
        public MemberRemoveAction(IPermissionObject member) {
            this.member = member;
        }

        @Override
        public boolean isActionValid(IPermissionObject target) {
            if (member instanceof IPermissionGroup) {
                return (target instanceof IPermissionGroup) && ((IPermissionGroup) target).isMember((IPermissionGroup) member, true);
            } else if (member instanceof IPermissionUser) {
                return (target instanceof IPermissionGroup) && ((IPermissionGroup) target).isMember((IPermissionUser) member, true);
            } else {
                throw new UnsupportedOperationException();
            }
        }

        @Override
        public boolean isAllowedToPerformAction(User user, IPermissionObject target) {
            return user.hasPermission(PermissionManager.groupEditPermission) || (member instanceof IPermissionUser && ((IPermissionGroup) target).isOwner(user));
        }

        @Override
        public void performAction(User user, IPermissionObject target) {
            AuditLog.logEvent(new GroupRemoveMemberEvent(user.getName(), member, (IPermissionGroup) target));
            
            if (member instanceof IPermissionGroup) {
                ((IPermissionGroup) target).removeGroup((IPermissionGroup) member);
                user.sendLocalizedMessage("general.permissions.member.remGroup", getName(member), getName(target));
            } else if (member instanceof IPermissionUser) {
                ((IPermissionGroup) target).removeUser((IPermissionUser) member);
                user.sendLocalizedMessage("general.permissions.member.remUser", getName(member), getName(target));
            } else {
                throw new UnsupportedOperationException();
            }
        }
    }
    
    private class OwnerAddAction extends PermissionAction {
        private final IPermissionUser owner;
        
        public OwnerAddAction(IPermissionUser owner) {
            this.owner = owner;
        }

        @Override
        public boolean isActionValid(IPermissionObject target) {
            return (target instanceof IPermissionGroup) && !((IPermissionGroup) target).isOwner(owner);
        }

        @Override
        public boolean isAllowedToPerformAction(User user, IPermissionObject target) {
            return user.hasPermission(PermissionManager.groupEditPermission);
        }

        @Override
        public void performAction(User user, IPermissionObject target) {
            AuditLog.logEvent(new GroupAddOwnerEvent(user.getName(), owner, (IPermissionGroup) target));
            
            ((IPermissionGroup) target).addOwner((IPermissionUser) owner);
            user.sendLocalizedMessage("general.permissions.owner.add", getName(owner), getName(target));
        }
    }
    
    private class OwnerRemoveAction extends PermissionAction {
        private final IPermissionUser owner;
        
        public OwnerRemoveAction(IPermissionUser owner) {
            this.owner = owner;
        }

        @Override
        public boolean isActionValid(IPermissionObject target) {
            return (target instanceof IPermissionGroup) && ((IPermissionGroup) target).isOwner(owner);
        }

        @Override
        public boolean isAllowedToPerformAction(User user, IPermissionObject target) {
            return user.hasPermission(PermissionManager.groupEditPermission);
        }

        @Override
        public void performAction(User user, IPermissionObject target) {
            AuditLog.logEvent(new GroupRemoveOwnerEvent(user.getName(), owner, (IPermissionGroup) target));
            
            ((IPermissionGroup) target).removeOwner((IPermissionUser) owner);
            user.sendLocalizedMessage("general.permissions.owner.rem", getName(owner), getName(target));
        }
    }
    
    private class ChatPrefixAction extends PermissionAction {
        private final String prefix;
        
        public ChatPrefixAction(String prefix) {
            this.prefix = (prefix.equals("<null>")) ? null : prefix;
        }

        @Override
        public boolean isActionValid(IPermissionObject target) {
            return target instanceof IPermissionGroup;
        }

        @Override
        public boolean isAllowedToPerformAction(User user, IPermissionObject target) {
            return user.hasPermission(PermissionManager.groupEditPermission);
        }

        @Override
        public void performAction(User user, IPermissionObject target) {
            ((IPermissionGroup) target).setPrefix(prefix);
            user.sendLocalizedMessage("general.permissions.prefix", getName(target), (prefix == null) ? "<null>" : prefix);
        }
    }
    
    private class ChatColorAction extends PermissionAction {
        private final ChatColor color;
        
        public ChatColorAction(ChatColor color) {
            this.color = color;
        }

        @Override
        public boolean isActionValid(IPermissionObject target) {
            return target instanceof IPermissionGroup;
        }

        @Override
        public boolean isAllowedToPerformAction(User user, IPermissionObject target) {
            return user.hasPermission(PermissionManager.groupEditPermission);
        }

        @Override
        public void performAction(User user, IPermissionObject target) {
            ((IPermissionGroup) target).setChatColor(color != null, (color == null) ? ChatColor.WHITE : color);
            user.sendLocalizedMessage("general.permissions.chatColor", getName(target), (color == null) ? "<null>" : (color.toString() + color.getChar()));
        }
    }
    
    private class GroupPriorityAction extends PermissionAction {
        private final int priority;
        
        public GroupPriorityAction(int priority) {
            this.priority = priority;
        }

        @Override
        public boolean isActionValid(IPermissionObject target) {
            return target instanceof IPermissionGroup;
        }

        @Override
        public boolean isAllowedToPerformAction(User user, IPermissionObject target) {
            return user.hasPermission(PermissionManager.groupEditPermission);
        }

        @Override
        public void performAction(User user, IPermissionObject target) {
            ((IPermissionGroup) target).setPriority(priority);
            user.sendLocalizedMessage("general.permissions.priority", getName(target), priority + "");
        }
    }
    
    private class InfoAction extends PermissionAction {
        public InfoAction() {
            // Do nothing
        }
        
        @Override
        public boolean isActionValid(IPermissionObject target) {
            return target instanceof IPermissionGroup || target instanceof IPermissionUser;
        }
        
        @Override
        public boolean isAllowedToPerformAction(User user, IPermissionObject target) {
            if (target instanceof IPermissionUser) {
                return user.hasPermission(PermissionManager.userInfoPermission);
            } else if (target instanceof IPermissionGroup) {
                return user.hasPermission(PermissionManager.groupInfoPermission);
            } else {
                throw new UnsupportedOperationException();
            }
        }
        
        @Override
        public void performAction(User user, IPermissionObject target) {
            if (target instanceof IPermissionUser) {
                IPermissionUser userTarget = (IPermissionUser) target;
                List<Long> groups = userTarget.getParentGroups(true);
                List<Permission> permissions = userTarget.getPermissions(false);
                Map<String, String> variables = userTarget.getDefinedVariables();
                
                user.sendLocalizedMessage("general.permissions.info.user.pre", userTarget.getName());
                user.sendLocalizedMessage("general.permissions.info.id", userTarget.getId() + "");
                user.sendLocalizedMessage("general.permissions.info.user.uuid", userTarget.getUuid().toString());
                
                user.sendLocalizedMessage("general.permissions.info.user.inGroups");
                if (groups.size() == 0) {
                    user.sendLocalizedMessage("general.permissions.info.none");
                } else if (groups.size() > 20) {
                    user.sendLocalizedMessage("general.permissions.info.tooMany");
                } else {
                    for (Long g : groups) {
                        user.sendLocalizedMessage("general.permissions.info.user.group", PermissionManager.getInstance().getGroup(g).getName());
                    }
                }
                
                user.sendLocalizedMessage("general.permissions.info.permissions");
                if (permissions.size() == 0) {
                    user.sendLocalizedMessage("general.permissions.info.none");
                } else if (permissions.size() > 20) {
                    user.sendLocalizedMessage("general.permissions.info.tooMany");
                } else {
                    for (Permission p : permissions) {
                        user.sendLocalizedMessage("general.permissions.info.permission", p.getFullName());
                    }
                }
                
                user.sendLocalizedMessage("general.permissions.info.variables");
                if (variables.size() == 0) {
                    user.sendLocalizedMessage("general.permissions.info.none");
                } else if (variables.size() > 20) {
                    user.sendLocalizedMessage("general.permissions.info.tooMany");
                } else {
                    for (Entry<String, String> v : variables.entrySet()) {
                        user.sendLocalizedMessage("general.permissions.info.variable", v.getKey(), v.getValue());
                    }
                }
            } else if (target instanceof IPermissionGroup) {
                IPermissionGroup groupTarget = (IPermissionGroup) target;
                List<Long> owners = groupTarget.getOwners();
                List<Long> users = groupTarget.getUsers();
                List<Long> groups = groupTarget.getGroups();
                List<Permission> permissions = groupTarget.getPermissions(false);
                Map<String, String> variables = groupTarget.getDefinedVariables();
                
                user.sendLocalizedMessage("general.permissions.info.group.pre", groupTarget.getName());
                user.sendLocalizedMessage("general.permissions.info.id", groupTarget.getId() + "");
                user.sendLocalizedMessage("general.permissions.info.group.priority", groupTarget.getPriority() + "");
                
                if (groupTarget.getPrefix() != null && !groupTarget.getPrefix().isEmpty()) {
                    user.sendLocalizedMessage("general.permissions.info.group.chat", (groupTarget.isChatColorSet()) ? groupTarget.getChatColor().toString() : "", "[" + groupTarget.getPrefix() + "]");
                } else {
                    user.sendLocalizedMessage("general.permissions.info.group.noChat", (groupTarget.isChatColorSet()) ? groupTarget.getChatColor().toString() : "");
                }
                
                user.sendLocalizedMessage("general.permissions.info.group.owners");
                if (owners.size() == 0) {
                    user.sendLocalizedMessage("general.permissions.info.none");
                } else if (owners.size() > 20) {
                    user.sendLocalizedMessage("general.permissions.info.tooMany");
                } else {
                    for (Long o : owners) {
                        user.sendLocalizedMessage("general.permissions.info.group.member", PermissionManager.getInstance().getUser(o).getName());
                    }
                }
                
                user.sendLocalizedMessage("general.permissions.info.group.userMembers");
                if (users.size() == 0) {
                    user.sendLocalizedMessage("general.permissions.info.none");
                } else if (users.size() > 20) {
                    user.sendLocalizedMessage("general.permissions.info.tooMany");
                } else {
                    for (Long u : users) {
                        user.sendLocalizedMessage("general.permissions.info.group.member", PermissionManager.getInstance().getUser(u).getName());
                    }
                }
                
                user.sendLocalizedMessage("general.permissions.info.group.groupMembers");
                if (groups.size() == 0) {
                    user.sendLocalizedMessage("general.permissions.info.none");
                } else if (groups.size() > 20) {
                    user.sendLocalizedMessage("general.permissions.info.tooMany");
                } else {
                    for (Long g : groups) {
                        user.sendLocalizedMessage("general.permissions.info.group.member", PermissionManager.getInstance().getGroup(g).getName());
                    }
                }
                
                user.sendLocalizedMessage("general.permissions.info.permissions");
                if (permissions.size() == 0) {
                    user.sendLocalizedMessage("general.permissions.info.none");
                } else if (permissions.size() > 20) {
                    user.sendLocalizedMessage("general.permissions.info.tooMany");
                } else {
                    for (Permission p : permissions) {
                        user.sendLocalizedMessage("general.permissions.info.permission", p.getFullName());
                    }
                }
                
                user.sendLocalizedMessage("general.permissions.info.variables");
                if (variables.size() == 0) {
                    user.sendLocalizedMessage("general.permissions.info.none");
                } else if (variables.size() > 20) {
                    user.sendLocalizedMessage("general.permissions.info.tooMany");
                } else {
                    for (Entry<String, String> v : variables.entrySet()) {
                        user.sendLocalizedMessage("general.permissions.info.variable", v.getKey(), v.getValue());
                    }
                }
            } else {
                throw new UnsupportedOperationException();
            }
        }
    }
}
