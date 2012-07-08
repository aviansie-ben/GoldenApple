package com.bendude56.goldenapple.util;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;

import org.bukkit.Material;
import org.bukkit.entity.EntityType;

public class Constants {
	
	private static final HashSet<Byte> transparentBlocks;
	private static final List<EntityType> hostileMobs;
	private static final List<EntityType> passiveMobs;
	
	public static HashSet<Byte> getTransparentBlocks() {
		return transparentBlocks;
	}
	
	public static List<EntityType> getHostileMobs() {
		return hostileMobs;
	}
	
	public static List<EntityType> getPassiveMobs() {
		return passiveMobs;
	}
	
	static {
		transparentBlocks = new HashSet<Byte>(32);
		initTransparentBlocks();
		hostileMobs = new ArrayList<EntityType>();
		initHostileMobs();
		passiveMobs = new ArrayList<EntityType>();
		initPassiveMobs();
	}
	
	private static void initTransparentBlocks() {
		transparentBlocks.add(((Integer) Material.AIR.getId()).byteValue());
		transparentBlocks.add(((Integer) Material.BREWING_STAND.getId()).byteValue());
		transparentBlocks.add(((Integer) Material.BROWN_MUSHROOM.getId()).byteValue());
		transparentBlocks.add(((Integer) Material.CAKE.getId()).byteValue());
		transparentBlocks.add(((Integer) Material.CROPS.getId()).byteValue());
		transparentBlocks.add(((Integer) Material.DETECTOR_RAIL.getId()).byteValue());
		transparentBlocks.add(((Integer) Material.DIODE_BLOCK_ON.getId()).byteValue());
		transparentBlocks.add(((Integer) Material.DIODE_BLOCK_OFF.getId()).byteValue());
		transparentBlocks.add(((Integer) Material.LADDER.getId()).byteValue());
		transparentBlocks.add(((Integer) Material.LAVA.getId()).byteValue());
		transparentBlocks.add(((Integer) Material.LEVER.getId()).byteValue());
		transparentBlocks.add(((Integer) Material.LONG_GRASS.getId()).byteValue());
		transparentBlocks.add(((Integer) Material.MELON_STEM.getId()).byteValue());
		transparentBlocks.add(((Integer) Material.NETHER_STALK.getId()).byteValue());
		transparentBlocks.add(((Integer) Material.PAINTING.getId()).byteValue());
		transparentBlocks.add(((Integer) Material.PORTAL.getId()).byteValue());
		transparentBlocks.add(((Integer) Material.POWERED_RAIL.getId()).byteValue());
		transparentBlocks.add(((Integer) Material.PUMPKIN_STEM.getId()).byteValue());
		transparentBlocks.add(((Integer) Material.RAILS.getId()).byteValue());
		transparentBlocks.add(((Integer) Material.RED_MUSHROOM.getId()).byteValue());
		transparentBlocks.add(((Integer) Material.RED_ROSE.getId()).byteValue());
		transparentBlocks.add(((Integer) Material.REDSTONE_TORCH_ON.getId()).byteValue());
		transparentBlocks.add(((Integer) Material.REDSTONE_TORCH_OFF.getId()).byteValue());
		transparentBlocks.add(((Integer) Material.REDSTONE_WIRE.getId()).byteValue());
		transparentBlocks.add(((Integer) Material.SAPLING.getId()).byteValue());
		transparentBlocks.add(((Integer) Material.SIGN_POST.getId()).byteValue());
		transparentBlocks.add(((Integer) Material.SNOW.getId()).byteValue());
		transparentBlocks.add(((Integer) Material.TORCH.getId()).byteValue());
		transparentBlocks.add(((Integer) Material.VINE.getId()).byteValue());
		transparentBlocks.add(((Integer) Material.WALL_SIGN.getId()).byteValue());
		transparentBlocks.add(((Integer) Material.WATER.getId()).byteValue());
		transparentBlocks.add(((Integer) Material.YELLOW_FLOWER.getId()).byteValue());
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
