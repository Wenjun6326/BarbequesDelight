package com.mao.barbequesdelight.common.network;

import com.mao.barbequesdelight.BarbequesDelight;
import net.minecraft.core.BlockPos;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.Identifier;

/**
 * "Turn this grill half over", sent by the client when the player sneak-right-clicks a grill.
 *
 * <p>Carrying the intent explicitly avoids a 26.1 trap: vanilla's {@code MultiPlayerGameMode}
 * skips the block interaction entirely when the player sneaks while holding an item, and the
 * server-side sneak flag it would otherwise rely on is not reliably in step with the client.
 * Sending the flip as its own message means the server performs it from the authoritative
 * block-entity state, with no dependence on sneak propagation.</p>
 */
public record FlipGrillPayload(BlockPos pos) implements CustomPacketPayload {

    public static final CustomPacketPayload.Type<FlipGrillPayload> TYPE =
            new CustomPacketPayload.Type<>(
                    Identifier.fromNamespaceAndPath(BarbequesDelight.MODID, "flip_grill"));

    public static final StreamCodec<RegistryFriendlyByteBuf, FlipGrillPayload> STREAM_CODEC =
            StreamCodec.composite(
                    BlockPos.STREAM_CODEC, FlipGrillPayload::pos,
                    FlipGrillPayload::new);

    @Override
    public CustomPacketPayload.Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }
}
