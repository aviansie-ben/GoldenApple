package com.bendude56.goldenapple.select;

import java.util.logging.Level;

import org.bukkit.Location;
import org.bukkit.World;

import com.bendude56.goldenapple.GoldenApple;
import com.bendude56.goldenapple.User;

public class SimpleSelectManager extends SelectManager {
    private ISelectionProvider provider;
    
    public SimpleSelectManager() {
        initSelectionProvider();
    }
    
    private void initSelectionProvider() {
        String providerName = GoldenApple.getInstanceMainConfig().getString("modules.select.provider", "builtin");
        try {
            initSelectionProvider(providerName);
        } catch (Exception e) {
            GoldenApple.log(Level.SEVERE, "Failed to load selection provider '" + providerName + "'. Falling back to 'builtin'!");
            GoldenApple.log(Level.SEVERE, e);
            
            try {
                initSelectionProvider("builtin");
            } catch (Exception e2) {
                throw new RuntimeException(e2);
            }
        }
    }
    
    private void initSelectionProvider(String provider) throws InstantiationException, IllegalAccessException, ClassNotFoundException {
        if (provider.equalsIgnoreCase("worldedit")) {
            setSelectionProvider((ISelectionProvider) Class.forName("com.bendude56.goldenapple.select.WorldEditSelectionProvider").newInstance());
        } else if (provider.equalsIgnoreCase("builtin")) {
            setSelectionProvider((ISelectionProvider) Class.forName("com.bendude56.goldenapple.select.SimpleSelectionProvider").newInstance());
        } else {
            throw new IllegalArgumentException("Cannot find selection provider '" + provider + "'");
        }
    }
    
    @Override
    public ISelectionProvider getSelectionProvider() {
        return provider;
    }
    
    @Override
    public void setSelectionProvider(ISelectionProvider provider) {
        this.provider = provider;
    }
    
    @Override
    public boolean isSelectionMade(User user) {
        return provider.isSelectionMade(user);
    }
    
    @Override
    public Location getSelectionMinimum(User user) {
        return provider.getSelectionMinimum(user);
    }
    
    @Override
    public Location getSelectionMaximum(User user) {
        return provider.getSelectionMaximum(user);
    }
    
    @Override
    public World getSelectionWorld(User user) {
        return provider.getSelectionWorld(user);
    }
    
}
