package com.bendude56.goldenapple.request;

import java.util.List;

import com.bendude56.goldenapple.User;
import com.bendude56.goldenapple.permissions.IPermissionUser;
import com.bendude56.goldenapple.permissions.PermissionManager.Permission;
import com.bendude56.goldenapple.permissions.PermissionManager.PermissionNode;

public abstract class RequestManager {
    // goldenapple.request
    public static PermissionNode requestNode;
    public static Permission statsPermission;
    public static Permission viewAllPermission;
    public static Permission reassignPermission;
    public static Permission editQueuePermission;
    
    protected static RequestManager instance;
    
    public static RequestManager getInstance() {
        return instance;
    }
    
    public abstract RequestQueue getRequestQueueById(long id);
    public abstract RequestQueue getRequestQueueByName(String name);
    public abstract List<RequestQueue> getAllRequestQueues();
    
    public abstract Request getRequestById(long id);
    
    public abstract int getNumAssignedRequests(IPermissionUser receiver);
    public abstract int getMaxAssignedRequests();
    
    public abstract void deleteQueue(long id);
    public abstract RequestQueue createQueue(String name);
    
    public abstract void deleteOldRequests();
    
    public abstract void notifyAutoAssignUserEvent(User user, AutoAssignUserEvent event);
    public abstract void notifyAutoAssignRequestEvent(Request request, AutoAssignRequestEvent event);
    
    public static enum AutoAssignUserEvent {
        LOGIN, LOGOUT, CLOSE, HOLD, ENABLE
    }
    
    public static enum AutoAssignRequestEvent {
        CREATE, ASSIGN, CLOSE
    }
}
