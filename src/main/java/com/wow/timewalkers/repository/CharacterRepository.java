package com.wow.timewalkers.repository;

import com.wow.timewalkers.entity.WowCharacter;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface CharacterRepository extends JpaRepository<WowCharacter, Long> {

    // Names are stored in uppercase, so callers must uppercase before querying.
    // Returns Optional — the service calls .orElseThrow() to produce a 404 if empty.
    Optional<WowCharacter> findByName(String name);

    // Derived existence check — generates SELECT COUNT(*) > 0 ... under the hood.
    // More efficient than findByName(...).isPresent() since it avoids loading the entity.
    boolean existsByName(String name);
}
