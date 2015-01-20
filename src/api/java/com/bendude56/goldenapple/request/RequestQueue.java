package com.bendude56.goldenapple.request;

import java.util.Deque;
import java.util.List;

import com.bendude56.goldenapple.User;
import com.bendude56.goldenapple.permissions.IPermissionGroup;
import com.bendude56.goldenapple.permissions.IPermissionUser;

public interface RequestQueue {
    public long getId();
    public String getName();
    
    public IPermissionGroup getSendingGroup();
    public IPermissionGroup getReceivingGroup();
    
    public void setSendingGroup(IPermissionGroup group);
    public void setReceivingGroup(IPermissionGroup group);
    
    public int getMaxRequestsPerSender();
    public boolean getAllowNoReceiver();
    
    public void setMaxRequestsPerSender(int maxRequestsPerSender);
    public void setAllowNoReceiver(boolean allowNoReceiver);
    
    public List<User> getOnlineReceivers();
    public Deque<User> getAutoAssignQueue();
    
    public void addToAutoAssignQueue(User receiver);
    public void removeFromAutoAssignQueue(User receiver);
    
    public void addToOnlineReceivers(User receiver);
    public void removeFromOnlineReceivers(User receiver);
    
    public Request getRequest(long id);
    public List<Request> getAllRequests(boolean includeClosed, boolean includeOnHold);
    public List<Request> getRequestsBySender(IPermissionUser sender, boolean includeClosed, boolean includeOnHold);
    public List<Request> getRequestsByReceiver(IPermissionUser receiver, boolean includeClosed, boolean includeOnHold);
    public List<Request> getOtherRequests(IPermissionUser receiver, boolean includeClosed, boolean includeOnHold);
    public List<Request> getUnassignedRequests(boolean includeClosed, boolean includeOnHold);
    
    public Request createRequest(User sender, String message);
    public int deleteOldRequests(int maxClosedDays);
    
    public boolean isAutoAssign(User receiver);
    public boolean isReceiving(User receiver);
    
    public boolean canReceive(IPermissionUser user);
    public boolean canSend(IPermissionUser user);
    
    public void setAutoAssign(User receiver, boolean autoAssign);
    public void setReceiving(User receiver, boolean receive);
}
