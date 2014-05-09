package com.bendude56.goldenapple.request;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;

import org.bukkit.Bukkit;
import org.bukkit.scheduler.BukkitTask;

import com.bendude56.goldenapple.GoldenApple;
import com.bendude56.goldenapple.User;
import com.bendude56.goldenapple.permissions.IPermissionUser;

public class SimpleRequestManager extends RequestManager {
    protected HashMap<Long, RequestQueue> queues = new HashMap<Long, RequestQueue>();
    protected HashMap<Long, Long> requestQueues = new HashMap<Long, Long>();
    
    private int assignLimit;
    private int requestDeleteDays;
    
    private BukkitTask deleteOldTask;
    private AutoAssignManager assigner;
    
    public SimpleRequestManager() {
        GoldenApple.getInstanceDatabaseManager().createOrUpdateTable("requestqueues");
        GoldenApple.getInstanceDatabaseManager().createOrUpdateTable("requestqueuereceivers");
        GoldenApple.getInstanceDatabaseManager().createOrUpdateTable("requests");
        
        assignLimit = GoldenApple.getInstanceMainConfig().getInt("modules.request.assignLimit", 3);
        requestDeleteDays = GoldenApple.getInstanceMainConfig().getInt("modules.request.requestDeleteDays", 1);
    }
    
    protected void loadQueues() {
        try {
            ResultSet r = GoldenApple.getInstanceDatabaseManager().executeQuery("SELECT * FROM RequestQueues");
            
            try {
                while (r.next()) {
                    RequestQueue queue = new DatabaseRequestQueue(r);
                    
                    queues.put(queue.getId(), queue);
                    
                    for (User u : User.getOnlineUsers()) {
                        if (queue.canReceive(u) && queue.isReceiving(u)) {
                            queue.addToOnlineReceivers(u);
                        }
                        
                        if (queue.canReceive(u) && queue.isAutoAssign(u)) {
                            queue.addToAutoAssignQueue(u);
                        }
                    }
                }
            } finally {
                GoldenApple.getInstanceDatabaseManager().closeResult(r);
            }
            
            deleteOldRequests();
        } catch (SQLException e) {
            throw new RuntimeException("Failed to load request queues from database", e);
        }
    }
    
    protected void scheduleTasks() {
        deleteOldTask = Bukkit.getScheduler().runTaskTimer(GoldenApple.getInstance(), new Runnable() {
            @Override
            public void run() {
                deleteOldRequests();
            }
        }, 20 * 60, 20 *60);
        
        assigner = new AutoAssignManager();
    }

    @Override
    public RequestQueue getRequestQueueById(long id) {
        return queues.get(id);
    }

    @Override
    public RequestQueue getRequestQueueByName(String name) {
        for (RequestQueue queue : queues.values()) {
            if (queue.getName().equalsIgnoreCase(name)) {
                return queue;
            }
        }
        
        return null;
    }

    @Override
    public List<RequestQueue> getAllRequestQueues() {
        return Collections.unmodifiableList(new ArrayList<RequestQueue>(queues.values()));
    }

    @Override
    public int getNumAssignedRequests(IPermissionUser receiver) {
        int count = 0;
        
        for (RequestQueue queue : queues.values()) {
            count += queue.getRequestsByReceiver(receiver, false, false).size();
        }
        
        return count;
    }
    
    @Override
    public int getMaxAssignedRequests() {
        return assignLimit;
    }

    @Override
    public Request getRequestById(long id) {
        if (requestQueues.containsKey(id))
            return getRequestQueueById(requestQueues.get(id)).getRequest(id);
        else
            return null;
    }

    @Override
    public void deleteOldRequests() {
        if (requestDeleteDays == 0) return;
        
        int count = 0;
        
        for (RequestQueue queue : queues.values()) {
            count += queue.deleteOldRequests(requestDeleteDays);
        }
        
        if (count > 0)
            GoldenApple.log("Deleted " + count + " closed request(s) from database");
    }

    @Override
    public void deleteQueue(long id) {
        if (!queues.containsKey(id))
            throw new IllegalArgumentException("No queue exists with ID " + id);
        
        for (Request r : queues.remove(id).getAllRequests(true, true)) {
            requestQueues.remove(r.getId());
            notifyAutoAssignRequestEvent(r, AutoAssignRequestEvent.CLOSE);
        }
        
        try {
            GoldenApple.getInstanceDatabaseManager().execute("DELETE FROM RequestQueues WHERE ID=?", id);
        } catch (SQLException e) {
            throw new RuntimeException("Failed to delete request queue from database", e);
        }
    }

    @Override
    public RequestQueue createQueue(String name) {
        if (getRequestQueueByName(name) != null)
            throw new IllegalArgumentException("Queue " + name + " already exists");
        
        long id;
        
        try {
            ResultSet r = GoldenApple.getInstanceDatabaseManager().executeReturnGenKeys("INSERT INTO RequestQueues (Name, MaxRequestsPerSender, AllowNoReceiver) VALUES (?, 1, 1)", name);
            
            try {
                r.next();
                id = r.getLong(1);
            } finally {
                GoldenApple.getInstanceDatabaseManager().closeResult(r);
            }
        } catch (SQLException e) {
            throw new RuntimeException("Failed to add request queue to database", e);
        }
        
        RequestQueue queue = new DatabaseRequestQueue(id, name);
        queues.put(id, queue);
        
        return queue;
    }

    public void close() {
        deleteOldTask.cancel();
        assigner.notifyShutdown();
    }

    @Override
    public void notifyAutoAssignUserEvent(User user, AutoAssignUserEvent event) {
        if (assigner != null) assigner.notifyUserEvent(user, event);
    }

    @Override
    public void notifyAutoAssignRequestEvent(Request request, AutoAssignRequestEvent event) {
        if (assigner != null) assigner.notifyRequestEvent(request, event);
    }

}
