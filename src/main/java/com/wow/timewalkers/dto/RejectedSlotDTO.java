package com.wow.timewalkers.dto;

import com.wow.timewalkers.enums.EquipmentSlot;

// Describes a single slot that failed validation, along with the reason why.
// Included in GearValidationException and returned in 400 error responses.
public record RejectedSlotDTO(EquipmentSlot slot, String reason) {}
