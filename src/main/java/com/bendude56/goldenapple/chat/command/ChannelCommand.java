package com.bendude56.goldenapple.chat.command;

import java.util.Map.Entry;

import com.bendude56.goldenapple.GoldenApple;
import com.bendude56.goldenapple.User;
import com.bendude56.goldenapple.chat.ChatManager;
import com.bendude56.goldenapple.chat.IAclChatChannel;
import com.bendude56.goldenapple.chat.IChatChannel;
import com.bendude56.goldenapple.chat.IChatChannel.ChatChannelAccessLevel;
import com.bendude56.goldenapple.chat.IChatChannel.ChatChannelFeature;
import com.bendude56.goldenapple.chat.IPersistentChatChannel;
import com.bendude56.goldenapple.command.DualSyntaxCommand;
import com.bendude56.goldenapple.command.VerifyCommand;
import com.bendude56.goldenapple.permissions.IPermissionGroup;
import com.bendude56.goldenapple.permissions.IPermissionUser;
import com.bendude56.goldenapple.permissions.PermissionManager;
import com.bendude56.goldenapple.util.ComplexArgumentParser;
import com.bendude56.goldenapple.util.ComplexArgumentParser.ArgumentInfo;

public class ChannelCommand extends DualSyntaxCommand {
    @Override
    public void onExecuteComplex(GoldenApple instance, User user, String commandLabel, String[] args) {
        ComplexArgumentParser arg = new ComplexArgumentParser(getArguments());
        IChatChannel channel = ChatManager.getInstance().getActiveChannel(user);
        
        if (args.length == 0 || args[0].equals("-?") || args[0].equals("help")) {
            sendHelp(user, commandLabel, true, ChatManager.getInstance().getActiveChannelLevel(user));
            return;
        }
        
        if (!arg.parse(user, args)) {
            return;
        }
        
        user.sendLocalizedMessage("module.chat.header");
        
        if (arg.isDefined("channellist")) {
            sendChannelList(user);
        }
        
        if (arg.isDefined("add")) {
            if (!user.hasPermission(ChatManager.channelAddPermission)) {
                GoldenApple.logPermissionFail(user, commandLabel, args, true);
                return;
            } else if (ChatManager.getInstance().channelExists(arg.getString("add"))) {
                user.sendLocalizedMessage("module.chat.error.channelAlreadyExists", arg.getString("add"));
                return;
            } else {
                channel = ChatManager.getInstance().createChannel(arg.getString("add"));
                
                if (!channel.join(user, true)) {
                    return;
                }
            }
        } else if (arg.isDefined("join")) {
            if (!ChatManager.getInstance().channelExists(arg.getString("join"))) {
                user.sendLocalizedMessage("module.chat.error.channelNotFound", arg.getString("join"));
            } else {
                channel = ChatManager.getInstance().getChannel(arg.getString("join"));
                
                if (!channel.join(user, true)) {
                    return;
                }
            }
        }
        
        if (arg.isDefined("userlist")) {
            if (!sendUserList(user, channel)) {
                return;
            }
        }
        
        if (arg.isDefined("whois")) {
            for (IPermissionUser u : arg.getUserList("whois")) {
                if (!sendWhoisInformation(user, u, channel, commandLabel, args)) {
                    return;
                }
            }
        }
        
        if (arg.isDefined("kick")) {
            for (IPermissionUser u : arg.getUserList("kick")) {
                if (!kickUser(user, (User) u, channel, commandLabel, args)) {
                    return;
                }
            }
        }
        
        if (arg.isDefined("userlevel")) {
            for (Entry<String, Object> userLevel : arg.getKeyValuePairList("userlevel")) {
                if (!setUserLevel(user, (IPermissionUser) userLevel.getValue(), userLevel.getKey(), true, channel, commandLabel, args)) {
                    return;
                }
            }
        }
        
        if (arg.isDefined("grouplevel")) {
            for (Entry<String, Object> groupLevel : arg.getKeyValuePairList("grouplevel")) {
                if (!setGroupLevel(user, (IPermissionGroup) groupLevel.getValue(), groupLevel.getKey(), true, channel, commandLabel, args)) {
                    return;
                }
            }
        }
        
        if (arg.isDefined("defaultlevel")) {
            if (!setDefaultLevel(user, arg.getString("defaultlevel"), true, channel, commandLabel, args)) {
                return;
            }
        }
        
        if (arg.isDefined("motd")) {
            if (!setMotd(user, arg.getString("motd"), channel, commandLabel, args)) {
                return;
            }
        }
        
        if (arg.isDefined("strict")) {
            if (!toggleStrictCensoring(user, channel, commandLabel, args)) {
                return;
            }
        }
        
        if (arg.isDefined("delete")) {
            if (!deleteChannel(user, arg.isDefined("verify"), true, channel, commandLabel, args)) {
                return;
            }
        }
        
        if (arg.isDefined("leave")) {
            if (channel == null) {
                user.sendLocalizedMessage("module.chat.error.notInChannel.command");
                return;
            } else {
                channel.leave(user, true);
            }
        }
    }
    
