package com.wow.timewalkers.dto;

import com.wow.timewalkers.enums.EquipmentSlot;

// One requested equip entry whose item name had no match in the database.
// Included in EquipResponseDTO so the client can tell the user which items
// were silently skipped instead of a partial apply looking like a full one.
public record NotFoundSlotDTO(EquipmentSlot slot, String itemName) {}
