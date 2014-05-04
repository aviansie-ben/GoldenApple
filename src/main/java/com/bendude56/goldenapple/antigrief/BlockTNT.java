package com.bendude56.goldenapple.antigrief;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.Map;

import com.bendude56.goldenapple.GoldenApple;

import net.minecraft.server.v1_7_R3.Block;
import net.minecraft.server.v1_7_R3.Entity;
import net.minecraft.server.v1_7_R3.EntityArrow;
import net.minecraft.server.v1_7_R3.EntityLiving;
import net.minecraft.server.v1_7_R3.EntityTNTPrimed;
import net.minecraft.server.v1_7_R3.Explosion;
import net.minecraft.server.v1_7_R3.RegistrySimple;
import net.minecraft.server.v1_7_R3.StepSound;
import net.minecraft.server.v1_7_R3.World;

public class BlockTNT extends net.minecraft.server.v1_7_R3.BlockTNT {
	
	private static void removeRegistration() throws NoSuchFieldException, SecurityException, IllegalArgumentException, IllegalAccessException {
		Map<Object, Object> registryMap;
		
		Field f = RegistrySimple.class.getDeclaredField("c");
		f.setAccessible(true);
		registryMap = (Map<Object, Object>) f.get(Block.REGISTRY);
		
		registryMap.remove("minecraft:tnt");
	}
	
	public static void registerBlock() throws Exception {
		// Create a new TNT block
		Block tnt = prepClass((Block)BlockTNT.class.getConstructors()[0].newInstance());
		
		// Unregister the old block (Suppresses warning messages)
		removeRegistration();
		
		// Add the new block definition to the registry
		Block.REGISTRY.a(46, "tnt", tnt);
	}
	
	@SuppressWarnings("deprecation")
	public static void unregisterBlock() throws Exception {
		// Create a new TNT block
		Block tnt = prepClass((Block)net.minecraft.server.v1_7_R3.BlockTNT.class.getConstructors()[0].newInstance());
		
		// Unregister the old block (Suppresses warning messages)
		removeRegistration();
		
		// Add the new block definition to the registry
		Block.REGISTRY.a(46, "tnt", tnt);
	}
	
	private static Block prepClass(Block b) throws Exception {
		Method m;
		
		m = Block.class.getDeclaredMethod("c", new Class<?>[] { float.class });
		m.setAccessible(true);
		m.invoke(b, 0.0F);
		
		m = Block.class.getDeclaredMethod("a", new Class<?>[] { StepSound.class });
		m.setAccessible(true);
		m.invoke(b, Block.h);
		
		b.c("tnt");
		
		m = Block.class.getDeclaredMethod("d", new Class<?>[] { String.class });
		m.setAccessible(true);
		m.invoke(b, "tnt");
		
		return b;
	}

	public BlockTNT() {
		super();
	}
	
	@Override
	public void onPlace(World world, int i, int j, int k) {
        if (world.isBlockIndirectlyPowered(i, j, k) && !GoldenApple.getInstanceMainConfig().getBoolean("modules.antigrief.noRedstoneTnt", true)) {
            this.postBreak(world, i, j, k, 1);
            world.setAir(i, j, k);
        }
    }
	
	@Override
	public void doPhysics(World world, int i, int j, int k, Block block) {
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
