package com.thesecretden.uncovered_tech.main.api.crafting.cache;

import com.thesecretden.uncovered_tech.main.api.crafting.UCSerializableRecipe;

import java.util.List;

public interface IRecipeList {
    List<? extends UCSerializableRecipe> getSubRecipes();
}
