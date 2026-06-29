-- Armor pieces table (Plate, Mail, Leather, Cloth, Agnostic slots)
CREATE TABLE IF NOT EXISTS armor_pieces (
    id             BIGSERIAL    PRIMARY KEY,
    armor_type     VARCHAR(50)  NOT NULL,   -- 'Plate', 'Mail', 'Leather', 'Cloth', 'Agnostic'
    slot           VARCHAR(50)  NOT NULL,
    name           VARCHAR(150) NOT NULL,
    expansion      VARCHAR(100) NOT NULL,
    primary_stat   VARCHAR(50),
    secondary_stat VARCHAR(50),
    cost           INTEGER      NOT NULL,
    notes          TEXT,
    wowhead_url    VARCHAR(300),
    icon_url       VARCHAR(300)
);

-- Weapons table (1H, 2H, Offhand/Shield, Ranged)
CREATE TABLE IF NOT EXISTS weapons (
    id             BIGSERIAL    PRIMARY KEY,
    weapon_slot    VARCHAR(20)  NOT NULL,   -- '1H', '2H', 'Off-Hand', 'Ranged'
    weapon_stat    VARCHAR(50)  NOT NULL,   -- 'Strength', 'Agility', 'Intellect', etc.
    weapon_type    VARCHAR(30)  NOT NULL,   -- 'Sword', 'Axe', 'Mace', 'Polearm', 'Shield', etc.
    name           VARCHAR(150) NOT NULL,
    expansion      VARCHAR(100) NOT NULL,
    primary_stat   VARCHAR(50),
    secondary_stat VARCHAR(50),
    cost           INTEGER      NOT NULL,
    notes          TEXT,
    wowhead_url    VARCHAR(300),
    icon_url       VARCHAR(300)
);

CREATE INDEX IF NOT EXISTS idx_armor_expansion  ON armor_pieces(expansion);
CREATE INDEX IF NOT EXISTS idx_armor_armor_type ON armor_pieces(armor_type);
CREATE INDEX IF NOT EXISTS idx_weapon_expansion ON weapons(expansion);

CREATE TABLE IF NOT EXISTS characters (
    id              BIGSERIAL    PRIMARY KEY,
    name            VARCHAR(100) NOT NULL UNIQUE,
    race            VARCHAR(50)  NOT NULL,
    character_class VARCHAR(50)  NOT NULL,
    gender          VARCHAR(50)  NOT NULL DEFAULT 'MALE'
);

CREATE TABLE IF NOT EXISTS character_equipment (
    id             BIGSERIAL   PRIMARY KEY,
    character_id   BIGINT      NOT NULL REFERENCES characters(id),
    slot           VARCHAR(30) NOT NULL,
    item_type      VARCHAR(10) NOT NULL,
    armor_piece_id BIGINT      REFERENCES armor_pieces(id),
    weapon_id      BIGINT      REFERENCES weapons(id),
    UNIQUE (character_id, slot)
);

CREATE INDEX IF NOT EXISTS idx_char_equipment_character ON character_equipment(character_id);

-- Timewalking event schedule, populated by the TimewalkingSponge addon + sync script.
-- expansion must match the values used in armor_pieces.expansion and weapons.expansion.
CREATE TABLE IF NOT EXISTS timewalking_events (
    id                    BIGSERIAL    PRIMARY KEY,
    expansion             VARCHAR(100) NOT NULL,
    start_date            DATE         NOT NULL,
    end_date              DATE         NOT NULL,
    is_turbulent_timeways BOOLEAN      NOT NULL DEFAULT FALSE,
    CONSTRAINT uq_timewalking_event UNIQUE (expansion, start_date)
);

CREATE INDEX IF NOT EXISTS idx_tw_events_expansion   ON timewalking_events(expansion);
CREATE INDEX IF NOT EXISTS idx_tw_events_start_date  ON timewalking_events(start_date);
