package com.wow.timewalkers.service;

import com.wow.timewalkers.dto.GearPlanEventDTO;
import com.wow.timewalkers.dto.GearPlanResponseDTO;
import com.wow.timewalkers.entity.ArmorPiece;
import com.wow.timewalkers.entity.CharacterEquipment;
import com.wow.timewalkers.entity.TimewalkingEvent;
import com.wow.timewalkers.entity.Weapon;
import com.wow.timewalkers.entity.WowCharacter;
import com.wow.timewalkers.enums.EquipmentSlot;
import com.wow.timewalkers.enums.WowClass;
import com.wow.timewalkers.enums.WowGender;
import com.wow.timewalkers.enums.WowRace;
import com.wow.timewalkers.exception.CharacterNotFoundException;
import com.wow.timewalkers.repository.ArmorPieceRepository;
import com.wow.timewalkers.repository.CharacterEquipmentRepository;
import com.wow.timewalkers.repository.CharacterRepository;
import com.wow.timewalkers.repository.TimewalkingEventRepository;
import com.wow.timewalkers.repository.WeaponRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class GearPlanServiceTest {

    @Mock private CharacterRepository characterRepository;
    @Mock private CharacterEquipmentRepository equipmentRepository;
    @Mock private ArmorPieceRepository armorPieceRepository;
    @Mock private WeaponRepository weaponRepository;
    @Mock private TimewalkingEventRepository twEventRepository;

    private GearPlanService gearPlanService;

    @BeforeEach
    void setUp() {
        gearPlanService = new GearPlanService(
                characterRepository, equipmentRepository,
                armorPieceRepository, weaponRepository,
                twEventRepository, new GearValidator());
    }

    // -----------------------------------------------------------------------
    // Helpers
    // -----------------------------------------------------------------------

    private WowCharacter character(WowClass cls) {
        WowCharacter c = new WowCharacter();
        c.setName("TESTCHAR");
        c.setRace(WowRace.HUMAN);
        c.setCharacterClass(cls);
        c.setGender(WowGender.MALE);
        return c;
    }

    private ArmorPiece armor(String name, String slot, String armorType, String primaryStat, int cost) {
        ArmorPiece ap = new ArmorPiece();
        ap.setName(name);
        ap.setSlot(slot);
        ap.setArmorType(armorType);
        ap.setPrimaryStat(primaryStat);
        ap.setCost(cost);
        return ap;
    }

    private Weapon weapon(String name, String weaponSlot, String weaponType, String weaponStat, int cost) {
        Weapon w = new Weapon();
        w.setName(name);
        w.setWeaponSlot(weaponSlot);
        w.setWeaponType(weaponType);
        w.setWeaponStat(weaponStat);
        w.setCost(cost);
        return w;
    }

    private CharacterEquipment equippedWithArmor(WowCharacter c, EquipmentSlot slot, ArmorPiece ap) {
        CharacterEquipment ce = new CharacterEquipment();
        ce.setWowCharacter(c);
        ce.setSlot(slot);
        ce.setArmorPiece(ap);
        return ce;
    }

    private CharacterEquipment equippedWithWeapon(WowCharacter c, EquipmentSlot slot, Weapon w) {
        CharacterEquipment ce = new CharacterEquipment();
        ce.setWowCharacter(c);
        ce.setSlot(slot);
        ce.setWeapon(w);
        return ce;
    }

    private TimewalkingEvent event(String expansion, LocalDate start, LocalDate end) {
        TimewalkingEvent e = new TimewalkingEvent();
        e.setExpansion(expansion);
        e.setStartDate(start);
        e.setEndDate(end);
        e.setTurbulentTimeways(false);
        return e;
    }

    /** Builds a full set of 14 armor-slot equipment rows (excludes weapon slots). */
    private List<CharacterEquipment> fullArmorLoadout(WowCharacter c, String armorType, String primaryStat) {
        return List.of(
                equippedWithArmor(c, EquipmentSlot.HEAD,      armor("Helm",   "Head",      armorType,    primaryStat, 100)),
                equippedWithArmor(c, EquipmentSlot.NECK,      armor("Neck",   "Neck",      "Agnostic",   null,        100)),
                equippedWithArmor(c, EquipmentSlot.SHOULDERS, armor("Shldr",  "Shoulders", armorType,    primaryStat, 100)),
                equippedWithArmor(c, EquipmentSlot.BACK,      armor("Cape",   "Back",      "Agnostic",   null,        100)),
                equippedWithArmor(c, EquipmentSlot.CHEST,     armor("Chest",  "Chest",     armorType,    primaryStat, 100)),
                equippedWithArmor(c, EquipmentSlot.WRIST,     armor("Wrist",  "Wrist",     armorType,    primaryStat, 100)),
                equippedWithArmor(c, EquipmentSlot.HANDS,     armor("Hands",  "Hands",     armorType,    primaryStat, 100)),
                equippedWithArmor(c, EquipmentSlot.WAIST,     armor("Belt",   "Waist",     armorType,    primaryStat, 100)),
                equippedWithArmor(c, EquipmentSlot.LEGS,      armor("Legs",   "Legs",      armorType,    primaryStat, 100)),
                equippedWithArmor(c, EquipmentSlot.FEET,      armor("Boots",  "Feet",      armorType,    primaryStat, 100)),
                equippedWithArmor(c, EquipmentSlot.FINGER_1,  armor("Ring1",  "Finger",    "Agnostic",   null,        100)),
                equippedWithArmor(c, EquipmentSlot.FINGER_2,  armor("Ring2",  "Finger",    "Agnostic",   null,        100)),
                equippedWithArmor(c, EquipmentSlot.TRINKET_1, armor("Trink1", "Trinket",   "Agnostic",   null,        100)),
                equippedWithArmor(c, EquipmentSlot.TRINKET_2, armor("Trink2", "Trinket",   "Agnostic",   null,        100))
        );
    }

    // -----------------------------------------------------------------------
    // Character Lookup
    // -----------------------------------------------------------------------

    @Nested
    @DisplayName("Character lookup")
    class CharacterLookup {

        @Test
        @DisplayName("throws CharacterNotFoundException when character does not exist")
        void throwsWhenCharacterNotFound() {
            when(characterRepository.findByName("NOBODY")).thenReturn(Optional.empty());

            assertThatThrownBy(() -> gearPlanService.computeGearPlan("nobody", null))
                    .isInstanceOf(CharacterNotFoundException.class)
                    .hasMessageContaining("NOBODY");
        }
    }

    // -----------------------------------------------------------------------
    // Stat Resolution
    // -----------------------------------------------------------------------

    @Nested
    @DisplayName("Stat resolution")
    class StatResolution {

        @Test
        @DisplayName("pure class ignores preferredStat and uses its fixed stat")
        void pureClassIgnoresPreferredStat() {
            WowCharacter c = character(WowClass.DEATH_KNIGHT); // fixed: Strength
            when(characterRepository.findByName("TESTCHAR")).thenReturn(Optional.of(c));
            when(equipmentRepository.findByWowCharacter(c)).thenReturn(List.of());
            when(twEventRepository.findByStartDateGreaterThanEqualOrderByStartDateAsc(any())).thenReturn(List.of());

            GearPlanResponseDTO result = gearPlanService.computeGearPlan("testchar", "Intellect");

            assertThat(result.resolvedStat()).isEqualTo("Strength");
        }

        @Test
        @DisplayName("hybrid class uses a valid preferredStat supplied by the caller")
        void hybridClassUsesValidPreferredStat() {
            WowCharacter c = character(WowClass.DRUID); // default: Agility
            when(characterRepository.findByName("TESTCHAR")).thenReturn(Optional.of(c));
            when(equipmentRepository.findByWowCharacter(c)).thenReturn(List.of());
            when(twEventRepository.findByStartDateGreaterThanEqualOrderByStartDateAsc(any())).thenReturn(List.of());

            GearPlanResponseDTO result = gearPlanService.computeGearPlan("testchar", "Intellect");

            assertThat(result.resolvedStat()).isEqualTo("Intellect");
        }

        @Test
        @DisplayName("hybrid class falls back to class default when preferredStat is null")
        void hybridClassFallsBackToDefaultWhenNullPreferredStat() {
            WowCharacter c = character(WowClass.DRUID);
            when(characterRepository.findByName("TESTCHAR")).thenReturn(Optional.of(c));
            when(equipmentRepository.findByWowCharacter(c)).thenReturn(List.of());
            when(twEventRepository.findByStartDateGreaterThanEqualOrderByStartDateAsc(any())).thenReturn(List.of());

            GearPlanResponseDTO result = gearPlanService.computeGearPlan("testchar", null);

            assertThat(result.resolvedStat()).isEqualTo("Agility");
        }

        @Test
        @DisplayName("hybrid class falls back to class default when preferredStat is not a valid stat name")
        void hybridClassFallsBackToDefaultOnInvalidPreferredStat() {
            WowCharacter c = character(WowClass.MONK); // default: Agility
            when(characterRepository.findByName("TESTCHAR")).thenReturn(Optional.of(c));
            when(equipmentRepository.findByWowCharacter(c)).thenReturn(List.of());
            when(twEventRepository.findByStartDateGreaterThanEqualOrderByStartDateAsc(any())).thenReturn(List.of());

            GearPlanResponseDTO result = gearPlanService.computeGearPlan("testchar", "CriticalStrike");

            assertThat(result.resolvedStat()).isEqualTo("Agility");
        }

        @Test
        @DisplayName("Paladin default stat is Strength")
        void paladinDefaultStatIsStrength() {
            WowCharacter c = character(WowClass.PALADIN);
            when(characterRepository.findByName("TESTCHAR")).thenReturn(Optional.of(c));
            when(equipmentRepository.findByWowCharacter(c)).thenReturn(List.of());
            when(twEventRepository.findByStartDateGreaterThanEqualOrderByStartDateAsc(any())).thenReturn(List.of());

            GearPlanResponseDTO result = gearPlanService.computeGearPlan("testchar", null);

            assertThat(result.resolvedStat()).isEqualTo("Strength");
        }
    }

    // -----------------------------------------------------------------------
    // Stat Options
    // -----------------------------------------------------------------------

    @Nested
    @DisplayName("Stat options")
    class StatOptions {

        @Test
        @DisplayName("pure class returns empty statOptions")
        void pureClassHasEmptyStatOptions() {
            WowCharacter c = character(WowClass.WARRIOR);
            when(characterRepository.findByName("TESTCHAR")).thenReturn(Optional.of(c));
            when(equipmentRepository.findByWowCharacter(c)).thenReturn(List.of());
            when(twEventRepository.findByStartDateGreaterThanEqualOrderByStartDateAsc(any())).thenReturn(List.of());

            assertThat(gearPlanService.computeGearPlan("testchar", null).statOptions()).isEmpty();
        }

        @Test
        @DisplayName("Druid returns [Agility, Intellect] statOptions")
        void druidHasAgilityIntellectOptions() {
            WowCharacter c = character(WowClass.DRUID);
            when(characterRepository.findByName("TESTCHAR")).thenReturn(Optional.of(c));
            when(equipmentRepository.findByWowCharacter(c)).thenReturn(List.of());
            when(twEventRepository.findByStartDateGreaterThanEqualOrderByStartDateAsc(any())).thenReturn(List.of());

            assertThat(gearPlanService.computeGearPlan("testchar", null).statOptions())
                    .containsExactly("Agility", "Intellect");
        }

        @Test
        @DisplayName("Monk returns [Agility, Intellect] statOptions")
        void monkHasAgilityIntellectOptions() {
            WowCharacter c = character(WowClass.MONK);
            when(characterRepository.findByName("TESTCHAR")).thenReturn(Optional.of(c));
            when(equipmentRepository.findByWowCharacter(c)).thenReturn(List.of());
            when(twEventRepository.findByStartDateGreaterThanEqualOrderByStartDateAsc(any())).thenReturn(List.of());

            assertThat(gearPlanService.computeGearPlan("testchar", null).statOptions())
                    .containsExactly("Agility", "Intellect");
        }

        @Test
        @DisplayName("Shaman returns [Agility, Intellect] statOptions")
        void shamanHasAgilityIntellectOptions() {
            WowCharacter c = character(WowClass.SHAMAN);
            when(characterRepository.findByName("TESTCHAR")).thenReturn(Optional.of(c));
            when(equipmentRepository.findByWowCharacter(c)).thenReturn(List.of());
            when(twEventRepository.findByStartDateGreaterThanEqualOrderByStartDateAsc(any())).thenReturn(List.of());

            assertThat(gearPlanService.computeGearPlan("testchar", null).statOptions())
                    .containsExactly("Agility", "Intellect");
        }

        @Test
        @DisplayName("Paladin returns [Strength, Intellect] statOptions")
        void paladinHasStrengthIntellectOptions() {
            WowCharacter c = character(WowClass.PALADIN);
            when(characterRepository.findByName("TESTCHAR")).thenReturn(Optional.of(c));
            when(equipmentRepository.findByWowCharacter(c)).thenReturn(List.of());
            when(twEventRepository.findByStartDateGreaterThanEqualOrderByStartDateAsc(any())).thenReturn(List.of());

            assertThat(gearPlanService.computeGearPlan("testchar", null).statOptions())
                    .containsExactly("Strength", "Intellect");
        }
    }

    // -----------------------------------------------------------------------
    // Fully Equipped State
    // -----------------------------------------------------------------------

    @Nested
    @DisplayName("Fully equipped state")
    class FullyEquippedState {

        @Test
        @DisplayName("character with all 16 slots filled reports fullyEquipped with today as date and no events")
        void alreadyFullyEquippedReturnsToday() {
            WowCharacter c = character(WowClass.WARRIOR);
            List<CharacterEquipment> allSlots = new java.util.ArrayList<>(fullArmorLoadout(c, "Plate", "Strength"));
            allSlots.add(equippedWithWeapon(c, EquipmentSlot.MAIN_HAND, weapon("Sword",  "1H",       "Sword",  "Strength", 200)));
            allSlots.add(equippedWithWeapon(c, EquipmentSlot.OFF_HAND,  weapon("Shield", "Off-Hand", "Shield", "Strength", 150)));

            when(characterRepository.findByName("TESTCHAR")).thenReturn(Optional.of(c));
            when(equipmentRepository.findByWowCharacter(c)).thenReturn(allSlots);
            when(twEventRepository.findByStartDateGreaterThanEqualOrderByStartDateAsc(any())).thenReturn(List.of());

            GearPlanResponseDTO result = gearPlanService.computeGearPlan("testchar", null);

            assertThat(result.fullyEquipped()).isTrue();
            assertThat(result.events()).isEmpty();
            assertThat(result.unresolvableSlots()).isEmpty();
            assertThat(result.fullyEquippedDate()).isEqualTo(LocalDate.now());
        }

        @Test
        @DisplayName("2H weapon in main hand — off-hand slot treated as implicitly filled")
        void twoHandedWeaponMakesOffHandImplicitlyFilled() {
            WowCharacter c = character(WowClass.WARRIOR);
            List<CharacterEquipment> slots = new java.util.ArrayList<>(fullArmorLoadout(c, "Plate", "Strength"));
            slots.add(equippedWithWeapon(c, EquipmentSlot.MAIN_HAND, weapon("Greataxe", "2H", "Axe", "Strength", 300)));
            // OFF_HAND intentionally absent — 2H blocks the slot

            when(characterRepository.findByName("TESTCHAR")).thenReturn(Optional.of(c));
            when(equipmentRepository.findByWowCharacter(c)).thenReturn(slots);
            when(twEventRepository.findByStartDateGreaterThanEqualOrderByStartDateAsc(any())).thenReturn(List.of());

            GearPlanResponseDTO result = gearPlanService.computeGearPlan("testchar", null);

            assertThat(result.fullyEquipped()).isTrue();
            assertThat(result.unresolvableSlots()).isEmpty();
        }

        @Test
        @DisplayName("alreadyEquippedSlots lists all currently filled slots sorted alphabetically")
        void alreadyEquippedSlotsAreSorted() {
            WowCharacter c = character(WowClass.WARRIOR);
            List<CharacterEquipment> slots = new java.util.ArrayList<>(fullArmorLoadout(c, "Plate", "Strength"));
            slots.add(equippedWithWeapon(c, EquipmentSlot.MAIN_HAND, weapon("Sword",  "1H",       "Sword",  "Strength", 200)));
            slots.add(equippedWithWeapon(c, EquipmentSlot.OFF_HAND,  weapon("Shield", "Off-Hand", "Shield", "Strength", 150)));

            when(characterRepository.findByName("TESTCHAR")).thenReturn(Optional.of(c));
            when(equipmentRepository.findByWowCharacter(c)).thenReturn(slots);
            when(twEventRepository.findByStartDateGreaterThanEqualOrderByStartDateAsc(any())).thenReturn(List.of());

            GearPlanResponseDTO result = gearPlanService.computeGearPlan("testchar", null);

            assertThat(result.alreadyEquippedSlots()).isSorted();
            assertThat(result.alreadyEquippedSlots()).hasSize(16);
        }
    }

    // -----------------------------------------------------------------------
    // Gear Plan Events
    // -----------------------------------------------------------------------

    @Nested
    @DisplayName("Gear plan events")
    class GearPlanEvents {

        @Test
        @DisplayName("event that covers one armor slot produces one event entry with the correct item")
        void singleEventCoversOneArmorSlot() {
            WowCharacter c = character(WowClass.WARRIOR);
            when(characterRepository.findByName("TESTCHAR")).thenReturn(Optional.of(c));
            when(equipmentRepository.findByWowCharacter(c)).thenReturn(List.of());

            LocalDate start = LocalDate.now().plusDays(1);
            when(twEventRepository.findByStartDateGreaterThanEqualOrderByStartDateAsc(any()))
                    .thenReturn(List.of(event("Burning Crusade", start, start.plusDays(6))));

            ArmorPiece helm = armor("Gladiator Helm", "Head", "Plate", "Strength", 150);
            when(armorPieceRepository.findDistinctSlotsByExpansionAndArmorTypes(eq("Burning Crusade"), any()))
                    .thenReturn(List.of("Head"));
            when(armorPieceRepository.findByExpansionAndSlotAndArmorTypeIn(eq("Burning Crusade"), eq("Head"), any()))
                    .thenReturn(List.of(helm));
            when(weaponRepository.findByExpansion("Burning Crusade")).thenReturn(List.of());

            GearPlanResponseDTO result = gearPlanService.computeGearPlan("testchar", null);

            assertThat(result.events()).hasSize(1);
            GearPlanEventDTO planEvent = result.events().get(0);
            assertThat(planEvent.expansion()).isEqualTo("Burning Crusade");
            assertThat(planEvent.startDate()).isEqualTo(start);
            assertThat(planEvent.slots()).hasSize(1);
            assertThat(planEvent.slots().get(0).slot()).isEqualTo("HEAD");
            assertThat(planEvent.slots().get(0).itemName()).isEqualTo("Gladiator Helm");
            assertThat(planEvent.slots().get(0).cost()).isEqualTo(150);
        }

        @Test
        @DisplayName("multiple events are listed in chronological order")
        void eventsArePresentedInChronologicalOrder() {
            WowCharacter c = character(WowClass.WARRIOR);
            when(characterRepository.findByName("TESTCHAR")).thenReturn(Optional.of(c));
            when(equipmentRepository.findByWowCharacter(c)).thenReturn(List.of());

            LocalDate firstStart  = LocalDate.now().plusDays(1);
            LocalDate secondStart = LocalDate.now().plusDays(30);
            when(twEventRepository.findByStartDateGreaterThanEqualOrderByStartDateAsc(any()))
                    .thenReturn(List.of(
                            event("Burning Crusade",        firstStart,  firstStart.plusDays(6)),
                            event("Wrath of the Lich King", secondStart, secondStart.plusDays(6))));

            when(armorPieceRepository.findDistinctSlotsByExpansionAndArmorTypes(eq("Burning Crusade"), any()))
                    .thenReturn(List.of("Head"));
            when(armorPieceRepository.findByExpansionAndSlotAndArmorTypeIn(eq("Burning Crusade"), eq("Head"), any()))
                    .thenReturn(List.of(armor("BC Helm", "Head", "Plate", "Strength", 150)));
            when(weaponRepository.findByExpansion("Burning Crusade")).thenReturn(List.of());

            when(armorPieceRepository.findDistinctSlotsByExpansionAndArmorTypes(eq("Wrath of the Lich King"), any()))
                    .thenReturn(List.of("Chest"));
            when(armorPieceRepository.findByExpansionAndSlotAndArmorTypeIn(eq("Wrath of the Lich King"), eq("Chest"), any()))
                    .thenReturn(List.of(armor("WotLK Chest", "Chest", "Plate", "Strength", 200)));
            when(weaponRepository.findByExpansion("Wrath of the Lich King")).thenReturn(List.of());

            GearPlanResponseDTO result = gearPlanService.computeGearPlan("testchar", null);

            assertThat(result.events()).hasSize(2);
            assertThat(result.events().get(0).expansion()).isEqualTo("Burning Crusade");
            assertThat(result.events().get(1).expansion()).isEqualTo("Wrath of the Lich King");
        }

        @Test
        @DisplayName("fullyEquippedDate equals the start date of the last event that fills remaining slots")
        void fullyEquippedDateIsLastEventStartDate() {
            WowCharacter c = character(WowClass.WARRIOR);
            when(characterRepository.findByName("TESTCHAR")).thenReturn(Optional.of(c));
            when(equipmentRepository.findByWowCharacter(c)).thenReturn(List.of());

            LocalDate firstStart  = LocalDate.now().plusDays(1);
            LocalDate secondStart = LocalDate.now().plusDays(30);
            when(twEventRepository.findByStartDateGreaterThanEqualOrderByStartDateAsc(any()))
                    .thenReturn(List.of(
                            event("Burning Crusade",        firstStart,  firstStart.plusDays(6)),
                            event("Wrath of the Lich King", secondStart, secondStart.plusDays(6))));

            // First event covers only Head
            when(armorPieceRepository.findDistinctSlotsByExpansionAndArmorTypes(eq("Burning Crusade"), any()))
                    .thenReturn(List.of("Head"));
            when(armorPieceRepository.findByExpansionAndSlotAndArmorTypeIn(eq("Burning Crusade"), eq("Head"), any()))
                    .thenReturn(List.of(armor("BC Helm", "Head", "Plate", "Strength", 150)));
            when(weaponRepository.findByExpansion("Burning Crusade")).thenReturn(List.of());

            // Second event covers all remaining armor + weapons
            when(armorPieceRepository.findDistinctSlotsByExpansionAndArmorTypes(eq("Wrath of the Lich King"), any()))
                    .thenReturn(List.of("Neck", "Shoulders", "Back", "Chest", "Wrist", "Hands", "Waist", "Legs", "Feet", "Finger", "Trinket"));
            for (String slot : List.of("Neck", "Shoulders", "Back", "Chest", "Wrist", "Hands", "Waist", "Legs", "Feet")) {
                when(armorPieceRepository.findByExpansionAndSlotAndArmorTypeIn(eq("Wrath of the Lich King"), eq(slot), any()))
                        .thenReturn(List.of(armor(slot + " piece", slot, "Plate", "Strength", 100)));
            }
            when(armorPieceRepository.findByExpansionAndSlotAndArmorTypeIn(eq("Wrath of the Lich King"), eq("Finger"), any()))
                    .thenReturn(List.of(armor("Ring A", "Finger", "Agnostic", null, 50), armor("Ring B", "Finger", "Agnostic", null, 50)));
            when(armorPieceRepository.findByExpansionAndSlotAndArmorTypeIn(eq("Wrath of the Lich King"), eq("Trinket"), any()))
                    .thenReturn(List.of(armor("Trinket A", "Trinket", "Agnostic", null, 50), armor("Trinket B", "Trinket", "Agnostic", null, 50)));
            when(weaponRepository.findByExpansion("Wrath of the Lich King"))
                    .thenReturn(List.of(
                            weapon("WotLK Sword",  "1H",       "Sword",  "Strength", 200),
                            weapon("WotLK Shield", "Off-Hand", "Shield", "Strength", 150)));

            GearPlanResponseDTO result = gearPlanService.computeGearPlan("testchar", null);

            assertThat(result.fullyEquipped()).isTrue();
            assertThat(result.fullyEquippedDate()).isEqualTo(secondStart);
        }

        @Test
        @DisplayName("turbulent timeways flag is propagated from the event entity to the DTO")
        void turbulentTimewaysFlagPropagated() {
            WowCharacter c = character(WowClass.WARRIOR);
            when(characterRepository.findByName("TESTCHAR")).thenReturn(Optional.of(c));
            when(equipmentRepository.findByWowCharacter(c)).thenReturn(List.of());

            LocalDate start = LocalDate.now().plusDays(1);
            TimewalkingEvent turb = event("Burning Crusade", start, start.plusDays(6));
            turb.setTurbulentTimeways(true);
            when(twEventRepository.findByStartDateGreaterThanEqualOrderByStartDateAsc(any())).thenReturn(List.of(turb));

            when(armorPieceRepository.findDistinctSlotsByExpansionAndArmorTypes(eq("Burning Crusade"), any()))
                    .thenReturn(List.of("Head"));
            when(armorPieceRepository.findByExpansionAndSlotAndArmorTypeIn(eq("Burning Crusade"), eq("Head"), any()))
                    .thenReturn(List.of(armor("Gladiator Helm", "Head", "Plate", "Strength", 150)));
            when(weaponRepository.findByExpansion("Burning Crusade")).thenReturn(List.of());

            GearPlanResponseDTO result = gearPlanService.computeGearPlan("testchar", null);

            assertThat(result.events()).hasSize(1);
            assertThat(result.events().get(0).turbulentTimeways()).isTrue();
        }

        @Test
        @DisplayName("event loop stops as soon as all unfilled slots are covered")
        void stopsIteratingEventsWhenAllSlotsFilled() {
            WowCharacter c = character(WowClass.WARRIOR);
            when(characterRepository.findByName("TESTCHAR")).thenReturn(Optional.of(c));

            // Pre-fill all slots except HEAD
            List<CharacterEquipment> slots = new java.util.ArrayList<>(fullArmorLoadout(c, "Plate", "Strength"));
            slots.removeIf(ce -> ce.getSlot() == EquipmentSlot.HEAD);
            slots.add(equippedWithWeapon(c, EquipmentSlot.MAIN_HAND, weapon("Sword",  "1H",       "Sword",  "Strength", 200)));
            slots.add(equippedWithWeapon(c, EquipmentSlot.OFF_HAND,  weapon("Shield", "Off-Hand", "Shield", "Strength", 150)));
            when(equipmentRepository.findByWowCharacter(c)).thenReturn(slots);

            LocalDate firstStart  = LocalDate.now().plusDays(1);
            LocalDate secondStart = LocalDate.now().plusDays(30);
            when(twEventRepository.findByStartDateGreaterThanEqualOrderByStartDateAsc(any()))
                    .thenReturn(List.of(
                            event("Burning Crusade",        firstStart,  firstStart.plusDays(6)),
                            event("Wrath of the Lich King", secondStart, secondStart.plusDays(6))));

            // First event fills HEAD — second event should never be consulted
            when(armorPieceRepository.findDistinctSlotsByExpansionAndArmorTypes(eq("Burning Crusade"), any()))
                    .thenReturn(List.of("Head"));
            when(armorPieceRepository.findByExpansionAndSlotAndArmorTypeIn(eq("Burning Crusade"), eq("Head"), any()))
                    .thenReturn(List.of(armor("BC Helm", "Head", "Plate", "Strength", 150)));
            when(weaponRepository.findByExpansion("Burning Crusade")).thenReturn(List.of());

            GearPlanResponseDTO result = gearPlanService.computeGearPlan("testchar", null);

            assertThat(result.fullyEquipped()).isTrue();
            assertThat(result.events()).hasSize(1);
            assertThat(result.events().get(0).expansion()).isEqualTo("Burning Crusade");
        }
    }

    // -----------------------------------------------------------------------
    // Stat Filtering
    // -----------------------------------------------------------------------

    @Nested
    @DisplayName("Stat filtering")
    class StatFiltering {

        @Test
        @DisplayName("armor item with a non-matching primary stat is excluded from the plan")
        void armorWithWrongPrimaryStatExcluded() {
            WowCharacter c = character(WowClass.WARRIOR); // resolvedStat = Strength
            when(characterRepository.findByName("TESTCHAR")).thenReturn(Optional.of(c));
            when(equipmentRepository.findByWowCharacter(c)).thenReturn(List.of());

            LocalDate start = LocalDate.now().plusDays(1);
            when(twEventRepository.findByStartDateGreaterThanEqualOrderByStartDateAsc(any()))
                    .thenReturn(List.of(event("Burning Crusade", start, start.plusDays(6))));

            // Only an Intellect helm — incompatible with Warrior (Strength)
            when(armorPieceRepository.findDistinctSlotsByExpansionAndArmorTypes(eq("Burning Crusade"), any()))
                    .thenReturn(List.of("Head"));
            when(armorPieceRepository.findByExpansionAndSlotAndArmorTypeIn(eq("Burning Crusade"), eq("Head"), any()))
                    .thenReturn(List.of(armor("Intellect Helm", "Head", "Plate", "Intellect", 150)));
            when(weaponRepository.findByExpansion("Burning Crusade")).thenReturn(List.of());

            GearPlanResponseDTO result = gearPlanService.computeGearPlan("testchar", null);

            assertThat(result.events()).isEmpty();
            assertThat(result.unresolvableSlots()).contains("HEAD");
        }

        @Test
        @DisplayName("armor with no primary stat field is included for any class (pure rating item)")
        void armorWithNullPrimaryStatAlwaysIncluded() {
            WowCharacter c = character(WowClass.WARRIOR);
            when(characterRepository.findByName("TESTCHAR")).thenReturn(Optional.of(c));
            when(equipmentRepository.findByWowCharacter(c)).thenReturn(List.of());

            LocalDate start = LocalDate.now().plusDays(1);
            when(twEventRepository.findByStartDateGreaterThanEqualOrderByStartDateAsc(any()))
                    .thenReturn(List.of(event("Burning Crusade", start, start.plusDays(6))));

            when(armorPieceRepository.findDistinctSlotsByExpansionAndArmorTypes(eq("Burning Crusade"), any()))
                    .thenReturn(List.of("Neck"));
            when(armorPieceRepository.findByExpansionAndSlotAndArmorTypeIn(eq("Burning Crusade"), eq("Neck"), any()))
                    .thenReturn(List.of(armor("Neck of Ratings", "Neck", "Agnostic", null, 100)));
            when(weaponRepository.findByExpansion("Burning Crusade")).thenReturn(List.of());

            GearPlanResponseDTO result = gearPlanService.computeGearPlan("testchar", null);

            assertThat(result.events()).hasSize(1);
            assertThat(result.events().get(0).slots().get(0).itemName()).isEqualTo("Neck of Ratings");
        }

        @Test
        @DisplayName("armor with 'No Primary Stat' note is treated as universally compatible")
        void armorWithNoPrimaryStatNoteAlwaysIncluded() {
            WowCharacter c = character(WowClass.WARRIOR);
            when(characterRepository.findByName("TESTCHAR")).thenReturn(Optional.of(c));
            when(equipmentRepository.findByWowCharacter(c)).thenReturn(List.of());

            LocalDate start = LocalDate.now().plusDays(1);
            when(twEventRepository.findByStartDateGreaterThanEqualOrderByStartDateAsc(any()))
                    .thenReturn(List.of(event("Burning Crusade", start, start.plusDays(6))));

            // primaryStat says Intellect but notes override to "No Primary Stat" — must be included for Warrior
            ArmorPiece specialHelm = armor("Special Helm", "Head", "Plate", "Intellect", 150);
            specialHelm.setNotes("No Primary Stat");
            when(armorPieceRepository.findDistinctSlotsByExpansionAndArmorTypes(eq("Burning Crusade"), any()))
                    .thenReturn(List.of("Head"));
            when(armorPieceRepository.findByExpansionAndSlotAndArmorTypeIn(eq("Burning Crusade"), eq("Head"), any()))
                    .thenReturn(List.of(specialHelm));
            when(weaponRepository.findByExpansion("Burning Crusade")).thenReturn(List.of());

            GearPlanResponseDTO result = gearPlanService.computeGearPlan("testchar", null);

            assertThat(result.events()).hasSize(1);
            assertThat(result.events().get(0).slots().get(0).itemName()).isEqualTo("Special Helm");
        }

        @Test
        @DisplayName("weapon with a non-matching stat is excluded from the plan")
        void weaponWithWrongStatExcluded() {
            WowCharacter c = character(WowClass.WARRIOR); // resolvedStat = Strength
            when(characterRepository.findByName("TESTCHAR")).thenReturn(Optional.of(c));
            when(equipmentRepository.findByWowCharacter(c)).thenReturn(fullArmorLoadout(c, "Plate", "Strength"));

            LocalDate start = LocalDate.now().plusDays(1);
            when(twEventRepository.findByStartDateGreaterThanEqualOrderByStartDateAsc(any()))
                    .thenReturn(List.of(event("Burning Crusade", start, start.plusDays(6))));

            when(armorPieceRepository.findDistinctSlotsByExpansionAndArmorTypes(eq("Burning Crusade"), any()))
                    .thenReturn(List.of());
            // Only an Intellect weapon available
            when(weaponRepository.findByExpansion("Burning Crusade"))
                    .thenReturn(List.of(weapon("Caster Sword", "1H", "Sword", "Intellect", 200)));

            GearPlanResponseDTO result = gearPlanService.computeGearPlan("testchar", null);

            assertThat(result.events()).isEmpty();
            assertThat(result.unresolvableSlots()).contains("MAIN_HAND");
        }

        @Test
        @DisplayName("weapon with null stat is included regardless of class")
        void weaponWithNullStatAlwaysIncluded() {
            WowCharacter c = character(WowClass.WARRIOR);
            when(characterRepository.findByName("TESTCHAR")).thenReturn(Optional.of(c));
            when(equipmentRepository.findByWowCharacter(c)).thenReturn(fullArmorLoadout(c, "Plate", "Strength"));

            LocalDate start = LocalDate.now().plusDays(1);
            when(twEventRepository.findByStartDateGreaterThanEqualOrderByStartDateAsc(any()))
                    .thenReturn(List.of(event("Burning Crusade", start, start.plusDays(6))));

            when(armorPieceRepository.findDistinctSlotsByExpansionAndArmorTypes(eq("Burning Crusade"), any()))
                    .thenReturn(List.of());
            when(weaponRepository.findByExpansion("Burning Crusade"))
                    .thenReturn(List.of(
                            weapon("Timeless Sword",  "1H",       "Sword",  null, 200),
                            weapon("Timeless Shield", "Off-Hand", "Shield", null, 150)));

            GearPlanResponseDTO result = gearPlanService.computeGearPlan("testchar", null);

            assertThat(result.events()).hasSize(1);
            assertThat(result.events().get(0).slots())
                    .extracting(s -> s.slot())
                    .containsExactlyInAnyOrder("MAIN_HAND", "OFF_HAND");
        }
    }

    // -----------------------------------------------------------------------
    // Weapon Plan
    // -----------------------------------------------------------------------

    @Nested
    @DisplayName("Weapon plan")
    class WeaponPlan {

        @Test
        @DisplayName("2H weapon covers main hand slot item — off-hand is implicitly filled and has no slot DTO")
        void twoHandedWeaponCoversOffHandImplicitly() {
            WowCharacter c = character(WowClass.WARRIOR);
            when(characterRepository.findByName("TESTCHAR")).thenReturn(Optional.of(c));
            when(equipmentRepository.findByWowCharacter(c)).thenReturn(fullArmorLoadout(c, "Plate", "Strength"));

            LocalDate start = LocalDate.now().plusDays(1);
            when(twEventRepository.findByStartDateGreaterThanEqualOrderByStartDateAsc(any()))
                    .thenReturn(List.of(event("Burning Crusade", start, start.plusDays(6))));

            when(armorPieceRepository.findDistinctSlotsByExpansionAndArmorTypes(eq("Burning Crusade"), any()))
                    .thenReturn(List.of());
            when(weaponRepository.findByExpansion("Burning Crusade"))
                    .thenReturn(List.of(weapon("Mighty Greataxe", "2H", "Axe", "Strength", 300)));

            GearPlanResponseDTO result = gearPlanService.computeGearPlan("testchar", null);

            assertThat(result.fullyEquipped()).isTrue();
            assertThat(result.events()).hasSize(1);
            // Only MAIN_HAND appears as an equippable item — OFF_HAND covered implicitly
            assertThat(result.events().get(0).slots())
                    .extracting(s -> s.slot())
                    .containsExactly("MAIN_HAND");
        }

        @Test
        @DisplayName("class that cannot use a weapon type does not receive that weapon in the plan")
        void forbiddenWeaponTypeNotIncludedInPlan() {
            WowCharacter c = character(WowClass.MAGE); // cannot use Axe
            when(characterRepository.findByName("TESTCHAR")).thenReturn(Optional.of(c));
            when(equipmentRepository.findByWowCharacter(c)).thenReturn(fullArmorLoadout(c, "Cloth", "Intellect"));

            LocalDate start = LocalDate.now().plusDays(1);
            when(twEventRepository.findByStartDateGreaterThanEqualOrderByStartDateAsc(any()))
                    .thenReturn(List.of(event("Burning Crusade", start, start.plusDays(6))));

            when(armorPieceRepository.findDistinctSlotsByExpansionAndArmorTypes(eq("Burning Crusade"), any()))
                    .thenReturn(List.of());
            // Only an Axe available — Mages cannot use Axes
            when(weaponRepository.findByExpansion("Burning Crusade"))
                    .thenReturn(List.of(weapon("Intellect Axe", "1H", "Axe", "Intellect", 300)));

            GearPlanResponseDTO result = gearPlanService.computeGearPlan("testchar", null);

            assertThat(result.events()).isEmpty();
            assertThat(result.unresolvableSlots()).contains("MAIN_HAND");
        }

        @Test
        @DisplayName("dual-wield class can fill both weapon slots from the same event")
        void dualWieldClassGetsBothWeaponSlots() {
            WowCharacter c = character(WowClass.ROGUE); // can dual wield Daggers
            when(characterRepository.findByName("TESTCHAR")).thenReturn(Optional.of(c));
            when(equipmentRepository.findByWowCharacter(c)).thenReturn(fullArmorLoadout(c, "Leather", "Agility"));

            LocalDate start = LocalDate.now().plusDays(1);
            when(twEventRepository.findByStartDateGreaterThanEqualOrderByStartDateAsc(any()))
                    .thenReturn(List.of(event("Burning Crusade", start, start.plusDays(6))));

            when(armorPieceRepository.findDistinctSlotsByExpansionAndArmorTypes(eq("Burning Crusade"), any()))
                    .thenReturn(List.of());
            // Two daggers — first is main hand candidate, second is off-hand via dual wield
            when(weaponRepository.findByExpansion("Burning Crusade"))
                    .thenReturn(List.of(
                            weapon("Dagger A", "1H", "Dagger", "Agility", 200),
                            weapon("Dagger B", "1H", "Dagger", "Agility", 200)));

            GearPlanResponseDTO result = gearPlanService.computeGearPlan("testchar", null);

            assertThat(result.events()).hasSize(1);
            assertThat(result.events().get(0).slots())
                    .extracting(s -> s.slot())
                    .containsExactlyInAnyOrder("MAIN_HAND", "OFF_HAND");
        }

        @Test
        @DisplayName("non-dual-wield class does not get a 1H weapon in the off-hand slot")
        void nonDualWieldClassDoesNotGetOffHandWeapon() {
            WowCharacter c = character(WowClass.WARRIOR); // can dual wield, but let's test someone who can't
            // Use Mage — no dual wield, only Dagger/Sword/Staff/Wand
            WowCharacter mage = character(WowClass.MAGE);
            when(characterRepository.findByName("TESTCHAR")).thenReturn(Optional.of(mage));
            when(equipmentRepository.findByWowCharacter(mage)).thenReturn(fullArmorLoadout(mage, "Cloth", "Intellect"));

            LocalDate start = LocalDate.now().plusDays(1);
            when(twEventRepository.findByStartDateGreaterThanEqualOrderByStartDateAsc(any()))
                    .thenReturn(List.of(event("Burning Crusade", start, start.plusDays(6))));

            when(armorPieceRepository.findDistinctSlotsByExpansionAndArmorTypes(eq("Burning Crusade"), any()))
                    .thenReturn(List.of());
            // Two swords available; Mage can use Sword but cannot dual wield
            when(weaponRepository.findByExpansion("Burning Crusade"))
                    .thenReturn(List.of(
                            weapon("Intellect Sword A", "1H", "Sword", "Intellect", 200),
                            weapon("Intellect Sword B", "1H", "Sword", "Intellect", 200)));

            GearPlanResponseDTO result = gearPlanService.computeGearPlan("testchar", null);

            assertThat(result.events()).hasSize(1);
            // Only MAIN_HAND filled — OFF_HAND requires dual wield for a 1H weapon
            assertThat(result.events().get(0).slots())
                    .extracting(s -> s.slot())
                    .containsOnly("MAIN_HAND");
            assertThat(result.unresolvableSlots()).contains("OFF_HAND");
        }
    }

    // -----------------------------------------------------------------------
    // Unresolvable Slots
    // -----------------------------------------------------------------------

    @Nested
    @DisplayName("Unresolvable slots")
    class UnresolvableSlots {

        @Test
        @DisplayName("no upcoming events means all unfilled slots are reported as unresolvable")
        void noUpcomingEventsAllSlotsUnresolvable() {
            WowCharacter c = character(WowClass.WARRIOR);
            when(characterRepository.findByName("TESTCHAR")).thenReturn(Optional.of(c));
            when(equipmentRepository.findByWowCharacter(c)).thenReturn(List.of());
            when(twEventRepository.findByStartDateGreaterThanEqualOrderByStartDateAsc(any())).thenReturn(List.of());

            GearPlanResponseDTO result = gearPlanService.computeGearPlan("testchar", null);

            assertThat(result.fullyEquipped()).isFalse();
            assertThat(result.fullyEquippedDate()).isNull();
            assertThat(result.unresolvableSlots()).hasSize(16);
        }

        @Test
        @DisplayName("slots covered by events are absent from unresolvableSlots; remaining are listed")
        void partiallyCoveredSlotsReported() {
            WowCharacter c = character(WowClass.WARRIOR);
            when(characterRepository.findByName("TESTCHAR")).thenReturn(Optional.of(c));
            when(equipmentRepository.findByWowCharacter(c)).thenReturn(List.of());

            LocalDate start = LocalDate.now().plusDays(1);
            when(twEventRepository.findByStartDateGreaterThanEqualOrderByStartDateAsc(any()))
                    .thenReturn(List.of(event("Burning Crusade", start, start.plusDays(6))));

            when(armorPieceRepository.findDistinctSlotsByExpansionAndArmorTypes(eq("Burning Crusade"), any()))
                    .thenReturn(List.of("Head"));
            when(armorPieceRepository.findByExpansionAndSlotAndArmorTypeIn(eq("Burning Crusade"), eq("Head"), any()))
                    .thenReturn(List.of(armor("BC Helm", "Head", "Plate", "Strength", 150)));
            when(weaponRepository.findByExpansion("Burning Crusade")).thenReturn(List.of());

            GearPlanResponseDTO result = gearPlanService.computeGearPlan("testchar", null);

            assertThat(result.fullyEquipped()).isFalse();
            assertThat(result.fullyEquippedDate()).isNull();
            assertThat(result.unresolvableSlots()).doesNotContain("HEAD");
            assertThat(result.unresolvableSlots()).contains("NECK", "CHEST", "MAIN_HAND", "OFF_HAND");
        }

        @Test
        @DisplayName("event with no compatible gear for this class produces no event entry")
        void eventWithNoCompatibleGearProducesNoEntry() {
            WowCharacter c = character(WowClass.WARRIOR); // resolvedStat = Strength
            when(characterRepository.findByName("TESTCHAR")).thenReturn(Optional.of(c));
            when(equipmentRepository.findByWowCharacter(c)).thenReturn(List.of());

            LocalDate start = LocalDate.now().plusDays(1);
            when(twEventRepository.findByStartDateGreaterThanEqualOrderByStartDateAsc(any()))
                    .thenReturn(List.of(event("Burning Crusade", start, start.plusDays(6))));

            // The event only has Intellect and Cloth gear — nothing useful for a Warrior
            when(armorPieceRepository.findDistinctSlotsByExpansionAndArmorTypes(eq("Burning Crusade"), any()))
                    .thenReturn(List.of("Head"));
            when(armorPieceRepository.findByExpansionAndSlotAndArmorTypeIn(eq("Burning Crusade"), eq("Head"), any()))
                    .thenReturn(List.of(armor("Cloth Intellect Helm", "Head", "Plate", "Intellect", 150)));
            when(weaponRepository.findByExpansion("Burning Crusade")).thenReturn(List.of());

            GearPlanResponseDTO result = gearPlanService.computeGearPlan("testchar", null);

            assertThat(result.events()).isEmpty();
            assertThat(result.unresolvableSlots()).isNotEmpty();
        }
    }

    // -----------------------------------------------------------------------
    // Already Equipped Item Exclusion
    // -----------------------------------------------------------------------

    @Nested
    @DisplayName("Already equipped item exclusion")
    class AlreadyEquippedExclusion {

        @Test
        @DisplayName("item already worn by the character is not re-suggested in the plan")
        void alreadyEquippedItemNotSuggestedAgain() {
            WowCharacter c = character(WowClass.WARRIOR);
            // Character has a helm already — HEAD is filled and should not appear in any event
            ArmorPiece existingHelm = armor("Gladiator Helm", "Head", "Plate", "Strength", 150);
            when(characterRepository.findByName("TESTCHAR")).thenReturn(Optional.of(c));
            when(equipmentRepository.findByWowCharacter(c))
                    .thenReturn(List.of(equippedWithArmor(c, EquipmentSlot.HEAD, existingHelm)));

            LocalDate start = LocalDate.now().plusDays(1);
            when(twEventRepository.findByStartDateGreaterThanEqualOrderByStartDateAsc(any()))
                    .thenReturn(List.of(event("Burning Crusade", start, start.plusDays(6))));

            // Event offers a Neck piece (HEAD is already filled so it won't be in unfilled)
            when(armorPieceRepository.findDistinctSlotsByExpansionAndArmorTypes(eq("Burning Crusade"), any()))
                    .thenReturn(List.of("Neck"));
            when(armorPieceRepository.findByExpansionAndSlotAndArmorTypeIn(eq("Burning Crusade"), eq("Neck"), any()))
                    .thenReturn(List.of(armor("BC Neck", "Neck", "Agnostic", null, 100)));
            when(weaponRepository.findByExpansion("Burning Crusade")).thenReturn(List.of());

            GearPlanResponseDTO result = gearPlanService.computeGearPlan("testchar", null);

            assertThat(result.alreadyEquippedSlots()).contains("HEAD");
            assertThat(result.events().get(0).slots())
                    .extracting(s -> s.slot())
                    .doesNotContain("HEAD")
                    .contains("NECK");
        }

        @Test
        @DisplayName("item name exclusion check is case-insensitive — differently-cased duplicate is skipped")
        void equippedItemNameCheckIsCaseInsensitive() {
            WowCharacter c = character(WowClass.WARRIOR);
            // FINGER_1 is equipped; FINGER_2 is the only unfilled armor slot
            ArmorPiece ring1 = armor("Gladiator's Ring", "Finger", "Agnostic", null, 100);
            List<CharacterEquipment> slots = new java.util.ArrayList<>();
            slots.add(equippedWithArmor(c, EquipmentSlot.HEAD,      armor("Helm",   "Head",      "Plate",    "Strength", 100)));
            slots.add(equippedWithArmor(c, EquipmentSlot.NECK,      armor("Neck",   "Neck",      "Agnostic", null,        100)));
            slots.add(equippedWithArmor(c, EquipmentSlot.SHOULDERS, armor("Shldr",  "Shoulders", "Plate",    "Strength", 100)));
            slots.add(equippedWithArmor(c, EquipmentSlot.BACK,      armor("Cape",   "Back",      "Agnostic", null,        100)));
            slots.add(equippedWithArmor(c, EquipmentSlot.CHEST,     armor("Chest",  "Chest",     "Plate",    "Strength", 100)));
            slots.add(equippedWithArmor(c, EquipmentSlot.WRIST,     armor("Wrist",  "Wrist",     "Plate",    "Strength", 100)));
            slots.add(equippedWithArmor(c, EquipmentSlot.HANDS,     armor("Hands",  "Hands",     "Plate",    "Strength", 100)));
            slots.add(equippedWithArmor(c, EquipmentSlot.WAIST,     armor("Belt",   "Waist",     "Plate",    "Strength", 100)));
            slots.add(equippedWithArmor(c, EquipmentSlot.LEGS,      armor("Legs",   "Legs",      "Plate",    "Strength", 100)));
            slots.add(equippedWithArmor(c, EquipmentSlot.FEET,      armor("Boots",  "Feet",      "Plate",    "Strength", 100)));
            slots.add(equippedWithArmor(c, EquipmentSlot.FINGER_1,  ring1));
            slots.add(equippedWithArmor(c, EquipmentSlot.TRINKET_1, armor("Trink1", "Trinket",   "Agnostic", null,        100)));
            slots.add(equippedWithArmor(c, EquipmentSlot.TRINKET_2, armor("Trink2", "Trinket",   "Agnostic", null,        100)));
            slots.add(equippedWithWeapon(c, EquipmentSlot.MAIN_HAND, weapon("Sword",  "1H",       "Sword",  "Strength", 200)));
            slots.add(equippedWithWeapon(c, EquipmentSlot.OFF_HAND,  weapon("Shield", "Off-Hand", "Shield", "Strength", 150)));

            when(characterRepository.findByName("TESTCHAR")).thenReturn(Optional.of(c));
            when(equipmentRepository.findByWowCharacter(c)).thenReturn(slots);

            LocalDate start = LocalDate.now().plusDays(1);
            when(twEventRepository.findByStartDateGreaterThanEqualOrderByStartDateAsc(any()))
                    .thenReturn(List.of(event("Burning Crusade", start, start.plusDays(6))));

            // Event offers the same ring in ALL-CAPS (should be excluded) then a different ring
            ArmorPiece sameRingUpperCase = armor("GLADIATOR'S RING", "Finger", "Agnostic", null, 100);
            ArmorPiece differentRing     = armor("Veteran's Band",   "Finger", "Agnostic", null, 75);
            when(armorPieceRepository.findDistinctSlotsByExpansionAndArmorTypes(eq("Burning Crusade"), any()))
                    .thenReturn(List.of("Finger"));
            when(armorPieceRepository.findByExpansionAndSlotAndArmorTypeIn(eq("Burning Crusade"), eq("Finger"), any()))
                    .thenReturn(List.of(sameRingUpperCase, differentRing));
            when(weaponRepository.findByExpansion("Burning Crusade")).thenReturn(List.of());

            GearPlanResponseDTO result = gearPlanService.computeGearPlan("testchar", null);

            // The ALL-CAPS duplicate should be skipped; Veteran's Band fills FINGER_2
            assertThat(result.fullyEquipped()).isTrue();
            assertThat(result.events()).hasSize(1);
            assertThat(result.events().get(0).slots().get(0).slot()).isEqualTo("FINGER_2");
            assertThat(result.events().get(0).slots().get(0).itemName()).isEqualTo("Veteran's Band");
        }
    }
}
