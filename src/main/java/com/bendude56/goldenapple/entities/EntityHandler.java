package com.bendude56.goldenapple.entities;

import java.util.ArrayList;
import java.util.List;

import org.bukkit.DyeColor;
import org.bukkit.Location;
import org.bukkit.entity.Creeper;
import org.bukkit.entity.Damageable;
import org.bukkit.entity.Entity;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.entity.Sheep;

import com.bendude56.goldenapple.util.Calculations;
import com.bendude56.goldenapple.util.Constants;

/**
 * This class is meant to handle basic mob and entity functions. This includes
 * spawning mobs, killing mobs, and handling targeting events.
 * 
 * @author Deaboy
 * 
 */
public class EntityHandler {
    
    public boolean isHostile(Entity entity) {
        return Constants.getHostileMobs().contains(entity.getType());
    }
    
    public boolean isPassive(Entity entity) {
        return Constants.getPassiveMobs().contains(entity.getType());
    }
    
    /**
     * This method is used to gather a list of all hostile mobs in an area.
     * 
     * @param loc The center point of the search
     * @param range The range to include mobs
     * @param ignoreY Whether to ignore height or not
     * @return An ArrayList<Entity> of all hostile mobs within the range.
     */
    public List<Entity> getHostileMobsInArea(Location loc, int range, boolean ignoreY) {
        List<Entity> mobs = new ArrayList<Entity>();
        for (Entity mob : loc.getWorld().getEntities()) {
            if (Calculations.getDistance(loc, mob.getLocation(), ignoreY) <= range && isHostile(mob)) {
                mobs.add(mob);
            }
        }
        return mobs;
    }
    
    /**
     * This method is used to gather a list of all passive mobs in an area.
     * 
     * @param loc The center point of the search
     * @param range The range to include mobs
     * @param ignoreY Whether to ignore height or not
     * @return Returns an ArrayList<Entity> of all passive mobs within the
     * range.
     */
    public List<Entity> getPassiveMobsInArea(Location loc, int range, boolean ignoreY) {
        List<Entity> mobs = new ArrayList<Entity>();
        for (Entity mob : loc.getWorld().getEntities()) {
            if (Calculations.getDistance(loc, mob.getLocation(), ignoreY) <= range && isPassive(mob)) {
                mobs.add(mob);
            }
        }
        return mobs;
    }
    
    /**
     * This method is used to gather a list of all living mobs in an area.
     * 
     * @param loc The center point of the search
     * @param range The range to include mobs
     * @param ignoreY Whether to ignore height or not
     * @return Returns an ArrayList<Entity> of all living mobs within the range.
     */
    public List<Entity> getAllMobsInArea(Location loc, int range, boolean ignoreY) {
        List<Entity> mobs = new ArrayList<Entity>();
        for (Entity mob : loc.getWorld().getEntities()) {
            if (Calculations.getDistance(loc, mob.getLocation(), ignoreY) <= range && (isPassive(mob) || isHostile(mob))) {
                mobs.add(mob);
            }
        }
        return mobs;
    }
    
