package com.mao.barbequesdelight;

import com.mao.barbequesdelight.registry.*;
import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.registry.CompostableRegistry;
import net.minecraft.core.Registry;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.ItemStack;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class BarbequesDelight implements ModInitializer {
    public static final String MODID = "barbequesdelight";
    public static final Logger LOGGER = LoggerFactory.getLogger(BarbequesDelight.class);

    public static final ResourceKey<CreativeModeTab> ITEM_GROUP =
            ResourceKey.create(Registries.CREATIVE_MODE_TAB, asID("main"));

    @Override
    public void onInitialize() {
        Registry.register(BuiltInRegistries.CREATIVE_MODE_TAB, ITEM_GROUP,
                CreativeModeTab.builder(CreativeModeTab.Row.TOP, 0)
                        .title(Component.translatable("itemGroup.barbequesdelight.main"))
                        .icon(() -> new ItemStack(BBQDItems.GRILL))
                        .displayItems((params, output) -> BBQDItems.addToTab(output))
                        .build());

        BBQDItems.registerBBQDItems();
        BBQDBlocks.registerBBQDBlocks();
        BBQDRecipes.registerBBQDRecipes();
        BBQDEvents.registerBBQDEvents();
        BBQDEntityTypes.registerBBQDEntityTypes();
        CompostableRegistry.INSTANCE.add(BBQDItems.BURNT_FOOD, 0.2f);

        LOGGER.info("Barbeque's Delight is loaded");
    }

    public static Identifier asID(String id) {
        return Identifier.fromNamespaceAndPath(MODID, id);
    }
}
