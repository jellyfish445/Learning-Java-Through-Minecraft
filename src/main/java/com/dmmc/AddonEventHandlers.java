package com.dmmc;

import com.dmmc.util.GearUpgradeHelper;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.Blocks;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.ModList;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.entity.living.LivingDeathEvent;
import net.neoforged.neoforge.event.entity.player.PlayerInteractEvent;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Random;

@EventBusSubscriber(modid = DmmcMod.MOD_ID)
public class AddonEventHandlers {

    private static final Map<String, Integer> BOSS_TIERS = Map.ofEntries(
            Map.entry("divinerpg:sunstorm",          1),
            Map.entry("minecraft:wither",             2),
            Map.entry("divinerpg:termasect",          2),
            Map.entry("divinerpg:eternal_archer",     3),
            Map.entry("divinerpg:experienced_cori",   4),
            Map.entry("minecraft:ender_dragon",       4),
            Map.entry("divinerpg:densos",             5),
            Map.entry("divinerpg:reyvor",             5),
            Map.entry("divinerpg:karot",              5),
            Map.entry("divinerpg:soul_fiend",         5),
            Map.entry("divinerpg:twilight_demon",     5),
            Map.entry("divinerpg:vamacheron",         5),
            Map.entry("divinerpg:rollum",             6),
            Map.entry("divinerpg:parasecta",          7),
            Map.entry("divinerpg:dramix",             7),
            Map.entry("divinerpg:hive_queen",         8),
            Map.entry("divinerpg:quadro",             8),
            Map.entry("divinerpg:karos",              8),
            Map.entry("divinerpg:raglok",             8),
            Map.entry("divinerpg:wreck",              8),
            Map.entry("divinerpg:lady_luna",          8)
    );


    private static List<UpgradeType> getArmorTypes() {
        var list = new ArrayList<>(List.of(UpgradeType.DEFENSE, UpgradeType.HEALTH));
        if (ModList.get().isLoaded("apothic_attributes")) list.add(UpgradeType.ARMOR_SHRED);
        if (ModList.get().isLoaded("apothic_attributes")) list.add(UpgradeType.HEALING_RECEIVED);
        if (ModList.get().isLoaded("apothic_attributes")) list.add(UpgradeType.OVERHEAL);
        if (ModList.get().isLoaded("apothic_attributes")) list.add(UpgradeType.DODGE_CHANCE);
        if (ModList.get().isLoaded("caelus")) list.add(UpgradeType.FALL_FLYING);

        return list;
    }

    private static List<UpgradeType> getToolTypes() {
        var list = new ArrayList<>(List.of(UpgradeType.MINING_EFFICIENCY, UpgradeType.BLOCK_INTERACTION_RANGE));
        if (ModList.get().isLoaded("apothic_attributes_extension")){
            list.add(UpgradeType.MINING_FORTUNE);
        }
        return list;
    }

    private static List<UpgradeType> getFishingRodTypes() {
        var list = new ArrayList<UpgradeType>();
        if (ModList.get().isLoaded("apothic_attributes_extension")) {
            list.add(UpgradeType.FISHING_LUCK);
            list.add(UpgradeType.FISHING_SPEED);
        }
        return list;
    }

    private static List<UpgradeType> getWeaponTypes() {
        var list = new ArrayList<>(List.of(UpgradeType.ATTACK));
        if (ModList.get().isLoaded("apothic_attributes")) {
            list.add(UpgradeType.CRIT_CHANCE);
            list.add(UpgradeType.LIFESTEAL);
            list.add(UpgradeType.CRIT_DAMAGE);
            list.add(UpgradeType.CURRENT_HP_DAMAGE);
        }
        if (ModList.get().isLoaded("apothic_attributes_extension")) {
            list.add(UpgradeType.MOB_LOOTING);
        }
        return list;
    }


    @SubscribeEvent
    public static void onBossDeath(LivingDeathEvent event) {
        if (!ModList.get().isLoaded("divinerpg")) return;
        if (event.getEntity().level().isClientSide()) return;

        LivingEntity dead = event.getEntity();
        String registryName = BuiltInRegistries.ENTITY_TYPE
                .getKey(dead.getType()).toString();

        Integer tier = BOSS_TIERS.get(registryName);
        if (tier == null) return;

        // Random upgrade type from combined armor + weapon pool
        List<UpgradeType> allTypes = new ArrayList<>();
        allTypes.addAll(getArmorTypes());
        allTypes.addAll(getWeaponTypes());
        allTypes.addAll(getFishingRodTypes());
        allTypes.addAll(getToolTypes());
        UpgradeType chosen = allTypes.get(new Random().nextInt(allTypes.size()));

        ItemStack drop = new ItemStack(AddonItems.BOSS_UPGRADE.get());
        drop.set(AddonDataComponents.UPGRADE_TYPE.get(), chosen);
        drop.set(AddonDataComponents.UPGRADE_TIER.get(), tier);

        dead.spawnAtLocation(drop);
        DmmcMod.LOGGER.info("Dropped {} tier-{} boss_upgrade from {}", chosen, tier, registryName);
    }

