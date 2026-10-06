package com.mao.barbequesdelight.registry;

import com.mao.barbequesdelight.BarbequesDelight;
import com.mao.barbequesdelight.common.item.SeasoningItem;
import com.mao.barbequesdelight.common.item.SimpleSkewerItem;
import com.mao.barbequesdelight.common.util.BBQDSeasoning;
import net.minecraft.core.Registry;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.component.Consumable;
import vectorwing.farmersdelight.common.item.ConsumableItem;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Function;

public class BBQDItems {
    /** Registration order is preserved so the creative tab matches the 1.20.1 ordering. */
    private static final List<Item> TAB_ORDER = new ArrayList<>();

    public static final Item GRILL = register("grill",
            props -> new BlockItem(BBQDBlocks.GRILL, props), blockItemProps());

    public static final Item INGREDIENTS_BASIN = register("ingredients_basin",
            props -> new BlockItem(BBQDBlocks.INGREDIENTS_BASIN, props), blockItemProps());

    public static final Item TRAY = register("tray",
            props -> new BlockItem(BBQDBlocks.TRAY, props), blockItemProps());

    public static final Item CUMIN_POWDER = register("cumin_powder",
            props -> new SeasoningItem(props, BBQDSeasoning.CUMIN), new Item.Properties());

    public static final Item PEPPER_POWDER = register("pepper_powder",
            props -> new SeasoningItem(props, BBQDSeasoning.PEPPER), new Item.Properties());

    public static final Item CHILLI_POWDER = register("chilli_powder",
            props -> new SeasoningItem(props, BBQDSeasoning.CHILLI), new Item.Properties());

    // ---------------------------------------------------------------------------------
    // UNFINISHED UPSTREAM FEATURE - deliberately left incomplete.
    //
    // These three sauces exist only in the author's development snapshot; they are NOT in
    // the released 1.1.0 build. Upstream registers them but never shipped any asset or
    // recipe for them: no texture, no item model, no crafting recipe, and no
    // `item.barbequesdelight.<id>` / `.tooltip` / `.title` translation keys (only
    // `item.barbequesdelight.flavor.<flavour>` exists).
    //
    // They consequently show as missing-texture placeholders named after their raw
    // translation key, and cannot be obtained in survival.
    //
    // DO NOT "fix" this locally: this port mirrors upstream so that an upstream update can
    // be merged without conflicts. See the README for the full list of what is missing.
    // ---------------------------------------------------------------------------------
    public static final Item HONEY_MUSTARD_SAUCE = register("honey_mustard_sauce",
            props -> new SeasoningItem(props, BBQDSeasoning.HONEY_MUSTARD), new Item.Properties());

    public static final Item BUFFALO_SAUCE = register("buffalo_sauce",
            props -> new SeasoningItem(props, BBQDSeasoning.BUFFALO), new Item.Properties());

    public static final Item BARBECUE_SAUCE = register("barbecue_sauce",
            props -> new SeasoningItem(props, BBQDSeasoning.BARBECUE), new Item.Properties());

    public static final Item KEBAB_WRAP = register("kebab_wrap",
            props -> new ConsumableItem(props, true), foodProps(BBQDFoods.SKEWER_WRAP));

    public static final Item KEBAB_SANDWICH = register("kebab_sandwich",
            Item::new, foodProps(BBQDFoods.SKEWER_SANDWICH));

    public static final Item BIBIMBAP = register("bibimbap",
            props -> new ConsumableItem(props, true), bowlFoodProps(BBQDFoods.BIBIMBAP));

    public static final Item BURNT_FOOD = register("burnt_food",
            props -> new ConsumableItem(props, true), foodProps(BBQDFoods.BURNT_FOOD));

    public static final Item COD_SKEWER = register("cod_skewer", BBQDItems::rawSkewer, new Item.Properties());
    public static final Item SALMON_SKEWER = register("salmon_skewer", BBQDItems::rawSkewer, new Item.Properties());
    public static final Item CHICKEN_SKEWER = register("chicken_skewer", BBQDItems::rawSkewer, new Item.Properties());
    public static final Item BEEF_SKEWER = register("beef_skewer", BBQDItems::rawSkewer, new Item.Properties());
    public static final Item LAMB_SKEWER = register("lamb_skewer", BBQDItems::rawSkewer, new Item.Properties());
    public static final Item RABBIT_SKEWER = register("rabbit_skewer", BBQDItems::rawSkewer, new Item.Properties());
    public static final Item PORK_SAUSAGE_SKEWER = register("pork_sausage_skewer", BBQDItems::rawSkewer, new Item.Properties());
    public static final Item POTATO_SKEWER = register("potato_skewer", BBQDItems::rawSkewer, new Item.Properties());
    public static final Item FRUIT_AND_VEGETABLE_SKEWER = register("fruit_and_vegetable_skewer", BBQDItems::rawSkewer, new Item.Properties());

