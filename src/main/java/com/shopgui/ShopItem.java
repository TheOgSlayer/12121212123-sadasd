package com.shopgui;

import org.bukkit.Material;

/**
 * One thing that can be bought.
 *
 * @param id       material name (e.g. DIAMOND) or, for shards, the mob name sent to the command
 * @param name     what the player sees
 * @param material the item given to the player, or null for shards
 * @param price    price per unit
 */
public record ShopItem(String id, String name, Material material, double price) {
    public boolean isShard() {
        return material == null;
    }
}
