package com.shopgui;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

import io.papermc.paper.dialog.Dialog;
import io.papermc.paper.registry.data.dialog.ActionButton;
import io.papermc.paper.registry.data.dialog.DialogBase;
import io.papermc.paper.registry.data.dialog.action.DialogAction;
import io.papermc.paper.registry.data.dialog.body.DialogBody;
import io.papermc.paper.registry.data.dialog.input.DialogInput;
import io.papermc.paper.registry.data.dialog.type.DialogType;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.event.ClickCallback;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.format.TextDecoration;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;

/** Builds and shows the three dialog screens: main menu, category, buy. */
public final class ShopMenus {

    private static final int BUTTON_WIDTH = 220;
    static final String AMOUNT_KEY = "amount";

    private final ShopGUI plugin;

    public ShopMenus(ShopGUI plugin) {
        this.plugin = plugin;
    }

    // ---------------------------------------------------------------- screens

    /** /shop: the seven categories. */
    public void openMain(Player p) {
        List<DialogBody> body = new ArrayList<>();
        body.add(DialogBody.plainMessage(Component.text("Choose a shop", NamedTextColor.GRAY)));

        List<ActionButton> buttons = new ArrayList<>();
        for (Category c : Category.values()) {
            buttons.add(button(Component.text(c.title(), c.color()), () -> openCategory(p, c)));
        }
        ActionButton close = ActionButton.builder(Component.text("Close")).width(BUTTON_WIDTH).build();

        show(p, title("SHOP", NamedTextColor.GOLD), body, List.of(), buttons, close, 2);
    }

    /** The item list of one shop. */
    public void openCategory(Player p, Category c) {
        List<ShopItem> items = plugin.prices().items(c);
        if (items.isEmpty()) {
            p.sendMessage(Component.text("The " + c.title() + " has nothing for sale right now.", NamedTextColor.RED));
            openMain(p);
            return;
        }

        List<DialogBody> body = new ArrayList<>();
        body.add(DialogBody.plainMessage(Component.text("Click an item to choose how many to buy", NamedTextColor.GRAY)));
        body.add(DialogBody.plainMessage(
            Component.text("Balance: ", NamedTextColor.WHITE).append(money(plugin.balance(p)))));

        List<ActionButton> buttons = new ArrayList<>();
        for (ShopItem item : items) {
            Component label = Component.text(item.name(), NamedTextColor.WHITE)
                .append(Component.text("  "))
                .append(money(item.price()));
            buttons.add(button(label, () -> openBuy(p, c, item, null)));
        }
        ActionButton back = button(Component.text("Back"), () -> openMain(p));

        show(p, title(c.title().toUpperCase(Locale.ROOT), c.color()), body, List.of(), buttons, back, 2);
    }

    /** Amount prompt for one item. {@code notice} is an optional status line shown on top. */
    public void openBuy(Player p, Category c, ShopItem item, Component notice) {
        List<DialogBody> body = new ArrayList<>();
        if (notice != null) {
            body.add(DialogBody.plainMessage(notice));
        }
        body.add(DialogBody.plainMessage(Component.text(item.name(), NamedTextColor.GOLD, TextDecoration.BOLD)));
        body.add(DialogBody.plainMessage(
            Component.text("Price each: ", NamedTextColor.WHITE).append(money(item.price()))));
        body.add(DialogBody.plainMessage(
            Component.text("Balance: ", NamedTextColor.WHITE).append(money(plugin.balance(p)))));

        List<DialogInput> inputs = new ArrayList<>();
        inputs.add(DialogInput.text(AMOUNT_KEY, Component.text("Enter amount to buy"))
            .width(200)
            .initial("1")
            .maxLength(6)
            .build());

        DialogAction buyAction = DialogAction.customClick(
            (view, audience) -> {
                String typed = view.getText(AMOUNT_KEY);
                Bukkit.getScheduler().runTask(plugin, () -> plugin.purchase(p, c, item, typed));
            },
            ClickCallback.Options.builder().uses(ClickCallback.UNLIMITED_USES).build());

        List<ActionButton> buttons = new ArrayList<>();
        buttons.add(ActionButton.builder(Component.text("Buy", NamedTextColor.GREEN))
            .width(BUTTON_WIDTH)
            .action(buyAction)
            .build());
        ActionButton back = button(Component.text("Back"), () -> openCategory(p, c));

        show(p, title(item.name().toUpperCase(Locale.ROOT), c.color()), body, inputs, buttons, back, 1);
    }

    // ---------------------------------------------------------------- helpers

    private ActionButton button(Component label, Runnable onClick) {
        DialogAction action = DialogAction.customClick(
            (view, audience) -> Bukkit.getScheduler().runTask(plugin, onClick),
            ClickCallback.Options.builder().uses(ClickCallback.UNLIMITED_USES).build());
        return ActionButton.builder(label).width(BUTTON_WIDTH).action(action).build();
    }

    private void show(Player p, Component title, List<DialogBody> body, List<DialogInput> inputs,
                      List<ActionButton> actions, ActionButton exit, int columns) {
        Dialog dialog = Dialog.create(factory -> factory.empty()
            .base(DialogBase.builder(title)
                .body(body)
                .inputs(inputs)
                .canCloseWithEscape(true)
                .build())
            .type(DialogType.multiAction(actions, exit, columns)));
        p.showDialog(dialog);
    }

    private static Component title(String text, net.kyori.adventure.text.format.TextColor color) {
        return Component.text(text, color, TextDecoration.BOLD);
    }

    static Component money(double amount) {
        return Component.text("$", NamedTextColor.GREEN)
            .append(Component.text(fmt(amount), NamedTextColor.WHITE));
    }

    static String fmt(double amount) {
        return String.format(Locale.US, "%,.2f", amount);
    }
}
