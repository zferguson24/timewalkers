package com.wow.timewalkers.service;

import com.wow.timewalkers.entity.Weapon;
import com.wow.timewalkers.enums.WowClass;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

// Pure unit test — no Spring context needed. GearValidator has no Spring dependencies,
// so we just instantiate it directly. Tests run instantly with no DB or wire-up overhead.
class GearValidatorTest {

    private GearValidator validator;

    @BeforeEach
    void setUp() {
        validator = new GearValidator();
    }

    // -----------------------------------------------------------------------
    // Helper factory methods — keep test bodies readable
    // -----------------------------------------------------------------------

    private Weapon weapon(String weaponSlot, String weaponType) {
        Weapon w = new Weapon();
        w.setWeaponSlot(weaponSlot);
        w.setWeaponType(weaponType);
        w.setWeaponStat("Agility");
        w.setName("Test Weapon");
        w.setExpansion("Classic");
        w.setCost(0);
        return w;
    }

    // -----------------------------------------------------------------------
    // isArmorTypeAllowed
    // -----------------------------------------------------------------------

    @Nested
    @DisplayName("isArmorTypeAllowed")
    class IsArmorTypeAllowed {

        @Test
        @DisplayName("Agnostic armor is allowed for every class")
        void agnosticAllowedForAll() {
            for (WowClass wc : WowClass.values()) {
                assertThat(validator.isArmorTypeAllowed(wc, "Agnostic"))
                        .as("Agnostic should be allowed for %s", wc)
                        .isTrue();
            }
        }

        @Test
        @DisplayName("Cloth classes can wear Cloth")
        void clothClasses() {
            for (WowClass wc : new WowClass[]{WowClass.MAGE, WowClass.PRIEST, WowClass.WARLOCK}) {
                assertThat(validator.isArmorTypeAllowed(wc, "Cloth")).isTrue();
            }
        }

        @Test
        @DisplayName("Cloth classes cannot wear Plate")
        void clothClassesCannotWearPlate() {
            assertThat(validator.isArmorTypeAllowed(WowClass.MAGE, "Plate")).isFalse();
            assertThat(validator.isArmorTypeAllowed(WowClass.PRIEST, "Plate")).isFalse();
            assertThat(validator.isArmorTypeAllowed(WowClass.WARLOCK, "Plate")).isFalse();
        }

        @Test
        @DisplayName("Leather classes can wear Leather")
        void leatherClasses() {
            for (WowClass wc : new WowClass[]{WowClass.DEMON_HUNTER, WowClass.DRUID, WowClass.MONK, WowClass.ROGUE}) {
                assertThat(validator.isArmorTypeAllowed(wc, "Leather")).isTrue();
            }
        }

        @Test
        @DisplayName("Leather classes cannot wear Mail")
        void leatherClassesCannotWearMail() {
            assertThat(validator.isArmorTypeAllowed(WowClass.DEMON_HUNTER, "Mail")).isFalse();
            assertThat(validator.isArmorTypeAllowed(WowClass.ROGUE, "Mail")).isFalse();
        }

        @Test
        @DisplayName("Mail classes can wear Mail")
        void mailClasses() {
            for (WowClass wc : new WowClass[]{WowClass.EVOKER, WowClass.HUNTER, WowClass.SHAMAN}) {
                assertThat(validator.isArmorTypeAllowed(wc, "Mail")).isTrue();
            }
        }

        @Test
        @DisplayName("Mail classes cannot wear Cloth")
        void mailClassesCannotWearCloth() {
            assertThat(validator.isArmorTypeAllowed(WowClass.HUNTER, "Cloth")).isFalse();
            assertThat(validator.isArmorTypeAllowed(WowClass.SHAMAN, "Cloth")).isFalse();
        }

        @Test
        @DisplayName("Plate classes can wear Plate")
        void plateClasses() {
            for (WowClass wc : new WowClass[]{WowClass.DEATH_KNIGHT, WowClass.PALADIN, WowClass.WARRIOR}) {
                assertThat(validator.isArmorTypeAllowed(wc, "Plate")).isTrue();
            }
        }

        @Test
        @DisplayName("Plate classes cannot wear Leather")
        void plateClassesCannotWearLeather() {
            assertThat(validator.isArmorTypeAllowed(WowClass.WARRIOR, "Leather")).isFalse();
            assertThat(validator.isArmorTypeAllowed(WowClass.PALADIN, "Leather")).isFalse();
        }

