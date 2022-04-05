package com.thesecretden.uncovered_tech.main.common.blocks;

import com.google.common.base.Preconditions;
import com.thesecretden.uncovered_tech.main.api.UCEnums.IOSideConfig;
import com.thesecretden.uncovered_tech.main.api.UCProperties;
import com.thesecretden.uncovered_tech.main.common.registries.UCContainerTypes;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.TextComponent;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.MenuProvider;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.Property;
import net.minecraft.world.level.storage.loot.LootContext;
import net.minecraft.world.level.storage.loot.parameters.LootContextParamSets;
import net.minecraft.world.level.storage.loot.parameters.LootContextParams;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.Vec3;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.VoxelShape;
import org.jetbrains.annotations.NotNull;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.Collection;
import java.util.List;

public class UCBlockInterfaces {

    public interface IBlockOverlayText {
        @Nullable
        Component[] getOverlayText(Player player, HitResult mop, boolean hammer);
    }

    public interface ISoundTE {
        boolean shouldPlaySound(String sound);

        default float getSoundRadiusSq() {
            return 256.0F;
        }
    }

    public interface IComparatorOverride {
        int getComparatorInputOverride();
    }

    public interface IRedstoneOutput {
        default int getWeakRSOutput(Direction dir) {
            return getStrongRSOutput(dir);
        }

        int getStrongRSOutput(Direction side);

        boolean canConnectRedstone(Direction dir);
    }

    public interface IColouredBlock {
        boolean hasCustomBlockColours();

        int getRenderColour(BlockState state, @Nullable BlockGetter getter, @Nullable BlockPos pos, int tintIndex);
    }

    public interface IColouredTE {
        int getRenderColour(int tintIndex);
    }

    public interface IDirectionalTE {
        Direction getFacing();

        void setFacing(Direction facing);

        PlacementLimitation getFacingLimitation();

        default Direction getFacingForPlacement(BlockPlaceContext ctx) {
            Direction dir = getFacingLimitation().getDirectionForPlacement(ctx);
            return mirrorFacingOnPlacement(ctx.getPlayer()) ? dir.getOpposite() : dir;
        }

        default boolean mirrorFacingOnPlacement(LivingEntity entity) {
            return false;
        }

        default boolean canHammerRotate(Direction dir, Vec3 hit, LivingEntity entity) {
            return true;
        }

        default void afterRotation(Direction oldDir, Direction newDir) {}
    }

    public interface BlockstateProvider {
        BlockState getState();
        void setState(BlockState newState);
    }

    public interface IStateBasedDirectional extends IDirectionalTE, BlockstateProvider {
        Property<Direction> getFacingProperty();

        @Override
        default Direction getFacing() {
            BlockState state = getState();
            if (state.hasProperty(getFacingProperty()))
                return state.getValue(getFacingProperty());
            else
                return Direction.NORTH;
        }

        @Override
        default void setFacing(Direction facing) {
            BlockState oldState = getState();
            BlockState newState = oldState.setValue(getFacingProperty(), facing);
            setState(newState);
        }
    }

    public interface IAdvancedDirectionTE extends IDirectionalTE {
        void onDirectionalPlacement(Direction dir, float hitX, float hitY, float hitZ, LivingEntity entity);
    }

    public interface IConfigurableSides {
        IOSideConfig getSideConfig(Direction dir);

        boolean toggleSide(Direction dir, Player player);
    }

    public interface ITileEntityDrop extends IReadOnPlacement {
        List<ItemStack> getTileEntityDrop(LootContext ctx);

        default ItemStack getPickBlock(@Nullable Player player, BlockState state, HitResult result) {
            BlockEntity tile = (BlockEntity)this;
            if (!(tile.getLevel() instanceof ServerLevel level))
                return new ItemStack(state.getBlock());
            return getTileEntityDrop(new LootContext.Builder(level).withOptionalParameter(LootContextParams.TOOL, ItemStack.EMPTY).withOptionalParameter(LootContextParams.BLOCK_STATE, level.getBlockState(tile.getBlockPos())).withOptionalParameter(LootContextParams.ORIGIN, Vec3.atCenterOf(tile.getBlockPos())).create(LootContextParamSets.BLOCK)).get(0);
        }
    }

