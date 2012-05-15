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
public class PrivateArea extends Area {
	private PermissionUser owner;
	private List<PermissionUser> guests = new ArrayList<PermissionUser>();
	private PermissionGroup group;
	
	public PrivateArea(Location corner1, Location corner2, boolean ignoreY, PermissionUser owner) {
		this.setOwner(owner);
		this.setCorner1(corner1);
		this.setCorner2(corner2);
		this.ignoreY(ignoreY);
	}
	
	public PermissionUser getOwner() {
		return owner;
	}
	
	public List<PermissionUser> getGuests() {
		return guests;
	}
	
	public PermissionGroup getGroup() {
		return group;
	}
	
	public void setOwner(PermissionUser newOwner) {
		if (newOwner != null)
			owner = newOwner;
	}
	
	public void addGuest(PermissionUser guest) {
		if (guest != null && !guests.contains(guest))
			guests.add(guest);
	}
	
	public void remGuest(PermissionUser guest) {
		if (guest != null && guests.contains(guest))
			guests.remove(guest);
	}
	
	public void clearGuests() {
		guests.clear();
	}
	
	public void setGroup(PermissionGroup newGroup) {
		if (newGroup != null)
			group = newGroup;
	}
}