    @Override
    public void onExecuteSimple(GoldenApple instance, User user, String commandLabel, String[] args) {
        IChatChannel channel = ChatManager.getInstance().getActiveChannel(user);
        
        if (args.length == 0 || args[0].equals("-?") || args[0].equals("help")) {
            sendHelp(user, commandLabel, false, ChatManager.getInstance().getActiveChannelLevel(user));
            return;
        }
        
        if (args[0].equalsIgnoreCase("list") && channel == null) {
            if (args.length != 1) {
                sendHelp(user, commandLabel, false, null);
            } else {
                sendChannelList(user);
            }
        } else if (args[0].equalsIgnoreCase("add")) {
            if (!user.hasPermission(ChatManager.channelAddPermission)) {
                GoldenApple.logPermissionFail(user, commandLabel, args, true);
            } else if (args.length != 2) {
                sendHelp(user, commandLabel, false, (channel == null) ? null : channel.getAccessLevel(user));
            } else if (ChatManager.getInstance().channelExists(args[1])) {
                user.sendLocalizedMessage("module.chat.error.channelAlreadyExists", args[1]);
            } else {
                channel = ChatManager.getInstance().createChannel(args[1]);
                channel.join(user, true);
            }
        } else if (args[0].equalsIgnoreCase("join")) {
            if (args.length != 2) {
                sendHelp(user, commandLabel, false, (channel == null) ? null : channel.getAccessLevel(user));
            } else if (!ChatManager.getInstance().channelExists(args[1])) {
                user.sendLocalizedMessage("module.chat.error.channelNotFound", args[1]);
            } else {
                channel = ChatManager.getInstance().getChannel(args[1]);
                channel.join(user, true);
            }
        } else if (args[0].equalsIgnoreCase("list") && channel != null) {
            if (args.length != 1) {
                sendHelp(user, commandLabel, false, (channel == null) ? null : channel.getAccessLevel(user));
            } else {
                sendUserList(user, channel);
            }
        } else if (args[0].equalsIgnoreCase("whois")) {
            if (args.length != 2) {
                sendHelp(user, commandLabel, false, (channel == null) ? null : channel.getAccessLevel(user));
            } else {
                IPermissionUser target = PermissionManager.getInstance().findUser(args[1], true);
                
                if (target == null) {
                    user.sendLocalizedMessage("shared.parser.userNotFound.error", args[1]);
                } else {
                    sendWhoisInformation(user, target, channel, commandLabel, args);
                }
            }
        } else if (args[0].equalsIgnoreCase("kick")) {
            if (args.length != 2) {
                sendHelp(user, commandLabel, false, (channel == null) ? null : channel.getAccessLevel(user));
            } else {
                User target = User.findUser(args[1]);
                
                if (target == null) {
                    user.sendLocalizedMessage("shared.parser.userNotFound.error", args[1]);
                } else {
                    kickUser(user, target, channel, commandLabel, args);
                }
            }
        } else if (args[0].equalsIgnoreCase("level")) {
            if (args.length != 3) {
                sendHelp(user, commandLabel, false, (channel == null) ? null : channel.getAccessLevel(user));
            } else if (args[1].equalsIgnoreCase("default")) {
                setDefaultLevel(user, args[2], false, channel, commandLabel, args);
            } else {
                IPermissionUser target = PermissionManager.getInstance().findUser(args[1], true);
                
                if (target == null) {
                    user.sendLocalizedMessage("shared.parser.userNotFound.error", args[1]);
                } else {
                    setUserLevel(user, target, args[2], false, channel, commandLabel, args);
                }
            }
        } else if (args[0].equalsIgnoreCase("motd")) {
            if (args.length < 2) {
                sendHelp(user, commandLabel, false, (channel == null) ? null : channel.getAccessLevel(user));
            } else {
                String motd = "";
                
                for (int i = 1; i < args.length; i++) {
                    if (!motd.isEmpty()) {
                        motd += " ";
                    }
                    
                    motd += args[i];
                }
                
                setMotd(user, motd, channel, commandLabel, args);
            }
        } else if (args[0].equalsIgnoreCase("strict")) {
            if (args.length != 1) {
                sendHelp(user, commandLabel, false, (channel == null) ? null : channel.getAccessLevel(user));
            } else {
                toggleStrictCensoring(user, channel, commandLabel, args);
            }
        } else if (args[0].equalsIgnoreCase("delete")) {
            if (args.length != 1 && (args.length != 2 || !args[1].equals("-v"))) {
                sendHelp(user, commandLabel, false, (channel == null) ? null : channel.getAccessLevel(user));
            } else {
                deleteChannel(user, args.length == 2, false, channel, commandLabel, args);
            }
        } else if (args[0].equalsIgnoreCase("leave")) {
            if (args.length != 1) {
                sendHelp(user, commandLabel, false, (channel == null) ? null : channel.getAccessLevel(user));
            } else {
                if (channel == null) {
                    user.sendLocalizedMessage("module.chat.error.notInChannel.command");
                    return;
                } else {
                    channel.leave(user, true);
                }
            }
        } else {
            sendHelp(user, commandLabel, false, (channel == null) ? null : channel.getAccessLevel(user));
        }
    }
    
