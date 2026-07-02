package com.wow.timewalkers.service;

import com.wow.timewalkers.dto.GearPlanEventDTO;
import com.wow.timewalkers.dto.GearPlanResponseDTO;
import com.wow.timewalkers.dto.GearPlanSlotDTO;
import com.wow.timewalkers.entity.ArmorPiece;
import com.wow.timewalkers.entity.CharacterEquipment;
import com.wow.timewalkers.entity.TimewalkingEvent;
import com.wow.timewalkers.entity.Weapon;
import com.wow.timewalkers.entity.WowCharacter;
import com.wow.timewalkers.enums.EquipmentSlot;
import com.wow.timewalkers.enums.WowClass;
import com.wow.timewalkers.exception.CharacterNotFoundException;
import com.wow.timewalkers.repository.ArmorPieceRepository;
import com.wow.timewalkers.repository.CharacterEquipmentRepository;
import com.wow.timewalkers.repository.CharacterRepository;
import com.wow.timewalkers.repository.TimewalkingEventRepository;
import com.wow.timewalkers.repository.WeaponRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.*;
import java.util.stream.Stream;

@Service
@Transactional(readOnly = true)
public class GearPlanService {

    private static final Logger log = LoggerFactory.getLogger(GearPlanService.class);

    private static final List<EquipmentSlot> ALL_SLOTS = List.of(
            EquipmentSlot.HEAD, EquipmentSlot.NECK, EquipmentSlot.SHOULDERS, EquipmentSlot.BACK,
            EquipmentSlot.CHEST, EquipmentSlot.WRIST, EquipmentSlot.HANDS, EquipmentSlot.WAIST,
            EquipmentSlot.LEGS, EquipmentSlot.FEET, EquipmentSlot.FINGER_1, EquipmentSlot.FINGER_2,
            EquipmentSlot.TRINKET_1, EquipmentSlot.TRINKET_2, EquipmentSlot.MAIN_HAND, EquipmentSlot.OFF_HAND
    );

    private static final Map<String, List<EquipmentSlot>> ARMOR_SLOT_MAP = new HashMap<>();
    static {
        ARMOR_SLOT_MAP.put("Head",      List.of(EquipmentSlot.HEAD));
        ARMOR_SLOT_MAP.put("Shoulders", List.of(EquipmentSlot.SHOULDERS));
        ARMOR_SLOT_MAP.put("Chest",     List.of(EquipmentSlot.CHEST));
        ARMOR_SLOT_MAP.put("Wrist",     List.of(EquipmentSlot.WRIST));
        ARMOR_SLOT_MAP.put("Hands",     List.of(EquipmentSlot.HANDS));
        ARMOR_SLOT_MAP.put("Waist",     List.of(EquipmentSlot.WAIST));
        ARMOR_SLOT_MAP.put("Legs",      List.of(EquipmentSlot.LEGS));
        ARMOR_SLOT_MAP.put("Feet",      List.of(EquipmentSlot.FEET));
        ARMOR_SLOT_MAP.put("Neck",      List.of(EquipmentSlot.NECK));
        ARMOR_SLOT_MAP.put("Back",      List.of(EquipmentSlot.BACK));
        ARMOR_SLOT_MAP.put("Finger",    List.of(EquipmentSlot.FINGER_1, EquipmentSlot.FINGER_2));
        ARMOR_SLOT_MAP.put("Trinket",   List.of(EquipmentSlot.TRINKET_1, EquipmentSlot.TRINKET_2));
    }

    // Pure classes have one fixed primary stat; the preferredStat param is ignored for them.
    private static final Map<WowClass, String> FIXED_STAT_BY_CLASS = new EnumMap<>(WowClass.class);
    // Hybrid classes support two stats; this is the default (their non-Intellect option).
    private static final Map<WowClass, String> DEFAULT_STAT_BY_CLASS = new EnumMap<>(WowClass.class);
    private static final Set<String> VALID_STATS = Set.of(
            GearConstants.STAT_STRENGTH, GearConstants.STAT_AGILITY, GearConstants.STAT_INTELLECT);

