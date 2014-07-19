package com.bendude56.goldenapple.chat;

import java.util.List;

import com.bendude56.goldenapple.GoldenApple;
import com.bendude56.goldenapple.User;
import com.bendude56.goldenapple.permissions.IPermissionUser;

/**
 * Represents a chat channel that users can connect to and use to chat to each
 * other.
 * 
 * @author ben_dude56
 */
public interface IChatChannel {
    /**
     * Gets the name by which the channel can be uniquely identified.
     * 
     * @return The channel's identifier.
     */
    public String getName();
    
    /**
     * Gets the name that should be shown to users when referring to this chat
     * channel.
     * 
     * @return The channel's display name.
     */
    public String getDisplayName();
    
    /**
     * Sets the name that should be shown to users when referring to this chat
     * channel.
     * 
     * @param displayName The channel's new display name.
     */
    public void setDisplayName(String displayName);
    
    /**
     * Attempts to have the specified user join this chat channel. Relevant
     * messages will be sent directly to the user automatically.
     * <p />
     * If the user does not have permission to join this chat channel, they will
     * be told this and no further action will be taken.
     * <p />
     * In the event that the user is already in a chat channel, the user will be
     * disconnected from their current channel in order to join this channel.
     * 
     * @param user The user who is attempting to join the channel.
     * @param broadcast Whether or not a join message should be broadcast to the
     * channel.
     * @return {@code true} if the user has successfully joined. {@code false}
     * otherwise.
     */
    public boolean join(User user, boolean broadcast);
    
    /**
     * Causes the specified user to leave this chat channel. The user will be
     * notified automatically. This should be used when a user leaves a chat
     * channel normally; if the user was forcibly kicked from the channel,
     * {@link #kick(User, boolean)} should be used instead.
     * 
     * @param user The user who is leaving the channel.
     * @param broadcast Whether or not a leave message should be broadcast to
     * the channel.
     */
    public void leave(User user, boolean broadcast);
    
    /**
     * Kicks the specified user from this chat channel. The user will be
     * notified automatically that they have been kicked.
     * 
     * @param user The user to be kicked from this channel.
     * @param broadcast Whether or not a leave message should be broadcast to
     * the channel.
     */
    public void kick(User user, boolean broadcast);
    
    /**
     * Gets the chat censor that this channel is using to censor messages.
     * 
     * @return This channel's chat censor.
     */
    public IChatCensor getCensor();
    
    /**
     * Sets the chat censor that should be used to censor messages sent to this
     * channel.
     * 
     * @param censor The chat censor that should be used.
     */
    public void setCensor(IChatCensor censor);
    
    /**
     * Gets the message of the day that should be displayed to users when
     * joining this channel. This message will be automatically sent to any
     * users for whom {@link #join(User, boolean)} is called.
     * 
     * @return The MOTD set on this channel.
     */
    public String getMotd();
    
    /**
     * Sets the message of the day that will be displayed to users when they
     * join this channel.
     * 
     * @param motd The new MOTD to be displayed on this channel.
     */
    public void setMotd(String motd);
    
    /**
     * Calculates the access level granted to the requested user. This function
     * will <strong>fully recalculate</strong> a user's access level and should
     * be used sparingly for performance reasons.
     * <p />
     * Where possible, {@link #getAccessLevel(IPermissionUser)} should be used
     * instead of this function, since that function will look into the cache of
     * access levels for users currently connected to this channel before
     * recalculating a user's access level.
     * 
     * @param user The user whose access level should be recalculated.
     * @return The access level that has been granted to this user.
     */
    public ChatChannelAccessLevel calculateAccessLevel(IPermissionUser user);
    
    /**
     * Gets the default access level which is granted to all users regardless of
     * their group membership.
     * 
     * @return This channel's default access level.
     */
    public ChatChannelAccessLevel getDefaultAccessLevel();
    
    /**
     * Sets the default access level to be granted to all users connecting to
     * this channel.
     * <p />
     * For security reasons, only {@link ChatChannelAccessLevel#CHAT} and below
     * are supported. Any attempt to set the default access level higher than
     * this will result in an {@link IllegalArgumentException}.
     * 
     * @param level The level to be granted to all users on this channel. Must
     * be {@link ChatChannelAccessLevel#CHAT} or below.
     */
    public void setDefaultAccessLevel(ChatChannelAccessLevel level);
    
    /**
     * Gets the cached access level of a user who is currently connected to this
     * channel.
     * <p />
     * This function will not attempt to recalculate a user's access level if
     * their access level has not been cached. If recalculation is desired,
     * {@link #getAccessLevel(IPermissionUser)} should be used instead.
     * 
     * @param user The user whose cached access level should be retrieved.
     * @return The access level that has been cached for the requested user, or
     * {@code null} if they are not currently connected to this channel.
     */
    public ChatChannelAccessLevel getCachedAccessLevel(User user);
    
