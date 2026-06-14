package com.wow.timewalkers.enums;

public enum EquipmentSlot {
    HEAD(ItemType.ARMOR, false),
    SHOULDERS(ItemType.ARMOR, false),
    CHEST(ItemType.ARMOR, false),
    WRIST(ItemType.ARMOR, false),
    HANDS(ItemType.ARMOR, false),
    WAIST(ItemType.ARMOR, false),
    LEGS(ItemType.ARMOR, false),
    FEET(ItemType.ARMOR, false),
    NECK(ItemType.ARMOR, true),
    BACK(ItemType.ARMOR, true),
    FINGER_1(ItemType.ARMOR, true),
    FINGER_2(ItemType.ARMOR, true),
    TRINKET_1(ItemType.ARMOR, true),
    TRINKET_2(ItemType.ARMOR, true),
    MAIN_HAND(ItemType.WEAPON, false),
    OFF_HAND(ItemType.WEAPON, false);

    private final ItemType itemType;
    private final boolean agnostic;

    EquipmentSlot(ItemType itemType, boolean agnostic) {
        this.itemType = itemType;
        this.agnostic = agnostic;
    }

    public ItemType getItemType() { return itemType; }
    public boolean isAgnostic() { return agnostic; }
}
