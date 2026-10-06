package com.mao.barbequesdelight.common.block.client;

import com.mao.barbequesdelight.common.block.blockentity.BlockEntityInv;
import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderer;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;
import net.minecraft.client.renderer.blockentity.state.BlockEntityRenderState;
import net.minecraft.client.renderer.feature.ModelFeatureRenderer;
import net.minecraft.client.renderer.item.ItemModelResolver;
import net.minecraft.client.renderer.item.ItemStackRenderState;
import net.minecraft.client.renderer.state.level.CameraRenderState;
import net.minecraft.core.Direction;
import net.minecraft.util.RandomSource;
import net.minecraft.world.item.ItemDisplayContext;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.HorizontalDirectionalBlock;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.phys.Vec3;

import java.util.ArrayList;
import java.util.List;

/**
 * Shared machinery for the mod's three item-displaying block entity renderers.
 *
 * <p>Minecraft 26.1 replaced the single {@code render(...)} hook with an extract/submit
 * split, and removed {@code ItemRenderer}: {@link #extractRenderState} builds a plain data
 * snapshot (resolving item models via {@link ItemModelResolver}), and {@link #submit} draws
 * it through a {@link SubmitNodeCollector}.</p>
 */
public abstract class AbstractItemDisplayRenderer<T extends BlockEntity & BlockEntityInv, S extends AbstractItemDisplayRenderer.DisplayRenderState>
        implements BlockEntityRenderer<T, S> {

    /** One resolved item model plus its placement within the block. */
    public static final class ItemEntry {
        public final ItemStackRenderState state = new ItemStackRenderState();
        /** Model offset along the faced axis (blocks). */
        public float offsetX;
        /** Model offset along the perpendicular axis (blocks). */
        public float offsetY;
        /** Vertical lift (blocks). */
        public double lift = 0.0;
        /** Extra yaw applied after the block-facing rotation. */
        public float yawDegrees = 0.0F;
        public float scale = 1.0F;
        /** When false the item is drawn upright instead of lying flat. */
        public boolean flat = true;
        /** Extra pre-rotation about X/Z, applied before facing/yaw (basin piles). */
        public float preXRot = 0.0F;
        public float preZRot = 0.0F;
        /** Pre-translation, applied before facing/yaw (basin piles). */
        public float preX;
        public float preY;
        public float preZ;
        public boolean usePreTranslation = false;
    }

    /** Snapshot of everything the submit pass needs. */
    public static class DisplayRenderState extends BlockEntityRenderState {
        public final List<ItemEntry> items = new ArrayList<>();
        /** Yaw that aligns models with the block's facing. */
        public float facingYaw;
        public Direction.Axis facingAxis = Direction.Axis.Z;

        public ItemEntry newEntry() {
            ItemEntry entry = new ItemEntry();
            items.add(entry);
            return entry;
        }

        public void reset() {
            items.clear();
        }
    }

    protected final ItemModelResolver itemModelResolver;

    protected AbstractItemDisplayRenderer(BlockEntityRendererProvider.Context context) {
        this.itemModelResolver = context.itemModelResolver();
    }

    @Override
    public abstract S createRenderState();

    @Override
    public void extractRenderState(T entity, S state, float partialTick, Vec3 cameraPos,
                                   ModelFeatureRenderer.CrumblingOverlay crumblingOverlay) {
        BlockEntityRenderState.extractBase(entity, state, crumblingOverlay);
        state.reset();

        Direction facing = entity.getBlockState()
                .getValue(HorizontalDirectionalBlock.FACING).getOpposite();
        state.facingYaw = -facing.toYRot();
        state.facingAxis = facing.getAxis();

        extractItems(entity, state, facing);
    }

    /** Populates {@code state} with the items to draw. */
    protected abstract void extractItems(T entity, S state, Direction facing);

    /** Resolves {@code stack} into a fresh entry appended to {@code state}. */
    protected ItemEntry addItem(T entity, S state, ItemStack stack) {
        ItemEntry entry = state.newEntry();
        itemModelResolver.updateForTopItem(entry.state, stack, ItemDisplayContext.FIXED,
                entity.getLevel(), null, 0);
        return entry;
    }

    /** A deterministic random source, matching the original seeded particle/model scatter. */
    protected static RandomSource seededRandom(long seed) {
        RandomSource random = RandomSource.create();
        random.setSeed(seed);
        return random;
    }

    @Override
    public void submit(S state, PoseStack poseStack, SubmitNodeCollector collector, CameraRenderState camera) {
        for (ItemEntry entry : state.items) {
            if (entry.state.isEmpty()) {
                continue;
            }

            poseStack.pushPose();

            if (entry.usePreTranslation) {
                poseStack.translate(entry.preX, entry.preY, entry.preZ);
                if (entry.preXRot != 0.0F) {
                    poseStack.mulPose(com.mojang.math.Axis.XP.rotationDegrees(entry.preXRot));
                }
                if (entry.preZRot != 0.0F) {
                    poseStack.mulPose(com.mojang.math.Axis.ZP.rotationDegrees(entry.preZRot));
                }
                poseStack.mulPose(com.mojang.math.Axis.YP.rotationDegrees(state.facingYaw));
            } else {
                float x = state.facingAxis == Direction.Axis.X ? 0.5F : 0.5F;
                poseStack.translate(x, 0.0, 0.5);
                poseStack.mulPose(com.mojang.math.Axis.YP.rotationDegrees(state.facingYaw));
                if (entry.flat) {
                    poseStack.mulPose(com.mojang.math.Axis.XP.rotationDegrees(90.0F));
                }
                poseStack.translate(0.0, entry.lift, 0.0);
            }

            poseStack.translate(entry.offsetX, entry.offsetY, 0.0);
            poseStack.scale(entry.scale, entry.scale, entry.scale);
            if (entry.yawDegrees != 0.0F) {
                poseStack.mulPose(com.mojang.math.Axis.YP.rotationDegrees(entry.yawDegrees));
            }

            entry.state.submit(poseStack, collector, state.lightCoords, 0, 0);
            poseStack.popPose();
        }
    }
}
