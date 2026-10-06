package com.mao.barbequesdelight.common.block.client;

import com.mao.barbequesdelight.common.block.blockentity.TrayBlockEntity;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;
import net.minecraft.core.Direction;
import net.minecraft.world.item.ItemStack;

/**
 * Draws the tray's three slots. Each slot's stack size decides how many flat item models
 * are drawn, spread along the tray's length.
 */
public class TrayRenderer
        extends AbstractItemDisplayRenderer<TrayBlockEntity, TrayRenderer.State> {

    public static class State extends AbstractItemDisplayRenderer.DisplayRenderState {
    }

    public TrayRenderer(BlockEntityRendererProvider.Context context) {
        super(context);
    }

    @Override
    public State createRenderState() {
        return new State();
    }

    @Override
    protected void extractItems(TrayBlockEntity entity, State state, Direction facing) {
        for (int i = 0; i < entity.getItems().size(); i++) {
            ItemStack stack = entity.getItems().get(i);
            if (stack.isEmpty()) {
                continue;
            }

            int models = getModelCount(stack);
            // Vertical slot offset, matching the original tray placement.
            float slotLift = 0.3f + (i == 0 ? 0.1f : i == 1 ? -0.2f : -0.5f) - 0.2f;

            for (int j = 0; j < models; j++) {
                ItemEntry entry = addItem(entity, state, stack);

                // Spread the models along the axis the tray is facing.
                float spread = 0.3f - (j * 0.2f);
                if (facing.getAxis() == Direction.Axis.Z) {
                    entry.offsetX = spread - 0.0f;
                    entry.offsetY = 0.0f;
                } else {
                    entry.offsetX = 0.0f;
                    entry.offsetY = spread;
                }

                entry.lift = 0.075 + slotLift;
                entry.flat = true;
                entry.scale = 0.375f;
            }
        }
    }

    private int getModelCount(ItemStack stack) {
        int maxCount = stack.getMaxStackSize();
        int count = stack.getCount();

        if (maxCount == 64) {
            if (count >= 64) return 4;
            else if (count >= 48) return 3;
            else if (count >= 32) return 2;
            else return 1;
        } else {
            if (count >= 16) return 4;
            else if (count >= 12) return 3;
            else if (count >= 6) return 2;
            else return 1;
        }
    }
}
