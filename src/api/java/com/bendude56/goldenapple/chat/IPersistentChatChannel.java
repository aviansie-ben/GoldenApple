package com.bendude56.goldenapple.chat;

/**
 * Represents a channel which is stored to a persistent medium, such as a
 * database. Channels of this type will not disappear if the server is rebooted.
 * 
 * @author ben_dude56
 */
public interface IPersistentChatChannel extends IChatChannel {
    
    /**
     * Saves this channel to its persistent medium.
     */
    public void save();
}
