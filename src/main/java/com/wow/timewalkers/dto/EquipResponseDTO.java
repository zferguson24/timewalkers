package com.wow.timewalkers.dto;

import com.wow.timewalkers.enums.EquipmentSlot;

import java.util.List;

// Returned by PATCH /api/characters/{name}/gear on success (200).
// 'equipped'  — slots that were successfully equipped in this request
// 'notFound'  — slots whose requested item name had no match in the database
// The full character loadout (all 16 slots) is always included in 'character'.
public record EquipResponseDTO(
        CharacterDTO character,
        List<EquipmentSlot> equipped,
        List<EquipmentSlot> notFound
) {}
