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
 * JEI category for skewering.
 *
 * <p>The recipe reports a shapeless display because the ingredient order genuinely does not
 * matter, but showing it as a crafting-table recipe would be misleading: skewering happens in
 * the basin, with a stick in the main hand and the garnish in the off hand. This category gives
 * it its own page and says so.</p>
 */
public class SkeweringCategory implements IRecipeCategory<RecipeDisplay> {

    public static final IRecipeType<RecipeDisplay> TYPE =
            IRecipeType.create(BarbequesDelight.MODID, "skewering", RecipeDisplay.class);

    private static final int WIDTH = 132;
    private static final int HEIGHT = 62;
    private static final int CAPTION_HEIGHT = 10;

    private final IDrawable icon;

    public SkeweringCategory(IGuiHelper guiHelper) {
        this.icon = guiHelper.createDrawableItemLike(BBQDItems.INGREDIENTS_BASIN);
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
        return Component.translatable("barbequesdelight.jei.skewering");
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
        List<SlotDisplay> ingredients = JeiDisplays.inputs(display);
        List<SlotDisplay> results = JeiDisplays.results(display);

        // Unordered ingredients, which is exactly what the shapeless marker conveys.
        builder.setShapeless(6, 16);

        int x = 10;
        for (SlotDisplay ingredient : ingredients) {
            builder.addInputSlot(x, 28).setStandardSlotBackground().add(ingredient);
            x += 22;
        }

        var output = builder.addOutputSlot(x + 10, 28).setOutputSlotBackground();
        for (SlotDisplay result : results) {
            output.add(result);
        }
    }

    @Override
    public void createRecipeExtras(IRecipeExtrasBuilder builder, RecipeDisplay display, IFocusGroup focuses) {
        // Top caption names the station; the bottom caption carries the two-hand requirement,
        // which the Forge original also spelled out on its JEI page.
        builder.addText(Component.translatable("barbequesdelight.jei.item_basin"),
                        WIDTH, CAPTION_HEIGHT)
                .setTextAlignment(HorizontalAlignment.CENTER)
                .setPosition(0, 2, WIDTH, CAPTION_HEIGHT, HorizontalAlignment.CENTER, VerticalAlignment.TOP);

        builder.addText(Component.translatable("barbequesdelight.jei.mainhand"),
                        WIDTH, CAPTION_HEIGHT)
                .setTextAlignment(HorizontalAlignment.CENTER)
                .setPosition(0, HEIGHT - CAPTION_HEIGHT - 2, WIDTH, CAPTION_HEIGHT,
                        HorizontalAlignment.CENTER, VerticalAlignment.BOTTOM);
    }
}
