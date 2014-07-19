package com.bendude56.goldenapple.chat;

public interface IChatCensor {
    /**
     * Censors the specified message.
     * 
     * @param message The message that should be censored.
     * @return The message that should be sent out after being censored.
     */
    public String censorMessage(String message);
}
