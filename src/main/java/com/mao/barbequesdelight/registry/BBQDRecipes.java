package com.mao.barbequesdelight.registry;

import com.mao.barbequesdelight.BarbequesDelight;
import com.mao.barbequesdelight.common.recipe.GrillingRecipe;
import com.mao.barbequesdelight.common.recipe.SkeweringRecipe;
import net.minecraft.core.Registry;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.item.crafting.RecipeBookCategory;
import net.minecraft.world.item.crafting.RecipeSerializer;
import net.minecraft.world.item.crafting.RecipeType;

/**
 * Recipe types and serializers.
 *
 * <p>26.1 collapsed {@code RecipeSerializer} into a record of a {@code MapCodec} plus a
 * {@code StreamCodec}, so there are no hand-written {@code read}/{@code write} classes any
 * more. The registry ids are unchanged, so recipe JSON {@code "type"} values still match.</p>
 */
public class BBQDRecipes {

    public static final RecipeType<GrillingRecipe> GRILLING_TYPE =
            registerType("grilling");
    public static final RecipeType<SkeweringRecipe> SKEWERING_TYPE =
            registerType("skewering");

    public static final RecipeSerializer<GrillingRecipe> GRILLING_SERIALIZER =
            Registry.register(BuiltInRegistries.RECIPE_SERIALIZER,
                    BarbequesDelight.asID("grilling"),
                    new RecipeSerializer<>(GrillingRecipe.CODEC, GrillingRecipe.STREAM_CODEC));

    public static final RecipeSerializer<SkeweringRecipe> SKEWERING_SERIALIZER =
            Registry.register(BuiltInRegistries.RECIPE_SERIALIZER,
                    BarbequesDelight.asID("skewering"),
                    new RecipeSerializer<>(SkeweringRecipe.CODEC, SkeweringRecipe.STREAM_CODEC));

    // These recipes are never shown in the recipe book, but the interface still needs a
    // category. The campfire category is the closest vanilla fit.
    public static final RecipeBookCategory GRILLING_BOOK_CATEGORY = registerBookCategory("grilling");
    public static final RecipeBookCategory SKEWERING_BOOK_CATEGORY = registerBookCategory("skewering");

    private static <T extends net.minecraft.world.item.crafting.Recipe<?>> RecipeType<T> registerType(String id) {
        ResourceKey<RecipeType<?>> key = ResourceKey.create(Registries.RECIPE_TYPE, BarbequesDelight.asID(id));
        return Registry.register(BuiltInRegistries.RECIPE_TYPE, key, new RecipeType<T>() {
            @Override
            public String toString() {
                return BarbequesDelight.MODID + ":" + id;
            }
        });
    }

    private static RecipeBookCategory registerBookCategory(String id) {
        ResourceKey<RecipeBookCategory> key =
                ResourceKey.create(Registries.RECIPE_BOOK_CATEGORY, BarbequesDelight.asID(id));
        return Registry.register(BuiltInRegistries.RECIPE_BOOK_CATEGORY, key, new RecipeBookCategory());
    }

    public static void registerBBQDRecipes() {
        // Custom recipe serializers are NOT synchronised to the client by default. Fabric
        // negotiates which serializers each side understands and only sends recipes whose
        // serializer was declared here. Without this the client ends up with zero
        // barbequesdelight:grilling recipes while the server has all of them, so every
        // client-side recipe lookup misses. Vanilla turns a missed block-interaction prediction
        // into "send ServerboundUseItemPacket instead of ServerboundUseItemOnPacket", so the
        // server never even sees the click: items cannot be placed and nothing can be flipped.
        net.fabricmc.fabric.api.recipe.v1.sync.RecipeSynchronization
                .synchronizeRecipeSerializer(GRILLING_SERIALIZER);
        net.fabricmc.fabric.api.recipe.v1.sync.RecipeSynchronization
                .synchronizeRecipeSerializer(SKEWERING_SERIALIZER);

        BarbequesDelight.LOGGER.debug("Register BBQD Recipes For" + BarbequesDelight.MODID);
    }
}
