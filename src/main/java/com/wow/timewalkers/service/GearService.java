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
import org.springframework.stereotype.Service;

import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

// @Service marks this class as a Spring-managed service bean.
// It is functionally equivalent to @Component but communicates intent —
// this layer contains business logic and sits between the controller and repository.
@Service
public class GearService {

    private final ArmorPieceRepository armorPieceRepository;
    private final WeaponRepository weaponRepository;
    private final GearMapper gearMapper;

    public GearService(ArmorPieceRepository armorPieceRepository,
                       WeaponRepository weaponRepository,
                       GearMapper gearMapper) {
        this.armorPieceRepository = armorPieceRepository;
        this.weaponRepository = weaponRepository;
        this.gearMapper = gearMapper;
    }

    // findAll() is provided by JpaRepository and issues a SELECT * on the table.
    // The stream + map pattern converts each entity to a DTO before returning,
    // keeping JPA entities out of the API response.
    public List<ArmorPieceDTO> getAllArmorPieces() {
        return armorPieceRepository.findAll()
                .stream()
                .map(gearMapper::toArmorPieceDTO)
                .toList();
    }

    public List<WeaponDTO> getAllWeapons() {
        return weaponRepository.findAll()
                .stream()
                .map(gearMapper::toWeaponDTO)
                .toList();
    }

    // Spring Data derives the SQL query from the method name:
    // findByNameContainingIgnoreCase -> WHERE LOWER(name) LIKE LOWER('%?%')
    public List<ArmorPieceDTO> getArmorPiecesByName(String name) {
        return armorPieceRepository.findByNameContainingIgnoreCase(name)
                .stream()
                .map(gearMapper::toArmorPieceDTO)
                .toList();
    }

    public List<WeaponDTO> getWeaponsByName(String name) {
        return weaponRepository.findByNameContainingIgnoreCase(name)
                .stream()
                .map(gearMapper::toWeaponDTO)
                .toList();
    }

    // Fires two independent partial-match queries and combines results into a single DTO
    public ExpansionGearDTO getGearByExpansion(String expansion) {
        List<ArmorPieceDTO> armor = armorPieceRepository.findByExpansionContainingIgnoreCase(expansion)
                .stream()
                .map(gearMapper::toArmorPieceDTO)
                .toList();

        List<WeaponDTO> weapons = weaponRepository.findByExpansionContainingIgnoreCase(expansion)
                .stream()
                .map(gearMapper::toWeaponDTO)
                .toList();

        return new ExpansionGearDTO(expansion, armor, weapons);
    }

    // findByArmorTypeContainingIgnoreCase -> WHERE LOWER(armor_type) LIKE LOWER('%?%')
    public List<ArmorPieceDTO> getArmorPiecesByType(String armorType) {
        return armorPieceRepository.findByArmorTypeContainingIgnoreCase(armorType)
                .stream()
                .map(gearMapper::toArmorPieceDTO)
                .toList();
    }

    // findByWeaponTypeContainingIgnoreCase -> WHERE LOWER(weapon_type) LIKE LOWER('%?%')
    public List<WeaponDTO> getWeaponsByType(String weaponType) {
        return weaponRepository.findByWeaponTypeContainingIgnoreCase(weaponType)
                .stream()
                .map(gearMapper::toWeaponDTO)
                .toList();
    }

    // Unified search: fans out across name, expansion, armor type, and weapon type queries.
    // Results are deduplicated by item name (names are unique in the dataset) and sorted
    // alphabetically before mapping to DTOs.
    public GearSearchResultDTO searchGear(String query) {
        Map<String, ArmorPiece> armorByName = new LinkedHashMap<>();
        Map<String, Weapon> weaponByName = new LinkedHashMap<>();

        armorPieceRepository.findByNameContainingIgnoreCase(query)
                .forEach(ap -> armorByName.put(ap.getName(), ap));
        weaponRepository.findByNameContainingIgnoreCase(query)
                .forEach(w -> weaponByName.put(w.getName(), w));

        armorPieceRepository.findByExpansionContainingIgnoreCase(query)
                .forEach(ap -> armorByName.put(ap.getName(), ap));
        weaponRepository.findByExpansionContainingIgnoreCase(query)
                .forEach(w -> weaponByName.put(w.getName(), w));

        armorPieceRepository.findByArmorTypeContainingIgnoreCase(query)
                .forEach(ap -> armorByName.put(ap.getName(), ap));
        armorPieceRepository.findBySlotContainingIgnoreCase(query)
                .forEach(ap -> armorByName.put(ap.getName(), ap));

        weaponRepository.findByWeaponTypeContainingIgnoreCase(query)
                .forEach(w -> weaponByName.put(w.getName(), w));
        weaponRepository.findByWeaponSlotContainingIgnoreCase(query)
                .forEach(w -> weaponByName.put(w.getName(), w));

        List<ArmorPieceDTO> armor = armorByName.values().stream()
                .sorted(Comparator.comparing(ArmorPiece::getName, String.CASE_INSENSITIVE_ORDER))
                .map(gearMapper::toArmorPieceDTO)
                .toList();

        List<WeaponDTO> weapons = weaponByName.values().stream()
                .sorted(Comparator.comparing(Weapon::getName, String.CASE_INSENSITIVE_ORDER))
                .map(gearMapper::toWeaponDTO)
                .toList();

        return new GearSearchResultDTO(armor, weapons);
    }
}
