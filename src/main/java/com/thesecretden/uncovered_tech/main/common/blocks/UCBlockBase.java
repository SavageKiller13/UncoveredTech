package com.thesecretden.uncovered_tech.main.common.blocks;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.NonNullList;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.SimpleWaterloggedBlock;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.material.Fluid;
import net.minecraft.world.level.material.FluidState;
import net.minecraft.world.level.material.Fluids;
import net.minecraft.world.level.material.PushReaction;
import net.minecraft.world.level.pathfinder.PathComputationType;
import net.minecraft.world.ticks.ScheduledTick;
import org.jetbrains.annotations.Nullable;

public class UCBlockBase extends Block implements SimpleWaterloggedBlock {

    boolean isHidden;
    protected int lightOpacity;
    protected PushReaction mobilityFlag = PushReaction.NORMAL;
    protected final boolean specialBlock;
    private final boolean fitsIntoContainer;

    public UCBlockBase(BlockBehaviour.Properties properties) {
        this(properties, true);
    }

    public UCBlockBase(BlockBehaviour.Properties properties, boolean fitsIntoContainer) {
        super(properties.dynamicShape());
        this.fitsIntoContainer = fitsIntoContainer;
        this.specialBlock = !defaultBlockState().canOcclude();

        this.registerDefaultState(getInitDefaultState());
        lightOpacity = 15;
    }

    public UCBlockBase setHidden(boolean shouldHide) {
        isHidden = shouldHide;
        return this;
    }

    public UCBlockBase setLightOpacity(int opacity) {
        lightOpacity = opacity;
        return this;
    }

    @Override
    @SuppressWarnings("deprecation")
    public int getLightBlock(BlockState state, BlockGetter getter, BlockPos pos) {
        if (specialBlock)
            return 0;
        else if (state.isSolidRender(getter, pos))
            return lightOpacity;
        else
            return state.propagatesSkylightDown(getter, pos) ? 0 : 1;
    }

    public UCBlockBase setMobility(PushReaction flag) {
        mobilityFlag = flag;
        return this;
    }

    @Override
    @SuppressWarnings("deprecation")
    public PushReaction getPistonPushReaction(BlockState state) {
        return mobilityFlag;
    }

    @Override
    public float getShadeBrightness(BlockState state, BlockGetter getter, BlockPos pos) {
        return specialBlock ? 1 : super.getShadeBrightness(state, getter, pos);
    }

    @Override
    public boolean propagatesSkylightDown(BlockState state, BlockGetter getter, BlockPos pos) {
        return specialBlock || super.propagatesSkylightDown(state, getter, pos);
    }

    protected BlockState getInitDefaultState() {
        BlockState state = this.stateDefinition.any();
        if (state.hasProperty(BlockStateProperties.WATERLOGGED))
            state = state.setValue(BlockStateProperties.WATERLOGGED, false);
        return state;
    }

    public void onUCBlockPlacedBy(BlockPlaceContext ctx, BlockState state) {}

    public boolean canUCBlockBePlace(BlockState state, BlockPlaceContext ctx) {
        return true;
    }

    @Override
    public void setPlacedBy(Level level, BlockPos pos, BlockState state, @Nullable LivingEntity entity, ItemStack stack) {
        super.setPlacedBy(level, pos, state, entity, stack);
    }

    @Override
    public void fillItemCategory(CreativeModeTab tab, NonNullList<ItemStack> items) {
        items.add(new ItemStack(this, 1));
    }

    @Override
    @SuppressWarnings("deprecation")
    public boolean triggerEvent(BlockState state, Level level, BlockPos pos, int eventId, int eventParam) {
        if (level.isClientSide && eventId == 255) {
            level.sendBlockUpdated(pos, state, state, 3);
            return true;
        }

        return super.triggerEvent(state, level, pos, eventId, eventParam);
    }

    @Override
    @SuppressWarnings("deprecation")
    public boolean isPathfindable(BlockState state, BlockGetter getter, BlockPos pos, PathComputationType path) {
        return false;
    }

    public static BlockState applyLocationWaterlogging(BlockState state, Level level, BlockPos pos) {
        if (state.hasProperty(BlockStateProperties.WATERLOGGED))
            return state.setValue(BlockStateProperties.WATERLOGGED, level.getFluidState(pos).getType() == Fluids.WATER);
        return state;
    }

    @Nullable
    @Override
    public BlockState getStateForPlacement(BlockPlaceContext ctx) {
        BlockState state = this.defaultBlockState();
        state = applyLocationWaterlogging(state, ctx.getLevel(), ctx.getClickedPos());
        return state;
    }

    @Override
    @SuppressWarnings("deprecation")
    public BlockState updateShape(BlockState state, Direction dir, BlockState newState, LevelAccessor levelIn, BlockPos pos, BlockPos newPos) {
        if (state.hasProperty(BlockStateProperties.WATERLOGGED) && state.getValue(BlockStateProperties.WATERLOGGED))
            levelIn.getFluidTicks().schedule(new ScheduledTick<>(Fluids.WATER, pos, Fluids.WATER.getTickDelay(levelIn), 0));
        return super.updateShape(state, dir, newState, levelIn, pos, newPos);
    }

    @Override
    @SuppressWarnings("deprecation")
    public FluidState getFluidState(BlockState state) {
        if (state.hasProperty(BlockStateProperties.WATERLOGGED) && state.getValue(BlockStateProperties.WATERLOGGED))
            return Fluids.WATER.getSource(false);
        return super.getFluidState(state);
    }

    @Override
    public boolean canPlaceLiquid(BlockGetter getter, BlockPos pos, BlockState state, Fluid fluid) {
        return state.hasProperty(BlockStateProperties.WATERLOGGED) && SimpleWaterloggedBlock.super.canPlaceLiquid(getter, pos, state, fluid);
    }

    @Override
    public boolean placeLiquid(LevelAccessor levelIn, BlockPos pos, BlockState state, FluidState fluidState) {
        return state.hasProperty(BlockStateProperties.WATERLOGGED) && SimpleWaterloggedBlock.super.placeLiquid(levelIn, pos, state, fluidState);
    }

    @Override
    public ItemStack pickupBlock(LevelAccessor levelIn, BlockPos pos, BlockState state) {
        if (state.hasProperty(BlockStateProperties.WATERLOGGED))
            return SimpleWaterloggedBlock.super.pickupBlock(levelIn, pos, state);
        else
            return ItemStack.EMPTY;
    }

    public boolean fitIntoContainer() {
        return fitsIntoContainer;
    }


}



















