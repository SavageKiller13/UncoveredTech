package com.thesecretden.uncovered_tech.main.common.util;

import com.google.common.collect.BiMap;
import com.google.common.collect.ImmutableBiMap;
import com.google.common.collect.ImmutableMap;
import com.mojang.math.Vector4f;
import com.thesecretden.uncovered_tech.main.UncoveredTech;
import com.thesecretden.uncovered_tech.main.api.fluid.UCFluidUtils;
import com.thesecretden.uncovered_tech.main.api.utils.DirectionalBlockPos;
import com.thesecretden.uncovered_tech.main.api.utils.UCCapabilityReference;
import com.thesecretden.uncovered_tech.main.api.utils.UCDirectionUtils;
import com.thesecretden.uncovered_tech.main.common.util.inventory.IUCInventory;
import net.minecraft.client.resources.language.I18n;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.NonNullList;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.tags.TagKey;
import net.minecraft.util.Mth;
import net.minecraft.world.Container;
import net.minecraft.world.Containers;
import net.minecraft.world.entity.HumanoidArm;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.CraftingContainer;
import net.minecraft.world.inventory.MenuType;
import net.minecraft.world.item.DyeColor;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Tier;
import net.minecraft.world.item.crafting.CraftingRecipe;
import net.minecraft.world.item.crafting.RecipeType;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.BucketPickup;
import net.minecraft.world.level.block.LiquidBlockContainer;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.material.*;
import net.minecraft.world.level.storage.loot.BuiltInLootTables;
import net.minecraft.world.level.storage.loot.LootContext;
import net.minecraft.world.level.storage.loot.LootTable;
import net.minecraft.world.level.storage.loot.parameters.LootContextParamSets;
import net.minecraft.world.level.storage.loot.parameters.LootContextParams;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.common.Tags;
import net.minecraftforge.common.TierSortingRegistry;
import net.minecraftforge.fluids.FluidAttributes;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.fluids.capability.CapabilityFluidHandler;
import net.minecraftforge.fluids.capability.IFluidHandler;
import net.minecraftforge.fml.ModList;
import net.minecraftforge.items.IItemHandler;
import net.minecraftforge.items.ItemHandlerHelper;
import org.apache.commons.compress.utils.Lists;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.text.DecimalFormat;
import java.util.*;
import java.math.BigDecimal;
import java.util.function.BiPredicate;

public class UCUtils {
    public static final Random RAND = new Random();
    public static final DecimalFormat NUMBERFORMAT_PREFIXED = new DecimalFormat("+#;-#");

    public static boolean compareItemNBT(ItemStack stack1, ItemStack stack2) {
        if ((stack1.isEmpty()) != (stack2.isEmpty()))
            return false;
        boolean hasTag1 = stack1.hasTag();
        boolean hasTag2 = stack2.hasTag();
        if (hasTag1 != hasTag2)
            return false;
        if (hasTag1 && !stack1.getOrCreateTag().equals(stack2.getOrCreateTag()))
            return false;
        return stack1.areCapsCompatible(stack2);
    }

    public static final BiMap<TagKey<Item>, DyeColor> DYES_BY_TAG = ImmutableBiMap.<TagKey<Item>, DyeColor>builder().put(Tags.Items.DYES_BLACK, DyeColor.BLACK).put(Tags.Items.DYES_RED, DyeColor.RED).put(Tags.Items.DYES_GREEN, DyeColor.GREEN).put(Tags.Items.DYES_BROWN, DyeColor.BROWN).put(Tags.Items.DYES_BLUE, DyeColor.BLUE).put(Tags.Items.DYES_PURPLE, DyeColor.PURPLE).put(Tags.Items.DYES_CYAN, DyeColor.CYAN).put(Tags.Items.DYES_LIGHT_GRAY, DyeColor.LIGHT_GRAY).put(Tags.Items.DYES_GRAY, DyeColor.GRAY).put(Tags.Items.DYES_PINK, DyeColor.PINK).put(Tags.Items.DYES_LIME, DyeColor.LIME).put(Tags.Items.DYES_YELLOW, DyeColor.YELLOW).put(Tags.Items.DYES_LIGHT_BLUE, DyeColor.LIGHT_BLUE).put(Tags.Items.DYES_MAGENTA, DyeColor.MAGENTA).put(Tags.Items.DYES_ORANGE, DyeColor.ORANGE).put(Tags.Items.DYES_WHITE, DyeColor.WHITE).build();

