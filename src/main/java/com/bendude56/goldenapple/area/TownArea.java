package com.bendude56.goldenapple.area;

import org.bukkit.Location;

import com.bendude56.goldenapple.GoldenApple;
import com.bendude56.goldenapple.permissions.IPermissionUser;
import com.bendude56.goldenapple.permissions.PermissionUser;

public class TownArea extends ParentArea {
	private Long ownerID;
	
	public TownArea(Location corner1, Location corner2, boolean ignoreY, IPermissionUser owner) {
		this.setOwner(owner);
		this.setCorner1(corner1);
		this.setCorner2(corner2);
		this.ignoreY(ignoreY);
	}
	
	public void setOwner(IPermissionUser newOwner) {
		if (newOwner != null)
			ownerID = newOwner.getId();
	}
	
	public PermissionUser getOwner() {
		return GoldenApple.getInstance().permissions.getUser(ownerID);
	}

	public boolean isOwner(IPermissionUser user) {
		return (user.getId() == ownerID);
	}
	
	public boolean canEdit(IPermissionUser user) {
		return (isOwner(user));
	}
}
