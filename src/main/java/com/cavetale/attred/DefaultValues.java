package com.cavetale.attred;

import org.bukkit.Material;

/**
 * All values are taken from https://minecraft.gamepedia.com/
 */
final class DefaultValues {
    private DefaultValues() { }

    static double getDefaultArmor(Material mat) {
        switch (mat) {
        case LEATHER_HELMET: return 1;
        case LEATHER_CHESTPLATE: return 3;
        case LEATHER_LEGGINGS: return 2;
        case LEATHER_BOOTS: return 1;
        case GOLD_HELMET: return 2;
        case GOLD_CHESTPLATE: return 5;
        case GOLD_LEGGINGS: return 3;
        case GOLD_BOOTS: return 1;
        case CHAINMAIL_HELMET: return 2;
        case CHAINMAIL_CHESTPLATE: return 5;
        case CHAINMAIL_LEGGINGS: return 4;
        case CHAINMAIL_BOOTS: return 1;
        case IRON_HELMET: return 2;
        case IRON_CHESTPLATE: return 6;
        case IRON_LEGGINGS: return 5;
        case IRON_BOOTS: return 2;
        case DIAMOND_HELMET: return 3;
        case DIAMOND_CHESTPLATE: return 8;
        case DIAMOND_LEGGINGS: return 6;
        case DIAMOND_BOOTS: return 3;
        default: return 0;
        }
    }

    static double getDefaultArmorToughness(Material mat) {
        switch (mat) {
        case DIAMOND_HELMET: return 3;
        case DIAMOND_CHESTPLATE: return 8;
        case DIAMOND_LEGGINGS: return 6;
        case DIAMOND_BOOTS: return 3;
        default: return 0;
        }
    }

    static double getDefaultAttackDamage(Material mat) {
        switch (mat) {
        case WOOD_SWORD: return 4;
        case GOLD_SWORD: return 4;
        case STONE_SWORD: return 5;
        case IRON_SWORD: return 6;
        case DIAMOND_SWORD: return 7;
            // Axes
        case WOOD_AXE: return 7;
        case GOLD_AXE: return 7;
        case STONE_AXE: return 9;
        case IRON_AXE: return 9;
        case DIAMOND_AXE: return 9;
            // Pickaxes
        case WOOD_PICKAXE: return 2;
        case GOLD_PICKAXE: return 2;
        case STONE_PICKAXE: return 3;
        case IRON_PICKAXE: return 4;
        case DIAMOND_PICKAXE: return 5;
            // Shovel
        case WOOD_SPADE: return 2.5;
        case GOLD_SPADE: return 2.5;
        case STONE_SPADE: return 3.5;
        case IRON_SPADE: return 4.5;
        case DIAMOND_SPADE: return 5.5;
            // Hoe
        case WOOD_HOE: return 1;
        case GOLD_HOE: return 1;
        case STONE_HOE: return 1;
        case IRON_HOE: return 1;
        case DIAMOND_HOE: return 1;
        default: return 0;
        }
    }

    static double getDefaultAttackSpeed(Material mat) {
        switch (mat) {
        case WOOD_SWORD:
        case GOLD_SWORD:
        case STONE_SWORD:
        case IRON_SWORD:
        case DIAMOND_SWORD:
            return 1.6;
            // Axes
        case WOOD_AXE: return 0.8;
        case GOLD_AXE: return 1.0;
        case STONE_AXE: return 0.8;
        case IRON_AXE: return 0.9;
        case DIAMOND_AXE: return 1.0;
            // Pickaxes
        case WOOD_PICKAXE: return 1.2;
        case STONE_PICKAXE: return 1.2;
        case GOLD_PICKAXE: return 1.2;
        case IRON_PICKAXE: return 1.2;
        case DIAMOND_PICKAXE: return 1.2;
            // Shovels
        case WOOD_SPADE: return 1.0;
        case GOLD_SPADE: return 1.0;
        case STONE_SPADE: return 1.0;
        case IRON_SPADE: return 1.0;
        case DIAMOND_SPADE: return 1.0;
            // Hoes
        case WOOD_HOE: return 1.0;
        case GOLD_HOE: return 1.0;
        case STONE_HOE: return 2.0;
        case IRON_HOE: return 3.0;
        case DIAMOND_HOE: return 4.0;
        default: return 0;
        }
    }
}