    @Nullable
    public static DyeColor getDye(ItemStack stack) {
        if (stack.isEmpty())
            return null;
        if (stack.is(Tags.Items.DYES))
            for (Map.Entry<TagKey<Item>, DyeColor> entry : DYES_BY_TAG.entrySet())
                if (stack.is(entry.getKey()))
                    return entry.getValue();
        return null;
    }

    public static boolean isDye(ItemStack stack) {
        return stack.is(Tags.Items.DYES);
    }

    public static FluidStack copyFluidStackWithAmount(FluidStack stack, int amount) {
        return UCFluidUtils.copyStackWithAmount(stack, amount);
    }

    private static final long UUID_BASE = 129806301965L;
    private static long UUIDAdd = 1L;

    public static UUID generateNewUUID() {
        UUID uuid = new UUID(UUID_BASE, UUIDAdd);
        UUIDAdd++;
        return uuid;
    }

    public static boolean isBlockAt(Level level, BlockPos pos, Block block) {
        return level.getBlockState(pos).getBlock() == block;
    }

    public static double generateLuckInfluencedDouble(double median, double deviation, double luck, Random rand, boolean isBad, double luckScale) {
        double number = rand.nextDouble() * deviation;
        if (isBad)
            number = -number;
        number += luckScale * luck;
        if (deviation < 0)
            number = Math.max(number, deviation);
        else
            number = Math.min(number, deviation);
        return median + number;
    }

    public static String formatDouble(double d, String string) {
        DecimalFormat format = new DecimalFormat(string);
        return format.format(d);
    }

    public static String toLargeNumber(int value, String decimalPrecision, int useKilo) {
        float formatted = value >= 1000000000 ? value / 1000000000f : value >= 1000000 ? value / 1000000f : value >= useKilo ? value / 1000f : value;
        String notation = value >= 1000000000 ? "B" : value >= 1000000 ? "M" : value >= useKilo ? "K" : "";
        return formatDouble(formatted, "0." + decimalPrecision) + notation;
    }

    public static String toCamelCase(String string) {
        return string.substring(0, 1).toUpperCase(Locale.ROOT) + string.toLowerCase(Locale.ROOT);
    }

    public static String getHarvestLevelName(Tier tier) {
        return I18n.get(UncoveredTech.DESC_INFO + "miningLevel." + TierSortingRegistry.getName(tier));
    }

    public static String getModName(String modid) {
        return ModList.get().getModContainerById(modid).map(container -> container.getModInfo().getDisplayName()).orElse(modid);
    }

    public static <T> int findSequenceInList(List<T> list, T[] sequence, BiPredicate<T, T> equal) {
        if (list.size() <= 0 || list.size() < sequence.length)
            return -1;

        for (int i = 0; i < list.size(); i++)
            if (equal.test(sequence[0], list.get(i))) {
                boolean found = true;
                for (int j = 1; j < sequence.length; j++)
                    if (!(found = equal.test(sequence[j], list.get(i + j))))
                        break;
                if (found)
                    return i;
            }
        return -1;
    }

