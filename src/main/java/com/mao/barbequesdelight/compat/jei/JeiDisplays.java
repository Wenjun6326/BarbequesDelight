package com.mao.barbequesdelight.compat.jei;

import net.minecraft.world.item.crafting.display.FurnaceRecipeDisplay;
import net.minecraft.world.item.crafting.display.RecipeDisplay;
import net.minecraft.world.item.crafting.display.ShapelessCraftingRecipeDisplay;
import net.minecraft.world.item.crafting.display.SlotDisplay;

import java.util.List;

/**
 * Pulls the pieces out of a vanilla {@link RecipeDisplay}.
 *
 * <p>Both of this mod's recipe types deliberately report an existing display shape rather than a
 * bespoke one: grilling is a furnace-style display (one input, one output, a duration) and
 * skewering is shapeless (the ingredient order is irrelevant). Reusing them keeps the JSON-facing
 * behaviour identical to the Forge original while still giving JEI something concrete to draw.</p>
 */
final class JeiDisplays {

    private JeiDisplays() {
    }

    static List<SlotDisplay> inputs(RecipeDisplay display) {
        if (display instanceof FurnaceRecipeDisplay furnace) {
            return List.of(furnace.ingredient());
        }
        if (display instanceof ShapelessCraftingRecipeDisplay shapeless) {
            return shapeless.ingredients();
        }
        return List.of();
    }

    static List<SlotDisplay> results(RecipeDisplay display) {
        if (display instanceof FurnaceRecipeDisplay furnace) {
            return List.of(furnace.result());
        }
        if (display instanceof ShapelessCraftingRecipeDisplay shapeless) {
            return List.of(shapeless.result());
        }
        return List.of();
    }

    /** Cooking seconds, or 0 when the display carries no duration. */
    static int durationSeconds(RecipeDisplay display) {
        if (display instanceof FurnaceRecipeDisplay furnace) {
            return Math.max(1, furnace.duration() / 20);
        }
        return 0;
    }
}
