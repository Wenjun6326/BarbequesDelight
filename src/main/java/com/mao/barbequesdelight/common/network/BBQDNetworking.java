package com.mao.barbequesdelight.common.network;

import com.mao.barbequesdelight.common.block.blockentity.GrillBlockEntity;
import net.fabricmc.fabric.api.networking.v1.PayloadTypeRegistry;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.level.block.entity.BlockEntity;

/** Wiring for {@link FlipGrillPayload}: one serverbound type plus its authoritative handler. */
public final class BBQDNetworking {

    private BBQDNetworking() {
    }

    public static void registerPayloads() {
        PayloadTypeRegistry.serverboundPlay().register(FlipGrillPayload.TYPE, FlipGrillPayload.STREAM_CODEC);
    }

    public static void registerServerHandlers() {
        ServerPlayNetworking.registerGlobalReceiver(FlipGrillPayload.TYPE,
                (payload, context) -> context.server().execute(() -> handleFlip(context.player(), payload.pos())));
    }

    /**
     * Turns a grill half over, validated entirely from server state.
     *
     * <p>The client is only trusted to say "the player asked to flip this block". Reach, the
     * presence of a grill and whether that half is actually ready are all re-checked here, so a
     * forged packet cannot flip anything the player could not have flipped.</p>
     */
    private static void handleFlip(ServerPlayer player, BlockPos pos) {
        ServerLevel level = player.level();

        if (player.distanceToSqr(pos.getX() + 0.5, pos.getY() + 0.5, pos.getZ() + 0.5)
                > Math.pow(player.blockInteractionRange() + 1.0, 2.0)) {
            return;
        }
        if (!level.isLoaded(pos) || !level.hasChunkAt(pos)) {
            return;
        }

        BlockEntity entity = level.getBlockEntity(pos);
        if (!(entity instanceof GrillBlockEntity grill)) {
            return;
        }

        // Either half may be the one the player meant; flip whichever is actually ready.
        for (int slot = 0; slot < grill.getContainerSize(); slot++) {
            if (grill.canFlip(slot)) {
                grill.flip(slot);
                return;
            }
        }
    }
}