    public static Direction rotateFacingTowardsDir(Direction face, Direction dir) {
        if (dir == Direction.NORTH)
            return face;
        else if (dir == Direction.SOUTH && face.getAxis() != Direction.Axis.Y)
            return face.getClockWise().getClockWise();
        else if (dir == Direction.WEST && face.getAxis() != Direction.Axis.Y)
            return face.getCounterClockWise();
        else if (dir == Direction.EAST && face.getAxis() != Direction.Axis.Y)
            return face.getClockWise();
        else if (dir == Direction.DOWN && face.getAxis() != Direction.Axis.Y)
            return UCDirectionUtils.rotateAround(face, Direction.Axis.X);
        else if (dir == Direction.UP && face.getAxis() != Direction.Axis.X)
            return UCDirectionUtils.rotateAround(face, Direction.Axis.X).getOpposite();
        return face;
    }

    public static Vec3 getFlowVector(Level level, BlockPos pos) {
        BlockState state = level.getBlockState(pos);
        FluidState fluidState = state.getFluidState();
        return fluidState.getFlow(level, pos);
    }

    public static double minInArray(double... f) {
        if (f.length < 1)
            return 0;
        double min = f[0];
        for (int i = 1; i < f.length; i++)
            min = Math.min(min, f[i]);
        return min;
    }

    public static double maxInArray(double... f) {
        if (f.length < 1)
            return 0;
        double max = f[0];
        for (int i = 1; i < f.length; i++)
            max = Math.max(max, f[i]);
        return max;
    }

    public static int intFromRGBA(Vector4f rgba) {
        float[] array = {
                rgba.x(),
                rgba.y(),
                rgba.z(),
                rgba.w(),
        };
        return intFromRGBA(array);
    }

    public static int intFromRGBA(float[] rgba) {
        int rgbaReturn = (int)(255 * rgba[3]); //Start at Alpha Value
        rgbaReturn = (rgbaReturn<<8) + (int)(255 * rgba[0]); //Shift 8 bits left to Red
        rgbaReturn = (rgbaReturn<<8) + (int)(255 * rgba[1]); //Shift 8 bits left to Green
        rgbaReturn = (rgbaReturn<<8) + (int)(255 * rgba[2]); //Shift 8 bits left to Blue
        return rgbaReturn;
    }

    public static Vector4f vec4fFromDye(DyeColor dyeColor) {
        if (dyeColor == null)
            return new Vector4f(1, 1, 1, 1);
        float[] rgb = dyeColor.getTextureDiffuseColors();
        return new Vector4f(rgb[0], rgb[1], rgb[2], 1);
    }

    public static Vector4f vec4fFromInt(int argb) {
        return new Vector4f(
                ((argb >> 16) & 255) / 255f,
                ((argb >> 8) & 255) / 255f,
                (argb & 255) / 255f,
                ((argb >> 24) & 255) / 255f
        );
    }

    public static FluidStack drainFluidBlock(Level level, BlockPos pos, IFluidHandler.FluidAction action) {
        BlockState state = level.getBlockState(pos);
        FluidState fluidState = state.getFluidState();

        if (fluidState.isSource() && state.getBlock() instanceof BucketPickup bucketPickup) {
            if (action.execute())
                bucketPickup.pickupBlock(level, pos, state);
            return new FluidStack(fluidState.getType(), FluidAttributes.BUCKET_VOLUME);
        }
        return FluidStack.EMPTY;
    }

    public static Fluid getRelatedFluid(Level level, BlockPos pos) {
        return level.getBlockState(pos).getFluidState().getType();
    }