    static {
        FIXED_STAT_BY_CLASS.put(WowClass.DEATH_KNIGHT, GearConstants.STAT_STRENGTH);
        FIXED_STAT_BY_CLASS.put(WowClass.WARRIOR,      GearConstants.STAT_STRENGTH);
        FIXED_STAT_BY_CLASS.put(WowClass.DEMON_HUNTER, GearConstants.STAT_AGILITY);
        FIXED_STAT_BY_CLASS.put(WowClass.HUNTER,       GearConstants.STAT_AGILITY);
        FIXED_STAT_BY_CLASS.put(WowClass.ROGUE,        GearConstants.STAT_AGILITY);
        FIXED_STAT_BY_CLASS.put(WowClass.EVOKER,       GearConstants.STAT_INTELLECT);
        FIXED_STAT_BY_CLASS.put(WowClass.MAGE,         GearConstants.STAT_INTELLECT);
        FIXED_STAT_BY_CLASS.put(WowClass.PRIEST,       GearConstants.STAT_INTELLECT);
        FIXED_STAT_BY_CLASS.put(WowClass.WARLOCK,      GearConstants.STAT_INTELLECT);

        DEFAULT_STAT_BY_CLASS.put(WowClass.DRUID,   GearConstants.STAT_AGILITY);
        DEFAULT_STAT_BY_CLASS.put(WowClass.MONK,    GearConstants.STAT_AGILITY);
        DEFAULT_STAT_BY_CLASS.put(WowClass.SHAMAN,  GearConstants.STAT_AGILITY);
        DEFAULT_STAT_BY_CLASS.put(WowClass.PALADIN, GearConstants.STAT_STRENGTH);
    }

    private final CharacterRepository characterRepository;
    private final CharacterEquipmentRepository equipmentRepository;
    private final ArmorPieceRepository armorPieceRepository;
    private final WeaponRepository weaponRepository;
    private final TimewalkingEventRepository twEventRepository;
    private final GearValidator gearValidator;

    public GearPlanService(CharacterRepository characterRepository,
                           CharacterEquipmentRepository equipmentRepository,
                           ArmorPieceRepository armorPieceRepository,
                           WeaponRepository weaponRepository,
                           TimewalkingEventRepository twEventRepository,
                           GearValidator gearValidator) {
        this.characterRepository = characterRepository;
        this.equipmentRepository = equipmentRepository;
        this.armorPieceRepository = armorPieceRepository;
        this.weaponRepository = weaponRepository;
        this.twEventRepository = twEventRepository;
        this.gearValidator = gearValidator;
    }

