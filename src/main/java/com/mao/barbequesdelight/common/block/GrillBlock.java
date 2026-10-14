package com.mao.barbequesdelight.common.block;

import com.mao.barbequesdelight.common.block.blockentity.GrillBlockEntity;
import com.mao.barbequesdelight.common.item.SeasoningItem;
import com.mao.barbequesdelight.common.recipe.GrillingRecipe;
import com.mao.barbequesdelight.registry.BBQDEntityTypes;
import com.mojang.serialization.MapCodec;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.network.chat.Component;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.util.RandomSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.Containers;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.item.crafting.CampfireCookingRecipe;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.ScheduledTickAccess;
import net.minecraft.world.level.block.BaseEntityBlock;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.HorizontalDirectionalBlock;
import net.minecraft.world.level.block.Mirror;
import net.minecraft.world.level.block.RenderShape;
import net.minecraft.world.level.block.Rotation;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityTicker;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.block.state.properties.BooleanProperty;
import net.minecraft.world.level.block.state.properties.EnumProperty;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.Shapes;
import net.minecraft.world.phys.shapes.VoxelShape;
import org.jetbrains.annotations.Nullable;
import vectorwing.farmersdelight.common.registry.ModBlocks;
import vectorwing.farmersdelight.common.registry.ModSounds;

import java.util.Optional;

/**
 * The grill block. Two halves on the top face, each cooking one item while heated.
 * Sneak-clicking a partially cooked item flips it, which is required for it to cook
 * successfully instead of burning.
 *
 * <p>A grill can also hold its own campfire, mirroring the Forge original: placing it on a lit
 * campfire (or right-clicking a campfire onto its side) sets {@link #HAS_CAMPFIRE}, which lights
 * the block, swaps in the campfire model, heats the grill on its own, and returns the campfire
 * when the grill is broken.</p>
 */
public class GrillBlock extends BaseEntityBlock {
    public static final MapCodec<GrillBlock> CODEC = simpleCodec(GrillBlock::new);

    public static final EnumProperty<Direction> FACING = HorizontalDirectionalBlock.FACING;
    /** True when a campfire is mounted in this block, matching the Forge original. */
    public static final BooleanProperty HAS_CAMPFIRE = BooleanProperty.create("has_campfire");
    private static final VoxelShape SHAPE;

    public GrillBlock(Properties properties) {
        super(properties);
        registerDefaultState(this.stateDefinition.any()
                .setValue(FACING, Direction.NORTH)
                .setValue(HAS_CAMPFIRE, false));
    }

    @Override
    protected MapCodec<? extends BaseEntityBlock> codec() {
        return CODEC;
    }

    @Override
    protected VoxelShape getShape(BlockState state, BlockGetter level, BlockPos pos, CollisionContext context) {
        return SHAPE;
    }

    @Override
    public void animateTick(BlockState state, Level level, BlockPos pos, RandomSource random) {
        if (level.getBlockEntity(pos) instanceof GrillBlockEntity grill && grill.isBarbecuing()) {
            double x = (double) pos.getX() + 0.5;
            double y = pos.getY();
            double z = (double) pos.getZ() + 0.5;
            if (random.nextInt(8) == 0) {
                level.playLocalSound(x, y, z, ModSounds.BLOCK_SKILLET_SIZZLE.get(),
                        SoundSource.BLOCKS, 0.4F, random.nextFloat() * 0.2F + 0.9F, false);
            }
        }
    }

    @Override
    protected BlockState updateShape(BlockState state, LevelReader level, ScheduledTickAccess ticks, BlockPos pos,
                                     Direction direction, BlockPos neighborPos, BlockState neighborState,
                                     RandomSource random) {
        if (direction == Direction.DOWN && !state.canSurvive(level, pos)) {
            return Blocks.AIR.defaultBlockState();
        }
        return super.updateShape(state, level, ticks, pos, direction, neighborPos, neighborState, random);
    }

