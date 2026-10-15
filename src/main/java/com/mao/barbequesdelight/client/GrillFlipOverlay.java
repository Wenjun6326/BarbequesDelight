package com.mao.barbequesdelight.client;

import com.mao.barbequesdelight.common.block.blockentity.GrillBlockEntity;
import net.fabricmc.fabric.api.client.rendering.v1.hud.HudElement;
import net.minecraft.client.DeltaTracker;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.network.chat.Component;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.Vec3;

/**
 * Draws "sneak right click to flip" under the crosshair when the player looks at a grill half
 * that is ready to be turned.
 *
 * <p>Ported from the Forge original's {@code content/block/BBQOverlay}. The Forge version reads
 * the block the player is aiming at through l2library's {@code RayTraceUtil}; here the same
 * information comes from {@code Entity#pick}, which is what vanilla uses for the crosshair, so
 * the hint appears under exactly the same conditions as the target outline.</p>
 */
public class GrillFlipOverlay implements HudElement {

    @Override
    public void extractRenderState(GuiGraphicsExtractor graphics, DeltaTracker delta) {
        Minecraft minecraft = Minecraft.getInstance();
        LocalPlayer player = minecraft.player;
        if (player == null || minecraft.level == null || minecraft.options.hideGui) {
            return;
        }

        BlockHitResult hit = pickGrill(player, delta);
        if (hit == null) {
            return;
        }

        if (!(minecraft.level.getBlockEntity(hit.getBlockPos()) instanceof GrillBlockEntity grill)) {
            return;
        }

        int slot = grill.getSlotForHitting(hit, minecraft.level);
        if (!grill.canFlip(slot)) {
            return;
        }

        Component text = Component.translatable("barbequesdelight.info.flip");
        graphics.centeredText(minecraft.font, text,
                graphics.guiWidth() / 2,
                graphics.guiHeight() / 2 + 16,
                0xFFFFFFFF);
    }

    /** The grill the crosshair is on, or {@code null} when it is not on a grill. */
    private static BlockHitResult pickGrill(LocalPlayer player, DeltaTracker delta) {
        HitResult hit = player.pick(player.blockInteractionRange(), delta.getGameTimeDeltaPartialTick(false), false);
        if (hit.getType() == HitResult.Type.BLOCK && hit instanceof BlockHitResult blockHit) {
            Vec3 location = blockHit.getLocation();
            return new BlockHitResult(location, blockHit.getDirection(), blockHit.getBlockPos(),
                    blockHit.isInside());
        }
        return null;
    }
}
