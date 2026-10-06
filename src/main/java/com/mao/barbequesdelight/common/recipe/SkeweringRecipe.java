package com.mao.barbequesdelight.common.recipe;

import com.mao.barbequesdelight.registry.BBQDRecipes;
import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.ItemStackTemplate;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.item.crafting.PlacementInfo;
import net.minecraft.world.item.crafting.Recipe;
import net.minecraft.world.item.crafting.RecipeBookCategory;
import net.minecraft.world.item.crafting.RecipeSerializer;
import net.minecraft.world.item.crafting.RecipeType;
import net.minecraft.world.level.Level;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.List;

/**
 * Skewering: 1-2 ingredient stacks combined with a tool (normally a stick) in a basin half.
 *
 * <p>JSON shape is unchanged from 1.20.1:</p>
 * <pre>{@code
 * { "type": "barbequesdelight:skewering",
 *   "ingredients": [ { "item": "minecraft:brown_mushroom" }, { "item": "minecraft:beef" } ],
 *   "result": { "item": "barbequesdelight:beef_skewer" },
 *   "count": 2 }
 * }</pre>
 *
 * <p>Consumption of the tool and ingredients happens in
 * {@code IngredientsBasinBlockEntity#skewer} because 26.1's {@code assemble} is defined as a
 * pure result builder. Inferring the implicit tool from the result's crafting remainder
 * reproduces the original behaviour, where every raw skewer requires a stick.</p>
 */
public class SkeweringRecipe implements Recipe<SkeweringRecipeInput> {

    /** Maximum number of ingredient slots a skewering recipe may declare. */
    public static final int MAX_INGREDIENTS = 2;

    private final List<Ingredient> ingredients;
    private final ItemStackTemplate result;
    private final ItemStackTemplate tool;
    private final int ingredientCount;

    public SkeweringRecipe(List<Ingredient> ingredients, ItemStackTemplate result,
                           ItemStackTemplate tool, int ingredientCount) {
        this.ingredients = List.copyOf(ingredients);
        this.result = result;
        // Only the explicit "container" is stored. The implicit tool is derived lazily in
        // #resolveTool() because item components are not bound yet while datapacks load.
        this.tool = tool;
        this.ingredientCount = ingredientCount;
    }

    public List<Ingredient> getIngredientList() {
        return ingredients;
    }

    public ItemStackTemplate getResultTemplate() {
        return result;
    }

    /** The explicit tool template, if one was declared (otherwise inferred). */
    public java.util.Optional<ItemStackTemplate> getToolTemplate() {
        return java.util.Optional.ofNullable(tool);
    }

    public ItemStack getResultStack() {
        return result.create();
    }

    /**
     * Resolves the required tool at runtime: an explicit {@code container} wins, otherwise the
     * result's crafting remainder is used (which is how every shipped skewer requires a stick).
     *
     * <p>Deferred because {@code ItemStackTemplate#create} cannot run while datapacks load.</p>
     */
    @Nullable
    public ItemStackTemplate resolveTool() {
        if (tool != null) {
            return tool;
        }
        ItemStackTemplate remainder = result.create().getItem().getCraftingRemainder();
        return (remainder != null && !remainder.create().isEmpty()) ? remainder : null;
    }

    /** The tool required, or an empty stack when the recipe needs none. */
    public ItemStack getTool() {
        ItemStackTemplate resolved = resolveTool();
        return resolved == null ? ItemStack.EMPTY : resolved.create();
    }

    public boolean hasTool() {
        return resolveTool() != null;
    }

    public int getIngredientCount() {
        return ingredientCount;
    }

    @Override
    public boolean matches(SkeweringRecipeInput input, @NotNull Level level) {
        // Every ingredient slot of the recipe must be satisfied by a distinct non-empty
        // input slot, and no extra input slots may be filled - the 1.20.1 semantics.
        List<ItemStack> inputs = List.of(input.ingredient(), input.garnish());

        int filled = 0;
        for (ItemStack stack : inputs) {
            if (!stack.isEmpty()) {
                filled++;
            }
        }
        if (filled != ingredients.size()) {
            return false;
        }

        boolean[] used = new boolean[inputs.size()];
        for (Ingredient ingredient : ingredients) {
            boolean matched = false;
            for (int i = 0; i < inputs.size(); i++) {
                if (!used[i] && !inputs.get(i).isEmpty() && ingredient.test(inputs.get(i))) {
                    used[i] = true;
                    matched = true;
                    break;
                }
            }
            if (!matched) {
                return false;
            }
        }

        // The tool is compared by item identity only, ignoring count and components.
        ItemStackTemplate requiredTemplate = resolveTool();
        if (requiredTemplate == null) {
            return true;
        }
        ItemStack required = requiredTemplate.create();
        ItemStack held = input.tool();
        return !held.isEmpty() && ItemStack.isSameItem(required, held);
    }

