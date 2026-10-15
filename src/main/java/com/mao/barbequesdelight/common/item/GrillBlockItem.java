package com.mao.barbequesdelight.common.item;

import net.minecraft.world.item.BlockItem;
import net.minecraft.world.level.block.Block;

/**
 * The grill's item form.
 *
 * <p>The Forge original used this hook to override the placement position onto a lit campfire
 * ({@code BlockPlaceContext#replaceClicked}). That does not translate: in 26.1 the field is
 * {@code protected} and, more importantly, vanilla has already fixed the target position by the
 * time this runs, so forcing it only moved the grill one block up.</p>
 *
 * <p>The mount is therefore handled the ordinary way instead: the grill is placed normally on
 * top of the campfire, and {@code GrillBlock#getStateForPlacement} looks at the block below and
 * sets {@code has_campfire} when it finds a lit campfire there.</p>
 */
public class GrillBlockItem extends BlockItem {

    public GrillBlockItem(Block block, Properties properties) {
        super(block, properties);
    }
}