    public static boolean placeFluidBlock(Level level, BlockPos pos, FluidStack stack) {
        Fluid fluid = stack.getFluid();
        if (!(fluid instanceof FlowingFluid) || stack.getAmount() < FluidAttributes.BUCKET_VOLUME)
            return false;
        else {
            BlockState state = level.getBlockState(pos);
            Material material = state.getMaterial();
            boolean flag = !material.isSolid();
            boolean flag1 = material.isReplaceable();
            if (level.isEmptyBlock(pos) || flag || flag1 || state.getBlock() instanceof LiquidBlockContainer && ((LiquidBlockContainer)state.getBlock()).canPlaceLiquid(level, pos, state, fluid)) {
                if (state.getBlock() instanceof LiquidBlockContainer && fluid == Fluids.WATER)
                    ((LiquidBlockContainer)state.getBlock()).placeLiquid(level, pos, state, ((FlowingFluid)fluid).getSource(false));
                else {
                    if (!level.isClientSide && (flag || flag1) && !material.isLiquid())
                        level.destroyBlock(pos, true);

                    level.setBlock(pos, fluid.defaultFluidState().createLegacyBlock(), 11);
                }
                stack.shrink(FluidAttributes.BUCKET_VOLUME);
                return true;
            } else
                return false;
        }
    }

    public static BlockState getStateFromItemStack(ItemStack stack) {
        if (stack.isEmpty())
            return null;
        Block block = Block.byItem(stack.getItem());
        if (block != Blocks.AIR)
            return block.defaultBlockState();
        return null;
    }

    public static ItemStack insertStackIntoInventory(UCCapabilityReference<IItemHandler> reference, ItemStack stack, boolean simulate) {
        IItemHandler handler = reference.getNullable();
        if (handler != null && !stack.isEmpty())
            return ItemHandlerHelper.insertItem(handler, stack.copy(), simulate);
        else
            return stack;
    }

    public static void dropStackAtPos(Level level, DirectionalBlockPos pos, ItemStack stack) {
        dropStackAtPos(level, pos.pos(), stack, pos.dir());
    }

    public static void dropStackAtPos(Level level, BlockPos pos, ItemStack stack, @Nonnull Direction direction) {
        if (!stack.isEmpty()) {
            ItemEntity entity = new ItemEntity(level, pos.getX() + .5, pos.getY() + .5, pos.getZ() + .5, stack.copy());
            entity.setDeltaMovement(0.075 * direction.getStepX(), 0.025, 0.075 * direction.getStepZ());
            level.addFreshEntity(entity);
        }
    }

    public static void dropStackAtPos(Level level, BlockPos pos, ItemStack stack) {
        Containers.dropItemStack(level, pos.getX(), pos.getY(), pos.getZ(), stack);
    }

    public static boolean isFluidRelatedItemStack(ItemStack stack) {
        if (stack.isEmpty())
            return false;
        return stack.getCapability(CapabilityFluidHandler.FLUID_HANDLER_ITEM_CAPABILITY).isPresent();
    }

    public static Optional<CraftingRecipe> findCraftingRecipe(CraftingContainer crafting, Level level) {
        return level.getRecipeManager().getRecipeFor(RecipeType.CRAFTING, crafting, level);
    }

    public static NonNullList<ItemStack> createNonNullItemStackListFromItemStack(ItemStack stack) {
        NonNullList<ItemStack> list = NonNullList.withSize(1, ItemStack.EMPTY);
        list.set(0, stack);
        return list;
    }

    public static float[] rotateToFacing(float[] in, Direction dir) {
        for (int i = 0; i < in.length; i++)
            in[i] -= .5F;
        float[] returnFacing = new float[in.length];
        for (int i = 0; i < in.length; i++)
            for (int j = 0; j < 3; j++) {
                if (j == 0)
                    returnFacing[i+j] = in[i+0]*dir.getStepZ()+in[i+1]*dir.getStepX()+in[i+2]*dir.getStepY();
                else if (j == 1)
                    returnFacing[i+j] = in[i+0]*dir.getStepX()+in[i+1]*dir.getStepY()+in[i+2]*dir.getStepZ();
                else
                    returnFacing[i+j] = in[i+0]*dir.getStepY()+in[i+1]*dir.getStepZ()+in[i+2]*dir.getStepX();
            }
        for (int i = 0; i < in.length; i++)
            returnFacing[i] += .5;
        return returnFacing;
    }

