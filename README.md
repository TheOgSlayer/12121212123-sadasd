# ShopGUI

A `/shop` plugin for Paper 1.21.6+ that uses the **Dialog** screens (no chest GUIs).

`/shop` opens a menu with 7 shops: **End, Nether, Gear, Food, Overworld, Minerals, Shards**.
Click a shop, click an item, type how many you want, press **Buy**.

Requires **Vault** and an economy plugin (e.g. EssentialsX).

## Install
1. Download `ShopGUI.jar` (see "Getting the jar" below) and put it in `plugins/`.
2. Start the server once. `plugins/ShopGUI/prices.yml` and `config.yml` are created.
3. Edit the prices, then run `/shop reload`.

## Commands & permissions
| Command | Permission | Default |
|---|---|---|
| `/shop` | `shopgui.use` | everyone |
| `/shop reload` | `shopgui.reload` | op |

## prices.yml
```yaml
gear:
  TOTEM_OF_UNDYING: 1500.0   # price per item
  ARROW: 2.0
shards:
  cow: 5000.0                # price per spawner; "cow" is sent to the shard command
```
* Item names are [Bukkit material names](https://hub.spigotmc.org/javadocs/spigot/org/bukkit/Material.html).
* Price `0` (or deleting the line) removes the item. You can add more items to any shop.
* Each shop has its own prices, so e.g. `ENDER_PEARL` can cost different amounts in End and Gear.

## config.yml
```yaml
shard-command: "givespawner {player} {mob} {amount}"   # run from the console
max-amount: 2304                                        # max per purchase
```
Buying shards withdraws the money, then runs the shard command. If the command fails the player is refunded.

## Getting the jar (GitHub)
Every push is built automatically by GitHub Actions:
1. Open the repo's **Actions** tab, click the latest run.
2. Scroll to **Artifacts** and download **ShopGUI** (a zip containing `ShopGUI.jar`).

To build locally instead: `mvn package` (Java 21), jar is in `target/`.
