package com.bendude56.goldenapple.request;

import java.util.ArrayDeque;
import java.util.ArrayList;

import org.bukkit.Bukkit;
import org.bukkit.scheduler.BukkitTask;

import com.bendude56.goldenapple.GoldenApple;
import com.bendude56.goldenapple.User;
import com.bendude56.goldenapple.request.RequestManager.AutoAssignRequestEvent;
import com.bendude56.goldenapple.request.RequestManager.AutoAssignUserEvent;

public class AutoAssignManager {
    private boolean autoAssignEnabled;
    private boolean autoAssignOnLogin;
    private boolean autoAssignOnClose;
    private boolean autoAssignOnHold;
    private boolean autoAssignConfirm;
    private int autoAssignTimeout;
    private int autoAssignLimit;
    
    private ArrayList<Request> requestBacklog = new ArrayList<Request>();
    private ArrayList<Assigner> assigners = new ArrayList<Assigner>();
    
    public AutoAssignManager() {
        autoAssignEnabled = GoldenApple.getInstanceMainConfig().getBoolean("modules.request.autoAssignEnabled", true);
        autoAssignOnLogin = GoldenApple.getInstanceMainConfig().getBoolean("modules.request.autoAssignOnLogin", true);
        autoAssignOnClose = GoldenApple.getInstanceMainConfig().getBoolean("modules.request.autoAssignOnClose", true);
        autoAssignOnHold = GoldenApple.getInstanceMainConfig().getBoolean("modules.request.autoAssignOnHold", true);
        autoAssignConfirm = GoldenApple.getInstanceMainConfig().getBoolean("modules.request.autoAssignConfirm", true);
        autoAssignTimeout = GoldenApple.getInstanceMainConfig().getInt("modules.request.autoAssignTimeout", 60);
        autoAssignLimit = GoldenApple.getInstanceMainConfig().getInt("modules.request.autoAssignLimit", 1);
        
        for (RequestQueue queue : RequestManager.getInstance().getAllRequestQueues()) {
            for (Request r : queue.getUnassignedRequests(false, false)) {
                notifyRequestEvent(r, AutoAssignRequestEvent.CREATE);
            }
        }
    }
    
    public void notifyUserEvent(User user, AutoAssignUserEvent event) {
        if (event == AutoAssignUserEvent.LOGIN && autoAssignOnLogin)
            notifyUserFree(user);
        else if (event == AutoAssignUserEvent.CLOSE && autoAssignOnClose)
            notifyUserFree(user);
        else if (event == AutoAssignUserEvent.HOLD && autoAssignOnHold)
            notifyUserFree(user);
        else if (event == AutoAssignUserEvent.ENABLE)
            notifyUserFree(user);
        else if (event == AutoAssignUserEvent.LOGOUT)
            notifyUserLeave(user);
    }
    
    public void notifyRequestEvent(Request request, AutoAssignRequestEvent event) {
        if (event == AutoAssignRequestEvent.ASSIGN)
            notifyRequestTaken(request);
        else if (event == AutoAssignRequestEvent.CLOSE)
            notifyRequestClosed(request);
        else if (event == AutoAssignRequestEvent.CREATE)
            notifyRequestOpen(request);
    }
    
    public void notifyShutdown() {
        for (Assigner a : assigners) {
            a.notifyShutdown();
        }
    }
    
    private void notifyUserFree(User user) {
        if (!autoAssignEnabled && canAutoAssign(user)) return;
        ArrayList<Request> toAssign = new ArrayList<Request>();
        int maxToAssign = autoAssignLimit - RequestManager.getInstance().getNumAssignedRequests(user);
        
        for (Request r : requestBacklog) {
            if (r.getQueue().canReceive(user) && r.getQueue().isAutoAssign(user) && !r.isOnDoNotAssign(user)) {
                toAssign.add(r);
                
                if (toAssign.size() >= maxToAssign) break;
            }
        }
        
        for (Request assigned : toAssign) {
            requestBacklog.remove(assigned);
            
            if (autoAssignConfirm) {
                Assigner assigner = new Assigner(assigned, user);
                assigners.add(assigner);
                assigner.findNextReceiver();
            } else {
                assigned.assignTo(user);
                user.sendLocalizedMessage("module.request.autoAssign.assigned", assigned.getQueue().getName(), assigned.getId() );
            }
        }
    }
    
