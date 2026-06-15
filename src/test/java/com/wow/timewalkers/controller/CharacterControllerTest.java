package com.wow.timewalkers.controller;

import com.wow.timewalkers.dto.*;
import com.wow.timewalkers.enums.EquipmentSlot;
import com.wow.timewalkers.enums.WowClass;
import com.wow.timewalkers.enums.WowRace;
import com.wow.timewalkers.exception.CharacterNameConflictException;
import com.wow.timewalkers.exception.CharacterNotFoundException;
import com.wow.timewalkers.exception.GearValidationException;
import com.wow.timewalkers.exception.GlobalExceptionHandler;
import com.wow.timewalkers.exception.InvalidRaceClassCombinationException;
import com.wow.timewalkers.service.CharacterService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

// Standalone MockMvc — see GearControllerTest for the general strategy.
// Request bodies are written as raw JSON strings to avoid a direct dependency on
// jackson-databind in the test source, since Spring MVC uses Jackson internally
// via the message converter without us needing to import ObjectMapper.
@ExtendWith(MockitoExtension.class)
class CharacterControllerTest {

    @Mock
    private CharacterService characterService;

    private MockMvc mockMvc;

    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders
                .standaloneSetup(new CharacterController(characterService))
                .setControllerAdvice(new GlobalExceptionHandler())
                .build();
    }

    // -----------------------------------------------------------------------
    // Helpers
    // -----------------------------------------------------------------------

    private CharacterDTO emptyCharacterDTO() {
        List<EquippedSlotDTO> slots = Arrays.stream(EquipmentSlot.values())
                .map(s -> new EquippedSlotDTO(s, false, null))
                .collect(Collectors.toList());
        return new CharacterDTO("JARAXXUS", WowRace.NIGHT_ELF, WowClass.DEMON_HUNTER, slots);
    }

    // -----------------------------------------------------------------------
    // GET /api/characters
    // -----------------------------------------------------------------------

    @Test
    @DisplayName("GET /api/characters returns 200 with list of character summaries")
    void getAllCharactersReturns200() throws Exception {
        CharacterSummaryDTO summary = new CharacterSummaryDTO("JARAXXUS", WowRace.NIGHT_ELF, WowClass.DEMON_HUNTER);
        when(characterService.getAllCharacters()).thenReturn(List.of(summary));

        mockMvc.perform(get("/api/characters"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].name").value("JARAXXUS"))
                .andExpect(jsonPath("$[0].race").value("NIGHT_ELF"))
                .andExpect(jsonPath("$[0].characterClass").value("DEMON_HUNTER"));
    }

    @Test
    @DisplayName("GET /api/characters returns 200 with empty array when no characters exist")
    void getAllCharactersReturnsEmptyList() throws Exception {
        when(characterService.getAllCharacters()).thenReturn(List.of());

        mockMvc.perform(get("/api/characters"))
                .andExpect(status().isOk())
                .andExpect(content().json("[]"));
    }

    // -----------------------------------------------------------------------
    // POST /api/characters
    // -----------------------------------------------------------------------

    @Test
    @DisplayName("POST /api/characters returns 201 with new character")
    void createCharacterReturns201() throws Exception {
        when(characterService.createCharacter(any())).thenReturn(emptyCharacterDTO());

        mockMvc.perform(post("/api/characters")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "name": "jaraxxus",
                                  "race": "NIGHT_ELF",
                                  "characterClass": "DEMON_HUNTER"
                                }
                                """))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.name").value("JARAXXUS"))
                .andExpect(jsonPath("$.race").value("NIGHT_ELF"))
                .andExpect(jsonPath("$.characterClass").value("DEMON_HUNTER"))
                .andExpect(jsonPath("$.equipment").isArray());
    }

    @Test
    @DisplayName("POST /api/characters returns 409 when name already exists")
    void createCharacterReturns409OnConflict() throws Exception {
        when(characterService.createCharacter(any()))
                .thenThrow(new CharacterNameConflictException("A character named 'JARAXXUS' already exists"));

        mockMvc.perform(post("/api/characters")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "name": "jaraxxus",
                                  "race": "NIGHT_ELF",
                                  "characterClass": "DEMON_HUNTER"
                                }
                                """))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.message").value("A character named 'JARAXXUS' already exists"));
    }

    @Test
    @DisplayName("POST /api/characters returns 400 when race/class combination is invalid")
    void createCharacterReturns400OnInvalidRaceClass() throws Exception {
        when(characterService.createCharacter(any()))
                .thenThrow(new InvalidRaceClassCombinationException("Human cannot be a Evoker"));

        mockMvc.perform(post("/api/characters")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "name": "jaraxxus",
                                  "race": "HUMAN",
                                  "characterClass": "EVOKER"
                                }
                                """))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value("Human cannot be a Evoker"));
    }

    // -----------------------------------------------------------------------
    // GET /api/characters/{name}
    // -----------------------------------------------------------------------

    @Test
    @DisplayName("GET /api/characters/{name} returns 200 with character")
    void getCharacterReturns200() throws Exception {
        when(characterService.getCharacter("jaraxxus")).thenReturn(emptyCharacterDTO());

        mockMvc.perform(get("/api/characters/jaraxxus"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.name").value("JARAXXUS"))
                .andExpect(jsonPath("$.equipment").isArray());
    }

    @Test
    @DisplayName("GET /api/characters/{name} returns 404 when character not found")
    void getCharacterReturns404() throws Exception {
        when(characterService.getCharacter("nobody"))
                .thenThrow(new CharacterNotFoundException("No character found with name 'NOBODY'"));

        mockMvc.perform(get("/api/characters/nobody"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.message").value("No character found with name 'NOBODY'"));
    }

    // -----------------------------------------------------------------------
    // PATCH /api/characters/{name}/gear
    // -----------------------------------------------------------------------

    @Test
    @DisplayName("PATCH /api/characters/{name}/gear returns 200 on success")
    void equipGearReturns200() throws Exception {
        CharacterDTO charDto = emptyCharacterDTO();
        EquipResponseDTO response = new EquipResponseDTO(charDto, List.of(EquipmentSlot.HEAD), List.of());
        when(characterService.equipGear(eq("jaraxxus"), any())).thenReturn(response);

        mockMvc.perform(patch("/api/characters/jaraxxus/gear")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "slots": [
                                    { "slot": "HEAD", "itemName": "Leather Helm" }
                                  ]
                                }
                                """))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.character.name").value("JARAXXUS"))
                .andExpect(jsonPath("$.equipped[0]").value("HEAD"))
                .andExpect(jsonPath("$.notFound").isArray());
    }

    @Test
    @DisplayName("PATCH /api/characters/{name}/gear returns 400 when gear validation fails")
    void equipGearReturns400OnValidationFailure() throws Exception {
        List<RejectedSlotDTO> rejections = List.of(
                new RejectedSlotDTO(EquipmentSlot.HEAD, "DEMON_HUNTER cannot equip Plate armor"));
        when(characterService.equipGear(eq("jaraxxus"), any()))
                .thenThrow(new GearValidationException("Armor type not allowed for this class", rejections));

        mockMvc.perform(patch("/api/characters/jaraxxus/gear")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "slots": [
                                    { "slot": "HEAD", "itemName": "Plate Helm" }
                                  ]
                                }
                                """))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value("Armor type not allowed for this class"))
                .andExpect(jsonPath("$.rejected[0].slot").value("HEAD"))
                .andExpect(jsonPath("$.rejected[0].reason").value("DEMON_HUNTER cannot equip Plate armor"));
    }

    @Test
    @DisplayName("PATCH /api/characters/{name}/gear returns 404 when character not found")
    void equipGearReturns404WhenCharacterMissing() throws Exception {
        when(characterService.equipGear(eq("nobody"), any()))
                .thenThrow(new CharacterNotFoundException("No character found with name 'NOBODY'"));

        mockMvc.perform(patch("/api/characters/nobody/gear")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "slots": [
                                    { "slot": "HEAD", "itemName": "Helmet" }
                                  ]
                                }
                                """))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.message").value("No character found with name 'NOBODY'"));
    }

    // -----------------------------------------------------------------------
    // DELETE /api/characters/{name}/gear
    // -----------------------------------------------------------------------

    @Test
    @DisplayName("DELETE /api/characters/{name}/gear returns 200 with updated character")
    void unequipGearReturns200() throws Exception {
        when(characterService.unequipGear(eq("jaraxxus"), any())).thenReturn(emptyCharacterDTO());

        mockMvc.perform(delete("/api/characters/jaraxxus/gear")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "slots": ["HEAD", "CHEST"]
                                }
                                """))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.name").value("JARAXXUS"))
                .andExpect(jsonPath("$.equipment").isArray());
    }

    @Test
    @DisplayName("DELETE /api/characters/{name}/gear returns 404 when character not found")
    void unequipGearReturns404WhenCharacterMissing() throws Exception {
        when(characterService.unequipGear(eq("nobody"), any()))
                .thenThrow(new CharacterNotFoundException("No character found with name 'NOBODY'"));

        mockMvc.perform(delete("/api/characters/nobody/gear")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "slots": ["HEAD"]
                                }
                                """))
                .andExpect(status().isNotFound());
    }
}
