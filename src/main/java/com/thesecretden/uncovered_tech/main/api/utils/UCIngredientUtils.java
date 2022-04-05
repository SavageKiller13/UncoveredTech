package com.thesecretden.uncovered_tech.main.api.utils;

import com.thesecretden.uncovered_tech.main.api.crafting.IngredientWithSize;
import net.minecraft.core.NonNullList;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.Ingredient;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.function.BiPredicate;
import java.util.function.Function;

public class UCIngredientUtils {
    public static boolean stacksMatchIngredientList(List<Ingredient> list, NonNullList<ItemStack> stacks) {
        return stacksMatchList(list, stacks, i -> 1, Ingredient::test);
    }

    public static boolean stacksMatchIngredientWithSizeList(List<IngredientWithSize> list, NonNullList<ItemStack> stacks) {
        return stacksMatchList(list, stacks, IngredientWithSize::getCount, IngredientWithSize::testIgnoringSize);
    }

    public static Ingredient createIngredientFromList(List<ItemStack> list) {
        return Ingredient.of(list.toArray(new ItemStack[0]));
    }

    private static <T> boolean stacksMatchList(List<T> list, NonNullList<ItemStack> stacks, Function<T, Integer> size, BiPredicate<T, ItemStack> matchesIgnoreSize) {
        List<ItemStack> queryList = new ArrayList<>(stacks.size());
        for (ItemStack s: stacks)
            if (!s.isEmpty())
                queryList.add(s.copy());

        for (T ingr : list)
            if (ingr != null) {
                int amount = size.apply(ingr);
                Iterator<ItemStack> stackIterator = queryList.iterator();
                while (stackIterator.hasNext()) {
                    ItemStack query = stackIterator.next();
                    if (!query.isEmpty()) {
                        if (matchesIgnoreSize.test(ingr, query)) {
                            if (query.getCount() > amount) {
                                query.shrink(amount);
                            } else {
                                amount -= query.getCount();
                                query.setCount(0);
                            }
                        }
                        if (query.getCount() <= 0)
                            stackIterator.remove();
                        if (amount <= 0)
                            break;
                    }
                }

                if (amount > 0)
                    return false;
            }

        return true;
    }

    public static boolean hasPlayerIngredient(Player player, IngredientWithSize ingredient) {
        int amount = ingredient.getCount();
        ItemStack stack;
        for (InteractionHand hand : InteractionHand.values()) {
            stack = player.getItemInHand(hand);
            if (ingredient.test(stack)) {
                amount -= stack.getCount();
                if (amount <= 0)
                    return true;
            }
        }
        for (int i = 0; i < player.getInventory().getContainerSize(); i++) {
            stack = player.getInventory().getItem(i);
            if (ingredient.test(stack)) {
                amount -= stack.getCount();
                if (amount <= 0)
                    return true;
            }
        }
        return amount <= 0;
    }

    public static void consumePlayerIngredient(Player player, IngredientWithSize ingredient) {
        int amount = ingredient.getCount();
        ItemStack stack;
        for (InteractionHand hand : InteractionHand.values()) {
            stack = player.getItemInHand(hand);
            if (ingredient.testIgnoringSize(stack)) {
                int taken = Math.min(amount, stack.getCount());
                amount -= taken;
                stack.shrink(taken);
                if (stack.getCount() <= 0)
                    player.setItemInHand(hand, ItemStack.EMPTY);
                if (amount <= 0)
                    return;
            }
        }
        for (int i = 0; i < player.getInventory().getContainerSize(); i++) {
            stack = player.getInventory().getItem(i);
            if (ingredient.testIgnoringSize(stack)) {
                int taken = Math.min(amount, stack.getCount());
                amount -= taken;
                stack.shrink(taken);
                if (stack.getCount() <= 0)
                    player.getInventory().setItem(i, ItemStack.EMPTY);
                if (amount <= 0)
                    return;
            }
        }
    }
}
