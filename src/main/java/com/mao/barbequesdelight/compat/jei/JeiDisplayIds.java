package com.mao.barbequesdelight.compat.jei;

import com.mao.barbequesdelight.BarbequesDelight;
import net.minecraft.resources.Identifier;
import net.minecraft.world.item.crafting.display.RecipeDisplay;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Remembers which recipe produced which {@link RecipeDisplay}.
 *
 * <p>JEI distinguishes the entries inside one category by {@code IRecipeCategory#getIdentifier}.
 * The display objects themselves carry no id, so without this every grilling recipe looked like
 * the same entry and JEI showed a single page ("1/1") instead of all nine.</p>
 */
final class JeiDisplayIds {

    private static final Map<RecipeDisplay, Identifier> IDS = new ConcurrentHashMap<>();

    private JeiDisplayIds() {
    }

    static void remember(RecipeDisplay display, Identifier recipeId) {
        IDS.put(display, recipeId);
    }

    /** The recipe this display came from, or a synthetic id so JEI still gets something unique. */
    static Identifier of(RecipeDisplay display) {
        Identifier known = IDS.get(display);
        if (known != null) {
            return known;
        }
        return Identifier.fromNamespaceAndPath(BarbequesDelight.MODID,
                "display/" + Integer.toHexString(System.identityHashCode(display)));
    }
}
