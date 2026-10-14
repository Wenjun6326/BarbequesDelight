package com.mao.barbequesdelight.registry;

import com.mao.barbequesdelight.BarbequesDelight;
import com.mao.barbequesdelight.common.block.GrillBlock;
import com.mao.barbequesdelight.common.block.IngredientsBasinBlock;
import com.mao.barbequesdelight.common.block.TrayBlock;
import net.minecraft.core.Registry;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockBehaviour;
import vectorwing.farmersdelight.common.registry.ModBlocks;

import java.util.function.Function;

public class BBQDBlocks {
    /**
     * The grill lights up when it holds a campfire, matching the Forge original. 26.1 makes light
     * a construction-time property ({@code Properties.lightLevel}) rather than an overridable
     * method, so it is declared here.
     */
    public static final Block GRILL = register("grill",
            props -> new GrillBlock(props), BlockBehaviour.Properties.ofLegacyCopy(ModBlocks.SKILLET.get())
                    .lightLevel(state -> state.getValue(GrillBlock.HAS_CAMPFIRE) ? 15 : 0));

    public static final Block INGREDIENTS_BASIN = register("ingredients_basin",
            props -> new IngredientsBasinBlock(props), BlockBehaviour.Properties.ofLegacyCopy(Blocks.OAK_PLANKS));

    public static final Block TRAY = register("tray",
            props -> new TrayBlock(props), BlockBehaviour.Properties.ofLegacyCopy(Blocks.OAK_PLANKS));

    private static Block register(String id, Function<BlockBehaviour.Properties, Block> factory,
                                  BlockBehaviour.Properties properties) {
        ResourceKey<Block> key = ResourceKey.create(Registries.BLOCK, BarbequesDelight.asID(id));
        Block block = factory.apply(properties.setId(key));
        return Registry.register(BuiltInRegistries.BLOCK, key, block);
    }

    public static void registerBBQDBlocks() {
        BarbequesDelight.LOGGER.debug("Register BBQD Blocks For" + BarbequesDelight.MODID);
    }
}
