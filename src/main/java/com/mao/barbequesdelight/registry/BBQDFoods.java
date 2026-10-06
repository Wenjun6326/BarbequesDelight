package com.mao.barbequesdelight.registry;

import net.minecraft.core.Holder;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.food.FoodProperties;
import net.minecraft.world.item.component.Consumable;
import net.minecraft.world.item.consume_effects.ApplyStatusEffectsConsumeEffect;
import vectorwing.farmersdelight.common.registry.ModEffects;

/**
 * Food values, identical to the 1.20.1 release.
 *
 * <p>In 26.1 status effects are no longer part of {@link FoodProperties}: they live on the
 * {@code minecraft:consumable} component as {@link ApplyStatusEffectsConsumeEffect}s.
 * {@link #consumableFor} rebuilds the original effect payload for each food.</p>
 */
public class BBQDFoods {
    public static final FoodProperties GRILLED_COD_SKEWER = foods(7, 0.5f, false).build();
    public static final FoodProperties GRILLED_SALMON_SKEWER = foods(7, 0.5f, false).build();
    public static final FoodProperties GRILLED_CHICKEN_SKEWER = foods(7, 0.5f, false).build();
    public static final FoodProperties SKEWER_SANDWICH = foods(14, 0.8f, false).build();

    // Effect-bearing foods
    public static final FoodProperties BURNT_FOOD = effect(4, 0f, true, MobEffects.NAUSEA, 200, 0, 1.0f).build();
    public static final FoodProperties GRILLED_LAMB_SKEWER = effect(12, 0.6f, false, MobEffects.REGENERATION, 20 * 60, 0, 0.5f).build();
    public static final FoodProperties GRILLED_RABBIT_SKEWER = effect(10, 0.5f, false, MobEffects.JUMP_BOOST, 20 * 60, 0, 1.0f).build();
    public static final FoodProperties GRILLED_PORK_SAUSAGE_SKEWER = effect(8, 0.8f, false, MobEffects.RESISTANCE, 20 * 90, 0, 0.5f).build();
    public static final FoodProperties GRILLED_POTATO_SKEWER = effect(6, 0.6f, false, ModEffects.NOURISHMENT, 20 * 90, 0, 0.5f).build();
    public static final FoodProperties SKEWER_WRAP = effect(9, 0.8f, false, ModEffects.NOURISHMENT, 20 * 60, 0, 0.5f).build();
    public static final FoodProperties GRILLED_BEEF_SKEWER = effect(10, 0.6f, false, MobEffects.STRENGTH, 20 * 60, 0, 0.5f).build();
    public static final FoodProperties BIBIMBAP = effect(16, 0.8f, false, ModEffects.COMFORT, 20 * 90, 0, 1.0f).build();
    public static final FoodProperties GRILLED_FRUIT_AND_VEGETABLE_SKEWER = effect(8, 0.8f, false, MobEffects.REGENERATION, 20 * 120, 0, 0.5f).build();

    /**
     * The original effect payloads, kept in the same order as the {@code effect(...)}
     * declarations above so {@link #consumableFor(FoodProperties)} can rebuild them.
     */
    private record EffectSpec(Holder<MobEffect> effect, int duration, int amplifier, float chance) {
    }

    private static EffectSpec spec(FoodProperties food) {
        if (food == BURNT_FOOD) return new EffectSpec(MobEffects.NAUSEA, 200, 0, 1.0f);
        if (food == GRILLED_LAMB_SKEWER) return new EffectSpec(MobEffects.REGENERATION, 20 * 60, 0, 0.5f);
        if (food == GRILLED_RABBIT_SKEWER) return new EffectSpec(MobEffects.JUMP_BOOST, 20 * 60, 0, 1.0f);
        if (food == GRILLED_PORK_SAUSAGE_SKEWER) return new EffectSpec(MobEffects.RESISTANCE, 20 * 90, 0, 0.5f);
        if (food == GRILLED_POTATO_SKEWER) return new EffectSpec(ModEffects.NOURISHMENT, 20 * 90, 0, 0.5f);
        if (food == SKEWER_WRAP) return new EffectSpec(ModEffects.NOURISHMENT, 20 * 60, 0, 0.5f);
        if (food == GRILLED_BEEF_SKEWER) return new EffectSpec(MobEffects.STRENGTH, 20 * 60, 0, 0.5f);
        if (food == BIBIMBAP) return new EffectSpec(ModEffects.COMFORT, 20 * 90, 0, 1.0f);
        if (food == GRILLED_FRUIT_AND_VEGETABLE_SKEWER) return new EffectSpec(MobEffects.REGENERATION, 20 * 120, 0, 0.5f);
        return null;
    }

    /**
     * Builds the {@code minecraft:consumable} component for a food, restoring the status
     * effects that used to be baked into {@code FoodProperties} in 1.20.1.
     */
    public static Consumable consumableFor(FoodProperties food) {
        Consumable.Builder builder = Consumable.builder()
                .consumeSeconds(Consumable.DEFAULT_CONSUME_SECONDS)
                .animation(net.minecraft.world.item.ItemUseAnimation.EAT)
                .sound(net.minecraft.sounds.SoundEvents.GENERIC_EAT)
                .hasConsumeParticles(true);

        EffectSpec spec = spec(food);
        if (spec != null) {
            builder.onConsume(new ApplyStatusEffectsConsumeEffect(
                    new MobEffectInstance(spec.effect(), spec.duration(), spec.amplifier()),
                    spec.chance()));
        }
        return builder.build();
    }

    private static FoodProperties.Builder foods(int hunger, float saturation, boolean alwaysEdible) {
        FoodProperties.Builder builder = new FoodProperties.Builder();
        if (alwaysEdible) {
            builder.alwaysEdible();
        }
        return builder.nutrition(hunger).saturationModifier(saturation);
    }

    private static FoodProperties.Builder effect(int hunger, float saturation, boolean alwaysEdible,
                                                Holder<MobEffect> effect, int duration, int amplifier, float chance) {
        return foods(hunger, saturation, alwaysEdible);
    }
}
