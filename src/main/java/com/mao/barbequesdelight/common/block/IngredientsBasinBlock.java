package com.mao.barbequesdelight.common.block;

import com.mao.barbequesdelight.common.block.blockentity.IngredientsBasinBlockEntity;
import com.mojang.serialization.MapCodec;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.Containers;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.context.BlockPlaceContext;
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
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.EnumProperty;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.shapes.BooleanOp;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.Shapes;
import net.minecraft.world.phys.shapes.VoxelShape;
import net.minecraft.util.RandomSource;
import org.jetbrains.annotations.Nullable;

/**
 * The ingredients basin: a two-half tray that holds skewering ingredients. Holding a tool
 * (a stick) in the main hand and right-clicking a filled half produces a skewer.
 */
public class IngredientsBasinBlock extends BaseEntityBlock {
    public static final MapCodec<IngredientsBasinBlock> CODEC = simpleCodec(IngredientsBasinBlock::new);

    public static final EnumProperty<Direction> FACING = HorizontalDirectionalBlock.FACING;
    public static final VoxelShape OUTER;
    public static final VoxelShape SHAPE_X;
    public static final VoxelShape SHAPE_Z;

    public IngredientsBasinBlock(Properties properties) {
        super(properties);
        registerDefaultState(this.stateDefinition.any().setValue(FACING, Direction.NORTH));
    }

    @Override
    protected MapCodec<? extends BaseEntityBlock> codec() {
        return CODEC;
    }

    @Override
    protected InteractionResult useItemOn(ItemStack stack, BlockState state, Level level, BlockPos pos,
                                          Player player, InteractionHand hand, BlockHitResult hit) {
        return handleUse(level, pos, player, hand, hit);
    }

    @Override
    protected InteractionResult useWithoutItem(BlockState state, Level level, BlockPos pos,
                                               Player player, BlockHitResult hit) {
        return handleUse(level, pos, player, InteractionHand.MAIN_HAND, hit);
    }

    private InteractionResult handleUse(Level level, BlockPos pos, Player player, InteractionHand hand,
                                        BlockHitResult hit) {
        if (!(level.getBlockEntity(pos) instanceof IngredientsBasinBlockEntity basin)) {
            return InteractionResult.PASS;
        }

        int slot = basin.getSlotForHitting(hit, level);
        if (slot >= basin.getContainerSize()) {
            return InteractionResult.PASS;
        }

        ItemStack itemInBasin = basin.getItem(slot);
        ItemStack stack = player.getItemInHand(hand);

        if (itemInBasin.isEmpty() && !stack.isEmpty()) {
            if (!basin.skewer(player, slot, hand)) {
                // Not a valid skewering setup - just store the whole held stack.
                basin.setItem(slot, stack.split(stack.getCount()));
                basin.inventoryChanged();
                level.playSound(null, pos, SoundEvents.WOOD_HIT, SoundSource.BLOCKS, 1.0f, 1.0f);
                return InteractionResult.SUCCESS;
            }
            level.playSound(null, pos, SoundEvents.BUNDLE_INSERT, SoundSource.PLAYERS, 1.0f, 1.0f);
        } else if (basin.skewer(player, slot, hand)) {
            level.playSound(null, pos, SoundEvents.BUNDLE_INSERT, SoundSource.PLAYERS, 1.0f, 1.0f);
        } else {
            level.playSound(null, pos, SoundEvents.WOOD_HIT, SoundSource.BLOCKS, 1.0f, 1.0f);
            player.getInventory().placeItemBackInInventory(itemInBasin.split(itemInBasin.getCount()));
        }

        basin.inventoryChanged();
        return InteractionResult.SUCCESS;
    }

    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
        builder.add(FACING);
    }

    @Override
    public BlockState getStateForPlacement(BlockPlaceContext context) {
        return this.defaultBlockState().setValue(FACING, context.getHorizontalDirection().getOpposite());
    }

    @Override
    protected BlockState rotate(BlockState state, Rotation rotation) {
        return state.setValue(FACING, rotation.rotate(state.getValue(FACING)));
    }

    @Override
    protected BlockState mirror(BlockState state, Mirror mirror) {
        return state.rotate(mirror.getRotation(state.getValue(FACING)));
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
     * The 1.20.1 predicate {@code Block.hasTopRim || Block.sideCoversSmallSquare} no longer
     * exists; {@code canSupportCenter} is its modern equivalent (a sturdy centre face).
     */
    @Override
    protected boolean canSurvive(BlockState state, LevelReader level, BlockPos pos) {
        BlockPos floorPos = pos.below();
        return Block.canSupportCenter(level, floorPos, Direction.UP);
    }

    @Override
    protected VoxelShape getShape(BlockState state, BlockGetter level, BlockPos pos, CollisionContext context) {
        return state.getValue(FACING).getAxis() == Direction.Axis.X ? SHAPE_X : SHAPE_Z;
    }

    @Override
    protected void affectNeighborsAfterRemoval(BlockState state, net.minecraft.server.level.ServerLevel level,
                                              BlockPos pos, boolean movedByPiston) {
        if (level.getBlockEntity(pos) instanceof IngredientsBasinBlockEntity basin) {
            Containers.dropContents(level, pos, basin);
            level.updateNeighbourForOutputSignal(pos, this);
        }
    }

    @Nullable
    @Override
    public BlockEntity newBlockEntity(BlockPos pos, BlockState state) {
        return new IngredientsBasinBlockEntity(pos, state);
    }

    @Override
    protected RenderShape getRenderShape(BlockState state) {
        return RenderShape.MODEL;
    }

    static {
        OUTER = Block.box(1, 0, 1, 15, 4, 15);
        SHAPE_Z = Shapes.join(OUTER,
                Shapes.or(Block.box(2, 1, 2, 7.5, 4, 14),
                        Block.box(8.5, 1, 2, 14, 4, 14)),
                BooleanOp.ONLY_FIRST);
        SHAPE_X = Shapes.join(OUTER,
                Shapes.or(Block.box(2, 1, 2, 14, 4, 7.5),
                        Block.box(2, 1, 8.5, 14, 4, 14)),
                BooleanOp.ONLY_FIRST);
    }
}
