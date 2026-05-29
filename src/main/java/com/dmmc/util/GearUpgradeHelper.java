package com.dmmc.util;

import com.dmmc.AddonDataComponents;
import com.dmmc.AddonDataComponents.UpgradeEntry;
import com.dmmc.DmmcMod;
import com.dmmc.UpgradeType;
import net.minecraft.core.Holder;
import net.minecraft.core.component.DataComponents;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.EquipmentSlotGroup;
import net.minecraft.world.entity.ai.attributes.Attribute;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.*;
import net.minecraft.world.item.component.ItemAttributeModifiers;
import net.neoforged.fml.ModList;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class GearUpgradeHelper {

    private static final int MAX_SLOTS = 2;

    // -------------------------------------------------------
    // Slot management
    // -------------------------------------------------------

    public static int getSlots(ItemStack stack) {
        return stack.getOrDefault(AddonDataComponents.UPGRADE_SLOTS.get(), 0);
    }

    public static boolean addSlot(ItemStack stack) {
        int current = getSlots(stack);
        if (current >= MAX_SLOTS) return false;
        stack.set(AddonDataComponents.UPGRADE_SLOTS.get(), current + 1);
        return true;
    }

    public static List<UpgradeEntry> getUpgrades(ItemStack stack) {
        return stack.getOrDefault(AddonDataComponents.APPLIED_UPGRADES.get(), List.of());
    }

    public static boolean hasOpenSlot(ItemStack stack) {
        return getUpgrades(stack).size() < getSlots(stack);
    }

    // -------------------------------------------------------
    // Applying upgrades
    // -------------------------------------------------------

    /**
     * Reads boss_upgrade components off upgradeItem, validates gear has an open slot,
     * applies the attribute modifier, returns the modified gear stack.
     * Returns empty if anything is invalid.
     */

    public static Optional<ItemStack> applyUpgrade(ItemStack gear, ItemStack upgradeItem) {
        UpgradeType type = upgradeItem.get(AddonDataComponents.UPGRADE_TYPE.get());
        Integer tier     = upgradeItem.get(AddonDataComponents.UPGRADE_TIER.get());

        if (type == null || tier == null) return Optional.empty();
        if (!hasOpenSlot(gear))           return Optional.empty();

        // Validate upgrade type matches gear category
        if (type.isArmorUpgrade()  && !isArmor(gear))  return Optional.empty();
        if (type.isWeaponUpgrade() && !isWeapon(gear)) return Optional.empty();
        if (type.isToolUpgrade() && !isTool(gear)) return Optional.empty();
        if (type.isFishingRodUpgrade() && !isFishingRod(gear)) return Optional.empty();

        // Apotheosis soft-dep guard
        if (type.requiresApotheosis() && !ModList.get().isLoaded("apothic_attributes")) {
            return Optional.empty();
        }

        if (type.requiresCaelus() && !ModList.get().isLoaded("caelus")) {
            return Optional.empty();
        }

        if (type.requiresApothicAttributesExtension() && !ModList.get().isLoaded("apothic_attributes_extension")){
            return Optional.empty();
        }

        Optional<Holder<Attribute>> attrHolder = resolveAttribute(type);
        if (attrHolder.isEmpty()) return Optional.empty();

        // Unique modifier ID per slot index so two upgrades on one item don't collide
        int slotIndex = getUpgrades(gear).size();
        ResourceLocation modId = ResourceLocation.fromNamespaceAndPath(DmmcMod.MOD_ID,
                type.name().toLowerCase() + "_" + slotIndex);

        double bonus = calculateBonus(type, tier);
        AttributeModifier modifier = new AttributeModifier(modId, bonus,
                AttributeModifier.Operation.ADD_VALUE);

        ItemStack result = gear.copy();

        ItemAttributeModifiers existingMods = result.getOrDefault(
                DataComponents.ATTRIBUTE_MODIFIERS, ItemAttributeModifiers.EMPTY);

// Build a new entry and append it
        ItemAttributeModifiers.Entry entry = new ItemAttributeModifiers.Entry(
                attrHolder.get(),
                modifier,
                EquipmentSlotGroup.ANY);

        List<ItemAttributeModifiers.Entry> entries = new ArrayList<>(existingMods.modifiers());
        entries.add(entry);

        result.set(DataComponents.ATTRIBUTE_MODIFIERS,
                new ItemAttributeModifiers(entries, existingMods.showInTooltip()));
        return Optional.of(result);
    }

    // -------------------------------------------------------
    // Attribute resolution
    // -------------------------------------------------------

    private static Optional<Holder<Attribute>> resolveAttribute(UpgradeType type) {
        return switch (type) {
            case DEFENSE     -> Optional.of(Attributes.ARMOR);
            case HEALTH      -> Optional.of(Attributes.MAX_HEALTH);
            case DODGE_CHANCE -> getApothAttribute("DODGE_CHANCE");
            case OVERHEAL -> getApothAttribute("OVERHEAL");
            case ATTACK      -> Optional.of(Attributes.ATTACK_DAMAGE);
            case ARMOR_SHRED -> getApothAttribute("ARMOR_SHRED");
            case CRIT_CHANCE -> getApothAttribute("CRIT_CHANCE");
            case LIFESTEAL   -> getApothAttribute("LIFE_STEAL");
            case HEALING_RECEIVED    -> getApothAttribute("HEALING_RECEIVED");
            case CURRENT_HP_DAMAGE -> getApothAttribute("CURRENT_HP_DAMAGE");
            case CRIT_DAMAGE -> getApothAttribute("CRIT_DAMAGE");
            case FALL_FLYING -> getCaelusAttribute();
            case MOB_LOOTING -> getApothicAttributesExtensionAttribute("MOB_LOOTING");
            case FISHING_SPEED -> getApothicAttributesExtensionAttribute("FISHING_SPEED");
            case MINING_EFFICIENCY -> Optional.of(Attributes.MINING_EFFICIENCY);
            case FISHING_LUCK -> getApothicAttributesExtensionAttribute("FISHING_LUCK");
            case MINING_FORTUNE -> getApothicAttributesExtensionAttribute("MINING_FORTUNE");
            case BLOCK_INTERACTION_RANGE -> Optional.of(Attributes.BLOCK_INTERACTION_RANGE);
        };
    }

    @SuppressWarnings("unchecked")
    private static Optional<Holder<Attribute>> getApothAttribute(String fieldName) {
        try {
            Class<?> alAttrs = Class.forName(
                    "dev.shadowsoffire.apothic_attributes.api.ALObjects$Attributes");
            var field = alAttrs.getField(fieldName);
            return Optional.of((Holder<Attribute>) field.get(null));
        } catch (Exception e) {
            DmmcMod.LOGGER.warn("Could not resolve Apothic Attribute field '{}': {}", fieldName, e.getMessage());
            return Optional.empty();
        }
    }
    @SuppressWarnings("unchecked")
    private static Optional<Holder<Attribute>> getCaelusAttribute() {
        try {
            Class<?> apiClass = Class.forName("com.illusivesoulworks.caelus.api.CaelusApi");
            Object instance = apiClass.getMethod("getInstance").invoke(null);
            Holder<Attribute> attr = (Holder<Attribute>) apiClass.getMethod("getFallFlyingAttribute").invoke(instance);
            return Optional.of(attr);
        } catch (Exception e) {
            DmmcMod.LOGGER.warn("Could not resolve Caelus fall flying attribute: {}", e.getMessage());
            return Optional.empty();
        }
    }

    @SuppressWarnings("unchecked")
    private static Optional<Holder<Attribute>> getApothicAttributesExtensionAttribute(String fieldName) {
        try {
            Class<?> alAttrs = Class.forName("com.chen1335.apothicAttributesExtensionAPI.objects.ModAttributes$Attributes");
            var field = alAttrs.getField(fieldName);
            return Optional.of((Holder<Attribute>) field.get(null));
        } catch (Exception e) {
            DmmcMod.LOGGER.warn("Could not resolve AAE Attribute Field: '{}': '{}'", fieldName, e.getMessage());
            return Optional.empty();
        }
    }

    // Stat scaling

    public static double calculateBonus(UpgradeType type, int tier) {
        return switch (type) {
            case DEFENSE     -> 0.5  + 0.5  * (tier - 1); // +0.5 → +4.0 armor
            case HEALTH      -> 1.0  + 1.0  * (tier - 1); // +1.0 → +8.0 max hp
            case ATTACK      -> 0.5  + 0.5  * (tier - 1); // +0.5 → +4.0 damage
            case CRIT_CHANCE -> 0.02 + 0.02 * (tier - 1); // +2% → +16% crit
            case LIFESTEAL   -> 0.03 + 0.03 * (tier - 1); // +3% → +24% lifesteal
            case ARMOR_SHRED -> 0.02 + 0.02 * (tier - 1); // +2% → +16% shred
            case HEALING_RECEIVED -> 1.0 + 0.5 * (tier - 1);
            case FALL_FLYING -> 1.0;
            case CRIT_DAMAGE -> 0.05 + 0.05 * (tier - 1); // +5% → +40% extra crit damage
            case DODGE_CHANCE -> 0.02 + 0.02 * (tier - 1); // +2% → +16% dodge
            case OVERHEAL -> 0.03 + 0.02 * (tier - 1); // +3% → +17% overheal
            case CURRENT_HP_DAMAGE -> 0.01 + 0.01 * (tier - 1); // +1% → +8% current HP damage
            case MINING_FORTUNE, FISHING_LUCK, MOB_LOOTING -> 2 * (tier - 1);
            case BLOCK_INTERACTION_RANGE -> 1 + 0.5 * (tier - 1);
            case MINING_EFFICIENCY, FISHING_SPEED -> 0.5 * (tier);
        };
    }

    // -------------------------------------------------------
    // Item category helpers
    // -------------------------------------------------------

    public static boolean isArmor(ItemStack stack) {
        return stack.getItem() instanceof ArmorItem;
    }

    public static boolean isWeapon(ItemStack stack) {
        return stack.getItem() instanceof SwordItem
                || stack.getItem() instanceof AxeItem;
    }

    public static boolean isTool(ItemStack stack) {
        return stack.getItem() instanceof PickaxeItem
                || stack.getItem() instanceof AxeItem
                || stack.getItem() instanceof ShovelItem;
    }

    public static boolean isFishingRod(ItemStack stack) {
        return stack.getItem() instanceof FishingRodItem;
    }
    // -------------------------------------------------------
    // Particles
    // -------------------------------------------------------

    public static void spawnSlotParticles(ServerLevel level, Player player) {
        level.sendParticles(ParticleTypes.ENCHANT,
                player.getX(), player.getY() + 1.0, player.getZ(),
                40, 0.5, 0.5, 0.5, 0.3);
    }

    public static void spawnUpgradeParticles(ServerLevel level, Player player, UpgradeType type) {
        var particle = type.isArmorUpgrade() ? ParticleTypes.END_ROD : ParticleTypes.CRIT;
        level.sendParticles(particle,
                player.getX(), player.getY() + 1.0, player.getZ(),
                30, 0.4, 0.4, 0.4, 0.2);
        level.sendParticles(ParticleTypes.TOTEM_OF_UNDYING,
                player.getX(), player.getY() + 1.0, player.getZ(),
                15, 0.3, 0.3, 0.3, 0.1);
    }
}
