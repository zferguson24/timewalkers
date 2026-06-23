package com.wow.timewalkers.service;

import com.wow.timewalkers.dto.ArmorPieceDTO;
import com.wow.timewalkers.dto.ExpansionGearDTO;
import com.wow.timewalkers.dto.GearSearchResultDTO;
import com.wow.timewalkers.dto.WeaponDTO;
import com.wow.timewalkers.entity.ArmorPiece;
import com.wow.timewalkers.entity.Weapon;
import com.wow.timewalkers.mapper.GearMapper;
import com.wow.timewalkers.repository.ArmorPieceRepository;
import com.wow.timewalkers.repository.WeaponRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.*;

// @ExtendWith(MockitoExtension.class) activates Mockito's JUnit 5 extension.
// It initializes all @Mock fields before each test and validates stubbing after.
// Much lighter than @SpringBootTest — no application context is started.
@ExtendWith(MockitoExtension.class)
class GearServiceTest {

    @Mock
    private ArmorPieceRepository armorPieceRepository;

    @Mock
    private WeaponRepository weaponRepository;

    // GearMapper has no dependencies — instantiate the real one instead of mocking it
    // so that mapping logic is also exercised.
    private GearService gearService;

    @BeforeEach
    void setUp() {
        gearService = new GearService(armorPieceRepository, weaponRepository, new GearMapper());
    }

    // -----------------------------------------------------------------------
    // Helpers
    // -----------------------------------------------------------------------

    private ArmorPiece armor(String name, String type, String expansion) {
        ArmorPiece ap = new ArmorPiece();
        ap.setName(name);
        ap.setArmorType(type);
        ap.setSlot("Head");
        ap.setExpansion(expansion);
        ap.setCost(0);
        return ap;
    }

    private Weapon weapon(String name, String expansion) {
        Weapon w = new Weapon();
        w.setName(name);
        w.setWeaponSlot("1H");
        w.setWeaponStat("Agility");
        w.setWeaponType("Sword");
        w.setExpansion(expansion);
        w.setCost(0);
        return w;
    }

    // -----------------------------------------------------------------------
    // getAllArmorPieces
    // -----------------------------------------------------------------------

    @Test
    @DisplayName("getAllArmorPieces returns mapped DTOs for all entities from the repo")
    void getAllArmorPiecesReturnsMappedList() {
        when(armorPieceRepository.findAll())
                .thenReturn(List.of(armor("Helm A", "Plate", "Classic"), armor("Helm B", "Leather", "Shadowlands")));

        List<ArmorPieceDTO> result = gearService.getAllArmorPieces();

        assertThat(result).hasSize(2);
        assertThat(result.get(0).name()).isEqualTo("Helm A");
        assertThat(result.get(1).name()).isEqualTo("Helm B");
        verify(armorPieceRepository).findAll();
    }

    @Test
    @DisplayName("getAllArmorPieces returns empty list when repo is empty")
    void getAllArmorPiecesEmpty() {
        when(armorPieceRepository.findAll()).thenReturn(List.of());

        assertThat(gearService.getAllArmorPieces()).isEmpty();
    }

    // -----------------------------------------------------------------------
    // getAllWeapons
    // -----------------------------------------------------------------------

    @Test
    @DisplayName("getAllWeapons returns mapped DTOs for all entities from the repo")
    void getAllWeaponsReturnsMappedList() {
        when(weaponRepository.findAll())
                .thenReturn(List.of(weapon("Sword A", "Classic"), weapon("Sword B", "Shadowlands")));

        List<WeaponDTO> result = gearService.getAllWeapons();

        assertThat(result).hasSize(2);
        assertThat(result.get(0).name()).isEqualTo("Sword A");
        verify(weaponRepository).findAll();
    }

    @Test
    @DisplayName("getAllWeapons returns empty list when repo is empty")
    void getAllWeaponsEmpty() {
        when(weaponRepository.findAll()).thenReturn(List.of());

        assertThat(gearService.getAllWeapons()).isEmpty();
    }

    // -----------------------------------------------------------------------
    // getArmorPiecesByName
    // -----------------------------------------------------------------------

    @Test
    @DisplayName("getArmorPiecesByName passes search term to repo and returns mapped results")
    void getArmorPiecesByNameDelegatesAndMaps() {
        when(armorPieceRepository.findByNameContainingIgnoreCase("Battlegear"))
                .thenReturn(List.of(armor("Dragonstalker's Battlegear", "Mail", "Classic")));

        List<ArmorPieceDTO> result = gearService.getArmorPiecesByName("Battlegear");

        assertThat(result).hasSize(1);
        assertThat(result.get(0).name()).isEqualTo("Dragonstalker's Battlegear");
        verify(armorPieceRepository).findByNameContainingIgnoreCase("Battlegear");
    }

