package com.bendude56.goldenapple.permissions;

import java.util.List;

import org.bukkit.ChatColor;

public interface IPermissionGroup extends IPermissionObject {
	public String getName();
	
	public int getPriority();
	public void setPriority(int priority);
	
	public boolean isChatColorSet();
	public ChatColor getChatColor();
	public String getPrefix();
	public void setChatColor(boolean isSet, ChatColor color);
	public void setPrefix(String prefix);
	
	public List<Long> getUsers();
	public List<Long> getAllUsers();
	public void addUser(IPermissionUser user);
	public void removeUser(IPermissionUser user);
	public boolean isMember(IPermissionUser user, boolean directOnly);
	
	public List<Long> getGroups();
	public List<Long> getAllGroups();
	public void addGroup(IPermissionGroup group);
	public void removeGroup(IPermissionGroup group);
	public boolean isMember(IPermissionGroup group, boolean directOnly);
}
