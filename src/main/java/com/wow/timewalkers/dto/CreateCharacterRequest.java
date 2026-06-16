package com.wow.timewalkers.dto;

import com.wow.timewalkers.enums.WowClass;
import com.wow.timewalkers.enums.WowGender;
import com.wow.timewalkers.enums.WowRace;

// Request body for POST /api/characters.
// Jackson deserializes the JSON into this record using the canonical constructor.
// Enum fields are deserialized from their string names (e.g. "NIGHT_ELF" -> WowRace.NIGHT_ELF).
public record CreateCharacterRequest(String name, WowRace race, WowClass characterClass, WowGender gender) {}
