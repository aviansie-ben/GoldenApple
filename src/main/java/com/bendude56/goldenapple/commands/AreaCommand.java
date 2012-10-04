package com.bendude56.goldenapple.commands;

import java.util.List;

import org.bukkit.entity.Player;

import com.bendude56.goldenapple.GoldenApple;
import com.bendude56.goldenapple.User;
import com.bendude56.goldenapple.area.Area;

public class AreaCommand extends DualSyntaxCommand
{

	@Override
	public void onCommandComplex(GoldenApple instance, User user, String commandLabel, String[] args)
	{
		Arguments arguments = new Arguments(args);
		
		Long selectedId = null;
		
		if (user.getHandle() instanceof Player)
		{
			
		}
		
		if (args.length == 0 || args[0].equals("-?") || args[0].equals("help"))
		{
			sendHelp(user, commandLabel, true);
			return;
		}
		
		instance.locale.sendMessage(user, "header.area", false);
		
		// LIST command. Exclusive: if command contains any version of "list" will
		//      return after executing the command
		
		if (arguments.getBoolean("-l")
				|| arguments.getBoolean("-ls")
				|| arguments.getBoolean("-list"))
		{
			Integer page;
			if ((page = arguments.getInteger("p")) == null
					&& (page = arguments.getInteger("pg")) == null
					&& (page = arguments.getInteger("page")) == null)
			{
				page = 1;
			}
			
			sendAreaList(user, page - 1);
			
			return;
		}
		
		if (arguments.contains("s"))
		{
			selectedId = arguments.getLong("s");
			
			if (selectedId == null)
			{
				instance.locale.sendMessage(user,  "shared.parameterMissing",  false, "-s");
				return;
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
			return;
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
