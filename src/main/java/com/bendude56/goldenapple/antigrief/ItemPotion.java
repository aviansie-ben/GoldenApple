package com.bendude56.goldenapple.antigrief;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.Map;

import org.bukkit.craftbukkit.v1_7_R1.inventory.CraftItemStack;
import org.bukkit.potion.Potion;
import org.bukkit.potion.PotionType;

import com.bendude56.goldenapple.GoldenApple;

import net.minecraft.server.v1_7_R1.EntityHuman;
import net.minecraft.server.v1_7_R1.Item;
import net.minecraft.server.v1_7_R1.ItemStack;
import net.minecraft.server.v1_7_R1.RegistrySimple;
import net.minecraft.server.v1_7_R1.World;

public class ItemPotion extends net.minecraft.server.v1_7_R1.ItemPotion {
	
	public static HashMap<PotionType, String> typeConfigName = new HashMap<PotionType, String>();
	
	static {
		typeConfigName.put(PotionType.REGEN, "regenerate");
		typeConfigName.put(PotionType.SPEED, "swiftness");
		typeConfigName.put(PotionType.FIRE_RESISTANCE, "fireResistance");
		typeConfigName.put(PotionType.POISON, "poison");
		typeConfigName.put(PotionType.INSTANT_HEAL, "instantHealth");
		typeConfigName.put(PotionType.WEAKNESS, "weakness");
		typeConfigName.put(PotionType.STRENGTH, "strength");
		typeConfigName.put(PotionType.INVISIBILITY, "invisibility");
		typeConfigName.put(PotionType.NIGHT_VISION, "nightVision");
		typeConfigName.put(PotionType.SLOWNESS, "slowness");
		typeConfigName.put(PotionType.INSTANT_DAMAGE, "harming");
	}
	
	private static void removeRegistration() throws NoSuchFieldException, SecurityException, IllegalArgumentException, IllegalAccessException {
		Map<Object, Object> registryMap;
		
		Field f = RegistrySimple.class.getDeclaredField("c");
		f.setAccessible(true);
		registryMap = (Map<Object, Object>) f.get(Item.REGISTRY);
		
		registryMap.remove("minecraft:potion");
	}
	
	public static void registerItem() throws Exception {
		// Create a new potion item
		Item potion = prepClass((Item)ItemPotion.class.getConstructors()[0].newInstance());
		
		// Unregister the old item (Suppresses warning messages)
		removeRegistration();
		
		// Add the new item definition to the registry
		Item.REGISTRY.a(373, "potion", potion);
	}
	
	public static void unregisterItem() throws Exception {
		// Create a new potion item
		Item potion = prepClass((Item)net.minecraft.server.v1_7_R1.ItemPotion.class.getConstructors()[0].newInstance());
		
		// Unregister the old item (Suppresses warning messages)
		removeRegistration();
		
		// Add the new item definition to the registry
		Item.REGISTRY.a(373, "potion", potion);
	}
	
	private static Item prepClass(Item i) throws Exception {
		Method m;
		
		i.c("potion");
		
		m = Item.class.getDeclaredMethod("f", new Class<?>[] { String.class });
		m.setAccessible(true);
		m.invoke(i, "potion");
		
		return i;
	}

	public ItemPotion() {
		super();
	}
	
	@Override
	public ItemStack b(ItemStack itemstack, World world, EntityHuman entityhuman) {
		try {
			if (!GoldenApple.getInstanceMainConfig().getBoolean("modules.antigrief.noDrinkPotion." + typeConfigName.get(Potion.fromItemStack(CraftItemStack.asBukkitCopy(itemstack)).getType()), true))
				return super.b(itemstack, world, entityhuman);
		} catch (Exception e) { }
		return itemstack;
    }

}
