package com.mao.barbequesdelight.compat.jei;

import com.mao.barbequesdelight.BarbequesDelight;
import com.mao.barbequesdelight.registry.BBQDItems;
import mezz.jei.api.gui.builder.IRecipeLayoutBuilder;
import mezz.jei.api.gui.drawable.IDrawable;
import mezz.jei.api.gui.placement.HorizontalAlignment;
import mezz.jei.api.gui.placement.VerticalAlignment;
import mezz.jei.api.gui.widgets.IRecipeExtrasBuilder;
import mezz.jei.api.helpers.IGuiHelper;
import mezz.jei.api.recipe.IFocusGroup;
import mezz.jei.api.recipe.category.IRecipeCategory;
import mezz.jei.api.recipe.types.IRecipeType;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import net.minecraft.world.item.crafting.display.RecipeDisplay;
import net.minecraft.world.item.crafting.display.SlotDisplay;

import java.util.List;

/**
 * JEI category for grilling: one skewer in, one skewer out, with the per-side cooking time.
 *
 * <p>26.1 lets a recipe describe itself to viewers through {@link RecipeDisplay}, but JEI still
 * needs a category to lay that display out in - and it separates the entries by
 * {@link #getIdentifier}, which is why {@link JeiDisplayIds} exists.</p>
 */
public class GrillingCategory implements IRecipeCategory<RecipeDisplay> {

    public static final IRecipeType<RecipeDisplay> TYPE =
            IRecipeType.create(BarbequesDelight.MODID, "grilling", RecipeDisplay.class);

    private static final int WIDTH = 116;
    private static final int HEIGHT = 58;
    /** Caption line height, so slots can start below it instead of overlapping it. */
    private static final int CAPTION_HEIGHT = 12;

    private final IDrawable icon;

    public GrillingCategory(IGuiHelper guiHelper) {
        this.icon = guiHelper.createDrawableItemLike(BBQDItems.GRILL);
    }

    @Override
    public IRecipeType<RecipeDisplay> getRecipeType() {
        return TYPE;
    }

    @Override
    public Identifier getIdentifier(RecipeDisplay display) {
        return JeiDisplayIds.of(display);
    }

    @Override
    public Component getTitle() {
        return Component.translatable("barbequesdelight.jei.grilling");
    }

    @Override
    public int getWidth() {
        return WIDTH;
    }

    @Override
    public int getHeight() {
        return HEIGHT;
    }

    @Override
    public IDrawable getIcon() {
        return icon;
    }

    @Override
    public void setRecipe(IRecipeLayoutBuilder builder, RecipeDisplay display, IFocusGroup focuses) {
        List<SlotDisplay> inputs = JeiDisplays.inputs(display);
        List<SlotDisplay> results = JeiDisplays.results(display);

        // Slots sit below the caption line. Placing them higher made the cooking-time text
        // overlap the slot frames.
        var input = builder.addInputSlot(12, 24).setStandardSlotBackground();
        for (SlotDisplay slot : inputs) {
            input.add(slot);
        }

        var output = builder.addOutputSlot(78, 24).setOutputSlotBackground();
        for (SlotDisplay slot : results) {
            output.add(slot);
        }
    }

    @Override
    public void createRecipeExtras(IRecipeExtrasBuilder builder, RecipeDisplay display, IFocusGroup focuses) {
        int seconds = JeiDisplays.durationSeconds(display);
        if (seconds > 0) {
            builder.addText(Component.translatable("barbequesdelight.jei.time", seconds),
                            WIDTH, CAPTION_HEIGHT)
                    .setTextAlignment(HorizontalAlignment.CENTER)
                    .setPosition(0, 2, WIDTH, CAPTION_HEIGHT,
                            HorizontalAlignment.CENTER, VerticalAlignment.TOP);
        }
        builder.addRecipeArrow().setPosition(42, 24);
    }
}