    public GearPlanResponseDTO computeGearPlan(String name, String preferredStat) {
        WowCharacter character = characterRepository.findByName(name.toUpperCase())
                .orElseThrow(() -> new CharacterNotFoundException(
                        "No character found with name '" + name.toUpperCase() + "'"));

        WowClass wowClass = character.getCharacterClass();
        String armorType = gearValidator.getArmorType(wowClass);
        String resolvedStat = resolveEffectiveStat(wowClass, preferredStat);
        log.debug("Computing gear plan for {} ({}) — preferredStat={}, resolvedStat={}",
                character.getName(), wowClass, preferredStat, resolvedStat);

        List<CharacterEquipment> currentEquipment = equipmentRepository.findByWowCharacter(character);
        Set<EquipmentSlot> filledSlots = new HashSet<>();
        Set<String> equippedItemNames = new HashSet<>();
        boolean mainHandIs2HOrRanged = false;
        String equippedMainHandWeaponName = null;
        String equippedOffHandWeaponName = null;

        for (CharacterEquipment ce : currentEquipment) {
            filledSlots.add(ce.getSlot());
            if (ce.getArmorPiece() != null)
                equippedItemNames.add(ce.getArmorPiece().getName().toLowerCase());
            else if (ce.getWeapon() != null)
                equippedItemNames.add(ce.getWeapon().getName().toLowerCase());
            if (ce.getSlot() == EquipmentSlot.MAIN_HAND && ce.getWeapon() != null) {
                if (gearValidator.isTwoHandedOrRanged(ce.getWeapon())) {
                    mainHandIs2HOrRanged = true;
                }
                equippedMainHandWeaponName = ce.getWeapon().getName().toLowerCase();
            }
            if (ce.getSlot() == EquipmentSlot.OFF_HAND && ce.getWeapon() != null) {
                equippedOffHandWeaponName = ce.getWeapon().getName().toLowerCase();
            }
        }

        Set<EquipmentSlot> unfilled = new LinkedHashSet<>();
        for (EquipmentSlot slot : ALL_SLOTS) {
            if (!filledSlots.contains(slot)) {
                if (slot == EquipmentSlot.OFF_HAND && mainHandIs2HOrRanged) continue;
                unfilled.add(slot);
            }
        }
        log.debug("Gear plan [{}] — {} slots filled, {} unfilled", character.getName(), filledSlots.size(), unfilled.size());

        List<String> alreadyEquipped = filledSlots.stream()
                .map(EquipmentSlot::name)
                .sorted()
                .toList();

        List<TimewalkingEvent> upcomingEvents = twEventRepository
                .findByStartDateGreaterThanEqualOrderByStartDateAsc(LocalDate.now());

        if (upcomingEvents.isEmpty()) {
            log.error("No upcoming timewalking events found — timewalking_events table may be empty or unpopulated");
        } else {
            log.debug("Gear plan [{}] — {} upcoming events to evaluate", character.getName(), upcomingEvents.size());
        }

        List<GearPlanEventDTO> planEvents = new ArrayList<>();
        int cumulativeFilled = filledSlots.size();

        for (TimewalkingEvent event : upcomingEvents) {
            if (unfilled.isEmpty()) break;

            Map<EquipmentSlot, GearPlanSlotDTO> covered = resolveCoveredSlots(
                    event.getExpansion(), wowClass, armorType, resolvedStat, unfilled, equippedItemNames,
                    equippedMainHandWeaponName, equippedOffHandWeaponName);
            if (covered.isEmpty()) {
                log.debug("Gear plan [{}] — event '{}' ({}) covered 0 unfilled slots, skipping",
                        character.getName(), event.getExpansion(), event.getStartDate());
                continue;
            }
            log.debug("Gear plan [{}] — event '{}' ({}) covers {} slots: {}",
                    character.getName(), event.getExpansion(), event.getStartDate(),
                    covered.size(), covered.keySet());

            unfilled.removeAll(covered.keySet());
            cumulativeFilled += covered.size();

            List<GearPlanSlotDTO> items = covered.values().stream()
                    .filter(Objects::nonNull)
                    .toList();

            if (!items.isEmpty()) {
                planEvents.add(new GearPlanEventDTO(
                        event.getExpansion(),
                        event.getStartDate(),
                        event.getEndDate(),
                        items,
                        cumulativeFilled,
                        event.isTurbulentTimeways()
                ));
            }
        }

        List<String> unresolvable = unfilled.stream().map(EquipmentSlot::name).toList();
        boolean fullyEquipped = unresolvable.isEmpty();
        log.debug("Gear plan [{}] — {} plan events generated; fully equipped: {}", character.getName(), planEvents.size(), fullyEquipped);

        LocalDate fullyEquippedDate = null;
        if (fullyEquipped) {
            fullyEquippedDate = planEvents.isEmpty()
                    ? LocalDate.now()
                    : planEvents.get(planEvents.size() - 1).startDate();
        }

        return new GearPlanResponseDTO(
                character.getName(),
                resolvedStat,
                fullyEquipped,
                fullyEquippedDate,
                alreadyEquipped,
                unresolvable,
                planEvents,
                computeStatOptions(wowClass)
        );
    }

    private List<String> computeStatOptions(WowClass wowClass) {
        String defaultStat = DEFAULT_STAT_BY_CLASS.get(wowClass);
        if (defaultStat == null) return List.of();
        return List.of(defaultStat, GearConstants.STAT_INTELLECT);
    }

    // Returns the stat used to filter the gear plan. Pure classes always use their fixed stat.
    // Hybrid classes use preferredStat when valid, falling back to their non-Intellect default.
    private String resolveEffectiveStat(WowClass wowClass, String preferredStat) {
        String fixed = FIXED_STAT_BY_CLASS.get(wowClass);
        if (fixed != null) return fixed;
        if (preferredStat != null && VALID_STATS.contains(preferredStat)) return preferredStat;
        return DEFAULT_STAT_BY_CLASS.get(wowClass);
    }

