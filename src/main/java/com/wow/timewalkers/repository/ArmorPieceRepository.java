package com.wow.timewalkers.repository;

import com.wow.timewalkers.entity.ArmorPiece;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

// @Repository marks this interface as a Spring Data repository.
// It's technically optional here (JpaRepository is enough for component scanning),
// but adds clarity and enables Spring to translate JPA exceptions into
// Spring's DataAccessException hierarchy.
//
// JpaRepository<ArmorPiece, Long> provides standard CRUD methods out of the box:
//   save(), findById(), findAll(), deleteById(), existsById(), count(), etc.
// Spring Data generates the implementation at runtime — no SQL or boilerplate needed.
@Repository
public interface ArmorPieceRepository extends JpaRepository<ArmorPiece, Long> {

    // Spring Data derives the full SQL query from the method name at startup.
    // findByNameIgnoreCase -> SELECT * FROM armor_pieces WHERE LOWER(name) = LOWER(?)
    // Returns Optional because there may be zero or one exact match.
    Optional<ArmorPiece> findByNameIgnoreCase(String name);

    // findByNameContainingIgnoreCase -> WHERE LOWER(name) LIKE LOWER('%?%')
    // Used for the partial-match search endpoints.
    List<ArmorPiece> findByNameContainingIgnoreCase(String name);

    // findByExpansionContainingIgnoreCase -> WHERE LOWER(expansion) LIKE LOWER('%?%')
    List<ArmorPiece> findByExpansionContainingIgnoreCase(String expansion);

    // findByArmorTypeContainingIgnoreCase -> WHERE LOWER(armor_type) LIKE LOWER('%?%')
    List<ArmorPiece> findByArmorTypeContainingIgnoreCase(String armorType);

    // findBySlotContainingIgnoreCase -> WHERE LOWER(slot) LIKE LOWER('%?%')
    List<ArmorPiece> findBySlotContainingIgnoreCase(String slot);

    // Returns the distinct slot names (e.g. "Head", "Finger") offered by an expansion
    // for the given armor types. Used by GearPlanService to determine which equipment
    // slots an upcoming event can cover for a character.
    @Query("SELECT DISTINCT a.slot FROM ArmorPiece a WHERE a.expansion = :expansion AND a.armorType IN :armorTypes")
    List<String> findDistinctSlotsByExpansionAndArmorTypes(
            @Param("expansion") String expansion,
            @Param("armorTypes") List<String> armorTypes);

    // Returns all items for the given expansion + slot + armor type combination.
    // The caller filters by primary stat compatibility and limits to the number needed.
    List<ArmorPiece> findByExpansionAndSlotAndArmorTypeIn(
            String expansion, String slot, java.util.Collection<String> armorTypes);
}
