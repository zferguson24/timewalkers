package com.wow.timewalkers.service;

import com.wow.timewalkers.entity.Weapon;
import com.wow.timewalkers.enums.WowClass;
import org.springframework.stereotype.Component;

import java.util.EnumMap;
import java.util.Map;
import java.util.Set;

// @Component is the generic Spring stereotype annotation — it marks a class for
// component scanning so Spring will instantiate and manage it as a bean.
// @Service and @Repository are specializations of @Component with added meaning.
// GearValidator is pure logic with no persistence, so @Component fits best.
@Component
public class GearValidator {

    // Static maps initialized once at class-load time — no need to rebuild on
    // every request. EnumMap is more efficient than HashMap when keys are enums.
    private static final Map<WowClass, String> ARMOR_TYPE_BY_CLASS = new EnumMap<>(WowClass.class);
    private static final Map<WowClass, Set<String>> WEAPON_TYPES_BY_CLASS = new EnumMap<>(WowClass.class);
    private static final Set<WowClass> DUAL_WIELD_CLASSES;
    private static final Set<WowClass> SHIELD_CLASSES;
    // Classes that can equip Held In Off-hand frills (tomes, orbs, etc.)
    private static final Set<WowClass> OFFHAND_FRILL_CLASSES;
    // Classes that cannot equip any two-handed weapon regardless of type
    private static final Set<WowClass> TWO_HANDED_FORBIDDEN_CLASSES;

    static {
        // Armor restrictions — each class is locked to one armor type
        ARMOR_TYPE_BY_CLASS.put(WowClass.MAGE,         "Cloth");
        ARMOR_TYPE_BY_CLASS.put(WowClass.PRIEST,       "Cloth");
        ARMOR_TYPE_BY_CLASS.put(WowClass.WARLOCK,      "Cloth");
        ARMOR_TYPE_BY_CLASS.put(WowClass.DEMON_HUNTER, "Leather");
        ARMOR_TYPE_BY_CLASS.put(WowClass.DRUID,        "Leather");
        ARMOR_TYPE_BY_CLASS.put(WowClass.MONK,         "Leather");
        ARMOR_TYPE_BY_CLASS.put(WowClass.ROGUE,        "Leather");
        ARMOR_TYPE_BY_CLASS.put(WowClass.EVOKER,       "Mail");
        ARMOR_TYPE_BY_CLASS.put(WowClass.HUNTER,       "Mail");
        ARMOR_TYPE_BY_CLASS.put(WowClass.SHAMAN,       "Mail");
        ARMOR_TYPE_BY_CLASS.put(WowClass.DEATH_KNIGHT, "Plate");
        ARMOR_TYPE_BY_CLASS.put(WowClass.PALADIN,      "Plate");
        ARMOR_TYPE_BY_CLASS.put(WowClass.WARRIOR,      "Plate");

        // Weapon type strings match the weapon_type column values in the DB exactly
        WEAPON_TYPES_BY_CLASS.put(WowClass.DEATH_KNIGHT, Set.of("Sword", "Axe", "Mace", "Polearm"));
        WEAPON_TYPES_BY_CLASS.put(WowClass.DEMON_HUNTER, Set.of("Axe", "Sword", "Fist Weapon", "Warglaive"));
        WEAPON_TYPES_BY_CLASS.put(WowClass.DRUID,        Set.of("Dagger", "Mace", "Polearm", "Staff", "Fist Weapon"));
        WEAPON_TYPES_BY_CLASS.put(WowClass.EVOKER,       Set.of("Axe", "Dagger", "Fist Weapon", "Mace", "Staff", "Sword"));
        WEAPON_TYPES_BY_CLASS.put(WowClass.HUNTER,       Set.of("Axe", "Bow", "Crossbow", "Dagger", "Fist Weapon", "Gun", "Polearm", "Staff", "Sword"));
        WEAPON_TYPES_BY_CLASS.put(WowClass.MAGE,         Set.of("Dagger", "Sword", "Staff", "Wand"));
        WEAPON_TYPES_BY_CLASS.put(WowClass.MONK,         Set.of("Axe", "Dagger", "Fist Weapon", "Mace", "Polearm", "Staff", "Sword"));
        WEAPON_TYPES_BY_CLASS.put(WowClass.PALADIN,      Set.of("Axe", "Mace", "Polearm", "Sword", "Shield"));
        WEAPON_TYPES_BY_CLASS.put(WowClass.PRIEST,       Set.of("Dagger", "Mace", "Staff", "Wand"));
        WEAPON_TYPES_BY_CLASS.put(WowClass.ROGUE,        Set.of("Axe", "Dagger", "Fist Weapon", "Mace", "Sword"));
        WEAPON_TYPES_BY_CLASS.put(WowClass.SHAMAN,       Set.of("Axe", "Dagger", "Fist Weapon", "Mace", "Polearm", "Staff", "Shield"));
        WEAPON_TYPES_BY_CLASS.put(WowClass.WARLOCK,      Set.of("Dagger", "Staff", "Sword", "Wand"));
        WEAPON_TYPES_BY_CLASS.put(WowClass.WARRIOR,      Set.of("Axe", "Mace", "Polearm", "Sword", "Shield"));

        DUAL_WIELD_CLASSES = Set.of(
                WowClass.DEATH_KNIGHT, WowClass.DEMON_HUNTER, WowClass.MONK,
                WowClass.ROGUE, WowClass.SHAMAN, WowClass.WARRIOR);

        SHIELD_CLASSES = Set.of(WowClass.PALADIN, WowClass.SHAMAN, WowClass.WARRIOR);

        OFFHAND_FRILL_CLASSES = Set.of(
                WowClass.DRUID, WowClass.EVOKER, WowClass.MAGE, WowClass.PRIEST, WowClass.WARLOCK);

        TWO_HANDED_FORBIDDEN_CLASSES = Set.of(WowClass.DEMON_HUNTER, WowClass.ROGUE);
    }

