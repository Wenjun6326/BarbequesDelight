package com.mao.barbequesdelight.integration.rei;

import com.mao.barbequesdelight.BarbequesDelight;
import me.shedaniel.rei.api.client.gui.Renderer;
import me.shedaniel.rei.api.client.registry.category.CategoryRegistry;
import me.shedaniel.rei.api.client.registry.display.DisplayCategory;
import me.shedaniel.rei.api.client.registry.display.DisplayRegistry;
import me.shedaniel.rei.api.common.category.CategoryIdentifier;
import me.shedaniel.rei.api.common.display.Display;
import me.shedaniel.rei.api.common.util.EntryStacks;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.ItemLike;

/**
 * Roughly Enough Items integration.
 *
 * <p>In 26.1 recipes expose their own vanilla {@code RecipeDisplay}s through
 * {@code Recipe#display()}, which REI turns into displays automatically. This plugin therefore
 * only has to register the two categories and their workstations.</p>
 */
public class BBQDREIPlugin implements me.shedaniel.rei.api.client.plugins.REIClientPlugin {

    public static final CategoryIdentifier<Display> GRILLING =
            CategoryIdentifier.of(BarbequesDelight.asID("grilling"));
    public static final CategoryIdentifier<Display> SKEWERING =
            CategoryIdentifier.of(BarbequesDelight.asID("skewering"));

    @Override
    public void registerCategories(CategoryRegistry registry) {
        registry.add(new WorkstationCategory(GRILLING, "barbequesdelight.rei.grilling",
                com.mao.barbequesdelight.registry.BBQDItems.GRILL,
                BarbequesDelight.asID("textures/gui/grill_rei.png")));
        registry.add(new WorkstationCategory(SKEWERING, "barbequesdelight.rei.skewering",
                com.mao.barbequesdelight.registry.BBQDItems.INGREDIENTS_BASIN,
                BarbequesDelight.asID("textures/gui/skewering_rei.png")));

        registry.addWorkstations(GRILLING,
                EntryStacks.of(com.mao.barbequesdelight.registry.BBQDItems.GRILL));
        registry.addWorkstations(SKEWERING,
                EntryStacks.of(com.mao.barbequesdelight.registry.BBQDItems.INGREDIENTS_BASIN));
    }

    @Override
    public void registerDisplays(DisplayRegistry registry) {
        // Displays come from the recipes' own RecipeDisplay list.
    }

    /** Minimal category: title, icon and workstation are all REI needs for our displays. */
    private record WorkstationCategory(CategoryIdentifier<Display> id, String titleKey,
                                       ItemLike icon, Identifier background)
            implements DisplayCategory<Display> {

        @Override
        public CategoryIdentifier<? extends Display> getCategoryIdentifier() {
            return id;
        }

        @Override
        public Component getTitle() {
            return Component.translatable(titleKey);
        }

        @Override
        public Renderer getIcon() {
            return EntryStacks.of(icon);
        }
    }

    /** Kept for API symmetry with older builds. */
    public static ItemStack icon(ItemLike item) {
        return new ItemStack(item);
    }
}
