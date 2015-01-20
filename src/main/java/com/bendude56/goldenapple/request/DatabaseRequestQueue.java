package com.bendude56.goldenapple.request;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.Deque;
import java.util.HashMap;
import java.util.List;
import java.util.logging.Level;

import com.bendude56.goldenapple.GoldenApple;
import com.bendude56.goldenapple.User;
import com.bendude56.goldenapple.permissions.IPermissionGroup;
import com.bendude56.goldenapple.permissions.IPermissionUser;
import com.bendude56.goldenapple.permissions.PermissionManager;
import com.bendude56.goldenapple.request.Request.RequestStatus;
import com.bendude56.goldenapple.request.RequestManager.AutoAssignRequestEvent;

public class DatabaseRequestQueue implements RequestQueue {
    private long id;
    private String name;
    
    private Long sendGroup;
    private Long receiveGroup;
    
    private int maxRequestsPerSender;
    private boolean allowNoReceiver;
    
    private HashMap<Long, Request> requests = new HashMap<Long, Request>();
    
    private HashMap<Long, Boolean> receiversReceiving = new HashMap<Long, Boolean>();
    private HashMap<Long, Boolean> receiversAutoAssign = new HashMap<Long, Boolean>();
    
    private ArrayList<User> onlineReceivers = new ArrayList<User>();
    private ArrayDeque<User> autoAssignQueue = new ArrayDeque<User>();
    
    private ArrayList<Request> requestQueue = new ArrayList<Request>();
    private ArrayList<Request> requestRemoveOrder = new ArrayList<Request>();
    
    public DatabaseRequestQueue(ResultSet r) throws SQLException {
        id = r.getLong("ID");
        name = r.getString("Name");
        
        sendGroup = r.getLong("SendGroup");
        if (r.wasNull()) {
            sendGroup = null;
        }
        
        receiveGroup = r.getLong("ReceiveGroup");
        if (r.wasNull()) {
            receiveGroup = null;
        }
        
        maxRequestsPerSender = r.getInt("MaxRequestsPerSender");
        allowNoReceiver = r.getBoolean("AllowNoReceiver");
        
        loadRequests();
        loadReceivers();
    }
    
    public DatabaseRequestQueue(long id, String name) {
        this.id = id;
        this.name = name;
        
        this.sendGroup = null;
        this.receiveGroup = null;
        
        this.maxRequestsPerSender = 1;
        this.allowNoReceiver = true;
    }
    
    private void loadRequests() throws SQLException {
        SimpleRequestManager manager = (SimpleRequestManager) RequestManager.getInstance();
        ResultSet r = GoldenApple.getInstanceDatabaseManager().executeQuery("SELECT * FROM Requests WHERE QueueID=?", id);
        
        try {
            while (r.next()) {
                Request request = new DatabaseRequest(r);
                
                requests.put(request.getId(), request);
                
                if (request.getStatus() == RequestStatus.CLOSED) {
                    requestRemoveOrder.add(request);
                } else {
                    requestQueue.add(request);
                }
                
                manager.requestQueues.put(request.getId(), id);
            }
        } finally {
            GoldenApple.getInstanceDatabaseManager().closeResult(r);
        }
        
        Collections.sort(requestRemoveOrder, new RequestCloseDateComparator());
        Collections.sort(requestQueue, new RequestCreateDateComparator());
    }
    
    private void loadReceivers() throws SQLException {
        ResultSet r = GoldenApple.getInstanceDatabaseManager().executeQuery("SELECT * FROM RequestQueueReceivers WHERE QueueID=?", id);
        
        try {
            while (r.next()) {
                long user = r.getLong("UserID");
                
                receiversReceiving.put(user, r.getBoolean("Receiving"));
                receiversAutoAssign.put(user, r.getBoolean("AutoAssign"));
            }
        } finally {
            GoldenApple.getInstanceDatabaseManager().closeResult(r);
        }
    }
    
    @Override
    public long getId() {
        return id;
    }
    
    @Override
    public String getName() {
        return name;
    }
    
    @Override
    public IPermissionGroup getSendingGroup() {
        return (sendGroup == null) ? null : PermissionManager.getInstance().getGroup(sendGroup);
    }
    
    @Override
    public IPermissionGroup getReceivingGroup() {
        return (receiveGroup == null) ? null : PermissionManager.getInstance().getGroup(receiveGroup);
    }
    
