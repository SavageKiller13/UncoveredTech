package com.thesecretden.uncovered_tech.main.common.blocks;

import com.thesecretden.uncovered_tech.main.UncoveredTech;
import com.thesecretden.uncovered_tech.main.api.UCProperties;
import com.thesecretden.uncovered_tech.main.api.client.UCTextUtils;
import com.thesecretden.uncovered_tech.main.common.util.ItemNBTHelper;
import net.minecraft.ChatFormatting;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.TranslatableComponent;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.item.crafting.RecipeType;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraftforge.fluids.FluidStack;
import org.jetbrains.annotations.Nullable;

import java.util.List;

public class UCBlockItem extends BlockItem {
    private int burnTime;

    public UCBlockItem(Block b, Item.Properties properties) {
        super(b, properties);
    }

    public UCBlockItem(Block b) {
        this(b, new Item.Properties().tab(UncoveredTech.MATERIALS_TAB));
    }

    @Override
    public String getDescriptionId(ItemStack stack) {
        return getBlock().getDescriptionId();
    }

    @Override
    public void appendHoverText(ItemStack stack, @Nullable Level level, List<Component> componentList, TooltipFlag flag) {
        super.appendHoverText(stack, level, componentList, flag);

        if (ItemNBTHelper.hasKey(stack, "energyStorage"))
            componentList.add(UCTextUtils.applyFormat(new TranslatableComponent(UncoveredTech.DESC_INFO + "energyStored", ItemNBTHelper.getInt(stack, "energyStorage")), ChatFormatting.DARK_RED));
        if (ItemNBTHelper.hasKey(stack, "tank")) {
            FluidStack fluidStack = FluidStack.loadFluidStackFromNBT(ItemNBTHelper.getTagCompound(stack, "tank"));
            if (fluidStack != null)
                componentList.add(UCTextUtils.applyFormat(new TranslatableComponent(UncoveredTech.DESC_INFO + "fluidStored", fluidStack.getDisplayName(), fluidStack.getAmount()), ChatFormatting.AQUA));
         }
    }

    public UCBlockItem setBurnTime(int burnTime) {
        this.burnTime = burnTime;
        return this;
    }

    public int getBurnTime(ItemStack stack, RecipeType<?> type) {
        return this.burnTime;
    }

    @Override
    protected boolean placeBlock(BlockPlaceContext ctx, BlockState state) {
        Block block = state.getBlock();
        if (block instanceof UCBlockBase ucBlock) {
            if (!ucBlock.canUCBlockBePlace(state, ctx))
                return false;
            boolean blockReturn = super.placeBlock(ctx, state);
            if (blockReturn)
                ucBlock.onUCBlockPlacedBy(ctx, state);
            return blockReturn;
        } else
            return super.placeBlock(ctx, state);
    }

    @Override
    protected boolean updateCustomBlockEntityTag(BlockPos pos, Level level, @Nullable Player player, ItemStack stack, BlockState state) {
        if (!state.hasProperty(UCProperties.MULTIBLOCKSLAVE))
            return super.updateCustomBlockEntityTag(pos, level, player, stack, state);
        else
            return false;
    }

    @Override
    public boolean canFitInsideContainerItems() {
        return !(getBlock() instanceof UCBlockBase ucBlock) || ucBlock.fitIntoContainer();
    }

    public static class UCBlockItemNoInventory extends UCBlockItem {
        public UCBlockItemNoInventory(Block block, Properties properties) {
            super(block, properties);
        }

        @Nullable
        @Override
        public CompoundTag getShareTag(ItemStack stack) {
            CompoundTag tag = super.getShareTag(stack);
            if (tag != null) {
                tag = tag.copy();
                tag.remove("inventory");
            }
            return tag;
        }
    }
}






























