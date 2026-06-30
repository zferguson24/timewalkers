package com.wow.timewalkers.service;

import com.wow.timewalkers.dto.*;
import com.wow.timewalkers.entity.ArmorPiece;
import com.wow.timewalkers.entity.CharacterEquipment;
import com.wow.timewalkers.entity.Weapon;
import com.wow.timewalkers.entity.WowCharacter;
import com.wow.timewalkers.enums.EquipmentSlot;
import com.wow.timewalkers.enums.ItemType;
import com.wow.timewalkers.enums.WowClass;
import com.wow.timewalkers.exception.CharacterNameConflictException;
import com.wow.timewalkers.exception.CharacterNotFoundException;
import com.wow.timewalkers.exception.GearValidationException;
import com.wow.timewalkers.mapper.CharacterMapper;
import com.wow.timewalkers.repository.ArmorPieceRepository;
import com.wow.timewalkers.repository.CharacterEquipmentRepository;
import com.wow.timewalkers.repository.CharacterRepository;
import com.wow.timewalkers.repository.WeaponRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

// @Transactional on the class means every public method runs inside a database transaction.
// If a RuntimeException is thrown, the transaction is rolled back automatically.
// Individual methods can override this with their own @Transactional settings (e.g. readOnly).
@Service
@Transactional
public class CharacterService {

    private static final Logger log = LoggerFactory.getLogger(CharacterService.class);

    private final CharacterRepository characterRepository;
    private final CharacterEquipmentRepository equipmentRepository;
    private final ArmorPieceRepository armorPieceRepository;
    private final WeaponRepository weaponRepository;
    private final CharacterMapper characterMapper;
    private final GearValidator gearValidator;
    private final CharacterValidator characterValidator;

    public CharacterService(CharacterRepository characterRepository,
                            CharacterEquipmentRepository equipmentRepository,
                            ArmorPieceRepository armorPieceRepository,
                            WeaponRepository weaponRepository,
                            CharacterMapper characterMapper,
                            GearValidator gearValidator,
                            CharacterValidator characterValidator) {
        this.characterRepository = characterRepository;
        this.equipmentRepository = equipmentRepository;
        this.armorPieceRepository = armorPieceRepository;
        this.weaponRepository = weaponRepository;
        this.characterMapper = characterMapper;
        this.gearValidator = gearValidator;
        this.characterValidator = characterValidator;
    }

    public CharacterDTO createCharacter(CreateCharacterRequest request) {
        characterValidator.validateRaceClassCombination(request.race(), request.characterClass());
        // Normalize name to uppercase before any DB interaction
        String name = request.name().toUpperCase();
        log.debug("Creating character: name={}, race={}, class={}", name, request.race(), request.characterClass());
        if (characterRepository.existsByName(name)) {
            throw new CharacterNameConflictException("A character named '" + name + "' already exists");
        }
        WowCharacter character = new WowCharacter();
        character.setName(name);
        character.setRace(request.race());
        character.setCharacterClass(request.characterClass());
        character.setGender(request.gender());
        // save() either inserts (new entity) or updates (existing entity with an id set).
        // It returns the managed entity with its generated id populated.
        characterRepository.save(character);
        log.debug("Character created: {}", name);
        return characterMapper.toCharacterDTO(character, List.of());
    }

    @Transactional(readOnly = true)
    public List<CharacterSummaryDTO> getAllCharacters() {
        List<CharacterSummaryDTO> result = characterRepository.findAll().stream()
                .map(c -> new CharacterSummaryDTO(c.getName(), c.getRace(), c.getCharacterClass(), c.getGender()))
                .toList();
        log.debug("Fetched {} characters", result.size());
        return result;
    }

    // readOnly = true is a performance hint to the JPA provider — Hibernate can skip
    // dirty-checking (detecting changed fields) since no writes will occur.
    @Transactional(readOnly = true)
    public CharacterDTO getCharacter(String name) {
        log.debug("Fetching character: {}", name.toUpperCase());
        WowCharacter character = findCharacter(name);
        List<CharacterEquipment> equipment = equipmentRepository.findByWowCharacter(character);
        log.debug("Character {} found — {} equipment slots loaded", character.getName(), equipment.size());
        return characterMapper.toCharacterDTO(character, equipment);
    }