    @Override
    public void setSendingGroup(IPermissionGroup group) {
        if (group == null) {
            sendGroup = null;
        } else {
            sendGroup = group.getId();
        }
        
        try {
            GoldenApple.getInstanceDatabaseManager().execute("UPDATE RequestQueues SET SendGroup=? WHERE ID=?", sendGroup, id);
        } catch (SQLException e) {
            throw new RuntimeException("Failed to save change to the database", e);
        }
    }
    
    @Override
    public void setReceivingGroup(IPermissionGroup group) {
        if (group == null) {
            receiveGroup = null;
        } else {
            receiveGroup = group.getId();
        }
        
        try {
            for (long userId : new ArrayList<Long>(receiversReceiving.keySet())) {
                if (group == null || !group.isMember(PermissionManager.getInstance().getUser(userId), false)) {
                    receiversReceiving.remove(userId);
                    receiversAutoAssign.remove(userId);
                    
                    GoldenApple.getInstanceDatabaseManager().execute("DELETE FROM RequestQueueReceivers WHERE QueueID=? AND UserID=?", id, userId);
                    
                    User user;
                    if ((user = User.getUser(userId)) != null) {
                        onlineReceivers.remove(user);
                        autoAssignQueue.remove(user);
                    }
                }
            }
            
            GoldenApple.getInstanceDatabaseManager().execute("UPDATE RequestQueues SET ReceiveGroup=? WHERE ID=?", receiveGroup, id);
        } catch (SQLException e) {
            throw new RuntimeException("Failed to save change to the database", e);
        }
    }
    
    @Override
    public int getMaxRequestsPerSender() {
        return maxRequestsPerSender;
    }
    
    @Override
    public boolean getAllowNoReceiver() {
        return allowNoReceiver;
    }
    
    @Override
    public void setMaxRequestsPerSender(int maxRequestsPerSender) {
        this.maxRequestsPerSender = maxRequestsPerSender;
        
        try {
            GoldenApple.getInstanceDatabaseManager().execute("UPDATE RequestQueues SET MaxRequestsPerSender=? WHERE ID=?", maxRequestsPerSender, id);
        } catch (SQLException e) {
            throw new RuntimeException("Failed to save change to the database", e);
        }
    }
    
    @Override
    public void setAllowNoReceiver(boolean allowNoReceiver) {
        this.allowNoReceiver = allowNoReceiver;
        
        try {
            GoldenApple.getInstanceDatabaseManager().execute("UPDATE RequestQueues SET AllowNoReceiver=? WHERE ID=?", allowNoReceiver, id);
        } catch (SQLException e) {
            throw new RuntimeException("Failed to save change to the database", e);
        }
    }
    
    @Override
    public List<User> getOnlineReceivers() {
        return onlineReceivers;
    }
    
    @Override
    public Deque<User> getAutoAssignQueue() {
        return autoAssignQueue;
    }
    
    @Override
    public void addToAutoAssignQueue(User receiver) {
        if (!autoAssignQueue.contains(receiver)) {
            autoAssignQueue.add(receiver);
        }
    }
    
    @Override
    public void removeFromAutoAssignQueue(User receiver) {
        if (autoAssignQueue.contains(receiver)) {
            autoAssignQueue.remove(receiver);
        }
    }
    
    @Override
    public void addToOnlineReceivers(User receiver) {
        if (!onlineReceivers.contains(receiver)) {
            onlineReceivers.add(receiver);
        }
    }
    
    @Override
    public void removeFromOnlineReceivers(User receiver) {
        if (onlineReceivers.contains(receiver)) {
            onlineReceivers.remove(receiver);
        }
    }
    
    @Override
    public Request getRequest(long id) {
        return requests.get(id);
    }
    
    @Override
    public List<Request> getAllRequests(boolean includeClosed, boolean includeOnHold) {
        // TODO Filter out requests that are on hold
        if (includeClosed) {
            ArrayList<Request> requests = new ArrayList<Request>(this.requests.values());
            Collections.sort(requests, new RequestCreateDateComparator());
            
            return Collections.unmodifiableList(requests);
        } else {
            return Collections.unmodifiableList(requestQueue);
        }
    }
    
    @Override
    public List<Request> getRequestsBySender(IPermissionUser sender, boolean includeClosed, boolean includeOnHold) {
        if (includeClosed) {
            List<Request> requests = new ArrayList<Request>();
            
            for (Request r : this.requests.values()) {
                if (r.getSender().getId() == sender.getId() && (r.getStatus() == RequestStatus.OPEN || includeOnHold)) {
                    requests.add(r);
                }
            }
            
            Collections.sort(requests, new RequestCreateDateComparator());
            return Collections.unmodifiableList(requests);
        } else {
            List<Request> requests = new ArrayList<Request>();
            
            for (Request r : requestQueue) {
                if (r.getSender().getId() == sender.getId() && (r.getStatus() == RequestStatus.OPEN || includeOnHold)) {
                    requests.add(r);
                }
            }
            
            return Collections.unmodifiableList(requests);
        }
    }
    
