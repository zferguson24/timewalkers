package com.wow.timewalkers.service;

import com.wow.timewalkers.dto.ArmorPieceDTO;
import com.wow.timewalkers.dto.ExpansionGearDTO;
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
        ap.setSlot("Helm");
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
        when(armorPieceRepository.findByExpansionIgnoreCase("Classic"))
                .thenReturn(List.of(armor("Classic Helm", "Mail", "Classic")));
        when(weaponRepository.findByExpansionIgnoreCase("Classic"))
                .thenReturn(List.of(weapon("Classic Sword", "Classic")));

        ExpansionGearDTO result = gearService.getGearByExpansion("Classic");

        assertThat(result.expansion()).isEqualTo("Classic");
        assertThat(result.armorPieces()).hasSize(1);
        assertThat(result.weapons()).hasSize(1);
        assertThat(result.armorPieces().get(0).name()).isEqualTo("Classic Helm");
        assertThat(result.weapons().get(0).name()).isEqualTo("Classic Sword");
    }

    @Test
    @DisplayName("getGearByExpansion returns empty lists when expansion has no items")
    void getGearByExpansionEmpty() {
        when(armorPieceRepository.findByExpansionIgnoreCase("Unknown")).thenReturn(List.of());
        when(weaponRepository.findByExpansionIgnoreCase("Unknown")).thenReturn(List.of());

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
        when(armorPieceRepository.findByArmorTypeIgnoreCase("Plate"))
                .thenReturn(List.of(armor("Plate Helm", "Plate", "Classic")));

        List<ArmorPieceDTO> result = gearService.getArmorPiecesByType("Plate");

        assertThat(result).hasSize(1);
        assertThat(result.get(0).armorType()).isEqualTo("Plate");
        verify(armorPieceRepository).findByArmorTypeIgnoreCase("Plate");
    }

    @Test
    @DisplayName("getArmorPiecesByType returns empty list for unknown type")
    void getArmorPiecesByTypeNoMatch() {
        when(armorPieceRepository.findByArmorTypeIgnoreCase("Crystal")).thenReturn(List.of());

        assertThat(gearService.getArmorPiecesByType("Crystal")).isEmpty();
    }
}
