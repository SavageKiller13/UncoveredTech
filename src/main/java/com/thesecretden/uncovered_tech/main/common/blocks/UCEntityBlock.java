package com.thesecretden.uncovered_tech.main.common.blocks;

import com.google.common.collect.ImmutableList;
import com.thesecretden.uncovered_tech.main.api.UCProperties;
import com.thesecretden.uncovered_tech.main.common.blocks.UCBlockInterfaces.*;
import com.thesecretden.uncovered_tech.main.common.blocks.ticking.UCClientTickableTE;
import com.thesecretden.uncovered_tech.main.common.blocks.ticking.UCServerTickableTE;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.MenuProvider;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.EntityBlock;
import net.minecraft.world.level.block.Mirror;
import net.minecraft.world.level.block.Rotation;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityTicker;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.BooleanProperty;
import net.minecraft.world.level.block.state.properties.Property;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.Shapes;
import net.minecraft.world.phys.shapes.VoxelShape;
import net.minecraftforge.network.NetworkHooks;
import net.minecraftforge.registries.RegistryObject;
import org.jetbrains.annotations.Nullable;

import java.util.Collection;
import java.util.List;
import java.util.function.BiFunction;

public class UCEntityBlock<T extends BlockEntity> extends UCBlockBase implements IColouredBlock, EntityBlock {
    private boolean hasColours = false;
    private final BiFunction<BlockPos, BlockState, T> makeEntity;
    private TEClassInspectedData classData;

    public UCEntityBlock(BiFunction<BlockPos, BlockState, T> makeEntity, Properties properties) {
        this(makeEntity, properties, true);
    }

    public UCEntityBlock(BiFunction<BlockPos, BlockState, T> makeEntity, Properties properties, boolean fitsIntoContainer) {
        super(properties, fitsIntoContainer);
        this.makeEntity = makeEntity;
    }

    public UCEntityBlock(RegistryObject<BlockEntityType<T>> tileType, Properties properties) {
        this(tileType, properties, true);
    }

    public UCEntityBlock(RegistryObject<BlockEntityType<T>> tileType, Properties properties, boolean fitsIntoContainer) {
        this((prop, state) -> tileType.get().create(prop, state), properties, fitsIntoContainer);
    }

    @Nullable
    @Override
    public BlockEntity newBlockEntity(BlockPos pos, BlockState state) {
        return makeEntity.apply(pos, state);
    }

    @Nullable
    @Override
    public <T2 extends BlockEntity> BlockEntityTicker<T2> getTicker(Level level, BlockState state, BlockEntityType<T2> type) {
        BlockEntityTicker<T2> baseTicker = getClassData().makeBaseTicker(level.isClientSide);
        if (makeEntity instanceof MultiblockTEType<?> multiTEType && type != multiTEType.master())
            return null;
        return baseTicker;
    }

    private static final List<BooleanProperty> DEFAULT_OFF = ImmutableList.of(UCProperties.MULTIBLOCKSLAVE, UCProperties.ACTIVE, UCProperties.MIRRORED);

    @Override
    protected BlockState getInitDefaultState() {
        BlockState returnState = super.getInitDefaultState();
        if (returnState.hasProperty(UCProperties.FACING_ALL))
            returnState = returnState.setValue(UCProperties.FACING_ALL, getDefaultFacing());
        else if (returnState.hasProperty(UCProperties.FACING_HORIZONTAL))
            returnState = returnState.setValue(UCProperties.FACING_HORIZONTAL, getDefaultFacing());
        for (BooleanProperty defaultOff : DEFAULT_OFF)
            if (returnState.hasProperty(defaultOff))
                returnState = returnState.setValue(defaultOff, false);
        return returnState;
    }

    @Override
    @SuppressWarnings("deprecation")
    public void onRemove(BlockState state, Level level, BlockPos pos, BlockState newState, boolean isMoving) {
        BlockEntity tile = level.getBlockEntity(pos);
        if (state.getBlock() != newState.getBlock()) {
            if (tile instanceof UCBaseTileEntity)
                ((UCBaseTileEntity)tile).setOverrideState(state);
            if (tile instanceof IHasDummyBlocks)
                ((IHasDummyBlocks)tile).breakDummies(pos, state);
        }
        super.onRemove(state, level, pos, newState, isMoving);
    }