    // Both matchers concatenate the item's stat-bearing text fields and delegate to
    // GearValidator.hasCompatibleStatText — the single place that interprets the
    // free-text stat convention (see the comment there for the rules).
    private boolean armorMatchesStat(ArmorPiece ap, String requiredStat) {
        String combined = Stream.of(ap.getPrimaryStat(), ap.getSecondaryStat(), ap.getNotes())
                .filter(Objects::nonNull)
                .collect(java.util.stream.Collectors.joining(" "));
        return gearValidator.hasCompatibleStatText(combined, requiredStat);
    }

    private boolean weaponMatchesStat(Weapon w, String requiredStat) {
        return gearValidator.hasCompatibleStatText(w.getWeaponStat(), requiredStat);
    }

    private Map<EquipmentSlot, GearPlanSlotDTO> resolveCoveredSlots(
            String expansion, WowClass wowClass, String armorType,
            String resolvedStat, Set<EquipmentSlot> unfilled, Set<String> equippedItemNames,
            String equippedMainHandWeaponName, String equippedOffHandWeaponName) {

        Map<EquipmentSlot, GearPlanSlotDTO> covered = new LinkedHashMap<>();

        List<String> armorSlots = armorPieceRepository.findDistinctSlotsByExpansionAndArmorTypes(
                expansion, List.of(armorType, GearConstants.ARMOR_AGNOSTIC));

        for (String dbSlot : armorSlots) {
            List<EquipmentSlot> mapped = ARMOR_SLOT_MAP.get(dbSlot);
            if (mapped == null) continue;

            List<EquipmentSlot> unfilledMapped = mapped.stream().filter(unfilled::contains).toList();
            if (unfilledMapped.isEmpty()) continue;

            // Fetch all candidates for this slot, filter by primary stat compatibility,
            // then take only as many as there are unfilled slots to fill.
            List<ArmorPiece> candidates = armorPieceRepository
                    .findByExpansionAndSlotAndArmorTypeIn(expansion, dbSlot, List.of(armorType, GearConstants.ARMOR_AGNOSTIC))
                    .stream()
                    .filter(ap -> armorMatchesStat(ap, resolvedStat))
                    .filter(ap -> !equippedItemNames.contains(ap.getName().toLowerCase()))
                    .limit(unfilledMapped.size())
                    .toList();

            for (int i = 0; i < candidates.size(); i++) {
                EquipmentSlot es = unfilledMapped.get(i);
                ArmorPiece ap = candidates.get(i);
                covered.put(es, new GearPlanSlotDTO(es.name(), ap.getName(), ap.getIconUrl(), ap.getCost()));
            }
        }

        List<Weapon> weapons = weaponRepository.findByExpansion(expansion);
        Weapon mainHandCandidate = null;
        Weapon offHandCandidate = null;

        for (Weapon weapon : weapons) {
            if (mainHandCandidate == null && unfilled.contains(EquipmentSlot.MAIN_HAND)
                    && weaponMatchesStat(weapon, resolvedStat)
                    && !weapon.getName().toLowerCase().equals(equippedMainHandWeaponName)
                    && (wowClass != WowClass.HUNTER || GearConstants.SLOT_RANGED.equals(weapon.getWeaponSlot()))
                    && gearValidator.validateForMainHand(wowClass, weapon) == null) {
                mainHandCandidate = weapon;
            }
            if (offHandCandidate == null && unfilled.contains(EquipmentSlot.OFF_HAND)
                    && weaponMatchesStat(weapon, resolvedStat)
                    && !weapon.getName().toLowerCase().equals(equippedOffHandWeaponName)
                    && gearValidator.validateForOffHand(wowClass, weapon) == null) {
                offHandCandidate = weapon;
            }
        }

        if (mainHandCandidate != null) {
            covered.put(EquipmentSlot.MAIN_HAND, new GearPlanSlotDTO(
                    "MAIN_HAND", mainHandCandidate.getName(),
                    mainHandCandidate.getIconUrl(), mainHandCandidate.getCost()));
            if (gearValidator.isTwoHandedOrRanged(mainHandCandidate)
                    && unfilled.contains(EquipmentSlot.OFF_HAND)) {
                covered.put(EquipmentSlot.OFF_HAND, null);
                offHandCandidate = null;
            }
        }

        if (offHandCandidate != null) {
            covered.put(EquipmentSlot.OFF_HAND, new GearPlanSlotDTO(
                    "OFF_HAND", offHandCandidate.getName(),
                    offHandCandidate.getIconUrl(), offHandCandidate.getCost()));
        }

        return covered;
    }
}
