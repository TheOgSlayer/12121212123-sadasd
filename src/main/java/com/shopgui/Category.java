package com.shopgui;

import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.format.TextColor;

/** The seven shops, in the order they appear in /shop. */
public enum Category {
    END("end", "End Shop", NamedTextColor.DARK_PURPLE),
    NETHER("nether", "Nether Shop", NamedTextColor.RED),
    GEAR("gear", "Gear Shop", NamedTextColor.AQUA),
    FOOD("food", "Food Shop", NamedTextColor.GREEN),
    OVERWORLD("overworld", "Overworld Shop", NamedTextColor.DARK_GREEN),
    MINERALS("minerals", "Minerals Shop", NamedTextColor.GOLD),
    SHARDS("shards", "Shards Shop", NamedTextColor.LIGHT_PURPLE);

    private final String key;
    private final String title;
    private final TextColor color;

    Category(String key, String title, TextColor color) {
        this.key = key;
        this.title = title;
        this.color = color;
    }

    /** Section name in prices.yml. */
    public String key() { return key; }

    public String title() { return title; }

    public TextColor color() { return color; }

    public boolean isShards() { return this == SHARDS; }
}
