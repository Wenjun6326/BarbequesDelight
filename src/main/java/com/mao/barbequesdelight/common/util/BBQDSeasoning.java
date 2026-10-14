package com.mao.barbequesdelight.common.util;

import net.minecraft.ChatFormatting;
import net.minecraft.core.component.DataComponents;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.food.FoodProperties;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.component.Consumable;
import net.minecraft.world.item.component.CustomData;
import net.minecraft.world.item.consume_effects.ApplyStatusEffectsConsumeEffect;
import net.minecraft.world.item.consume_effects.ConsumeEffect;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

/**
 * The six seasonings a skewer can carry. The flavour is stored on the stack as the
 * {@code seasoning} string of {@code minecraft:custom_data}, matching the NBT key used
 * by the 1.20.1 original so seasoned skewers behave identically.
 *
 * <p>The first three are dry powders (64 uses); the last three are sauces (16 uses), matching
 * the Forge original. {@code BARBEQUE} uses the Forge spelling, which is also the item id
 * ({@code barbeque_sauce}).</p>
 */
public enum BBQDSeasoning {
    CUMIN(ChatFormatting.YELLOW, true),
    PEPPER(ChatFormatting.GRAY, true),
    CHILLI(ChatFormatting.DARK_RED, true),
    HONEY_MUSTARD(ChatFormatting.YELLOW, false),
    BUFFALO(ChatFormatting.RED, false),
    BARBEQUE(ChatFormatting.GOLD, false);

    public static final String TAG_SEASONING = "seasoning";

    /** Uses a powder carries before running out. */
    public static final int POWDER_USES = 64;
    /** Uses a sauce carries before running out. */
    public static final int SAUCE_USES = 16;

    public final ChatFormatting color;
    public final String name;
    /** True for the dry powders, false for the sauces. */
    public final boolean powder;

    BBQDSeasoning(ChatFormatting color, boolean powder) {
        this.color = color;
        this.name = name().toLowerCase(Locale.ROOT);
        this.powder = powder;
    }

    public ChatFormatting getColor() {
        return color;
    }

    public String getName() {
        return name;
    }

    /** {@code cumin_powder} for powders, {@code honey_mustard_sauce} for sauces. */
    public String getItemId() {
        return name + (powder ? "_powder" : "_sauce");
    }

    public int getUses() {
        return powder ? POWDER_USES : SAUCE_USES;
    }

    /** Pepper halves the time it takes to eat a skewer. */
    public int pepper(int time) {
        return (this == PEPPER) ? (time / 2) : time;
    }

    /**
     * Effects applied when a seasoned skewer is eaten, matching the Forge original.
     *
     * <p>Note that Forge applies the fire damage to both CHILLI and BUFFALO; the 1.20.1 Fabric
     * release only damaged with CHILLI and also restored hunger, which the Forge version never
     * does. This port follows Forge.</p>
     */
    public void onFinish(LivingEntity le) {
        if (this == CHILLI || this == BUFFALO) {
            le.hurt(le.damageSources().inFire(), 2);
        }

        if (this == CUMIN) {
            le.heal(2);
        }
    }

    /**
     * Rewrites the food-related components of a skewer for this flavour, mirroring Forge's
     * {@code modify} and {@code appendEffect}:
     *
     * <ul>
     *   <li>PEPPER shortens the eating time.</li>
     *   <li>CHILLI makes the skewer edible even when full.</li>
     *   <li>HONEY_MUSTARD halves the effect duration and doubles its chance.</li>
     *   <li>BUFFALO halves the effect duration and raises its amplifier by one.</li>
     *   <li>BARBEQUE doubles the effect duration.</li>
     * </ul>
     *
     * <p>In 1.20.1 these were baked into the item's {@code FoodProperties} at seasoning time.
     * 26.1 splits that into the {@code minecraft:food} and {@code minecraft:consumable}
     * components, so both are rebuilt here.</p>
     */
    public void applyTo(ItemStack stack) {
        FoodProperties base = stack.get(DataComponents.FOOD);
        if (base == null) {
            return;
        }

        // 26.1's FoodProperties is a plain record: nutrition, saturation, always-edible.
        stack.set(DataComponents.FOOD, new FoodProperties(
                base.nutrition(), base.saturation(), base.canAlwaysEat() || this == CHILLI));

        Consumable consumable = stack.get(DataComponents.CONSUMABLE);
        if (consumable == null) {
            return;
        }
        Consumable.Builder builder = Consumable.builder()
                .consumeSeconds(this == PEPPER
                        ? consumable.consumeSeconds() / 2.0F
                        : consumable.consumeSeconds())
                .animation(consumable.animation())
                .sound(consumable.sound())
                .hasConsumeParticles(consumable.hasConsumeParticles());

        for (ConsumeEffect effect : consumable.onConsumeEffects()) {
            builder.onConsume(adjustEffect(effect));
        }
        stack.set(DataComponents.CONSUMABLE, builder.build());
    }

    /** Applies this flavour's duration / amplifier / chance change to one consume effect. */
    private ConsumeEffect adjustEffect(ConsumeEffect effect) {
        if (!(effect instanceof ApplyStatusEffectsConsumeEffect applied)) {
            return effect;
        }
        List<MobEffectInstance> adjusted = new ArrayList<>(applied.effects().size());
        for (MobEffectInstance instance : applied.effects()) {
            int duration = instance.getDuration();
            int amplifier = instance.getAmplifier();
            switch (this) {
                case HONEY_MUSTARD -> duration /= 2;
                case BUFFALO -> {
                    duration /= 2;
                    amplifier++;
                }
                case BARBEQUE -> duration *= 2;
                default -> {
                }
            }
            adjusted.add(new MobEffectInstance(instance.getEffect(), duration, amplifier));
        }
        float chance = this == HONEY_MUSTARD ? applied.probability() * 2.0F : applied.probability();
        return new ApplyStatusEffectsConsumeEffect(adjusted, chance);
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

    /**
     * Writes this flavour onto {@code stack}'s custom data and rewrites its food components.
     */
    public void apply(ItemStack stack) {
        CustomData.update(DataComponents.CUSTOM_DATA, stack, tag -> tag.putString(TAG_SEASONING, name()));
        applyTo(stack);
    }
}
