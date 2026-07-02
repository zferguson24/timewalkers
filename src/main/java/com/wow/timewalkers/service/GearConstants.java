package com.wow.timewalkers.service;

// Central definitions for the domain strings stored in the gear tables.
// These values must match the armor_pieces / weapons column contents exactly
// (weapon_slot, weapon_type, armor_type, primary_stat, notes conventions).
// Always reference these constants instead of inline literals so a typo is a
// compile error rather than a silently-failing string comparison.
public final class GearConstants {

    private GearConstants() {}

    // weapons.weapon_slot values
    public static final String SLOT_1H = "1H";
    public static final String SLOT_2H = "2H";
    public static final String SLOT_OFF_HAND = "Off-Hand";
    public static final String SLOT_RANGED = "Ranged";

    // armor_pieces.armor_type values
    public static final String ARMOR_PLATE = "Plate";
    public static final String ARMOR_MAIL = "Mail";
    public static final String ARMOR_LEATHER = "Leather";
    public static final String ARMOR_CLOTH = "Cloth";
    // Agnostic = rings/trinkets/neck/cloak, wearable by any class
    public static final String ARMOR_AGNOSTIC = "Agnostic";

    // weapons.weapon_type values
    public static final String TYPE_AXE = "Axe";
    public static final String TYPE_BOW = "Bow";
    public static final String TYPE_CROSSBOW = "Crossbow";
    public static final String TYPE_DAGGER = "Dagger";
    public static final String TYPE_FIST_WEAPON = "Fist Weapon";
    public static final String TYPE_GUN = "Gun";
    public static final String TYPE_HELD_IN_OFF_HAND = "Held In Off-hand";
    public static final String TYPE_MACE = "Mace";
    public static final String TYPE_POLEARM = "Polearm";
    public static final String TYPE_SHIELD = "Shield";
    public static final String TYPE_STAFF = "Staff";
    public static final String TYPE_SWORD = "Sword";
    public static final String TYPE_WAND = "Wand";
    public static final String TYPE_WARGLAIVE = "Warglaive";

    // Primary stat names as they appear in primary_stat / weapon_stat text
    public static final String STAT_STRENGTH = "Strength";
    public static final String STAT_AGILITY = "Agility";
    public static final String STAT_INTELLECT = "Intellect";

    // Marker phrase in armor_pieces.notes for items with no primary stat
    // (pure secondary-stat items, compatible with any class)
    public static final String NOTE_NO_PRIMARY_STAT = "No Primary Stat";
}