    private boolean sendChannelList(User user) {
        user.sendLocalizedMessage("module.chat.channelList.header");
        
        for (IChatChannel c : ChatManager.getInstance().getActiveChannels()) {
            switch (c.getDisplayType(user)) {
                case CONNECTED:
                    user.sendLocalizedMessage("module.chat.channelList.entry.connected", c.getListedName());
                    break;
                case NORMAL:
                    user.sendLocalizedMessage("module.chat.channelList.entry.normal", c.getListedName());
                    break;
                case GRAYED_OUT:
                    user.sendLocalizedMessage("module.chat.channelList.entry.grayedOut", c.getListedName());
                    break;
                case HIDDEN:
                	break;
            }
        }
        
        return true;
    }
    
    private boolean sendUserList(User user, IChatChannel channel) {
        if (channel == null) {
            user.sendLocalizedMessage("module.chat.error.notInChannel.command");
            return false;
        } else if (!channel.isFeatureAccessible(user, ChatChannelFeature.LIST_USERS)) {
            user.sendLocalizedMessage("module.chat.error.unsupportedCommand");
            return false;
        } else {
            user.sendLocalizedMessage("module.chat.userList.header");
            
            for (User online : channel.getActiveUsers()) {
                user.sendLocalizedMessage("module.chat.userList.entry", online.getChatDisplayName());
            }
            
            return true;
        }
    }
    
