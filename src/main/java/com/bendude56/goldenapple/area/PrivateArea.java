package com.bendude56.goldenapple.area;

import java.util.ArrayList;
import java.util.List;

import org.bukkit.Location;

import com.bendude56.goldenapple.GoldenApple;
import com.bendude56.goldenapple.area.AreaManager.AreaType;
import com.bendude56.goldenapple.permissions.IPermissionUser;
import com.bendude56.goldenapple.permissions.PermissionGroup;
import com.bendude56.goldenapple.permissions.PermissionUser;

/**
 * The class used to protect regions of land against griefing. Extends Area class.
 * 
 * @author Deaboy
 */
public class PrivateArea extends ParentArea implements IArea
{
	private Long ownerID;
	private List<Long> guestIDs = new ArrayList<Long>();
	private Long groupID;
	
	public PrivateArea(Long ID, Location corner1, Location corner2, boolean ignoreY, IPermissionUser owner) {
		super(ID, AreaType.PRIVATE, corner1, corner2, ignoreY);
		this.setOwner(owner);
	}
	
	public void setOwner(IPermissionUser newOwner) {
		if (newOwner != null)
			ownerID = newOwner.getId();
	}
	
	public PermissionUser getOwner() {
		return PermissionManager.getInstance().getUser(ownerID);
	}

	public boolean isOwner(IPermissionUser user) {
		return (user.getId() == ownerID);
	}
	
	public void setGroup(PermissionGroup newGroup) {
		if (newGroup != null)
			groupID = newGroup.getId();
	}
	
	public PermissionGroup getGroup() {
		return PermissionManager.getInstance().getGroup(groupID);
	}

	public boolean memberOfGroup(IPermissionUser user) {
		// TODO Implement the new group system
		return false;
		// return (getGroup().getMembers().contains(user.getId()));
	}
	
	public void addGuest(IPermissionUser guest) {
		if (guest != null && !guestIDs.contains(guest.getId()))
			guestIDs.add(guest.getId());
	}
	
	public void remGuest(IPermissionUser guest) {
		if (guest != null && guestIDs.contains(guest))
			guestIDs.remove(guest);
	}
	
	public void remGuest(Long guestID) {
		if (guestID != null && guestIDs.contains(guestID))
			guestIDs.remove(guestID);
	}
	
	public List<PermissionUser> getGuests() {
		List<PermissionUser> guests = new ArrayList<PermissionUser>();
		for (Long guestID : guestIDs) {
			if (PermissionManager.getInstance().getUser(guestID) != null) {
				guests.add(PermissionManager.getInstance().getUser(guestID));
			}
		}
		return guests;
	}

	public boolean isGuest(IPermissionUser user) {
		return guestIDs.contains(user.getId());
	}
	
	public void clearGuests() {
		guestIDs.clear();
	}
	
	public boolean canEdit(IPermissionUser user) {
		return (isOwner(user) || isGuest(user) || memberOfGroup(user));
	}
	
	public AreaType getType()
	{
		return AreaType.PRIVATE;
	}
}