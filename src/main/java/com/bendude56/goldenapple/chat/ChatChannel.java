package com.bendude56.goldenapple.chat;

import org.bukkit.ChatColor;

public class ChatChannel {
	private final long ID;

	private String label;

	private ChatColor color;

	public ChatChannel(long ID, String label) {
		this.ID = ID;
		this.label = label;
		this.color = ChatColor.WHITE;
	}
	public ChatChannel(long ID, String label, ChatColor color) {
		this.ID = ID;
		this.label = label;
		this.color = color;
	}

	public long getId() {
		return this.ID;
	}

	public void setLabel(String label) {
		this.label = label;
	}
	public String getLabel() {
		return this.label;
	}

	public void setColor(ChatColor color) {
		this.color = color;
	}
	public ChatColor getColor() {
		return this.color;
	}
}