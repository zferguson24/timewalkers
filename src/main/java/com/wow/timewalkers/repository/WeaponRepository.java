package com.wow.timewalkers.repository;

import com.wow.timewalkers.entity.Weapon;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

// Spring Data generates the implementation of this interface at runtime,
// including all inherited JpaRepository methods and the derived queries below.
@Repository
public interface WeaponRepository extends JpaRepository<Weapon, Long> {

    // Exact match — used by the equip feature to look up a weapon by its full name
    Optional<Weapon> findByNameIgnoreCase(String name);

    // Partial match — used by the search endpoint
    List<Weapon> findByNameContainingIgnoreCase(String name);

    // Partial match on expansion — used by the expansion gear endpoint
    List<Weapon> findByExpansionContainingIgnoreCase(String expansion);

    // Partial match on weapon_type — used by the weapon type search endpoint
    List<Weapon> findByWeaponTypeContainingIgnoreCase(String weaponType);

    // findByWeaponSlotContainingIgnoreCase -> WHERE LOWER(weapon_slot) LIKE LOWER('%?%')
    List<Weapon> findByWeaponSlotContainingIgnoreCase(String weaponSlot);
}