        @Test
        @DisplayName("Demon Hunter cannot wear Plate (key class/armor restriction)")
        void demonHunterCannotWearPlate() {
            assertThat(validator.isArmorTypeAllowed(WowClass.DEMON_HUNTER, "Plate")).isFalse();
        }
    }

    // -----------------------------------------------------------------------
    // canDualWield
    // -----------------------------------------------------------------------

    @Nested
    @DisplayName("canDualWield")
    class CanDualWield {

        @Test
        @DisplayName("Dual-wield classes return true")
        void dualWieldClasses() {
            for (WowClass wc : new WowClass[]{
                    WowClass.DEATH_KNIGHT, WowClass.DEMON_HUNTER, WowClass.MONK,
                    WowClass.ROGUE, WowClass.SHAMAN, WowClass.WARRIOR}) {
                assertThat(validator.canDualWield(wc)).as("%s should dual wield", wc).isTrue();
            }
        }

        @Test
        @DisplayName("Non-dual-wield classes return false")
        void nonDualWieldClasses() {
            for (WowClass wc : new WowClass[]{
                    WowClass.DRUID, WowClass.EVOKER, WowClass.HUNTER,
                    WowClass.MAGE, WowClass.PALADIN, WowClass.PRIEST,
                    WowClass.WARLOCK}) {
                assertThat(validator.canDualWield(wc)).as("%s should not dual wield", wc).isFalse();
            }
        }
    }

    // -----------------------------------------------------------------------
    // canUseShield
    // -----------------------------------------------------------------------

    @Nested
    @DisplayName("canUseShield")
    class CanUseShield {

        @Test
        @DisplayName("Shield classes return true")
        void shieldClasses() {
            assertThat(validator.canUseShield(WowClass.PALADIN)).isTrue();
            assertThat(validator.canUseShield(WowClass.SHAMAN)).isTrue();
            assertThat(validator.canUseShield(WowClass.WARRIOR)).isTrue();
        }

        @Test
        @DisplayName("Non-shield classes return false")
        void nonShieldClasses() {
            assertThat(validator.canUseShield(WowClass.MAGE)).isFalse();
            assertThat(validator.canUseShield(WowClass.ROGUE)).isFalse();
            assertThat(validator.canUseShield(WowClass.DEMON_HUNTER)).isFalse();
            assertThat(validator.canUseShield(WowClass.DRUID)).isFalse();
        }
    }

    // -----------------------------------------------------------------------
    // isTwoHandedOrRanged
    // -----------------------------------------------------------------------

    @Nested
    @DisplayName("isTwoHandedOrRanged")
    class IsTwoHandedOrRanged {

        @Test
        @DisplayName("weapon_slot '2H' returns true")
        void twoHanded() {
            assertThat(validator.isTwoHandedOrRanged(weapon("2H", "Sword"))).isTrue();
        }

        @Test
        @DisplayName("weapon_slot 'Ranged' returns true")
        void ranged() {
            assertThat(validator.isTwoHandedOrRanged(weapon("Ranged", "Bow"))).isTrue();
        }

        @Test
        @DisplayName("weapon_slot '1H' returns false")
        void oneHanded() {
            assertThat(validator.isTwoHandedOrRanged(weapon("1H", "Sword"))).isFalse();
        }

        @Test
        @DisplayName("weapon_slot 'Offhand' returns false")
        void offhand() {
            assertThat(validator.isTwoHandedOrRanged(weapon("Offhand", "Shield"))).isFalse();
        }
    }

    // -----------------------------------------------------------------------
    // validateForMainHand
    // -----------------------------------------------------------------------

    @Nested
    @DisplayName("validateForMainHand")
    class ValidateForMainHand {

        @Test
        @DisplayName("Offhand-slot item (Shield) is rejected from main hand")
        void offhandSlotItemRejected() {
            String result = validator.validateForMainHand(WowClass.WARRIOR, weapon("Offhand", "Shield"));
            assertThat(result).isNotNull().contains("off-hand slot");
        }

        @Test
        @DisplayName("Held In Off-hand item is rejected from main hand")
        void heldInOffhandRejected() {
            String result = validator.validateForMainHand(WowClass.MAGE, weapon("Offhand", "Held In Off-hand"));
            assertThat(result).isNotNull().contains("off-hand slot");
        }

        @Test
        @DisplayName("Ranged weapon by non-Hunter is rejected")
        void rangedByNonHunter() {
            String result = validator.validateForMainHand(WowClass.WARRIOR, weapon("Ranged", "Bow"));
            assertThat(result).isNotNull().contains("Hunter");
        }

