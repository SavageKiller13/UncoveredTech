package com.thesecretden.uncovered_tech.main.api.crafting;

import com.google.common.base.Preconditions;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParseException;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.thesecretden.uncovered_tech.main.api.fluid.UCFluidUtils;
import com.thesecretden.uncovered_tech.main.api.utils.FastEither;
import com.thesecretden.uncovered_tech.main.api.utils.UCItemUtils;
import com.thesecretden.uncovered_tech.main.api.utils.UCTagUtils;
import net.minecraft.core.Registry;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.TagKey;
import net.minecraft.util.GsonHelper;
import net.minecraft.world.level.material.Fluid;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.fluids.capability.IFluidHandler;
import net.minecraftforge.registries.ForgeRegistries;
import org.jetbrains.annotations.Nullable;

import javax.annotation.Nonnull;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Predicate;
import java.util.stream.Collectors;

public class FluidTagInput implements Predicate<FluidStack> {
    protected final FastEither<TagKey<Fluid>, List<ResourceLocation>> fluidTag;
    protected final int amount;
    protected final CompoundTag tag;

    public FluidTagInput(FastEither<TagKey<Fluid>, List<ResourceLocation>> matching, int amount, CompoundTag tag) {
        this.fluidTag = matching;
        this.amount = amount;
        this.tag = tag;
    }

    public FluidTagInput(TagKey<Fluid> fluidTag, int amount, CompoundTag tag) {
        this(FastEither.left(fluidTag), amount, tag);
    }

    public FluidTagInput(ResourceLocation rl, int amount, CompoundTag tag) {
        this(TagKey.create(Registry.FLUID_REGISTRY, rl), amount, tag);
    }

    public FluidTagInput(ResourceLocation rl, int amount) {
        this(rl, amount, null);
    }

    public FluidTagInput(TagKey<Fluid> tag, int amount) {
        this(tag, amount, null);
    }

    public static FluidTagInput deserialize(JsonElement input) {
        Preconditions.checkArgument(input instanceof JsonObject, "FluidTagWithSize can only be deserialized from a JsonObject");
        JsonObject object = input.getAsJsonObject();
        ResourceLocation rl = new ResourceLocation(GsonHelper.getAsString(object, "tag"));
        if (!GsonHelper.isValidNode(object, "nbt"))
            return new FluidTagInput(rl, GsonHelper.getAsInt(object, "amount"));
        try {
            CompoundTag nbt = UCItemUtils.parseNBTFromJson(object.get("nbt"));
            return new FluidTagInput(rl, GsonHelper.getAsInt(object, "amount"), nbt);
        } catch (CommandSyntaxException e) {
            throw new JsonParseException(e);
        }
    }

    public FluidTagInput withAmount(int amount) {
        return new FluidTagInput(this.fluidTag, amount, this.tag);
    }

    @Override
    public boolean test(@Nullable FluidStack stack) {
        return testIgnoringAmount(stack) && stack.getAmount() >= this.amount;
    }

    public boolean testIgnoringAmount(@Nullable FluidStack stack) {
        if (stack == null)
            return false;
        if (!fluidTag.map(t -> stack.getFluid().is(t), l -> l.contains(stack.getFluid().getRegistryName())))
            return false;
        if (this.tag != null)
            return stack.hasTag() && stack.getTag().equals(this.tag);
        return true;
    }

    @Nonnull
    public List<FluidStack> getMatchingStacks() {
        return fluidTag.map(t -> UCTagUtils.elementStream(Registry.FLUID, t), l -> l.stream().map(ForgeRegistries.FLUIDS::getValue)).map(fluid -> new FluidStack(fluid, FluidTagInput.this.amount, FluidTagInput.this.tag)).collect(Collectors.toList());
    }

    @Nonnull
    public JsonElement serialize() {
        JsonObject object = new JsonObject();
        ResourceLocation name = this.fluidTag.orThrow().location();
        object.addProperty("tag", name.toString());
        object.addProperty("amount", this.amount);
        if (this.tag != null)
            object.addProperty("nbt", this.tag.toString());
        return object;
    }

    public int getAmount() {
        return amount;
    }

    public FluidStack getRandomizedExampleStack(int random) {
        List<FluidStack> all = getMatchingStacks();
        return all.get((random/20) % all.size());
    }

    public static FluidTagInput read(FriendlyByteBuf buf) {
        int numMatching = buf.readVarInt();
        List<ResourceLocation> matching = new ArrayList<>(numMatching);
        for (int i = 0; i < numMatching; i++)
            matching.add(buf.readResourceLocation());
        int amount = buf.readInt();
        CompoundTag tag = buf.readBoolean() ? buf.readNbt() : null;
        return new FluidTagInput(FastEither.right(matching), amount, tag);
    }

    public void write(FriendlyByteBuf buf) {
        List<ResourceLocation> matching = fluidTag.map(f -> UCTagUtils.elementStream(Registry.FLUID, f).map(Fluid::getRegistryName).collect(Collectors.toList()), l -> l);
        buf.writeVarInt(matching.size());
        for (ResourceLocation rl : matching)
            buf.writeResourceLocation(rl);
        buf.writeInt(this.amount);
        buf.writeBoolean(this.tag != null);
        if (this.tag != null)
            buf.writeNbt(this.tag);
    }

    public boolean extractFrom(IFluidHandler handler, IFluidHandler.FluidAction action) {
        for (int tank = 0; tank < handler.getTanks(); tank++) {
            FluidStack inTank = handler.getFluidInTank(tank);
            if (testIgnoringAmount(inTank)) {
                FluidStack toExtract = UCFluidUtils.copyStackWithAmount(inTank, this.amount);
                FluidStack extractedSim = handler.drain(toExtract, IFluidHandler.FluidAction.SIMULATE);
                if (extractedSim.getAmount() >= this.amount) {
                    if (action != IFluidHandler.FluidAction.SIMULATE)
                        handler.drain(toExtract, action);
                    return true;
                }
            }
        }
        return false;
    }
}




























