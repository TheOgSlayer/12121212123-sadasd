package com.shopgui;

import java.io.File;
import java.util.Map;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import net.milkbowl.vault.economy.Economy;
import net.milkbowl.vault.economy.EconomyResponse;
import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.plugin.RegisteredServiceProvider;
import org.bukkit.plugin.java.JavaPlugin;

public final class ShopGUI extends JavaPlugin {

    private final PriceStore prices = new PriceStore();
    private ShopMenus menus;
    private Economy economy;

    @Override
    public void onEnable() {
        saveDefaultConfig();
        if (!new File(getDataFolder(), "prices.yml").exists()) {
            saveResource("prices.yml", false);
        }

        if (!setupEconomy()) {
            getLogger().severe("No Vault economy found (is EssentialsX installed?). Disabling.");
            Bukkit.getPluginManager().disablePlugin(this);
            return;
        }

        prices.load(new File(getDataFolder(), "prices.yml"), getLogger());
        menus = new ShopMenus(this);

        if (getCommand("shop") != null) {
            getCommand("shop").setExecutor(this);
        }
    }

    private boolean setupEconomy() {
        RegisteredServiceProvider<Economy> rsp = getServer().getServicesManager().getRegistration(Economy.class);
        if (rsp == null) {
            return false;
        }
        economy = rsp.getProvider();
        return economy != null;
    }

    PriceStore prices() {
        return prices;
    }

    double balance(Player p) {
        return economy.getBalance(p);
    }

    // ---------------------------------------------------------------- /shop

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (args.length > 0 && args[0].equalsIgnoreCase("reload")) {
            if (!sender.hasPermission("shopgui.reload")) {
                sender.sendMessage(Component.text("You don't have permission to do that.", NamedTextColor.RED));
                return true;
            }
            reloadConfig();
            prices.load(new File(getDataFolder(), "prices.yml"), getLogger());
            sender.sendMessage(Component.text("ShopGUI reloaded.", NamedTextColor.GREEN));
            return true;
        }

        if (!(sender instanceof Player player)) {
            sender.sendMessage("Only players can use /shop.");
            return true;
        }
        menus.openMain(player);
        return true;
    }

    // ---------------------------------------------------------------- buying

    /** Called (on the main thread) when the player presses "Buy" with whatever they typed. */
    void purchase(Player p, Category cat, ShopItem item, String typed) {
        int max = Math.max(1, getConfig().getInt("max-amount", 2304));

        int amount;
        try {
            long parsed = Long.parseLong(typed == null ? "" : typed.trim());
            if (parsed < 1) {
                menus.openBuy(p, cat, item, error("Amount must be at least 1."));
                return;
            }
            if (parsed > max) {
                menus.openBuy(p, cat, item, error("You can buy at most " + max + " at a time."));
                return;
            }
            amount = (int) parsed;
        } catch (NumberFormatException e) {
            menus.openBuy(p, cat, item, error("Enter a whole number."));
            return;
        }

        double total = item.price() * amount;
        if (!economy.has(p, total)) {
            menus.openBuy(p, cat, item, error("You need $" + ShopMenus.fmt(total) + " but only have $"
                + ShopMenus.fmt(economy.getBalance(p)) + "."));
            return;
        }

        EconomyResponse paid = economy.withdrawPlayer(p, total);
        if (!paid.transactionSuccess()) {
            menus.openBuy(p, cat, item, error("Payment failed: " + paid.errorMessage));
            return;
        }

        boolean delivered = item.isShard() ? giveShards(p, item, amount) : giveItems(p, item, amount);
        if (!delivered) {
            economy.depositPlayer(p, total); // refund
            menus.openBuy(p, cat, item, error("Could not deliver that order, you were not charged."));
            return;
        }

        String summary = "Bought " + amount + "x " + item.name() + " for $" + ShopMenus.fmt(total) + ".";
        p.sendMessage(Component.text(summary, NamedTextColor.GREEN));
        menus.openBuy(p, cat, item, Component.text(summary, NamedTextColor.GREEN));
    }

    private boolean giveItems(Player p, ShopItem item, int amount) {
        int left = amount;
        boolean dropped = false;
        while (left > 0) {
            int n = Math.min(left, item.material().getMaxStackSize());
            Map<Integer, ItemStack> overflow = p.getInventory().addItem(new ItemStack(item.material(), n));
            for (ItemStack extra : overflow.values()) {
                p.getWorld().dropItemNaturally(p.getLocation(), extra);
                dropped = true;
            }
            left -= n;
        }
        if (dropped) {
            p.sendMessage(Component.text("Your inventory was full, the rest was dropped at your feet.",
                NamedTextColor.YELLOW));
        }
        return true;
    }

    private boolean giveShards(Player p, ShopItem item, int amount) {
        String template = getConfig().getString("shard-command", "givespawner {player} {mob} {amount}");
        String cmd = template
            .replace("{player}", p.getName())
            .replace("{mob}", item.id())
            .replace("{amount}", Integer.toString(amount));
        if (cmd.startsWith("/")) {
            cmd = cmd.substring(1);
        }
        return Bukkit.dispatchCommand(Bukkit.getConsoleSender(), cmd);
    }

    private static Component error(String text) {
        return Component.text(text, NamedTextColor.RED);
    }
}
