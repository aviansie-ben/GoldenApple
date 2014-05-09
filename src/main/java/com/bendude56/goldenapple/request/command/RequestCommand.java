package com.bendude56.goldenapple.request.command;

import java.text.SimpleDateFormat;
import java.util.List;

import com.bendude56.goldenapple.GoldenApple;
import com.bendude56.goldenapple.User;
import com.bendude56.goldenapple.command.GoldenAppleCommand;
import com.bendude56.goldenapple.permissions.IPermissionUser;
import com.bendude56.goldenapple.permissions.PermissionManager;
import com.bendude56.goldenapple.request.Request;
import com.bendude56.goldenapple.request.Request.RequestStatus;
import com.bendude56.goldenapple.request.RequestManager;
import com.bendude56.goldenapple.request.RequestManager.AutoAssignUserEvent;
import com.bendude56.goldenapple.request.RequestQueue;

public class RequestCommand extends GoldenAppleCommand {

    @Override
    public boolean onExecute(GoldenApple instance, User user, String commandLabel, String[] args) {
        if (args.length == 0 || args[0].equalsIgnoreCase("help") || args[0].equalsIgnoreCase("-?")) {
            sendHelp(user, commandLabel);
            return true;
        }
        
        user.sendLocalizedMessage("header.request");
        
        RequestQueue queue = RequestManager.getInstance().getRequestQueueByName(args[0]);
        
        if (args[0].equalsIgnoreCase("list")) {
            boolean v = user.hasPermission(RequestManager.viewAllPermission);
            boolean listed = false;
            
            user.sendLocalizedMessage("general.request.listqueue.head");
            for (RequestQueue rq : RequestManager.getInstance().getAllRequestQueues()) {
                boolean s = rq.canSend(user);
                boolean r = rq.canReceive(user);
                
                String message = "general.request.listqueue.entry";
                if (s) message += "S";
                if (r) message += "R";
                if (v) message += "V";
                
                if (s || r || v) {
                    user.sendLocalizedMessage(message, rq.getName());
                    listed = true;
                }
            }
            
            if (!listed) {
                user.sendLocalizedMessage("general.request.listqueue.empty");
            }
        } else if (queue == null) {
            user.sendLocalizedMessage("error.request.queueNotFound", args[0]);
        } else if (!queue.canReceive(user) && !queue.canSend(user) && !user.hasPermission(RequestManager.viewAllPermission)) {
            GoldenApple.logPermissionFail(user, commandLabel, args, true);
        } else {
            if (args.length == 1 || args[1].equalsIgnoreCase("list")) {
                if (queue.canSend(user)) {
                    List<Request> requests = queue.getRequestsBySender(user, false, true);
                    user.sendLocalizedMessage("general.request.list.yours");
                    
                    if (requests.size() == 0) {
                        user.sendLocalizedMessage("general.request.list.empty");
                    } else {
                        for (Request r : requests) {
                            user.sendLocalizedMessage("general.request.list.entry", r.getId() + "", new SimpleDateFormat("yyyy-MM-dd HH:mm").format(r.getCreatedTime()), r.getSender().getName());
                        }
                    }
                }
                
                if (queue.canReceive(user)) {
                    List<Request> requests = queue.getRequestsByReceiver(user, false, true);
                    user.sendLocalizedMessage("general.request.list.assignedToYou");
                    
                    if (requests.size() == 0) {
                        user.sendLocalizedMessage("general.request.list.empty");
                    } else {
                        for (Request r : requests) {
                            user.sendLocalizedMessage("general.request.list.entry", r.getId() + "", new SimpleDateFormat("yyyy-MM-dd HH:mm").format(r.getCreatedTime()), r.getSender().getName());
                        }
                    }
                }
                
                if (queue.canReceive(user) || user.hasPermission(RequestManager.viewAllPermission)) {
                    List<Request> requests = queue.getUnassignedRequests(false, true);
                    user.sendLocalizedMessage("general.request.list.unassigned");
                    
                    if (requests.size() == 0) {
                        user.sendLocalizedMessage("general.request.list.empty");
                    } else {
                        for (Request r : requests) {
                            user.sendLocalizedMessage("general.request.list.entry", r.getId() + "", new SimpleDateFormat("yyyy-MM-dd HH:mm").format(r.getCreatedTime()), r.getSender().getName());
                        }
                    }
                }
                
                if (user.hasPermission(RequestManager.viewAllPermission)) {
                    List<Request> requests = queue.getOtherRequests(user, false, true);
                    user.sendLocalizedMessage("general.request.list.assignedToOther");
                    
                    if (requests.size() == 0) {
                        user.sendLocalizedMessage("general.request.list.empty");
                    } else {
                        for (Request r : requests) {
                            user.sendLocalizedMessage("general.request.list.entry", r.getId() + "", new SimpleDateFormat("yyyy-MM-dd HH:mm").format(r.getCreatedTime()), r.getSender().getName());
                        }
                    }
                }
            } else if (args[1].equalsIgnoreCase("send") || args[1].equalsIgnoreCase("add")) {
                if (!queue.canSend(user)) {
                    GoldenApple.logPermissionFail(user, commandLabel, args, true);
                } else if (queue.getMaxRequestsPerSender() != 0 && queue.getRequestsBySender(user, false, true).size() >= queue.getMaxRequestsPerSender()) {
                    user.sendLocalizedMessage("error.request.maxOpen");
                } else {
                    if (args.length > 2) {
                        String message = args[2];
                        for (int i = 3; i < args.length; i++) message += " " + args[i];
                        
                        Request r = queue.createRequest(user, message);
                        user.sendLocalizedMessage("general.request.sent", r.getId() + "", queue.getName());
                    } else {
                        sendHelp(user, commandLabel);
                    }
                }
            } else if (args[1].equalsIgnoreCase("notify")) {
                if (!queue.canReceive(user)) {
                    GoldenApple.logPermissionFail(user, commandLabel, args, true);
                } else if (queue.isReceiving(user)) {
                    queue.setReceiving(user, false);
                    queue.removeFromOnlineReceivers(user);
                    
                    if (queue.isAutoAssign(user)) {
                        queue.setAutoAssign(user, false);
                        queue.removeFromAutoAssignQueue(user);
                    }
                    
                    user.sendLocalizedMessage("general.request.notifyOff", queue.getName());
                } else {
                    queue.setReceiving(user, true);
                    queue.addToOnlineReceivers(user);
                    
                    user.sendLocalizedMessage("general.request.notifyOn", queue.getName());
                }
            } else if (args[1].equalsIgnoreCase("autoassign")) {
                if (!queue.canReceive(user)) {
                    GoldenApple.logPermissionFail(user, commandLabel, args, true);
                } else if (queue.isAutoAssign(user)) {
                    queue.setAutoAssign(user, false);
                    queue.removeFromAutoAssignQueue(user);
                    
                    user.sendLocalizedMessage("general.request.autoAssignOff", queue.getName());
                } else {
                    queue.setAutoAssign(user, true);
                    queue.addToAutoAssignQueue(user);
                    
                    if (!queue.isReceiving(user)) {
                        queue.setReceiving(user, true);
                        queue.addToOnlineReceivers(user);
                    }
                    
                    RequestManager.getInstance().notifyAutoAssignUserEvent(user, AutoAssignUserEvent.ENABLE);
                    
                    user.sendLocalizedMessage("general.request.autoAssignOn", queue.getName());
                }
            } else {
                Request r;
                
                try {
                    r = queue.getRequest(Long.parseLong(args[1]));
                } catch (NumberFormatException e) {
                    user.sendLocalizedMessage("shared.unknownOption", args[1]);
                    return true;
                }
                
                if (r == null) {
                    user.sendLocalizedMessage("error.request.requestNotFound", args[1]);
                    return true;
                } else if (!r.canView(user)) {
                    GoldenApple.logPermissionFail(user, commandLabel, args, true);
                    return true;
                }
                
                if (args.length == 2 || args[2].equalsIgnoreCase("info")) {
                    String status = "???";
                    String receiver = (r.getAssignedReceiver() == null) ? GoldenApple.getInstance().getLocalizationManager().getMessage(user, "general.request.nobody") : r.getAssignedReceiver().getName();
                    
                    switch (r.getStatus()) {
                        case OPEN:
                            status = GoldenApple.getInstance().getLocalizationManager().getMessage(user, "general.request.status.open");
                            break;
                        case ON_HOLD:
                            status = GoldenApple.getInstance().getLocalizationManager().getMessage(user, "general.request.status.hold");
                            break;
                        case CLOSED:
                            status = GoldenApple.getInstance().getLocalizationManager().getMessage(user, "general.request.status.closed").replace("%1", new SimpleDateFormat("yyyy-MM-dd HH:mm").format(r.getClosedTime()));
                            break;
                    }
                    
                    user.sendLocalizedMultilineMessage("general.request.info", r.getId() + "", r.getSender().getName(), receiver, r.getMessage(), new SimpleDateFormat("yyyy-MM-dd HH:mm").format(r.getCreatedTime()), status);
                } else if (args[2].equalsIgnoreCase("own")) {
                    if (!queue.canReceive(user)) {
                        GoldenApple.logPermissionFail(user, commandLabel, args, true);
                    } else if (r.getAssignedReceiver() != null && !user.hasPermission(RequestManager.reassignPermission)) {
                        user.sendLocalizedMessage("error.request.alreadyAssigned");
                    } else if (RequestManager.getInstance().getNumAssignedRequests(user) >= RequestManager.getInstance().getMaxAssignedRequests()) {
                        user.sendLocalizedMessage("error.request.maxAssignedSelf");
                    } else {
                        r.assignTo(user);
                        user.sendLocalizedMessage("general.request.assigned", r.getId() + "", user.getName());
                    }
                } else if (args[2].equalsIgnoreCase("disown")) {
                    if (r.getAssignedReceiver() == null) {
                        user.sendLocalizedMessage("error.request.notAssigned");
                    } else if (r.getAssignedReceiver().getId() != user.getId() && !user.hasPermission(RequestManager.reassignPermission)) {
                        user.sendLocalizedMessage("error.request.notAssignedToYou");
                    } else {
                        r.assignTo(null);
                        user.sendLocalizedMessage("general.request.assigned", r.getId() + "", GoldenApple.getInstance().getLocalizationManager().getMessage(user, "general.request.nobody"));
                    }
                } else if (args[2].equalsIgnoreCase("assign")) {
                    if (args.length != 4) {
                        user.sendLocalizedMessage("shared.parameterMissing", "assign");
                    } else if (!user.hasPermission(RequestManager.reassignPermission)) {
                        GoldenApple.logPermissionFail(user, commandLabel, args, true);
                    } else {
                        IPermissionUser assignTo = PermissionManager.getInstance().findUser(args[3], false);
                        
                        if (!queue.canReceive(assignTo)) {
                            user.sendLocalizedMessage("error.request.cannotReceive", assignTo.getName());
                        } else if (r.getStatus() == RequestStatus.OPEN && RequestManager.getInstance().getNumAssignedRequests(assignTo) >= RequestManager.getInstance().getMaxAssignedRequests()) {
                            user.sendLocalizedMessage("error.request.maxAssignedOther", assignTo.getName());
                        } else {
                            r.assignTo(assignTo);
                            user.sendLocalizedMessage("general.request.assigned", r.getId() + "", assignTo.getName());
                        }
                    }
                } else if (args[2].equalsIgnoreCase("close")) {
                    if (r.getStatus() == RequestStatus.CLOSED) {
                        user.sendLocalizedMessage("error.request.alreadyClosed");
                    } else if (r.getSender().getId() != user.getId() && (r.getAssignedReceiver() == null || r.getAssignedReceiver().getId() != user.getId())) {
                        user.sendLocalizedMessage("error.request.notAssignedToYou");
                    } else {
                        r.setStatus(RequestStatus.CLOSED);
                        user.sendLocalizedMessage("general.request.closed", r.getId() + "");
                    }
                } else if (args[2].equalsIgnoreCase("hold")) {
                    if (r.getStatus() == RequestStatus.ON_HOLD) {
                        user.sendLocalizedMessage("error.request.alreadyOnHold");
                    } else if (r.getAssignedReceiver() == null || r.getAssignedReceiver().getId() != user.getId()) {
                        user.sendLocalizedMessage("error.request.notAssignedToYou");
                    } else {
                        r.setStatus(RequestStatus.ON_HOLD);
                        user.sendLocalizedMessage("general.request.onHold", r.getId() + "");
                    }
                } else if (args[2].equalsIgnoreCase("open")) {
                    if (r.getStatus() == RequestStatus.OPEN) {
                        user.sendLocalizedMessage("error.request.alreadyOpen");
                    } else if (r.getAssignedReceiver() == null || r.getAssignedReceiver().getId() != user.getId()) {
                        user.sendLocalizedMessage("error.request.notAssignedToYou");
                    } else if (RequestManager.getInstance().getNumAssignedRequests(user) >= RequestManager.getInstance().getMaxAssignedRequests()) {
                        user.sendLocalizedMessage("error.request.maxAssignedSelf");
                    } else {
                        r.setStatus(RequestStatus.OPEN);
                        user.sendLocalizedMessage("general.request.opened", r.getId() + "");
                    }
                } else {
                    user.sendLocalizedMessage("shared.unknownOption", args[2]);
                }
            }
        }
        
        return true;
    }
    
    private void sendHelp(User user, String commandLabel) {
        user.sendLocalizedMessage("header.help");
        user.sendLocalizedMultilineMessage("help.request", commandLabel);
    }

}