    private void notifyUserLeave(User user) {
        if (!autoAssignEnabled) return;
        
        ArrayList<Assigner> assigner = new ArrayList<Assigner>();
        
        for (Assigner a : assigners) {
            if (a.user.getId() == user.getId()) {
                assigner.add(a);
            }
        }
        
        for (Assigner a : assigner) {
            a.notifyLogout();
        }
    }
    
    private void notifyRequestOpen(Request r) {
        if (!autoAssignEnabled) return;
        
        if (autoAssignConfirm) {
            Assigner assigner = new Assigner(r);
            assigners.add(assigner);
            assigner.findNextReceiver();
        } else {
            User assignTo = null;
            
            for (User receiver : r.getQueue().getAutoAssignQueue()) {
                if (canAutoAssign(receiver) && !r.isOnDoNotAssign(receiver)) {
                    assignTo = receiver;
                    break;
                }
            }
            
            if (assignTo != null) {
                r.assignTo(assignTo);
                assignTo.sendLocalizedMessage("module.request.autoAssign.assigned", r.getQueue().getName(), r.getId() );
            } else {
                requestBacklog.add(r);
            }
        }
    }
    
    private void notifyRequestTaken(Request r) {
        if (!autoAssignEnabled) return;
        
        if (r.getAssignedReceiver() == null) {
            notifyRequestOpen(r);
        } else {
            if (autoAssignConfirm) {
                Assigner assigner = null;
                
                for (Assigner a : assigners) {
                    if (a.request == r) {
                        assigner = a;
                        break;
                    }
                }
                
                if (assigner != null) {
                    assigner.notifyTaken();
                }
            }
            
            requestBacklog.remove(r);
        }
    }
    
    private void notifyRequestClosed(Request r) {
        if (!autoAssignEnabled || !autoAssignConfirm) return;
        Assigner assigner = null;
        
        for (Assigner a : assigners) {
            if (a.request == r) {
                assigner = a;
                break;
            }
        }
        
        if (assigner != null) {
            assigner.notifyClosed();
        }
        
        requestBacklog.remove(r);
    }
    
    public boolean canAutoAssign(User u) {
        return RequestManager.getInstance().getNumAssignedRequests(u) < autoAssignLimit;
    }
    
    private class Assigner {
        public Request request;
        
        public User user;
        public ArrayDeque<User> users;
        public BukkitTask waitTask;
        
        public Assigner(Request request) {
            this.request = request;
            this.users = new ArrayDeque<User>(request.getQueue().getAutoAssignQueue());
        }
        
        public Assigner(Request request, User user) {
            this.request = request;
            this.users = new ArrayDeque<User>();
            this.users.add(user);
        }
        
        public void findNextReceiver() {
            if (users.size() > 0) {
                user = users.remove();
                
                if (canAutoAssign(user) && !request.isOnDoNotAssign(user)) {
                    user.sendLocalizedMessage("module.request.autoAssign.request.begin", request.getQueue().getName(), request.getId() );
                    waitTask = Bukkit.getScheduler().runTaskLater(GoldenApple.getInstance(), new Runnable() {
                        @Override
                        public void run() {
                            notifyTimeout();
                        }
                    }, 20 * autoAssignTimeout);
                } else {
                    findNextReceiver();
                }
            } else {
                requestBacklog.add(request);
                assigners.remove(this);
            }
        }
        
        public void notifyTaken() {
            waitTask.cancel();
            
            if (request.getAssignedReceiver().getId() != user.getId()) {
                user.sendLocalizedMessage("module.request.autoAssign.request.end", request.getQueue().getName(), request.getId() );
            } else {
                request.getQueue().removeFromAutoAssignQueue(user);
                request.getQueue().addToAutoAssignQueue(user);
            }
        }
        
        public void notifyLogout() {
            findNextReceiver();
        }
        
        public void notifyShutdown() {
            waitTask.cancel();
        }
        
        public void notifyClosed() {
            waitTask.cancel();
            assigners.remove(this);
        }
        
        private void notifyTimeout() {
            user.sendLocalizedMessage("module.request.autoAssign.request.end", request.getQueue().getName(), request.getId() );
            findNextReceiver();
        }
    }
}
