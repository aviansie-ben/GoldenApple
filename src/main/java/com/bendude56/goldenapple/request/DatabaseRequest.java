package com.bendude56.goldenapple.request;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.util.Date;
import java.util.HashSet;

import com.bendude56.goldenapple.GoldenApple;
import com.bendude56.goldenapple.User;
import com.bendude56.goldenapple.permissions.IPermissionUser;
import com.bendude56.goldenapple.permissions.PermissionManager;
import com.bendude56.goldenapple.request.RequestManager.AutoAssignRequestEvent;
import com.bendude56.goldenapple.request.RequestManager.AutoAssignUserEvent;

public class DatabaseRequest implements Request {
    private long id;
    private long queueId;
    
    private long sender;
    private Long assignedReceiver;
    
    private String message;
    
    private Timestamp createdDate;
    private Timestamp closedDate;
    
    private boolean onHold;
    
    private HashSet<Long> doNotAssign = new HashSet<Long>();
    
    public DatabaseRequest(ResultSet r) throws SQLException {
        id = r.getLong("ID");
        queueId = r.getLong("QueueID");
        
        sender = r.getLong("Sender");
        assignedReceiver = r.getLong("AssignedReceiver");
        if (r.wasNull()) {
            assignedReceiver = null;
        }
        
        message = r.getString("Message");
        
        createdDate = r.getTimestamp("Created");
        closedDate = r.getTimestamp("Closed");
        
        onHold = r.getBoolean("OnHold");
    }
    
    public DatabaseRequest(long id, long queueId, long sender, String message, Timestamp created) {
        this.id = id;
        this.queueId = queueId;
        
        this.sender = sender;
        this.assignedReceiver = null;
        
        this.message = message;
        
        this.createdDate = created;
        this.closedDate = null;
        
        this.onHold = false;
    }
    
    @Override
    public long getId() {
        return id;
    }
    
    @Override
    public RequestQueue getQueue() {
        return RequestManager.getInstance().getRequestQueueById(queueId);
    }
    
    @Override
    public IPermissionUser getSender() {
        return PermissionManager.getInstance().getUser(sender);
    }
    
    @Override
    public IPermissionUser getAssignedReceiver() {
        return (assignedReceiver == null) ? null : PermissionManager.getInstance().getUser(assignedReceiver);
    }
    
    @Override
    public void assignTo(IPermissionUser receiver) {
        if (assignedReceiver != null) {
            doNotAssign.add(assignedReceiver);
        }
        
        if (receiver != null) {
            assignedReceiver = receiver.getId();
        } else {
            assignedReceiver = null;
        }
        
        RequestManager.getInstance().notifyAutoAssignRequestEvent(this, AutoAssignRequestEvent.ASSIGN);
        
        try {
            GoldenApple.getInstanceDatabaseManager().execute("UPDATE Requests SET AssignedReceiver=? WHERE ID=?", (receiver == null) ? null : receiver.getId(), id);
        } catch (SQLException e) {
            throw new RuntimeException("Failed to update request database", e);
        }
    }
    
    @Override
    public String getMessage() {
        return message;
    }
    
    @Override
    public RequestStatus getStatus() {
        if (closedDate != null) {
            return RequestStatus.CLOSED;
        } else if (onHold) {
            return RequestStatus.ON_HOLD;
        } else {
            return RequestStatus.OPEN;
        }
    }
    
    @Override
    public Date getCreatedTime() {
        return createdDate;
    }
    
    @Override
    public Date getClosedTime() {
        return closedDate;
    }
    
    @Override
    public void setStatus(RequestStatus status) {
        if (status == getStatus()) {
            return;
        }
        DatabaseRequestQueue queue = (DatabaseRequestQueue) getQueue();
        User receiver = (assignedReceiver == null) ? null : User.getUser(assignedReceiver);
        
        if (status != RequestStatus.CLOSED && closedDate != null) {
            closedDate = null;
            queue.removeRequestFromDeleteQueue(this);
            queue.addRequestToRequestQueue(this);
            
            try {
                GoldenApple.getInstanceDatabaseManager().execute("UPDATE Requests SET Closed=NULL WHERE ID=?", id);
            } catch (SQLException e) {
                throw new RuntimeException("Failed to update request database", e);
            }
        }
        if (status != RequestStatus.ON_HOLD && onHold) {
            onHold = false;
            
            try {
                GoldenApple.getInstanceDatabaseManager().execute("UPDATE Requests SET OnHold=0 WHERE ID=?", id);
            } catch (SQLException e) {
                throw new RuntimeException("Failed to update request database", e);
            }
        }
        
        if (status == RequestStatus.CLOSED) {
            closedDate = new Timestamp(System.currentTimeMillis());
            queue.addRequestToDeleteQueue(this);
            queue.removeRequestFromRequestQueue(this);
            
            if (receiver != null) {
                RequestManager.getInstance().notifyAutoAssignUserEvent(receiver, AutoAssignUserEvent.CLOSE);
            }
            RequestManager.getInstance().notifyAutoAssignRequestEvent(this, AutoAssignRequestEvent.CLOSE);
            
            try {
                GoldenApple.getInstanceDatabaseManager().execute("UPDATE Requests SET Closed=? WHERE ID=?", closedDate, id);
            } catch (SQLException e) {
                throw new RuntimeException("Failed to update request database", e);
            }
        } else if (status == RequestStatus.ON_HOLD) {
            onHold = true;
            
            if (receiver != null) {
                RequestManager.getInstance().notifyAutoAssignUserEvent(receiver, AutoAssignUserEvent.HOLD);
            }
            
            try {
                GoldenApple.getInstanceDatabaseManager().execute("UPDATE Requests SET OnHold=1 WHERE ID=?", id);
            } catch (SQLException e) {
                throw new RuntimeException("Failed to update request database", e);
            }
        }
    }
    
    @Override
    public boolean canView(IPermissionUser user) {
        if (user.getId() == sender) {
            return true;
        } else if (user.hasPermission(RequestManager.viewAllPermission)) {
            return true;
        } else if (getQueue().canReceive(user)) {
            return assignedReceiver == user.getId() || assignedReceiver == null;
        } else {
            return false;
        }
    }
    
    @Override
    public boolean isOnDoNotAssign(IPermissionUser user) {
        return doNotAssign.contains(user.getId());
    }
    
}
