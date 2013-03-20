package com.bendude56.goldenapple.antigrief;

import java.util.HashMap;

import org.bukkit.craftbukkit.v1_4_R1.inventory.CraftItemStack;
import org.bukkit.potion.Potion;
import org.bukkit.potion.PotionType;

import com.bendude56.goldenapple.GoldenApple;

import net.minecraft.server.v1_5_R1.EntityHuman;
import net.minecraft.server.v1_5_R1.Item;
import net.minecraft.server.v1_5_R1.ItemStack;
import net.minecraft.server.v1_5_R1.World;

public class ItemPotion extends net.minecraft.server.v1_5_R1.ItemPotion {
	
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
	
	public static void registerItem() throws Exception {
		Item.byId[Item.POTION.id] = null;
		Item potion = prepClass((Item)ItemPotion.class.getConstructors()[0].newInstance(117));
		
		Item.POTION = (ItemPotion)potion;
	}
	
	public static void unregisterItem() throws Exception {
		Item.byId[Item.POTION.id] = null;
		Item potion = prepClass((Item)net.minecraft.server.v1_5_R1.ItemPotion.class.getConstructors()[0].newInstance(117));
		
		Item.POTION = (ItemPotion)potion;
	}
	
	private static Item prepClass(Item i) throws Exception {
		return i.b(13, 8).b("potion");
	}

	public ItemPotion(int i) {
		super(i);
	}
	
	public ItemStack b(ItemStack itemstack, World world, EntityHuman entityhuman) {
		try {
			if (!GoldenApple.getInstance().mainConfig.getBoolean("modules.antigrief.noDrinkPotion." + typeConfigName.get(Potion.fromItemStack(CraftItemStack.asBukkitCopy(itemstack)).getType()), true))
				return super.b(itemstack, world, entityhuman);
		} catch (Exception e) { }
		return itemstack;
    }
	
	public ItemStack a(ItemStack itemstack, World world, EntityHuman entityhuman) {
        if (g(itemstack.getData())) {
            if (!entityhuman.abilities.canInstantlyBuild) {
                --itemstack.count;
            }

            world.makeSound(entityhuman, "random.bow", 0.5F, 0.4F / (d.nextFloat() * 0.4F + 0.8F));
            if (!world.isStatic) {
                world.addEntity(new EntityPotion(world, entityhuman, itemstack));
            }

            return itemstack;
        } else {
            entityhuman.a(itemstack, this.c_(itemstack));
            return itemstack;
        }
    }

}
