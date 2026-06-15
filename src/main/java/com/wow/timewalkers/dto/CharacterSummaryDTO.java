package com.wow.timewalkers.dto;

import com.wow.timewalkers.enums.WowClass;
import com.wow.timewalkers.enums.WowRace;

public record CharacterSummaryDTO(String name, WowRace race, WowClass characterClass) {}
