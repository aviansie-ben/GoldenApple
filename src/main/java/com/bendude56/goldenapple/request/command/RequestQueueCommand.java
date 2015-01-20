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
        
        user.sendLocalizedMessage("module.request.header");
        
        RequestQueue queue = RequestManager.getInstance().getRequestQueueByName(args[0]);
        
        if (args[0].equalsIgnoreCase("list")) {
            List<RequestQueue> queues = RequestManager.getInstance().getAllRequestQueues();
            user.sendLocalizedMessage("module.request.editQueue.list.header");
            
            if (queues.size() > 0) {
                for (RequestQueue rq : queues) {
                    user.sendLocalizedMessage("module.request.editQueue.list.entry", rq.getName());
                }
            } else {
                user.sendLocalizedMessage("module.request.editQueue.list.empty");
            }
        } else if (args.length > 1 && args[1].equalsIgnoreCase("create")) {
            if (queue != null) {
                user.sendLocalizedMessage("module.request.editQueue.create.alreadyExists", queue.getName());
            } else {
                queue = RequestManager.getInstance().createQueue(args[0]);
                user.sendLocalizedMessage("module.request.editQueue.create.success", queue.getName());
            }
        } else if (queue == null) {
            user.sendLocalizedMessage("module.request.editQueue.error.notFound", args[0]);
        } else if (!queue.canReceive(user) && !queue.canSend(user) && !user.hasPermission(RequestManager.viewAllPermission)) {
            GoldenApple.logPermissionFail(user, commandLabel, args, true);
        } else {
            if (args.length == 1 || args[1].equalsIgnoreCase("info")) {
                String sendGroup = (queue.getSendingGroup() == null) ? user.getLocalizedMessage("module.request.info.nobody") : queue.getSendingGroup().getName();
                String receiveGroup = (queue.getReceivingGroup() == null) ? user.getLocalizedMessage("module.request.info.nobody") : queue.getReceivingGroup().getName();
                String allowOffline = user.getLocalizedMessage((queue.getAllowNoReceiver()) ? "shared.values.yes" : "shared.values.no");
                
                user.sendLocalizedMessage("module.request.editQueue.info", queue.getName(), sendGroup, receiveGroup, allowOffline, queue.getMaxRequestsPerSender());
            } else if (args[1].equalsIgnoreCase("delete")) {
                if (args.length == 2 || !args[2].equalsIgnoreCase("-v")) {
                    user.sendLocalizedMessage("module.request.editQueue.warning", queue.getName());
                    
                    String cmd = commandLabel;
                    for (String a : args) {
                        cmd += " " + a;
                    }
                    cmd += " -v";
                    VerifyCommand.commands.put(user, cmd);
                } else {
                    RequestManager.getInstance().deleteQueue(queue.getId());
                    user.sendLocalizedMessage("module.request.editQueue.success", queue.getName());
                }
            } else if (args[1].equalsIgnoreCase("send")) {
                if (args.length == 2) {
                    user.sendLocalizedMessage("shared.parser.parameterMissing", "send");
                } else {
                    IPermissionGroup group = PermissionManager.getInstance().getGroup(args[2]);
                    
                    if (group == null) {
                        user.sendLocalizedMessage("shared.parser.groupNotFound.error", args[2]);
                    } else {
                        queue.setSendingGroup(group);
                        user.sendLocalizedMessage("module.request.editQueue.setOption.sendGroup", queue.getName(), group.getName());
                    }
                }
            } else if (args[1].equalsIgnoreCase("receive")) {
                if (args.length == 2) {
                    user.sendLocalizedMessage("shared.parser.parameterMissing", "receive");
                } else {
                    IPermissionGroup group = PermissionManager.getInstance().getGroup(args[2]);
                    
                    if (group == null) {
                        user.sendLocalizedMessage("shared.parser.groupNotFound.error", args[2]);
                    } else {
                        queue.setReceivingGroup(group);
                        user.sendLocalizedMessage("module.request.editQueue.setOption.receiveGroup", queue.getName(), group.getName());
                    }
                }
            } else if (args[1].equalsIgnoreCase("allowoffline")) {
                if (queue.getAllowNoReceiver()) {
                    queue.setAllowNoReceiver(false);
                    user.sendLocalizedMessage("module.request.editQueue.setOption.allowOffline.off", queue.getName());
                } else {
                    queue.setAllowNoReceiver(true);
                    user.sendLocalizedMessage("module.request.editQueue.setOption.allowOffline.on", queue.getName());
                }
            } else if (args[1].equalsIgnoreCase("maxpersender")) {
                if (args.length == 2) {
                    user.sendLocalizedMessage("shared.parser.parameterMissing", "maxpersender");
                } else {
                    try {
                        int maxRequestsPerSender = Integer.parseInt(args[2]);
                        
                        queue.setMaxRequestsPerSender(maxRequestsPerSender);
                        user.sendLocalizedMessage("module.request.editQueue.setOption.maxPerSender", queue.getName(), maxRequestsPerSender);
                    } catch (NumberFormatException e) {
                        user.sendLocalizedMessage("shared.convertError.number", args[2]);
                    }
                }
            } else {
                user.sendLocalizedMessage("shared.parser.unknownOption", args[1]);
            }
        }
        
        return true;
    }
    
    private void sendHelp(User user, String commandLabel) {
        user.sendLocalizedMessage("module.request.header");
        user.sendLocalizedMessage("module.request.editQueue.help", commandLabel);
    }
    
}
