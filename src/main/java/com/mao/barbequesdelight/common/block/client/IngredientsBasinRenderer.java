package com.mao.barbequesdelight.common.block.client;

import com.mao.barbequesdelight.common.block.blockentity.IngredientsBasinBlockEntity;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;
import net.minecraft.core.Direction;
import net.minecraft.util.RandomSource;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;

/**
 * Draws the ingredient piles in the basin. Stack size controls how many models are drawn,
 * and a stack-seeded random scatters them slightly so a pile looks hand-placed.
 */
public class IngredientsBasinRenderer
        extends AbstractItemDisplayRenderer<IngredientsBasinBlockEntity, IngredientsBasinRenderer.State> {

    public static class State extends AbstractItemDisplayRenderer.DisplayRenderState {
    }

    public IngredientsBasinRenderer(BlockEntityRendererProvider.Context context) {
        super(context);
    }

    @Override
    public State createRenderState() {
        return new State();
    }

    @Override
    protected void extractItems(IngredientsBasinBlockEntity entity, State state, Direction facing) {
        for (int i = 0; i < entity.getItems().size(); i++) {
            ItemStack stack = entity.getItems().get(i);

            // The original seeded the scatter from the item id plus the stack count.
            int seed = stack.isEmpty() ? 187 : Item.getId(stack.getItem()) + stack.getCount();
            RandomSource random = seededRandom(seed);

            int models = getModelCount(stack);
            for (int j = 0; j < models; j++) {
                float scatterX = (random.nextFloat() * 2.0F - 1.0F) * 0.15F * 0.2F;
                float scatterZ = (random.nextFloat() * 2.0F - 1.0F) * 0.15F * 0.2F;

                ItemEntry entry = addItem(entity, state, stack);
                entry.usePreTranslation = true;
                entry.flat = false; // the pile pose already includes its own tilt
                entry.scale = 0.375f;

                // Basin half offset, matching getBasinItemOffset.
                entry.offsetX = i == 0 ? 0.2f : -0.2f;
                entry.offsetY = 0.0f;

                applyPilePose(entry, facing.getOpposite(), j, scatterX, scatterZ);
            }
        }
    }

    protected int getModelCount(ItemStack stack) {
        if (stack.getCount() > 48) {
            return 12;
        } else if (stack.getCount() > 32) {
            return 10;
        } else if (stack.getCount() > 16) {
            return 8;
        } else if (stack.getCount() > 8) {
            return 6;
        } else {
            return stack.getCount() > 1 ? 2 : 1;
        }
    }

    /** Port of the original {@code renderPose} switch. */
    private static void applyPilePose(ItemEntry entry, Direction direction, int count,
                                      float xOffset, float zOffset) {
        switch (direction) {
            case SOUTH -> {
                entry.preX = (float) (0.5 + xOffset);
                entry.preY = 0.2f;
                entry.preZ = (float) (0.15 + (double) count / 20);
                entry.preXRot = -(20 + count * 4);
            }
            case NORTH -> {
                entry.preX = (float) (0.5 + xOffset);
                entry.preY = 0.2f;
                entry.preZ = (float) (0.85 - (double) count / 20);
                entry.preXRot = -(20 + count * 4);
            }
            case EAST -> {
                entry.preX = (float) (0.15 + (double) count / 20);
                entry.preY = 0.2f;
                entry.preZ = (float) (0.5 + zOffset);
                entry.preZRot = (20 + count * 4);
            }
            case WEST -> {
                entry.preX = (float) (0.85 - (double) count / 20);
                entry.preY = 0.2f;
                entry.preZ = (float) (0.5 + zOffset);
                entry.preZRot = -(20 + count * 4);
            }
            default -> {
            }
        }
    }
}
