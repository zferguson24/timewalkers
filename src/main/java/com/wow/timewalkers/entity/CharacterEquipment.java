package com.wow.timewalkers.entity;

import com.wow.timewalkers.enums.EquipmentSlot;
import com.wow.timewalkers.enums.ItemType;
import jakarta.persistence.*;

// Junction table between a character and the items they have equipped.
// Each row represents one slot for one character. The UNIQUE constraint on
// (character_id, slot) ensures a character can only have one item per slot.
@Entity
@Table(name = "character_equipment",
        uniqueConstraints = @UniqueConstraint(columnNames = {"character_id", "slot"}))
public class CharacterEquipment {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // @ManyToOne — many equipment rows can belong to one character.
    // FetchType.LAZY means the WowCharacter is not loaded from the DB until accessed.
    // This avoids loading the parent every time an equipment row is fetched.
    // @JoinColumn specifies the foreign key column name in this table.
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "character_id", nullable = false)
    private WowCharacter wowCharacter;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private EquipmentSlot slot;

    // Discriminator that tells us which of the two FK columns below is populated
    @Enumerated(EnumType.STRING)
    @Column(name = "item_type", nullable = false)
    private ItemType itemType;

    // Exactly one of these two FK columns will be non-null per row,
    // determined by itemType. LAZY fetch avoids loading the item until needed.
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "armor_piece_id")
    private ArmorPiece armorPiece;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "weapon_id")
    private Weapon weapon;

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public WowCharacter getWowCharacter() { return wowCharacter; }
    public void setWowCharacter(WowCharacter wowCharacter) { this.wowCharacter = wowCharacter; }

    public EquipmentSlot getSlot() { return slot; }
    public void setSlot(EquipmentSlot slot) { this.slot = slot; }

    public ItemType getItemType() { return itemType; }
    public void setItemType(ItemType itemType) { this.itemType = itemType; }

    public ArmorPiece getArmorPiece() { return armorPiece; }
    public void setArmorPiece(ArmorPiece armorPiece) { this.armorPiece = armorPiece; }

    public Weapon getWeapon() { return weapon; }
    public void setWeapon(Weapon weapon) { this.weapon = weapon; }
}
