package com.mao.barbequesdelight.common.block.client;

import com.mao.barbequesdelight.common.block.blockentity.GrillBlockEntity;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;
import net.minecraft.core.Direction;
import net.minecraft.world.item.ItemStack;

/**
 * Draws the two grill halves. Items lie flat on the grate, and a flipped item is rotated
 * 180 degrees to show its other side.
 */
public class GrillRenderer
        extends AbstractItemDisplayRenderer<GrillBlockEntity, GrillRenderer.State> {

    public static class State extends AbstractItemDisplayRenderer.DisplayRenderState {
    }

    public GrillRenderer(BlockEntityRendererProvider.Context context) {
        super(context);
    }

    @Override
    public State createRenderState() {
        return new State();
    }

    @Override
    protected void extractItems(GrillBlockEntity entity, State state, Direction facing) {
        for (int i = 0; i < entity.getItems().size(); i++) {
            ItemStack stack = entity.getItems().get(i);
            if (stack.isEmpty()) {
                continue;
            }

            ItemEntry entry = addItem(entity, state, stack);

            final float xOffset = 0.2f;
            final float yOffset = 0.0f;
            float[] xs = {xOffset, -xOffset};
            float[] ys = {yOffset, yOffset};

            // The original swaps the offset axes for the two odd-indexed directions.
            int directionIndex = facing.get2DDataValue();
            if (directionIndex % 2 != 0) {
                entry.offsetX = ys[i];
                entry.offsetY = xs[i];
            } else {
                entry.offsetX = xs[i];
                entry.offsetY = ys[i];
            }

            entry.lift = 0.96;
            entry.flat = true;
            entry.scale = 0.4f;
            entry.yawDegrees = entity.getFlipped(i) ? 180.0F : 0.0F;
        }
    }
}
