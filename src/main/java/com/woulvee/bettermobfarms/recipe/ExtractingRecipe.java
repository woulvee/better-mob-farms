package com.woulvee.bettermobfarms.recipe;

import java.util.List;
import java.util.Optional;

import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;

import net.minecraft.core.component.DataComponentType;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.ItemStackTemplate;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.item.crafting.PlacementInfo;
import net.minecraft.world.item.crafting.Recipe;
import net.minecraft.world.item.crafting.RecipeBookCategories;
import net.minecraft.world.item.crafting.RecipeBookCategory;
import net.minecraft.world.item.crafting.RecipeHolder;
import net.minecraft.world.item.crafting.RecipeSerializer;
import net.minecraft.world.item.crafting.RecipeType;
import net.minecraft.world.item.crafting.SingleRecipeInput;
import net.minecraft.world.level.Level;

// Extracts a fluid from the item in the off hand into a syringe, e.g. poison from a poisonous potato.
// JSON: {"type": "bettermobfarms:extracting", "ingredient": ..., "syringe": {...},
//        "byproduct": {...} (optional), "copy_components": ["minecraft:potion_contents"] (optional)}
// copy_components lists components that are carried over from the input to the filled syringe.
public record ExtractingRecipe(Ingredient ingredient, ItemStackTemplate syringe, Optional<ItemStackTemplate> byproductTemplate,
                               List<DataComponentType<?>> copyComponents) implements Recipe<SingleRecipeInput> {

    public static final MapCodec<ExtractingRecipe> CODEC = RecordCodecBuilder.mapCodec(i -> i.group(
            Ingredient.CODEC.fieldOf("ingredient").forGetter(ExtractingRecipe::ingredient),
            ItemStackTemplate.CODEC.fieldOf("syringe").forGetter(ExtractingRecipe::syringe),
            ItemStackTemplate.CODEC.optionalFieldOf("byproduct").forGetter(ExtractingRecipe::byproductTemplate),
            DataComponentType.CODEC.listOf().optionalFieldOf("copy_components", List.of()).forGetter(ExtractingRecipe::copyComponents)
    ).apply(i, ExtractingRecipe::new));

    public static final StreamCodec<RegistryFriendlyByteBuf, ExtractingRecipe> STREAM_CODEC = StreamCodec.composite(
            Ingredient.CONTENTS_STREAM_CODEC, ExtractingRecipe::ingredient,
            ItemStackTemplate.STREAM_CODEC, ExtractingRecipe::syringe,
            ByteBufCodecs.optional(ItemStackTemplate.STREAM_CODEC), ExtractingRecipe::byproductTemplate,
            DataComponentType.STREAM_CODEC.apply(ByteBufCodecs.list()), ExtractingRecipe::copyComponents,
            ExtractingRecipe::new);

    // Finds the recipe for an off hand item. Works on both sides, the client uses the recipes synced by BMFRecipes.
    public static Optional<ExtractingRecipe> find(Level level, ItemStack input) {
        if (input.isEmpty()) {
            return Optional.empty();
        }
        SingleRecipeInput recipeInput = new SingleRecipeInput(input);
        if (level instanceof ServerLevel serverLevel) {
            return serverLevel.recipeAccess().getRecipeFor(BMFRecipes.EXTRACTING_TYPE.get(), recipeInput, serverLevel).map(RecipeHolder::value);
        }
        return BMFRecipes.clientRecipes().getRecipesFor(BMFRecipes.EXTRACTING_TYPE.get(), recipeInput, level).findFirst().map(RecipeHolder::value);
    }

    @Override
    public boolean matches(SingleRecipeInput input, Level level) {
        return ingredient.test(input.item());
    }

    // The filled syringe
    @Override
    public ItemStack assemble(SingleRecipeInput input) {
        ItemStack filled = syringe.create();
        for (DataComponentType<?> type : copyComponents) {
            copyComponent(type, input.item(), filled);
        }
        return filled;
    }

    private static <T> void copyComponent(DataComponentType<T> type, ItemStack from, ItemStack to) {
        to.copyFrom(type, from);
    }

    // What is left of the input item, or an empty stack
    public ItemStack byproduct() {
        return byproductTemplate.map(ItemStackTemplate::create).orElse(ItemStack.EMPTY);
    }

    @Override
    public boolean isSpecial() {
        return true;
    }

    @Override
    public boolean showNotification() {
        return false;
    }

    @Override
    public String group() {
        return "";
    }

    @Override
    public RecipeSerializer<ExtractingRecipe> getSerializer() {
        return BMFRecipes.EXTRACTING_SERIALIZER.get();
    }

    @Override
    public RecipeType<ExtractingRecipe> getType() {
        return BMFRecipes.EXTRACTING_TYPE.get();
    }

    @Override
    public PlacementInfo placementInfo() {
        return PlacementInfo.NOT_PLACEABLE;
    }

    @Override
    public RecipeBookCategory recipeBookCategory() {
        return RecipeBookCategories.CRAFTING_MISC;
    }
}
