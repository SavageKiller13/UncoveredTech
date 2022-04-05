package com.thesecretden.uncovered_tech.main.common.items;

import com.thesecretden.uncovered_tech.main.UncoveredTech;
import com.thesecretden.uncovered_tech.main.common.registries.UCContainerTypes;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.TextComponent;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.util.Mth;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.MenuProvider;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.RecipeType;
import net.minecraftforge.network.NetworkHooks;
import org.jetbrains.annotations.Nullable;

import javax.annotation.Nonnull;

public class UCBaseItem extends Item implements UCItemInterfaces.IColouredItem {
    private int burnTime = -1;
    private boolean isHidden = false;

    public UCBaseItem() {
        this(new Properties());
    }

    public UCBaseItem(Properties properties) {
        this(properties, UncoveredTech.MATERIALS_TAB);
    }

    public UCBaseItem(Properties properties, CreativeModeTab tab) {
        super(properties.tab(tab));
    }

    public UCBaseItem setBurnTime(int burnTime) {
        this.burnTime = burnTime;
        return this;
    }

    @Override
    public int getBurnTime(ItemStack stack, RecipeType<?> type) {
        return burnTime;
    }

    public boolean isHidden() {
        return isHidden;
    }

    protected void openGui(Player player, InteractionHand hand) {
        openGui(player, hand == InteractionHand.MAIN_HAND ? EquipmentSlot.MAINHAND : EquipmentSlot.OFFHAND);
    }

    protected void openGui(Player player, EquipmentSlot slot) {
        ItemStack stack = player.getItemBySlot(slot);
        NetworkHooks.openGui((ServerPlayer) player, new MenuProvider() {
            @Override
            public Component getDisplayName() {
                return new TextComponent("");
            }

            @Nullable
            @Override
            public AbstractContainerMenu createMenu(int id, @Nonnull Inventory inv, Player player) {
                if (!(stack.getItem() instanceof UCBaseItem))
                    return null;
                UCContainerTypes.ItemContainerType<?> containerType = ((UCBaseItem)stack.getItem()).getContainerType();
                if (containerType == null)
                    return null;
                return containerType.create(id, inv, player.level, slot, stack);
            }
        }, buffer -> buffer.writeInt(slot.ordinal()));
    }

    @Override
    public boolean isRepairable(ItemStack stack) {
        return false;
    }

    public boolean isUCRepairable(@Nonnull ItemStack stack) {
        return super.isRepairable(stack);
    }

    @Override
    public boolean isBookEnchantable(ItemStack stack, ItemStack book) {
        return false;
    }

    @Nullable
    protected UCContainerTypes.ItemContainerType<?> getContainerType() {
        return null;
    }

    public boolean canEquip(ItemStack stack, EquipmentSlot armourType, Entity entity) {
        return Mob.getEquipmentSlotForItem(stack) == armourType || getEquipmentSlot(stack) == armourType;
    }

    @Override
    public int getBarColor(ItemStack stack) {
        return Mth.hsvToRgb(Math.max(0.0F, getBarWidth(stack)/(float)MAX_BAR_WIDTH)/3.0F, 1.0F, 1.0F);
    }
}



























