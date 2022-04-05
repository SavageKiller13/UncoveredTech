package com.thesecretden.uncovered_tech.main.datagen.resources;

import com.thesecretden.uncovered_tech.main.api.EnumMetals;
import com.thesecretden.uncovered_tech.main.api.UCTags;
import com.thesecretden.uncovered_tech.main.api.crafting.IngredientWithSize;
import net.minecraft.tags.TagKey;
import net.minecraft.world.item.Item;
import net.minecraftforge.common.crafting.conditions.ICondition;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import static com.thesecretden.uncovered_tech.main.api.utils.UCTagUtils.createItemWrapper;
import static com.thesecretden.uncovered_tech.main.datagen.recipes.Recipes.getTagCondition;

public enum RecipeMetals {

    IRON("iron", true, true, new SecondaryOutput(UCTags.getDust("nickel"), .1f), new SecondaryOutput(UCTags.getDust("aluminium"), 0.05f)),
    GOLD("gold", true, true, new SecondaryOutput(UCTags.getDust("silver"), .35f), new SecondaryOutput(UCTags.getDust("lead"), .07f), new SecondaryOutput(UCTags.getDust("zinc"), .04f), new SecondaryOutput(UCTags.getDust("copper"), .6f)),
    COPPER("copper", true, true, new SecondaryOutput(UCTags.getDust("gold"), .1f), new SecondaryOutput(UCTags.getDust("iron"), .2f), new SecondaryOutput(UCTags.getDust("platinum"), .02f)),

    ALUMINIUM("aluminium", true, true, new SecondaryOutput(UCTags.getDust("gallium"), .01f)),
    LEAD("lead", true, true, new SecondaryOutput(UCTags.getDust("silver"), .5f)),
    SILVER("silver", true, false),
    NICKEL("nickel", true, false),
    URANIUM("uranium", true, true, new SecondaryOutput(UCTags.getDust("lead"), .12f)),
    TIN("tin", true, true, new SecondaryOutput(UCTags.getDust("iron"), .2f), new SecondaryOutput(UCTags.getDust("aluminium"), .07f), new SecondaryOutput(UCTags.getDust("lithium"), .01f), new SecondaryOutput(UCTags.getDust("tungsten"), .16f), new SecondaryOutput(UCTags.getDust("molybdenum"), .09f)),
    ZINC("zinc", true, true, new SecondaryOutput(UCTags.getDust("iron"), .5f), new SecondaryOutput(UCTags.getDust("lead"), .15f), new SecondaryOutput(UCTags.getDust("copper"), .2f), new SecondaryOutput(UCTags.getDust("gallium"), .15f)),
    PLATINUM("platinum", true, false),
    TUNGSTEN("tungsten", true, true, new SecondaryOutput(UCTags.getDust("iron"), .5f)),
    OSMIUM("osmium", true, false),
    COBALT("cobalt", true, true, new SecondaryOutput(UCTags.getDust("iron"), .1f), new SecondaryOutput(UCTags.getDust("zinc"), .1f), new SecondaryOutput(UCTags.getDust("copper"), .1f), new SecondaryOutput(UCTags.getDust("nickel"), .1f), new SecondaryOutput(UCTags.getDust("aluminium"), .1f)),
    GALLIUM("gallium", true, false),
    LITHIUM("lithium", true, false),
    MOLYBDENUM("molybdenum", true, true, new SecondaryOutput(UCTags.getDust("copper"), .1f)),

    ARDITE("ardite", false, true),

