package com.mao.barbequesdelight.common.item;

import com.mao.barbequesdelight.common.util.BBQDSeasoning;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.food.FoodProperties;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.item.component.TooltipDisplay;
import net.minecraft.world.level.Level;
import org.jetbrains.annotations.Nullable;
import vectorwing.farmersdelight.common.item.ConsumableItem;

import java.util.function.Consumer;

/**
 * A cooked skewer. Seasoning changes its display name, its tooltip, how long it takes to
 * eat (pepper), and adds an on-eat effect.
 */
public class SimpleSkewerItem extends ConsumableItem {

    public SimpleSkewerItem(Item.Properties properties, FoodProperties food, boolean hasTooltip) {
        super(properties.food(food, com.mao.barbequesdelight.registry.BBQDFoods.consumableFor(food)), hasTooltip);
    }

    @Override
    public int getUseDuration(ItemStack stack, LivingEntity entity) {
        BBQDSeasoning seasoning = BBQDSeasoning.matching(stack);
        if (seasoning != null) {
            return seasoning.pepper(super.getUseDuration(stack, entity));
        }
        return super.getUseDuration(stack, entity);
    }

    @Override
    public void affectConsumer(ItemStack stack, Level world, LivingEntity le) {
        super.affectConsumer(stack, world, le);
        BBQDSeasoning seasoning = BBQDSeasoning.matching(stack);
        if (seasoning != null) {
            seasoning.onFinish(le);
        }
    }

    @Override
    public Component getName(ItemStack stack) {
        BBQDSeasoning seasoning = BBQDSeasoning.matching(stack);
        if (seasoning != null) {
            return Component.translatable("item.barbequesdelight." + seasoning.getName() + ".title")
                    .append(Component.translatable(stack.getItem().getDescriptionId()));
        }
        return super.getName(stack);
    }

    @Override
    public void appendHoverText(ItemStack stack, Item.TooltipContext context, TooltipDisplay display,
                                Consumer<Component> tooltip, TooltipFlag flag) {
        super.appendHoverText(stack, context, display, tooltip, flag);

        BBQDSeasoning seasoning = BBQDSeasoning.matching(stack);
        if (seasoning != null) {
            MutableComponent text = Component.translatable("item.barbequesdelight.skewers.tooltip")
                    .withStyle(ChatFormatting.GOLD)
                    .append(Component.translatable("item.barbequesdelight.flavor." + seasoning.getName())
                            .withStyle(seasoning.getColor()));
            tooltip.accept(text);
        }
    }
}
