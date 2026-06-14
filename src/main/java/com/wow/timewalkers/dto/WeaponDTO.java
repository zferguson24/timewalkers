package com.wow.timewalkers.dto;

// Record DTO for weapon responses — see ArmorPieceDTO for notes on records and DTOs.
public record WeaponDTO(
        String weaponSlot,
        String weaponStat,
        String weaponType,
        String name,
        String expansion,
        String primaryStat,
        String secondaryStat,
        Integer cost,
        String notes,
        String wowheadUrl,
        String iconUrl
) {}
