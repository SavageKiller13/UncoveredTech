package com.thesecretden.uncovered_tech.main.api.crafting;

import com.google.common.base.Preconditions;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.common.util.Lazy;

public record StackWithChance(Lazy<ItemStack> stack, float chance) {
    public StackWithChance {
        Preconditions.checkNotNull(stack);
    }

    public StackWithChance(ItemStack stack, float chance) {
        this(Lazy.of(() -> stack), chance);
    }

    public CompoundTag writeToNBT() {
        CompoundTag compoundNBT = new CompoundTag();
        compoundNBT.put("stack", stack.get().save(new CompoundTag()));
        compoundNBT.putFloat("chance", chance);
        return compoundNBT;
    }

    public static StackWithChance readFromNBT(CompoundTag compoundNBT) {
        Preconditions.checkNotNull(compoundNBT);
        Preconditions.checkArgument(compoundNBT.contains("chance"));
        Preconditions.checkArgument(compoundNBT.contains("stack"));
        final ItemStack stack = ItemStack.of(compoundNBT.getCompound("stack"));
        final float chance = compoundNBT.getFloat("chance");
        return new StackWithChance(stack, chance);
    }

    public void write(FriendlyByteBuf buf) {
        buf.writeItem(this.stack.get());
        buf.writeFloat(this.chance);
    }

    public static StackWithChance read(FriendlyByteBuf buf) {
        return new StackWithChance(buf.readItem(), buf.readFloat());
    }

    public StackWithChance recalculate(float totalChance) {
        return new StackWithChance(this.stack, this.chance / totalChance);
    }
}
