package com.mao.barbequesdelight.common.util;

import net.fabricmc.fabric.api.recipe.v1.sync.SynchronizedRecipes;
import net.minecraft.world.item.crafting.Recipe;
import net.minecraft.world.item.crafting.RecipeHolder;
import net.minecraft.world.item.crafting.RecipeInput;
import net.minecraft.world.item.crafting.RecipeManager;
import net.minecraft.world.item.crafting.RecipeType;
import net.minecraft.world.level.Level;
import org.jetbrains.annotations.Nullable;

import java.util.Optional;

/**
 * Side-safe recipe lookup that works on <b>both</b> sides.
 *
 * <p>26.1 moved server recipe lookup onto {@code ServerLevel.recipeAccess()}, which returns a
 * {@link RecipeManager}; a {@code ClientLevel} has no reachable {@code RecipeManager}
 * ({@code ClientRecipeContainer} only implements the narrower {@code RecipeAccess} interface, and
 * {@code ClientLevel.getConnection()} is private). The 1.20.1 original could match recipes on
 * either side because {@code World.getRecipeManager()} existed on both, so a direct port that
 * cast {@code (ServerLevel) level} threw {@link ClassCastException} and hard-crashed the game the
 * moment a player right-clicked a grill or basin.</p>
 *
 * <p>Block interactions ({@code useItemOn} / {@code useWithoutItem}) run on both sides - the
 * client runs them first to predict the result - so merely skipping client-side matching is not
 * enough: the client would then fail to predict placing an item on the grill or skewering in the
 * basin. Instead the client stores the recipes Fabric API synchronises to it, which exposes the
 * same {@code getFirstMatch(type, input, level)} shape as the server lookup, so both sides reach
 * the same answer and the prediction matches the authoritative result.</p>
 */
public final class BBQDRecipesHelper {

    private static final org.slf4j.Logger LOGGER =
            org.slf4j.LoggerFactory.getLogger("BarbequesDelight");

    /**
     * Recipes synchronised to the client, installed from the Fabric API recipe-sync event.
     * Stays {@code null} on a dedicated server, where {@link #manager(Level)} is used instead.
     */
    @Nullable
    private static volatile SynchronizedRecipes clientRecipes;

    private BBQDRecipesHelper() {
    }

    /** The server-side recipe manager, or {@code null} when {@code level} is a client level. */
    @Nullable
    public static RecipeManager manager(@Nullable Level level) {
        return level instanceof net.minecraft.server.level.ServerLevel server ? server.recipeAccess() : null;
    }

    /** Installed by the client initialiser; never called on a dedicated server. */
    public static void setClientRecipes(@Nullable SynchronizedRecipes recipes) {
        clientRecipes = recipes;
    }

    /**
     * Looks up the first matching recipe on whichever side this is running: the server's
     * {@link RecipeManager} when available, otherwise the client's synchronised recipes.
     *
     * <p>The client branch is defensive on purpose. It is pure prediction - the server always
     * re-runs the interaction authoritatively - so a failure there must never turn a click into a
     * crash. Any throwable degrades to "no match", which is exactly the pre-existing behaviour.</p>
     */
    public static <I extends RecipeInput, T extends Recipe<I>> Optional<RecipeHolder<T>> find(
            @Nullable Level level, RecipeType<T> type, I input) {
        if (level == null) {
            return Optional.empty();
        }
        RecipeManager server = manager(level);
        if (server != null) {
            return server.getRecipeFor(type, input, level);
        }
        SynchronizedRecipes client = clientRecipes;
        if (client == null) {
            return Optional.empty();
        }
        try {
            return client.getFirstMatch(type, input, level);
        } catch (Throwable t) {
            // Prediction only; never let this break the interaction.
            LOGGER.debug("client-side recipe lookup failed, skipping prediction", t);
            return Optional.empty();
        }
    }
}
