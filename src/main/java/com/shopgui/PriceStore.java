package com.shopgui;

import java.io.File;
import java.util.ArrayList;
import java.util.EnumMap;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.logging.Logger;

import org.bukkit.Material;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.YamlConfiguration;

/** Reads prices.yml into the item lists shown by each shop. */
public final class PriceStore {

    private static final Map<String, String> DISPLAY_NAMES = new HashMap<>();
    static {
        DISPLAY_NAMES.put("EXPERIENCE_BOTTLE", "Bottle o' Enchanting");
        DISPLAY_NAMES.put("COOKED_BEEF", "Steak");
        DISPLAY_NAMES.put("DRAGON_BREATH", "Dragon's Breath");
        DISPLAY_NAMES.put("TOTEM_OF_UNDYING", "Totem of Undying");
        DISPLAY_NAMES.put("QUARTZ", "Nether Quartz");
    }

    private final Map<Category, List<ShopItem>> items = new EnumMap<>(Category.class);

    public void load(File file, Logger log) {
        items.clear();
        for (Category c : Category.values()) {
            items.put(c, new ArrayList<>());
        }

        YamlConfiguration yml = YamlConfiguration.loadConfiguration(file);
        int total = 0;
        for (Category c : Category.values()) {
            ConfigurationSection section = yml.getConfigurationSection(c.key());
            if (section == null) {
                log.warning("prices.yml has no \"" + c.key() + "\" section; that shop will be empty.");
                continue;
            }
            for (String key : section.getKeys(false)) {
                double price = section.getDouble(key, 0.0);
                if (price <= 0) {
                    continue; // price 0 = disabled
                }
                ShopItem item = c.isShards() ? shard(key, price) : material(key, price, log, c);
                if (item != null) {
                    items.get(c).add(item);
                    total++;
                }
            }
        }
        log.info("Loaded " + total + " shop items.");
    }

    public List<ShopItem> items(Category c) {
        return items.getOrDefault(c, List.of());
    }

    private static ShopItem shard(String mob, double price) {
        return new ShopItem(mob, pretty(mob) + " Shard", null, price);
    }

    private static ShopItem material(String key, double price, Logger log, Category c) {
        Material m = Material.matchMaterial(key);
        if (m == null || !m.isItem()) {
            log.warning("prices.yml: unknown item \"" + key + "\" in \"" + c.key() + "\"; skipped.");
            return null;
        }
        String name = DISPLAY_NAMES.getOrDefault(m.name(), pretty(m.name()));
        return new ShopItem(m.name(), name, m, price);
    }

    static String pretty(String raw) {
        String[] parts = raw.toLowerCase(Locale.ROOT).split("_");
        StringBuilder sb = new StringBuilder();
        for (String p : parts) {
            if (p.isEmpty()) continue;
            if (sb.length() > 0) sb.append(' ');
            sb.append(Character.toUpperCase(p.charAt(0))).append(p.substring(1));
        }
        return sb.toString();
    }
}
