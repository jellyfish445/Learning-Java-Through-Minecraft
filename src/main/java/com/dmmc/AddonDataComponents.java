package com.dmmc;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.core.component.DataComponentType;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.codec.ByteBufCodecs;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;

import java.util.List;

public class AddonDataComponents {

    public static final DeferredRegister<DataComponentType<?>> DATA_COMPONENTS =
            DeferredRegister.create(Registries.DATA_COMPONENT_TYPE, DmmcMod.MOD_ID);

    // -------------------------------------------------------
    // UpgradeEntry — stored in the applied_upgrades list on gear
    // -------------------------------------------------------

    public record UpgradeEntry(UpgradeType upgradeType, int tier) {
        public static final Codec<UpgradeEntry> CODEC = RecordCodecBuilder.create(inst -> inst.group(
                Codec.STRING
                        .xmap(UpgradeType::valueOf, UpgradeType::name)
                        .fieldOf("upgrade_type")
                        .forGetter(UpgradeEntry::upgradeType),
                Codec.intRange(1, 8)
                        .fieldOf("tier")
                        .forGetter(UpgradeEntry::tier)
        ).apply(inst, UpgradeEntry::new));
    }

    // -------------------------------------------------------
    // Components on gear items
    // -------------------------------------------------------

    /** How many upgrade slots have been added to this item (0–2). */
    public static final DeferredHolder<DataComponentType<?>, DataComponentType<Integer>> UPGRADE_SLOTS =
            DATA_COMPONENTS.register("upgrade_slots", () ->
                    DataComponentType.<Integer>builder()
                            .persistent(Codec.intRange(0, 2))
                            .networkSynchronized(ByteBufCodecs.INT)
                            .build()
            );

    /** List of upgrades socketed into this item. */
    public static final DeferredHolder<DataComponentType<?>, DataComponentType<List<UpgradeEntry>>> APPLIED_UPGRADES =
            DATA_COMPONENTS.register("applied_upgrades", () ->
                    DataComponentType.<List<UpgradeEntry>>builder()
                            .persistent(UpgradeEntry.CODEC.listOf())
                            .networkSynchronized(ByteBufCodecs.fromCodec(UpgradeEntry.CODEC.listOf()))
                            .build()
            );

    // -------------------------------------------------------
    // Components on boss_upgrade item
    // -------------------------------------------------------

    /** Which stat this upgrade improves. */
    public static final DeferredHolder<DataComponentType<?>, DataComponentType<UpgradeType>> UPGRADE_TYPE =
            DATA_COMPONENTS.register("upgrade_type", () ->
                    DataComponentType.<UpgradeType>builder()
                            .persistent(Codec.STRING.xmap(UpgradeType::valueOf, UpgradeType::name))
                            .networkSynchronized(ByteBufCodecs.fromCodec(
                                    Codec.STRING.xmap(UpgradeType::valueOf, UpgradeType::name)))
                            .build()
            );

    /** Tier 1–8 from which boss was killed. */
    public static final DeferredHolder<DataComponentType<?>, DataComponentType<Integer>> UPGRADE_TIER =
            DATA_COMPONENTS.register("upgrade_tier", () ->
                    DataComponentType.<Integer>builder()
                            .persistent(Codec.intRange(1, 8))
                            .networkSynchronized(ByteBufCodecs.INT)
                            .build()
            );
}
