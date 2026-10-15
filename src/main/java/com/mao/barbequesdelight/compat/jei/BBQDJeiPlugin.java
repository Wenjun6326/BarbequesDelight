package com.mao.barbequesdelight.compat.jei;

import com.mao.barbequesdelight.common.util.BBQDRecipesHelper;
import com.mao.barbequesdelight.registry.BBQDRecipes;
import mezz.jei.api.IModPlugin;
import mezz.jei.api.JeiPlugin;
import mezz.jei.api.registration.IRecipeRegistration;
import mezz.jei.api.registration.IRecipeCategoryRegistration;
import net.minecraft.resources.Identifier;
import net.minecraft.world.item.crafting.RecipeHolder;
import net.minecraft.world.item.crafting.display.RecipeDisplay;

import java.util.ArrayList;
import java.util.List;

/**
 * JEI integration.
 *
 * <p>Registered through the {@code jei_mod_plugin} entrypoint, so JEI is a soft dependency: the
 * mod runs fine without it and simply gains the recipe pages when it is installed.</p>
 *
 * <p>Each page renders the recipe's own {@link RecipeDisplay} - the same object the game uses -
 * so the JEI page cannot drift from actual behaviour.</p>
 */
@JeiPlugin
public class BBQDJeiPlugin implements IModPlugin {

    private static final Identifier UID =
            Identifier.fromNamespaceAndPath("barbequesdelight", "jei_plugin");

    @Override
    public Identifier getPluginUid() {
        return UID;
    }

    @Override
    public void registerCategories(IRecipeCategoryRegistration registration) {
        registration.addRecipeCategories(
                new GrillingCategory(registration.getJeiHelpers().getGuiHelper()),
                new SkeweringCategory(registration.getJeiHelpers().getGuiHelper()));
    }

    @Override
    public void registerRecipes(IRecipeRegistration registration) {
        registration.addRecipes(GrillingCategory.TYPE, displays(BBQDRecipes.GRILLING_TYPE));
        registration.addRecipes(SkeweringCategory.TYPE, displays(BBQDRecipes.SKEWERING_TYPE));
    }

    /**
     * Every display produced by the synced recipes of one of our types.
     *
     * <p>Each display is filed under the id of the recipe it came from, which is what lets JEI
     * tell the nine grilling recipes apart instead of collapsing them into a single page.</p>
     */
    private static <T extends net.minecraft.world.item.crafting.Recipe<?>> List<RecipeDisplay> displays(
            net.minecraft.world.item.crafting.RecipeType<T> type) {
        List<RecipeDisplay> out = new ArrayList<>();
        for (RecipeHolder<?> holder : BBQDRecipesHelper.allClientRecipes()) {
            if (holder.value().getType() != type) {
                continue;
            }
            for (RecipeDisplay display : holder.value().display()) {
                JeiDisplayIds.remember(display, holder.id().identifier());
                out.add(display);
            }
        }
        return out;
    }
}
