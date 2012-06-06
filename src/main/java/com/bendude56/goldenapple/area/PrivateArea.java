package com.bendude56.goldenapple.area;

import java.util.ArrayList;
import java.util.List;

import org.bukkit.Location;

import com.bendude56.goldenapple.permissions.PermissionGroup;
import com.bendude56.goldenapple.permissions.PermissionUser;

/**
 * The class used to protect regions of land against griefing. Extends Area class.
 * 
 * @author Deaboy
 */
public class PrivateArea extends ParentArea {
	private PermissionUser owner;
	private List<PermissionUser> guests = new ArrayList<PermissionUser>();
	private PermissionGroup group;
	
	public PrivateArea(Location corner1, Location corner2, boolean ignoreY, PermissionUser owner) {
		this.setOwner(owner);
		this.setCorner1(corner1);
		this.setCorner2(corner2);
		this.ignoreY(ignoreY);
	}
	
	public void setOwner(PermissionUser newOwner) {
		if (newOwner != null)
			owner = newOwner;
	}
	
	public PermissionUser getOwner() {
		return owner;
	}

	public boolean isOwner(PermissionUser user) {
		return (user == owner);
	}
	
	public void setGroup(PermissionGroup newGroup) {
		if (newGroup != null)
			group = newGroup;
	}
	
	public PermissionGroup getGroup() {
		return group;
	}

	public boolean memberOfGroup(PermissionUser user) {
		return (getGroup().getMembers().contains(user));
	}
	
	public void addGuest(PermissionUser guest) {
		if (guest != null && !guests.contains(guest))
			guests.add(guest);
	}
	
	public void remGuest(PermissionUser guest) {
		if (guest != null && guests.contains(guest))
			guests.remove(guest);
	}
	
	public List<PermissionUser> getGuests() {
		return guests;
	}

	public boolean isGuest(PermissionUser user) {
		return guests.contains(user);
	}
	
	public void clearGuests() {
		guests.clear();
	}
	
	public boolean canBuildHere(PermissionUser user) {
		return (isOwner(user) || isGuest(user) || memberOfGroup(user));
	}
}