    @Override
    public List<Request> getRequestsByReceiver(IPermissionUser receiver, boolean includeClosed, boolean includeOnHold) {
        if (receiver == null) {
            return getUnassignedRequests(includeClosed, includeOnHold);
        } else if (includeClosed) {
            List<Request> requests = new ArrayList<Request>();
            
            for (Request r : this.requests.values()) {
                if (r.getAssignedReceiver() != null && r.getAssignedReceiver().getId() == receiver.getId() && (r.getStatus() == RequestStatus.OPEN || includeOnHold)) {
                    requests.add(r);
                }
            }
            
            Collections.sort(requests, new RequestCreateDateComparator());
            return Collections.unmodifiableList(requests);
        } else {
            List<Request> requests = new ArrayList<Request>();
            
            for (Request r : requestQueue) {
                if (r.getAssignedReceiver() != null && r.getAssignedReceiver().getId() == receiver.getId() && (r.getStatus() == RequestStatus.OPEN || includeOnHold)) {
                    requests.add(r);
                }
            }
            
            return Collections.unmodifiableList(requests);
        }
    }
    
    @Override
    public List<Request> getUnassignedRequests(boolean includeClosed, boolean includeOnHold) {
        if (includeClosed) {
            List<Request> requests = new ArrayList<Request>();
            
            for (Request r : this.requests.values()) {
                if (r.getAssignedReceiver() == null && (r.getStatus() == RequestStatus.OPEN || includeOnHold)) {
                    requests.add(r);
                }
            }
            
            Collections.sort(requests, new RequestCreateDateComparator());
            return Collections.unmodifiableList(requests);
        } else {
            List<Request> requests = new ArrayList<Request>();
            
            for (Request r : requestQueue) {
                if (r.getAssignedReceiver() == null && (r.getStatus() == RequestStatus.OPEN || includeOnHold)) {
                    requests.add(r);
                }
            }
            
            return Collections.unmodifiableList(requests);
        }
    }
    
    @Override
    public List<Request> getOtherRequests(IPermissionUser receiver, boolean includeClosed, boolean includeOnHold) {
        if (includeClosed) {
            List<Request> requests = new ArrayList<Request>();
            
            for (Request r : this.requests.values()) {
                if (r.getAssignedReceiver() != null && r.getAssignedReceiver().getId() != receiver.getId() && (r.getStatus() == RequestStatus.OPEN || includeOnHold)) {
                    requests.add(r);
                }
            }
            
            Collections.sort(requests, new RequestCreateDateComparator());
            return Collections.unmodifiableList(requests);
        } else {
            List<Request> requests = new ArrayList<Request>();
            
            for (Request r : requestQueue) {
                if (r.getAssignedReceiver() != null && r.getAssignedReceiver().getId() != receiver.getId() && (r.getStatus() == RequestStatus.OPEN || includeOnHold)) {
                    requests.add(r);
                }
            }
            
            return Collections.unmodifiableList(requests);
        }
    }
    
    @Override
    public boolean canReceive(IPermissionUser user) {
        IPermissionGroup group = getReceivingGroup();
        return group != null && group.isMember(user, false);
    }
    
    @Override
    public boolean canSend(IPermissionUser user) {
        IPermissionGroup group = getSendingGroup();
        return group == null || group.isMember(user, false);
    }
    
    @Override
    public Request createRequest(User sender, String message) {
        SimpleRequestManager manager = (SimpleRequestManager) RequestManager.getInstance();
        long requestId;
        
        try {
            ResultSet r = GoldenApple.getInstanceDatabaseManager().executeReturnGenKeys("INSERT INTO Requests (QueueID, Sender, Message) VALUES (?, ?, ?)", id, sender.getId(), message);
            
            try {
                r.next();
                requestId = r.getLong(1);
                
                Request request = new DatabaseRequest(requestId, id, sender.getId(), message, new Timestamp(System.currentTimeMillis()));
                
                requests.put(requestId, request);
                manager.requestQueues.put(requestId, id);
                requestQueue.add(request);
                
                for (User receiver : onlineReceivers) {
                    receiver.sendLocalizedMessage("module.request.notify.newRequest", name, requestId);
                }
                
                RequestManager.getInstance().notifyAutoAssignRequestEvent(request, AutoAssignRequestEvent.CREATE);
                
                return request;
            } finally {
                GoldenApple.getInstanceDatabaseManager().closeResult(r);
            }
        } catch (SQLException e) {
            throw new RuntimeException("Failed to create request in database", e);
        }
    }
    