    public CharacterDTO equipGear(String name, EquipRequest request) {
        WowCharacter character = findCharacter(name);
        WowClass wowClass = character.getCharacterClass();
        List<EquipSlotRequest> slotRequests = request.slots();

        // Step 1: Look up every requested item by exact name (case-insensitive).
        // Items that exist go into the found maps; items that don't go into notFound.
        // Using LinkedHashMap to preserve the order slots were specified in the request.
        Map<EquipmentSlot, ArmorPiece> foundArmor = new LinkedHashMap<>();
        Map<EquipmentSlot, Weapon> foundWeapons = new LinkedHashMap<>();
        List<EquipmentSlot> notFound = new ArrayList<>();
        List<String> notFoundNames = new ArrayList<>();

        for (EquipSlotRequest sr : slotRequests) {
            EquipmentSlot slot = sr.slot();
            if (slot.getItemType() == ItemType.ARMOR) {
                // ifPresentOrElse is an Optional convenience — runs one branch if a value
                // exists, the other if it's empty, avoiding explicit null checks
                armorPieceRepository.findByNameIgnoreCase(sr.itemName())
                        .ifPresentOrElse(
                                ap -> foundArmor.put(slot, ap),
                                () -> { notFound.add(slot); notFoundNames.add(sr.itemName()); });
            } else {
                weaponRepository.findByNameIgnoreCase(sr.itemName())
                        .ifPresentOrElse(
                                w -> foundWeapons.put(slot, w),
                                () -> { notFound.add(slot); notFoundNames.add(sr.itemName()); });
            }
        }
        log.debug("equipGear [{}] step 1 — {} armor found, {} weapons found, {} items not found{}",
                name, foundArmor.size(), foundWeapons.size(), notFound.size(),
                notFoundNames.isEmpty() ? "" : ": " + notFoundNames);

        // Step 2: Armor type validation — reject the whole request if any found armor
        // item is the wrong type for this character's class (e.g. Plate on a Mage).
        List<RejectedSlotDTO> armorRejections = new ArrayList<>();
        for (Map.Entry<EquipmentSlot, ArmorPiece> entry : foundArmor.entrySet()) {
            if (!entry.getKey().isAgnostic()
                    && !gearValidator.isArmorTypeAllowed(wowClass, entry.getValue().getArmorType())) {
                armorRejections.add(new RejectedSlotDTO(entry.getKey(),
                        wowClass.name() + " cannot equip " + entry.getValue().getArmorType() + " armor"));
            }
        }
        if (!armorRejections.isEmpty()) {
            // Throwing here causes the @Transactional proxy to roll back the transaction.
            // GlobalExceptionHandler catches this and returns a 400 response.
            throw new GearValidationException("Armor type not allowed for this class", armorRejections);
        }
        log.debug("equipGear [{}] step 2 — armor type validation passed", name);

        // Step 3: Uniqueness check for rings and trinkets.
        // The same item cannot occupy both slots of a paired group simultaneously.
        List<RejectedSlotDTO> uniquenessRejections = new ArrayList<>();
        List<List<EquipmentSlot>> pairedSlotGroups = List.of(
                List.of(EquipmentSlot.FINGER_1, EquipmentSlot.FINGER_2),
                List.of(EquipmentSlot.TRINKET_1, EquipmentSlot.TRINKET_2)
        );
        for (List<EquipmentSlot> pair : pairedSlotGroups) {
            EquipmentSlot slotA = pair.get(0);
            EquipmentSlot slotB = pair.get(1);
            boolean aInRequest = foundArmor.containsKey(slotA);
            boolean bInRequest = foundArmor.containsKey(slotB);
            if (!aInRequest && !bInRequest) continue;

            String nameA = aInRequest ? foundArmor.get(slotA).getName()
                    : equipmentRepository.findByWowCharacterAndSlot(character, slotA)
                            .map(ce -> ce.getArmorPiece() != null ? ce.getArmorPiece().getName() : null)
                            .orElse(null);
            String nameB = bInRequest ? foundArmor.get(slotB).getName()
                    : equipmentRepository.findByWowCharacterAndSlot(character, slotB)
                            .map(ce -> ce.getArmorPiece() != null ? ce.getArmorPiece().getName() : null)
                            .orElse(null);

            if (nameA != null && nameA.equalsIgnoreCase(nameB)) {
                String msg = "\"" + nameA + "\" is already equipped another slot";
                if (aInRequest) uniquenessRejections.add(new RejectedSlotDTO(slotA, msg));
                if (bInRequest) uniquenessRejections.add(new RejectedSlotDTO(slotB, msg));
            }
        }
        if (!uniquenessRejections.isEmpty()) {
            throw new GearValidationException("Duplicate unique item", uniquenessRejections);
        }
        log.debug("equipGear [{}] step 3 — uniqueness check passed", name);

        // Step 4: Weapon validation (cross-checks the effective main-hand against off-hand).
        // First, determine the effective main-hand: what the character currently has equipped
        // (or what's being newly equipped), used to catch the 2H + off-hand conflict.
        boolean requestHasOffHand = slotRequests.stream()
                .anyMatch(sr -> sr.slot() == EquipmentSlot.OFF_HAND);

        Weapon effectiveMainHand = foundWeapons.containsKey(EquipmentSlot.MAIN_HAND)
                ? foundWeapons.get(EquipmentSlot.MAIN_HAND)
                // If MAIN_HAND isn't in the request, check what's currently equipped
                : equipmentRepository.findByWowCharacterAndSlot(character, EquipmentSlot.MAIN_HAND)
                        .map(CharacterEquipment::getWeapon)
                        .orElse(null);

        if (requestHasOffHand && effectiveMainHand != null
                && gearValidator.isTwoHandedOrRanged(effectiveMainHand)) {
            throw new GearValidationException(
                    "Cannot equip an off-hand item when the main-hand is a two-handed or ranged weapon",
                    List.of(
                            new RejectedSlotDTO(EquipmentSlot.MAIN_HAND,
                                    "Two-handed/ranged weapon conflicts with off-hand"),
                            new RejectedSlotDTO(EquipmentSlot.OFF_HAND,
                                    "Cannot equip off-hand alongside a two-handed or ranged main-hand")));
        }

        // Validate each weapon's type and slot eligibility for this class
        List<RejectedSlotDTO> weaponRejections = new ArrayList<>();
        for (Map.Entry<EquipmentSlot, Weapon> entry : foundWeapons.entrySet()) {
            String error = entry.getKey() == EquipmentSlot.MAIN_HAND
                    ? gearValidator.validateForMainHand(wowClass, entry.getValue())
                    : gearValidator.validateForOffHand(wowClass, entry.getValue());
            if (error != null) {
                weaponRejections.add(new RejectedSlotDTO(entry.getKey(), error));
            }
        }
        if (!weaponRejections.isEmpty()) {
            throw new GearValidationException("Weapon not allowed for this class", weaponRejections);
        }
        log.debug("equipGear [{}] step 4 — weapon validation passed", name);

        // Step 5: All validations passed — persist equipment.
        for (Map.Entry<EquipmentSlot, ArmorPiece> entry : foundArmor.entrySet()) {
            upsertEquipment(character, entry.getKey(), ItemType.ARMOR, entry.getValue(), null);
        }

        for (Map.Entry<EquipmentSlot, Weapon> entry : foundWeapons.entrySet()) {
            upsertEquipment(character, entry.getKey(), ItemType.WEAPON, null, entry.getValue());
            // Equipping a 2H or ranged weapon clears the off-hand slot
            if (entry.getKey() == EquipmentSlot.MAIN_HAND
                    && gearValidator.isTwoHandedOrRanged(entry.getValue())) {
                equipmentRepository.deleteByWowCharacterAndSlotIn(character, List.of(EquipmentSlot.OFF_HAND));
            }
        }
        log.debug("equipGear [{}] step 5 — persisted {} armor, {} weapons", name, foundArmor.size(), foundWeapons.size());

        List<CharacterEquipment> allEquipment = equipmentRepository.findByWowCharacter(character);
        return characterMapper.toCharacterDTO(character, allEquipment);
    }

