package com.wow.timewalkers.service;

import com.wow.timewalkers.enums.WowClass;
import com.wow.timewalkers.enums.WowRace;
import com.wow.timewalkers.exception.InvalidRaceClassCombinationException;
import org.springframework.stereotype.Component;

import java.util.EnumMap;
import java.util.EnumSet;
import java.util.Map;
import java.util.Set;

@Component
public class CharacterValidator {

    private static final Map<WowClass, Set<WowRace>> VALID_COMBINATIONS;

    static {
        VALID_COMBINATIONS = new EnumMap<>(WowClass.class);

        EnumSet<WowRace> allRaces = EnumSet.allOf(WowRace.class);

        VALID_COMBINATIONS.put(WowClass.DEATH_KNIGHT, EnumSet.of(
                WowRace.HUMAN, WowRace.ORC, WowRace.DWARF, WowRace.GNOME, WowRace.NIGHT_ELF,
                WowRace.DRAENEI, WowRace.WORGEN, WowRace.VOID_ELF, WowRace.LIGHTFORGED_DRAENEI,
                WowRace.KUL_TIRAN, WowRace.DARK_IRON_DWARF, WowRace.MECHAGNOME,
                WowRace.TROLL, WowRace.TAUREN, WowRace.UNDEAD, WowRace.BLOOD_ELF, WowRace.GOBLIN,
                WowRace.NIGHTBORNE, WowRace.HIGHMOUNTAIN_TAUREN, WowRace.MAGHAR_ORC,
                WowRace.VULPERA, WowRace.ZANDALARI_TROLL, WowRace.PANDAREN, WowRace.EARTHEN
        ));

        VALID_COMBINATIONS.put(WowClass.DEMON_HUNTER, EnumSet.of(
                WowRace.NIGHT_ELF, WowRace.BLOOD_ELF, WowRace.VOID_ELF
        ));

        VALID_COMBINATIONS.put(WowClass.DRUID, EnumSet.of(
                WowRace.NIGHT_ELF, WowRace.WORGEN, WowRace.TAUREN, WowRace.TROLL,
                WowRace.ZANDALARI_TROLL, WowRace.KUL_TIRAN, WowRace.HIGHMOUNTAIN_TAUREN,
                WowRace.HARANIR
        ));

        VALID_COMBINATIONS.put(WowClass.EVOKER, EnumSet.of(WowRace.DRACTHYR));

        VALID_COMBINATIONS.put(WowClass.HUNTER, EnumSet.copyOf(allRaces));

        VALID_COMBINATIONS.put(WowClass.MAGE, EnumSet.copyOf(allRaces));

        // All races except Dracthyr (as of patch 11.0.5, Dracthyr cannot be Monk)
        VALID_COMBINATIONS.put(WowClass.MONK, EnumSet.complementOf(EnumSet.of(WowRace.DRACTHYR)));

        VALID_COMBINATIONS.put(WowClass.PALADIN, EnumSet.of(
                WowRace.HUMAN, WowRace.DWARF, WowRace.DRAENEI, WowRace.BLOOD_ELF, WowRace.TAUREN,
                WowRace.LIGHTFORGED_DRAENEI, WowRace.DARK_IRON_DWARF, WowRace.ZANDALARI_TROLL,
                WowRace.EARTHEN
        ));

        VALID_COMBINATIONS.put(WowClass.PRIEST, EnumSet.copyOf(allRaces));

        VALID_COMBINATIONS.put(WowClass.ROGUE, EnumSet.copyOf(allRaces));

        VALID_COMBINATIONS.put(WowClass.SHAMAN, EnumSet.of(
                WowRace.ORC, WowRace.DWARF, WowRace.DARK_IRON_DWARF, WowRace.DRAENEI,
                WowRace.TROLL, WowRace.TAUREN, WowRace.GOBLIN,
                WowRace.HIGHMOUNTAIN_TAUREN, WowRace.MAGHAR_ORC,
                WowRace.VULPERA, WowRace.ZANDALARI_TROLL, WowRace.PANDAREN,
                WowRace.EARTHEN, WowRace.HARANIR
        ));

        VALID_COMBINATIONS.put(WowClass.WARLOCK, EnumSet.copyOf(allRaces));

        VALID_COMBINATIONS.put(WowClass.WARRIOR, EnumSet.copyOf(allRaces));
    }

    public void validateRaceClassCombination(WowRace race, WowClass wowClass) {
        Set<WowRace> validRaces = VALID_COMBINATIONS.get(wowClass);
        if (validRaces == null || !validRaces.contains(race)) {
            throw new InvalidRaceClassCombinationException(
                    formatEnumName(race.name()) + " cannot be a " + formatEnumName(wowClass.name()));
        }
    }

    // Package-visible for tests
    static Set<WowRace> validRacesFor(WowClass wowClass) {
        return VALID_COMBINATIONS.get(wowClass);
    }

    private String formatEnumName(String name) {
        String[] parts = name.split("_");
        StringBuilder sb = new StringBuilder();
        for (String part : parts) {
            if (!sb.isEmpty()) sb.append(" ");
            sb.append(Character.toUpperCase(part.charAt(0)));
            sb.append(part.substring(1).toLowerCase());
        }
        return sb.toString();
    }
}
