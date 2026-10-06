package com.mao.barbequesdelight.common.item;

import com.mao.barbequesdelight.common.util.BBQDSeasoning;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.core.particles.DustParticleOptions;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.item.component.TooltipDisplay;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;
import net.minecraft.sounds.SoundEvents;
import org.jetbrains.annotations.Nullable;

import java.util.function.Consumer;

/**
 * A seasoning powder or sauce. Carries 64 uses and applies its flavour to a skewer placed
 * on a Farmer's Delight cutting board.
 */
public class SeasoningItem extends Item {
    private final BBQDSeasoning seasoning;

    public SeasoningItem(Item.Properties properties, BBQDSeasoning seasoning) {
        super(properties.durability(64));
        this.seasoning = seasoning;
    }

    public BBQDSeasoning getSeasoning() {
        return seasoning;
    }

    @Environment(EnvType.CLIENT)
    @Override
    public void appendHoverText(ItemStack stack, Item.TooltipContext context, TooltipDisplay display,
                                Consumer<Component> tooltip, TooltipFlag flag) {
        tooltip.accept(Component.translatable("item.barbequesdelight." + getSeasoning().getName() + ".tooltip")
                .withStyle(ChatFormatting.YELLOW));
    }

    /** Applies this seasoning to {@code skewer}, plays the feedback, and uses one durability. */
    public void sprinkle(ItemStack skewer, Vec3 pos, Player player, ItemStack stackInHand) {
        getSeasoning().apply(skewer);

        player.playSound(SoundEvents.SAND_BREAK, 0.7f, 1.0f);

        Integer color = seasoning.color.getColor();
        int rgb = color == null ? 0 : color;
        float r = ((rgb >> 16) & 0xFF) / 255.0f;
        float g = ((rgb >> 8) & 0xFF) / 255.0f;
        float b = (rgb & 0xFF) / 255.0f;
        player.level().addParticle(new DustParticleOptions(rgb, 1.5f), pos.x, pos.y, pos.z, 8, 0d, 0);

        stackInHand.hurtAndBreak(1, player, net.minecraft.world.entity.EquipmentSlot.MAINHAND);
    }

    public boolean canSprinkle(ItemStack storedStack) {
        if (storedStack.isEmpty()) {
            return false;
        }
        if (!(storedStack.getItem() instanceof SimpleSkewerItem)) {
            return false;
        }
        // A skewer may only be seasoned once.
        return !BBQDSeasoning.hasSeasoning(storedStack);
    }
}
