package com.deaboy.goldenapple.permissions;

import java.util.ArrayList;
import java.util.List;

public class Group {
	
	private String name;
	private List<User> members = new ArrayList<User>();
	
	
		
	// -- SIMPLE AND ROUTINE METHODS AND FUNCTIONS -- //
	
	public String getName() {
		return name;
	}
	
	public List<User> getMembers() {
		return members;
	}
}