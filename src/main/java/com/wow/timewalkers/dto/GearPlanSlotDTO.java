package com.wow.timewalkers.dto;

public record GearPlanSlotDTO(
        String slot,
        String itemName,
        String iconUrl,
        int cost
) {}