    /**
     * This method gets a string and returns an EntityType for which it has
     * found a reasonable match.
     * 
     * @param name The name of the mob
     * @return EntityType of matched mob. Returns null of no match is found.
     */
    public EntityType getMobByName(String name) {
        name.toLowerCase();
        name.replaceAll("_", "");
        if (name.equals("zombie")) {
            return EntityType.ZOMBIE;
        } else if (name.equals("creeper") || name.equals("chargedcreeper") || name.equals("supercreeper")) {
            return EntityType.CREEPER;
        } else if (name.equals("skeleton") || name.equals("skele")) {
            return EntityType.SKELETON;
        } else if (name.equals("spider")) {
            return EntityType.SPIDER;
        } else if (name.equals("slime")) {
            return EntityType.SLIME;
        } else if (name.equals("cavespider") || name.equals("bluespider") || name.equals("smallspider") || name.equals("smallspider")) {
            return EntityType.CAVE_SPIDER;
        } else if (name.equals("silverfish")) {
            return EntityType.SILVERFISH;
        } else if (name.equals("zombiepigman") || name.equals("pigzombie")) {
            return EntityType.PIG_ZOMBIE;
        } else if (name.equals("ghast")) {
            return EntityType.GHAST;
        } else if (name.equals("magmaqube") || name.equals("lavaslime") || name.equals("magmaslime")) {
            return EntityType.MAGMA_CUBE;
        } else if (name.equals("Blaze")) {
            return EntityType.BLAZE;
        } else if (name.equals("enderman")) {
            return EntityType.ENDERMAN;
        } else if (name.equals("enderdragon") || name.equals("dragon")) {
            return EntityType.ENDER_DRAGON;
        } else if (name.equals("pig")) {
            return EntityType.PIG;
        } else if (name.equals("sheep")) {
            return EntityType.SHEEP;
        } else if (name.equals("cow")) {
            return EntityType.COW;
        } else if (name.equals("chicken")) {
            return EntityType.CHICKEN;
        } else if (name.equals("squid")) {
            return EntityType.SQUID;
        } else if (name.equals("wolf") || name.equals("cat")) {
            return EntityType.WOLF;
        } else if (name.equals("cat") || name.equals("ocelot")) {
            return EntityType.OCELOT;
        } else if (name.equals("snowgolem") || name.equals("snowman")) {
            return EntityType.SNOWMAN;
        } else if (name.equals("irongolem") || name.equals("ironman")) {
            return EntityType.IRON_GOLEM;
        } else if (name.equals("villager") || name.equals("testificate") || name.equals("npc")) {
            return EntityType.VILLAGER;
        } else if (name.equals("mooshroom") || name.equals("mushroomcow")) {
            return EntityType.MUSHROOM_COW;
        } else {
            return null;
        }
    }
    
    /**
     * This method spawns mobs stacked on top of each other repeatedly. Designed
     * to work with the /spawnmob command.
     * 
     * @param loc The location at which to spawn the mobs
     * @param args Arguments representing the mobs to spawn
     * @param amount The number of mobs to spawn at the specified location
     * @return returns The number of mobs that were successfully spawned
     */
    public int spawnMobs(Location loc, String[] args, int amount) {
        int counter = 0;
        while (amount > 0) {
            amount--;
            LivingEntity lastMob = null;
            
            for (int i = 0; i < args.length; i++) {
                EntityType type = getMobByName(args[i].split(":")[0]);
                if (type != null) {
                    LivingEntity mob = (LivingEntity) loc.getWorld().spawnEntity(loc, type);
                    counter++;
                    if (mob.getType() == EntityType.CREEPER && (args[i].equalsIgnoreCase("supercreeper") || args[i].equalsIgnoreCase("chargedcreeper"))) {
                        ((Creeper) mob).setPowered(true);
                    } else if (mob.getType() == EntityType.SHEEP && (args[i].split(":").length > 1)) {
                        ((Sheep) mob).setColor(DyeColor.valueOf(args[i].split(";")[1]));
                    }
                    if (lastMob != null) {
                        lastMob.setPassenger(mob);
                    }
                    lastMob = mob;
                }
            }
        }
        return counter;
    }
    
    /**
     * This method kills mobs within a certain range of the specified location
     * while causing mobs to drop loot and experience
     * 
     * @param loc The epicenter
     * @param args The mobs to kill
     * @param range The range from the epicenter
     * @param ignoreY Ignore the y-coordinate while searching
     * @param killer The player that was the killer
     * @return Returns an int of the mobs killed
     */
    public int killMobs(Location loc, String[] args, int range, boolean ignoreY, Player killer) {
        int counter = 0;
        List<EntityType> types = new ArrayList<EntityType>();
        for (String name : args) {
            if (getMobByName(name) != null) {
                types.add(getMobByName(name));
            }
        }
        List<Entity> mobs = getAllMobsInArea(loc, range, ignoreY);
        
        for (Entity mob : mobs) {
            if (types.contains(mob.getType())) {
                if (mob instanceof LivingEntity && killer != null) {
                    ((LivingEntity) mob).damage(((Damageable) mob).getHealth(), killer);
                } else {
                    mob.remove();
                }
                counter++;
            }
        }
        return counter;
    }
    
    /**
     * This method kills mobs within a certain range of the specified location
     * 
     * @param loc The epicenter
     * @param args The mobs to kill
     * @param range The range from the epicenter
     * @param ignoreY Ignore the y-coordinate while searching
     * @return Returns an int of the mobs killed
     */
    public int killMobs(Location loc, String[] args, int range, boolean ignoreY) {
        return killMobs(loc, args, range, ignoreY, null);
    }
}