    public CharacterDTO unequipGear(String name, UnequipRequest request) {
        log.debug("Unequipping {} slots from {}", request.slots().size(), name.toUpperCase());
        WowCharacter character = findCharacter(name);
        equipmentRepository.deleteByWowCharacterAndSlotIn(character, request.slots());
        List<CharacterEquipment> allEquipment = equipmentRepository.findByWowCharacter(character);
        return characterMapper.toCharacterDTO(character, allEquipment);
    }

    // Private helper shared by all methods that need a character — centralizes the
    // 404 logic so each public method doesn't repeat it.
    private WowCharacter findCharacter(String name) {
        return characterRepository.findByName(name.toUpperCase())
                .orElseThrow(() -> new CharacterNotFoundException(
                        "No character found with name '" + name.toUpperCase() + "'"));
    }

    // Upsert pattern: find an existing equipment row for this slot or create a new one,
    // then set all fields and save. JPA's save() detects whether to INSERT or UPDATE
    // based on whether the entity has an id set.
    private void upsertEquipment(WowCharacter character, EquipmentSlot slot,
                                  ItemType itemType, ArmorPiece armorPiece, Weapon weapon) {
        CharacterEquipment ce = equipmentRepository.findByWowCharacterAndSlot(character, slot)
                .orElse(new CharacterEquipment());
        ce.setWowCharacter(character);
        ce.setSlot(slot);
        ce.setItemType(itemType);
        ce.setArmorPiece(armorPiece);
        ce.setWeapon(weapon);
        equipmentRepository.save(ce);
    }
}
