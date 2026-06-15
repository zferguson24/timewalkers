package com.wow.timewalkers.service;

import com.wow.timewalkers.enums.WowClass;
import com.wow.timewalkers.enums.WowRace;
import com.wow.timewalkers.exception.InvalidRaceClassCombinationException;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThatCode;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class CharacterValidatorTest {

    private final CharacterValidator validator = new CharacterValidator();

    @Nested
    @DisplayName("validateRaceClassCombination")
    class ValidateRaceClassCombination {

        // Valid combos — should not throw

        @Test
        @DisplayName("Night Elf Demon Hunter is valid")
        void nightElfDemonHunterValid() {
            assertThatCode(() -> validator.validateRaceClassCombination(
                    WowRace.NIGHT_ELF, WowClass.DEMON_HUNTER))
                    .doesNotThrowAnyException();
        }

        @Test
        @DisplayName("Blood Elf Demon Hunter is valid")
        void bloodElfDemonHunterValid() {
            assertThatCode(() -> validator.validateRaceClassCombination(
                    WowRace.BLOOD_ELF, WowClass.DEMON_HUNTER))
                    .doesNotThrowAnyException();
        }

        @Test
        @DisplayName("Void Elf Demon Hunter is valid")
        void voidElfDemonHunterValid() {
            assertThatCode(() -> validator.validateRaceClassCombination(
                    WowRace.VOID_ELF, WowClass.DEMON_HUNTER))
                    .doesNotThrowAnyException();
        }

        @Test
        @DisplayName("Dracthyr Evoker is valid")
        void dracthyrEvokerValid() {
            assertThatCode(() -> validator.validateRaceClassCombination(
                    WowRace.DRACTHYR, WowClass.EVOKER))
                    .doesNotThrowAnyException();
        }

        @Test
        @DisplayName("Human Paladin is valid")
        void humanPaladinValid() {
            assertThatCode(() -> validator.validateRaceClassCombination(
                    WowRace.HUMAN, WowClass.PALADIN))
                    .doesNotThrowAnyException();
        }

        @Test
        @DisplayName("Orc Shaman is valid")
        void orcShamanValid() {
            assertThatCode(() -> validator.validateRaceClassCombination(
                    WowRace.ORC, WowClass.SHAMAN))
                    .doesNotThrowAnyException();
        }

        @Test
        @DisplayName("Night Elf Druid is valid")
        void nightElfDruidValid() {
            assertThatCode(() -> validator.validateRaceClassCombination(
                    WowRace.NIGHT_ELF, WowClass.DRUID))
                    .doesNotThrowAnyException();
        }

        @Test
        @DisplayName("Human can be any unrestricted class (Warrior)")
        void humanWarriorValid() {
            assertThatCode(() -> validator.validateRaceClassCombination(
                    WowRace.HUMAN, WowClass.WARRIOR))
                    .doesNotThrowAnyException();
        }

        // Invalid combos — should throw

        @Test
        @DisplayName("Human Evoker is invalid — Evoker is Dracthyr only")
        void humanEvokerInvalid() {
            assertThatThrownBy(() -> validator.validateRaceClassCombination(
                    WowRace.HUMAN, WowClass.EVOKER))
                    .isInstanceOf(InvalidRaceClassCombinationException.class)
                    .hasMessageContaining("Human")
                    .hasMessageContaining("Evoker");
        }

        @Test
        @DisplayName("Dracthyr Demon Hunter is invalid")
        void dracthyrDemonHunterInvalid() {
            assertThatThrownBy(() -> validator.validateRaceClassCombination(
                    WowRace.DRACTHYR, WowClass.DEMON_HUNTER))
                    .isInstanceOf(InvalidRaceClassCombinationException.class);
        }

        @Test
        @DisplayName("Human Demon Hunter is invalid")
        void humanDemonHunterInvalid() {
            assertThatThrownBy(() -> validator.validateRaceClassCombination(
                    WowRace.HUMAN, WowClass.DEMON_HUNTER))
                    .isInstanceOf(InvalidRaceClassCombinationException.class)
                    .hasMessageContaining("Human")
                    .hasMessageContaining("Demon Hunter");
        }

        @Test
        @DisplayName("Dracthyr Monk is invalid")
        void dracthyrMonkInvalid() {
            assertThatThrownBy(() -> validator.validateRaceClassCombination(
                    WowRace.DRACTHYR, WowClass.MONK))
                    .isInstanceOf(InvalidRaceClassCombinationException.class);
        }

        @Test
        @DisplayName("Dracthyr Paladin is invalid")
        void dracthyrPaladinInvalid() {
            assertThatThrownBy(() -> validator.validateRaceClassCombination(
                    WowRace.DRACTHYR, WowClass.PALADIN))
                    .isInstanceOf(InvalidRaceClassCombinationException.class);
        }

        @Test
        @DisplayName("Gnome Druid is invalid")
        void gnomeDruidInvalid() {
            assertThatThrownBy(() -> validator.validateRaceClassCombination(
                    WowRace.GNOME, WowClass.DRUID))
                    .isInstanceOf(InvalidRaceClassCombinationException.class);
        }

        @Test
        @DisplayName("Human Shaman is invalid")
        void humanShamanInvalid() {
            assertThatThrownBy(() -> validator.validateRaceClassCombination(
                    WowRace.HUMAN, WowClass.SHAMAN))
                    .isInstanceOf(InvalidRaceClassCombinationException.class)
                    .hasMessageContaining("Human")
                    .hasMessageContaining("Shaman");
        }

        @Test
        @DisplayName("Dracthyr Death Knight is invalid")
        void dracthyrDeathKnightInvalid() {
            assertThatThrownBy(() -> validator.validateRaceClassCombination(
                    WowRace.DRACTHYR, WowClass.DEATH_KNIGHT))
                    .isInstanceOf(InvalidRaceClassCombinationException.class);
        }

        @Test
        @DisplayName("Error message uses human-readable names, not raw enum values")
        void errorMessageIsHumanReadable() {
            assertThatThrownBy(() -> validator.validateRaceClassCombination(
                    WowRace.HIGHMOUNTAIN_TAUREN, WowClass.DEMON_HUNTER))
                    .isInstanceOf(InvalidRaceClassCombinationException.class)
                    .hasMessage("Highmountain Tauren cannot be a Demon Hunter");
        }
    }
}
