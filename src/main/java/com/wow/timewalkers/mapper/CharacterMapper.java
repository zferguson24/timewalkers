package com.wow.timewalkers.mapper;

import com.wow.timewalkers.dto.CharacterDTO;
import com.wow.timewalkers.dto.EquippedSlotDTO;
import com.wow.timewalkers.entity.CharacterEquipment;
import com.wow.timewalkers.entity.WowCharacter;
import com.wow.timewalkers.enums.EquipmentSlot;
import com.wow.timewalkers.enums.ItemType;
import org.springframework.stereotype.Component;

import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Component
public class CharacterMapper {

    // GearMapper is injected here to delegate item-level mapping
    private final GearMapper gearMapper;

    public CharacterMapper(GearMapper gearMapper) {
        this.gearMapper = gearMapper;
    }

    public CharacterDTO toCharacterDTO(WowCharacter character, List<CharacterEquipment> equipment) {
        // Build a lookup map from slot -> equipment row for O(1) access below
        Map<EquipmentSlot, CharacterEquipment> equippedBySlot = equipment.stream()
                .collect(Collectors.toMap(CharacterEquipment::getSlot, e -> e));

        // Iterate over every possible slot in declaration order so the response
        // always includes all 16 slots, whether equipped or not.
        // Arrays.stream(EquipmentSlot.values()) produces them in enum declaration order.
        List<EquippedSlotDTO> slots = Arrays.stream(EquipmentSlot.values())
                .map(slot -> {
                    CharacterEquipment ce = equippedBySlot.get(slot);
                    if (ce == null) {
                        return new EquippedSlotDTO(slot, false, null);
                    }
                    // Determine the item type and delegate to the appropriate mapper method.
                    // The item field is typed as Object; Jackson serializes it correctly
                    // based on the actual runtime type (ArmorPieceDTO or WeaponDTO).
                    Object item = ce.getItemType() == ItemType.ARMOR
                            ? gearMapper.toArmorPieceDTO(ce.getArmorPiece())
                            : gearMapper.toWeaponDTO(ce.getWeapon());
                    return new EquippedSlotDTO(slot, true, item);
                })
                .toList();

        return new CharacterDTO(character.getName(), character.getRace(), character.getCharacterClass(), character.getGender(), slots);
    }
}
