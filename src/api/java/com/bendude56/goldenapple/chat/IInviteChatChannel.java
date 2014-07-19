package com.bendude56.goldenapple.chat;

import java.util.List;

import com.bendude56.goldenapple.User;

/**
 * Represents a channel whose permissions are simplified, with an owner and the
 * ability to invite other users to chat.
 * 
 * @author ben_dude56
 */
public interface IInviteChatChannel extends IChatChannel {
    /**
     * Sends an invite to the specified user to join this channel. The user will
     * be sent an invite and they will be added to the list of invited users who
     * can use this channel.
     * 
     * @param user The user who should be invited to this channel.
     */
    public void sendInvite(User user);
    
    /**
     * Cancels an existing invite which was sent to a user on this channel. The
     * user will no longer be able to join this channel and will be kicked from
     * the channel if they are currently connected.
     * 
     * @param user The user whose invite should be cancelled.
     */
    public void cancelInvite(User user);
    
    /**
     * Gets the owner of this chat channel. Only the owner is permitted to
     * invite new users.
     * 
     * @return The current owner of this channel.
     */
    public User getOwner();
    
    /**
     * Gets a list of users who have been invited to join this channel and are
     * permitted to connect and chat.
     * <p />
     * The returned list cannot be modified directly. To send or cancel invites,
     * {@link #sendInvite(User)} and {@link #cancelInvite(User)} should be used
     * instead.
     * 
     * @return A list of users who have been invited.
     */
    public List<User> getInvitedUsers();
    
    /**
     * Checks whether the specified user has been invited to join this chat
     * channel.
     * 
     * @param user The user to check.
     * @return {@code true} if the user has been invited. {@code false}
     * otherwise.
     */
    public boolean isInvited(User user);
}
