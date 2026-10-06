package com.mao.barbequesdelight.common.util;

import net.minecraft.ChatFormatting;
import net.minecraft.core.component.DataComponents;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.component.CustomData;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

/**
 * The six seasonings a skewer can carry. The flavour is stored on the stack as the
 * {@code seasoning} string of {@code minecraft:custom_data}, matching the NBT key used
 * by the 1.20.1 original so seasoned skewers behave identically.
 */
public enum BBQDSeasoning {
    CUMIN(ChatFormatting.YELLOW),
    PEPPER(ChatFormatting.GRAY),
    CHILLI(ChatFormatting.DARK_RED),
    BUFFALO(ChatFormatting.RED),
    HONEY_MUSTARD(ChatFormatting.YELLOW),
    BARBECUE(ChatFormatting.GOLD);

    public static final String TAG_SEASONING = "seasoning";

    public final ChatFormatting color;
    public final String name;

    BBQDSeasoning(ChatFormatting color) {
        this.color = color;
        this.name = name().toLowerCase(Locale.ROOT);
    }

    public ChatFormatting getColor() {
        return color;
    }

    public String getName() {
        return name;
    }

    /** Pepper halves the time it takes to eat a skewer. */
    public int pepper(int time) {
        return (this == PEPPER) ? (time / 2) : time;
    }

    public void onFinish(LivingEntity le) {
        if (this == CHILLI) {
            le.hurt(le.damageSources().inFire(), 2);
            if (le instanceof Player player) {
                player.getFoodData().eat(2, 0.5f);
            }
        }

        if (this == CUMIN) {
            le.heal(2);
        }

        if (this == BUFFALO) {
            // Snapshot first: effect list is cleared before the modified effects are re-added.
            List<MobEffectInstance> snapshot = new ArrayList<>(le.getActiveEffects());
            le.removeAllEffects();
            for (MobEffectInstance effect : snapshot) {
                le.addEffect(new MobEffectInstance(
                        effect.getEffect(),
                        effect.getDuration() / 2,
                        effect.getAmplifier() + 1));
            }
        }
    }

    @Nullable
    public static BBQDSeasoning matching(ItemStack stack) {
        CompoundTag nbt = stack.getOrDefault(DataComponents.CUSTOM_DATA, CustomData.EMPTY).copyTag();
        String value = nbt.getString(TAG_SEASONING).orElse("");
        if (!value.isEmpty()) {
            try {
                return Enum.valueOf(BBQDSeasoning.class, value);
            } catch (Exception ignored) {
                // Unknown / removed flavour - treat as unseasoned.
            }
        }
        return null;
    }

    /** True when the stack already carries any flavour. */
    public static boolean hasSeasoning(ItemStack stack) {
        CompoundTag nbt = stack.getOrDefault(DataComponents.CUSTOM_DATA, CustomData.EMPTY).copyTag();
        return nbt.contains(TAG_SEASONING);
    }

    /** Writes this flavour onto {@code stack}'s custom data. */
    public void apply(ItemStack stack) {
        CustomData.update(DataComponents.CUSTOM_DATA, stack, tag -> tag.putString(TAG_SEASONING, name()));
    }
}
