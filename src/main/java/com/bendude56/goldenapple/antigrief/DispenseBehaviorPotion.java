package com.bendude56.goldenapple.antigrief;

import net.minecraft.server.v1_5_R3.DispenseBehaviorItem;
import net.minecraft.server.v1_5_R3.IDispenseBehavior;
import net.minecraft.server.v1_5_R3.ISourceBlock;
import net.minecraft.server.v1_5_R3.ItemStack;
import net.minecraft.server.v1_5_R3.MinecraftServer;

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
