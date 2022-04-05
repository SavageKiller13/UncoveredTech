package com.thesecretden.uncovered_tech.main.common.items;

import com.thesecretden.uncovered_tech.main.api.fluid.UCFluidUtils;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.fluids.FluidStack;

import java.util.Optional;

public class UCItemInterfaces {
    public interface IColouredItem {
        default boolean hasCustomItemColours() {
            return false;
        }

        default int getColourForUCItem(ItemStack stack, int pass) {
            return 16777215;
        }
    }

    public interface IAdvancedFluidItem {
        int getCapacity(ItemStack stack, int baseCapacity);

        default boolean allowFluid(ItemStack container, FluidStack stack) {
            return true;
        }

        default FluidStack getFluid(ItemStack container) {
            Optional<FluidStack> optional = UCFluidUtils.getFluidContained(container);
            if (optional.isPresent())
                return optional.orElseThrow(RuntimeException::new);
            else
                return null;
        }
    }

    public interface IScrollWheel {
        void onScrollWheel(ItemStack stack, Player player, boolean forward);
    }
}























