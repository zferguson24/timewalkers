package com.wow.timewalkers.dto;

import com.wow.timewalkers.enums.WowClass;
import com.wow.timewalkers.enums.WowRace;

import java.util.List;

// Full character response — always includes all 16 equipment slots regardless of
// whether they are equipped. Each slot is represented by an EquippedSlotDTO.
public record CharacterDTO(
        String name,
        WowRace race,
        WowClass characterClass,
        List<EquippedSlotDTO> equipment
) {}
