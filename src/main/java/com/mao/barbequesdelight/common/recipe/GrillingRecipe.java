package com.mao.barbequesdelight.common.recipe;

import com.mao.barbequesdelight.registry.BBQDItems;
import com.mao.barbequesdelight.registry.BBQDRecipes;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.ItemStackTemplate;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.item.crafting.PlacementInfo;
import net.minecraft.world.item.crafting.Recipe;
import net.minecraft.world.item.crafting.RecipeBookCategory;
import net.minecraft.world.item.crafting.RecipeSerializer;
import net.minecraft.world.item.crafting.RecipeType;
import net.minecraft.world.item.crafting.SingleRecipeInput;
import net.minecraft.world.level.Level;
import org.jetbrains.annotations.NotNull;

/**
 * Grilling: one input item plus a grilling time, producing one result.
 *
 * <p>JSON shape is unchanged from 1.20.1:</p>
 * <pre>{@code
 * { "type": "barbequesdelight:grilling",
 *   "ingredient": { "item": "barbequesdelight:beef_skewer" },
 *   "result": "barbequesdelight:grilled_beef_skewer",
 *   "grillingtime": 360 }
 * }</pre>
 */
public class GrillingRecipe implements Recipe<SingleRecipeInput> {

    private final Ingredient ingredient;
    private final ItemStackTemplate result;
    private final int grillingTime;

    public GrillingRecipe(Ingredient ingredient, ItemStackTemplate result, int grillingTime) {
        this.ingredient = ingredient;
        this.result = result;
        this.grillingTime = grillingTime;
    }

    public int getGrillingTime() {
        return grillingTime;
    }

    /** Backwards-compatible accessor name used by the 1.20.1 code base. */
    public int getGrillingtime() {
        return grillingTime;
    }

    public Ingredient getIngredient() {
        return ingredient;
    }

    public ItemStackTemplate getResultTemplate() {
        return result;
    }

    /** The cooked result, as a plain stack (vanilla result accessor). */
    public ItemStack getResultStack() {
        return result.create();
    }

    @Override
    public boolean matches(SingleRecipeInput input, @NotNull Level level) {
        return ingredient.test(input.item());
    }

    @Override
    public @NotNull ItemStack assemble(SingleRecipeInput input) {
        return result.create();
    }

    @Override
    public boolean isSpecial() {
        // Was isIgnoredInRecipeBook() in 1.20.1.
        return true;
    }

    @Override
    public boolean showNotification() {
        return false;
    }

    @Override
    public @NotNull String group() {
        return "";
    }

    @Override
    public @NotNull PlacementInfo placementInfo() {
        return PlacementInfo.create(ingredient);
    }

    @Override
    public @NotNull RecipeBookCategory recipeBookCategory() {
        return BBQDRecipes.GRILLING_BOOK_CATEGORY;
    }

    @Override
    public @NotNull RecipeSerializer<? extends Recipe<SingleRecipeInput>> getSerializer() {
        return BBQDRecipes.GRILLING_SERIALIZER;
    }

    @Override
    public @NotNull RecipeType<? extends Recipe<SingleRecipeInput>> getType() {
        return BBQDRecipes.GRILLING_TYPE;
    }

    /**
     * Viewer integration. 26.1 exposes recipes to recipe viewers through vanilla
     * {@code RecipeDisplay}s, so no viewer-specific plugin code is needed for the data itself.
     */
    @Override
    public @NotNull java.util.List<net.minecraft.world.item.crafting.display.RecipeDisplay> display() {
        // FurnaceRecipeDisplay(ingredient, fuel, result, craftingStation, duration, experience)
        return java.util.List.of(new net.minecraft.world.item.crafting.display.FurnaceRecipeDisplay(
                ingredient.display(),
                net.minecraft.world.item.crafting.display.SlotDisplay.Empty.INSTANCE,
                new net.minecraft.world.item.crafting.display.SlotDisplay.ItemStackSlotDisplay(result),
                new net.minecraft.world.item.crafting.display.SlotDisplay.ItemSlotDisplay(BBQDItems.GRILL),
                grillingTime,
                0.0F));
    }

    public static final MapCodec<GrillingRecipe> CODEC = RecordCodecBuilder.mapCodec(instance -> instance.group(
            Ingredient.CODEC.fieldOf("ingredient").forGetter(GrillingRecipe::getIngredient),
            ItemStackTemplate.CODEC.fieldOf("result").forGetter(GrillingRecipe::getResultTemplate),
            com.mojang.serialization.Codec.INT.fieldOf("grillingtime").forGetter(GrillingRecipe::getGrillingTime)
    ).apply(instance, GrillingRecipe::new));

    public static final StreamCodec<RegistryFriendlyByteBuf, GrillingRecipe> STREAM_CODEC = StreamCodec.composite(
            Ingredient.CONTENTS_STREAM_CODEC, GrillingRecipe::getIngredient,
            ItemStackTemplate.STREAM_CODEC, GrillingRecipe::getResultTemplate,
            net.minecraft.network.codec.ByteBufCodecs.VAR_INT, GrillingRecipe::getGrillingTime,
            GrillingRecipe::new);
}