    @Test
    @DisplayName("getArmorPiecesByName returns empty list when no match")
    void getArmorPiecesByNameNoMatch() {
        when(armorPieceRepository.findByNameContainingIgnoreCase("zzz")).thenReturn(List.of());

        assertThat(gearService.getArmorPiecesByName("zzz")).isEmpty();
    }

    // -----------------------------------------------------------------------
    // getWeaponsByName
    // -----------------------------------------------------------------------

    @Test
    @DisplayName("getWeaponsByName passes search term to repo and returns mapped results")
    void getWeaponsByNameDelegatesAndMaps() {
        when(weaponRepository.findByNameContainingIgnoreCase("Azzinoth"))
                .thenReturn(List.of(weapon("Warglaive of Azzinoth", "The Burning Crusade")));

        List<WeaponDTO> result = gearService.getWeaponsByName("Azzinoth");

        assertThat(result).hasSize(1);
        assertThat(result.get(0).name()).isEqualTo("Warglaive of Azzinoth");
    }

    @Test
    @DisplayName("getWeaponsByName returns empty list when no match")
    void getWeaponsByNameNoMatch() {
        when(weaponRepository.findByNameContainingIgnoreCase("zzz")).thenReturn(List.of());

        assertThat(gearService.getWeaponsByName("zzz")).isEmpty();
    }

    // -----------------------------------------------------------------------
    // getGearByExpansion
    // -----------------------------------------------------------------------

    @Test
    @DisplayName("getGearByExpansion combines armor and weapon results under the expansion name")
    void getGearByExpansionCombinesResults() {
        when(armorPieceRepository.findByExpansionContainingIgnoreCase("Classic"))
                .thenReturn(List.of(armor("Classic Helm", "Mail", "Classic")));
        when(weaponRepository.findByExpansionContainingIgnoreCase("Classic"))
                .thenReturn(List.of(weapon("Classic Sword", "Classic")));

        ExpansionGearDTO result = gearService.getGearByExpansion("Classic");

        assertThat(result.expansion()).isEqualTo("Classic");
        assertThat(result.armorPieces()).hasSize(1);
        assertThat(result.weapons()).hasSize(1);
        assertThat(result.armorPieces().get(0).name()).isEqualTo("Classic Helm");
        assertThat(result.weapons().get(0).name()).isEqualTo("Classic Sword");
    }

    @Test
    @DisplayName("getGearByExpansion supports partial expansion name matching")
    void getGearByExpansionPartialMatch() {
        when(armorPieceRepository.findByExpansionContainingIgnoreCase("Burning")).thenReturn(List.of());
        when(weaponRepository.findByExpansionContainingIgnoreCase("Burning"))
                .thenReturn(List.of(weapon("Warglaive of Azzinoth", "The Burning Crusade")));

        ExpansionGearDTO result = gearService.getGearByExpansion("Burning");

        assertThat(result.weapons()).hasSize(1);
    }

    @Test
    @DisplayName("getGearByExpansion returns empty lists when expansion has no items")
    void getGearByExpansionEmpty() {
        when(armorPieceRepository.findByExpansionContainingIgnoreCase("Unknown")).thenReturn(List.of());
        when(weaponRepository.findByExpansionContainingIgnoreCase("Unknown")).thenReturn(List.of());

        ExpansionGearDTO result = gearService.getGearByExpansion("Unknown");

        assertThat(result.expansion()).isEqualTo("Unknown");
        assertThat(result.armorPieces()).isEmpty();
        assertThat(result.weapons()).isEmpty();
    }

    // -----------------------------------------------------------------------
    // getArmorPiecesByType
    // -----------------------------------------------------------------------

    @Test
    @DisplayName("getArmorPiecesByType passes armor type to repo and returns mapped results")
    void getArmorPiecesByTypeDelegatesAndMaps() {
        when(armorPieceRepository.findByArmorTypeContainingIgnoreCase("Plate"))
                .thenReturn(List.of(armor("Plate Helm", "Plate", "Classic")));

        List<ArmorPieceDTO> result = gearService.getArmorPiecesByType("Plate");

        assertThat(result).hasSize(1);
        assertThat(result.get(0).armorType()).isEqualTo("Plate");
        verify(armorPieceRepository).findByArmorTypeContainingIgnoreCase("Plate");
    }

