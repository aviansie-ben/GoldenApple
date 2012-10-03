package com.bendude56.goldenapple.area;

import org.bukkit.Location;
import org.bukkit.World;

import com.bendude56.goldenapple.area.AreaManager.AreaType;
import com.bendude56.goldenapple.util.Calculations;

/**
 * The basic area object that manages land from which other area-based classes
 * inherit properties from.
 * 
 * @author Deaboy
 * 
 */
public class Area implements IArea
{
	final Long			ID;
	
	private Location	corner1;
	private Location	corner2;
	
	private boolean		ignoreY;
	private boolean		disabled;
	
	private final AreaType	type;
	
	public Area(Long ID, AreaType type, Location corner1, Location corner2, boolean ignoreY){
		this.ID = ID;
		this.corner1 = corner1.clone();
		this.corner2 = corner2.clone();
		this.ignoreY = ignoreY;
		this.type = type;
	}
	
	/**
	 * Determines if this area contains the given location. Takes ignoreY into account.
	 * @param location The location in question
	 * @return True if the location is within this area, false if not
	 */
	public boolean contains(Location location){
		if (this.disabled) {
			return false;
		} else if (this.getWorld() != location.getWorld()) {
			return false;
		} else if (Calculations.isBetween(corner1.getX(), location.getX(), corner2.getX()) && Calculations.isBetween(corner1.getZ(), location.getZ(), corner2.getZ()) && (!ignoreY || Calculations.isBetween(corner1.getY(), location.getY(), corner2.getY()))) {
			return true;
		} else {
			return false;
		}
	}
	
	/**
	 * This method determines if another area overlaps with this one
	 * at any point. Does not take into account either area's children
	 * (if any).
	 * @param area The area to compare with this one
	 * @return True if the two areas overlap at any point, false if they do not.
	 */
	public boolean overlapsWithArea(Area area){
		if(area.getWorld() != this.getWorld())
			return false;
		if(isDisabled() || area.isDisabled())
			return false;
		if(area.getArea() < this.getArea())
			return area.overlapsWithArea(this);
		
		double WIDTH = area.getWidth();
		double HEIGHT = area.getHeight();
		double DEPTH = area.getDepth();
		
		Location c1 = getCorner1();
		Location c2 = getCorner2();
		fixCorners(c1,c2);
		
		for (double a = 0; a <= Math.ceil(this.getWidth()/area.getWidth()); a++){
			for(double b = 0; b <= Math.ceil(this.getHeight()/area.getHeight()); b++){
				for(double c = 0; c <- Math.ceil(this.getDepth()/area.getDepth()); c++){
					if(area.contains(new Location(getWorld(), ((a*WIDTH>getWidth()) ? c1.getX() : c2.getX()+a*WIDTH),
															  ((b*HEIGHT>getHeight()) ? c1.getY() : c2.getY()+a*HEIGHT),
															  ((c*DEPTH>getDepth()) ? c1.getZ() : c2.getZ()+a*DEPTH))))
						return true;
				}
			}
		}
		return false;
	}
	
	/**
	 * Determines if this Area completely contains another Area. 
	 * @param area The area to test this one with
	 * @return True if the other area is completely contained within this one, false if not.
	 */
	public boolean contains(Area area){
		return(contains(area.corner1) && contains(area.corner2));
	}
	
	public Long 		getID() {
		return ID;
	}
	public World 		getWorld() {
		return corner1.getWorld();
	}
	public void 		setCorner1(Location c) {
		if (c.getWorld() == getWorld())
			corner1 = c.clone();
	}
	/**
	 * Returns this area's corner1, taking ignoreY into account.
	 */
	public Location 	getCorner1() {
		Location loc = corner1.clone();
		if (ignoreY)
			loc.setY(getWorld().getMaxHeight());
		return loc;
	}
	/**
	 * As opposed to <i>getCorner1()</i>, this method does <u>not</u> take ignoreY into account.
	 * @return Returns an exact clone of the area's corner1.
	 */
	public Location		getRealCorner1(){
		return corner1.clone();
	}
	public void 		setCorner2(Location c) {
		if (c.getWorld() == getWorld())
			corner2 = c.clone();
	}
	/**
	 * Returns this area's corner2, taking ignoreY into account.
	 */
	public Location 	getCorner2() {
		Location loc = corner2.clone();
		if (ignoreY)
			loc.setY(0);
		return loc;
	}
	/**
	 * As opposed to <i>getCorner2()</i>, this method does <u>not</u> take ignoreY into account.
	 * @return Returns an exact clone of the area's corner2.
	 */
	public Location		getRealCorner2() {
		return corner2.clone();
	}
	public void 		ignoreY(boolean ignore) {
		ignoreY = ignore;
	}
	public boolean	 	ignoreY() {
		return ignoreY;
	}
	public void 		setDisabled(boolean disabled){
		this.disabled = disabled;
	}
	public boolean 		isDisabled() {
		return disabled;
	}
	
	private void fixCorners(Location corner1, Location corner2){
		Location c1 = new Location(getWorld(),
				Math.max(corner1.getX(), corner2.getX()),
				Math.max(corner1.getY(), corner2.getY()),
				Math.max(corner1.getZ(), corner2.getZ()));
		Location c2 = new Location(getWorld(),
				Math.min(corner1.getX(), corner2.getX()),
				Math.min(corner1.getY(), corner2.getY()),
				Math.min(corner1.getZ(), corner2.getZ()));
		corner1=c1; //Avoiding INFINITE LOOP by directly modifying variables
		corner2=c2;
	}
	
	public double getArea(){
		return(getWidth()*getHeight()*getDepth());
	}
	public double getWidth(){
		return(Math.abs(corner1.getX())+Math.abs(corner2.getX()));
	}
	public double getHeight(){
		if (ignoreY)
			return(getWorld().getMaxHeight());
		else
			return(Math.abs(corner1.getY())+Math.abs(corner2.getY()));
	}
	public double getDepth(){
		return(Math.abs(corner1.getZ())+Math.abs(corner2.getZ()));
	}
	
	public AreaType getType()
	{
		return type;
	}
}
