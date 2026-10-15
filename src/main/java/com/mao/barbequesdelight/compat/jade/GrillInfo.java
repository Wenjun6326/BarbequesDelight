package com.mao.barbequesdelight.compat.jade;

import com.mao.barbequesdelight.common.block.blockentity.GrillBlockEntity;
import com.mao.barbequesdelight.registry.BBQDItems;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import net.minecraft.world.item.ItemStack;
import snownee.jade.api.BlockAccessor;
import snownee.jade.api.IBlockComponentProvider;
import snownee.jade.api.ITooltip;
import snownee.jade.api.config.IPluginConfig;

/**
 * Shows, for each occupied grill half, the item on it and how far along it is.
 *
 * <p>Ported from the Forge original's {@code compat/jade/GrillInfo}. The state wording mirrors
 * {@code GrillBlockEntity.ItemEntry#getTooltip} there exactly:</p>
 * <ul>
 *   <li>not yet half done -&gt; remaining seconds</li>
 *   <li>half done and not flipped -&gt; "ready to flip"</li>
 *   <li>flipped, still cooking -&gt; remaining seconds</li>
 *   <li>flipped and done -&gt; "cooked"</li>
 * </ul>
 */
public class GrillInfo implements IBlockComponentProvider {

    public static final GrillInfo INSTANCE = new GrillInfo();

    public static final Identifier UID =
            Identifier.fromNamespaceAndPath("barbequesdelight", "grill");

    @Override
    public void appendTooltip(ITooltip tooltip, BlockAccessor access, IPluginConfig config) {
        if (!(access.getBlockEntity() instanceof GrillBlockEntity grill)) {
            return;
        }

        for (int i = 0; i < grill.getContainerSize(); i++) {
            ItemStack stack = grill.getItem(i);
            if (stack.isEmpty()) {
                continue;
            }

            tooltip.add(stack.getHoverName());
            tooltip.append(statusOf(grill, i, stack));
        }
    }

    /** Mirrors the Forge {@code ItemEntry#getTooltip} state machine. */
    private static Component statusOf(GrillBlockEntity grill, int i, ItemStack stack) {
        int time = grill.getCookingTime(i);
        int duration = grill.getCookingTotalTime(i);

        if (duration <= 0) {
            return Component.empty();
        }

        if (stack.is(BBQDItems.BURNT_FOOD)) {
            return Component.translatable("barbequesdelight.jade.burnt")
                    .withStyle(ChatFormatting.RED);
        }

        if (!grill.getFlipped(i)) {
            if (time < duration / 2) {
                // Round up, so a nearly-finished half never reads as 0 seconds.
                return Component.translatable("barbequesdelight.jade.cook", (duration / 2 - time + 19) / 20)
                        .withStyle(ChatFormatting.GRAY);
            }
            return Component.translatable("barbequesdelight.jade.flip")
                    .withStyle(ChatFormatting.YELLOW);
        }

        if (time < duration) {
            return Component.translatable("barbequesdelight.jade.cook", (duration - time + 19) / 20)
                    .withStyle(ChatFormatting.GRAY);
        }
        return Component.translatable("barbequesdelight.jade.cooked")
                .withStyle(ChatFormatting.YELLOW);
    }

    @Override
    public Identifier getUid() {
        return UID;
    }
}
