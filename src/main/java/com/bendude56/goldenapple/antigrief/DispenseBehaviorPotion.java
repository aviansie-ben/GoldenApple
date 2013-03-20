package com.bendude56.goldenapple.antigrief;

import net.minecraft.server.v1_5_R1.DispenseBehaviorItem;
import net.minecraft.server.v1_5_R1.IDispenseBehavior;
import net.minecraft.server.v1_5_R1.ISourceBlock;
import net.minecraft.server.v1_5_R1.ItemStack;
import net.minecraft.server.v1_5_R1.MinecraftServer;

public class DispenseBehaviorPotion implements IDispenseBehavior {

    private final DispenseBehaviorItem c;

    final MinecraftServer b;

    public DispenseBehaviorPotion(MinecraftServer minecraftserver) {
        this.b = minecraftserver;
        this.c = new DispenseBehaviorItem();
    }

    public ItemStack a(ISourceBlock isourceblock, ItemStack itemstack) {
        return ItemPotion.f(itemstack.getData()) ? (new DispenseBehaviorThrownPotion(this, itemstack)).a(isourceblock, itemstack) : this.c.a(isourceblock, itemstack);
    }
}
