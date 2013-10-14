package com.bendude56.goldenapple.antigrief;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;

import org.bukkit.Material;

import com.bendude56.goldenapple.GoldenApple;

import net.minecraft.server.v1_6_R3.Block;
import net.minecraft.server.v1_6_R3.Entity;
import net.minecraft.server.v1_6_R3.EntityArrow;
import net.minecraft.server.v1_6_R3.EntityLiving;
import net.minecraft.server.v1_6_R3.EntityTNTPrimed;
import net.minecraft.server.v1_6_R3.Explosion;
import net.minecraft.server.v1_6_R3.StepSound;
import net.minecraft.server.v1_6_R3.World;

public class BlockTNT extends net.minecraft.server.v1_6_R3.BlockTNT {
	
	// TODO Take a look at registration and unregistration now that getId() is deprecated
	
	@SuppressWarnings("deprecation")
	public static void registerBlock() throws Exception {
		Block.byId[Material.TNT.getId()] = null;
		Block tnt = prepClass((Block)BlockTNT.class.getConstructors()[0].newInstance(46));
		
		Field f = Block.class.getField("TNT");
		Field mod = Field.class.getDeclaredField("modifiers");
		mod.setAccessible(true);
		mod.setInt(f, mod.getInt(f)  & ~Modifier.FINAL);
		
		f.set(null, tnt);
	}
	
	@SuppressWarnings("deprecation")
	public static void unregisterBlock() throws Exception {
		Block.byId[Material.TNT.getId()] = null;
		Block tnt = prepClass((Block)net.minecraft.server.v1_6_R3.BlockTNT.class.getConstructors()[0].newInstance(46, 8));
		
		Field f = Block.class.getField("TNT");
		Field mod = Field.class.getDeclaredField("modifiers");
		mod.setAccessible(true);
		mod.setInt(f, mod.getInt(f)  & ~Modifier.FINAL);
		
		f.set(null, tnt);
	}
	
	private static Block prepClass(Block b) throws Exception {
		Method m = Block.class.getDeclaredMethod("c", new Class<?>[] { float.class });
		m.setAccessible(true);
		m.invoke(b, 0.0F);
		
		m = Block.class.getDeclaredMethod("a", new Class<?>[] { StepSound.class });
		m.setAccessible(true);
		m.invoke(b, Block.g);
		
		b.c("tnt");
		
		return b;
	}

	public BlockTNT(int i) {
		super(i);
	}
	
	@Override
	public void onPlace(World world, int i, int j, int k) {
        if (world.isBlockIndirectlyPowered(i, j, k) && !GoldenApple.getInstanceMainConfig().getBoolean("modules.antigrief.noRedstoneTnt", true)) {
            this.postBreak(world, i, j, k, 1);
            world.setAir(i, j, k);
        }
    }
	
	@Override
	public void doPhysics(World world, int i, int j, int k, int l) {
        if (world.isBlockIndirectlyPowered(i, j, k) && !GoldenApple.getInstanceMainConfig().getBoolean("modules.antigrief.noRedstoneTnt", true)) {
            this.postBreak(world, i, j, k, 1);
            world.setAir(i, j, k);
        }
    }
	
	@Override
	public void wasExploded(World world, int i, int j, int k, Explosion explosion) {
		if (!world.isStatic && !GoldenApple.getInstanceMainConfig().getBoolean("modules.antigrief.noExplosionTnt", true)) {
            EntityTNTPrimed entitytntprimed = new EntityTNTPrimed(world, i + 0.5F, j + 0.5F, k + 0.5F, explosion.c());

            entitytntprimed.fuseTicks = world.random.nextInt(entitytntprimed.fuseTicks / 4) + entitytntprimed.fuseTicks / 8;
            world.addEntity(entitytntprimed);
        }
    }
	
	@Override
	public void a(World world, int i, int j, int k, Entity entity) {
		if (entity instanceof EntityArrow && !world.isStatic) {
            EntityArrow entityarrow = (EntityArrow) entity;

            if (entityarrow.isBurning() && !GoldenApple.getInstanceMainConfig().getBoolean("modules.antigrief.noFireArrowTnt", true)) {
                this.a(world, i, j, k, 1, entityarrow.shooter instanceof EntityLiving ? (EntityLiving) entityarrow.shooter : null);
                world.setAir(i, j, k);
            }
        }
    }

}