    @Test
    @DisplayName("getArmorPiecesByType returns empty list for unknown type")
    void getArmorPiecesByTypeNoMatch() {
        when(armorPieceRepository.findByArmorTypeContainingIgnoreCase("Crystal")).thenReturn(List.of());

        assertThat(gearService.getArmorPiecesByType("Crystal")).isEmpty();
    }

    // -----------------------------------------------------------------------
    // getWeaponsByType
    // -----------------------------------------------------------------------

    @Test
    @DisplayName("getWeaponsByType passes weapon type to repo and returns mapped results")
    void getWeaponsByTypeDelegatesAndMaps() {
        Weapon w = new Weapon();
        w.setName("Quel'Serrar");
        w.setWeaponSlot("1H");
        w.setWeaponStat("Strength");
        w.setWeaponType("Sword");
        w.setExpansion("Classic");
        w.setCost(0);
        when(weaponRepository.findByWeaponTypeContainingIgnoreCase("Sword")).thenReturn(List.of(w));

        List<WeaponDTO> result = gearService.getWeaponsByType("Sword");

        assertThat(result).hasSize(1);
        assertThat(result.get(0).weaponType()).isEqualTo("Sword");
        verify(weaponRepository).findByWeaponTypeContainingIgnoreCase("Sword");
    }

    @Test
    @DisplayName("getWeaponsByType returns empty list for unknown type")
    void getWeaponsByTypeNoMatch() {
        when(weaponRepository.findByWeaponTypeContainingIgnoreCase("Halberd")).thenReturn(List.of());

        assertThat(gearService.getWeaponsByType("Halberd")).isEmpty();
    }

    // -----------------------------------------------------------------------
    // searchGear
    // -----------------------------------------------------------------------

    @Test
    @DisplayName("searchGear returns armor and weapons sorted alphabetically")
    void searchGearReturnsSortedResults() {
        when(armorPieceRepository.findByNameContainingIgnoreCase("Classic"))
                .thenReturn(List.of(armor("Classic Helm B", "Plate", "Classic"), armor("Classic Helm A", "Plate", "Classic")));
        when(weaponRepository.findByNameContainingIgnoreCase("Classic"))
                .thenReturn(List.of(weapon("Classic Sword B", "Classic"), weapon("Classic Sword A", "Classic")));
        when(armorPieceRepository.findByExpansionContainingIgnoreCase("Classic")).thenReturn(List.of());
        when(weaponRepository.findByExpansionContainingIgnoreCase("Classic")).thenReturn(List.of());
        when(armorPieceRepository.findByArmorTypeContainingIgnoreCase("Classic")).thenReturn(List.of());
        when(armorPieceRepository.findBySlotContainingIgnoreCase("Classic")).thenReturn(List.of());
        when(weaponRepository.findByWeaponTypeContainingIgnoreCase("Classic")).thenReturn(List.of());
        when(weaponRepository.findByWeaponSlotContainingIgnoreCase("Classic")).thenReturn(List.of());

        GearSearchResultDTO result = gearService.searchGear("Classic");

        assertThat(result.armorPieces()).extracting(ArmorPieceDTO::name)
                .containsExactly("Classic Helm A", "Classic Helm B");
        assertThat(result.weapons()).extracting(WeaponDTO::name)
                .containsExactly("Classic Sword A", "Classic Sword B");
    }

    @Test
    @DisplayName("searchGear deduplicates items that appear in multiple source queries")
    void searchGearDeduplicatesAcrossSources() {
        ArmorPiece sharedArmor = armor("Dragonstalker Helm", "Mail", "Classic");
        when(armorPieceRepository.findByNameContainingIgnoreCase("Dragon"))
                .thenReturn(List.of(sharedArmor));
        when(weaponRepository.findByNameContainingIgnoreCase("Dragon")).thenReturn(List.of());
        // Same item also returned by expansion query — should not appear twice
        when(armorPieceRepository.findByExpansionContainingIgnoreCase("Dragon"))
                .thenReturn(List.of(sharedArmor));
        when(weaponRepository.findByExpansionContainingIgnoreCase("Dragon")).thenReturn(List.of());
        when(armorPieceRepository.findByArmorTypeContainingIgnoreCase("Dragon")).thenReturn(List.of());
        when(armorPieceRepository.findBySlotContainingIgnoreCase("Dragon")).thenReturn(List.of());
        when(weaponRepository.findByWeaponTypeContainingIgnoreCase("Dragon")).thenReturn(List.of());
        when(weaponRepository.findByWeaponSlotContainingIgnoreCase("Dragon")).thenReturn(List.of());

        GearSearchResultDTO result = gearService.searchGear("Dragon");

        assertThat(result.armorPieces()).hasSize(1);
        assertThat(result.armorPieces().get(0).name()).isEqualTo("Dragonstalker Helm");
    }

