package com.mao.barbequesdelight.mixin.client;

import com.mao.barbequesdelight.common.block.blockentity.GrillBlockEntity;
import com.mao.barbequesdelight.common.network.FlipGrillPayload;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.multiplayer.MultiPlayerGameMode;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.core.BlockPos;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.phys.BlockHitResult;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

/**
 * Makes sneak-right-click turn a grill item over.
 *
 * <p>Vanilla's {@code MultiPlayerGameMode.performUseItemOn} deliberately skips the block
 * interaction entirely when the player is sneaking <em>and</em> holding an item:</p>
 *
 * <pre>
 *   boolean skipBlock = isSecondaryUseActive() &amp;&amp; hasItemInHand;
 *   if (!skipBlock) { ... BlockState.useItemOn(...) ... } else { ... sendUseItem ... }
 * </pre>
 *
 * <p>That rule exists so sneaking with a block in hand places it instead of opening a chest. It
 * also means the grill never hears about the click, which is why the flip prompt appeared while
 * sneak + right click did nothing.</p>
 *
 * <p>Rather than trying to smuggle the intent through the block interaction (and depending on
 * the server's sneak flag being in step, which it demonstrably is not), this sends an explicit
 * {@link FlipGrillPayload}. The server re-validates reach and readiness and performs the flip
 * from its own state, so nothing here has to be trusted.</p>
 */
@Mixin(MultiPlayerGameMode.class)
public abstract class GrillFlipMixin {

    @Inject(method = "performUseItemOn", at = @At("HEAD"), cancellable = true)
    private void bbqd$flipGrillOnSneak(LocalPlayer player, InteractionHand hand, BlockHitResult hit,
                                       CallbackInfoReturnable<InteractionResult> cir) {
        if (!player.isSecondaryUseActive()) {
            return;
        }

        if (!(player.level() instanceof ClientLevel level)) {
            return;
        }

        BlockPos pos = hit.getBlockPos();

        if (!(level.getBlockEntity(pos) instanceof GrillBlockEntity grill)) {
            return;
        }

        // getSlotForHitting answers with the slot count when the crosshair is off the top face.
        int slot = grill.getSlotForHitting(hit, level);
        if (!grill.canFlip(slot)) {
            return;
        }

        Minecraft minecraft = Minecraft.getInstance();
        if (minecraft.getConnection() == null) {
            return;
        }

        net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking
                .send(new FlipGrillPayload(pos));

        // Predict locally so the item turns immediately; the server's block update confirms it.
        grill.flip(slot);
        cir.setReturnValue(InteractionResult.SUCCESS);
    }
}
