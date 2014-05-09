package com.bendude56.goldenapple.request.command;

import java.util.List;

import com.bendude56.goldenapple.GoldenApple;
import com.bendude56.goldenapple.User;
import com.bendude56.goldenapple.command.GoldenAppleCommand;
import com.bendude56.goldenapple.command.VerifyCommand;
import com.bendude56.goldenapple.permissions.IPermissionGroup;
import com.bendude56.goldenapple.permissions.PermissionManager;
import com.bendude56.goldenapple.request.RequestManager;
import com.bendude56.goldenapple.request.RequestQueue;

public class RequestQueueCommand extends GoldenAppleCommand {

    @Override
    public boolean onExecute(GoldenApple instance, User user, String commandLabel, String[] args) {
        if (!user.hasPermission(RequestManager.editQueuePermission)) {
            GoldenApple.logPermissionFail(user, commandLabel, args, true);
            return true;
        }
        
        if (args.length == 0 || args[0].equalsIgnoreCase("help") || args[0].equalsIgnoreCase("-?")) {
            sendHelp(user, commandLabel);
            return true;
        }
        
        user.sendLocalizedMessage("header.request");
        
        RequestQueue queue = RequestManager.getInstance().getRequestQueueByName(args[0]);
        
        if (args[0].equalsIgnoreCase("list")) {
            List<RequestQueue> queues = RequestManager.getInstance().getAllRequestQueues();
            user.sendLocalizedMessage("general.requestqueue.list.head");
            
            if (queues.size() > 0) {
                for (RequestQueue rq : queues) {
                    user.sendLocalizedMessage("general.requestqueue.list.entry", rq.getName());
                }
            } else {
                user.sendLocalizedMessage("general.requestqueue.list.empty");
            }
        } else if (args.length > 1 && args[1].equalsIgnoreCase("create")) {
            if (queue != null) {
                user.sendLocalizedMessage("error.requestqueue.alreadyExists", queue.getName());
            } else {
                queue = RequestManager.getInstance().createQueue(args[0]);
                user.sendLocalizedMessage("general.requestqueue.created", queue.getName());
            }
        } else if (queue == null) {
            user.sendLocalizedMessage("error.request.queueNotFound", args[0]);
        } else if (!queue.canReceive(user) && !queue.canSend(user) && !user.hasPermission(RequestManager.viewAllPermission)) {
            GoldenApple.logPermissionFail(user, commandLabel, args, true);
        } else {
            if (args.length == 1 || args[1].equalsIgnoreCase("info")) {
                String sendGroup = (queue.getSendingGroup() == null) ? GoldenApple.getInstance().getLocalizationManager().getMessage(user, "general.request.nobody") : queue.getSendingGroup().getName();
                String receiveGroup = (queue.getReceivingGroup() == null) ? GoldenApple.getInstance().getLocalizationManager().getMessage(user, "general.request.nobody") : queue.getReceivingGroup().getName();
                String allowOffline = GoldenApple.getInstance().getLocalizationManager().getMessage(user, (queue.getAllowNoReceiver()) ? "shared.yes" : "shared.no");
                
                user.sendLocalizedMultilineMessage("general.requestqueue.info", queue.getName(), sendGroup, receiveGroup, allowOffline, queue.getMaxRequestsPerSender() + "");
            } else if (args[1].equalsIgnoreCase("delete")) {
                if (args.length == 2 || !args[2].equalsIgnoreCase("-v")) {
                    user.sendLocalizedMessage("general.requestqueue.deleteVerify", queue.getName());
                    
                    String cmd = commandLabel;
                    for (String a : args) cmd += " " + a;
                    cmd += " -v";
                    VerifyCommand.commands.put(user, cmd);
                } else {
                    RequestManager.getInstance().deleteQueue(queue.getId());
                    user.sendLocalizedMessage("general.requestqueue.deleted", queue.getName());
                }
            } else if (args[1].equalsIgnoreCase("send")) {
                if (args.length == 2) {
                    user.sendLocalizedMessage("shared.parameterMissing", "send");
                } else {
                    IPermissionGroup group = PermissionManager.getInstance().getGroup(args[2]);
                    
                    if (group == null) {
                        user.sendLocalizedMessage("shared.groupNotFoundWarning", args[2]);
                    } else {
                        queue.setSendingGroup(group);
                        user.sendLocalizedMessage("general.requestqueue.sendGroupSet", queue.getName(), group.getName());
                    }
                }
            } else if (args[1].equalsIgnoreCase("receive")) {
                if (args.length == 2) {
                    user.sendLocalizedMessage("shared.parameterMissing", "receive");
                } else {
                    IPermissionGroup group = PermissionManager.getInstance().getGroup(args[2]);
                    
                    if (group == null) {
                        user.sendLocalizedMessage("shared.groupNotFoundWarning", args[2]);
                    } else {
                        queue.setReceivingGroup(group);
                        user.sendLocalizedMessage("general.requestqueue.receiveGroupSet", queue.getName(), group.getName());
                    }
                }
            } else if (args[1].equalsIgnoreCase("allowoffline")) {
                if (queue.getAllowNoReceiver()) {
                    queue.setAllowNoReceiver(false);
                    user.sendLocalizedMessage("general.requestqueue.allowOfflineOff", queue.getName());
                } else {
                    queue.setAllowNoReceiver(true);
                    user.sendLocalizedMessage("general.requestqueue.allowOfflineOn", queue.getName());
                }
            } else if (args[1].equalsIgnoreCase("maxpersender")) {
                if (args.length == 2) {
                    user.sendLocalizedMessage("shared.parameterMissing", "maxpersender");
                } else {
                    try {
                        int maxRequestsPerSender = Integer.parseInt(args[2]);
                        
                        queue.setMaxRequestsPerSender(maxRequestsPerSender);
                        user.sendLocalizedMessage("general.requestqueue.maxPerSenderSet", queue.getName(), maxRequestsPerSender + "");
                    } catch (NumberFormatException e) {
                        user.sendLocalizedMessage("shared.notANumber", args[2]);
                    }
                }
            } else {
                user.sendLocalizedMessage("shared.unknownOption", args[1]);
            }
        }
        
        return true;
    }
    
    private void sendHelp(User user, String commandLabel) {
        user.sendLocalizedMessage("header.help");
        user.sendLocalizedMultilineMessage("help.requestqueue", commandLabel);
    }

}
