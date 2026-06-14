package com.wow.timewalkers.controller;

import com.wow.timewalkers.dto.ArmorPieceDTO;
import com.wow.timewalkers.dto.ExpansionGearDTO;
import com.wow.timewalkers.dto.WeaponDTO;
import com.wow.timewalkers.exception.GlobalExceptionHandler;
import com.wow.timewalkers.service.GearService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import java.util.List;

import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

// Standalone MockMvc test — no Spring context is loaded. We construct the controller
// manually, wire in a Mockito mock for the service, and register the real
// GlobalExceptionHandler so 4xx responses behave just like in production.
// This is fast (no application context, no DB) and still exercises the full
// request-dispatch → controller → exception-handler pipeline.
@ExtendWith(MockitoExtension.class)
class GearControllerTest {

    @Mock
    private GearService gearService;

    private MockMvc mockMvc;

    @BeforeEach
    void setUp() {
        // standaloneSetup registers only this controller, not the full application context.
        // setControllerAdvice wires the real GlobalExceptionHandler so exception mappings work.
        mockMvc = MockMvcBuilders
                .standaloneSetup(new GearController(gearService))
                .setControllerAdvice(new GlobalExceptionHandler())
                .build();
    }

    // -----------------------------------------------------------------------
    // GET /api/gear/armor
    // -----------------------------------------------------------------------

    @Test
    @DisplayName("GET /api/gear/armor returns 200 with armor list")
    void getAllArmorReturns200() throws Exception {
        ArmorPieceDTO dto = new ArmorPieceDTO("Leather", "Helm", "Demon's Skull",
                "The Burning Crusade", "Agility", "Haste", 50, null, null, null);
        when(gearService.getAllArmorPieces()).thenReturn(List.of(dto));

        mockMvc.perform(get("/api/gear/armor"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].name").value("Demon's Skull"))
                .andExpect(jsonPath("$[0].armorType").value("Leather"));
    }

    @Test
    @DisplayName("GET /api/gear/armor returns 200 with empty array when no armor exists")
    void getAllArmorEmptyList() throws Exception {
        when(gearService.getAllArmorPieces()).thenReturn(List.of());

        mockMvc.perform(get("/api/gear/armor"))
                .andExpect(status().isOk())
                .andExpect(content().json("[]"));
    }

    // -----------------------------------------------------------------------
    // GET /api/gear/weapons
    // -----------------------------------------------------------------------

    @Test
    @DisplayName("GET /api/gear/weapons returns 200 with weapon list")
    void getAllWeaponsReturns200() throws Exception {
        WeaponDTO dto = new WeaponDTO("1H", "Agility", "Warglaive", "Warglaive of Azzinoth",
                "The Burning Crusade", "Agility", "Haste", 0, null, null, null);
        when(gearService.getAllWeapons()).thenReturn(List.of(dto));

        mockMvc.perform(get("/api/gear/weapons"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].name").value("Warglaive of Azzinoth"))
                .andExpect(jsonPath("$[0].weaponType").value("Warglaive"));
    }

    @Test
    @DisplayName("GET /api/gear/weapons returns 200 with empty array when no weapons exist")
    void getAllWeaponsEmptyList() throws Exception {
        when(gearService.getAllWeapons()).thenReturn(List.of());

        mockMvc.perform(get("/api/gear/weapons"))
                .andExpect(status().isOk())
                .andExpect(content().json("[]"));
    }

    // -----------------------------------------------------------------------
    // GET /api/gear/armor/search
    // -----------------------------------------------------------------------

    @Test
    @DisplayName("GET /api/gear/armor/search?name= returns 200 with matching results")
    void searchArmorReturns200() throws Exception {
        ArmorPieceDTO dto = new ArmorPieceDTO("Plate", "Helm", "Battlegear Helm",
                "Classic", "Strength", null, 100, null, null, null);
        when(gearService.getArmorPiecesByName("Battlegear")).thenReturn(List.of(dto));

        mockMvc.perform(get("/api/gear/armor/search").param("name", "Battlegear"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].name").value("Battlegear Helm"));

        verify(gearService).getArmorPiecesByName("Battlegear");
    }

    @Test
    @DisplayName("GET /api/gear/armor/search without name param returns 400")
    void searchArmorMissingParamReturns400() throws Exception {
        mockMvc.perform(get("/api/gear/armor/search"))
                .andExpect(status().isBadRequest());
    }

    // -----------------------------------------------------------------------
    // GET /api/gear/weapons/search
    // -----------------------------------------------------------------------

    @Test
    @DisplayName("GET /api/gear/weapons/search?name= returns 200 with matching results")
    void searchWeaponsReturns200() throws Exception {
        WeaponDTO dto = new WeaponDTO("1H", "Agility", "Sword", "Quel'Serrar",
                "Classic", "Agility", null, 0, null, null, null);
        when(gearService.getWeaponsByName("Quel")).thenReturn(List.of(dto));

        mockMvc.perform(get("/api/gear/weapons/search").param("name", "Quel"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].name").value("Quel'Serrar"));
    }

    @Test
    @DisplayName("GET /api/gear/weapons/search without name param returns 400")
    void searchWeaponsMissingParamReturns400() throws Exception {
        mockMvc.perform(get("/api/gear/weapons/search"))
                .andExpect(status().isBadRequest());
    }

    // -----------------------------------------------------------------------
    // GET /api/gear/expansion
    // -----------------------------------------------------------------------

    @Test
    @DisplayName("GET /api/gear/expansion?name= returns 200 with expansion gear")
    void getExpansionGearReturns200() throws Exception {
        ExpansionGearDTO dto = new ExpansionGearDTO("Classic", List.of(), List.of());
        when(gearService.getGearByExpansion("Classic")).thenReturn(dto);

        mockMvc.perform(get("/api/gear/expansion").param("name", "Classic"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.expansion").value("Classic"))
                .andExpect(jsonPath("$.armorPieces").isArray())
                .andExpect(jsonPath("$.weapons").isArray());
    }

    @Test
    @DisplayName("GET /api/gear/expansion without name param returns 400")
    void getExpansionGearMissingParamReturns400() throws Exception {
        mockMvc.perform(get("/api/gear/expansion"))
                .andExpect(status().isBadRequest());
    }

    // -----------------------------------------------------------------------
    // GET /api/gear/armor/type
    // -----------------------------------------------------------------------

    @Test
    @DisplayName("GET /api/gear/armor/type?name= returns 200 with filtered armor")
    void getArmorByTypeReturns200() throws Exception {
        ArmorPieceDTO dto = new ArmorPieceDTO("Plate", "Helm", "Plate Helm",
                "Classic", "Strength", null, 0, null, null, null);
        when(gearService.getArmorPiecesByType("Plate")).thenReturn(List.of(dto));

        mockMvc.perform(get("/api/gear/armor/type").param("name", "Plate"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].armorType").value("Plate"));

        verify(gearService).getArmorPiecesByType("Plate");
    }

    @Test
    @DisplayName("GET /api/gear/armor/type without name param returns 400")
    void getArmorByTypeMissingParamReturns400() throws Exception {
        mockMvc.perform(get("/api/gear/armor/type"))
                .andExpect(status().isBadRequest());
    }
}
