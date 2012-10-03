package com.bendude56.goldenapple.commands;

import java.util.HashMap;
import java.util.List;

import org.bukkit.entity.Player;

import com.bendude56.goldenapple.GoldenApple;
import com.bendude56.goldenapple.User;
import com.bendude56.goldenapple.area.Area;
import com.bendude56.goldenapple.util.CommandUtil;

public class AreaCommand extends DualSyntaxCommand
{

	@Override
	public void onCommandComplex(GoldenApple instance, User user, String commandLabel, String[] args)
	{
		long selectedId = -1;
		if (user.getHandle() instanceof Player)
		{
			
		}
		
		if (args.length == 0 || args[0].equals("-?") || args[0].equals("help"))
		{
			sendHelp(user, commandLabel, true);
			return;
		}
		
		instance.locale.sendMessage(user, "header.area", false);
		
		HashMap<String, String> arguments = CommandUtil.parseArguments(args);
		
		if (arguments.containsKey("-l")
				|| arguments.containsKey("-ls")
				|| arguments.containsKey("-list"))
		{
			int page;
			try
			{
				if (arguments.containsKey("p"))
					page = Integer.parseInt(arguments.get("p"));
				else if (arguments.containsKey("pg"))
					page = Integer.parseInt(arguments.get("pg"));
				else if (arguments.containsKey("page"))
					page = Integer.parseInt(arguments.get("page"));
				else
					page = 0;
			}
			catch (NumberFormatException e)
			{
				page = 0;
			}
			
			sendAreaList(user, page);
			
			return;
		}
		
		if (arguments.containsKey("s"))
		{
			try
			{
				selectedId = Long.parseLong(arguments.get("s"));
			}
			catch (NumberFormatException | ArrayIndexOutOfBoundsException e)
			{
				instance.locale.sendMessage(user,  "shared.parameterMissing",  false, "-s");
			}
			
			/*
			try
			{
			*/
				if (selectedId < 0 || !instance.areas.areaExists(selectedId))
				{
					instance.locale.sendMessage(user, "error.area.selectNotFound", false, String.valueOf(selectedId));
					return;
				}
				else if (args.length == 2)
				{
					instance.locale.sendMessage(user, "error.area.selectNoAction", false);
					return;
				}
			/*
			}
			
			catch (SQLException e)
			{
				instance.locale.sendMessage(user, "error.area.selectNotFound", false, String.valueOf(selectedId));
				return;
			}
			*/
		}
		
		else if (instance.areas.getAreasAtLocation(((Player) user.getHandle()).getLocation(), true).isEmpty())
		{
			instance.locale.sendMessage(user, "error.area.noArea", false);
		}
		
	}
	@Override
	public void onCommandSimple(GoldenApple instance, User user, String commandLabel, String[] args)
	{
		// TODO Auto-generated method stub
		
	}
	
	private void sendHelp(User user, String commandLabel, boolean b) {
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
