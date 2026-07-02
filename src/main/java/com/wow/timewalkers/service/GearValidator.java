package com.wow.timewalkers.service;

import com.wow.timewalkers.entity.Weapon;
import com.wow.timewalkers.enums.WowClass;
import org.springframework.stereotype.Component;

import java.util.EnumMap;
import java.util.Map;
import java.util.Set;

import static com.wow.timewalkers.service.GearConstants.*;

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
        ARMOR_TYPE_BY_CLASS.put(WowClass.MAGE,         ARMOR_CLOTH);
        ARMOR_TYPE_BY_CLASS.put(WowClass.PRIEST,       ARMOR_CLOTH);
        ARMOR_TYPE_BY_CLASS.put(WowClass.WARLOCK,      ARMOR_CLOTH);
        ARMOR_TYPE_BY_CLASS.put(WowClass.DEMON_HUNTER, ARMOR_LEATHER);
        ARMOR_TYPE_BY_CLASS.put(WowClass.DRUID,        ARMOR_LEATHER);
        ARMOR_TYPE_BY_CLASS.put(WowClass.MONK,         ARMOR_LEATHER);
        ARMOR_TYPE_BY_CLASS.put(WowClass.ROGUE,        ARMOR_LEATHER);
        ARMOR_TYPE_BY_CLASS.put(WowClass.EVOKER,       ARMOR_MAIL);
        ARMOR_TYPE_BY_CLASS.put(WowClass.HUNTER,       ARMOR_MAIL);
        ARMOR_TYPE_BY_CLASS.put(WowClass.SHAMAN,       ARMOR_MAIL);
        ARMOR_TYPE_BY_CLASS.put(WowClass.DEATH_KNIGHT, ARMOR_PLATE);
        ARMOR_TYPE_BY_CLASS.put(WowClass.PALADIN,      ARMOR_PLATE);
        ARMOR_TYPE_BY_CLASS.put(WowClass.WARRIOR,      ARMOR_PLATE);

        // Weapon type strings match the weapon_type column values in the DB exactly
        WEAPON_TYPES_BY_CLASS.put(WowClass.DEATH_KNIGHT, Set.of(TYPE_SWORD, TYPE_AXE, TYPE_MACE, TYPE_POLEARM));
        WEAPON_TYPES_BY_CLASS.put(WowClass.DEMON_HUNTER, Set.of(TYPE_AXE, TYPE_SWORD, TYPE_FIST_WEAPON, TYPE_WARGLAIVE));
        WEAPON_TYPES_BY_CLASS.put(WowClass.DRUID,        Set.of(TYPE_DAGGER, TYPE_MACE, TYPE_POLEARM, TYPE_STAFF, TYPE_FIST_WEAPON));
        WEAPON_TYPES_BY_CLASS.put(WowClass.EVOKER,       Set.of(TYPE_AXE, TYPE_DAGGER, TYPE_FIST_WEAPON, TYPE_MACE, TYPE_STAFF, TYPE_SWORD));
        WEAPON_TYPES_BY_CLASS.put(WowClass.HUNTER,       Set.of(TYPE_AXE, TYPE_BOW, TYPE_CROSSBOW, TYPE_DAGGER, TYPE_FIST_WEAPON, TYPE_GUN, TYPE_POLEARM, TYPE_STAFF, TYPE_SWORD));
        WEAPON_TYPES_BY_CLASS.put(WowClass.MAGE,         Set.of(TYPE_DAGGER, TYPE_SWORD, TYPE_STAFF, TYPE_WAND));
        WEAPON_TYPES_BY_CLASS.put(WowClass.MONK,         Set.of(TYPE_AXE, TYPE_DAGGER, TYPE_FIST_WEAPON, TYPE_MACE, TYPE_POLEARM, TYPE_STAFF, TYPE_SWORD));
        WEAPON_TYPES_BY_CLASS.put(WowClass.PALADIN,      Set.of(TYPE_AXE, TYPE_MACE, TYPE_POLEARM, TYPE_SWORD, TYPE_SHIELD));
        WEAPON_TYPES_BY_CLASS.put(WowClass.PRIEST,       Set.of(TYPE_DAGGER, TYPE_MACE, TYPE_STAFF, TYPE_WAND));
        WEAPON_TYPES_BY_CLASS.put(WowClass.ROGUE,        Set.of(TYPE_AXE, TYPE_DAGGER, TYPE_FIST_WEAPON, TYPE_MACE, TYPE_SWORD));
        WEAPON_TYPES_BY_CLASS.put(WowClass.SHAMAN,       Set.of(TYPE_AXE, TYPE_DAGGER, TYPE_FIST_WEAPON, TYPE_MACE, TYPE_POLEARM, TYPE_STAFF, TYPE_SHIELD));
        WEAPON_TYPES_BY_CLASS.put(WowClass.WARLOCK,      Set.of(TYPE_DAGGER, TYPE_STAFF, TYPE_SWORD, TYPE_WAND));
        WEAPON_TYPES_BY_CLASS.put(WowClass.WARRIOR,      Set.of(TYPE_AXE, TYPE_MACE, TYPE_POLEARM, TYPE_SWORD, TYPE_SHIELD));

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
        if (ARMOR_AGNOSTIC.equals(armorType)) return true;
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
        return SLOT_2H.equals(weapon.getWeaponSlot()) || SLOT_RANGED.equals(weapon.getWeaponSlot());
    }

    // DATA CONVENTION — stat compatibility is inferred from free text, not a real column.
    // The gear tables have no structured "which primary stat" field; the information lives
    // spread across primary_stat, secondary_stat, weapon_stat, and notes as prose
    // (e.g. "Agility", "Strength or Intellect", "No Primary Stat"). Until the schema grows
    // a dedicated stat column, this method is the single place that interprets that text:
    //   1. Blank/absent text          -> compatible (nothing says otherwise)
    //   2. Contains "No Primary Stat" -> compatible (explicitly stat-less item)
    //   3. No stat keyword at all     -> compatible (pure secondary-stat item)
    //   4. Otherwise                  -> compatible only if the text mentions requiredStat
    // Callers pass the concatenation of every stat-bearing text field for the item.
    public boolean hasCompatibleStatText(String statText, String requiredStat) {
        if (statText == null || statText.isBlank()) return true;
        if (statText.contains(NOTE_NO_PRIMARY_STAT)) return true;

        String lower = statText.toLowerCase();
        boolean hasAnyPrimaryStat = lower.contains(STAT_STRENGTH.toLowerCase())
                || lower.contains(STAT_AGILITY.toLowerCase())
                || lower.contains(STAT_INTELLECT.toLowerCase());
        if (!hasAnyPrimaryStat) return true;

        return lower.contains(requiredStat.toLowerCase());
    }

    // Returns null if the weapon is valid for MAIN_HAND, or an error message if not.
    // Returning a string (rather than throwing) lets the caller collect all violations
    // before deciding whether to reject the full request.
    public String validateForMainHand(WowClass wowClass, Weapon weapon) {
        String slot = weapon.getWeaponSlot();
        String type = weapon.getWeaponType();

        // Off-hand dedicated items (shields, frills) cannot be placed in main hand
        if (SLOT_OFF_HAND.equals(slot)) {
            return type + " items can only be equipped in the off-hand slot";
        }

        // Ranged weapons (Bow, Gun, Crossbow) are treated like 2H and are Hunter-only
        if (SLOT_RANGED.equals(slot) && wowClass != WowClass.HUNTER) {
            return "Only Hunters can equip ranged weapons";
        }

        if (SLOT_2H.equals(slot) && TWO_HANDED_FORBIDDEN_CLASSES.contains(wowClass)) {
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
        if (SLOT_2H.equals(slot) || SLOT_RANGED.equals(slot)) {
            return "Two-handed and ranged weapons cannot be equipped in the off-hand slot";
        }

        if (SLOT_OFF_HAND.equals(slot)) {
            if (TYPE_SHIELD.equals(type)) {
                return canUseShield(wowClass) ? null
                        : wowClass.name() + " cannot use shields";
            }
            // "Held In Off-hand" covers tomes, orbs, and similar caster off-hand items
            if (TYPE_HELD_IN_OFF_HAND.equals(type)) {
                return OFFHAND_FRILL_CLASSES.contains(wowClass) ? null
                        : wowClass.name() + " cannot use off-hand frill items";
            }
            return "Unknown off-hand item type: " + type;
        }

        // A 1H weapon in the off-hand requires dual wield eligibility
        if (SLOT_1H.equals(slot)) {
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
