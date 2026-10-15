package com.mao.barbequesdelight;

import com.mao.barbequesdelight.client.GrillFlipOverlay;
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
import net.fabricmc.fabric.api.client.rendering.v1.hud.HudElementRegistry;
import net.fabricmc.fabric.api.client.rendering.v1.hud.VanillaHudElements;
import net.minecraft.resources.Identifier;

@Environment(EnvType.CLIENT)
public class BarbequesDelightClient implements ClientModInitializer {

    private static final Identifier FLIP_HINT =
            Identifier.fromNamespaceAndPath("barbequesdelight", "grill_flip_hint");

    @Override
    public void onInitializeClient() {
        // 26.1 picks the terrain render layer (solid/cutout/translucent) automatically from
        // the sprite's transparency, so the old cutout registration for the grill is gone.
        BlockEntityRendererRegistry.register(BBQDEntityTypes.GRILL, GrillRenderer::new);
        BlockEntityRendererRegistry.register(BBQDEntityTypes.INGREDIENTS_BASIN, IngredientsBasinRenderer::new);
        BlockEntityRendererRegistry.register(BBQDEntityTypes.TRAY, TrayRenderer::new);

        // "Sneak right click to flip" hint, attached just after the crosshair so it sits right
        // below the aim point, matching the Forge original's overlay.
        HudElementRegistry.attachElementAfter(VanillaHudElements.CROSSHAIR, FLIP_HINT,
                new GrillFlipOverlay());

        // Give the basin and grill their client-side recipe source. Without this the client
        // cannot match recipes at all in 26.1, so every interaction would predict PASS.
        ClientRecipeSynchronizedEvent.EVENT.register((client, recipes) ->
                BBQDRecipesHelper.setClientRecipes(recipes));
    }
}
