package com.bendude56.goldenapple.antigrief;

import net.minecraft.server.v1_4_R1.DispenseBehaviorProjectile;
import net.minecraft.server.v1_4_R1.IPosition;
import net.minecraft.server.v1_4_R1.IProjectile;
import net.minecraft.server.v1_4_R1.ItemStack;
import net.minecraft.server.v1_4_R1.World;

class DispenseBehaviorThrownPotion extends DispenseBehaviorProjectile {

    final ItemStack b;

    final DispenseBehaviorPotion c;

    DispenseBehaviorThrownPotion(DispenseBehaviorPotion dispensebehaviorpotion, ItemStack itemstack) {
        this.c = dispensebehaviorpotion;
        this.b = itemstack;
    }

    protected IProjectile a(World world, IPosition iposition) {
        return new EntityPotion(world, iposition.getX(), iposition.getY(), iposition.getZ(), this.b.cloneItemStack());
    }

    protected float a() {
        return super.a() * 0.5F;
    }

    protected float b() {
        return super.b() * 1.25F;
    }
}
