package com.mao.barbequesdelight.registry;

import com.mao.barbequesdelight.common.item.SeasoningItem;
import net.fabricmc.fabric.api.event.player.UseBlockCallback;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.item.ItemStack;
import vectorwing.farmersdelight.common.block.entity.CuttingBoardBlockEntity;

/**
 * Lets a seasoning be applied to a skewer sitting on a Farmer's Delight cutting board.
 * Runs on both sides so the client prediction shows the particles and sound immediately.
 */
public class BBQDEvents {
    public static void registerBBQDEvents() {
        UseBlockCallback.EVENT.register((player, world, hand, hitResult) -> {
            if (world.getBlockEntity(hitResult.getBlockPos()) instanceof CuttingBoardBlockEntity board) {
                ItemStack handStack = player.getItemInHand(hand);
                ItemStack storedStack = board.getStoredItem();

                if (handStack.getItem() instanceof SeasoningItem seasoningItem
                        && seasoningItem.canSprinkle(storedStack)) {
                    seasoningItem.sprinkle(storedStack, hitResult.getLocation(), player, handStack);
                    return InteractionResult.SUCCESS;
                }
            }
            return InteractionResult.PASS;
        });
    }
}
