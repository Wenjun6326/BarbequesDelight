package com.mao.barbequesdelight.common.block.blockentity;

import com.mao.barbequesdelight.common.recipe.SkeweringRecipe;
import com.mao.barbequesdelight.common.recipe.SkeweringRecipeInput;
import com.mao.barbequesdelight.common.util.BBQDRecipesHelper;
import com.mao.barbequesdelight.registry.BBQDEntityTypes;
import com.mao.barbequesdelight.registry.BBQDRecipes;
import net.minecraft.core.BlockPos;
import net.minecraft.core.NonNullList;
import net.minecraft.world.ContainerHelper;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.RecipeHolder;
import net.minecraft.world.item.crafting.RecipeManager;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueOutput;
import net.minecraft.world.phys.Vec3;
import org.joml.Vector2f;
import vectorwing.farmersdelight.common.block.entity.SyncedBlockEntity;

/**
 * The ingredients basin: two halves, each holding an ingredient stack. Right-clicking a
 * half while holding a tool (a stick) in the main hand and an optional garnish in the off
 * hand consumes one tool and {@code count} of each ingredient to produce a skewer.
 */
public class IngredientsBasinBlockEntity extends SyncedBlockEntity implements BlockEntityInv {
    public final NonNullList<ItemStack> items;

    public IngredientsBasinBlockEntity(BlockPos pos, BlockState state) {
        this(BBQDEntityTypes.INGREDIENTS_BASIN, pos, state);
    }

    public IngredientsBasinBlockEntity(BlockEntityType<?> type, BlockPos pos, BlockState state) {
        super(type, pos, state);
        this.items = NonNullList.withSize(2, ItemStack.EMPTY);
    }

    /**
     * Attempts to craft a skewer from the half at {@code slot}.
     *
     * <p>Consumption lives here rather than in the recipe, because 26.1's
     * {@code Recipe#assemble} is defined as a pure result builder.</p>
     *
     * @return true when a matching recipe exists (and, on the server, was performed)
     */
    public boolean skewer(Player user, int slot, InteractionHand hand) {
        Level level = this.getLevel();
        if (level == null) {
            return false;
        }

        ItemStack tool = user.getItemInHand(hand);
        ItemStack basin = getItem(slot);
        ItemStack garnish = user.getOffhandItem();

        SkeweringRecipeInput input = new SkeweringRecipeInput(basin, garnish, tool);

        // Resolved on both sides: the server uses its RecipeManager, the client uses the recipes
        // Fabric API synchronised to it. Matching on the client is what makes the interaction
        // predict correctly instead of always returning PASS.
        var optional = BBQDRecipesHelper.find(level, BBQDRecipes.SKEWERING_TYPE, input);
        if (optional.isEmpty()) {
            return false;
        }

        SkeweringRecipe recipe = optional.get().value();
        ItemStack result = recipe.assemble(input);

        // Consume one tool and `count` of each non-empty ingredient slot, mirroring the
        // original in-recipe consumption.
        tool.shrink(1);
        int count = recipe.getIngredientCount();
        if (!basin.isEmpty() && recipe.getIngredientList().stream().anyMatch(i -> i.test(basin))) {
            basin.shrink(count);
        }
        if (!garnish.isEmpty() && recipe.getIngredientList().stream().anyMatch(i -> i.test(garnish))) {
            garnish.shrink(count);
        }

        ItemStack held = user.getItemInHand(hand);
        if (held.isEmpty()) {
            user.setItemInHand(hand, result);
        } else {
            user.getInventory().placeItemBackInInventory(result);
        }

        // If an ingredient stack was emptied, clear the slot so the basin renders correctly.
        if (!basin.isEmpty() && basin.getCount() <= 0) {
            setItem(slot, ItemStack.EMPTY);
        }
        inventoryChanged();
        return true;
    }

    public Vector2f getBasinItemOffset(int index) {
        final float xOffset = .2f;
        final float yOffset = .0f;
        final Vector2f[] offsets = {new Vector2f(xOffset, yOffset), new Vector2f(-xOffset, yOffset)};
        return offsets[index];
    }

    @Override
    public NonNullList<ItemStack> getItems() {
        return items;
    }

    @Override
    public int getMaxStackSize() {
        return 64;
    }

    public void inventoryChanged() {
        setChanged();
        Level level = this.getLevel();
        if (level != null) {
            BlockState state = this.getBlockState();
            level.sendBlockUpdated(this.getBlockPos(), state, state, net.minecraft.world.level.block.Block.UPDATE_ALL);
        }
    }

    @Override
    protected void saveAdditional(ValueOutput output) {
        super.saveAdditional(output);
        ContainerHelper.saveAllItems(output, this.items);
    }

    @Override
    protected void loadAdditional(ValueInput input) {
        super.loadAdditional(input);
        this.items.clear();
        ContainerHelper.loadAllItems(input, this.items);
    }
}
