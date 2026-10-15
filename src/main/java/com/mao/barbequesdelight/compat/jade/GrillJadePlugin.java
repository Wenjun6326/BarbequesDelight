package com.mao.barbequesdelight.compat.jade;

import com.mao.barbequesdelight.common.block.GrillBlock;
import com.mao.barbequesdelight.registry.BBQDBlocks;
import net.fabricmc.api.EnvType;
import net.fabricmc.loader.api.FabricLoader;
import snownee.jade.api.IWailaClientRegistration;
import snownee.jade.api.IWailaPlugin;

/**
 * Registers the grill's Jade tooltip.
 *
 * <p>Wired through the {@code jade} entrypoint in {@code fabric.mod.json}, which Jade only
 * loads when Jade itself is present. Jade is therefore a soft dependency: the mod still runs
 * without it and simply gains the tooltip when it is installed.</p>
 */
public class GrillJadePlugin implements IWailaPlugin {

    @Override
    public void registerClient(IWailaClientRegistration registration) {
        // Jade's tooltip API is client-only; on a dedicated server none of this is reachable.
        if (FabricLoader.getInstance().getEnvironmentType() != EnvType.CLIENT) {
            return;
        }
        if (BBQDBlocks.GRILL instanceof GrillBlock) {
            registration.registerBlockComponent(GrillInfo.INSTANCE, GrillBlock.class);
        }
    }
}