    public String getArmorType(WowClass wowClass) {
        return ARMOR_TYPE_BY_CLASS.get(wowClass);
    }

    // "Agnostic" items (rings, trinkets, neck, cloak) can be worn by any class
    public boolean isArmorTypeAllowed(WowClass wowClass, String armorType) {
        if ("Agnostic".equals(armorType)) return true;
        return armorType.equals(ARMOR_TYPE_BY_CLASS.get(wowClass));
    }

    public boolean canDualWield(WowClass wowClass) {
        return DUAL_WIELD_CLASSES.contains(wowClass);
    }

    public boolean canUseShield(WowClass wowClass) {
        return SHIELD_CLASSES.contains(wowClass);
    }

    // weapon_slot = '2H' or 'Ranged' — both occupy both weapon slots
    public boolean isTwoHandedOrRanged(Weapon weapon) {
        return "2H".equals(weapon.getWeaponSlot()) || "Ranged".equals(weapon.getWeaponSlot());
    }

    // Returns null if the weapon is valid for MAIN_HAND, or an error message if not.
    // Returning a string (rather than throwing) lets the caller collect all violations
    // before deciding whether to reject the full request.
    public String validateForMainHand(WowClass wowClass, Weapon weapon) {
        String slot = weapon.getWeaponSlot();
        String type = weapon.getWeaponType();

        // Off-hand dedicated items (shields, frills) cannot be placed in main hand
        if ("Off-Hand".equals(slot)) {
            return type + " items can only be equipped in the off-hand slot";
        }

        // Ranged weapons (Bow, Gun, Crossbow) are treated like 2H and are Hunter-only
        if ("Ranged".equals(slot) && wowClass != WowClass.HUNTER) {
            return "Only Hunters can equip ranged weapons";
        }

        if ("2H".equals(slot) && TWO_HANDED_FORBIDDEN_CLASSES.contains(wowClass)) {
            return wowClass.name() + " cannot equip two-handed weapons";
        }

        if (!WEAPON_TYPES_BY_CLASS.get(wowClass).contains(type)) {
            return wowClass.name() + " cannot use " + type;
        }

        return null;
    }

    // Returns null if valid for OFF_HAND, or an error message if not.
    public String validateForOffHand(WowClass wowClass, Weapon weapon) {
        String slot = weapon.getWeaponSlot();
        String type = weapon.getWeaponType();

        // 2H and ranged weapons can never go in the off-hand
        if ("2H".equals(slot) || "Ranged".equals(slot)) {
            return "Two-handed and ranged weapons cannot be equipped in the off-hand slot";
        }

        if ("Off-Hand".equals(slot)) {
            if ("Shield".equals(type)) {
                return canUseShield(wowClass) ? null
                        : wowClass.name() + " cannot use shields";
            }
            // "Held In Off-hand" covers tomes, orbs, and similar caster off-hand items
            if ("Held In Off-hand".equals(type)) {
                return OFFHAND_FRILL_CLASSES.contains(wowClass) ? null
                        : wowClass.name() + " cannot use off-hand frill items";
            }
            return "Unknown off-hand item type: " + type;
        }

        // A 1H weapon in the off-hand requires dual wield eligibility
        if ("1H".equals(slot)) {
            if (!canDualWield(wowClass)) {
                return wowClass.name() + " cannot dual wield";
            }
            if (!WEAPON_TYPES_BY_CLASS.get(wowClass).contains(type)) {
                return wowClass.name() + " cannot use " + type;
            }
            return null;
        }

        return "Invalid weapon slot for off-hand: " + slot;
    }
}