    @Override
    public int deleteOldRequests(int maxClosedDays) {
        int count = 0;
        
        try {
            SimpleRequestManager manager = (SimpleRequestManager) RequestManager.getInstance();
            Date oldestToKeep = new Date(System.currentTimeMillis() - (maxClosedDays * 1000 * 60 * 60 * 24));
            
            while (requestRemoveOrder.size() > 0 && requestRemoveOrder.get(0).getClosedTime().before(oldestToKeep)) {
                Request r = requestRemoveOrder.remove(0);
                
                GoldenApple.getInstanceDatabaseManager().execute("DELETE FROM Requests WHERE ID=?", r.getId());
                
                requests.remove(r.getId());
                manager.requestQueues.remove(r.getId());
                
                count++;
            }
        } catch (SQLException e) {
            GoldenApple.log(Level.WARNING, "Failed to delete old requests from queue '" + name + "':");
            GoldenApple.log(Level.WARNING, e);
        }
        
        return count;
    }
    
    @Override
    public boolean isAutoAssign(User receiver) {
        return (receiversAutoAssign.containsKey(receiver.getId())) ? receiversAutoAssign.get(receiver.getId()) : true;
    }
    
    @Override
    public boolean isReceiving(User receiver) {
        return (receiversReceiving.containsKey(receiver.getId())) ? receiversReceiving.get(receiver.getId()) : true;
    }
    
    @Override
    public void setAutoAssign(User receiver, boolean autoAssign) {
        receiversAutoAssign.put(receiver.getId(), autoAssign);
        if (!receiversReceiving.containsKey(receiver.getId())) {
            receiversReceiving.put(receiver.getId(), true);
        }
        
        try {
            GoldenApple.getInstanceDatabaseManager().execute("DELETE FROM RequestQueueReceivers WHERE QueueID=? AND UserID=?", id, receiver.getId());
            GoldenApple.getInstanceDatabaseManager().execute("INSERT INTO RequestQueueReceivers (QueueID, UserID, Receiving, AutoAssign) VALUES (?, ?, ?, ?)", id, receiver.getId(), autoAssign, isReceiving(receiver));
        } catch (SQLException e) {
            throw new RuntimeException("Failed to save change to the database", e);
        }
    }
    
    @Override
    public void setReceiving(User receiver, boolean receive) {
        receiversReceiving.put(receiver.getId(), receive);
        if (!receiversAutoAssign.containsKey(receiver.getId())) {
            receiversAutoAssign.put(receiver.getId(), true);
        }
        
        try {
            GoldenApple.getInstanceDatabaseManager().execute("DELETE FROM RequestQueueReceivers WHERE QueueID=? AND UserID=?", id, receiver.getId());
            GoldenApple.getInstanceDatabaseManager().execute("INSERT INTO RequestQueueReceivers (QueueID, UserID, Receiving, AutoAssign) VALUES (?, ?, ?, ?)", id, receiver.getId(), isAutoAssign(receiver), receive);
        } catch (SQLException e) {
            throw new RuntimeException("Failed to save change to the database", e);
        }
    }
    
    protected void addRequestToDeleteQueue(Request r) {
        int i = Collections.binarySearch(requestRemoveOrder, r, new RequestCloseDateComparator());
        if (i < 0) {
            i = -(i + 1);
        }
        
        requestRemoveOrder.add(i, r);
    }
    
    protected void removeRequestFromDeleteQueue(Request r) {
        requestRemoveOrder.remove(r);
    }
    
    protected void addRequestToRequestQueue(Request r) {
        int i = Collections.binarySearch(requestQueue, r, new RequestCreateDateComparator());
        if (i < 0) {
            i = -(i + 1);
        }
        
        requestQueue.add(i, r);
    }
    
    protected void removeRequestFromRequestQueue(Request r) {
        requestQueue.remove(r);
    }
    
    public static class RequestCloseDateComparator implements Comparator<Request> {
        @Override
        public int compare(Request o1, Request o2) {
            if (o1.getClosedTime() == null || o2.getClosedTime() == null) {
                throw new UnsupportedOperationException("Cannot sort open request by close date!");
            }
            
            return o1.getClosedTime().compareTo(o2.getClosedTime());
        }
    }
    
    public static class RequestCreateDateComparator implements Comparator<Request> {
        @Override
        public int compare(Request o1, Request o2) {
            return o1.getCreatedTime().compareTo(o2.getCreatedTime());
        }
    }
    
}
