package com.mao.barbequesdelight.common.block.blockentity;

import com.mao.barbequesdelight.registry.BBQDEntityTypes;
import net.minecraft.core.BlockPos;
import net.minecraft.core.NonNullList;
import net.minecraft.world.ContainerHelper;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueOutput;
import org.joml.Vector2f;
import vectorwing.farmersdelight.common.block.entity.SyncedBlockEntity;

/**
 * The tray: three independent display slots.
 *
 * <p>Empty-hand interaction takes one item from the highest occupied slot, or the whole
 * stack when sneaking.</p>
 */
public class TrayBlockEntity extends SyncedBlockEntity implements BlockEntityInv {
    public final NonNullList<ItemStack> items = NonNullList.withSize(3, ItemStack.EMPTY);

    public TrayBlockEntity(BlockPos pos, BlockState state) {
        this(BBQDEntityTypes.TRAY, pos, state);
    }

    public TrayBlockEntity(BlockEntityType<?> type, BlockPos pos, BlockState state) {
        super(type, pos, state);
    }

    @Override
    public NonNullList<ItemStack> getItems() {
        return items;
    }

    public Vector2f getTrayItemOffset(int index) {
        final float yOffset = 0.2f;
        final Vector2f[] offsets = {
                new Vector2f(0, yOffset + 0.1f),
                new Vector2f(0, yOffset - 0.2f),
                new Vector2f(0, yOffset - 0.5f)
        };
        return offsets[index];
    }

    /**
     * Puts the held stack onto the tray, mirroring the Forge original's
     * {@code StorageTile#insert}: an existing matching slot is topped up first, otherwise the
     * first empty slot is used. {@code one} inserts a single item (sneaking) instead of the whole
     * stack, and the amount is capped by the slot's remaining room.
     *
     * <p>Invoked for both hands on every right-click, so it must stay a no-op on the client:
     * the server is the only side allowed to move items.</p>
     *
     * @return true when an item would be (or was) accepted, so the interaction predicts correctly
     */
    public boolean insert(Level level, ItemStack handStack, boolean one) {
        if (handStack.isEmpty()) {
            return false;
        }

        int target = -1;
        for (int i = 0; i < getContainerSize(); i++) {
            ItemStack slot = getItem(i);
            if (slot.isEmpty()) {
                if (target < 0) {
                    target = i;
                }
            } else if (ItemStack.isSameItemSameComponents(slot, handStack)) {
                target = i;
                break;
            }
        }
        if (target < 0) {
            return false;
        }

        ItemStack slot = getItem(target);
        int room = handStack.getMaxStackSize() - slot.getCount();
        int amount = Math.min(room, one ? 1 : handStack.getCount());
        if (amount <= 0) {
            return false;
        }

        if (!level.isClientSide()) {
            ItemStack copy = handStack.split(amount);
            copy.grow(slot.getCount());
            setItem(target, copy);
            inventoryChanged();
        }
        return true;
    }

    /**
     * Takes from the highest occupied slot: a single item when sneaking, the whole stack
     * otherwise - the Forge original's {@code split(isShiftKeyDown() ? 1 : getCount())}.
     *
     * @return true when something would be (or was) taken
     */
    public boolean take(Player player, boolean one) {
        for (int i = getContainerSize() - 1; i >= 0; i--) {
            ItemStack slot = getItem(i);
            if (slot.isEmpty()) {
                continue;
            }

            Level level = getLevel();
            if (level == null || !level.isClientSide()) {
                int amount = one ? 1 : slot.getCount();
                player.getInventory().placeItemBackInInventory(slot.split(amount));
                inventoryChanged();
            }
            return true;
        }
        return false;
    }

    public void inventoryChanged() {
        setChanged();
        Level level = this.getLevel();
        if (level != null && !level.isClientSide()) {
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
