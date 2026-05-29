package com.dmmc;

public enum UpgradeType {
    // Armor upgrades
    DEFENSE,
    HEALTH,
    ARMOR_SHRED,
    HEALING_RECEIVED,
    FALL_FLYING,
    DODGE_CHANCE,
    OVERHEAL,
    // Weapon upgrades
    ATTACK,
    CRIT_CHANCE,
    LIFESTEAL,
    CURRENT_HP_DAMAGE,
    CRIT_DAMAGE,
    MOB_LOOTING,
    // Tool upgrades
    MINING_EFFICIENCY,
    BLOCK_INTERACTION_RANGE,
    MINING_FORTUNE,
    FISHING_LUCK,
    FISHING_SPEED;

    public boolean isArmorUpgrade() {
        return this == DEFENSE || this == HEALTH || this == ARMOR_SHRED || this == HEALING_RECEIVED || this == FALL_FLYING || this == OVERHEAL || this == DODGE_CHANCE;
    }

    public boolean isWeaponUpgrade() {
        return this == ATTACK || this == CRIT_CHANCE || this == LIFESTEAL || this == CRIT_DAMAGE || this == CURRENT_HP_DAMAGE || this == MOB_LOOTING;
    }

    public boolean isToolUpgrade() {
        return this == MINING_EFFICIENCY || this == BLOCK_INTERACTION_RANGE || this == MINING_FORTUNE;
    }

    public boolean isFishingRodUpgrade() {
        return this == FISHING_LUCK || this == FISHING_SPEED;
    }

    public boolean requiresApotheosis() {
        return this == ARMOR_SHRED || this == CRIT_CHANCE || this == LIFESTEAL|| this == HEALING_RECEIVED || this == CRIT_DAMAGE || this == CURRENT_HP_DAMAGE || this == OVERHEAL || this == DODGE_CHANCE;
    }

    public boolean requiresCaelus() {
        return  this == FALL_FLYING;
    }

    public boolean requiresApothicAttributesExtension() {
        return this == MINING_FORTUNE || this == FISHING_LUCK || this == FISHING_SPEED || this == MOB_LOOTING;
    }
}
