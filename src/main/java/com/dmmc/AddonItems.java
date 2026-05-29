package com.dmmc;

import net.minecraft.world.item.Item;
import net.neoforged.neoforge.registries.DeferredItem;
import net.neoforged.neoforge.registries.DeferredRegister;

public class AddonItems {

    public static final DeferredRegister.Items ITEMS =
            DeferredRegister.createItems(DmmcMod.MOD_ID);

    /** Dropped by DivineRPG bosses. Variants via UPGRADE_TYPE + UPGRADE_TIER components. */
    public static final DeferredItem<Item> BOSS_UPGRADE =
            ITEMS.registerSimpleItem("boss_upgrade",
                    new Item.Properties().stacksTo(1));

    /** Right-click onto armor to add 1 upgrade slot (max 2). */
    public static final DeferredItem<Item> ARMOR_SLOT_TOKEN =
            ITEMS.registerSimpleItem("armor_slot_token",
                    new Item.Properties().stacksTo(16));

    /** Right-click onto a weapon to add 1 upgrade slot (max 2). */
    public static final DeferredItem<Item> WEAPON_SLOT_TOKEN =
            ITEMS.registerSimpleItem("weapon_slot_token",
                    new Item.Properties().stacksTo(16));

    /** Right-click onto fishing rod to add 1 upgrade slot (max 2). */
    public static final DeferredItem<Item> FISHING_ROD_SLOT_TOKEN =
            ITEMS.registerSimpleItem("fishing_rod_slot_token",
                    new Item.Properties().stacksTo(16));

    /** Right-click onto a tool to add 1 upgrade slot (max 2). */
    public static final DeferredItem<Item> TOOL_SLOT_TOKEN =
            ITEMS.registerSimpleItem("tool_slot_token",
                    new Item.Properties().stacksTo(16));

    /**
     * Output of the apply_upgrade Occultism ritual.
     * Right-click onto gear while a boss_upgrade is in inventory
     * to apply the upgrade to that gear piece.
     */
    public static final DeferredItem<Item> APPLY_UPGRADE_TOKEN =
            ITEMS.registerSimpleItem("apply_upgrade_token",
                    new Item.Properties().stacksTo(16));
}
