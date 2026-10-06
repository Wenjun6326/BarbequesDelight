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

    /** Takes one item (or the whole stack when sneaking) from the highest occupied slot. */
    public boolean removeItems(Player player) {
        for (int i = getContainerSize() - 1; i >= 0; i--) {
            ItemStack stack1 = getItem(i);
            if (!stack1.isEmpty()) {
                int count = player.isSecondaryUseActive() ? stack1.getCount() : 1;
                player.getInventory().placeItemBackInInventory(stack1.split(count));
                inventoryChanged();
                return true;
            }
        }
        return false;
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
