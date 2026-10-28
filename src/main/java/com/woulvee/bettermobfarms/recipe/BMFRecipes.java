package com.woulvee.bettermobfarms.recipe;

import com.woulvee.bettermobfarms.BetterMobFarms;

import net.minecraft.core.registries.Registries;
import net.minecraft.world.item.crafting.RecipeMap;
import net.minecraft.world.item.crafting.RecipeSerializer;
import net.minecraft.world.item.crafting.RecipeType;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;

public class BMFRecipes {
    public static final DeferredRegister<RecipeType<?>> RECIPE_TYPES = DeferredRegister.create(Registries.RECIPE_TYPE, BetterMobFarms.MODID);
    public static final DeferredRegister<RecipeSerializer<?>> RECIPE_SERIALIZERS = DeferredRegister.create(Registries.RECIPE_SERIALIZER, BetterMobFarms.MODID);

    public static final DeferredHolder<RecipeType<?>, RecipeType<ExtractingRecipe>> EXTRACTING_TYPE = RECIPE_TYPES.register("extracting", RecipeType::simple);
    public static final DeferredHolder<RecipeSerializer<?>, RecipeSerializer<ExtractingRecipe>> EXTRACTING_SERIALIZER = RECIPE_SERIALIZERS.register("extracting",
            () -> new RecipeSerializer<>(ExtractingRecipe.CODEC, ExtractingRecipe.STREAM_CODEC));

    // Extracting recipes the server sent to this client, see BetterMobFarms#onDatapackSync
    private static RecipeMap clientRecipes = RecipeMap.EMPTY;

    public static RecipeMap clientRecipes() {
        return clientRecipes;
    }

    public static void setClientRecipes(RecipeMap recipes) {
        clientRecipes = recipes;
    }
}
