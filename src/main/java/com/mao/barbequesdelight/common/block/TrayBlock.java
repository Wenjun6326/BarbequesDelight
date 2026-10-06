package com.mao.barbequesdelight.common.block;

import com.mao.barbequesdelight.common.block.blockentity.TrayBlockEntity;
import com.mao.barbequesdelight.registry.BBQDBlocks;
import com.mojang.serialization.MapCodec;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.util.RandomSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.Containers;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelAccessor;
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
import net.minecraft.world.level.block.state.properties.BooleanProperty;
import net.minecraft.world.level.block.state.properties.EnumProperty;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.shapes.BooleanOp;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.Shapes;
import net.minecraft.world.phys.shapes.VoxelShape;
import org.jetbrains.annotations.Nullable;

/**
 * The tray: three display slots that can be stacked up to three high. The middle tray of a
 * three-high stack reports {@code support=true} and renders with legs.
 */
public class TrayBlock extends BaseEntityBlock {
    public static final MapCodec<TrayBlock> CODEC = simpleCodec(TrayBlock::new);

    public static final VoxelShape SHAPE;
    public static final EnumProperty<Direction> FACING = HorizontalDirectionalBlock.FACING;
    public static final BooleanProperty SUPPORT = BooleanProperty.create("support");

    public TrayBlock(Properties properties) {
        super(properties);
        registerDefaultState(this.stateDefinition.any()
                .setValue(FACING, Direction.NORTH)
                .setValue(SUPPORT, false));
    }

    @Override
    protected MapCodec<? extends BaseEntityBlock> codec() {
        return CODEC;
    }

    @Override
    protected InteractionResult useItemOn(ItemStack stack, BlockState state, Level level, BlockPos pos,
                                          Player player, InteractionHand hand, BlockHitResult hit) {
        return handleUse(level, pos, player, stack);
    }

    @Override
    protected InteractionResult useWithoutItem(BlockState state, Level level, BlockPos pos,
                                               Player player, BlockHitResult hit) {
        return handleUse(level, pos, player, ItemStack.EMPTY);
    }

    private InteractionResult handleUse(Level level, BlockPos pos, Player player, ItemStack stack) {
        if (!(level.getBlockEntity(pos) instanceof TrayBlockEntity tray)) {
            return InteractionResult.PASS;
        }

        if (!stack.isEmpty()) {
            for (int i = 0; i < tray.getContainerSize(); ++i) {
                ItemStack itemStack = tray.getItem(i);
                if (itemStack.isEmpty()) {
                    tray.setItem(i, stack.split(stack.getCount()));
                    return InteractionResult.SUCCESS;
                }
                if (ItemStack.isSameItemSameComponents(stack, itemStack)
                        && itemStack.getCount() < itemStack.getMaxStackSize()) {
                    itemStack.setCount(itemStack.getCount() + stack.split(1).getCount());
                    return InteractionResult.SUCCESS;
                }
            }
            tray.inventoryChanged();
        } else if (tray.removeItems(player)) {
            return InteractionResult.SUCCESS;
        }

        return InteractionResult.PASS;
    }

    @Nullable
    @Override
    public BlockEntity newBlockEntity(BlockPos pos, BlockState state) {
        return new TrayBlockEntity(pos, state);
    }

    @Override
    protected RenderShape getRenderShape(BlockState state) {
        return RenderShape.MODEL;
    }

    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
        builder.add(FACING, SUPPORT);
    }

    @Override
    public BlockState getStateForPlacement(BlockPlaceContext context) {
        return this.defaultBlockState()
                .setValue(FACING, context.getHorizontalDirection().getOpposite())
                .setValue(SUPPORT, getTrayState(context.getLevel(), context.getClickedPos()));
    }

    @Override
    protected boolean canSurvive(BlockState state, LevelReader level, BlockPos pos) {
        BlockPos floorPos = pos.below();
        return Block.canSupportCenter(level, floorPos, Direction.UP)
                || this.getTrayState(level, pos);
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
        if (direction.getAxis().equals(Direction.Axis.Y)) {
            return state.setValue(SUPPORT, getTrayState(level, pos));
        }
        if (direction == Direction.DOWN && !state.canSurvive(level, pos)) {
            return Blocks.AIR.defaultBlockState();
        }
        return super.updateShape(state, level, ticks, pos, direction, neighborPos, neighborState, random);
    }

    @Override
    protected VoxelShape getShape(BlockState state, BlockGetter level, BlockPos pos, CollisionContext context) {
        return SHAPE;
    }

    @Override
    protected void affectNeighborsAfterRemoval(BlockState state, net.minecraft.server.level.ServerLevel level,
                                              BlockPos pos, boolean movedByPiston) {
        if (level.getBlockEntity(pos) instanceof TrayBlockEntity tray) {
            Containers.dropContents(level, pos, tray);
            level.updateNeighbourForOutputSignal(pos, this);
        }
    }

    /** True only for the middle tray of a three-high stack. */
    private boolean getTrayState(LevelReader world, BlockPos pos) {
        BlockPos twoBelow = pos.offset(0, -2, 0);
        return world.getBlockState(pos.below()).getBlock() == BBQDBlocks.TRAY
                && world.getBlockState(twoBelow).getBlock() != BBQDBlocks.TRAY;
    }

    static {
        SHAPE = Shapes.join(Block.box(0, 0, 0, 16, 3, 16),
                Shapes.or(Block.box(1, 1, 1, 15, 3, 15)),
                BooleanOp.ONLY_FIRST);
    }
}
