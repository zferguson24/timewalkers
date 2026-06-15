package com.wow.timewalkers.mapper;

import com.wow.timewalkers.dto.ArmorPieceDTO;
import com.wow.timewalkers.dto.WeaponDTO;
import com.wow.timewalkers.entity.ArmorPiece;
import com.wow.timewalkers.entity.Weapon;
import org.springframework.stereotype.Component;

// Mapper classes convert JPA entities into DTOs before they leave the service layer.
// Keeping entities out of API responses prevents accidental exposure of internal
// fields and decouples the DB schema from the API contract.
@Component
public class GearMapper {

    // Records use positional canonical constructors, so arguments must match
    // the order declared in the record definition exactly.
    public ArmorPieceDTO toArmorPieceDTO(ArmorPiece entity) {
        return new ArmorPieceDTO(
                entity.getArmorType(),
                entity.getSlot(),
                entity.getName(),
                entity.getExpansion(),
                entity.getPrimaryStat(),
                entity.getSecondaryStat(),
                entity.getCost(),
                entity.getNotes(),
                entity.getWowheadUrl(),
                entity.getIconUrl()
        );
    }

    public WeaponDTO toWeaponDTO(Weapon entity) {
        return new WeaponDTO(
                entity.getWeaponSlot(),
                entity.getWeaponStat(),
                entity.getWeaponType(),
                entity.getName(),
                entity.getExpansion(),
                entity.getPrimaryStat(),
                entity.getSecondaryStat(),
                entity.getCost(),
                entity.getNotes(),
                entity.getWowheadUrl(),
                entity.getIconUrl()
        );
    }
}
