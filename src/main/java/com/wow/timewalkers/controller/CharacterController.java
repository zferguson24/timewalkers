package com.wow.timewalkers.controller;

import com.wow.timewalkers.dto.*;
import com.wow.timewalkers.service.CharacterService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/characters")
public class CharacterController {

    private final CharacterService characterService;

    public CharacterController(CharacterService characterService) {
        this.characterService = characterService;
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
    public ResponseEntity<EquipResponseDTO> equipGear(@PathVariable String name,
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
}