    public static boolean isVecInBlock(Vec3 vec3, BlockPos pos, BlockPos offset, double eps) {
        return vec3.x >= pos.getX() - offset.getX() - eps && vec3.x <= pos.getX() - offset.getX() + 1 + eps && vec3.y >= pos.getY() - offset.getY() - eps && vec3.y <= pos.getY() - offset.getY() + 1 + eps && vec3.z >= pos.getZ() - offset.getZ() - eps && vec3.z <= pos.getZ() - offset.getZ() + 1 + eps;
    }

    public static Vec3 withCoordinate(Vec3 vertex, Direction.Axis axis, double value) {
        switch (axis) {
            case X: return new Vec3(value, vertex.y, vertex.z);
            case Y: return new Vec3(vertex.x, value, vertex.z);
            case Z: return new Vec3(vertex.x, vertex.y, value);
        }
        return vertex;
    }

    public static class InventoryCraftingFalse extends CraftingContainer {
        private static final AbstractContainerMenu nullContainer = new AbstractContainerMenu(MenuType.CRAFTING, 0) {
            @Override
            public void slotsChanged(Container paramIInventory) {
            }

            @Override
            public boolean stillValid(Player p_38874_) {
                return false;
            }
        };

        public InventoryCraftingFalse(int x, int y) {
            super(nullContainer, x, y);
        }

        public static CraftingContainer createFilledCraftingInventory(int x, int y, NonNullList<ItemStack> stacks) {
            CraftingContainer invContainer = new UCUtils.InventoryCraftingFalse(x, y);
            for (int i = 0; i < x * y; i++)
                if (!stacks.get(i).isEmpty())
                    invContainer.setItem(i, stacks.get(i).copy());
            return invContainer;
        }
    }

    public static Set<BlockPos> findMinOrMax(Set<BlockPos> in, boolean max, int coord) {
        Set<BlockPos> coordReturn = new HashSet<>();
        int currMinMax = max ? Integer.MIN_VALUE : Integer.MAX_VALUE;

        for (BlockPos cc : in) {
            int curr = (coord == 0 ? cc.getX() : (coord == 1 ? cc.getY() : cc.getY()));
            if (max ^ (curr < currMinMax))
                currMinMax = curr;
        }

        for (BlockPos cc : in) {
            int curr = (coord == 0 ? cc.getX() : (coord == 1 ? cc.getY() : cc.getZ()));
            if (curr == currMinMax)
                coordReturn.add(cc);
        }
        return coordReturn;
    }

    public static BlockEntity getExistingTileEntity(Level level, BlockPos pos) {
        if (level == null)
            return null;
        if (level.hasChunkAt(pos))
            return level.getBlockEntity(pos);
        return null;
    }

    public static void modifyInvStackSize(NonNullList<ItemStack> inv, int slot, int amount) {
        if (slot >= 0 && slot < inv.size() && !inv.get(slot).isEmpty()) {
            inv.get(slot).grow(amount);
            if (inv.get(slot).getCount() <= 0)
                inv.set(slot, ItemStack.EMPTY);
        }
    }

    public static void shuffleLootItems(List<ItemStack> stacks, int slotAmount, Random rand) {
        List<ItemStack> list = Lists.newArrayList();
        Iterator<ItemStack> iterator = stacks.iterator();
        while (iterator.hasNext()) {
            ItemStack stack = iterator.next();
            if (stack.getCount() <= 0)
                iterator.remove();
            else if (stack.getCount() > 1) {
                list.add(stack);
                iterator.remove();
            }
        }
        slotAmount = slotAmount - stacks.size();
        while (slotAmount > 0 && list.size() > 0) {
            ItemStack stack = list.remove(Mth.nextInt(rand, 0, list.size() - 1));
            int i = Mth.nextInt(rand, 1, stack.getCount() / 2);
            stack.shrink(i);
            ItemStack stack1 = stack.copy();
            stack1.setCount(i);

            if (stack.getCount() > 1 && rand.nextBoolean())
                list.add(stack);
            else
                stacks.add(stack);

            if (stack1.getCount() > 1 && rand.nextBoolean())
                list.add(stack1);
            else
                stacks.add(stack1);
        }
        stacks.addAll(list);
        Collections.shuffle(stacks, rand);
    }