    // -------------------------------------------------------
    // Right-click slot tokens onto gear
    // -------------------------------------------------------

    @SubscribeEvent
    public static void onSmithingTableUpgrade(PlayerInteractEvent.RightClickBlock event) {
        Player player = event.getEntity();
        if (player.level().isClientSide()) return;
        if (event.getHand() != InteractionHand.MAIN_HAND) return; // <-- here, before anything else

        if (!player.level().getBlockState(event.getPos()).is(Blocks.SMITHING_TABLE)) return;

        ItemStack mainHand = player.getMainHandItem();
        ItemStack offHand  = player.getOffhandItem();

        // Offhand must be a boss_upgrade
        if (!offHand.is(AddonItems.BOSS_UPGRADE.get())) return;

        // Mainhand must be gear (armor or weapon)
        if (!GearUpgradeHelper.isArmor(mainHand)
                && !GearUpgradeHelper.isWeapon(mainHand)
                && !GearUpgradeHelper.isTool(mainHand)
                && !GearUpgradeHelper.isFishingRod(mainHand)) return;

        Optional<ItemStack> result = GearUpgradeHelper.applyUpgrade(mainHand, offHand);
        if (result.isEmpty()) {
            player.displayClientMessage(
                    Component.translatable("dmmc.upgrade_failed"), true);
            event.setCanceled(true); // still cancel so GUI doesn't open
            return;
        }

        UpgradeType type = offHand.get(AddonDataComponents.UPGRADE_TYPE.get());

        player.setItemInHand(InteractionHand.MAIN_HAND, result.get());
        offHand.shrink(1);
        event.setCanceled(true);

        ServerLevel level = (ServerLevel) player.level();
        GearUpgradeHelper.spawnSlotParticles(level, player);
        if (type != null) GearUpgradeHelper.spawnUpgradeParticles(level, player, type);

        player.displayClientMessage(
                Component.translatable("dmmc.upgrade_applied"), true);
    }

    // -------------------------------------------------------
    // Helpers
    // -------------------------------------------------------


    @SubscribeEvent
    public static void onRightClickSlotToken(PlayerInteractEvent.RightClickBlock event) {
        Player player = event.getEntity();
        if (player.level().isClientSide()) return;
        if (event.getHand() != InteractionHand.MAIN_HAND) return;

        if (!player.level().getBlockState(event.getPos()).is(Blocks.SMITHING_TABLE)) return;

        ItemStack mainHand = player.getMainHandItem();
        ItemStack offHand  = player.getOffhandItem();

        boolean isArmorToken  = offHand.is(AddonItems.ARMOR_SLOT_TOKEN.get());
        boolean isWeaponToken = offHand.is(AddonItems.WEAPON_SLOT_TOKEN.get());
        boolean isFishingRodToken = offHand.is(AddonItems.FISHING_ROD_SLOT_TOKEN.get());
        boolean isToolToken = offHand.is(AddonItems.TOOL_SLOT_TOKEN.get());
        if (!isArmorToken && !isWeaponToken && !isFishingRodToken && !isToolToken) return;

        // Validate the mainhand gear matches the token type
        if (isArmorToken  && !GearUpgradeHelper.isArmor(mainHand))  return;
        if (isWeaponToken && !GearUpgradeHelper.isWeapon(mainHand)) return;
        if (isToolToken && !GearUpgradeHelper.isTool(mainHand)) return;
        if (isFishingRodToken && !GearUpgradeHelper.isFishingRod(mainHand)) return;

        if (!GearUpgradeHelper.addSlot(mainHand)) {
            player.displayClientMessage(
                    Component.translatable("dmmc.slots_full"), true);
            event.setCanceled(true);
            return;
        }

        offHand.shrink(1);
        event.setCanceled(true);

        GearUpgradeHelper.spawnSlotParticles((ServerLevel) player.level(), player);
        player.displayClientMessage(
                Component.translatable("dmmc.slot_added"), true);
    }
}
