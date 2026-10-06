package com.mao.barbequesdelight.registry;

import com.mao.barbequesdelight.BarbequesDelight;
import com.mao.barbequesdelight.common.block.blockentity.GrillBlockEntity;
import com.mao.barbequesdelight.common.block.blockentity.IngredientsBasinBlockEntity;
import com.mao.barbequesdelight.common.block.blockentity.TrayBlockEntity;
import net.fabricmc.fabric.api.object.builder.v1.block.entity.FabricBlockEntityTypeBuilder;
import net.minecraft.core.Registry;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.level.block.entity.BlockEntityType;

public class BBQDEntityTypes {

    public static final BlockEntityType<GrillBlockEntity> GRILL = register(
            "grill_block_entity",
            FabricBlockEntityTypeBuilder.create(GrillBlockEntity::new, BBQDBlocks.GRILL).build());

    public static final BlockEntityType<IngredientsBasinBlockEntity> INGREDIENTS_BASIN = register(
            "ingredients_basin_block_entity",
            FabricBlockEntityTypeBuilder.create(IngredientsBasinBlockEntity::new, BBQDBlocks.INGREDIENTS_BASIN).build());

    public static final BlockEntityType<TrayBlockEntity> TRAY = register(
            "tray_block_entity",
            FabricBlockEntityTypeBuilder.create(TrayBlockEntity::new, BBQDBlocks.TRAY).build());

    /**
     * 26.1 made {@code BlockEntityType}'s constructor private and removed its builder, so
     * Fabric API's {@code FabricBlockEntityTypeBuilder} is used to construct the instance.
     */
    private static <T extends BlockEntityType<?>> T register(String id, T type) {
        ResourceKey<BlockEntityType<?>> key =
                ResourceKey.create(Registries.BLOCK_ENTITY_TYPE, BarbequesDelight.asID(id));
        return Registry.register(BuiltInRegistries.BLOCK_ENTITY_TYPE, key, type);
    }

    public static void registerBBQDEntityTypes() {
        BarbequesDelight.LOGGER.debug("Register BBQD EntityTypes For" + BarbequesDelight.MODID);
    }
}
