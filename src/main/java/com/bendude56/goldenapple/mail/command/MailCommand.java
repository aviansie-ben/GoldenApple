package com.bendude56.goldenapple.mail.command;

import java.text.DateFormat;
import java.util.HashMap;
import java.util.List;

import com.bendude56.goldenapple.GoldenApple;
import com.bendude56.goldenapple.User;
import com.bendude56.goldenapple.command.GoldenAppleCommand;
import com.bendude56.goldenapple.mail.MailManager;
import com.bendude56.goldenapple.mail.MailMessage.MailStatus;
import com.bendude56.goldenapple.mail.MailMessageEditable;
import com.bendude56.goldenapple.mail.MailMessageSent;
import com.bendude56.goldenapple.permissions.IPermissionUser;
import com.bendude56.goldenapple.permissions.PermissionManager;

public class MailCommand extends GoldenAppleCommand {
    private HashMap<Long, MailMessageEditable> activeDrafts = new HashMap<Long, MailMessageEditable>();
    private HashMap<Long, Long> draftReplyTo = new HashMap<Long, Long>();
    
    @Override
    public boolean onExecute(GoldenApple instance, User user, String commandLabel, String[] args) {
        if (args.length != 0 && (args[0].equalsIgnoreCase("help") || args[0].equalsIgnoreCase("-?"))) {
            sendHelp(user, commandLabel);
            return true;
        }
        
        user.sendLocalizedMessage("header.mail");
        
        if (args.length == 0 || args[0].equalsIgnoreCase("all") || args[0].equalsIgnoreCase("list") || args[0].equalsIgnoreCase("unread")) {
            boolean unreadOnly = args.length != 0 && args[0].equalsIgnoreCase("unread");
            List<MailMessageSent> messages = MailManager.getInstance().getMessages(user, unreadOnly);
            
            int page = 1;
            int maxPage = (int) Math.max(1, Math.ceil(messages.size() / 5.0));
            
            if (args.length >= 2) {
                try {
                    page = Integer.parseInt(args[1]);
                } catch (NumberFormatException e) {
                    user.sendLocalizedMessage("shared.notANumber", args[1]);
                    return true;
                }
            }
            
            if (page < 1) {
                page = 1;
            } else if (page > maxPage) {
                page = maxPage;
            }
            
            if (messages.size() > 0) {
                user.sendLocalizedMessage((unreadOnly) ? "general.mail.inboxUnread" : "general.mail.inboxAll", messages.size() + "", page + "", maxPage + "");
                
                for (int i = ((page - 1) * 5); i < (page * 5) && i < messages.size(); i++) {
                    MailMessageSent message = messages.get(i);
                    
                    if (message.getSenderId() == -1) {
                        if (message.getStatus() == MailStatus.UNREAD) {
                            user.sendLocalizedMessage("general.mail.list.system.unread", MailManager.getInstance().getUserSpecificNumber(message) + "", message.getSubject(user));
                        } else {
                            user.sendLocalizedMessage("general.mail.list.system.read", MailManager.getInstance().getUserSpecificNumber(message) + "", message.getSubject(user));
                        }
                    } else {
                        if (message.getStatus() == MailStatus.UNREAD) {
                            user.sendLocalizedMessage("general.mail.list.player.unread", MailManager.getInstance().getUserSpecificNumber(message) + "", message.getSubject(user), message.getSender().getName());
                        } else if (message.getStatus() == MailStatus.REPLIED) {
                            user.sendLocalizedMessage("general.mail.list.player.replied", MailManager.getInstance().getUserSpecificNumber(message) + "", message.getSubject(user), message.getSender().getName());
                        } else {
                            user.sendLocalizedMessage("general.mail.list.player.read", MailManager.getInstance().getUserSpecificNumber(message) + "", message.getSubject(user), message.getSender().getName());
                        }
                    }
                }
                
                if (args.length == 0) {
                    if (maxPage > 1) {
                        user.sendLocalizedMessage("general.mail.more");
                    }
                    
                    user.sendLocalizedMessage("general.mail.readHelp");
                }
                
            } else {
                user.sendLocalizedMessage((unreadOnly) ? "general.mail.inboxUnreadEmpty" : "general.mail.inboxAllEmpty");
            }
        } else if (args[0].equalsIgnoreCase("read")) {
            if (args.length < 2) {
                user.sendLocalizedMessage("shared.parameterMissing", "read");
                return true;
            }
            
            int id;
            
            try {
                id = Integer.parseInt(args[1]);
            } catch (NumberFormatException e) {
                user.sendLocalizedMessage("shared.notANumber", args[1]);
                return true;
            }
            
            MailMessageSent message = MailManager.getInstance().getByUserSpecificNumber(user, id);
            
            if (message == null || message.getReceiverId() != user.getId()) {
                user.sendLocalizedMessage("error.mail.notFound", args[1]);
                return true;
            }
            
            IPermissionUser sender = message.getSender();
            
            user.sendLocalizedMessage("general.mail.read.id", message.getId() + "", id + "");
            user.sendLocalizedMessage("general.mail.read.subject", message.getSubject(user));
            
            if (sender == null) {
                user.sendLocalizedMessage("general.mail.read.from.system");
            } else {
                user.sendLocalizedMessage("general.mail.read.from.player", sender.getName(), sender.getUuid().toString());
            }
            
            user.sendLocalizedMessage("general.mail.read.sent", DateFormat.getDateTimeInstance(DateFormat.SHORT, DateFormat.SHORT).format(message.getSentTime()));
            
            if (message.getStatus() == MailStatus.REPLIED) {
                user.sendLocalizedMessage("general.mail.read.replied");
            }
            
            user.getHandle().sendMessage("");
            
            for (String messageLine : message.getContents(user).split("\n")) {
                user.getHandle().sendMessage(messageLine);
            }
            
            if (message.getStatus() == MailStatus.UNREAD) {
                message.setStatus(MailStatus.READ);
            }
        } else if (args[0].equalsIgnoreCase("delete")) {
            if (args.length < 2) {
                user.sendLocalizedMessage("shared.parameterMissing", "read");
                return true;
            }
            
            int id;
            
            try {
                id = Integer.parseInt(args[1]);
            } catch (NumberFormatException e) {
                user.sendLocalizedMessage("shared.notANumber", args[1]);
                return true;
            }
            
            MailMessageSent message = MailManager.getInstance().getByUserSpecificNumber(user, id);
            
            if (message == null || message.getReceiverId() != user.getId()) {
                user.sendLocalizedMessage("error.mail.notFound", args[1]);
                return true;
            }
            
            message.delete();
            user.sendLocalizedMessage("general.mail.deleted", id + "");
        } else if (args[0].equalsIgnoreCase("reply")) {
            if (!user.hasPermission(MailManager.mailReplyPermission)) {
                GoldenApple.logPermissionFail(user, commandLabel, args, true);
                return true;
            }
            
            if (args.length < 2) {
                user.sendLocalizedMessage("shared.parameterMissing", "reply");
                return true;
            }
            
            int id;
            
            try {
                id = Integer.parseInt(args[1]);
            } catch (NumberFormatException e) {
                user.sendLocalizedMessage("shared.notANumber", args[1]);
                return true;
            }
            
            MailMessageSent message = MailManager.getInstance().getByUserSpecificNumber(user, id);
            
            if (message == null || message.getReceiverId() != user.getId()) {
                user.sendLocalizedMessage("error.mail.notFound", args[1]);
                return true;
            } else if (message.getSender() == null) {
                user.sendLocalizedMessage("error.mail.noReplySystem");
                return true;
            }
            
            String subject = (message.getSubject(user).startsWith("RE: ")) ? message.getSubject(user) : "RE: " + message.getSubject(user);
            
            if (args.length > 2) {
                subject = args[2];
                
                for (int i = 3; i < args.length; i++) {
                    subject += " " + args[i];
                }
            }
            
            if (subject.length() > 64) {
                subject = subject.substring(0, 64);
            }
            
            activeDrafts.put(user.getId(), MailManager.getInstance().createTemporaryMessage(user, message.getSender(), subject));
            draftReplyTo.put(user.getId(), message.getId());
            
            user.sendLocalizedMessage("general.mail.createDraft", subject);
        } else if (args[0].equalsIgnoreCase("create")) {
            if (!user.hasPermission(MailManager.mailSendPermission)) {
                GoldenApple.logPermissionFail(user, commandLabel, args, true);
                return true;
            }
            
            if (args.length < 3) {
                user.sendLocalizedMessage("shared.parameterMissing", "create");
                return true;
            }
            
            IPermissionUser receiver = PermissionManager.getInstance().findUser(args[1], false);
            
            if (receiver == null) {
                user.sendLocalizedMessage("shared.userNotFoundError", args[1]);
                return true;
            }
            
            String subject = args[2];
                
            for (int i = 3; i < args.length; i++) {
                subject += " " + args[i];
            }
            
            if (subject.length() > 64) {
                subject = subject.substring(0, 64);
            }
            
            activeDrafts.put(user.getId(), MailManager.getInstance().createTemporaryMessage(user, receiver, subject));
            
            user.sendLocalizedMessage("general.mail.createDraft", subject);
        } else if (args[0].equalsIgnoreCase("draft")) {
            if (!activeDrafts.containsKey(user.getId())) {
                user.sendLocalizedMessage("error.mail.noDraft");
                return true;
            }
            
            MailMessageEditable message = activeDrafts.get(user.getId());
            
            user.sendLocalizedMessage("general.mail.read.subject", message.getSubject(user));
            user.sendLocalizedMessage("general.mail.read.to", message.getReceiver().getName(), message.getReceiver().getUuid().toString());
            
            user.getHandle().sendMessage("");
            
            for (String messageLine : message.getContents(user).split("\n")) {
                user.getHandle().sendMessage(messageLine);
            }
        } else if (args[0].equalsIgnoreCase("append")) {
            if (!activeDrafts.containsKey(user.getId())) {
                user.sendLocalizedMessage("error.mail.noDraft");
                return true;
            } else if (args.length < 2) {
                user.sendLocalizedMessage("shared.parameterMissing", "append");
                return true;
            }
            
            MailMessageEditable message = activeDrafts.get(user.getId());
            String contents = message.getContents(user);
            
            for (int i = 1; i < args.length; i++) {
                if (!contents.isEmpty()) {
                    contents += " ";
                }
                
                contents += args[i];
            }
            
            message.setContents(contents);
            
            user.sendLocalizedMessage("general.mail.appendDraft");
        } else if (args[0].equalsIgnoreCase("send")) {
            if (!activeDrafts.containsKey(user.getId())) {
                user.sendLocalizedMessage("error.mail.noDraft");
                return true;
            }
            
            MailMessageEditable message = activeDrafts.get(user.getId());
            
            if (message.getContents(user).isEmpty()) {
                user.sendLocalizedMessage("error.mail.emptySend");
                return true;
            }
            
            message.send();
            
            if (draftReplyTo.containsKey(user.getId())) {
                MailMessageSent replyTo = MailManager.getInstance().getMessage(draftReplyTo.get(user.getId()));
                
                if (replyTo != null) {
                    replyTo.setStatus(MailStatus.REPLIED);
                }
            }
            
            activeDrafts.remove(user.getId());
            draftReplyTo.remove(user.getId());
            
            user.sendLocalizedMessage("general.mail.sent");
        } else if (args[0].equalsIgnoreCase("discard")) {
            if (!activeDrafts.containsKey(user.getId())) {
                user.sendLocalizedMessage("error.mail.noDraft");
                return true;
            }
            
            activeDrafts.remove(user.getId());
            draftReplyTo.remove(user.getId());
            
            user.sendLocalizedMessage("general.mail.discardDraft");
        } else {
            user.sendLocalizedMessage("shared.unknownOption", args[0]);
        }
        
        return true;
    }
    
    private void sendHelp(User user, String commandLabel) {
        user.sendLocalizedMessage("header.help");
        user.sendLocalizedMultilineMessage("help.mail", commandLabel);
    }
}
