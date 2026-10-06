package com.mao.barbequesdelight;

import com.mao.barbequesdelight.common.block.client.GrillRenderer;
import com.mao.barbequesdelight.common.block.client.IngredientsBasinRenderer;
import com.mao.barbequesdelight.common.block.client.TrayRenderer;
import com.mao.barbequesdelight.common.util.BBQDRecipesHelper;
import com.mao.barbequesdelight.registry.BBQDEntityTypes;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.fabricmc.fabric.api.client.recipe.v1.sync.ClientRecipeSynchronizedEvent;
import net.fabricmc.fabric.api.client.rendering.v1.BlockEntityRendererRegistry;

@Environment(EnvType.CLIENT)
public class BarbequesDelightClient implements ClientModInitializer {

    @Override
    public void onInitializeClient() {
        // 26.1 picks the terrain render layer (solid/cutout/translucent) automatically from
        // the sprite's transparency, so the old cutout registration for the grill is gone.
        BlockEntityRendererRegistry.register(BBQDEntityTypes.GRILL, GrillRenderer::new);
        BlockEntityRendererRegistry.register(BBQDEntityTypes.INGREDIENTS_BASIN, IngredientsBasinRenderer::new);
        BlockEntityRendererRegistry.register(BBQDEntityTypes.TRAY, TrayRenderer::new);

        // Give the basin and grill their client-side recipe source. Without this the client
        // cannot match recipes at all in 26.1, so every interaction would predict PASS.
        ClientRecipeSynchronizedEvent.EVENT.register((client, recipes) ->
                BBQDRecipesHelper.setClientRecipes(recipes));
    }
}
