package com.wow.timewalkers.mapper;

import com.wow.timewalkers.dto.ArmorPieceDTO;
import com.wow.timewalkers.dto.WeaponDTO;
import com.wow.timewalkers.entity.ArmorPiece;
import com.wow.timewalkers.entity.Weapon;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

// Pure unit test — GearMapper has no Spring dependencies, instantiate directly.
class GearMapperTest {

    private GearMapper mapper;

    @BeforeEach
    void setUp() {
        mapper = new GearMapper();
    }

    @Test
    @DisplayName("toArmorPieceDTO maps all fields from entity to record")
    void toArmorPieceDTOMapsAllFields() {
        ArmorPiece entity = new ArmorPiece();
        entity.setArmorType("Leather");
        entity.setSlot("Helm");
        entity.setName("Helm of the Fallen Hero");
        entity.setExpansion("Wrath of the Lich King");
        entity.setPrimaryStat("Agility");
        entity.setSecondaryStat("Critical Strike");
        entity.setCost(200);
        entity.setNotes("From Tier 7");
        entity.setWowheadUrl("https://www.wowhead.com/item=12345");

        ArmorPieceDTO dto = mapper.toArmorPieceDTO(entity);

        assertThat(dto.armorType()).isEqualTo("Leather");
        assertThat(dto.slot()).isEqualTo("Helm");
        assertThat(dto.name()).isEqualTo("Helm of the Fallen Hero");
        assertThat(dto.expansion()).isEqualTo("Wrath of the Lich King");
        assertThat(dto.primaryStat()).isEqualTo("Agility");
        assertThat(dto.secondaryStat()).isEqualTo("Critical Strike");
        assertThat(dto.cost()).isEqualTo(200);
        assertThat(dto.notes()).isEqualTo("From Tier 7");
        assertThat(dto.wowheadUrl()).isEqualTo("https://www.wowhead.com/item=12345");
    }

    @Test
    @DisplayName("toArmorPieceDTO handles nullable fields (primaryStat, secondaryStat, notes, wowheadUrl)")
    void toArmorPieceDTOHandlesNulls() {
        ArmorPiece entity = new ArmorPiece();
        entity.setArmorType("Cloth");
        entity.setSlot("Ring");
        entity.setName("Simple Ring");
        entity.setExpansion("Classic");
        entity.setCost(0);
        // primaryStat, secondaryStat, notes, wowheadUrl intentionally left null

        ArmorPieceDTO dto = mapper.toArmorPieceDTO(entity);

        assertThat(dto.primaryStat()).isNull();
        assertThat(dto.secondaryStat()).isNull();
        assertThat(dto.notes()).isNull();
        assertThat(dto.wowheadUrl()).isNull();
    }

    @Test
    @DisplayName("toWeaponDTO maps all fields from entity to record")
    void toWeaponDTOMapsAllFields() {
        Weapon entity = new Weapon();
        entity.setWeaponSlot("1H");
        entity.setWeaponStat("Agility");
        entity.setWeaponType("Warglaive");
        entity.setName("Warglaive of Azzinoth");
        entity.setExpansion("The Burning Crusade");
        entity.setPrimaryStat("Agility");
        entity.setSecondaryStat("Haste");
        entity.setCost(0);
        entity.setNotes("Main Hand only");
        entity.setWowheadUrl("https://www.wowhead.com/item=32837");

        WeaponDTO dto = mapper.toWeaponDTO(entity);

        assertThat(dto.weaponSlot()).isEqualTo("1H");
        assertThat(dto.weaponStat()).isEqualTo("Agility");
        assertThat(dto.weaponType()).isEqualTo("Warglaive");
        assertThat(dto.name()).isEqualTo("Warglaive of Azzinoth");
        assertThat(dto.expansion()).isEqualTo("The Burning Crusade");
        assertThat(dto.primaryStat()).isEqualTo("Agility");
        assertThat(dto.secondaryStat()).isEqualTo("Haste");
        assertThat(dto.cost()).isEqualTo(0);
        assertThat(dto.notes()).isEqualTo("Main Hand only");
        assertThat(dto.wowheadUrl()).isEqualTo("https://www.wowhead.com/item=32837");
    }

    @Test
    @DisplayName("toWeaponDTO handles nullable fields")
    void toWeaponDTOHandlesNulls() {
        Weapon entity = new Weapon();
        entity.setWeaponSlot("2H");
        entity.setWeaponStat("Strength");
        entity.setWeaponType("Sword");
        entity.setName("Big Sword");
        entity.setExpansion("Classic");
        entity.setCost(10);
        // nullable fields omitted

        WeaponDTO dto = mapper.toWeaponDTO(entity);

        assertThat(dto.primaryStat()).isNull();
        assertThat(dto.secondaryStat()).isNull();
        assertThat(dto.notes()).isNull();
        assertThat(dto.wowheadUrl()).isNull();
    }
}
