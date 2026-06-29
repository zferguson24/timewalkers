package com.wow.timewalkers.controller;

import com.wow.timewalkers.dto.*;
import com.wow.timewalkers.enums.WowClass;
import com.wow.timewalkers.enums.WowRace;
import com.wow.timewalkers.service.CharacterService;
import com.wow.timewalkers.service.CharacterValidator;
import com.wow.timewalkers.service.GearPlanService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/characters")
public class CharacterController {

    private final CharacterService characterService;
    private final GearPlanService gearPlanService;
    private final CharacterValidator characterValidator;

    public CharacterController(CharacterService characterService, GearPlanService gearPlanService,
                               CharacterValidator characterValidator) {
        this.characterService = characterService;
        this.gearPlanService = gearPlanService;
        this.characterValidator = characterValidator;
    }

    // GET /api/characters/creation-info — static data needed to render the character creation form.
    // Fetched in parallel with the character list on the characters screen.
    @GetMapping("/creation-info")
    public ResponseEntity<Map<WowClass, List<WowRace>>> getCreationInfo() {
        return ResponseEntity.ok(characterValidator.getAllValidCombinations());
    }

    // GET /api/characters — list all characters (name, race, class only)
    @GetMapping
    public ResponseEntity<List<CharacterSummaryDTO>> getAllCharacters() {
        return ResponseEntity.ok(characterService.getAllCharacters());
    }

    // @PostMapping handles HTTP POST — used for resource creation.
    // @RequestBody tells Spring to deserialize the JSON request body into
    // a CreateCharacterRequest record using Jackson.
    // Returns 201 Created instead of the default 200.
    @PostMapping
    public ResponseEntity<CharacterDTO> createCharacter(@RequestBody CreateCharacterRequest request) {
        return ResponseEntity.status(201).body(characterService.createCharacter(request));
    }

    // @PathVariable extracts the {name} segment from the URL path.
    // Name is transformed to uppercase in the service before querying.
    @GetMapping("/{name}")
    public ResponseEntity<CharacterDTO> getCharacter(@PathVariable String name) {
        return ResponseEntity.ok(characterService.getCharacter(name));
    }

    // @PatchMapping handles HTTP PATCH — conventionally used for partial updates.
    // Only the slots listed in the request body are modified; others are left as-is.
    @PatchMapping("/{name}/gear")
    public ResponseEntity<CharacterDTO> equipGear(@PathVariable String name,
                                                   @RequestBody EquipRequest request) {
        return ResponseEntity.ok(characterService.equipGear(name, request));
    }

    // @DeleteMapping with a request body specifies which slots to clear.
    // Returns the full updated character loadout after removal.
    @DeleteMapping("/{name}/gear")
    public ResponseEntity<CharacterDTO> unequipGear(@PathVariable String name,
                                                     @RequestBody UnequipRequest request) {
        return ResponseEntity.ok(characterService.unequipGear(name, request));
    }

    // Returns the optimal Timewalking event order to fully equip this character.
    // preferredStat is used for hybrid classes (Druid, Monk, Paladin, Shaman) to select
    // which primary stat (Agility/Strength/Intellect) to optimise for; ignored for pure classes.
    @GetMapping("/{name}/gear-plan")
    public ResponseEntity<GearPlanResponseDTO> getGearPlan(
            @PathVariable String name,
            @RequestParam(required = false) String preferredStat) {
        return ResponseEntity.ok(gearPlanService.computeGearPlan(name, preferredStat));
    }
}
