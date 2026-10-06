package com.mao.barbequesdelight.common.block.blockentity;

import com.mao.barbequesdelight.common.recipe.GrillingRecipe;
import com.mao.barbequesdelight.common.util.BBQDRecipesHelper;
import com.mao.barbequesdelight.registry.BBQDEntityTypes;
import com.mao.barbequesdelight.registry.BBQDItems;
import com.mao.barbequesdelight.registry.BBQDRecipes;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.NonNullList;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.ContainerHelper;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.RecipeHolder;
import net.minecraft.world.item.crafting.RecipeManager;
import net.minecraft.world.item.crafting.RecipeType;
import net.minecraft.world.item.crafting.SingleRecipeInput;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.HorizontalDirectionalBlock;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.util.Mth;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueOutput;
import org.jetbrains.annotations.Nullable;
import org.joml.Vector2f;
import vectorwing.farmersdelight.common.block.entity.HeatableBlockEntity;
import vectorwing.farmersdelight.common.block.entity.SyncedBlockEntity;
import vectorwing.farmersdelight.common.registry.ModParticleTypes;

import java.util.List;
import java.util.Optional;

/**
 * The grill: two independent halves, each holding one item that cooks while the block is
 * heated. An item that reaches its full cooking time without being flipped becomes
 * {@code burnt_food}; flipping rewinds progress to the halfway point.
 */
public class GrillBlockEntity extends SyncedBlockEntity implements BlockEntityInv, HeatableBlockEntity {
    protected final NonNullList<ItemStack> items = NonNullList.withSize(2, ItemStack.EMPTY);
    public final int[] grillingTimes;
    protected final int[] grillingTimesTotal;
    public final boolean[] flipped;

    // NOTE: the tag names are intentionally "swapped" relative to their meaning. The 1.20.1
    // release wrote progress under "CookingTotalTimes" and targets under "CookingTimes";
    // keeping the literals preserves save compatibility with existing worlds.
    private static final String TAG_KEY_COOKING_TOTAL_TIMES = "CookingTimes";
    private static final String TAG_KEY_COOKING_TIMES = "CookingTotalTimes";
    private static final String TAG_FLIPPED = "Flipped";

    public GrillBlockEntity(BlockPos pos, BlockState state) {
        this(BBQDEntityTypes.GRILL, pos, state);
    }

    public GrillBlockEntity(BlockEntityType<?> type, BlockPos pos, BlockState state) {
        super(type, pos, state);
        this.grillingTimes = new int[2];
        this.grillingTimesTotal = new int[2];
        this.flipped = new boolean[2];
    }

    public void setBarbecuing(int i, int time) {
        this.grillingTimes[i] = 0;
        this.grillingTimesTotal[i] = time;
        this.setFlipped(i, false);
        inventoryChanged();
    }

    protected void barbecuing() {
        for (int i = 0; i < items.size(); ++i) {
            ItemStack stack = items.get(i);
            if (stack.isEmpty()) {
                continue;
            }

            ++grillingTimes[i];
            boolean flag = false;

            if (grillingTimes[i] == grillingTimesTotal[i]) {
                Level level = this.getLevel();
                if (level != null) {
                    SingleRecipeInput input = new SingleRecipeInput(stack);

                    // A grilling recipe wins; otherwise fall back to a vanilla campfire recipe.
                    ItemStack campfire = ((net.minecraft.server.level.ServerLevel) level).recipeAccess()
                            .getRecipeFor(RecipeType.CAMPFIRE_COOKING, input, level)
                            .map(holder -> holder.value().assemble(input))
                            .orElse(stack);

                    ItemStack result = ((net.minecraft.server.level.ServerLevel) level).recipeAccess()
                            .getRecipeFor(BBQDRecipes.GRILLING_TYPE, input, level)
                            .map(holder -> holder.value().assemble(input))
                            .orElse(campfire);

                    this.setItem(i, getFlipped(i) ? result : BBQDItems.BURNT_FOOD.getDefaultInstance());
                    flag = true;
                }
            } else if (grillingTimes[i] == grillingTimesTotal[i] * 2) {
                this.setItem(i, BBQDItems.BURNT_FOOD.getDefaultInstance());
                flag = true;
            }

            if (flag) {
                inventoryChanged();
            }
        }
    }

    private void fadeBarbecuing() {
        boolean flag = false;
        for (int i = 0; i < items.size(); ++i) {
            if (grillingTimes[i] > 0) {
                flag = true;
                grillingTimes[i] = Mth.clamp(grillingTimes[i] - 2, 0, grillingTimesTotal[i]);
            }
        }
        if (flag) {
            setChanged();
        }
    }

    public boolean flip(int i) {
        if (canFlip(i)) {
            setFlipped(i, true);
            this.grillingTimes[i] = (this.grillingTimesTotal[i] / 2);
            return true;
        }
        return false;
    }

    public void setFlipped(int i, boolean value) {
        this.flipped[i] = value;
        inventoryChanged();
    }

    public boolean getFlipped(int i) {
        return flipped[i];
    }

    public boolean canFlip(int i) {
        Level level = this.getLevel();
        return isBarbecuing()
                && grillingTimes[i] >= (grillingTimesTotal[i] / 2)
                && !getFlipped(i)
                && level != null && !level.isClientSide();
    }

