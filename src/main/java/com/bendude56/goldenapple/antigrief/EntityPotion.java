package com.bendude56.goldenapple.antigrief;

import java.lang.reflect.Field;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;

import org.bukkit.craftbukkit.v1_5_R3.entity.CraftLivingEntity;
import org.bukkit.craftbukkit.v1_5_R3.inventory.CraftItemStack;
import org.bukkit.entity.LivingEntity;
import org.bukkit.potion.Potion;

import com.bendude56.goldenapple.GoldenApple;

import net.minecraft.server.v1_5_R3.AxisAlignedBB;
import net.minecraft.server.v1_5_R3.EntityPlayer;
import net.minecraft.server.v1_5_R3.Item;
import net.minecraft.server.v1_5_R3.MobEffect;
import net.minecraft.server.v1_5_R3.MobEffectList;
import net.minecraft.server.v1_5_R3.MovingObjectPosition;
import net.minecraft.server.v1_5_R3.EntityLiving;
import net.minecraft.server.v1_5_R3.ItemStack;
import net.minecraft.server.v1_5_R3.World;

public class EntityPotion extends net.minecraft.server.v1_5_R3.EntityPotion {

	public EntityPotion(World world, EntityLiving entityliving, ItemStack itemstack) {
		super(world, entityliving, itemstack);
	}

	public EntityPotion(World world, double d0, double d1, double d2, ItemStack itemstack) {
		super(world, d0, d1, d2, itemstack);
	}

	public EntityPotion(World world, EntityLiving entityliving, int i) {
		super(world, entityliving, i);
	}

	public EntityPotion(World world) {
		super(world);
	}
	
	@SuppressWarnings("rawtypes")
	protected void a(MovingObjectPosition movingobjectposition) {
        if (!this.world.isStatic) {
        	List list;
        	ItemStack item;
        	try {
	        	Field c = net.minecraft.server.v1_5_R3.EntityPotion.class.getDeclaredField("c");
	        	c.setAccessible(true);
	        	
	            list = Item.POTION.g(item = (ItemStack)c.get(this));
        	} catch (Exception e) {
        		this.die();
        		return;
        	}

            if (list != null && !list.isEmpty()) {
                AxisAlignedBB axisalignedbb = this.boundingBox.grow(4.0D, 2.0D, 4.0D);
                List list1 = this.world.a(EntityLiving.class, axisalignedbb);

                if (list1 != null) { // CraftBukkit - Run code even if there are no entities around
                    Iterator iterator = list1.iterator();

                    // CraftBukkit
                    HashMap<LivingEntity, Double> affected = new HashMap<LivingEntity, Double>();

                    while (iterator.hasNext()) {
                        EntityLiving entityliving = (EntityLiving) iterator.next();
                        double d0 = this.e(entityliving);
                        
                        try {
	                        if (entityliving instanceof EntityPlayer) {
	                        	if (GoldenApple.getInstanceMainConfig().getBoolean("modules.antigrief.noSplashPlayerPotion." + ItemPotion.typeConfigName.get(Potion.fromItemStack(CraftItemStack.asBukkitCopy(item)).getType()), true))
	                				continue;
	                        } else {
	                        	if (GoldenApple.getInstanceMainConfig().getBoolean("modules.antigrief.noSplashMobPotion." + ItemPotion.typeConfigName.get(Potion.fromItemStack(CraftItemStack.asBukkitCopy(item)).getType()), true))
	                				continue;
	                        }
                        } catch (Exception e) {
                        	continue;
                        }

                        if (d0 < 16.0D) {
                            double d1 = 1.0D - Math.sqrt(d0) / 4.0D;

                            if (entityliving == movingobjectposition.entity) {
                                d1 = 1.0D;
                            }

                            // CraftBukkit start
                            affected.put((LivingEntity) entityliving.getBukkitEntity(), d1);
                        }
                    }

                    org.bukkit.event.entity.PotionSplashEvent event = org.bukkit.craftbukkit.v1_5_R3.event.CraftEventFactory.callPotionSplashEvent(this, affected);
                    if (!event.isCancelled()) {
                        for (LivingEntity victim : event.getAffectedEntities()) {
                            if (!(victim instanceof CraftLivingEntity)) {
                                continue;
                            }

                            EntityLiving entityliving = ((CraftLivingEntity) victim).getHandle();
                            double d1 = event.getIntensity(victim);
                            // CraftBukkit end

                            Iterator iterator1 = list.iterator();

                            while (iterator1.hasNext()) {
                                MobEffect mobeffect = (MobEffect) iterator1.next();
                                int i = mobeffect.getEffectId();

                                // CraftBukkit start - abide by PVP settings - for players only!
                                if (!this.world.pvpMode && this.getShooter() instanceof EntityPlayer && entityliving instanceof EntityPlayer && entityliving != this.getShooter()) {
                                    // Block SLOWER_MOVEMENT, SLOWER_DIG, HARM, BLINDNESS, HUNGER, WEAKNESS and POISON potions
                                    if (i == 2 || i == 4 || i == 7 || i == 15 || i == 17 || i == 18 || i == 19) continue;
                                }
                                // CraftBukkit end

                                if (MobEffectList.byId[i].isInstant()) {
                                    // CraftBukkit - added 'this'
                                    MobEffectList.byId[i].applyInstantEffect(this.getShooter(), entityliving, mobeffect.getAmplifier(), d1, this);
                                } else {
                                    int j = (int) (d1 * (double) mobeffect.getDuration() + 0.5D);

                                    if (j > 20) {
                                        entityliving.addEffect(new MobEffect(i, j, mobeffect.getAmplifier()));
                                    }
                                }
                            }
                        }
                    }
                }
            }

            this.world.triggerEffect(2002, (int) Math.round(this.locX), (int) Math.round(this.locY), (int) Math.round(this.locZ), this.getPotionValue());
            this.die();
        }
    }

}
