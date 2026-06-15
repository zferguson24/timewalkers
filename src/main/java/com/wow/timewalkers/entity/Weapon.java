package com.wow.timewalkers.entity;

import jakarta.persistence.*;

@Entity
@Table(name = "weapons")
public class Weapon {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // weapon_slot distinguishes where the weapon goes: '1H', '2H', 'Offhand', 'Ranged'
    @Column(name = "weapon_slot", nullable = false)
    private String weaponSlot;

    // weapon_stat indicates the primary scaling stat: Strength, Agility, Intellect
    @Column(name = "weapon_stat", nullable = false)
    private String weaponStat;

    // weapon_type is the specific weapon category: Sword, Axe, Staff, Wand, Shield, etc.
    @Column(name = "weapon_type", nullable = false)
    private String weaponType;

    @Column(nullable = false)
    private String name;

    @Column(nullable = false)
    private String expansion;

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

    public String getWeaponSlot() { return weaponSlot; }
    public void setWeaponSlot(String weaponSlot) { this.weaponSlot = weaponSlot; }

    public String getWeaponStat() { return weaponStat; }
    public void setWeaponStat(String weaponStat) { this.weaponStat = weaponStat; }

    public String getWeaponType() { return weaponType; }
    public void setWeaponType(String weaponType) { this.weaponType = weaponType; }

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