    public static void tick(Level world, BlockPos blockPos, BlockState blockState, GrillBlockEntity grill) {
        if (grill.isHeated()) {
            grill.barbecuing();
        } else {
            grill.fadeBarbecuing();
        }
    }

    public static void animationTick(Level world, BlockPos pos, BlockState blockState, GrillBlockEntity grill) {
        if (grill.isBarbecuing()) {
            grill.addParticles();
            var random = world.getRandom();
            if (random.nextFloat() < 0.2F) {
                double x = (double) pos.getX() + 0.5D + (random.nextDouble() * 0.4D - 0.2D);
                double y = (double) pos.getY() + 1.1D;
                double z = (double) pos.getZ() + 0.5D + (random.nextDouble() * 0.4D - 0.2D);
                double motionY = random.nextBoolean() ? 0.015D : 0.005D;
                world.addParticle(ModParticleTypes.STEAM.get(), x, y, z, 0.0D, motionY, 0.0D);
            }
        }
    }

    public boolean isHeated() {
        Level level = this.getLevel();
        return level != null && this.isHeated(level, this.getBlockPos());
    }

    public boolean isBarbecuing() {
        if (this.getLevel() == null || !isHeated()) {
            return false;
        }
        return !getItem(getItem(0).isEmpty() ? 1 : 0).isEmpty();
    }

    public Optional<GrillingRecipe> findMatchingRecipe(ItemStack itemStack) {
        Level level = this.getLevel();
        if (level == null || this.items.stream().noneMatch(ItemStack::isEmpty)) {
            return Optional.empty();
        }
        return BBQDRecipesHelper
                .find(level, BBQDRecipes.GRILLING_TYPE, new SingleRecipeInput(itemStack))
                .map(RecipeHolder::value);
    }

    public Optional<net.minecraft.world.item.crafting.CampfireCookingRecipe> findMatchingCampfireRecipe(ItemStack itemStack) {
        Level level = this.getLevel();
        if (level == null || this.items.stream().noneMatch(ItemStack::isEmpty)) {
            return Optional.empty();
        }
        return BBQDRecipesHelper
                .find(level, RecipeType.CAMPFIRE_COOKING, new SingleRecipeInput(itemStack))
                .map(RecipeHolder::value);
    }

    public Vector2f getGrillItemOffset(int index) {
        final float xOffset = .2f;
        final float yOffset = .0f;
        final Vector2f[] offsets = {new Vector2f(xOffset, yOffset), new Vector2f(-xOffset, yOffset)};
        return offsets[index];
    }

    public void inventoryChanged() {
        setChanged();
        Level level = this.getLevel();
        if (level != null) {
            BlockState state = this.getBlockState();
            level.sendBlockUpdated(this.getBlockPos(), state, state, net.minecraft.world.level.block.Block.UPDATE_ALL);
        }
    }

    private void addParticles() {
        Level level = this.getLevel();
        if (level == null) {
            return;
        }

        for (int i = 0; i < items.size(); ++i) {
            grillingTimes[i]++;
            if (items.get(i).isEmpty()) {
                continue;
            }

            Vector2f grillItemOffset = getGrillItemOffset(i);
            Direction direction = getBlockState().getValue(HorizontalDirectionalBlock.FACING);
            int directionIndex = direction.get2DDataValue();
            Vector2f offset = directionIndex % 2 == 0
                    ? grillItemOffset
                    : new Vector2f(grillItemOffset.y, grillItemOffset.x);

            double x = ((double) getBlockPos().getX() + 0.5D)
                    - (direction.getStepX() * offset.x)
                    + (direction.getClockWise().getStepX() * offset.x);
            double y = (double) getBlockPos().getY() + 1.0D;
            double z = ((double) getBlockPos().getZ() + 0.5D)
                    - (direction.getStepZ() * offset.y)
                    + (direction.getClockWise().getStepZ() * offset.y);

            if (level.getRandom().nextFloat() < 0.2f) {
                level.addParticle(ParticleTypes.SMOKE, x, y, z, 0.0D, 5.0E-4D, 0.0D);
            }
        }
    }

    @Override
    public NonNullList<ItemStack> getItems() {
        return items;
    }

    // ---- Persistence -------------------------------------------------------

    @Override
    protected void saveAdditional(ValueOutput output) {
        super.saveAdditional(output);
        ContainerHelper.saveAllItems(output, this.items, true);

        output.putIntArray(TAG_KEY_COOKING_TIMES, this.grillingTimes);
        output.putIntArray(TAG_KEY_COOKING_TOTAL_TIMES, this.grillingTimesTotal);
        for (int i = 0; i < this.flipped.length; i++) {
            output.putBoolean(TAG_FLIPPED + i, this.flipped[i]);
        }
    }

    @Override
    protected void loadAdditional(ValueInput input) {
        super.loadAdditional(input);
        this.items.clear();
        ContainerHelper.loadAllItems(input, this.items);

        input.getIntArray(TAG_KEY_COOKING_TIMES).ifPresent(read ->
                System.arraycopy(read, 0, grillingTimes, 0, Math.min(grillingTimes.length, read.length)));
        input.getIntArray(TAG_KEY_COOKING_TOTAL_TIMES).ifPresent(read ->
                System.arraycopy(read, 0, grillingTimesTotal, 0, Math.min(grillingTimesTotal.length, read.length)));

        for (int i = 0; i < this.flipped.length; i++) {
            this.flipped[i] = input.getBooleanOr(TAG_FLIPPED + i, false);
        }
    }
}