    private boolean sendWhoisInformation(User user, IPermissionUser target, IChatChannel channel, String commandLabel, String[] args) {
        if (channel == null) {
            user.sendLocalizedMessage("module.chat.error.notInChannel.command");
            return false;
        } else if (!channel.isFeatureAccessible(user, ChatChannelFeature.WHOIS)) {
            user.sendLocalizedMessage("module.chat.error.unsupportedCommand");
            return false;
        } else if (!channel.getAccessLevel(user).isModerator()) {
            GoldenApple.logPermissionFail(user, commandLabel, args, true);
            return false;
        } else {
            channel.sendWhoisInformation(user, target);
            return true;
        }
    }
    
    private boolean kickUser(User user, User target, IChatChannel channel, String commandLabel, String[] args) {
        if (channel == null) {
            user.sendLocalizedMessage("module.chat.error.notInChannel.command");
            return false;
        } else if (!channel.isFeatureAccessible(user, ChatChannelFeature.KICK_USER)) {
            user.sendLocalizedMessage("module.chat.error.unsupportedCommand");
            return false;
        } else {
            if (!channel.isInChannel(target)) {
                user.sendLocalizedMessage("module.chat.kick.notInChannel");
            } else if (!channel.getAccessLevel(user).canPunish(channel.getAccessLevel(target))) {
                GoldenApple.logPermissionFail(user, commandLabel, args, true);
                return false;
            } else {
                channel.kick(target, true);
            }
            
            return true;
        }
    }
    
    private boolean setUserLevel(User user, IPermissionUser target, String levelName, boolean complex, IChatChannel channel, String commandLabel, String[] args) {
        if (channel == null) {
            user.sendLocalizedMessage("module.chat.error.notInChannel.command");
            return false;
        } else if (!(channel instanceof IAclChatChannel) || !channel.isFeatureAccessible(user, ChatChannelFeature.SET_ACCESS_LEVELS)) {
            user.sendLocalizedMessage("module.chat.error.unsupportedCommand");
            return false;
        } else {
            ChatChannelAccessLevel level = (complex) ? ChatChannelAccessLevel.fromComplexCommandArgument(user, levelName) : ChatChannelAccessLevel.fromSimpleCommandArgument(user, levelName);
            
            if (level == null) {
                user.sendLocalizedMessage("module.chat.accessLevel.unknownLevel", levelName);
            } else if (!channel.getAccessLevel(user).canGrant(level)) {
                GoldenApple.logPermissionFail(user, commandLabel, args, true);
                return false;
            } else if (!channel.getAccessLevel(user).canRevoke(channel.getAccessLevel(target))) {
                GoldenApple.logPermissionFail(user, commandLabel, args, true);
                return false;
            } else if (level != ChatChannelAccessLevel.NO_ACCESS && level.getLevelId() < ((IAclChatChannel) channel).calculateMinimumAccessLevel(target).getLevelId()) {
                user.sendLocalizedMessage("module.chat.accessLevel.user.belowMinimum");
            } else {
                ((IAclChatChannel) channel).setExplicitAccessLevel(target, level);
                
                if (level == ChatChannelAccessLevel.NO_ACCESS) {
                    user.sendLocalizedMessage("module.chat.accessLevel.user.unset", target.getName());
                } else {
                    user.sendLocalizedMessage("module.chat.accessLevel.user.set", target.getName(), level.getDisplayName(user));
                }
            }
            
            return true;
        }
    }
    
