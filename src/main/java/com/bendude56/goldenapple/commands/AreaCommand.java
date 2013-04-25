package com.bendude56.goldenapple.commands;

import java.util.List;
import java.util.ArrayList;

import org.bukkit.entity.Player;

import com.bendude56.goldenapple.GoldenApple;
import com.bendude56.goldenapple.User;
import com.bendude56.goldenapple.area.Area;
import com.bendude56.goldenapple.area.AreaManager.AreaType;
import com.bendude56.goldenapple.area.ChildArea;
import com.bendude56.goldenapple.area.PrivateArea;
import com.bendude56.goldenapple.permissions.PermissionUser;

public class AreaCommand extends DualSyntaxCommand
{

	@Override
	public void onCommandComplex(GoldenApple instance, User user, String commandLabel, String[] args)
	{
		Arguments arguments = new Arguments(args);
		boolean verified;
		
		Long selectedId = null;
		Integer page;
		String username;
		AreaType type;
		List<AreaType> types = new ArrayList<AreaType>();
		
		
		
		if (arguments.contains("v"))
			verified = true;
		else
			verified = false;
		
		// Page selection
		page = arguments.getInteger("page");
		if (page == null)
			page = arguments.getInteger("pg");
		if (page == null)
			page = arguments.getInteger("p");
		if (page == null)
			page = 1;
		
		
		// Lot selection
		selectedId = arguments.getLong("lot");
		if (selectedId == null)
			selectedId = arguments.getLong("l");
		if (selectedId == null)
			selectedId = arguments.getLong("sel");
		if (selectedId == null)
			selectedId = arguments.getLong("select");
		if (selectedId == null)
			selectedId = arguments.getLong("s");
		if (selectedId == null)
		{
			List<Area> areas = GoldenApple.getInstance().areas.getAreasAtLocation(user.getPlayerHandle().getLocation(), true);
			if (areas.size() == 1)
				selectedId = areas.get(0).getID();
		}
		
		
		username = arguments.getString("owner");
		if (username == null)
			username = arguments.getString("own");
		if (username == null)
			username = arguments.getString("o");
		if (username == null)
			username = arguments.getString("name");
		if (username == null)
			username = arguments.getString("n");
		if (username == null)
		
		
		// Types filter
		if (arguments.contains("child"))
			types.add(AreaType.CHILD);
		if (arguments.contains("private") || arguments.contains("priv") || arguments.contains("lot"))
			types.add(AreaType.PRIVATE);
		if (arguments.contains("pvp"))
			types.add(AreaType.PVP);
		if (arguments.contains("safety") || arguments.contains("safe"))
			types.add(AreaType.SAFETY);
		if (arguments.contains("town"))
			types.add(AreaType.TOWN);
		
		
		
		
		
		
		// Check for different action commands
		if (args.length == 0 || arguments.contains("?") || arguments.contains("help"))
		{
			sendHelp(user, commandLabel, page, false);
			return;
		}
		else if (arguments.contains("list") || arguments.contains("ls"))
		{
			sendAreaList(user, page);
			return;
		}
		else if (arguments.contains("new") || arguments.contains("create"))
		{
			type = AreaType.fromString("new");
			if (type == null)
				type = AreaType.fromString("create");
			if (type == null)
			{
				if (types.size() == 0)
				{
					user.sendLocalizedMessage("error.area.chooseType", true);
					return;
				}
				else if (types.size() == 1)
				{
					type = types.get(0);
				}
				else
				{
					user.sendLocalizedMessage("error.area.tooManyTypes", true);
					return;
				}
			}
			
			
			// Get the user's selected corners
			
			
			// Create a new area of given type at the selected corners
			
			
		}
		else if (arguments.contains("extend") || arguments.contains("ext") || arguments.contains("ex") || arguments.contains("expand") || arguments.contains("add"))
		{
			Area parent;
			
			if (selectedId == null)
			{
				user.sendLocalizedMessage("error.area.chooseArea", true);
				return;
			}
			parent = instance.areas.getArea(selectedId);
			if (parent == null)
			{
				user.sendLocalizedMessage("error.area.invalidId", true);
				return;
			}
			else if (parent.getType() == AreaType.CHILD)
			{
				parent = ((ChildArea) parent).getParent();
			}
			
			// Get the user's selected corners
			
			
			// Create a new child area at the selected corners, whose parent is "parent";
			
			
		}
		else if (arguments.contains("delete") || arguments.contains("del") || arguments.contains("remove") || arguments.contains("rem"))
		{
			Area area;
			
			if (selectedId == null)
			{
				user.sendLocalizedMessage("error.area.chooseArea", true);
				return;
			}
			area = instance.areas.getArea(selectedId);
			if (area == null)
			{
				user.sendLocalizedMessage("error.area.invalidId", true);
				return;
			}
			
			// Verify the user's selection
			
			// Deletes the specified area after verification
			
			
		}
		else if (arguments.contains("edit"))
		{
			Area area;

			if (selectedId == null)
			{
				user.sendLocalizedMessage("error.area.chooseArea", true);
				return;
			}
			area = instance.areas.getArea(selectedId);
			if (area == null)
			{
				user.sendLocalizedMessage("error.area.invalidId", true);
				return;
			}
			
			
			// Change the owner of the private area
			if (username != null && !username.isEmpty())
			{
				PermissionUser u = PermissionManager.getInstance().getUser(username);
				if (u == null)
				{
					// Send invalid user message
					return;
				}
				if (area.getType() == AreaType.CHILD)
				{
					if (((ChildArea) area).getParent().getType() == AreaType.PRIVATE)
					{
						((PrivateArea) ((ChildArea) area).getParent()).setOwner(u);
						// Send message: owner change
					}
				}
				else if (area.getType() == AreaType.PRIVATE)
				{
					((PrivateArea) area).setOwner(u);
				}
			}
			
			// Add guests to private area
			if (arguments.contains("addguests") || arguments.contains("addg"))
			{
				int index;
				for (index = 0; index < args.length; index++)
				{
					if (args[index].contains("addg"))
						break;
				}
				
				Area temp_area = area;
				if (temp_area.getType() == AreaType.CHILD)
				{
					if (((ChildArea) temp_area).getParent().getType() == AreaType.PRIVATE)
						area = ((ChildArea) temp_area).getParent();
				}
				
				if (temp_area.getType() == AreaType.PRIVATE)
				{
					for (index++; index < args.length && !args[index].startsWith("-"); index++)
					{
						PermissionUser u = PermissionManager.getInstance().getUser(args[index]);
						
						if (u == null)
						{
							// send message: No user by that name
						}
						else
						{
							((PrivateArea) area).addGuest(u);
							// Send message: guest added
						}
					}
				}
			}
			
			// Remove guests from a private area
			if (arguments.contains("remguests") || arguments.contains("remg"))
			{
				int index;
				for (index = 0; index < args.length; index++)
				{
					if (args[index].contains("remg"))
						break;
				}
				
				Area temp_area = area;
				if (temp_area.getType() == AreaType.CHILD)
				{
					if (((ChildArea) temp_area).getParent().getType() == AreaType.PRIVATE)
						area = ((ChildArea) temp_area).getParent();
				}
				
				if (temp_area.getType() == AreaType.PRIVATE)
				{
					for (index++; index < args.length && !args[index].startsWith("-"); index++)
					{
						PermissionUser u = PermissionManager.getInstance().getUser(args[index]);
						
						if (u == null)
						{
							// send message: No user by that name
						}
						else
						{
							((PrivateArea) area).remGuest(u);
							// Send message: guest added
						}
					}
				}
			}
			
			
			// Edit ignoreY
			if (arguments.contains("ignorey"))
			{
				area.ignoreY(arguments.getBoolean("ignorey"));
				// Send message: ignore y set.
			}
			
			
			// Move the area
			if (arguments.contains("move") || arguments.contains("relocate"))
			{
				// Change the area's coordinates to whatever the player has selected.
			}
			
			
			
			
		}
		
	}
	
	
	
	@Override
	public void onCommandSimple(GoldenApple instance, User user, String commandLabel, String[] args)
	{
		// TODO Auto-generated method stub
		
	}
	
	/**
	 * 
	 * @param user
	 * @param commandLabel
	 * @param page
	 * @param b
	 */
	private void sendHelp(User user, String commandLabel, int page, boolean b)
	{
		// TODO Auto-generated method stub
		
	}
	
	private void sendAreaList(User user, int page)
	{
		if (page < 0)
		{
			return;
		}
		
		final int per_page = 20;
		
		List<Area> areas = GoldenApple.getInstance().areas.getAllAreas(false);
		
		if (page > Math.ceil(((double) areas.size())/per_page))
		{
			return;
		}
		
		areas = areas.subList(page*per_page, page*per_page+20 >= areas.size() ? areas.size() - 1 : page*per_page + 20);
		
		String message = new String();
		
		for (Area area : areas)
		{
			message += area.getID() + ", ";
		}
		
		user.getHandle().sendMessage(message);
	}
}
