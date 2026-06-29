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
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.*;
import java.util.stream.Stream;

@Service
@Transactional(readOnly = true)
public class GearPlanService {

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
    private static final Set<String> VALID_STATS = Set.of("Strength", "Agility", "Intellect");

    static {
        FIXED_STAT_BY_CLASS.put(WowClass.DEATH_KNIGHT, "Strength");
        FIXED_STAT_BY_CLASS.put(WowClass.WARRIOR,      "Strength");
        FIXED_STAT_BY_CLASS.put(WowClass.DEMON_HUNTER, "Agility");
        FIXED_STAT_BY_CLASS.put(WowClass.HUNTER,       "Agility");
        FIXED_STAT_BY_CLASS.put(WowClass.ROGUE,        "Agility");
        FIXED_STAT_BY_CLASS.put(WowClass.EVOKER,       "Intellect");
        FIXED_STAT_BY_CLASS.put(WowClass.MAGE,         "Intellect");
        FIXED_STAT_BY_CLASS.put(WowClass.PRIEST,       "Intellect");
        FIXED_STAT_BY_CLASS.put(WowClass.WARLOCK,      "Intellect");

        DEFAULT_STAT_BY_CLASS.put(WowClass.DRUID,   "Agility");
        DEFAULT_STAT_BY_CLASS.put(WowClass.MONK,    "Agility");
        DEFAULT_STAT_BY_CLASS.put(WowClass.SHAMAN,  "Agility");
        DEFAULT_STAT_BY_CLASS.put(WowClass.PALADIN, "Strength");
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

        List<CharacterEquipment> currentEquipment = equipmentRepository.findByWowCharacter(character);
        Set<EquipmentSlot> filledSlots = new HashSet<>();
        Set<String> equippedItemNames = new HashSet<>();
        boolean mainHandIs2HOrRanged = false;

        for (CharacterEquipment ce : currentEquipment) {
            filledSlots.add(ce.getSlot());
            if (ce.getArmorPiece() != null)
                equippedItemNames.add(ce.getArmorPiece().getName().toLowerCase());
            else if (ce.getWeapon() != null)
                equippedItemNames.add(ce.getWeapon().getName().toLowerCase());
            if (ce.getSlot() == EquipmentSlot.MAIN_HAND && ce.getWeapon() != null
                    && gearValidator.isTwoHandedOrRanged(ce.getWeapon())) {
                mainHandIs2HOrRanged = true;
            }
        }

        Set<EquipmentSlot> unfilled = new LinkedHashSet<>();
        for (EquipmentSlot slot : ALL_SLOTS) {
            if (!filledSlots.contains(slot)) {
                if (slot == EquipmentSlot.OFF_HAND && mainHandIs2HOrRanged) continue;
                unfilled.add(slot);
            }
        }

        List<String> alreadyEquipped = filledSlots.stream()
                .map(EquipmentSlot::name)
                .sorted()
                .toList();

        List<TimewalkingEvent> upcomingEvents = twEventRepository
                .findByStartDateGreaterThanEqualOrderByStartDateAsc(LocalDate.now());

        List<GearPlanEventDTO> planEvents = new ArrayList<>();
        int cumulativeFilled = filledSlots.size();

        for (TimewalkingEvent event : upcomingEvents) {
            if (unfilled.isEmpty()) break;

            Map<EquipmentSlot, GearPlanSlotDTO> covered = resolveCoveredSlots(
                    event.getExpansion(), wowClass, armorType, resolvedStat, unfilled, equippedItemNames);
            if (covered.isEmpty()) continue;

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
        return List.of(defaultStat, "Intellect");
    }

    // Returns the stat used to filter the gear plan. Pure classes always use their fixed stat.
    // Hybrid classes use preferredStat when valid, falling back to their non-Intellect default.
    private String resolveEffectiveStat(WowClass wowClass, String preferredStat) {
        String fixed = FIXED_STAT_BY_CLASS.get(wowClass);
        if (fixed != null) return fixed;
        if (preferredStat != null && VALID_STATS.contains(preferredStat)) return preferredStat;
        return DEFAULT_STAT_BY_CLASS.get(wowClass);
    }

    // Returns true if the item's combined text fields contain a primary stat keyword that
    // matches requiredStat, or if the item has no primary stat keyword at all (pure rating item).
    // Items explicitly marked "No Primary Stat" are treated as universally compatible.
    private boolean armorMatchesStat(ArmorPiece ap, String requiredStat) {
        if (ap.getNotes() != null && ap.getNotes().contains("No Primary Stat")) return true;

        String combined = Stream.of(ap.getPrimaryStat(), ap.getSecondaryStat(), ap.getNotes())
                .filter(Objects::nonNull)
                .collect(java.util.stream.Collectors.joining(" "))
                .toLowerCase();

        boolean hasAnyPrimaryStat = combined.contains("strength")
                || combined.contains("agility")
                || combined.contains("intellect");
        if (!hasAnyPrimaryStat) return true;

        return combined.contains(requiredStat.toLowerCase());
    }

    // Returns true if the weapon's stat is compatible with requiredStat, or if the
    // weapon has no stat designation at all.
    private boolean weaponMatchesStat(Weapon w, String requiredStat) {
        String stat = w.getWeaponStat();
        if (stat == null) return true;
        String lower = stat.toLowerCase();
        boolean hasAnyPrimaryStat = lower.contains("strength")
                || lower.contains("agility")
                || lower.contains("intellect");
        if (!hasAnyPrimaryStat) return true;
        return lower.contains(requiredStat.toLowerCase());
    }

    private Map<EquipmentSlot, GearPlanSlotDTO> resolveCoveredSlots(
            String expansion, WowClass wowClass, String armorType,
            String resolvedStat, Set<EquipmentSlot> unfilled, Set<String> equippedItemNames) {

        Map<EquipmentSlot, GearPlanSlotDTO> covered = new LinkedHashMap<>();

        List<String> armorSlots = armorPieceRepository.findDistinctSlotsByExpansionAndArmorTypes(
                expansion, List.of(armorType, "Agnostic"));

        for (String dbSlot : armorSlots) {
            List<EquipmentSlot> mapped = ARMOR_SLOT_MAP.get(dbSlot);
            if (mapped == null) continue;

            List<EquipmentSlot> unfilledMapped = mapped.stream().filter(unfilled::contains).toList();
            if (unfilledMapped.isEmpty()) continue;

            // Fetch all candidates for this slot, filter by primary stat compatibility,
            // then take only as many as there are unfilled slots to fill.
            List<ArmorPiece> candidates = armorPieceRepository
                    .findByExpansionAndSlotAndArmorTypeIn(expansion, dbSlot, List.of(armorType, "Agnostic"))
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
                    && !equippedItemNames.contains(weapon.getName().toLowerCase())
                    && gearValidator.validateForMainHand(wowClass, weapon) == null) {
                mainHandCandidate = weapon;
            }
            if (offHandCandidate == null && unfilled.contains(EquipmentSlot.OFF_HAND)
                    && weaponMatchesStat(weapon, resolvedStat)
                    && !equippedItemNames.contains(weapon.getName().toLowerCase())
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
