package com.bendude56.goldenapple.area;

import java.util.ArrayList;
import java.util.List;

import org.bukkit.Location;

import com.bendude56.goldenapple.area.AreaManager.AreaType;
import com.bendude56.goldenapple.permissions.IPermissionUser;

public class TownArea extends ParentArea implements IArea
{
	private Long owner;
	
	private List<PrivateArea> subdivisions = new ArrayList<PrivateArea>();
	
	public TownArea(Long ID, Location corner1, Location corner2, boolean ignoreY, IPermissionUser owner)
	{
		super(ID, AreaType.TOWN, corner1, corner2, ignoreY);
		this.owner = owner.getId();
	}
	
	public void addSubdivision(PrivateArea area){
		if(!subdivisions.contains(area))
			subdivisions.add(area);
	}
	public void deleteSubdivision(PrivateArea area){
		if(subdivisions.contains(area))
			subdivisions.remove(area);
	}
	
	public void setOwner(IPermissionUser owner) {
		if (owner != null)
			this.owner = owner.getId();
	}
	public Long getOwner() {
		return owner;
	}
	public boolean isOwner(IPermissionUser user) {
		return (user.getId() == owner);
	}
	public boolean canEdit(IPermissionUser user) {
		return (isOwner(user));
	}
	
	public AreaType getType()
	{
		return AreaType.TOWN;
	}
}