    @Override
    public void playerDestroy(Level level, Player player, BlockPos pos, BlockState state, @Nullable BlockEntity tile, ItemStack stack) {
        if (tile instanceof IAdditionalDrops) {
            Collection<ItemStack> stacks = ((IAdditionalDrops)tile).getExtraDrops(player, state);
            if (stacks != null && !stacks.isEmpty())
                for (ItemStack s : stacks)
                    if (!s.isEmpty())
                        popResource(level, pos, s);
        }
        super.playerDestroy(level, player, pos, state, tile, stack);
    }

    @Override
    public boolean canEntityDestroy(BlockState state, BlockGetter level, BlockPos pos, Entity entity) {
        BlockEntity tile = level.getBlockEntity(pos);
        if (tile instanceof IEntityProof)
            return ((IEntityProof)tile).canEntityDestroy(entity);
        return super.canEntityDestroy(state, level, pos, entity);
    }

    @Override
    public ItemStack getCloneItemStack(BlockState state, HitResult target, BlockGetter level, BlockPos pos, Player player) {
        BlockEntity tile = level.getBlockEntity(pos);
        if (tile instanceof ITileEntityDrop && target instanceof BlockHitResult) {
            ItemStack stack = ((ITileEntityDrop)tile).getPickBlock(player, level.getBlockState(pos), target);
            if (!stack.isEmpty())
                return stack;
        }
        Item item = this.asItem();
        return item == Items.AIR ? ItemStack.EMPTY : new ItemStack(item, 1);
    }

    @Override
    public boolean triggerEvent(BlockState state, Level level, BlockPos pos, int eventId, int eventParam) {
        super.triggerEvent(state, level, pos, eventId, eventParam);
        BlockEntity tileEntity = level.getBlockEntity(pos);
        return tileEntity != null && tileEntity.triggerEvent(eventId, eventParam);
    }

    protected Direction getDefaultFacing() {
        return Direction.NORTH;
    }

    @Override
    public void onUCBlockPlacedBy(BlockPlaceContext ctx, BlockState state) {
        Level level = ctx.getLevel();
        BlockPos pos = ctx.getClickedPos();
        BlockEntity tile = level.getBlockEntity(pos);
        Player player = ctx.getPlayer();
        Direction dir = ctx.getClickedFace();
        float hitX = (float)ctx.getClickLocation().x - pos.getX();
        float hitY = (float)ctx.getClickLocation().y - pos.getY();
        float hitZ = (float)ctx.getClickLocation().z - pos.getZ();
        ItemStack stack = ctx.getItemInHand();

        if (tile instanceof IDirectionalTE) {
            Direction direction = ((IDirectionalTE)tile).getFacingForPlacement(ctx);
            ((IDirectionalTE)tile).setFacing(direction);
            if (tile instanceof IAdvancedDirectionTE)
                ((IAdvancedDirectionTE)tile).onDirectionalPlacement(dir, hitX, hitY, hitZ, player);
        }
        if (tile instanceof IReadOnPlacement)
            ((IReadOnPlacement)tile).readOnPlacement(player, stack);
        if (tile instanceof IHasDummyBlocks)
            ((IHasDummyBlocks)tile).placeDummies(ctx, state);
        if (tile instanceof IPlacementInteraction)
            ((IPlacementInteraction)tile).onTEPlaced(level, pos, state, dir, hitX, hitY, hitZ, player, stack);
    }



