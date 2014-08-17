package com.bendude56.goldenapple.mail;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.Map.Entry;

import com.bendude56.goldenapple.GoldenApple;
import com.bendude56.goldenapple.User;

public class SimpleMailMessageLocalized extends BaseMailMessage implements MailMessageLocalized {
    private String localizedMessage;
    private HashMap<Integer, String> localizedArguments;
    
    public SimpleMailMessageLocalized(ResultSet r) throws SQLException {
        super(r);
        
        this.localizedMessage = r.getString("LocalizedMessage");
        this.localizedArguments = new HashMap<Integer, String>();
        
        ResultSet r2 = GoldenApple.getInstanceDatabaseManager().executeQuery("SELECT Argument, Value FROM MailArguments WHERE MailID=?", getId());
        
        try {
            while (r2.next()) {
                localizedArguments.put(r2.getInt("Argument"), r2.getString("Value"));
            }
        } finally {
            GoldenApple.getInstanceDatabaseManager().closeResult(r2);
        }
    }
    
    public SimpleMailMessageLocalized(long id, Date sent, MailStatus status, long receiver, long sender, String localizedMessage, String... arguments) {
        super(id, sent, status, receiver, sender);
        
        this.localizedMessage = localizedMessage;
        
        this.localizedArguments = new HashMap<Integer, String>();
        
        for (String arg : arguments) {
            this.localizedArguments.put(this.localizedArguments.size(), arg);
        }
    }
    
    @Override
    public String getSubject(User user) {
        return user.getLocalizedMessage("mail:" + localizedMessage + ".subject", (Object[]) getArguments());
    }
    
    @Override
    public String getContents(User user) {
        return user.getLocalizedMessage("mail:" + localizedMessage + ".contents", (Object[]) getArguments());
    }
    
    @Override
    public void insert() {
        try {
            ResultSet r = GoldenApple.getInstanceDatabaseManager().executeReturnGenKeys("INSERT INTO Mail (Sender, Receiver, Sent, Status, LocalizedMessage) VALUES (?, ?, ?, ?, ?)", (getSenderId() == -1) ? null : getSenderId(), getReceiverId(), getSentTime(), getStatus().identifier, localizedMessage);
            
            try {
                if (r.next()) {
                    this.setId(r.getLong(1));
                }
            } finally {
                GoldenApple.getInstanceDatabaseManager().closeResult(r);
            }
            
            for (Entry<Integer, String> arg : localizedArguments.entrySet()) {
                GoldenApple.getInstanceDatabaseManager().execute("INSERT INTO MailArguments (MailID, Argument, Value) VALUES (?, ?, ?)", this.getId(), arg.getKey(), arg.getValue());
            }
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public String getContentMessage() {
        return "mail:" + localizedMessage + ".contents";
    }

    @Override
    public String getSubjectMessage() {
        return "mail:" + localizedMessage + ".subject";
    }

    @Override
    public String[] getArguments() {
        ArrayList<String> args = new ArrayList<String>();
        
        for (int i = 0; localizedArguments.containsKey(i); i++) {
            args.add(localizedArguments.get(i));
        }
        
        return args.toArray(new String[args.size()]);
    }
    
}
