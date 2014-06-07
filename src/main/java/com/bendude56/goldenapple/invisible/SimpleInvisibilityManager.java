package com.bendude56.goldenapple.invisible;

import java.util.HashSet;

import org.bukkit.Bukkit;
import org.bukkit.entity.Player;

import com.bendude56.goldenapple.User;

public class SimpleInvisibilityManager extends InvisibilityManager {
    private HashSet<User> invisibleUsers;
    private HashSet<User> noInteractionUsers;
    private HashSet<User> allSeeingUsers;
    
    public SimpleInvisibilityManager() {
        invisibleUsers = new HashSet<User>();
        noInteractionUsers = new HashSet<User>();
        allSeeingUsers = new HashSet<User>();
    }
    
    @Override
    public void setInvisible(User user, boolean invisible) {
        if (invisible) {
            makeInvisible(user);
            invisibleUsers.add(user);
        } else {
            makeVisible(user);
            invisibleUsers.remove(user);
        }
    }
    
    @Override
    public boolean isInvisible(User user) {
        return invisibleUsers.contains(user);
    }
    
    @Override
    public void setInteractionEnabled(User user, boolean interact) {
        if (interact) {
            noInteractionUsers.remove(user);
        } else {
            noInteractionUsers.add(user);
        }
    }
    
    @Override
    public boolean isInteractionEnabled(User user) {
        return !noInteractionUsers.contains(user);
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
        for (User invisibleUser : invisibleUsers) {
            user.getPlayerHandle().showPlayer(invisibleUser.getPlayerHandle());
        }
    }
    
    private void makeNonAllSeeing(User user) {
        for (User invisibleUser : invisibleUsers) {
            user.getPlayerHandle().hidePlayer(invisibleUser.getPlayerHandle());
        }
    }
}
