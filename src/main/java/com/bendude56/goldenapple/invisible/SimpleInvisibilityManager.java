package com.bendude56.goldenapple.invisible;

import java.util.HashMap;
import java.util.HashSet;

import org.bukkit.Bukkit;
import org.bukkit.entity.Player;

import com.bendude56.goldenapple.User;

public class SimpleInvisibilityManager extends InvisibilityManager {
    private HashMap<User, HashSet<String>> invisibleUsers;
    private HashSet<User> allSeeingUsers;
    
    public SimpleInvisibilityManager() {
        invisibleUsers = new HashMap<User, HashSet<String>>();
        allSeeingUsers = new HashSet<User>();
    }
    
    @Override
    public void setInvisible(User user, boolean invisible) {
        if (invisible) {
            makeInvisible(user);
            invisibleUsers.put(user, new HashSet<String>());
        } else {
            makeVisible(user);
            invisibleUsers.remove(user);
        }
    }
    
    @Override
    public boolean isInvisible(User user) {
        return invisibleUsers.containsKey(user);
    }
    
    @Override
    public void setInvisibilityFlag(User user, String flag, boolean value) {
        if (!invisibleUsers.containsKey(user)) {
            throw new IllegalArgumentException("Cannot set invisibility flags on user who is not invisible!");
        } else if (value) {
            invisibleUsers.get(user).add(flag);
        } else {
            invisibleUsers.get(user).remove(flag);
        }
    }
    
    @Override
    public boolean isInvisibilityFlagSet(User user, String flag) {
        if (invisibleUsers.containsKey(user)) {
            return invisibleUsers.get(user).contains(flag);
        } else {
            return false;
        }
    }
    
    @Override
    public void setAllSeeing(User user, boolean allSeeing) {
        if (allSeeing) {
            makeAllSeeing(user);
            allSeeingUsers.add(user);
        } else {
            makeNonAllSeeing(user);
            allSeeingUsers.remove(user);
        }
    }
    
    @Override
    public boolean isAllSeeing(User user) {
        return allSeeingUsers.contains(user);
    }
    
    private void makeInvisible(User user) {
        for (Player p : Bukkit.getOnlinePlayers()) {
            if (!isAllSeeing(User.getUser(p))) {
                p.hidePlayer(user.getPlayerHandle());
            }
        }
    }
    
    private void makeVisible(User user) {
        for (Player p : Bukkit.getOnlinePlayers()) {
            p.showPlayer(user.getPlayerHandle());
        }
    }
    
    private void makeAllSeeing(User user) {
        for (User invisibleUser : invisibleUsers.keySet()) {
            user.getPlayerHandle().showPlayer(invisibleUser.getPlayerHandle());
        }
    }
    
    private void makeNonAllSeeing(User user) {
        for (User invisibleUser : invisibleUsers.keySet()) {
            user.getPlayerHandle().hidePlayer(invisibleUser.getPlayerHandle());
        }
    }
}
