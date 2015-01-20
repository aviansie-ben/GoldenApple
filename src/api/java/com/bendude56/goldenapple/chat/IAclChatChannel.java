package com.bendude56.goldenapple.chat;

import com.bendude56.goldenapple.permissions.IPermissionObject;
import com.bendude56.goldenapple.permissions.IPermissionUser;

/**
 * Represents a chat channel whose permissions are controlled by an Access
 * Control List. Both users and groups can be granted permissions on these
 * channels.
 * 
 * @author ben_dude56
 */
public interface IAclChatChannel extends IChatChannel {
    /**
     * Calculates the minimum access level that will be granted to a user.
     * <p />
     * The minimum access level is determined by the default level of the
     * channel as well as the access levels granted to the user indirectly by
     * groups they are a member of.
     * <p />
     * If a user's explicit access level is set lower than this access level, it
     * will have no effect.
     * 
     * @param user The user whose minimum access level should be calculated.
     * @return The calculate minimum access level.
     */
    public ChatChannelAccessLevel calculateMinimumAccessLevel(IPermissionUser user);
    
    /**
     * Gets the explicit access level which has been granted to a user or group
     * in the ACL.
     * 
     * @param obj The user or group whose explicit access level should be
     * retrieved.
     * @return The explicitly defined access level granted to the specified
     * object, or {@code null} if no explicit access is granted to the specified
     * object.
     */
    public ChatChannelAccessLevel getExplicitAccessLevel(IPermissionObject obj);
    
    /**
     * Sets the explicit access level which should be granted to a user or group
     * in the ACL.
     * 
     * @param obj The user or group whose explicit access level should be
     * modified.
     * @param level The access level which should be granted to the requested
     * object, or {@code null} to remove the specified object from the ACL
     * entirely.
     */
    public void setExplicitAccessLevel(IPermissionObject obj, ChatChannelAccessLevel level);
}
