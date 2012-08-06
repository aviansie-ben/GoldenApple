package com.bendude56.goldenapple.chat;

import java.util.ArrayList;
import java.util.List;

import org.bukkit.ChatColor;
import org.bukkit.entity.Player;

import com.bendude56.goldenapple.GoldenApple;
import com.bendude56.goldenapple.permissions.PermissionGroup;

public class ChatChannel {
	private final long ID;
	private long group;

	private String label;

	private ChatColor color;
	
	private List<Player> spies = new ArrayList<Player>();

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

	public void sendMessage(String message){
		for (Player player : GoldenApple.getInstance().chat.getPlayers(this))
			player.sendMessage(message);
		for (Player spy : getSpies())
			spy.sendMessage(color + "[" + label + "] " + ChatColor.WHITE + message);
	}

	public void setGroup(PermissionGroup group){
		this.group = group.getId();
	}
	public long getGroupId(){
		return group;
	}

	public void addSpy(Player player){
		if (!spies.contains(player))
			spies.add(player);
	}
	public List<Player> getSpies(){
		return spies;
	}
	public void removeSpy(Player player){
		if (spies.contains(player))
			spies.remove(player);
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
	
	@Override
	public String toString(){
		return (ID + ":" + label + ":" + color.toString() + ":" + group);
	}
}