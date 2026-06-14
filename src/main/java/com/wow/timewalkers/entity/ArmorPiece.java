package com.wow.timewalkers.entity;

import jakarta.persistence.*;

// @Entity tells JPA/Hibernate that this class maps to a database table.
// Each instance of this class represents one row in that table.
@Entity
// @Table specifies the exact table name. Without it, Hibernate would default
// to the class name, which would be "armor_piece" (not "armor_pieces").
@Table(name = "armor_pieces")
public class ArmorPiece {

    // @Id marks this field as the primary key
    // @GeneratedValue with IDENTITY delegates ID generation to the database
    // (BIGSERIAL in PostgreSQL — auto-incrementing column)
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // @Column lets us map to a column whose name differs from the field name,
    // and enforce constraints like nullable = false (validated by Hibernate)
    @Column(name = "armor_type", nullable = false)
    private String armorType;

    @Column(nullable = false)
    private String slot;

    @Column(nullable = false)
    private String name;

    @Column(nullable = false)
    private String expansion;

    // Nullable columns — some items have no stated primary or secondary stat
    @Column(name = "primary_stat")
    private String primaryStat;

    @Column(name = "secondary_stat")
    private String secondaryStat;

    @Column(nullable = false)
    private Integer cost;

    @Column
    private String notes;

    @Column(name = "wowhead_url")
    private String wowheadUrl;

    @Column(name = "icon_url")
    private String iconUrl;

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public String getArmorType() { return armorType; }
    public void setArmorType(String armorType) { this.armorType = armorType; }

    public String getSlot() { return slot; }
    public void setSlot(String slot) { this.slot = slot; }

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public String getExpansion() { return expansion; }
    public void setExpansion(String expansion) { this.expansion = expansion; }

    public String getPrimaryStat() { return primaryStat; }
    public void setPrimaryStat(String primaryStat) { this.primaryStat = primaryStat; }

    public String getSecondaryStat() { return secondaryStat; }
    public void setSecondaryStat(String secondaryStat) { this.secondaryStat = secondaryStat; }

    public Integer getCost() { return cost; }
    public void setCost(Integer cost) { this.cost = cost; }

    public String getNotes() { return notes; }
    public void setNotes(String notes) { this.notes = notes; }

    public String getWowheadUrl() { return wowheadUrl; }
    public void setWowheadUrl(String wowheadUrl) { this.wowheadUrl = wowheadUrl; }

    public String getIconUrl() { return iconUrl; }
    public void setIconUrl(String iconUrl) { this.iconUrl = iconUrl; }
}
