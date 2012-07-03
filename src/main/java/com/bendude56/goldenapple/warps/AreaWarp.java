package com.bendude56.goldenapple.warps;

import org.bukkit.Location;

import com.bendude56.goldenapple.area.Area;

/**
 * This class is a special warp assigned to
 * Areas when they are created, allowing for
 * easy teleporting to any given Area.
 * @author Deaboy
 *
 */
public class AreaWarp extends Warp{
	private Area area;
	
	public AreaWarp(Long ID, Location location, Area area) {
		super(ID, location);
		this.area = area;
	}
	
	public void setArea(Area area){
		this.area = area;
	}
	public Area getArea(){
		return area;
	}
}
