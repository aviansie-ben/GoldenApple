package com.bendude56.goldenapple.util;

import java.util.ArrayList;
import java.util.List;

import org.bukkit.entity.EntityType;

public class Constants {
	private static final List<EntityType> hostileMobs;
	private static final List<EntityType> passiveMobs;
	
	public static List<EntityType> getHostileMobs() {
		return hostileMobs;
	}
	
	public static List<EntityType> getPassiveMobs() {
		return passiveMobs;
	}
	
	static {
		hostileMobs = new ArrayList<EntityType>();
		initHostileMobs();
		passiveMobs = new ArrayList<EntityType>();
		initPassiveMobs();
	}
	
	private static void initHostileMobs() {
		// Overworld mobs
		hostileMobs.add(EntityType.ZOMBIE);
		hostileMobs.add(EntityType.CREEPER);
		hostileMobs.add(EntityType.SKELETON);
		hostileMobs.add(EntityType.SPIDER);
		hostileMobs.add(EntityType.SLIME);
		hostileMobs.add(EntityType.CAVE_SPIDER);
		hostileMobs.add(EntityType.SILVERFISH);
		
		// Nether mobs
		hostileMobs.add(EntityType.PIG_ZOMBIE);
		hostileMobs.add(EntityType.GHAST);
		hostileMobs.add(EntityType.MAGMA_CUBE);
		hostileMobs.add(EntityType.BLAZE);
		
		// End mobs
		hostileMobs.add(EntityType.ENDERMAN);
		hostileMobs.add(EntityType.ENDER_DRAGON);
	}
	private static void initPassiveMobs() {
		passiveMobs.add(EntityType.PIG);
		passiveMobs.add(EntityType.SHEEP);
		passiveMobs.add(EntityType.COW);
		passiveMobs.add(EntityType.CHICKEN);
		passiveMobs.add(EntityType.WOLF);
		passiveMobs.add(EntityType.OCELOT);
		passiveMobs.add(EntityType.SQUID);
		passiveMobs.add(EntityType.IRON_GOLEM);
		passiveMobs.add(EntityType.SNOWMAN);
		passiveMobs.add(EntityType.MUSHROOM_COW);
		// Ignore villagers to avoid accidentally killing them
		// passiveMobs.add(EntityType.VILLAGER);
	}

}
