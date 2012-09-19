package com.bendude56.goldenapple.commands;

import java.sql.SQLException;

import org.bukkit.entity.Player;

import com.bendude56.goldenapple.GoldenApple;
import com.bendude56.goldenapple.User;

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
		
		int arg = 0;
		
		if (args[0].equalsIgnoreCase("-s"))
		{
			try
			{
				selectedId = Long.parseLong(args[1]);
				arg = 2;
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
		else if (args[0].equalsIgnoreCase("-list") || args[0].equalsIgnoreCase("-ls"))
		{
			// TODO List areas.
		}
		else if (instance.areas.getAreasAtLocation(((Player) user.getHandle()).getLocation(), true).isEmpty())
		{
			instance.locale.sendMessage(user, "error.area.noArea", false);
		}
		
	}
	@Override
	public void onCommandSimple(GoldenApple instance, User user,
			String commandLabel, String[] args) {
		// TODO Auto-generated method stub
		
	}
	
	private void sendHelp(User user, String commandLabel, boolean b) {
		// TODO Auto-generated method stub
		
	}
}
