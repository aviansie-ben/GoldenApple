package com.bendude56.goldenapple.request;

import java.util.Date;

import com.bendude56.goldenapple.permissions.IPermissionUser;

public interface Request {
    public long getId();
    public RequestQueue getQueue();
    
    public IPermissionUser getSender();
    public IPermissionUser getAssignedReceiver();
    
    public void assignTo(IPermissionUser receiver);
    
    public String getMessage();
    
    public RequestStatus getStatus();
    public Date getCreatedTime();
    public Date getClosedTime();
    
    public void setStatus(RequestStatus status);
    
    public boolean canView(IPermissionUser user);
    public boolean isOnDoNotAssign(IPermissionUser user);
    
    public enum RequestStatus {
        OPEN, ON_HOLD, CLOSED
    }
}
