package com.wow.timewalkers.dto;

import java.util.List;

// Request body for PATCH /api/characters/{name}/gear.
// Only the slots listed here are modified; all other slots remain as-is (PATCH semantics).
public record EquipRequest(List<EquipSlotRequest> slots) {}
