package com.wow.timewalkers.service;

import com.wow.timewalkers.dto.*;
import com.wow.timewalkers.entity.ArmorPiece;
import com.wow.timewalkers.entity.CharacterEquipment;
import com.wow.timewalkers.entity.Weapon;
import com.wow.timewalkers.entity.WowCharacter;
import com.wow.timewalkers.enums.EquipmentSlot;
import com.wow.timewalkers.enums.ItemType;
import com.wow.timewalkers.enums.WowClass;
import com.wow.timewalkers.enums.WowGender;
import com.wow.timewalkers.enums.WowRace;
import com.wow.timewalkers.exception.CharacterNameConflictException;
import com.wow.timewalkers.exception.CharacterNotFoundException;
import com.wow.timewalkers.exception.GearValidationException;
import com.wow.timewalkers.exception.InvalidRaceClassCombinationException;
import com.wow.timewalkers.mapper.CharacterMapper;
import com.wow.timewalkers.mapper.GearMapper;
import com.wow.timewalkers.repository.ArmorPieceRepository;
import com.wow.timewalkers.repository.CharacterEquipmentRepository;
import com.wow.timewalkers.repository.CharacterRepository;
import com.wow.timewalkers.repository.WeaponRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class CharacterServiceTest {

    @Mock private CharacterRepository characterRepository;
    @Mock private CharacterEquipmentRepository equipmentRepository;
    @Mock private ArmorPieceRepository armorPieceRepository;
    @Mock private WeaponRepository weaponRepository;

    private CharacterService characterService;

    @BeforeEach
    void setUp() {
        // Use real mapper and validator — they have no external dependencies
        GearMapper gearMapper = new GearMapper();
        CharacterMapper characterMapper = new CharacterMapper(gearMapper);
        GearValidator gearValidator = new GearValidator();
        CharacterValidator characterValidator = new CharacterValidator();
        characterService = new CharacterService(
                characterRepository, equipmentRepository,
                armorPieceRepository, weaponRepository,
                characterMapper, gearValidator, characterValidator);
    }

    // -----------------------------------------------------------------------
    // Helpers
    // -----------------------------------------------------------------------

    private WowCharacter character(WowClass wowClass) {
        WowCharacter c = new WowCharacter();
        c.setName("JARAXXUS");
        c.setRace(WowRace.NIGHT_ELF);
        c.setCharacterClass(wowClass);
        return c;
    }

    private ArmorPiece armorPiece(String name, String armorType) {
        ArmorPiece ap = new ArmorPiece();
        ap.setName(name);
        ap.setArmorType(armorType);
        ap.setSlot("Head");
        ap.setExpansion("Classic");
        ap.setCost(0);
        return ap;
    }

    private ArmorPiece agnosticArmor(String name) {
        ArmorPiece ap = new ArmorPiece();
        ap.setName(name);
        ap.setArmorType("Agnostic");
        ap.setSlot("Finger");
        ap.setExpansion("Classic");
        ap.setCost(0);
        return ap;
    }

    private Weapon weapon(String name, String weaponSlot, String weaponType) {
        Weapon w = new Weapon();
        w.setName(name);
        w.setWeaponSlot(weaponSlot);
        w.setWeaponStat("Agility");
        w.setWeaponType(weaponType);
        w.setExpansion("Classic");
        w.setCost(0);
        return w;
    }

    private EquipSlotRequest slotRequest(EquipmentSlot slot, String itemName) {
        return new EquipSlotRequest(slot, itemName);
    }

    // -----------------------------------------------------------------------
    // getAllCharacters
    // -----------------------------------------------------------------------

    @Nested
    @DisplayName("getAllCharacters")
    class GetAllCharacters {

        @Test
        @DisplayName("Returns a summary DTO for each character in the repository")
        void returnsAllCharacters() {
            WowCharacter c1 = character(WowClass.DEMON_HUNTER);
            WowCharacter c2 = character(WowClass.WARRIOR);
            c2.setName("TESTCHAR");
            c2.setRace(WowRace.ORC);
            when(characterRepository.findAll()).thenReturn(List.of(c1, c2));

            List<CharacterSummaryDTO> result = characterService.getAllCharacters();

            assertThat(result).hasSize(2);
            assertThat(result.get(0).name()).isEqualTo("JARAXXUS");
            assertThat(result.get(0).characterClass()).isEqualTo(WowClass.DEMON_HUNTER);
            assertThat(result.get(0).race()).isEqualTo(WowRace.NIGHT_ELF);
            assertThat(result.get(1).name()).isEqualTo("TESTCHAR");
            assertThat(result.get(1).race()).isEqualTo(WowRace.ORC);
        }

        @Test
        @DisplayName("Returns empty list when no characters exist")
        void returnsEmptyList() {
            when(characterRepository.findAll()).thenReturn(List.of());

            assertThat(characterService.getAllCharacters()).isEmpty();
        }
    }

    // -----------------------------------------------------------------------
    // createCharacter
    // -----------------------------------------------------------------------

    @Nested
    @DisplayName("createCharacter")
    class CreateCharacter {

        @Test
        @DisplayName("Creates and returns a character with empty equipment")
        void createsCharacter() {
            when(characterRepository.existsByName("JARAXXUS")).thenReturn(false);
            when(characterRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));

            CharacterDTO result = characterService.createCharacter(
                    new CreateCharacterRequest("jaraxxus", WowRace.NIGHT_ELF, WowClass.DEMON_HUNTER, WowGender.MALE));

            assertThat(result.name()).isEqualTo("JARAXXUS");
            assertThat(result.race()).isEqualTo(WowRace.NIGHT_ELF);
            assertThat(result.characterClass()).isEqualTo(WowClass.DEMON_HUNTER);
            assertThat(result.equipment()).hasSize(16);
            assertThat(result.equipment()).allSatisfy(slot -> assertThat(slot.equipped()).isFalse());
        }

        @Test
        @DisplayName("Normalizes name to uppercase before saving")
        void normalizesNameToUppercase() {
            when(characterRepository.existsByName("JARAXXUS")).thenReturn(false);
            when(characterRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));

            characterService.createCharacter(
                    new CreateCharacterRequest("jaraxxus", WowRace.NIGHT_ELF, WowClass.DEMON_HUNTER, WowGender.MALE));

            // Verify existsByName was called with the uppercased version
            verify(characterRepository).existsByName("JARAXXUS");
        }

        @Test
        @DisplayName("Throws CharacterNameConflictException when name already exists")
        void throwsConflictWhenNameExists() {
            when(characterRepository.existsByName("JARAXXUS")).thenReturn(true);

            assertThatThrownBy(() -> characterService.createCharacter(
                    new CreateCharacterRequest("jaraxxus", WowRace.NIGHT_ELF, WowClass.DEMON_HUNTER, WowGender.MALE)))
                    .isInstanceOf(CharacterNameConflictException.class)
                    .hasMessageContaining("JARAXXUS");

            verify(characterRepository, never()).save(any());
        }

        @Test
        @DisplayName("Throws InvalidRaceClassCombinationException for invalid race/class combo")
        void throwsInvalidRaceClassForBadCombo() {
            // Human cannot be an Evoker
            assertThatThrownBy(() -> characterService.createCharacter(
                    new CreateCharacterRequest("jaraxxus", WowRace.HUMAN, WowClass.EVOKER, WowGender.MALE)))
                    .isInstanceOf(InvalidRaceClassCombinationException.class)
                    .hasMessageContaining("Human")
                    .hasMessageContaining("Evoker");

            verify(characterRepository, never()).existsByName(any());
            verify(characterRepository, never()).save(any());
        }

        @Test
        @DisplayName("Race/class validation runs before name conflict check")
        void raceClassValidationRunsBeforeNameCheck() {
            // Even if the name existed, we'd see the race/class error first
            assertThatThrownBy(() -> characterService.createCharacter(
                    new CreateCharacterRequest("jaraxxus", WowRace.DRACTHYR, WowClass.PALADIN, WowGender.MALE)))
                    .isInstanceOf(InvalidRaceClassCombinationException.class);

            verify(characterRepository, never()).existsByName(any());
        }
    }

    // -----------------------------------------------------------------------
    // getCharacter
    // -----------------------------------------------------------------------

    @Nested
    @DisplayName("getCharacter")
    class GetCharacter {

        @Test
        @DisplayName("Returns character DTO with current equipment")
        void returnsCharacterWithEquipment() {
            WowCharacter c = character(WowClass.DEMON_HUNTER);
            when(characterRepository.findByName("JARAXXUS")).thenReturn(Optional.of(c));
            when(equipmentRepository.findByWowCharacter(c)).thenReturn(List.of());

            CharacterDTO result = characterService.getCharacter("jaraxxus");

            assertThat(result.name()).isEqualTo("JARAXXUS");
            assertThat(result.equipment()).hasSize(16);
        }

        @Test
        @DisplayName("Uppercases name before lookup")
        void uppercasesName() {
            WowCharacter c = character(WowClass.DEMON_HUNTER);
            when(characterRepository.findByName("JARAXXUS")).thenReturn(Optional.of(c));
            when(equipmentRepository.findByWowCharacter(c)).thenReturn(List.of());

            characterService.getCharacter("jaraxxus");

            verify(characterRepository).findByName("JARAXXUS");
        }

        @Test
        @DisplayName("Throws CharacterNotFoundException when character does not exist")
        void throwsNotFound() {
            when(characterRepository.findByName("UNKNOWN")).thenReturn(Optional.empty());

            assertThatThrownBy(() -> characterService.getCharacter("unknown"))
                    .isInstanceOf(CharacterNotFoundException.class)
                    .hasMessageContaining("UNKNOWN");
        }
    }

    // -----------------------------------------------------------------------
    // equipGear
    // -----------------------------------------------------------------------

    @Nested
    @DisplayName("equipGear")
    class EquipGear {

        @Test
        @DisplayName("Item not found in DB is not saved and character is returned unchanged")
        void itemNotFoundInDbIsNotSaved() {
            WowCharacter c = character(WowClass.DEMON_HUNTER);
            when(characterRepository.findByName("JARAXXUS")).thenReturn(Optional.of(c));
            when(armorPieceRepository.findByNameIgnoreCase("Ghost Helm")).thenReturn(Optional.empty());
            when(equipmentRepository.findByWowCharacter(c)).thenReturn(List.of());

            EquipResponseDTO result = characterService.equipGear("JARAXXUS",
                    new EquipRequest(List.of(slotRequest(EquipmentSlot.HEAD, "Ghost Helm"))));

            assertThat(result.character().name()).isEqualTo("JARAXXUS");
            assertThat(result.equipped()).isEmpty();
            assertThat(result.notFound())
                    .containsExactly(new NotFoundSlotDTO(EquipmentSlot.HEAD, "Ghost Helm"));
            verify(equipmentRepository, never()).save(any());
        }

        @Test
        @DisplayName("Valid armor for class is saved and character is returned")
        void validArmorIsEquipped() {
            WowCharacter c = character(WowClass.DEMON_HUNTER);
            ArmorPiece leatherHelm = armorPiece("Leather Helm", "Leather");

            when(characterRepository.findByName("JARAXXUS")).thenReturn(Optional.of(c));
            when(armorPieceRepository.findByNameIgnoreCase("Leather Helm")).thenReturn(Optional.of(leatherHelm));
            // Service always checks MAIN_HAND for the 2H-conflict check, even in armor-only requests
            when(equipmentRepository.findByWowCharacterAndSlot(c, EquipmentSlot.MAIN_HAND)).thenReturn(Optional.empty());
            when(equipmentRepository.findByWowCharacterAndSlot(c, EquipmentSlot.HEAD)).thenReturn(Optional.empty());
            when(equipmentRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));
            when(equipmentRepository.findByWowCharacter(c)).thenReturn(List.of());

            EquipResponseDTO result = characterService.equipGear("JARAXXUS",
                    new EquipRequest(List.of(slotRequest(EquipmentSlot.HEAD, "Leather Helm"))));

            assertThat(result.character().name()).isEqualTo("JARAXXUS");
            verify(equipmentRepository).save(any());
        }

        @Test
        @DisplayName("Wrong armor type for class throws GearValidationException")
        void wrongArmorTypeThrowsValidationException() {
            WowCharacter c = character(WowClass.DEMON_HUNTER); // Leather only
            ArmorPiece plateHelm = armorPiece("Plate Helm", "Plate");

            when(characterRepository.findByName("JARAXXUS")).thenReturn(Optional.of(c));
            when(armorPieceRepository.findByNameIgnoreCase("Plate Helm")).thenReturn(Optional.of(plateHelm));

            assertThatThrownBy(() -> characterService.equipGear("JARAXXUS",
                    new EquipRequest(List.of(slotRequest(EquipmentSlot.HEAD, "Plate Helm")))))
                    .isInstanceOf(GearValidationException.class)
                    .hasMessageContaining("Armor type not allowed");

            verify(equipmentRepository, never()).save(any());
        }

        @Test
        @DisplayName("Mail armor on a Cloth class throws GearValidationException")
        void mailArmorOnClothClassRejected() {
            WowCharacter c = character(WowClass.MAGE);
            ArmorPiece mailHelm = armorPiece("Mail Helm", "Mail");

            when(characterRepository.findByName("JARAXXUS")).thenReturn(Optional.of(c));
            when(armorPieceRepository.findByNameIgnoreCase("Mail Helm")).thenReturn(Optional.of(mailHelm));

            assertThatThrownBy(() -> characterService.equipGear("JARAXXUS",
                    new EquipRequest(List.of(slotRequest(EquipmentSlot.HEAD, "Mail Helm")))))
                    .isInstanceOf(GearValidationException.class);
        }

        @Test
        @DisplayName("Agnostic armor (ring, neck) is allowed for any class")
        void agnosticArmorAllowedForAnyClass() {
            WowCharacter c = character(WowClass.MAGE); // Cloth only, but agnostic bypasses this
            ArmorPiece ring = agnosticArmor("Mightstone Ring");

            when(characterRepository.findByName("JARAXXUS")).thenReturn(Optional.of(c));
            when(armorPieceRepository.findByNameIgnoreCase("Mightstone Ring")).thenReturn(Optional.of(ring));
            when(equipmentRepository.findByWowCharacterAndSlot(c, EquipmentSlot.MAIN_HAND)).thenReturn(Optional.empty());
            // Uniqueness check reads the other paired slot (FINGER_2) to detect duplicate unique items
            when(equipmentRepository.findByWowCharacterAndSlot(c, EquipmentSlot.FINGER_2)).thenReturn(Optional.empty());
            when(equipmentRepository.findByWowCharacterAndSlot(c, EquipmentSlot.FINGER_1)).thenReturn(Optional.empty());
            when(equipmentRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));
            when(equipmentRepository.findByWowCharacter(c)).thenReturn(List.of());

            EquipResponseDTO result = characterService.equipGear("JARAXXUS",
                    new EquipRequest(List.of(slotRequest(EquipmentSlot.FINGER_1, "Mightstone Ring"))));

            assertThat(result.character().name()).isEqualTo("JARAXXUS");
            verify(equipmentRepository).save(any());
        }

        @Test
        @DisplayName("Valid 1H weapon for class is equipped in main hand")
        void validWeaponEquipped() {
            WowCharacter c = character(WowClass.DEMON_HUNTER);
            Weapon axe = weapon("Axe of Azzinoth", "1H", "Axe");

            when(characterRepository.findByName("JARAXXUS")).thenReturn(Optional.of(c));
            when(weaponRepository.findByNameIgnoreCase("Axe of Azzinoth")).thenReturn(Optional.of(axe));
            when(equipmentRepository.findByWowCharacterAndSlot(c, EquipmentSlot.MAIN_HAND)).thenReturn(Optional.empty());
            when(equipmentRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));
            when(equipmentRepository.findByWowCharacter(c)).thenReturn(List.of());

            EquipResponseDTO result = characterService.equipGear("JARAXXUS",
                    new EquipRequest(List.of(slotRequest(EquipmentSlot.MAIN_HAND, "Axe of Azzinoth"))));

            assertThat(result.character().name()).isEqualTo("JARAXXUS");
            verify(equipmentRepository).save(any());
        }

        @Test
        @DisplayName("Weapon type not allowed for class throws GearValidationException")
        void invalidWeaponTypeForClassRejected() {
            WowCharacter c = character(WowClass.DEMON_HUNTER); // no Staves
            Weapon staff = weapon("Big Staff", "2H", "Staff");

            when(characterRepository.findByName("JARAXXUS")).thenReturn(Optional.of(c));
            when(weaponRepository.findByNameIgnoreCase("Big Staff")).thenReturn(Optional.of(staff));

            assertThatThrownBy(() -> characterService.equipGear("JARAXXUS",
                    new EquipRequest(List.of(slotRequest(EquipmentSlot.MAIN_HAND, "Big Staff")))))
                    .isInstanceOf(GearValidationException.class)
                    .hasMessageContaining("Weapon not allowed");
        }

        @Test
        @DisplayName("Ranged weapon for non-Hunter throws GearValidationException")
        void rangedByNonHunterRejected() {
            WowCharacter c = character(WowClass.WARRIOR);
            Weapon bow = weapon("Bow of Light", "Ranged", "Bow");

            when(characterRepository.findByName("JARAXXUS")).thenReturn(Optional.of(c));
            when(weaponRepository.findByNameIgnoreCase("Bow of Light")).thenReturn(Optional.of(bow));

            assertThatThrownBy(() -> characterService.equipGear("JARAXXUS",
                    new EquipRequest(List.of(slotRequest(EquipmentSlot.MAIN_HAND, "Bow of Light")))))
                    .isInstanceOf(GearValidationException.class);
        }

        @Test
        @DisplayName("Off-Hand-slot item placed in MAIN_HAND slot throws GearValidationException")
        void offhandItemInMainHandRejected() {
            WowCharacter c = character(WowClass.WARRIOR);
            Weapon shield = weapon("Shield of the Ages", "Off-Hand", "Shield");

            when(characterRepository.findByName("JARAXXUS")).thenReturn(Optional.of(c));
            when(weaponRepository.findByNameIgnoreCase("Shield of the Ages")).thenReturn(Optional.of(shield));

            assertThatThrownBy(() -> characterService.equipGear("JARAXXUS",
                    new EquipRequest(List.of(slotRequest(EquipmentSlot.MAIN_HAND, "Shield of the Ages")))))
                    .isInstanceOf(GearValidationException.class);
        }

        @Test
        @DisplayName("2H weapon + off-hand in the same request throws GearValidationException")
        void twoHandedPlusOffhandInSameRequestRejected() {
            WowCharacter c = character(WowClass.WARRIOR);
            Weapon twoHander = weapon("Ashkandi", "2H", "Sword");
            Weapon shield = weapon("War Shield", "Off-Hand", "Shield");

            when(characterRepository.findByName("JARAXXUS")).thenReturn(Optional.of(c));
            when(weaponRepository.findByNameIgnoreCase("Ashkandi")).thenReturn(Optional.of(twoHander));
            when(weaponRepository.findByNameIgnoreCase("War Shield")).thenReturn(Optional.of(shield));

            assertThatThrownBy(() -> characterService.equipGear("JARAXXUS",
                    new EquipRequest(List.of(
                            slotRequest(EquipmentSlot.MAIN_HAND, "Ashkandi"),
                            slotRequest(EquipmentSlot.OFF_HAND, "War Shield")))))
                    .isInstanceOf(GearValidationException.class)
                    .hasMessageContaining("two-handed");
        }

        @Test
        @DisplayName("Equipping off-hand when character already has a 2H equipped throws GearValidationException")
        void offHandWithExisting2HRejected() {
            WowCharacter c = character(WowClass.WARRIOR);
            Weapon shield = weapon("War Shield", "Off-Hand", "Shield");

            // Simulate an existing 2H in MAIN_HAND in the DB
            Weapon existing2H = weapon("Ashkandi", "2H", "Sword");
            CharacterEquipment mainHandEquip = new CharacterEquipment();
            mainHandEquip.setSlot(EquipmentSlot.MAIN_HAND);
            mainHandEquip.setItemType(ItemType.WEAPON);
            mainHandEquip.setWeapon(existing2H);

            when(characterRepository.findByName("JARAXXUS")).thenReturn(Optional.of(c));
            when(weaponRepository.findByNameIgnoreCase("War Shield")).thenReturn(Optional.of(shield));
            when(equipmentRepository.findByWowCharacterAndSlot(c, EquipmentSlot.MAIN_HAND))
                    .thenReturn(Optional.of(mainHandEquip));

            assertThatThrownBy(() -> characterService.equipGear("JARAXXUS",
                    new EquipRequest(List.of(slotRequest(EquipmentSlot.OFF_HAND, "War Shield")))))
                    .isInstanceOf(GearValidationException.class)
                    .hasMessageContaining("two-handed");
        }

        @Test
        @DisplayName("Equipping a 2H weapon auto-clears the existing off-hand slot")
        void equipping2HClearsOffHand() {
            WowCharacter c = character(WowClass.WARRIOR);
            Weapon twoHander = weapon("Ashkandi", "2H", "Sword");

            when(characterRepository.findByName("JARAXXUS")).thenReturn(Optional.of(c));
            when(weaponRepository.findByNameIgnoreCase("Ashkandi")).thenReturn(Optional.of(twoHander));
            when(equipmentRepository.findByWowCharacterAndSlot(c, EquipmentSlot.MAIN_HAND)).thenReturn(Optional.empty());
            when(equipmentRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));
            when(equipmentRepository.findByWowCharacter(c)).thenReturn(List.of());

            characterService.equipGear("JARAXXUS",
                    new EquipRequest(List.of(slotRequest(EquipmentSlot.MAIN_HAND, "Ashkandi"))));

            // OFF_HAND should be force-cleared after equipping a 2H
            verify(equipmentRepository).deleteByWowCharacterAndSlotIn(c, List.of(EquipmentSlot.OFF_HAND));
        }

        @Test
        @DisplayName("Non-dual-wield class cannot equip 1H in off-hand")
        void nonDualWieldCannotOffHandOneHander() {
            WowCharacter c = character(WowClass.PALADIN); // no dual wield
            Weapon sword = weapon("Short Sword", "1H", "Sword");

            when(characterRepository.findByName("JARAXXUS")).thenReturn(Optional.of(c));
            when(weaponRepository.findByNameIgnoreCase("Short Sword")).thenReturn(Optional.of(sword));

            assertThatThrownBy(() -> characterService.equipGear("JARAXXUS",
                    new EquipRequest(List.of(slotRequest(EquipmentSlot.OFF_HAND, "Short Sword")))))
                    .isInstanceOf(GearValidationException.class)
                    .hasMessageContaining("Weapon not allowed");
        }

        @Test
        @DisplayName("Dual-wield class can equip 1H in off-hand")
        void dualWieldCanOffHandOneHander() {
            WowCharacter c = character(WowClass.ROGUE);
            Weapon dagger = weapon("Poisoned Dagger", "1H", "Dagger");

            when(characterRepository.findByName("JARAXXUS")).thenReturn(Optional.of(c));
            when(weaponRepository.findByNameIgnoreCase("Poisoned Dagger")).thenReturn(Optional.of(dagger));
            when(equipmentRepository.findByWowCharacterAndSlot(c, EquipmentSlot.MAIN_HAND)).thenReturn(Optional.empty());
            when(equipmentRepository.findByWowCharacterAndSlot(c, EquipmentSlot.OFF_HAND)).thenReturn(Optional.empty());
            when(equipmentRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));
            when(equipmentRepository.findByWowCharacter(c)).thenReturn(List.of());

            EquipResponseDTO result = characterService.equipGear("JARAXXUS",
                    new EquipRequest(List.of(slotRequest(EquipmentSlot.OFF_HAND, "Poisoned Dagger"))));

            assertThat(result.character().name()).isEqualTo("JARAXXUS");
            verify(equipmentRepository).save(any());
        }

        @Test
        @DisplayName("Shield can be equipped by Paladin in off-hand")
        void shieldEquippedByPaladin() {
            WowCharacter c = character(WowClass.PALADIN);
            Weapon shield = weapon("Holy Shield", "Off-Hand", "Shield");

            when(characterRepository.findByName("JARAXXUS")).thenReturn(Optional.of(c));
            when(weaponRepository.findByNameIgnoreCase("Holy Shield")).thenReturn(Optional.of(shield));
            when(equipmentRepository.findByWowCharacterAndSlot(c, EquipmentSlot.MAIN_HAND)).thenReturn(Optional.empty());
            when(equipmentRepository.findByWowCharacterAndSlot(c, EquipmentSlot.OFF_HAND)).thenReturn(Optional.empty());
            when(equipmentRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));
            when(equipmentRepository.findByWowCharacter(c)).thenReturn(List.of());

            EquipResponseDTO result = characterService.equipGear("JARAXXUS",
                    new EquipRequest(List.of(slotRequest(EquipmentSlot.OFF_HAND, "Holy Shield"))));

            assertThat(result.character().name()).isEqualTo("JARAXXUS");
            verify(equipmentRepository).save(any());
        }

        @Test
        @DisplayName("Shield cannot be equipped by Demon Hunter")
        void shieldRejectedForDH() {
            WowCharacter c = character(WowClass.DEMON_HUNTER);
            Weapon shield = weapon("Holy Shield", "Off-Hand", "Shield");

            when(characterRepository.findByName("JARAXXUS")).thenReturn(Optional.of(c));
            when(weaponRepository.findByNameIgnoreCase("Holy Shield")).thenReturn(Optional.of(shield));

            assertThatThrownBy(() -> characterService.equipGear("JARAXXUS",
                    new EquipRequest(List.of(slotRequest(EquipmentSlot.OFF_HAND, "Holy Shield")))))
                    .isInstanceOf(GearValidationException.class);
        }

        @Test
        @DisplayName("Held In Off-hand frill accepted by Mage")
        void frillAcceptedByMage() {
            WowCharacter c = character(WowClass.MAGE);
            Weapon tome = weapon("Tome of Arcane Power", "Off-Hand", "Held In Off-hand");

            when(characterRepository.findByName("JARAXXUS")).thenReturn(Optional.of(c));
            when(weaponRepository.findByNameIgnoreCase("Tome of Arcane Power")).thenReturn(Optional.of(tome));
            when(equipmentRepository.findByWowCharacterAndSlot(c, EquipmentSlot.MAIN_HAND)).thenReturn(Optional.empty());
            when(equipmentRepository.findByWowCharacterAndSlot(c, EquipmentSlot.OFF_HAND)).thenReturn(Optional.empty());
            when(equipmentRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));
            when(equipmentRepository.findByWowCharacter(c)).thenReturn(List.of());

            EquipResponseDTO result = characterService.equipGear("JARAXXUS",
                    new EquipRequest(List.of(slotRequest(EquipmentSlot.OFF_HAND, "Tome of Arcane Power"))));

            assertThat(result.character().name()).isEqualTo("JARAXXUS");
            verify(equipmentRepository).save(any());
        }

        @Test
        @DisplayName("Partial request: found item equipped, not-found item in notFound list")
        void partialRequestMixedResults() {
            WowCharacter c = character(WowClass.DEMON_HUNTER);
            ArmorPiece leatherHelm = armorPiece("Leather Helm", "Leather");

            when(characterRepository.findByName("JARAXXUS")).thenReturn(Optional.of(c));
            when(armorPieceRepository.findByNameIgnoreCase("Leather Helm")).thenReturn(Optional.of(leatherHelm));
            when(armorPieceRepository.findByNameIgnoreCase("Nonexistent Chest")).thenReturn(Optional.empty());
            when(equipmentRepository.findByWowCharacterAndSlot(c, EquipmentSlot.MAIN_HAND)).thenReturn(Optional.empty());
            when(equipmentRepository.findByWowCharacterAndSlot(c, EquipmentSlot.HEAD)).thenReturn(Optional.empty());
            when(equipmentRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));
            when(equipmentRepository.findByWowCharacter(c)).thenReturn(List.of());

            EquipResponseDTO result = characterService.equipGear("JARAXXUS",
                    new EquipRequest(List.of(
                            slotRequest(EquipmentSlot.HEAD, "Leather Helm"),
                            slotRequest(EquipmentSlot.CHEST, "Nonexistent Chest"))));

            assertThat(result.character().name()).isEqualTo("JARAXXUS");
            assertThat(result.equipped()).containsExactly(EquipmentSlot.HEAD);
            assertThat(result.notFound())
                    .containsExactly(new NotFoundSlotDTO(EquipmentSlot.CHEST, "Nonexistent Chest"));
            // HEAD was found and saved; CHEST was not found so save is called exactly once
            verify(equipmentRepository, times(1)).save(any());
        }

        @Test
        @DisplayName("Throws CharacterNotFoundException when character does not exist")
        void throwsNotFoundForMissingCharacter() {
            when(characterRepository.findByName("NOBODY")).thenReturn(Optional.empty());

            assertThatThrownBy(() -> characterService.equipGear("nobody",
                    new EquipRequest(List.of())))
                    .isInstanceOf(CharacterNotFoundException.class);
        }
    }

    // -----------------------------------------------------------------------
    // unequipGear
    // -----------------------------------------------------------------------

    @Nested
    @DisplayName("unequipGear")
    class UnequipGear {

        @Test
        @DisplayName("Unequipping calls deleteByWowCharacterAndSlotIn with correct slots")
        void unequipCallsDelete() {
            WowCharacter c = character(WowClass.DEMON_HUNTER);
            when(characterRepository.findByName("JARAXXUS")).thenReturn(Optional.of(c));
            when(equipmentRepository.findByWowCharacter(c)).thenReturn(List.of());

            characterService.unequipGear("JARAXXUS",
                    new UnequipRequest(List.of(EquipmentSlot.HEAD, EquipmentSlot.CHEST)));

            ArgumentCaptor<List> slotsCaptor = ArgumentCaptor.forClass(List.class);
            verify(equipmentRepository).deleteByWowCharacterAndSlotIn(eq(c), slotsCaptor.capture());
            assertThat(slotsCaptor.getValue())
                    .containsExactlyInAnyOrder(EquipmentSlot.HEAD, EquipmentSlot.CHEST);
        }

        @Test
        @DisplayName("Returns updated character DTO after unequipping")
        void returnsUpdatedCharacterAfterUnequip() {
            WowCharacter c = character(WowClass.DEMON_HUNTER);
            when(characterRepository.findByName("JARAXXUS")).thenReturn(Optional.of(c));
            when(equipmentRepository.findByWowCharacter(c)).thenReturn(List.of());

            CharacterDTO result = characterService.unequipGear("JARAXXUS",
                    new UnequipRequest(List.of(EquipmentSlot.HEAD)));

            assertThat(result.name()).isEqualTo("JARAXXUS");
            assertThat(result.equipment()).hasSize(16);
        }

        @Test
        @DisplayName("Throws CharacterNotFoundException when character does not exist")
        void throwsNotFoundForMissingCharacter() {
            when(characterRepository.findByName("NOBODY")).thenReturn(Optional.empty());

            assertThatThrownBy(() -> characterService.unequipGear("nobody",
                    new UnequipRequest(List.of(EquipmentSlot.HEAD))))
                    .isInstanceOf(CharacterNotFoundException.class);
        }
    }
}
