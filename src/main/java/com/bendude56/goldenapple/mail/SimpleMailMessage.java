package com.bendude56.goldenapple.mail;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Date;

import com.bendude56.goldenapple.GoldenApple;
import com.bendude56.goldenapple.User;

public class SimpleMailMessage extends BaseMailMessage {
    private String subject;
    private String contents;
    
    public SimpleMailMessage(ResultSet r) throws SQLException {
        super(r);
        
        this.subject = r.getString("Subject");
        this.contents = r.getString("Contents");
    }
    
    public SimpleMailMessage(long id, Date sent, MailStatus status, long receiver, long sender, String subject, String contents) {
        super(id, sent, status, receiver, sender);
        
        this.subject = subject;
        this.contents = contents;
    }
    
    @Override
    public String getSubject(User user) {
        return subject;
    }
    
    @Override
    public String getContents(User user) {
        return contents;
    }
    
    @Override
    public void insert() {
        try {
            ResultSet r = GoldenApple.getInstanceDatabaseManager().executeReturnGenKeys("INSERT INTO Mail (Sender, Receiver, Sent, Status, Subject, Contents) VALUES (?, ?, ?, ?, ?, ?)", (getSenderId() == -1) ? null : getSenderId(), getReceiverId(), getSentTime(), getStatus().identifier, subject, contents);
            
            try {
                if (r.next()) {
                    this.setId(r.getLong(1));
                }
            } finally {
                GoldenApple.getInstanceDatabaseManager().closeResult(r);
            }
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }
    
}