    @Override
    public InteractionResult use(BlockState state, Level level, BlockPos pos, Player player, InteractionHand hand, BlockHitResult hit) {
        InteractionResult superResult = super.use(state, level, pos, player, hand, hit);
        if (superResult.consumesAction())
            return superResult;
        final Direction dir = hit.getDirection();
        final float hitX = (float)hit.getLocation().x - pos.getX();
        final float hitY = (float)hit.getLocation().y - pos.getY();
        final float hitZ = (float)hit.getLocation().z - pos.getZ();
        ItemStack heldItem = player.getItemInHand(hand);
        BlockEntity tile = level.getBlockEntity(pos);
        if (tile instanceof IPlayerInteraction) {
            boolean b = ((IPlayerInteraction)tile).interact(dir, player, hand, heldItem, hitX, hitY, hitZ);
            if (b)
                return InteractionResult.SUCCESS;
        }

        if (tile instanceof MenuProvider menuProvider && hand == InteractionHand.MAIN_HAND && !player.isCrouching()) {
            if (!level.isClientSide) {
                if (menuProvider instanceof IInteractionObjectUC<?> interaction) {
                    interaction = interaction.getGuiMaster();
                    if (interaction != null && interaction.canUseGui(player))
                        NetworkHooks.openGui((ServerPlayer) player, interaction, ((BlockEntity)interaction).getBlockPos());
                } else
                    NetworkHooks.openGui((ServerPlayer) player, menuProvider);
            }
            return InteractionResult.SUCCESS;
        }
        return superResult;
    }

    @Nullable
    private Property<Direction> findFacingProperty(BlockState state) {
        if (state.hasProperty(UCProperties.FACING_ALL))
            return UCProperties.FACING_ALL;
        else if (state.hasProperty(UCProperties.FACING_HORIZONTAL))
            return UCProperties.FACING_HORIZONTAL;
        else
            return null;
    }

    @Override
    public BlockState rotate(BlockState state, Rotation rot) {
        Property<Direction> facingProp = findFacingProperty(state);
        if (facingProp != null && canRotate()) {
            Direction currentDirection = state.getValue(facingProp);
            Direction newDirection = rot.rotate(currentDirection);
            return state.setValue(facingProp, newDirection);
        }

        return super.rotate(state, rot);
    }

    @Override
    public BlockState mirror(BlockState state, Mirror mirror) {
        if (state.hasProperty(UCProperties.MIRRORED) && canRotate() && mirror == Mirror.LEFT_RIGHT)
            return state.setValue(UCProperties.MIRRORED, !state.getValue(UCProperties.MIRRORED));
        else {
            Property<Direction> facingProp = findFacingProperty(state);
            if (facingProp != null && canRotate()) {
                Direction currentDirection = state.getValue(facingProp);
                Direction newDirection = mirror.mirror(currentDirection);
                return state.setValue(facingProp, newDirection);
            }
        }
        return super.mirror(state, mirror);
    }

    protected boolean canRotate() {
        return !getStateDefinition().getProperties().contains(UCProperties.MULTIBLOCKSLAVE);
    }

    @Override
    @SuppressWarnings("deprecation")
    public void neighborChanged(BlockState state, Level level, BlockPos pos, Block block, BlockPos fromPos, boolean isMoving) {
        if (!level.isClientSide) {
            BlockEntity tile = level.getBlockEntity(pos);
            if (tile instanceof UCBaseTileEntity)
                ((UCBaseTileEntity)tile).onNeighbourBlockChange(fromPos);
        }
    }

    public UCEntityBlock setHasColours() {
        this.hasColours = true;
        return this;
    }

    @Override
    public boolean hasCustomBlockColours() {
        return hasColours;
    }

    @Override
    public int getRenderColour(BlockState state, @Nullable BlockGetter getter, @Nullable BlockPos pos, int tintIndex) {
        if (getter != null && pos != null) {
            BlockEntity tile = getter.getBlockEntity(pos);
            if (tile instanceof IColouredTE)
                return ((IColouredTE)tile).getRenderColour(tintIndex);
        }
        return 0xffffff;
    }

    @Override
    @SuppressWarnings("deprecation")
    public VoxelShape getShape(BlockState state, BlockGetter level, BlockPos pos, CollisionContext ctx) {
        if (state.getBlock() == this) {
            BlockEntity te = level.getBlockEntity(pos);
            if (te instanceof ISelectionBounds)
                return ((ISelectionBounds)te).getSelectionShape(ctx);
        }
        return super.getShape(state, level, pos, ctx);
    }