    @Override
    public @NotNull ItemStack assemble(SkeweringRecipeInput input) {
        return result.create();
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
    public @NotNull String group() {
        return "";
    }

    @Override
    public @NotNull PlacementInfo placementInfo() {
        return PlacementInfo.create(ingredients);
    }

    @Override
    public @NotNull RecipeBookCategory recipeBookCategory() {
        return BBQDRecipes.SKEWERING_BOOK_CATEGORY;
    }

    @Override
    public @NotNull RecipeSerializer<? extends Recipe<SkeweringRecipeInput>> getSerializer() {
        return BBQDRecipes.SKEWERING_SERIALIZER;
    }

    @Override
    public @NotNull RecipeType<? extends Recipe<SkeweringRecipeInput>> getType() {
        return BBQDRecipes.SKEWERING_TYPE;
    }

    /**
     * Viewer integration. 26.1 exposes recipes to recipe viewers through vanilla
     * {@code RecipeDisplay}s; the tool is shown as an extra ingredient so the stick
     * requirement stays visible.
     */
    @Override
    public @NotNull java.util.List<net.minecraft.world.item.crafting.display.RecipeDisplay> display() {
        java.util.List<net.minecraft.world.item.crafting.display.SlotDisplay> shown =
                new java.util.ArrayList<>();
        for (Ingredient ingredient : ingredients) {
            shown.add(ingredient.display());
        }
        if (resolveTool() != null) {
            shown.add(new net.minecraft.world.item.crafting.display.SlotDisplay.ItemStackSlotDisplay(resolveTool()));
        }

        return java.util.List.of(new net.minecraft.world.item.crafting.display.ShapelessCraftingRecipeDisplay(
                shown,
                new net.minecraft.world.item.crafting.display.SlotDisplay.ItemStackSlotDisplay(result),
                new net.minecraft.world.item.crafting.display.SlotDisplay.ItemSlotDisplay(
                        com.mao.barbequesdelight.registry.BBQDItems.INGREDIENTS_BASIN)));
    }

    public static final MapCodec<SkeweringRecipe> CODEC = RecordCodecBuilder.mapCodec(instance -> instance.group(
            Ingredient.CODEC.listOf(1, MAX_INGREDIENTS).fieldOf("ingredients")
                    .forGetter(SkeweringRecipe::getIngredientList),
            ItemStackTemplate.CODEC.fieldOf("result").forGetter(SkeweringRecipe::getResultTemplate),
            // Optional: when absent the tool is inferred from the result's crafting remainder.
            ItemStackTemplate.CODEC.optionalFieldOf("container").forGetter(SkeweringRecipe::getToolTemplate),
            Codec.INT.optionalFieldOf("count", 2).forGetter(SkeweringRecipe::getIngredientCount)
    ).apply(instance, (ingredients, result, tool, count) ->
            new SkeweringRecipe(ingredients, result, tool.orElse(null), count)));

    public static final StreamCodec<RegistryFriendlyByteBuf, SkeweringRecipe> STREAM_CODEC = StreamCodec.composite(
            Ingredient.CONTENTS_STREAM_CODEC.apply(ByteBufCodecs.list()), SkeweringRecipe::getIngredientList,
            ItemStackTemplate.STREAM_CODEC, SkeweringRecipe::getResultTemplate,
            ItemStackTemplate.STREAM_CODEC.apply(ByteBufCodecs::optional), SkeweringRecipe::getToolTemplate,
            ByteBufCodecs.VAR_INT, SkeweringRecipe::getIngredientCount,
            (ingredients, result, tool, count) -> new SkeweringRecipe(ingredients, result, tool.orElse(null), count));
}