    public interface IReadOnPlacement {
        void readOnPlacement(@Nullable LivingEntity entity, ItemStack stack);
    }

    public interface IAdditionalDrops {
        Collection<ItemStack> getExtraDrops(Player player, BlockState state);
    }

    public interface IEntityProof {
        boolean canEntityDestroy(Entity entity);
    }

    public interface IPlayerInteraction {
        boolean interact(Direction dir, Player player, InteractionHand hand, ItemStack stack, float hitX, float hitY, float hitZ);
    }

    public interface IHammerInteraction {
        boolean hammerUseSide(Direction dir, Player player, InteractionHand hand, Vec3 hit);
    }

    public interface IPlacementInteraction {
        void onTEPlaced(Level level, BlockPos pos, BlockState state, Direction dir, float hitX, float hitY, float hitZ, LivingEntity entity, ItemStack stack);
    }

    public interface IActiveState extends BlockstateProvider {
        default boolean getIsActive() {
            BlockState state = getState();
            if (state.hasProperty(UCProperties.ACTIVE))
                return state.getValue(UCProperties.ACTIVE);
            else
                return false;
        }

        default void setActive(boolean active) {
            BlockState state = getState();
            BlockState newState = state.setValue(UCProperties.ACTIVE, active);
            setState(newState);
        }
    }

    public interface IMirrorable extends BlockstateProvider {
        default boolean getIsMirrored() {
            BlockState state = getState();
            if (state.hasProperty(UCProperties.MIRRORED))
                return state.getValue(UCProperties.MIRRORED);
            else
                return false;
        }

        default void setMirrored(boolean mirrored) {
            BlockState state = getState();
            BlockState newState = state.setValue(UCProperties.MIRRORED, mirrored);
            setState(newState);
        }
    }

    public interface IBlockBounds extends ISelectionBounds, ICollisionBounds {
        @Nonnull
        VoxelShape getBlockBounds(@Nullable CollisionContext ctx);

        @NotNull
        @Override
        default VoxelShape getCollisionShape(CollisionContext ctx) {
            return getBlockBounds(ctx);
        }

        @NotNull
        @Override
        default VoxelShape getSelectionShape(@org.jetbrains.annotations.Nullable CollisionContext ctx) {
            return getBlockBounds(ctx);
        }
    }

    public interface ISelectionBounds {
        @Nonnull
        VoxelShape getSelectionShape(@Nullable CollisionContext ctx);
    }

    public interface ICollisionBounds {
        @Nonnull
        VoxelShape getCollisionShape(CollisionContext ctx);
    }

    public interface IHasDummyBlocks extends IGeneralMultiblock {
        void placeDummies(BlockPlaceContext ctx, BlockState state);

        void breakDummies(BlockPos pos, BlockState state);
    }

    public interface IGeneralMultiblock extends BlockstateProvider {
        @Nullable
        IGeneralMultiblock master();

        default boolean isDummy() {
            BlockState state = getState();
            if (state.hasProperty(UCProperties.MULTIBLOCKSLAVE))
                return state.getValue(UCProperties.MULTIBLOCKSLAVE);
            else
                return true;
        }
    }

    public interface IInteractionObjectUC<T extends BlockEntity & IInteractionObjectUC<T>> extends MenuProvider {
        @Nullable
        T getGuiMaster();

        UCContainerTypes.TEContainer<? super T, ?> getContainerType();

        boolean canUseGui(Player player);

        default boolean isValid() {
            return getGuiMaster() != null;
        }

        @Nonnull
        @Override
        default AbstractContainerMenu createMenu(int id, Inventory inventory, Player player) {
            T master = getGuiMaster();
            Preconditions.checkNotNull(master);
            UCContainerTypes.TEContainer<? super T, ?> type = getContainerType();
            return type.create(id, inventory, master);
        }

        @Override
        default Component getDisplayName() {
            return new TextComponent("");
        }
    }

    public interface IProcessTE {
        int[] getCurrentProcessesStep();

        int[] getCurrentProcessesMax();
    }
}

























