package com.wow.timewalkers.dto;

import com.wow.timewalkers.enums.EquipmentSlot;

import java.util.List;

// Request body for DELETE /api/characters/{name}/gear.
// Lists the specific slots to clear. Slots not listed remain equipped.
public record UnequipRequest(List<EquipmentSlot> slots) {}
