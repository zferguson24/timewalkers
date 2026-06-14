package com.wow.timewalkers.dto;

import com.wow.timewalkers.enums.EquipmentSlot;

// Represents the state of a single equipment slot in the response.
// 'item' is typed as Object because it can be either an ArmorPieceDTO or a WeaponDTO.
// Jackson serializes the actual runtime type's fields, so the JSON will contain
// the correct fields for whichever type is present. 'item' is null when unequipped.
public record EquippedSlotDTO(EquipmentSlot slot, boolean equipped, Object item) {}
