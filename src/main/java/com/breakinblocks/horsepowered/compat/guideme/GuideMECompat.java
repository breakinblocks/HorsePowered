package com.breakinblocks.horsepowered.compat.guideme;

import com.breakinblocks.horsepowered.HorsePowerMod;
import guideme.Guides;
import net.minecraft.world.item.ItemStack;

/**
 * Compatibility class for GuideME integration.
 * This class is only loaded when GuideME is present.
 */
public class GuideMECompat {

    /**
     * Creates a guide item for the Horse Powered guide.
     * @return The guide ItemStack
     */
    public static ItemStack createGuideItem() {
        return Guides.createGuideItem(HorsePowerMod.id("guide"));
    }
}