    /**
     * Sets the cached access level of a user in this channel.
     * <p />
     * This function should be avoided wherever possible, as discrepancies
     * between calculated access levels and cached access levels will not be
     * automatically detected.
     * 
     * @param user The user whose cached access level should be set.
     * @param level The level to be cached for the requested user, or
     * {@code null} to remove them from the cache entirely.
     */
    public void setCachedAccessLevel(User user, ChatChannelAccessLevel level);
    
    /**
     * Gets the access level that has been granted to the requested user.
     * <p />
     * This function will retrieve the user's cached level wherever possible,
     * but will recalculate a user's access level if their access is not cached.
     * 
     * @param user The user whose access level should be retrieved.
     * @return The access level granted to the user.
     */
    public ChatChannelAccessLevel getAccessLevel(IPermissionUser user);
    
    /**
     * Sends whois information about one user to another. Whois information
     * includes the user's access level and any mutes that have been put on them
     * in this channel.
     * 
     * @param user The user to send the whois information to.
     * @param target The user whose whois information should be retrieved.
     */
    public void sendWhoisInformation(User user, IPermissionUser target);
    
    /**
     * Gets a {@link ChatChannelDisplayType} that describes how this chat
     * channel should be displayed to a user when listing chat channels.
     * 
     * @param user The user who is listing chat channels.
     * @return A {@link ChatChannelDisplayType} describing how to display this
     * channel to the user.
     */
    public ChatChannelDisplayType getDisplayType(User user);
    
    /**
     * Gets the name which should be displayed to a user when listing chat
     * channels. If the display name of this channel differs from the
     * identifying name, the identifying name will be included to allow the user
     * to refer to this channel.
     * 
     * @return The name that should be displayed to users when displaying this
     * chat channel on a list.
     */
    public String getListedName();
    
    /**
     * Broadcasts an unlocalized message to users on this channel.
     * <p />
     * This type of broadcast should only be used for user-sent messages.
     * Control messages should be localized and sent through either
     * {@link #broadcastLocalizedMessage(String)} or
     * {@link #broadcastLocalizedMessage(String, String...)}.
     * 
     * @param message The message that should be broadcast on this channel.
     */
    public void broadcastMessage(String message);
    
    /**
     * Broadcasts a localized message to user's on this channel. The message
     * will be automatically translated individually for each user.
     * 
     * @param message The localization name of the message to be broadcast on
     * this channel.
     */
    public void broadcastLocalizedMessage(String message);
    
    /**
     * Broadcasts a localized message to user's on this channel. The message
     * will be automatically translated individually for each user.
     * 
     * @param message The localization name of the message to be broadcast on
     * this channel.
     * @param arguments The arguments to be used in the localization of the
     * message.
     */
    public void broadcastLocalizedMessage(String message, String... arguments);
    
    /**
     * Sends a chat message on behalf of a user. A user's permissions will be
     * checked and any errors will be relayed directly to the user.
     * 
     * @param user The user on behalf of whom the message should be sent.
     * @param message The message to send.
     */
    public void sendMessage(User user, String message);
    
    /**
     * Sends a /me-style message on behalf of a user. A user's permissions will
     * be checked and any errors will be relayed directly to the user.
     * 
     * @param user The user on behalf of whom the message should be sent.
     * @param message The message to send.
     */
    public void sendMeMessage(User user, String message);
    
    /**
     * Checks whether or not the specified user is currently connected to this
     * channel.
     * 
     * @param user The user to check.
     * @return {@code true} if the user is currently connected to this channel.
     * {@code false} otherwise.
     */
    public boolean isInChannel(User user);
    
    /**
     * Gets a list of users that are currently connected to this channel.
     * <p />
     * The returned list cannot be modified directly. To connect/disconnect
     * users, {@link #join(User, boolean)}, {@link #leave(User, boolean)}, and
     * {@link #kick(User, boolean)} should be used.
     * 
     * @return A list of users connected to this channel.
     */
    public List<User> getActiveUsers();
    
    /**
     * Deletes this channel. All connected users will be automatically kicked
     * and this channel will be removed from the list of active channels.
     */
    public void delete();
    
    /**
     * Checks whether or not a user is permitted to use the requested feature
     * from the /channel command. This is <strong>not a security
     * feature</strong>. It is used to allow custom commands to fully control
     * channels.
     * 
     * @param user The user attempting to use the feature.
     * @param feature The feature which is being used.
     * @return {@code true} if the feature is allowed to be used from /channel.
     * {@code false} otherwise.
     */
    public boolean isFeatureAccessible(User user, ChatChannelFeature feature);
    