    BRONZE("bronze", true, new AlloyProperties(5, new IngredientWithSize(UCTags.getTagsFor(EnumMetals.COPPER).ingot, 4), new IngredientWithSize(createItemWrapper(UCTags.getIngot("tin")))).addConditions(getTagCondition(UCTags.getIngot("tin")))),
    BRASS("brass", true, new AlloyProperties(5, new IngredientWithSize(UCTags.getTagsFor(EnumMetals.COPPER).ingot, 3), new IngredientWithSize(createItemWrapper(UCTags.getIngot("zinc")), 2)).addConditions(getTagCondition(UCTags.getIngot("zinc")))),
    CONSTANTAN("constantan", true, new AlloyProperties(2, new IngredientWithSize(UCTags.getTagsFor(EnumMetals.COPPER).ingot), new IngredientWithSize(createItemWrapper(UCTags.getIngot("nickel")))).addConditions(getTagCondition(UCTags.getIngot("nickel")))),
    INVAR("invar", true, new AlloyProperties(5, new IngredientWithSize(UCTags.getTagsFor(EnumMetals.IRON).ingot, 3), new IngredientWithSize(createItemWrapper(UCTags.getIngot("nickel")), 2)).addConditions(getTagCondition(UCTags.getIngot("nickel")))),
    ELECTRUM("electrum", true, new AlloyProperties(2, new IngredientWithSize(UCTags.getTagsFor(EnumMetals.GOLD).ingot), new IngredientWithSize(createItemWrapper(UCTags.getIngot("silver")))).addConditions(getTagCondition(UCTags.getIngot("silver")))),
    STEEL("steel", true, new AlloyProperties(5, new IngredientWithSize(UCTags.getTagsFor(EnumMetals.IRON).ingot, 4), new IngredientWithSize(UCTags.coal)).addConditions(getTagCondition(UCTags.coal)));

    private final String name;
    private final boolean isNative;
    private final TagKey<Item> ingot;
    private final TagKey<Item> dust;
    private final TagKey<Item> ore;
    private final TagKey<Item> rawOre;
    private final TagKey<Item> rawBlock;
    private final AlloyProperties alloyProperties;
    private final SecondaryOutput[] secondaryOutputs;

    RecipeMetals(String name, boolean isNative, boolean hasOre, AlloyProperties alloyProperties, SecondaryOutput... secondaryOutputs) {
        this.name = name;
        this.ingot = createItemWrapper(UCTags.getIngot(name));
        this.dust = createItemWrapper(UCTags.getDust(name));
        this.isNative = isNative;
        this.ore = !hasOre ? null : createItemWrapper(UCTags.getOre(name));
        this.rawOre = !hasOre ? null : createItemWrapper(UCTags.getRawOre(name));
        this.rawBlock = !hasOre ? null : createItemWrapper(UCTags.getRawBlock(name));
        this.alloyProperties = alloyProperties;
        this.secondaryOutputs = secondaryOutputs;
    }

    RecipeMetals(String name, boolean isNative, boolean hasOre, SecondaryOutput... secondaryOutputs) {
        this(name, isNative, hasOre, null, secondaryOutputs);
    }

    RecipeMetals(String name, boolean isNative, AlloyProperties alloyProperties) {
        this(name, isNative, false, alloyProperties);
    }

    public String getName() {
        return name;
    }

    public boolean isNative() {
        return isNative;
    }

    public TagKey<Item> getIngot() {
        return ingot;
    }

    public TagKey<Item> getDust() {
        return dust;
    }

    public TagKey<Item> getOre() {
        return ore;
    }

    public TagKey<Item> getRawOre() {
        return rawOre;
    }

    public TagKey<Item> getRawBlock() {
        return rawBlock;
    }

    public AlloyProperties getAlloyProperties() {
        return alloyProperties;
    }

    public SecondaryOutput[] getSecondaryOutputs() {
        return secondaryOutputs;
    }

    public static class AlloyProperties {
        private final int outSize;
        private final IngredientWithSize[] alloyIngredients;
        private final List<ICondition> conditions = new ArrayList<>();

        AlloyProperties(int outSize, IngredientWithSize... alloyIngredients) {
            this.outSize = outSize;
            this.alloyIngredients = alloyIngredients;
        }

        public AlloyProperties addConditions(ICondition... conditions) {
            Collections.addAll(this.conditions, conditions);
            return this;
        }

        public int getOutSize() {
            return outSize;
        }

        public IngredientWithSize[] getAlloyIngredients() {
            return alloyIngredients;
        }

        public List<ICondition> getConditions() {
            return conditions;
        }

        public boolean isSimple() {
            return alloyIngredients.length == 2;
        }
    }
}

























