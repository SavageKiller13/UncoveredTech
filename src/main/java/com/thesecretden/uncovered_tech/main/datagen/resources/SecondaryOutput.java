package com.thesecretden.uncovered_tech.main.datagen.resources;

import com.thesecretden.uncovered_tech.main.api.crafting.IngredientWithSize;
import com.thesecretden.uncovered_tech.main.datagen.recipes.Recipes;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.TagKey;
import net.minecraft.world.item.Item;
import net.minecraftforge.common.crafting.conditions.ICondition;

import static com.thesecretden.uncovered_tech.main.api.utils.UCTagUtils.createItemWrapper;

public class SecondaryOutput {
    private final IngredientWithSize item;
    private final float chance;
    private ICondition[] conditions;

    public SecondaryOutput(IngredientWithSize item, float chance) {
        this.item = item;
        this.chance = chance;
        this.conditions = new ICondition[0];
    }

    public SecondaryOutput(TagKey<Item> tag, float chance) {
        this(new IngredientWithSize(tag), chance);
        this.conditions = new ICondition[]{Recipes.getTagCondition(tag)};
    }

    public SecondaryOutput(ResourceLocation tag, float chance) {
        this(createItemWrapper(tag), chance);
    }

    public IngredientWithSize getItem() {
        return item;
    }

    public float getChance() {
        return chance;
    }

    public SecondaryOutput setConditions(ICondition[] conditions) {
        this.conditions = conditions;
        return this;
    }

    public ICondition[] getConditions() {
        return conditions;
    }
}
























