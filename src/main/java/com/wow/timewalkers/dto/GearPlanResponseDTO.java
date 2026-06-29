package com.wow.timewalkers.dto;

import java.time.LocalDate;
import java.util.List;

public record GearPlanResponseDTO(
        String characterName,
        String resolvedStat,
        boolean fullyEquipped,
        LocalDate fullyEquippedDate,
        List<String> alreadyEquippedSlots,
        List<String> unresolvableSlots,
        List<GearPlanEventDTO> events,
        List<String> statOptions
) {}