    @Test
    @DisplayName("searchGear returns items matched by armor slot")
    void searchGearByArmorSlot() {
        ArmorPiece ring1 = armor("Band of Eternity", "Agnostic", "Classic");
        ArmorPiece ring2 = armor("Ring of Power", "Agnostic", "Shadowlands");
        ring1.setSlot("Finger");
        ring2.setSlot("Finger");
        when(armorPieceRepository.findByNameContainingIgnoreCase("Finger")).thenReturn(List.of());
        when(weaponRepository.findByNameContainingIgnoreCase("Finger")).thenReturn(List.of());
        when(armorPieceRepository.findByExpansionContainingIgnoreCase("Finger")).thenReturn(List.of());
        when(weaponRepository.findByExpansionContainingIgnoreCase("Finger")).thenReturn(List.of());
        when(armorPieceRepository.findByArmorTypeContainingIgnoreCase("Finger")).thenReturn(List.of());
        when(armorPieceRepository.findBySlotContainingIgnoreCase("Finger")).thenReturn(List.of(ring1, ring2));
        when(weaponRepository.findByWeaponTypeContainingIgnoreCase("Finger")).thenReturn(List.of());
        when(weaponRepository.findByWeaponSlotContainingIgnoreCase("Finger")).thenReturn(List.of());

        GearSearchResultDTO result = gearService.searchGear("Finger");

        assertThat(result.armorPieces()).hasSize(2);
        assertThat(result.weapons()).isEmpty();
    }

    @Test
    @DisplayName("searchGear returns items matched by weapon slot")
    void searchGearByWeaponSlot() {
        Weapon offhand = weapon("Tome of Power", "Classic");
        offhand.setWeaponSlot("Off-Hand");
        when(armorPieceRepository.findByNameContainingIgnoreCase("Off-Hand")).thenReturn(List.of());
        when(weaponRepository.findByNameContainingIgnoreCase("Off-Hand")).thenReturn(List.of());
        when(armorPieceRepository.findByExpansionContainingIgnoreCase("Off-Hand")).thenReturn(List.of());
        when(weaponRepository.findByExpansionContainingIgnoreCase("Off-Hand")).thenReturn(List.of());
        when(armorPieceRepository.findByArmorTypeContainingIgnoreCase("Off-Hand")).thenReturn(List.of());
        when(armorPieceRepository.findBySlotContainingIgnoreCase("Off-Hand")).thenReturn(List.of());
        when(weaponRepository.findByWeaponTypeContainingIgnoreCase("Off-Hand")).thenReturn(List.of());
        when(weaponRepository.findByWeaponSlotContainingIgnoreCase("Off-Hand")).thenReturn(List.of(offhand));

        GearSearchResultDTO result = gearService.searchGear("Off-Hand");

        assertThat(result.armorPieces()).isEmpty();
        assertThat(result.weapons()).hasSize(1);
        assertThat(result.weapons().get(0).name()).isEqualTo("Tome of Power");
    }

    @Test
    @DisplayName("searchGear returns empty lists when no query matches anything")
    void searchGearNoMatch() {
        when(armorPieceRepository.findByNameContainingIgnoreCase("zzz")).thenReturn(List.of());
        when(weaponRepository.findByNameContainingIgnoreCase("zzz")).thenReturn(List.of());
        when(armorPieceRepository.findByExpansionContainingIgnoreCase("zzz")).thenReturn(List.of());
        when(weaponRepository.findByExpansionContainingIgnoreCase("zzz")).thenReturn(List.of());
        when(armorPieceRepository.findByArmorTypeContainingIgnoreCase("zzz")).thenReturn(List.of());
        when(armorPieceRepository.findBySlotContainingIgnoreCase("zzz")).thenReturn(List.of());
        when(weaponRepository.findByWeaponTypeContainingIgnoreCase("zzz")).thenReturn(List.of());
        when(weaponRepository.findByWeaponSlotContainingIgnoreCase("zzz")).thenReturn(List.of());

        GearSearchResultDTO result = gearService.searchGear("zzz");

        assertThat(result.armorPieces()).isEmpty();
        assertThat(result.weapons()).isEmpty();
    }
}
