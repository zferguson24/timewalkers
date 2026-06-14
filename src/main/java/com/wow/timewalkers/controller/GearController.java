package com.wow.timewalkers.controller;

import com.wow.timewalkers.dto.ArmorPieceDTO;
import com.wow.timewalkers.dto.ExpansionGearDTO;
import com.wow.timewalkers.dto.WeaponDTO;
import com.wow.timewalkers.service.GearService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

// @RestController combines @Controller (registers this class as a Spring MVC controller)
// and @ResponseBody (automatically serializes return values to JSON via Jackson).
// Without @ResponseBody, Spring would try to resolve a view template instead.
@RestController
// @RequestMapping sets a base URL prefix for all endpoints in this class
@RequestMapping("/api/gear")
public class GearController {

    // Constructor injection — Spring sees a single constructor and injects the
    // GearService bean automatically. Preferred over field injection (@Autowired)
    // because it makes dependencies explicit and testable.
    private final GearService gearService;

    public GearController(GearService gearService) {
        this.gearService = gearService;
    }

    // @GetMapping is shorthand for @RequestMapping(method = RequestMethod.GET).
    // ResponseEntity lets us control the full HTTP response (status, headers, body).
    // ResponseEntity.ok() sets status 200 and wraps the body.

    // GET /api/gear/armor — returns all armor pieces
    @GetMapping("/armor")
    public ResponseEntity<List<ArmorPieceDTO>> getAllArmor() {
        return ResponseEntity.ok(gearService.getAllArmorPieces());
    }

    // GET /api/gear/weapons — returns all weapons
    @GetMapping("/weapons")
    public ResponseEntity<List<WeaponDTO>> getAllWeapons() {
        return ResponseEntity.ok(gearService.getAllWeapons());
    }

    // @RequestParam binds a query parameter (?name=...) to the method argument.
    // Spring returns 400 automatically if a required @RequestParam is missing.

    // GET /api/gear/armor/search?name=... — partial, case-insensitive name search
    @GetMapping("/armor/search")
    public ResponseEntity<List<ArmorPieceDTO>> searchArmor(@RequestParam String name) {
        return ResponseEntity.ok(gearService.getArmorPiecesByName(name));
    }

    // GET /api/gear/weapons/search?name=... — partial, case-insensitive name search
    @GetMapping("/weapons/search")
    public ResponseEntity<List<WeaponDTO>> searchWeapons(@RequestParam String name) {
        return ResponseEntity.ok(gearService.getWeaponsByName(name));
    }

    // GET /api/gear/expansion?name=... — all armor + weapons for a given expansion
    @GetMapping("/expansion")
    public ResponseEntity<ExpansionGearDTO> getGearByExpansion(@RequestParam String name) {
        return ResponseEntity.ok(gearService.getGearByExpansion(name));
    }

    // GET /api/gear/armor/type?name=... — armor filtered by type (Plate, Mail, etc.)
    @GetMapping("/armor/type")
    public ResponseEntity<List<ArmorPieceDTO>> getArmorByType(@RequestParam String name) {
        return ResponseEntity.ok(gearService.getArmorPiecesByType(name));
    }
}