    private boolean setGroupLevel(User user, IPermissionGroup target, String levelName, boolean complex, IChatChannel channel, String commandLabel, String[] args) {
        if (channel == null) {
            user.sendLocalizedMessage("module.chat.error.notInChannel.command");
            return false;
        } else if (!(channel instanceof IAclChatChannel) || !channel.isFeatureAccessible(user, ChatChannelFeature.SET_ACCESS_LEVELS)) {
            user.sendLocalizedMessage("module.chat.error.unsupportedCommand");
            return false;
        } else if (!channel.getAccessLevel(user).isAdministrator()) {
            GoldenApple.logPermissionFail(user, commandLabel, args, true);
            return false;
        } else {
            ChatChannelAccessLevel level = (complex) ? ChatChannelAccessLevel.fromComplexCommandArgument(user, levelName) : ChatChannelAccessLevel.fromSimpleCommandArgument(user, levelName);
            
            if (level == null) {
                user.sendLocalizedMessage("module.chat.accessLevel.unknownLevel", levelName);
            } else {
                ((IAclChatChannel) channel).setExplicitAccessLevel(target, level);
                
                if (level == ChatChannelAccessLevel.NO_ACCESS) {
                    user.sendLocalizedMessage("module.chat.accessLevel.group.unset", target.getName());
                } else {
                    user.sendLocalizedMessage("module.chat.accessLevel.group.set", target.getName(), level.getDisplayName(user));
                }
            }
            
            return true;
        }
    }
    
    private boolean setDefaultLevel(User user, String levelName, boolean complex, IChatChannel channel, String commandLabel, String[] args) {
        if (channel == null) {
            user.sendLocalizedMessage("module.chat.error.notInChannel.command");
            return false;
        } else if (!channel.isFeatureAccessible(user, ChatChannelFeature.SET_ACCESS_LEVELS)) {
            user.sendLocalizedMessage("module.chat.error.unsupportedCommand");
            return false;
        } else if (!channel.getAccessLevel(user).isAdministrator()) {
            GoldenApple.logPermissionFail(user, commandLabel, args, true);
            return false;
        } else {
            ChatChannelAccessLevel level = (complex) ? ChatChannelAccessLevel.fromComplexCommandArgument(user, levelName) : ChatChannelAccessLevel.fromSimpleCommandArgument(user, levelName);
            
            if (level == null) {
                user.sendLocalizedMessage("module.chat.error.notInChannel.command");
            } else if (level.isVip()) {
                user.sendLocalizedMessage("module.chat.accessLevel.default.tooHigh");
            } else {
                channel.setDefaultAccessLevel(level);
                
                user.sendLocalizedMessage("module.chat.accessLevel.default.set", level.getDisplayName(user));
            }
            
            return true;
        }
    }
    
    private boolean setMotd(User user, String motd, IChatChannel channel, String commandLabel, String[] args) {
        if (channel == null) {
            user.sendLocalizedMessage("module.chat.error.notInChannel.command");
            return false;
        } else if (!channel.isFeatureAccessible(user, ChatChannelFeature.SET_MOTD)) {
            user.sendLocalizedMessage("module.chat.error.unsupportedCommand");
            return false;
        } else if (!channel.getAccessLevel(user).isSuperModerator()) {
            GoldenApple.logPermissionFail(user, commandLabel, args, true);
            return false;
        } else {
            if (motd.equalsIgnoreCase("none")) {
                channel.setMotd(null);
                
                if (channel instanceof IPersistentChatChannel) {
                    ((IPersistentChatChannel) channel).save();
                }
                
                user.sendLocalizedMessage("module.chat.motd.unset");
            } else {
                channel.setMotd(motd);
                
                if (channel instanceof IPersistentChatChannel) {
                    ((IPersistentChatChannel) channel).save();
                }
                
                user.sendLocalizedMessage("module.chat.motd.set");
            }
            
            return true;
        }
    }
    
