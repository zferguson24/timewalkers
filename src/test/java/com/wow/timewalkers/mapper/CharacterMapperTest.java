package com.wow.timewalkers.mapper;

import com.wow.timewalkers.dto.ArmorPieceDTO;
import com.wow.timewalkers.dto.CharacterDTO;
import com.wow.timewalkers.dto.EquippedSlotDTO;
import com.wow.timewalkers.dto.WeaponDTO;
import com.wow.timewalkers.entity.ArmorPiece;
import com.wow.timewalkers.entity.CharacterEquipment;
import com.wow.timewalkers.entity.Weapon;
import com.wow.timewalkers.entity.WowCharacter;
import com.wow.timewalkers.enums.EquipmentSlot;
import com.wow.timewalkers.enums.ItemType;
import com.wow.timewalkers.enums.WowClass;
import com.wow.timewalkers.enums.WowRace;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import static org.assertj.core.api.Assertions.assertThat;

// Pure unit test — both CharacterMapper and its GearMapper dependency are instantiated directly.
class CharacterMapperTest {

    private CharacterMapper mapper;

    @BeforeEach
    void setUp() {
        mapper = new CharacterMapper(new GearMapper());
    }

    private WowCharacter makeCharacter() {
        WowCharacter c = new WowCharacter();
        c.setName("JARAXXUS");
        c.setRace(WowRace.NIGHT_ELF);
        c.setCharacterClass(WowClass.DEMON_HUNTER);
        return c;
    }

    @Test
    @DisplayName("Empty equipment produces 16 slots all with equipped=false and null item")
    void emptyEquipmentProduces16UnequippedSlots() {
        CharacterDTO dto = mapper.toCharacterDTO(makeCharacter(), List.of());

        assertThat(dto.equipment()).hasSize(16);
        assertThat(dto.equipment()).allSatisfy(slot -> {
            assertThat(slot.equipped()).isFalse();
            assertThat(slot.item()).isNull();
        });
    }

    @Test
    @DisplayName("Slots appear in EquipmentSlot enum declaration order")
    void slotsAreInDeclarationOrder() {
        CharacterDTO dto = mapper.toCharacterDTO(makeCharacter(), List.of());

        EquipmentSlot[] expected = EquipmentSlot.values();
        for (int i = 0; i < expected.length; i++) {
            assertThat(dto.equipment().get(i).slot()).isEqualTo(expected[i]);
        }
    }

    @Test
    @DisplayName("Equipped armor slot shows equipped=true and an ArmorPieceDTO")
    void equippedArmorSlotMapsCorrectly() {
        ArmorPiece ap = new ArmorPiece();
        ap.setArmorType("Leather");
        ap.setSlot("Helm");
        ap.setName("Demon's Skull");
        ap.setExpansion("The Burning Crusade");
        ap.setCost(50);

        CharacterEquipment ce = new CharacterEquipment();
        ce.setSlot(EquipmentSlot.HEAD);
        ce.setItemType(ItemType.ARMOR);
        ce.setArmorPiece(ap);

        CharacterDTO dto = mapper.toCharacterDTO(makeCharacter(), List.of(ce));

        Map<EquipmentSlot, EquippedSlotDTO> bySlot = dto.equipment().stream()
                .collect(Collectors.toMap(EquippedSlotDTO::slot, s -> s));

        EquippedSlotDTO headSlot = bySlot.get(EquipmentSlot.HEAD);
        assertThat(headSlot.equipped()).isTrue();
        assertThat(headSlot.item()).isInstanceOf(ArmorPieceDTO.class);
        assertThat(((ArmorPieceDTO) headSlot.item()).name()).isEqualTo("Demon's Skull");

        // All other slots should be unequipped
        bySlot.entrySet().stream()
                .filter(e -> e.getKey() != EquipmentSlot.HEAD)
                .forEach(e -> {
                    assertThat(e.getValue().equipped()).isFalse();
                    assertThat(e.getValue().item()).isNull();
                });
    }

    @Test
    @DisplayName("Equipped weapon slot shows equipped=true and a WeaponDTO")
    void equippedWeaponSlotMapsCorrectly() {
        Weapon w = new Weapon();
        w.setWeaponSlot("1H");
        w.setWeaponStat("Agility");
        w.setWeaponType("Warglaive");
        w.setName("Warglaive of Azzinoth");
        w.setExpansion("The Burning Crusade");
        w.setCost(0);

        CharacterEquipment ce = new CharacterEquipment();
        ce.setSlot(EquipmentSlot.MAIN_HAND);
        ce.setItemType(ItemType.WEAPON);
        ce.setWeapon(w);

        CharacterDTO dto = mapper.toCharacterDTO(makeCharacter(), List.of(ce));

        Map<EquipmentSlot, EquippedSlotDTO> bySlot = dto.equipment().stream()
                .collect(Collectors.toMap(EquippedSlotDTO::slot, s -> s));

        EquippedSlotDTO mainHand = bySlot.get(EquipmentSlot.MAIN_HAND);
        assertThat(mainHand.equipped()).isTrue();
        assertThat(mainHand.item()).isInstanceOf(WeaponDTO.class);
        assertThat(((WeaponDTO) mainHand.item()).name()).isEqualTo("Warglaive of Azzinoth");
    }

    @Test
    @DisplayName("Character fields (name, race, class) are copied into DTO")
    void characterFieldsCopied() {
        CharacterDTO dto = mapper.toCharacterDTO(makeCharacter(), List.of());

        assertThat(dto.name()).isEqualTo("JARAXXUS");
        assertThat(dto.race()).isEqualTo(WowRace.NIGHT_ELF);
        assertThat(dto.characterClass()).isEqualTo(WowClass.DEMON_HUNTER);
    }

    @Test
    @DisplayName("Multiple equipped slots are all reflected in the DTO")
    void multipleEquippedSlots() {
        ArmorPiece helm = new ArmorPiece();
        helm.setArmorType("Leather"); helm.setSlot("Helm");
        helm.setName("Helm"); helm.setExpansion("Classic"); helm.setCost(0);

        ArmorPiece chest = new ArmorPiece();
        chest.setArmorType("Leather"); chest.setSlot("Chest");
        chest.setName("Chest"); chest.setExpansion("Classic"); chest.setCost(0);

        CharacterEquipment ceHelm = new CharacterEquipment();
        ceHelm.setSlot(EquipmentSlot.HEAD);
        ceHelm.setItemType(ItemType.ARMOR);
        ceHelm.setArmorPiece(helm);

        CharacterEquipment ceChest = new CharacterEquipment();
        ceChest.setSlot(EquipmentSlot.CHEST);
        ceChest.setItemType(ItemType.ARMOR);
        ceChest.setArmorPiece(chest);

        CharacterDTO dto = mapper.toCharacterDTO(makeCharacter(), List.of(ceHelm, ceChest));

        long equippedCount = dto.equipment().stream().filter(EquippedSlotDTO::equipped).count();
        assertThat(equippedCount).isEqualTo(2);

        Map<EquipmentSlot, EquippedSlotDTO> bySlot = dto.equipment().stream()
                .collect(Collectors.toMap(EquippedSlotDTO::slot, s -> s));
        assertThat(bySlot.get(EquipmentSlot.HEAD).equipped()).isTrue();
        assertThat(bySlot.get(EquipmentSlot.CHEST).equipped()).isTrue();
    }
}
