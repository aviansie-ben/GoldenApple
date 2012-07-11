package com.bendude56.goldenapple.chat;

import org.bukkit.ChatColor;

import com.bendude56.goldenapple.permissions.PermissionGroup;

public class ChatChannel {
	private final long ID;
	private long group;

	private String label;

	private ChatColor color;

	public ChatChannel(long ID, String label) {
		this.ID = ID;
		this.label = label;
		this.color = ChatColor.WHITE;
	}
	public ChatChannel(long ID, String label, PermissionGroup group) {
		this.ID = ID;
		this.label = label;
		this.group = group.getId();
	}
	public ChatChannel(long ID, String label, ChatColor color) {
		this.ID = ID;
		this.label = label;
		this.color = color;
	}
	public ChatChannel(long ID, String label, ChatColor color, PermissionGroup group) {
		this.ID = ID;
		this.label = label;
		this.color = color;
		this.group = group.getId();
	}

	public long getId() {
		return this.ID;
	}
	
	public void setGroup(PermissionGroup group){
		this.group = group.getId();
	}
	public long getGroupId(){
		return group;
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
	
	public String toString(){
		return (ID + ":" + label + ":" + color.toString() + ":" + group);
	}
}