    public enum ChatChannelFeature {
        SET_DISPLAY_NAME, SET_CENSOR, SET_MOTD, SET_ACCESS_LEVELS, WHOIS, KICK_USER, MUTE_USER, LIST_USERS, DELETE
    }
    
    public enum ChatChannelDisplayType {
        HIDDEN, GRAYED_OUT, NORMAL, CONNECTED
    }
    
    public enum ChatChannelAccessLevel {
        NO_ACCESS(0, "n", "none", "general.channel.levelDisplay.none"),
        JOIN(1, "j", "join", "general.channel.levelDisplay.join"),
        CHAT(2, "c", "chat", "general.channel.levelDisplay.chat"),
        VIP(3, "v", "vip", "general.channel.levelDisplay.vip"),
        MODERATOR(4, "m", "mod", "general.channel.levelDisplay.mod"),
        SUPER_MODERATOR(5, "s", "supermod", "general.channel.levelDisplay.supermod"),
        ADMINISTRATOR(6, "a", "admin", "general.channel.levelDisplay.admin");
        
        private final int levelId;
        private final String complexName;
        private final String simpleName;
        private final String displayName;
        
        private ChatChannelAccessLevel(int levelId, String complexName, String simpleName, String displayName) {
            this.levelId = levelId;
            this.complexName = complexName;
            this.simpleName = simpleName;
            this.displayName = displayName;
        }
        
        public boolean canJoin() {
            return this.levelId >= ChatChannelAccessLevel.JOIN.levelId;
        }
        
        public boolean canChat() {
            return this.levelId >= ChatChannelAccessLevel.CHAT.levelId;
        }
        
        public boolean isVip() {
            return this.levelId >= ChatChannelAccessLevel.VIP.levelId;
        }
        
        public boolean isModerator() {
            return this.levelId >= ChatChannelAccessLevel.MODERATOR.levelId;
        }
        
        public boolean isSuperModerator() {
            return this.levelId >= ChatChannelAccessLevel.SUPER_MODERATOR.levelId;
        }
        
        public boolean isAdministrator() {
            return this.levelId >= ChatChannelAccessLevel.ADMINISTRATOR.levelId;
        }
        
        public boolean canGrant(ChatChannelAccessLevel level) {
            if (this == ChatChannelAccessLevel.ADMINISTRATOR) {
                return true;
            } else if (this == ChatChannelAccessLevel.SUPER_MODERATOR) {
                return level.levelId < ChatChannelAccessLevel.VIP.levelId;
            } else {
                return false;
            }
        }
        
        public boolean canRevoke(ChatChannelAccessLevel level) {
            if (this == ChatChannelAccessLevel.ADMINISTRATOR) {
                return true;
            } else if (this == ChatChannelAccessLevel.SUPER_MODERATOR) {
                return this.levelId > level.levelId;
            } else {
                return false;
            }
        }
        
        public boolean canPunish(ChatChannelAccessLevel level) {
            if (this == ChatChannelAccessLevel.ADMINISTRATOR) {
                return true;
            } else if (this.levelId >= ChatChannelAccessLevel.MODERATOR.levelId) {
                return this.levelId > level.levelId;
            } else {
                return false;
            }
        }
        
        public int getLevelId() {
            return levelId;
        }
        
        public String getComplexName(User user) {
            return complexName;
        }
        
        public String getSimpleName(User user) {
            return simpleName;
        }
        
        public String getDisplayName(User user) {
            return GoldenApple.getInstance().getLocalizationManager().getMessage(user, displayName);
        }
        
        public static ChatChannelAccessLevel fromLevelId(int levelId) {
            for (ChatChannelAccessLevel level : ChatChannelAccessLevel.values()) {
                if (level.levelId == levelId) {
                    return level;
                }
            }
            
            return ChatChannelAccessLevel.NO_ACCESS;
        }
        
        public static ChatChannelAccessLevel fromComplexCommandArgument(User user, String argument) {
            for (ChatChannelAccessLevel level : ChatChannelAccessLevel.values()) {
                if (level.getComplexName(user).equalsIgnoreCase(argument)) {
                    return level;
                }
            }
            
            return null;
        }
        
        public static ChatChannelAccessLevel fromSimpleCommandArgument(User user, String argument) {
            for (ChatChannelAccessLevel level : ChatChannelAccessLevel.values()) {
                if (level.getSimpleName(user).equalsIgnoreCase(argument)) {
                    return level;
                }
            }
            
            return null;
        }
    }
}
