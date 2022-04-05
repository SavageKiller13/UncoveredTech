package com.thesecretden.uncovered_tech.main.api.crafting;

import com.thesecretden.uncovered_tech.main.api.crafting.cache.CachedRecipeList;
import net.minecraft.core.NonNullList;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.RecipeType;
import net.minecraft.world.level.Level;
import net.minecraftforge.common.util.Lazy;
import net.minecraftforge.items.ItemHandlerHelper;
import net.minecraftforge.registries.RegistryObject;
import com.google.common.collect.Lists;

import javax.annotation.Nonnull;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Random;
import java.util.stream.Collectors;

public class UCComplexAlloyRecipe extends MultiRecipe {
    public static RecipeType<UCComplexAlloyRecipe> TYPE;
    public static RegistryObject<UCRecipeSerializer<UCComplexAlloyRecipe>> SERIALIZER;

    public final IngredientWithSize input;
    public final IngredientWithSize[] extraInputs;
    public final Lazy<NonNullList<ItemStack>> output;
    public final List<StackWithChance> secondaryOutputs;

    public String specialRecipeType;
    public static List<String> specialRecipeTypes = new ArrayList<>();
    public static final CachedRecipeList<UCComplexAlloyRecipe> RECIPES = new CachedRecipeList<>(() -> TYPE, UCComplexAlloyRecipe.class);

    public UCComplexAlloyRecipe(ResourceLocation id, List<Lazy<ItemStack>> output, List<StackWithChance> secondaryOutputs, int time, int energy, IngredientWithSize input, IngredientWithSize... extraInputs) {
        super(output.get(0), TYPE, id);
        this.output = Lazy.of(() -> output.stream().map(Lazy::get).collect(Collectors.toCollection(NonNullList::create)));
        this.secondaryOutputs = secondaryOutputs;
        this.input = input;
        setTimeAndEnergy(time, energy);
        this.extraInputs = extraInputs;

        List<IngredientWithSize> inputList = Lists.newArrayList(this.input);
        if (this.extraInputs.length > 0)
            inputList.addAll(Lists.newArrayList(this.extraInputs));
        setInputListWithSizes(inputList);
        this.outputList = this.output;
    }

    @Override
    protected UCRecipeSerializer<UCComplexAlloyRecipe> getUCSerializer() {
        return SERIALIZER.get();
    }

    @Override
    public int getMultipleProcessTicks() {
        return 0;
    }

    public NonNullList<ItemStack> getBaseOutputs() {
        return this.output.get();
    }

    public NonNullList<ItemStack> generateActualOutput(ItemStack input, NonNullList<ItemStack> extraInputs, long seed) {
        Random random = new Random(seed);
        var output = this.output.get();
        NonNullList<ItemStack> actualOutput = NonNullList.withSize(output.size(), ItemStack.EMPTY);
        for (int i = 0; i < output.size(); i++)
            actualOutput.set(i, output.get(i).copy());

        for (StackWithChance secondary : secondaryOutputs) {
            if (secondary.chance() > random.nextFloat())
                continue;
            ItemStack remaining = secondary.stack().get();
            for (ItemStack existing : actualOutput)
                if (ItemHandlerHelper.canItemStacksStack(remaining, existing)) {
                    existing.grow(remaining.getCount());
                    remaining = ItemStack.EMPTY;
                    break;
                }
            if (!remaining.isEmpty())
                actualOutput.add(remaining);
        }
        return actualOutput;
    }

    public boolean matches(ItemStack input, NonNullList<ItemStack> extraInputs) {
        if (this.input != null && this.input.test(input)) {
            int[] consumed = getConsumedExtraInputs(extraInputs, false);
            return consumed != null;
        }
        return false;
    }

    public int[] getConsumedExtraInputs(NonNullList<ItemStack> extraInputs, boolean consume) {
        int[] consumed = new int[extraInputs.size()];
        for (IngredientWithSize add : this.extraInputs)
            if (add != null) {
                int addAmount = add.getCount();
                Iterator<ItemStack> stackIterator = extraInputs.iterator();
                int i = 0;
                while (stackIterator.hasNext()) {
                    ItemStack query = stackIterator.next();
                    if (!query.isEmpty()) {
                        if (add.test(query)) {
                            if (query.getCount() > addAmount) {
                                query.shrink(addAmount);
                                consumed[i] = addAmount;
                                addAmount = 0;
                            } else {
                                addAmount -= query.getCount();
                                consumed[i] = query.getCount();
                                query.setCount(0);
                            }
                        }
                        if (addAmount <= 0)
                            break;
                    }
                    i++;
                }

                if (addAmount > 0) {
                    for (int j = 0; j < consumed.length; j++)
                        extraInputs.get(j).grow(consumed[j]);
                    return null;
                }
            }

        if (!consume)
            for (int j = 0; j < consumed.length; j++)
                extraInputs.get(j).grow(consumed[j]);
        return consumed;
    }

    public boolean isValidInput(ItemStack stack) {
        return this.input != null && this.input.test(stack);
    }

    public boolean isValidExtraInput(ItemStack stack) {
        for (IngredientWithSize add : extraInputs)
            if (add != null && add.test(stack))
                return true;
        return false;
    }

    public UCComplexAlloyRecipe setSpecialRecipeType(String type) {
        this.specialRecipeType = type;
        if (!specialRecipeTypes.contains(type))
            specialRecipeTypes.add(type);
        return this;
    }

    public static UCComplexAlloyRecipe findRecipe(Level level, ItemStack input, NonNullList<ItemStack> extraInputs) {
        for (UCComplexAlloyRecipe recipe : RECIPES.getRecipes(level))
            if (recipe != null && recipe.matches(input, extraInputs))
                return recipe;
        return null;
    }

    public static boolean isValidRecipeInput(Level level, ItemStack stack) {
        for (UCComplexAlloyRecipe recipe : RECIPES.getRecipes(level))
            if (recipe != null && recipe.isValidInput(stack))
                return true;
        return false;
    }

    public static boolean isValidRecipeExtraInput(Level level, ItemStack stack) {
        for (UCComplexAlloyRecipe recipe : RECIPES.getRecipes(level))
            if (recipe != null && recipe.isValidExtraInput(stack))
                return true;
        return false;
    }
}























