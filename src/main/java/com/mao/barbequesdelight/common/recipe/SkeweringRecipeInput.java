package com.mao.barbequesdelight.common.recipe;

import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.RecipeInput;
import org.jetbrains.annotations.NotNull;

/**
 * Input for skewering: the ingredient held in a basin half, an optional garnish from the
 * player's off hand, and the tool (normally a stick) held in the main hand.
 *
 * <p>Mirrors the 1.20.1 {@code new SimpleInventory(basin, garnishes, tool)} layout.</p>
 */
public record SkeweringRecipeInput(ItemStack ingredient, ItemStack garnish, ItemStack tool) implements RecipeInput {

    @Override
    public @NotNull ItemStack getItem(int index) {
        return switch (index) {
            case 0 -> ingredient;
            case 1 -> garnish;
            case 2 -> tool;
            default -> ItemStack.EMPTY;
        };
    }

    @Override
    public int size() {
        return 3;
    }
}
