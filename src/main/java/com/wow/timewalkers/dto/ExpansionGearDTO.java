package com.wow.timewalkers.dto;

import java.util.List;

// Response wrapper for the /api/gear/expansion endpoint.
// Groups both armor and weapon lists under the expansion name.
public record ExpansionGearDTO(
        String expansion,
        List<ArmorPieceDTO> armorPieces,
        List<WeaponDTO> weapons
) {}
