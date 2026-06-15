package com.wow.timewalkers.dto;

import java.util.List;

public record GearSearchResultDTO(List<ArmorPieceDTO> armorPieces, List<WeaponDTO> weapons) {}
