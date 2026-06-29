package com.wow.timewalkers.dto;

import java.time.LocalDate;
import java.util.List;

public record GearPlanEventDTO(
        String expansion,
        LocalDate startDate,
        LocalDate endDate,
        List<GearPlanSlotDTO> slots,
        int cumulativeSlotsFilled,
        boolean turbulentTimeways
) {}