    @Override
    @SuppressWarnings("deprecation")
    public VoxelShape getCollisionShape(BlockState state, BlockGetter level, BlockPos pos, CollisionContext ctx) {
        if (getClassData().customCollisionBounds()) {
            BlockEntity te = level.getBlockEntity(pos);
            if (te instanceof ICollisionBounds collisionBounds)
                return collisionBounds.getCollisionShape(ctx);
            else
                return Shapes.empty();
        }
        return super.getCollisionShape(state, level, pos, ctx);
    }

    @Override
    @SuppressWarnings("deprecation")
    public VoxelShape getInteractionShape(BlockState state, BlockGetter level, BlockPos pos) {
        if (level.getBlockState(pos).getBlock() == this) {
            BlockEntity te = level.getBlockEntity(pos);
            if (te instanceof ISelectionBounds)
                return ((ISelectionBounds)te).getSelectionShape(null);
        }
        return super.getInteractionShape(state, level, pos);
    }

    @Override
    @SuppressWarnings("deprecation")
    public boolean hasAnalogOutputSignal(BlockState state) {
        return getClassData().hasComparatorOutput;
    }

    @Override
    @SuppressWarnings("deprecation")
    public int getAnalogOutputSignal(BlockState state, Level level, BlockPos pos) {
        BlockEntity te = level.getBlockEntity(pos);
        if (te instanceof UCBlockInterfaces.IComparatorOverride compOverride)
            return compOverride.getComparatorInputOverride();
        return 0;
    }

    @Override
    @SuppressWarnings("deprecation")
    public int getSignal(BlockState state, BlockGetter level, BlockPos pos, Direction dir) {
        BlockEntity te = level.getBlockEntity(pos);
        if (te instanceof UCBlockInterfaces.IRedstoneOutput rsOutput)
            return rsOutput.getWeakRSOutput(dir);
        return 0;
    }

    @Override
    @SuppressWarnings("deprecation")
    public int getDirectSignal(BlockState state, BlockGetter level, BlockPos pos, Direction dir) {
        BlockEntity te = level.getBlockEntity(pos);
        if (te instanceof UCBlockInterfaces.IRedstoneOutput rsOutput)
            return rsOutput.getStrongRSOutput(dir);
        return 0;
    }

    @Override
    public boolean canConnectRedstone(BlockState state, BlockGetter level, BlockPos pos, Direction direction) {
        BlockEntity te = level.getBlockEntity(pos);
        if (te instanceof UCBlockInterfaces.IRedstoneOutput rsOutput)
            return rsOutput.canConnectRedstone(direction);
        return false;
    }

    @Override
    @SuppressWarnings("deprecation")
    public void entityInside(BlockState state, Level level, BlockPos pos, Entity entity) {
        BlockEntity te = level.getBlockEntity(pos);
        if (te instanceof UCBaseTileEntity)
            ((UCBaseTileEntity)te).onEntityCollision(level, entity);
    }

    public static boolean areAllReplaceable(BlockPos start, BlockPos end, BlockPlaceContext ctx) {
        Level level = ctx.getLevel();
        return BlockPos.betweenClosedStream(start, end).allMatch(pos -> {
            BlockPlaceContext subCtx = BlockPlaceContext.at(ctx, pos, ctx.getClickedFace());
            return level.getBlockState(pos).canBeReplaced(subCtx);
        });
    }

    private TEClassInspectedData getClassData() {
        if (this.classData == null) {
            T tempTE = makeEntity.apply(BlockPos.ZERO, getInitDefaultState());
            this.classData = new TEClassInspectedData(tempTE instanceof UCServerTickableTE, tempTE instanceof UCClientTickableTE, tempTE instanceof IComparatorOverride, tempTE instanceof IRedstoneOutput, tempTE instanceof ICollisionBounds);
        }
        return this.classData;
    }

    private record TEClassInspectedData(boolean serverTicking, boolean clientTicking, boolean hasComparatorOutput, boolean emitsRedstone, boolean customCollisionBounds) {
        @Nullable
        public <T extends BlockEntity> BlockEntityTicker<T> makeBaseTicker(boolean isClient) {
            if (serverTicking && !isClient)
                return UCServerTickableTE.makeTicker();
            else if (clientTicking && isClient)
                return UCClientTickableTE.makeTicker();
            else
                return null;
        }
    }
}






