    public static final Item GRILLED_COD_SKEWER = register("grilled_cod_skewer",
            props -> grilledSkewer(props, BBQDFoods.GRILLED_COD_SKEWER, false), skewerProps());
    public static final Item GRILLED_SALMON_SKEWER = register("grilled_salmon_skewer",
            props -> grilledSkewer(props, BBQDFoods.GRILLED_SALMON_SKEWER, false), skewerProps());
    public static final Item GRILLED_CHICKEN_SKEWER = register("grilled_chicken_skewer",
            props -> grilledSkewer(props, BBQDFoods.GRILLED_CHICKEN_SKEWER, false), skewerProps());
    public static final Item GRILLED_BEEF_SKEWER = register("grilled_beef_skewer",
            props -> grilledSkewer(props, BBQDFoods.GRILLED_BEEF_SKEWER, true), skewerProps());
    public static final Item GRILLED_LAMB_SKEWER = register("grilled_lamb_skewer",
            props -> grilledSkewer(props, BBQDFoods.GRILLED_LAMB_SKEWER, true), skewerProps());
    public static final Item GRILLED_RABBIT_SKEWER = register("grilled_rabbit_skewer",
            props -> grilledSkewer(props, BBQDFoods.GRILLED_RABBIT_SKEWER, true), skewerProps());
    public static final Item GRILLED_PORK_SAUSAGE_SKEWER = register("grilled_pork_sausage_skewer",
            props -> grilledSkewer(props, BBQDFoods.GRILLED_PORK_SAUSAGE_SKEWER, true), skewerProps());
    public static final Item GRILLED_POTATO_SKEWER = register("grilled_potato_skewer",
            props -> grilledSkewer(props, BBQDFoods.GRILLED_POTATO_SKEWER, true), skewerProps());
    public static final Item GRILLED_FRUIT_AND_VEGETABLE_SKEWER = register("grilled_fruit_and_vegetable_skewer",
            props -> grilledSkewer(props, BBQDFoods.GRILLED_FRUIT_AND_VEGETABLE_SKEWER, true), skewerProps());

    /**
     * Properties for a {@link BlockItem}, so that the item resolves its translation key from the
     * block it places - i.e. {@code block.barbequesdelight.<id>} rather than
     * {@code item.barbequesdelight.<id>}.
     *
     * <p>26.1 resolves an item's translation key once, in the {@code Item} constructor, from a
     * {@code DependantName} that defaults to the {@code item.} prefix. {@link BlockItem}'s own
     * constructor does <em>not</em> switch that prefix any more, so this must be requested
     * explicitly. Vanilla does exactly the same thing in {@code Items.registerBlock}, which passes
     * {@code Properties.useBlockDescriptionPrefix()} into its item registration. Without this call
     * the block items render as their raw key (e.g. {@code item.barbequesdelight.grill}) in the
     * creative tab and in inventory tooltips, while Jade keeps working because it reads the
     * <em>block's</em> name ({@code block.barbequesdelight.grill}) instead.
     */
    private static Item.Properties blockItemProps() {
        return new Item.Properties().useBlockDescriptionPrefix();
    }

    /** Food properties plus the matching consume effects. */
    private static Item.Properties foodProps(net.minecraft.world.food.FoodProperties food) {
        return new Item.Properties().food(food, BBQDFoods.consumableFor(food));
    }

    /**
     * Bowl food: stack size 16 and the empty bowl returned on eat, matching FD's
     * {@code bowlFoodItem} (which returns bare Properties and so cannot be used directly
     * here - the item id is not known until registration).
     */
    private static Item.Properties bowlFoodProps(net.minecraft.world.food.FoodProperties food) {
        Consumable consumable = BBQDFoods.consumableFor(food);
        return new Item.Properties()
                .food(food, consumable)
                .stacksTo(16)
                .usingConvertsTo(Items.BOWL);
    }

    private static Item.Properties skewerProps() {
        return new Item.Properties().craftRemainder(Items.STICK);
    }

    private static Item rawSkewer(Item.Properties props) {
        return new Item(props.craftRemainder(Items.STICK));
    }

    private static Item grilledSkewer(Item.Properties props, net.minecraft.world.food.FoodProperties food,
                                      boolean hasTooltip) {
        return new SimpleSkewerItem(props, food, hasTooltip);
    }

    private static Item register(String id, Function<Item.Properties, Item> factory, Item.Properties properties) {
        ResourceKey<Item> key = ResourceKey.create(Registries.ITEM, BarbequesDelight.asID(id));
        // The id must be attached before construction: ConsumableItem reads it in its ctor.
        Item item = factory.apply(properties.setId(key));
        Item registered = Registry.register(BuiltInRegistries.ITEM, key, item);
        TAB_ORDER.add(registered);
        return registered;
    }

    /** Fills the mod's creative tab, in registration order. */
    public static void addToTab(CreativeModeTab.Output output) {
        for (Item item : TAB_ORDER) {
            output.accept(new ItemStack(item));
        }
    }

    public static void registerBBQDItems() {
        BarbequesDelight.LOGGER.debug("Register BBQD Items For" + BarbequesDelight.MODID);
    }
}