    public static int calcRedstoneFromInventory(IUCInventory inv) {
        if (inv == null)
            return 0;
        else {
            int max = inv.getComparatedSize();
            int i = 0;
            float f = 0.0F;
            for (int j = 0; j < max; ++j) {
                ItemStack stack = inv.getInventory().get(j);
                if (!stack.isEmpty()) {
                    f += (float)stack.getCount() / (float)Math.min(inv.getSlotLimit(j), stack.getMaxStackSize());
                    ++i;
                }
            }
            f = f / (float) max;
            return Mth.floor(f * 14.0F) + (i > 0 ? 1 : 0);
        }
    }

    public static List<ItemStack> getDrops(BlockState state, LootContext.Builder builder) {
        ResourceLocation rl = state.getBlock().getLootTable();
        if (rl == BuiltInLootTables.EMPTY)
            return Collections.emptyList();
        else {
            LootContext lc = builder.withParameter(LootContextParams.BLOCK_STATE, state).create(LootContextParamSets.BLOCK);
            ServerLevel serverLevel = lc.getLevel();
            LootTable lt = serverLevel.getServer().getLootTables().get(rl);
            return lt.getRandomItems(lc);
        }
    }

    public static ItemStack getPickBlock(BlockState state, HitResult result, Player player) {
        BlockGetter getter = getSingleBlockWorldAccess(state);
        return state.getBlock().getCloneItemStack(state, result, getter, BlockPos.ZERO, player);
    }

    public static ItemStack getPickBlock(BlockState state) {
        return getPickBlock(state, new BlockHitResult(Vec3.ZERO, Direction.DOWN, BlockPos.ZERO, false), UncoveredTech.proxy.getClientPlayer());
    }

    public static List<AABB> flipBoxes(boolean flipFront, boolean flipRight, List<AABB> boxes) {
        return flipBoxes(flipFront, flipRight, boxes.toArray(new AABB[0]));
    }

    public static List<AABB> flipBoxes(boolean flipFront, boolean flipRight, AABB... boxes) {
        List<AABB> ret = new ArrayList<>(boxes.length);
        for (AABB aabb : boxes)
            ret.add(flipBox(flipFront, flipRight, aabb));
        return ret;
    }

    public static AABB flipBox(boolean flipFront, boolean flipRight, AABB aabb) {
        AABB result = aabb;
        if (flipRight)
            result = new AABB(1 - result.maxX, result.minY, result.minZ, 1 - result.minX, result.maxY, result.maxZ);
        if (flipFront)
            result = new AABB(result.minX, result.minY, 1 - result.maxZ, result.maxX, result.maxY, 1 - result.minZ);
        return result;
    }

    public static BlockGetter getSingleBlockWorldAccess(BlockState state) {
        return new SingleBlockAccess(state);
    }

    private static class SingleBlockAccess implements BlockGetter {
        private final BlockState state;

        public SingleBlockAccess(BlockState state) {
            this.state = state;
        }

        @Nullable
        @Override
        public BlockEntity getBlockEntity(BlockPos pos) {
            return null;
        }

        @Nonnull
        @Override
        public BlockState getBlockState(BlockPos pos) {
            return pos.equals(BlockPos.ZERO) ? state : Blocks.AIR.defaultBlockState();
        }

        @Nonnull
        @Override
        public FluidState getFluidState(BlockPos pos) {
            return getBlockState(pos).getFluidState();
        }

        @Override
        public int getMaxLightLevel() {
            return 0;
        }

        @Override
        public int getHeight() {
            return 1;
        }

        @Override
        public int getMinBuildHeight() {
            return 0;
        }
    }
}























