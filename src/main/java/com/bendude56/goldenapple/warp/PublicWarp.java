package com.bendude56.goldenapple.warp;

import org.bukkit.Location;

import com.bendude56.goldenapple.GoldenApple;
import com.bendude56.goldenapple.permissions.IPermissionUser;
import com.bendude56.goldenapple.permissions.PermissionGroup;
import com.bendude56.goldenapple.warp.Warp;

/**
 * Child of the Warp class. Also holds a label and a group.
 * Only members of the group are allowed to warp here. Label
 * is simply a name, the name should not match any other warp's name.
 * @author Deaboy
 *
 */
public class PublicWarp extends Warp{
	private String label;
	private Long group;
	
	/**
	 * 
	 * @param ID The final ID number for the warp
	 * @param location The location of the warp
	 * @param label The custom label, or name of the warp
	 * @param group The group allowed to use this warp
	 */
	public PublicWarp(Long ID, Location location, String label, PermissionGroup group){
		super(ID, location);
		this.label = label;
		this.group = group.getId();
	}
	
	/**
	 * This method determines if the user is permitted to
	 * use this warp by checking if the warp has an assigned
	 * group, and if so, if the user is a member of said group.
	 * @param user The user in question
	 * @return True if the user can use the warp, false if not.
	 */
	public boolean canUse(IPermissionUser user){
		if (getGroup()==null)
			return true;
		if (GoldenApple.getInstance().permissions.getGroup(getGroup()).getMembers().contains(user.getId()))
			return true;
		return false;
	}
	
	public void setLabel(String label){
		this.label = label;
	}
	public String getLabel(){
		return label;
	}
	public void setGroup(PermissionGroup group){
		this.group = group.getId();
	}
	public Long getGroup(){
		return group;
	}
}
