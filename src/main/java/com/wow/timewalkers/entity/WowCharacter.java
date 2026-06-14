package com.wow.timewalkers.entity;

import com.wow.timewalkers.enums.WowClass;
import com.wow.timewalkers.enums.WowRace;
import jakarta.persistence.*;

// Named WowCharacter to avoid collision with java.lang.Character
@Entity
@Table(name = "characters")
public class WowCharacter {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // unique = true adds a UNIQUE constraint — enforced at both the DB level and
    // validated by Hibernate during schema validation (ddl-auto=validate)
    @Column(nullable = false, unique = true)
    private String name;

    // @Enumerated(EnumType.STRING) stores the enum constant name as a string in the DB
    // (e.g. "NIGHT_ELF") rather than its ordinal integer.
    // Ordinal storage (@Enumerated(EnumType.ORDINAL)) is fragile — reordering enum
    // constants would corrupt existing data.
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private WowRace race;

    // Field is named characterClass to avoid collision with the Java keyword 'class'.
    // @Column(name = ...) maps it to the expected DB column name.
    @Enumerated(EnumType.STRING)
    @Column(name = "character_class", nullable = false)
    private WowClass characterClass;

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public WowRace getRace() { return race; }
    public void setRace(WowRace race) { this.race = race; }

    public WowClass getCharacterClass() { return characterClass; }
    public void setCharacterClass(WowClass characterClass) { this.characterClass = characterClass; }
}