        @Test
        @DisplayName("Ranged weapon by Hunter is accepted")
        void rangedByHunter() {
            String result = validator.validateForMainHand(WowClass.HUNTER, weapon("Ranged", "Bow"));
            assertThat(result).isNull();
        }

        @Test
        @DisplayName("2H Sword is valid for Death Knight")
        void twoHandedSwordForDK() {
            assertThat(validator.validateForMainHand(WowClass.DEATH_KNIGHT, weapon("2H", "Sword"))).isNull();
        }

        @Test
        @DisplayName("Staff is rejected for Demon Hunter main hand")
        void staffRejectedForDH() {
            String result = validator.validateForMainHand(WowClass.DEMON_HUNTER, weapon("1H", "Staff"));
            assertThat(result).isNotNull().contains("DEMON_HUNTER");
        }

        @Test
        @DisplayName("Axe is valid for Demon Hunter main hand")
        void axeValidForDH() {
            assertThat(validator.validateForMainHand(WowClass.DEMON_HUNTER, weapon("1H", "Axe"))).isNull();
        }

        @Test
        @DisplayName("Warglaive is valid for Demon Hunter main hand")
        void warglaiveValidForDH() {
            assertThat(validator.validateForMainHand(WowClass.DEMON_HUNTER, weapon("1H", "Warglaive"))).isNull();
        }

        @Test
        @DisplayName("Wand is valid for Mage main hand")
        void wandValidForMage() {
            assertThat(validator.validateForMainHand(WowClass.MAGE, weapon("1H", "Wand"))).isNull();
        }

        @Test
        @DisplayName("Wand is rejected for Warrior main hand")
        void wandRejectedForWarrior() {
            String result = validator.validateForMainHand(WowClass.WARRIOR, weapon("1H", "Wand"));
            assertThat(result).isNotNull().contains("WARRIOR");
        }

        @Test
        @DisplayName("Gun is Hunter-only ranged weapon")
        void gunHunterOnly() {
            assertThat(validator.validateForMainHand(WowClass.HUNTER, weapon("Ranged", "Gun"))).isNull();
            assertThat(validator.validateForMainHand(WowClass.ROGUE, weapon("Ranged", "Gun"))).isNotNull();
        }

        @Test
        @DisplayName("Polearm is valid for Paladin")
        void polearmValidForPaladin() {
            assertThat(validator.validateForMainHand(WowClass.PALADIN, weapon("2H", "Polearm"))).isNull();
        }

        @Test
        @DisplayName("Polearm is invalid for Mage")
        void polearmInvalidForMage() {
            assertThat(validator.validateForMainHand(WowClass.MAGE, weapon("2H", "Polearm"))).isNotNull();
        }
    }

    // -----------------------------------------------------------------------
    // validateForOffHand
    // -----------------------------------------------------------------------

    @Nested
    @DisplayName("validateForOffHand")
    class ValidateForOffHand {

        @Test
        @DisplayName("2H weapon is rejected from off-hand")
        void twoHandedRejected() {
            String result = validator.validateForOffHand(WowClass.WARRIOR, weapon("2H", "Sword"));
            assertThat(result).isNotNull().contains("Two-handed");
        }

        @Test
        @DisplayName("Ranged weapon is rejected from off-hand")
        void rangedRejected() {
            String result = validator.validateForOffHand(WowClass.HUNTER, weapon("Ranged", "Bow"));
            assertThat(result).isNotNull().contains("Two-handed and ranged");
        }

        @Test
        @DisplayName("Shield is accepted by Paladin")
        void shieldForPaladin() {
            assertThat(validator.validateForOffHand(WowClass.PALADIN, weapon("Offhand", "Shield"))).isNull();
        }

        @Test
        @DisplayName("Shield is accepted by Warrior")
        void shieldForWarrior() {
            assertThat(validator.validateForOffHand(WowClass.WARRIOR, weapon("Offhand", "Shield"))).isNull();
        }

        @Test
        @DisplayName("Shield is accepted by Shaman")
        void shieldForShaman() {
            assertThat(validator.validateForOffHand(WowClass.SHAMAN, weapon("Offhand", "Shield"))).isNull();
        }

        @Test
        @DisplayName("Shield is rejected by Demon Hunter")
        void shieldRejectedForDH() {
            String result = validator.validateForOffHand(WowClass.DEMON_HUNTER, weapon("Offhand", "Shield"));
            assertThat(result).isNotNull().contains("cannot use shields");
        }

