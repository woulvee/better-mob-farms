package com.woulvee.bettermobfarms.recipe;

import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;

import net.minecraft.core.NonNullList;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.ItemStackTemplate;
import net.minecraft.world.item.crafting.CraftingInput;
import net.minecraft.world.item.crafting.CraftingRecipe;
import net.minecraft.world.item.crafting.Recipe;
import net.minecraft.world.item.crafting.RecipeSerializer;
import net.minecraft.world.item.crafting.ShapedRecipe;
import net.minecraft.world.item.crafting.ShapedRecipePattern;

// A shaped crafting recipe that uses damageable ingredients, like the swords of the punji sticks, as tools:
// they stay in the grid and lose tool_damage durability instead of being used up. A tool without enough
// durability left breaks. It stays a ShapedRecipe, so the recipe book and recipe viewers show it as usual.
public class ToolDamagingShapedRecipe extends ShapedRecipe {
    public static final MapCodec<ToolDamagingShapedRecipe> MAP_CODEC = RecordCodecBuilder.mapCodec(i -> i.group(
            Recipe.CommonInfo.MAP_CODEC.forGetter(o -> o.commonInfo),
            CraftingRecipe.CraftingBookInfo.MAP_CODEC.forGetter(o -> o.bookInfo),
            ShapedRecipePattern.MAP_CODEC.forGetter(o -> o.pattern),
            ItemStackTemplate.CODEC.fieldOf("result").forGetter(o -> o.result),
            Codec.intRange(1, Integer.MAX_VALUE).fieldOf("tool_damage").forGetter(o -> o.toolDamage)
    ).apply(i, ToolDamagingShapedRecipe::new));

    public static final StreamCodec<RegistryFriendlyByteBuf, ToolDamagingShapedRecipe> STREAM_CODEC = StreamCodec.composite(
            Recipe.CommonInfo.STREAM_CODEC, o -> o.commonInfo,
            CraftingRecipe.CraftingBookInfo.STREAM_CODEC, o -> o.bookInfo,
            ShapedRecipePattern.STREAM_CODEC, o -> o.pattern,
            ItemStackTemplate.STREAM_CODEC, o -> o.result,
            ByteBufCodecs.VAR_INT, o -> o.toolDamage,
            ToolDamagingShapedRecipe::new);

    private final ItemStackTemplate result;
    private final int toolDamage;

    public ToolDamagingShapedRecipe(Recipe.CommonInfo commonInfo, CraftingRecipe.CraftingBookInfo bookInfo, ShapedRecipePattern pattern,
            ItemStackTemplate result, int toolDamage) {
        super(commonInfo, bookInfo, pattern, result);
        this.result = result;
        this.toolDamage = toolDamage;
    }

    @Override
    public NonNullList<ItemStack> getRemainingItems(CraftingInput input) {
        NonNullList<ItemStack> remaining = super.getRemainingItems(input);
        for (int slot = 0; slot < remaining.size(); slot++) {
            ItemStack stack = input.getItem(slot);
            if (stack.isDamageableItem()) {
                ItemStack tool = stack.copyWithCount(1);
                int damage = tool.getDamageValue() + toolDamage;
                if (damage < tool.getMaxDamage()) {
                    tool.setDamageValue(damage);
                    remaining.set(slot, tool);
                }
            }
        }
        return remaining;
    }

    // ShapedRecipe pins the serializer type to its own, this recipe has a serializer of its own type
    @Override
    @SuppressWarnings({"unchecked", "rawtypes"})
    public RecipeSerializer<ShapedRecipe> getSerializer() {
        return (RecipeSerializer) BMFRecipes.TOOL_DAMAGING_SHAPED_SERIALIZER.get();
    }
}
