package com.wow.timewalkers.service;

import com.wow.timewalkers.dto.ArmorPieceDTO;
import com.wow.timewalkers.dto.ExpansionGearDTO;
import com.wow.timewalkers.dto.WeaponDTO;
import com.wow.timewalkers.mapper.GearMapper;
import com.wow.timewalkers.repository.ArmorPieceRepository;
import com.wow.timewalkers.repository.WeaponRepository;
import org.springframework.stereotype.Service;

import java.util.List;

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

    // Fires two independent queries and combines the results into a single DTO
    public ExpansionGearDTO getGearByExpansion(String expansion) {
        List<ArmorPieceDTO> armor = armorPieceRepository.findByExpansionIgnoreCase(expansion)
                .stream()
                .map(gearMapper::toArmorPieceDTO)
                .toList();

        List<WeaponDTO> weapons = weaponRepository.findByExpansionIgnoreCase(expansion)
                .stream()
                .map(gearMapper::toWeaponDTO)
                .toList();

        return new ExpansionGearDTO(expansion, armor, weapons);
    }

    // findByArmorTypeIgnoreCase -> WHERE LOWER(armor_type) = LOWER(?)
    public List<ArmorPieceDTO> getArmorPiecesByType(String armorType) {
        return armorPieceRepository.findByArmorTypeIgnoreCase(armorType)
                .stream()
                .map(gearMapper::toArmorPieceDTO)
                .toList();
    }
}
