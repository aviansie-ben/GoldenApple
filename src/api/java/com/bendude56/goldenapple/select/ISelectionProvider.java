package com.bendude56.goldenapple.select;

import org.bukkit.Location;
import org.bukkit.World;

import com.bendude56.goldenapple.User;

public interface ISelectionProvider {
    public String getProviderName();
    
    public boolean isSelectionMade(User user);
    public Location getSelectionMinimum(User user);
    public Location getSelectionMaximum(User user);
    public World getSelectionWorld(User user);
}
