package com.bendude56.goldenapple.mail;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Date;
import java.util.Deque;
import java.util.HashMap;
import java.util.List;

import com.bendude56.goldenapple.GoldenApple;
import com.bendude56.goldenapple.User;
import com.bendude56.goldenapple.mail.MailMessage.MailStatus;
import com.bendude56.goldenapple.permissions.IPermissionUser;

public class SimpleMailManager extends MailManager {
    private HashMap<Long, MailMessageSent> messageCache;
    private Deque<Long> messageCacheOut;
    
    private HashMap<Long, List<Long>> receiverMessageCache;
    
    private int maxCachedMessages;
    
    public SimpleMailManager() {
        messageCache = new HashMap<Long, MailMessageSent>();
        messageCacheOut = new ArrayDeque<Long>();
        receiverMessageCache = new HashMap<Long, List<Long>>();
        
        maxCachedMessages = Math.max(10, GoldenApple.getInstanceMainConfig().getInt("modules.mail.maxCachedMessages", 50));
        
        GoldenApple.getInstanceDatabaseManager().createOrUpdateTable("mail");
        GoldenApple.getInstanceDatabaseManager().createOrUpdateTable("mailarguments");
    }
    
    private void cacheMessage(MailMessageSent message) {
        messageCache.put(message.getId(), message);
        messageCacheOut.add(message.getId());
        
        if (messageCacheOut.size() > maxCachedMessages) {
            messageCache.remove(messageCacheOut.pop());
        }
    }
    
    private MailMessageSent loadMessage(ResultSet r) throws SQLException {
        if (r.getObject("LocalizedMessage") != null) {
            return new SimpleMailMessageLocalized(r);
        } else if (r.getObject("Subject") != null && r.getObject("Contents") != null) {
            return new SimpleMailMessage(r);
        } else {
            throw new UnsupportedOperationException();
        }
    }
    
    @Override
    public MailMessageSent getMessage(long id) {
        if (messageCache.containsKey(id)) {
            return messageCache.get(id);
        } else {
            try {
                ResultSet r = GoldenApple.getInstanceDatabaseManager().executeQuery("SELECT * FROM Mail WHERE ID=?", id);
                
                try {
                    if (r.next()) {
                        return loadMessage(r);
                    } else {
                        return null;
                    }
                } finally {
                    GoldenApple.getInstanceDatabaseManager().closeResult(r);
                }
            } catch (SQLException e) {
                throw new RuntimeException(e);
            }
        }
    }
    
    @Override
    public MailMessageSent getByUserSpecificNumber(IPermissionUser receiver, int userNum) {
        if (!receiverMessageCache.containsKey(receiver.getId())) {
            getMessages(receiver, false);
        }
        
        if (userNum <= 0 || userNum > receiverMessageCache.get(receiver.getId()).size()) {
            return null;
        }
        
        return getMessage(receiverMessageCache.get(receiver.getId()).get(receiverMessageCache.get(receiver.getId()).size() - userNum));
    }
    
    @Override
    public List<MailMessageSent> getMessages(IPermissionUser receiver, boolean unreadOnly) {
        List<MailMessageSent> messages = new ArrayList<MailMessageSent>();
        
        if (receiverMessageCache.containsKey(receiver.getId())) {
            for (Long messageId : receiverMessageCache.get(receiver.getId())) {
                MailMessageSent message = getMessage(messageId);
                
                if (!unreadOnly || message.getStatus() == MailStatus.UNREAD) {
                    messages.add(message);
                }
            }
        } else {
            try {
                ResultSet r = GoldenApple.getInstanceDatabaseManager().executeQuery("SELECT * FROM Mail WHERE Receiver=? ORDER BY Sent DESC", receiver.getId());
                List<Long> cache = new ArrayList<Long>();
                
                try {
                    while (r.next()) {
                        MailMessageSent message = loadMessage(r);
                        cacheMessage(message);
                        cache.add(message.getId());
                        
                        if (!unreadOnly || message.getStatus() == MailStatus.UNREAD) {
                            messages.add(message);
                        }
                    }
                } finally {
                    GoldenApple.getInstanceDatabaseManager().closeResult(r);
                }
                
                receiverMessageCache.put(receiver.getId(), cache);
            } catch (SQLException e) {
                throw new RuntimeException(e);
            }
        }
        
        return messages;
    }
    
    @Override
    public int getUserSpecificNumber(MailMessageSent message) {
        if (!receiverMessageCache.containsKey(message.getReceiverId())) {
            getMessages(message.getReceiver(), false);
        }
        
        return receiverMessageCache.get(message.getReceiverId()).size() - receiverMessageCache.get(message.getReceiverId()).indexOf(message.getId());
    }
    
    @Override
    public void uncacheMessage(MailMessageSent message) {
        messageCache.remove(message.getId());
        messageCacheOut.remove(message.getId());
        
        if (receiverMessageCache.containsKey(message.getReceiverId())) {
            receiverMessageCache.get(message.getReceiverId()).remove(message.getId());
        }
    }
    
    @Override
    public MailMessageEditable createTemporaryMessage(IPermissionUser sender, IPermissionUser receiver, String subject) {
        SimpleMailMessageTemporary message = new SimpleMailMessageTemporary(sender);
        
        message.setReceiver(receiver);
        message.setSubject(subject);
        
        return message;
    }
    
    private <T extends MailMessageSent> T sendMessage(T message) {
        message.insert();
        cacheMessage(message);
        
        if (receiverMessageCache.containsKey(message.getReceiverId())) {
            receiverMessageCache.get(message.getReceiverId()).add(0, message.getId());
        }
        
        User user = User.getUser(message.getReceiverId());
        
        if (user != null) {
            user.sendLocalizedMessage("module.mail.notify.newMail");
        }
        
        return message;
    }
    
    @Override
    public MailMessageLocalized sendSystemMessage(IPermissionUser receiver, String localeMessage, String... args) {
        return sendMessage(new SimpleMailMessageLocalized(0, new Date(), MailStatus.UNREAD, receiver.getId(), -1, localeMessage, args));
    }
    
    @Override
    public MailMessageSent sendMessage(IPermissionUser receiver, IPermissionUser sender, String subject, String contents) {
        return sendMessage(new SimpleMailMessage(0, new Date(), MailStatus.UNREAD, receiver.getId(), (sender == null) ? -1 : sender.getId(), subject, contents));
    }
    
    @Override
    public void clearCache() {
        messageCache.clear();
        messageCacheOut.clear();
        receiverMessageCache.clear();
    }
}
