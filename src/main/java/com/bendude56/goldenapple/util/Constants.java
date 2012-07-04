package com.bendude56.goldenapple.util;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;

import org.bukkit.Material;
import org.bukkit.entity.EntityType;

public class Constants {

	public static HashSet<Byte> transparentBlocks() {
		HashSet<Byte> blockList = new HashSet<Byte>(32);
				blockList.add(((Integer) Material.AIR.getId()).byteValue());
				blockList.add(((Integer) Material.BREWING_STAND.getId()).byteValue());
				blockList.add(((Integer) Material.BROWN_MUSHROOM.getId()).byteValue());
				blockList.add(((Integer) Material.CAKE.getId()).byteValue());
				blockList.add(((Integer) Material.CROPS.getId()).byteValue());
				blockList.add(((Integer) Material.DETECTOR_RAIL.getId()).byteValue());
				blockList.add(((Integer) Material.DIODE_BLOCK_ON.getId()).byteValue());
				blockList.add(((Integer) Material.DIODE_BLOCK_OFF.getId()).byteValue());
				blockList.add(((Integer) Material.LADDER.getId()).byteValue());
				blockList.add(((Integer) Material.LAVA.getId()).byteValue());
				blockList.add(((Integer) Material.LEVER.getId()).byteValue());
				blockList.add(((Integer) Material.LONG_GRASS.getId()).byteValue());
				blockList.add(((Integer) Material.MELON_STEM.getId()).byteValue());
				blockList.add(((Integer) Material.NETHER_STALK.getId()).byteValue());
				blockList.add(((Integer) Material.PAINTING.getId()).byteValue());
				blockList.add(((Integer) Material.PORTAL.getId()).byteValue());
				blockList.add(((Integer) Material.POWERED_RAIL.getId()).byteValue());
				blockList.add(((Integer) Material.PUMPKIN_STEM.getId()).byteValue());
				blockList.add(((Integer) Material.RAILS.getId()).byteValue());
				blockList.add(((Integer) Material.RED_MUSHROOM.getId()).byteValue());
				blockList.add(((Integer) Material.RED_ROSE.getId()).byteValue());
				blockList.add(((Integer) Material.REDSTONE_TORCH_ON.getId()).byteValue());
				blockList.add(((Integer) Material.REDSTONE_TORCH_OFF.getId()).byteValue());
				blockList.add(((Integer) Material.REDSTONE_WIRE.getId()).byteValue());
				blockList.add(((Integer) Material.SAPLING.getId()).byteValue());
				blockList.add(((Integer) Material.SIGN_POST.getId()).byteValue());
				blockList.add(((Integer) Material.SNOW.getId()).byteValue());
				blockList.add(((Integer) Material.TORCH.getId()).byteValue());
				blockList.add(((Integer) Material.VINE.getId()).byteValue());
				blockList.add(((Integer) Material.WALL_SIGN.getId()).byteValue());
				blockList.add(((Integer) Material.WATER.getId()).byteValue());
				blockList.add(((Integer) Material.YELLOW_FLOWER.getId()).byteValue());
		return blockList;
	}
	
	public static List<EntityType> hostileMobs(){
		List<EntityType> list = new ArrayList<EntityType>();
		list.add(EntityType.ZOMBIE); //Overworld Mobs
		list.add(EntityType.CREEPER);
		list.add(EntityType.SKELETON);
		list.add(EntityType.SPIDER);
		list.add(EntityType.SLIME);
		list.add(EntityType.CAVE_SPIDER);
		list.add(EntityType.SILVERFISH);
		list.add(EntityType.PIG_ZOMBIE); //Nether Mobs
		list.add(EntityType.GHAST);
		list.add(EntityType.MAGMA_CUBE);
		list.add(EntityType.BLAZE);
		list.add(EntityType.ENDERMAN); //End Mobs
		list.add(EntityType.ENDER_DRAGON);
		return list;
	}
	public static List<EntityType> passiveMobs(){
		List<EntityType> list = new ArrayList<EntityType>();
		list.add(EntityType.PIG);
		list.add(EntityType.SHEEP);
		list.add(EntityType.COW);
		list.add(EntityType.CHICKEN);
		list.add(EntityType.WOLF);
		list.add(EntityType.OCELOT);
		list.add(EntityType.SQUID);
		list.add(EntityType.IRON_GOLEM);
		list.add(EntityType.SNOWMAN);
		list.add(EntityType.MUSHROOM_COW);
		//list.add(EntityType.VILLAGER); IGNORES VILLAGERS TO AVOID ACCIDENTALLY KILLING THEM
		return list;
	}

}
