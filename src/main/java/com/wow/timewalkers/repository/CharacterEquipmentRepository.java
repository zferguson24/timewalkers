package com.wow.timewalkers.repository;

import com.wow.timewalkers.entity.CharacterEquipment;
import com.wow.timewalkers.entity.WowCharacter;
import com.wow.timewalkers.enums.EquipmentSlot;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Collection;
import java.util.List;
import java.util.Optional;

@Repository
public interface CharacterEquipmentRepository extends JpaRepository<CharacterEquipment, Long> {

    // Loads all equipped slots for a character — used to build the full loadout response
    List<CharacterEquipment> findByWowCharacter(WowCharacter character);

    // Looks up a single slot for a character — used by the upsert logic and 2H conflict check
    Optional<CharacterEquipment> findByWowCharacterAndSlot(WowCharacter character, EquipmentSlot slot);

    // Spring Data derived delete: performs a SELECT then deletes each found entity.
    // The Collection parameter maps to SQL IN (...), allowing bulk slot removal.
    // Requires a transaction (provided by the @Transactional service that calls it).
    void deleteByWowCharacterAndSlotIn(WowCharacter character, Collection<EquipmentSlot> slots);
}
