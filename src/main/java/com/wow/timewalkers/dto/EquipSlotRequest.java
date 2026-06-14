package com.wow.timewalkers.dto;

import com.wow.timewalkers.enums.EquipmentSlot;

// One entry in an equip request — identifies which slot to fill and what item to put in it.
// itemName must match an item name in the database exactly (case-insensitive).
public record EquipSlotRequest(EquipmentSlot slot, String itemName) {}