        @Test
        @DisplayName("Shield is rejected by Mage")
        void shieldRejectedForMage() {
            String result = validator.validateForOffHand(WowClass.MAGE, weapon("Offhand", "Shield"));
            assertThat(result).isNotNull().contains("cannot use shields");
        }

        @Test
        @DisplayName("Held In Off-hand is accepted by Mage")
        void frillForMage() {
            assertThat(validator.validateForOffHand(WowClass.MAGE, weapon("Offhand", "Held In Off-hand"))).isNull();
        }

        @Test
        @DisplayName("Held In Off-hand is accepted by all frill classes")
        void frillForAllFrillClasses() {
            for (WowClass wc : new WowClass[]{
                    WowClass.DRUID, WowClass.EVOKER, WowClass.MAGE,
                    WowClass.PRIEST, WowClass.WARLOCK}) {
                assertThat(validator.validateForOffHand(wc, weapon("Offhand", "Held In Off-hand")))
                        .as("%s should use Held In Off-hand", wc)
                        .isNull();
            }
        }

        @Test
        @DisplayName("Held In Off-hand is rejected by Rogue (dual-wield, not frill)")
        void frillRejectedForRogue() {
            String result = validator.validateForOffHand(WowClass.ROGUE, weapon("Offhand", "Held In Off-hand"));
            assertThat(result).isNotNull().contains("cannot use off-hand frill");
        }

        @Test
        @DisplayName("Held In Off-hand is rejected by Warrior")
        void frillRejectedForWarrior() {
            String result = validator.validateForOffHand(WowClass.WARRIOR, weapon("Offhand", "Held In Off-hand"));
            assertThat(result).isNotNull().contains("cannot use off-hand frill");
        }

        @Test
        @DisplayName("1H Axe in off-hand accepted by Rogue (dual wield)")
        void oneHandedOffHandForRogue() {
            assertThat(validator.validateForOffHand(WowClass.ROGUE, weapon("1H", "Axe"))).isNull();
        }

        @Test
        @DisplayName("1H Sword in off-hand accepted by Demon Hunter (dual wield)")
        void oneHandedOffHandForDH() {
            assertThat(validator.validateForOffHand(WowClass.DEMON_HUNTER, weapon("1H", "Sword"))).isNull();
        }

        @Test
        @DisplayName("1H weapon in off-hand rejected by Mage (no dual wield)")
        void oneHandedOffHandRejectedForMage() {
            String result = validator.validateForOffHand(WowClass.MAGE, weapon("1H", "Dagger"));
            assertThat(result).isNotNull().contains("cannot dual wield");
        }

        @Test
        @DisplayName("1H weapon in off-hand rejected by Hunter (no dual wield)")
        void oneHandedOffHandRejectedForHunter() {
            String result = validator.validateForOffHand(WowClass.HUNTER, weapon("1H", "Dagger"));
            assertThat(result).isNotNull().contains("cannot dual wield");
        }

        @Test
        @DisplayName("1H wrong weapon type in off-hand rejected even for dual-wield class")
        void wrongWeaponTypeInOffHandForDualWield() {
            // Rogues cannot use Maces? Check — yes, Rogue list: Axe, Dagger, Fist Weapon, Mace, Sword
            // Rogues cannot use Staves. Let's verify: Warrior cannot use Wands.
            String result = validator.validateForOffHand(WowClass.WARRIOR, weapon("1H", "Wand"));
            assertThat(result).isNotNull().contains("WARRIOR");
        }

        @Test
        @DisplayName("Warglaive in off-hand accepted by Demon Hunter (dual wield + correct type)")
        void warglaiveOffHandForDH() {
            assertThat(validator.validateForOffHand(WowClass.DEMON_HUNTER, weapon("1H", "Warglaive"))).isNull();
        }

        @Test
        @DisplayName("Death Knight can dual wield Axes")
        void dkDualWieldAxe() {
            assertThat(validator.validateForOffHand(WowClass.DEATH_KNIGHT, weapon("1H", "Axe"))).isNull();
        }

        @Test
        @DisplayName("Unknown Offhand item type returns an error message")
        void unknownOffhandItemType() {
            String result = validator.validateForOffHand(WowClass.WARRIOR, weapon("Offhand", "Mystery Item"));
            assertThat(result).isNotNull().contains("Unknown off-hand item type");
        }
    }
}