    /**
     * A mounted campfire is returned when the grill is broken, matching the Forge original's
     * loot table (which adds a campfire when {@code has_campfire} is set).
     */
    @Override
    public BlockState playerWillDestroy(Level level, BlockPos pos, BlockState state, Player player) {
        if (state.getValue(HAS_CAMPFIRE) && !level.isClientSide()) {
            popResource(level, pos, new ItemStack(Items.CAMPFIRE));
        }
        return super.playerWillDestroy(level, pos, state, player);
    }

    @Override
    protected void affectNeighborsAfterRemoval(BlockState state, net.minecraft.server.level.ServerLevel level,
                                              BlockPos pos, boolean movedByPiston) {
        BlockEntity blockEntity = level.getBlockEntity(pos);
        if (blockEntity instanceof GrillBlockEntity grill) {
            Containers.dropContents(level, pos, grill);
            level.updateNeighbourForOutputSignal(pos, this);
        }
    }

    @Override
    protected boolean propagatesSkylightDown(BlockState state) {
        return true;
    }

    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
        builder.add(FACING, HAS_CAMPFIRE);
    }

    /**
     * Placing a grill on a lit campfire mounts the campfire inside the grill, which is how the
     * Forge original works. The placement position is the campfire's own position.
     */
    @Override
    public BlockState getStateForPlacement(BlockPlaceContext context) {
        Level level = context.getLevel();
        BlockPos pos = context.getClickedPos();
        BlockState below = level.getBlockState(pos);
        boolean campfire = below.is(Blocks.CAMPFIRE)
                && below.getValue(BlockStateProperties.LIT)
                && !below.getValue(BlockStateProperties.WATERLOGGED);

        return this.defaultBlockState()
                .setValue(FACING, context.getHorizontalDirection().getOpposite())
                .setValue(HAS_CAMPFIRE, campfire);
    }

    /**
     * A grill may stand on a sturdy surface, <em>or</em> on a lit campfire it is mounting. The
     * vanilla check is still required for the normal case, because the grill is otherwise a
     * regular placeable block.
     */
    @Override
    protected boolean canSurvive(BlockState state, LevelReader level, BlockPos pos) {
        BlockState below = level.getBlockState(pos.below());
        if (below.is(Blocks.CAMPFIRE) && below.getValue(BlockStateProperties.LIT)) {
            return true;
        }
        return Block.canSupportCenter(level, pos.below(), Direction.UP);
    }

    @Override
    protected BlockState rotate(BlockState state, Rotation rotation) {
        return state.setValue(FACING, rotation.rotate(state.getValue(FACING)));
    }

    @Override
    protected BlockState mirror(BlockState state, Mirror mirror) {
        return state.rotate(mirror.getRotation(state.getValue(FACING)));
    }

    // ---- Interaction -------------------------------------------------------

    /**
     * Handles the sneaking case (flip). The non-sneaking case is split in 26.1 between
     * {@link #useItemOn} (holding an item) and {@link #useWithoutItem} (empty hand).
     */
    private InteractionResult handleUse(Level level, BlockPos pos, net.minecraft.world.entity.player.Player player,
                                        InteractionHand hand, BlockHitResult hit, boolean sneaking) {
        // Mounting a campfire onto a bare grill, as in the Forge original: right-click a grill
        // that has no campfire yet, holding a campfire, aiming at any side face.
        ItemStack heldInHand = player.getItemInHand(hand);
        BlockState blockState = level.getBlockState(pos);
        if (!blockState.getValue(HAS_CAMPFIRE)
                && heldInHand.is(Items.CAMPFIRE)
                && hit.getDirection().getAxis() != Direction.Axis.Y) {
            if (!level.isClientSide()) {
                level.setBlockAndUpdate(pos, blockState.setValue(HAS_CAMPFIRE, true));
                if (!player.getAbilities().instabuild) {
                    heldInHand.shrink(1);
                }
            }
            return InteractionResult.SUCCESS;
        }

        if (!(level.getBlockEntity(pos) instanceof GrillBlockEntity grill)) {
            return InteractionResult.PASS;
        }

        int i = grill.getSlotForHitting(hit, level);
        if (i >= grill.getContainerSize()) {
            return InteractionResult.PASS;
        }

        ItemStack stack = player.getItemInHand(hand);
        ItemStack grillItems = grill.getItem(i);

        if (!sneaking) {
            // Seasoning an item that is already cooking, as in the Forge original.
            if (!grillItems.isEmpty()
                    && stack.getItem() instanceof SeasoningItem seasoning
                    && seasoning.canSprinkle(grillItems)) {
                seasoning.sprinkle(grillItems, hit.getLocation(), player, stack);
                grill.inventoryChanged();
                return InteractionResult.SUCCESS;
            }

            Optional<GrillingRecipe> optional = grill.findMatchingRecipe(stack);
            Optional<CampfireCookingRecipe> campfireOptional = grill.findMatchingCampfireRecipe(stack);

            if (!stack.isEmpty() && grillItems.isEmpty() && (optional.isPresent() || campfireOptional.isPresent())) {
                grill.setItem(i, stack.split(1));

                optional.ifPresent(recipe -> grill.setBarbecuing(i, recipe.getGrillingTime()));
                campfireOptional.ifPresent(recipe -> grill.setBarbecuing(i, recipe.cookingTime() - 20 * 10));

                level.playSound(null, pos, SoundEvents.LANTERN_PLACE, SoundSource.BLOCKS, 0.7F, 1.0F);
                return InteractionResult.SUCCESS;
            } else if (!grillItems.isEmpty()) {
                player.getInventory().placeItemBackInInventory(grillItems.split(1));
                grill.inventoryChanged();
                return InteractionResult.SUCCESS;
            }
        } else if (grill.flip(i)) {
            level.playSound(null, pos.getX() + 0.5, pos.getY() + 0.5, pos.getZ() + 0.5,
                    ModSounds.BLOCK_SKILLET_ADD_FOOD.get(), SoundSource.BLOCKS, 0.8F, 1.0F);
            return InteractionResult.SUCCESS;
        }

        return InteractionResult.PASS;
    }

    @Override
    protected InteractionResult useItemOn(ItemStack stack, BlockState state, Level level, BlockPos pos,
                                          net.minecraft.world.entity.player.Player player, InteractionHand hand,
                                          BlockHitResult hit) {
        return handleUse(level, pos, player, hand, hit, player.isSecondaryUseActive());
    }

    @Override
    protected InteractionResult useWithoutItem(BlockState state, Level level, BlockPos pos,
                                               net.minecraft.world.entity.player.Player player, BlockHitResult hit) {
        return handleUse(level, pos, player, InteractionHand.MAIN_HAND, hit, player.isSecondaryUseActive());
    }

    // ---- Block entity ------------------------------------------------------

    @Nullable
    @Override
    public BlockEntity newBlockEntity(BlockPos pos, BlockState state) {
        return new GrillBlockEntity(pos, state);
    }

    @Nullable
    @Override
    public <T extends BlockEntity> BlockEntityTicker<T> getTicker(Level level, BlockState state,
                                                                 BlockEntityType<T> type) {
        return createTickerHelper(type, BBQDEntityTypes.GRILL,
                level.isClientSide()
                        ? (BlockEntityTicker<GrillBlockEntity>) GrillBlockEntity::animationTick
                        : (BlockEntityTicker<GrillBlockEntity>) GrillBlockEntity::tick);
    }

    @Override
    protected RenderShape getRenderShape(BlockState state) {
        return RenderShape.MODEL;
    }

    static {
        SHAPE = Shapes.or(
                Block.box(0.0D, 0.0D, 0.0D, 1.0D, 10.0D, 1.0D),
                Block.box(0.0D, 0.0D, 15.0D, 1.0D, 10.0D, 16.0D),
                Block.box(15.0D, 0.0D, 15.0D, 16.0D, 10.0D, 16.0D),
                Block.box(15.0D, 0.0D, 0.0D, 16.0D, 10.0D, 1.0D),
                Block.box(0.0D, 10.0D, 0.0D, 16.0D, 16.0D, 16.0D)
        );
    }
}
