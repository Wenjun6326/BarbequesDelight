package com.mao.barbequesdelight.common.block.blockentity;

import net.minecraft.core.Direction;
import net.minecraft.core.NonNullList;
import net.minecraft.world.ContainerHelper;
import net.minecraft.world.WorldlyContainer;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.HorizontalDirectionalBlock;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.Vec3;
import net.minecraft.core.BlockPos;
import org.jetbrains.annotations.Nullable;

/**
 * Shared inventory behaviour for the grill, basin and tray.
 *
 * <p>Deliberately exposes no automation-facing slots ({@link #getSlotsForFace} is empty),
 * matching the 1.20.1 original where {@code getAvailableSlots} returned an empty array, so
 * hoppers and pipes cannot insert or extract.</p>
 */
public interface BlockEntityInv extends WorldlyContainer {

    NonNullList<ItemStack> getItems();

    @Override
    default int getContainerSize() {
        return getItems().size();
    }

    @Override
    default boolean isEmpty() {
        for (int i = 0; i < getContainerSize(); i++) {
            if (!getItem(i).isEmpty()) {
                return false;
            }
        }
        return true;
    }

    @Override
    default ItemStack getItem(int slot) {
        return getItems().get(slot);
    }

    @Override
    default ItemStack removeItem(int slot, int count) {
        ItemStack result = ContainerHelper.removeItem(getItems(), slot, count);
        if (!result.isEmpty()) {
            setChanged();
        }
        return result;
    }

    @Override
    default void setItem(int slot, ItemStack stack) {
        ItemStack copy = stack.copy();
        stack.setCount(1);
        getItems().set(slot, copy);
        setChanged();
    }

    @Override
    default ItemStack removeItemNoUpdate(int slot) {
        return ContainerHelper.takeItem(getItems(), slot);
    }

    @Override
    default void clearContent() {
        for (int i = 0; i < getContainerSize(); i++) {
            removeItemNoUpdate(i);
        }
    }

    @Override
    default boolean stillValid(Player player) {
        return true;
    }

    @Override
    default int[] getSlotsForFace(Direction side) {
        return new int[0];
    }

    @Override
    default boolean canTakeItemThroughFace(int slot, ItemStack stack, Direction dir) {
        return true;
    }

    @Override
    default boolean canPlaceItemThroughFace(int slot, ItemStack stack, @Nullable Direction dir) {
        return true;
    }

    /**
     * Maps a hit on the top face to the left (0) or right (1) half of the block.
     * Returns the slot count as a sentinel when the hit is not on the top face.
     */
    default int getSlotForHitting(BlockHitResult hit, Level world) {
        if (hit.getType() == HitResult.Type.BLOCK && hit.getDirection() == Direction.UP) {
            Vec3 pos1 = hit.getLocation();
            Direction facing = world.getBlockState(hit.getBlockPos())
                    .getValue(HorizontalDirectionalBlock.FACING).getOpposite();
            BlockPos pos = hit.getBlockPos();

            boolean left = false;
            boolean right = false;
            switch (facing) {
                case NORTH -> {
                    left = pos1.x - (double) pos.getX() < 0.5D;
                    right = pos1.x - (double) pos.getX() > 0.5D;
                }
                case SOUTH -> {
                    left = pos1.x - (double) pos.getX() > 0.5D;
                    right = pos1.x - (double) pos.getX() < 0.5D;
                }
                case EAST -> {
                    left = pos1.z - (double) pos.getZ() < 0.5D;
                    right = pos1.z - (double) pos.getZ() > 0.5D;
                }
                case WEST -> {
                    left = pos1.z - (double) pos.getZ() > 0.5D;
                    right = pos1.z - (double) pos.getZ() < 0.5D;
                }
                default -> {
                }
            }

            if (left) {
                return 0;
            } else if (right) {
                return 1;
            }
        }
        return getContainerSize();
    }
}
