package com.bendude56.goldenapple.punish;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.logging.Level;

import org.bukkit.Bukkit;
import org.bukkit.entity.Player;

import com.bendude56.goldenapple.GoldenApple;
import com.bendude56.goldenapple.User;
import com.bendude56.goldenapple.chat.IChatChannel;
import com.bendude56.goldenapple.permissions.IPermissionUser;
import com.bendude56.goldenapple.punish.Punishment.RemainingTime;

public class SimplePunishmentManager extends PunishmentManager {
    private HashMap<Long, ArrayList<Punishment>> cache;
    
    public SimplePunishmentManager() {
        GoldenApple.getInstanceDatabaseManager().createOrUpdateTable("bans");
        GoldenApple.getInstanceDatabaseManager().createOrUpdateTable("mutes");
        GoldenApple.getInstanceDatabaseManager().createOrUpdateTable("warnings");
        
        cache = new HashMap<Long, ArrayList<Punishment>>();
        for (Player p : Bukkit.getOnlinePlayers()) {
            loadIntoCache(User.getUser(p));
        }
    }
    
    public int getLookupCacheCurrentSize() {
        return cache.size();
    }
    
    @Override
    public void loadIntoCache(IPermissionUser u) {
        if (cache.containsKey(u.getId())) {
            cache.get(u.getId()).clear();
        } else {
            cache.put(u.getId(), new ArrayList<Punishment>());
        }
        try {
            ResultSet r = GoldenApple.getInstanceDatabaseManager().executeQuery("SELECT * FROM Bans WHERE Target=?", u.getId());
            try {
                while (r.next()) {
                    cache.get(u.getId()).add(new SimplePunishmentBan(r));
                }
            } finally {
                GoldenApple.getInstanceDatabaseManager().closeResult(r);
            }
            r = GoldenApple.getInstanceDatabaseManager().executeQuery("SELECT * FROM Mutes WHERE Target=?", u.getId());
            try {
                while (r.next()) {
                    cache.get(u.getId()).add(new SimplePunishmentMute(r));
                }
            } finally {
                GoldenApple.getInstanceDatabaseManager().closeResult(r);
            }
            r = GoldenApple.getInstanceDatabaseManager().executeQuery("SELECT * FROM Warnings WHERE Target=?", u.getId());
            try {
                while (r.next()) {
                    cache.get(u.getId()).add(new SimplePunishmentWarning(r));
                }
            } finally {
                GoldenApple.getInstanceDatabaseManager().closeResult(r);
            }
        } catch (SQLException e) {
            GoldenApple.log(Level.SEVERE, "Error encountered while loading punishments for " + u.getName() + ":");
            GoldenApple.log(Level.SEVERE, e);
        }
        
        Collections.sort(cache.get(u.getId()), Collections.reverseOrder(new PunishmentTimeComparator()));
    }
    
    @Override
    public void unloadFromCache(IPermissionUser u) {
        cache.remove(u.getId());
    }
    
    @Override
    public void addPunishment(Punishment p, IPermissionUser u) {
        if (cache.containsKey(u.getId())) {
            int i = Collections.binarySearch(cache.get(u.getId()), p, Collections.reverseOrder(new PunishmentTimeComparator()));
            
            if (i < 0)
                i = -i - 1;
            
            cache.get(u.getId()).add(i, p);
        }
        
        p.insert();
    }
    
    @Override
    public void addMute(IPermissionUser target, IPermissionUser admin, String reason, RemainingTime duration, String channel) {
        addPunishment(new SimplePunishmentMute(target, admin, reason, duration, channel), target);
    }
    
    @Override
    public void addBan(IPermissionUser target, IPermissionUser admin, String reason, RemainingTime duration) {
        addPunishment(new SimplePunishmentBan(target, admin, reason, duration), target);
    }
    
    @Override
    public void addWarning(IPermissionUser target, IPermissionUser admin, String reason) {
        addPunishment(new SimplePunishmentWarning(target, admin, reason), target);
    }
    
    @Override
    public void purgePunishment(Punishment p) {
        p.delete();
        
        if (cache.containsKey(p.getTargetId())) {
            cache.get(p.getTargetId()).remove(p);
        }
    }
    
    @Override
    public boolean isMuted(IPermissionUser u, IChatChannel channel) {
        return (getActiveMute(u, null) != null) || (getActiveMute(u, channel) != null);
    }
    
    @Override
    public PunishmentMute getActiveMute(IPermissionUser u, IChatChannel channel) {
        for (Punishment p : getPunishments(u, SimplePunishmentMute.class)) {
            SimplePunishmentMute m = (SimplePunishmentMute) p;
            
            if (m.isExpired()) {
                continue;
            }
            
            if (channel == null && m.isGlobal()) {
                return m;
            } else if (channel != null && (m.isGlobal() || m.getChannelIdentifier().equals(channel.getName()))) {
                return m;
            }
        }
        return null;
    }
    
    @Override
    public Punishment getActivePunishment(IPermissionUser u, Class<? extends Punishment> punishmentType) {
        ArrayList<Punishment> punish = getPunishments(u, punishmentType);
        
        for (Punishment p : punish) {
            if (!p.isExpired()) {
                return p;
            }
        }
        return null;
    }
    
    @Override
    public boolean hasActivePunishment(IPermissionUser u, Class<? extends Punishment> punishmentType) {
        return getActivePunishment(u, punishmentType) != null;
    }
    
    @Override
    public ArrayList<Punishment> getPunishments(IPermissionUser u, Class<? extends Punishment> punishmentType) {
        boolean unloadCache = !cache.containsKey(u.getId());
        
        if (unloadCache) {
            loadIntoCache(u);
        }
        
        try {
            ArrayList<Punishment> punishments = new ArrayList<Punishment>();
            for (Punishment p : cache.get(u.getId())) {
                if (punishmentType.isInstance(p)) {
                    punishments.add(p);
                }
            }
            return punishments;
        } finally {
            if (unloadCache) {
                unloadFromCache(u);
            }
        }
    }
    
    @Override
    public void clearCache() {
        cache.clear();
    }
}