    private boolean toggleStrictCensoring(User user, IChatChannel channel, String commandLabel, String[] args) {
        if (channel == null) {
            user.sendLocalizedMessage("module.chat.error.notInChannel.command");
            return false;
        } else if (!channel.isFeatureAccessible(user, ChatChannelFeature.SET_CENSOR)) {
            user.sendLocalizedMessage("module.chat.error.unsupportedCommand");
            return false;
        } else if (!channel.getAccessLevel(user).isAdministrator()) {
            GoldenApple.logPermissionFail(user, commandLabel, args, true);
            return false;
        } else {
            if (channel.getCensor() == ChatManager.getInstance().getStrictCensor()) {
                channel.setCensor(ChatManager.getInstance().getDefaultCensor());
                
                user.sendLocalizedMessage("module.chat.censor.normal");
            } else {
                channel.setCensor(ChatManager.getInstance().getStrictCensor());
                
                user.sendLocalizedMessage("module.chat.censor.strict");
            }
            
            if (channel instanceof IPersistentChatChannel) {
                ((IPersistentChatChannel) channel).save();
            }
            
            return true;
        }
    }
    
    private boolean deleteChannel(User user, boolean verified, boolean complex, IChatChannel channel, String commandLabel, String[] args) {
        if (channel == null) {
            user.sendLocalizedMessage("module.chat.error.notInChannel.command");
            return false;
        } else if (!channel.isFeatureAccessible(user, ChatChannelFeature.DELETE)) {
            user.sendLocalizedMessage("module.chat.error.unsupportedCommand");
            return false;
        } else if (!channel.getAccessLevel(user).isAdministrator()) {
            GoldenApple.logPermissionFail(user, commandLabel, args, true);
            return false;
        } else {
            if (verified) {
                channel.delete();
            } else {
                VerifyCommand.commands.put(user, (complex) ? "gachannel -d -v" : "gachannel delete -v");
                user.sendLocalizedMessage("module.chat.delete.warning");
            }
            
            return true;
        }
    }
    
    private void sendHelp(User user, String commandLabel, boolean complex, ChatChannelAccessLevel level) {
        user.sendLocalizedMessage("module.chat.header");
        if (level == null) {
            showHelpSection(user, commandLabel, "out", complex);
        } else {
            showHelpSection(user, commandLabel, "inNormal", complex);
            if (level.isModerator()) {
                showHelpSection(user, commandLabel, "inMod", complex);
            }
            if (level.isSuperModerator()) {
                showHelpSection(user, commandLabel, "inSuperMod", complex);
            }
            if (level.isAdministrator()) {
                showHelpSection(user, commandLabel, "inAdmin", complex);
            }
        }
    }
    
    private void showHelpSection(User user, String commandLabel, String section, boolean complex) {
        user.sendLocalizedMessage((complex) ? "module.chat.help." + section + ".complex" : "module.chat.help." + section + ".simple", commandLabel);
    }
    
    private ArgumentInfo[] getArguments() {
        return new ArgumentInfo[] {
            ArgumentInfo.newSwitch("leave", "l", "leave"),
            ArgumentInfo.newString("join", "j", "join", false),
            ArgumentInfo.newSwitch("channellist", "cls", "channellist"),
            ArgumentInfo.newSwitch("userlist", "uls", "userlist"),
            
            ArgumentInfo.newString("add", "a", "add", false),
            
            ArgumentInfo.newUserList("kick", "k", "kick", false, true),
            ArgumentInfo.newUserList("whois", null, "whois", false, true),
            
            ArgumentInfo.newString("motd", null, "motd", true),
            ArgumentInfo.newKeyValuePair(ArgumentInfo.newUser("userlevel", "lvl", "userlevel", false, false)),
            
            ArgumentInfo.newSwitch("strict", null, "strict"),
            ArgumentInfo.newKeyValuePair(ArgumentInfo.newGroup("grouplevel", "glvl", "grouplevel", false)),
            ArgumentInfo.newString("defaultlevel", "dlvl", "defaultlevel", false),
            ArgumentInfo.newSwitch("delete", "d", "delete"),
            
            ArgumentInfo.newSwitch("verify", "v", "verify")
        };
    }
}
