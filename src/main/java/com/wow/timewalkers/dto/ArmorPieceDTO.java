package com.wow.timewalkers.dto;

// Java records are immutable data carriers — the compiler generates the canonical
// constructor, all accessors (armorType(), slot(), etc.), equals(), hashCode(),
// and toString() automatically. This replaces ~50 lines of a traditional POJO class.
//
// DTOs (Data Transfer Objects) exist to decouple the API response shape from the
// JPA entity. If the DB schema changes, the DTO can stay the same (or vice versa).
public record ArmorPieceDTO(
        String armorType,
        String slot,
        String name,
        String expansion,
        String primaryStat,
        String secondaryStat,
        Integer cost,
        String notes,
        String wowheadUrl
) {}
