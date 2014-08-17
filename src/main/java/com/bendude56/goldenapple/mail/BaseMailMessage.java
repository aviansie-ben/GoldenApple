package com.bendude56.goldenapple.mail;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Date;

import com.bendude56.goldenapple.GoldenApple;
import com.bendude56.goldenapple.permissions.IPermissionUser;
import com.bendude56.goldenapple.permissions.PermissionManager;

public abstract class BaseMailMessage implements MailMessageSent {
    private long id;
    private Date sent;
    private MailStatus status;
    private long receiver;
    private long sender;
    
    public BaseMailMessage(ResultSet r) throws SQLException {
        id = r.getLong("ID");
        sent = r.getTimestamp("Sent");
        status = MailStatus.fromIdentifier(r.getString("Status"));
        receiver = r.getLong("Receiver");
        sender = (r.getObject("Sender") == null) ? -1 : r.getInt("Sender");
    }
    
    public BaseMailMessage(long id, Date sent, MailStatus status, long receiver, long sender) {
        this.id = id;
        this.sent = sent;
        this.status = status;
        this.receiver = receiver;
        this.sender = sender;
    }
    
    @Override
    public long getId() {
        return id;
    }
    
    protected void setId(long id) {
        this.id = id;
    }
    
    @Override
    public Date getSentTime() {
        return sent;
    }
    
    @Override
    public MailStatus getStatus() {
        return status;
    }
    
    @Override
    public void setStatus(MailStatus status) {
        this.status = status;
        
        try {
            GoldenApple.getInstanceDatabaseManager().execute("UPDATE Mail SET Status=? WHERE ID=?", status.identifier, id);
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }
    
    @Override
    public long getReceiverId() {
        return receiver;
    }
    
    @Override
    public IPermissionUser getReceiver() {
        return (receiver == -1) ? null : PermissionManager.getInstance().getUser(receiver);
    }
    
    @Override
    public long getSenderId() {
        return sender;
    }
    
    @Override
    public IPermissionUser getSender() {
        return (sender == -1) ? null : PermissionManager.getInstance().getUser(sender);
    }
    
    @Override
    public void delete() {
        try {
            GoldenApple.getInstanceDatabaseManager().execute("DELETE FROM Mail WHERE ID=?", id);
            MailManager.getInstance().uncacheMessage(this);
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }
}
