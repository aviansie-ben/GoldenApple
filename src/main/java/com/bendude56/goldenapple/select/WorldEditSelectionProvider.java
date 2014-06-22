package com.bendude56.goldenapple.select;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.World;

import com.bendude56.goldenapple.User;
import com.sk89q.worldedit.bukkit.WorldEditPlugin;
import com.sk89q.worldedit.bukkit.selections.CuboidSelection;

public class WorldEditSelectionProvider implements ISelectionProvider {
    
    public WorldEditSelectionProvider() {
        if (!Bukkit.getPluginManager().isPluginEnabled("WorldEdit")) {
            throw new IllegalStateException("WorldEdit not found!");
        }
    }
    
    @Override
    public String getProviderName() {
        return "WorldEdit";
    }
    
    @Override
    public boolean isSelectionMade(User user) {
        return ((WorldEditPlugin) Bukkit.getPluginManager().getPlugin("WorldEdit")).getSelection(user.getPlayerHandle()) instanceof CuboidSelection;
    }
    
    @Override
    public Location getSelectionMinimum(User user) {
        return ((WorldEditPlugin) Bukkit.getPluginManager().getPlugin("WorldEdit")).getSelection(user.getPlayerHandle()).getMinimumPoint();
    }
    
    @Override
    public Location getSelectionMaximum(User user) {
        return ((WorldEditPlugin) Bukkit.getPluginManager().getPlugin("WorldEdit")).getSelection(user.getPlayerHandle()).getMaximumPoint();
    }
    
    @Override
    public World getSelectionWorld(User user) {
        return ((WorldEditPlugin) Bukkit.getPluginManager().getPlugin("WorldEdit")).getSelection(user.getPlayerHandle()).getWorld();
    }
    
}
