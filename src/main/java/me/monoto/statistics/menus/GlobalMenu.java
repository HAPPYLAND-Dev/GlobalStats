package me.monoto.statistics.menus;

import dev.triumphteam.gui.builder.item.ItemBuilder;
import dev.triumphteam.gui.guis.Gui;
import me.monoto.statistics.Statistics;
import me.monoto.statistics.menus.submenus.PlayerStatisticsMenuItems;
import me.monoto.statistics.menus.utils.BackButton;
import me.monoto.statistics.stats.PlayerStatistics;
import me.monoto.statistics.stats.StatisticsManager;
import me.monoto.statistics.utils.Formatters;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.TextComponent;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.format.TextDecoration;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.OfflinePlayer;
import org.bukkit.block.Block;
import org.bukkit.block.data.BlockData;
import org.bukkit.block.data.Powerable;
import org.bukkit.entity.Player;
import org.bukkit.persistence.PersistentDataType;

import java.util.*;
import java.util.function.Consumer;

public class GlobalMenu {
    public static void initialise(Player player) {
        Gui gui = Gui.gui().rows(3).title(Formatters.mini(Formatters.lang().getString("gui.main.title_global", "<black>全部统计"))).create();
        populateGlobal(gui);

        gui.setDefaultClickAction(event -> event.setCancelled(true));
        gui.open(player);
    }

    public static void initialise(Player player, OfflinePlayer oPlayer) {
        Gui gui = Gui.gui().rows(3).title(Formatters.mini(Formatters.lang().getString("gui.main.title_global", "<black>全部统计"))).create();

        populatePlayer(gui, oPlayer);

        gui.setDefaultClickAction(event -> event.setCancelled(true));
        gui.open(player);
    }

    private static void populateGlobal(Gui gui) {
        gui.setItem(10, ItemBuilder.from(Material.CHEST).name(Formatters.mini(Formatters.lang().getString("gui.main.all.title", "<red>所有玩家")).decoration(TextDecoration.ITALIC, false)).lore(Formatters.mini(Formatters.lang().getString("gui.main.all.lore", "<white>点击我来预览所有玩家")).decoration(TextDecoration.ITALIC, false)).asGuiItem(event -> PlayerListMenu.initialise((Player) event.getWhoClicked())));
        populateMenu(gui, null);
    }

    private static void populatePlayer(Gui gui, OfflinePlayer target) {
        gui.setItem(10, ItemBuilder.skull().owner(target).name(Formatters.getPlayerSkullTitle(target).decoration(TextDecoration.ITALIC, false)).asGuiItem());
        gui.setItem(26, BackButton.getBackButton().asGuiItem(event -> PlayerListMenu.initialise((Player) event.getWhoClicked())));
        populateMenu(gui, target);
    }

    private static void populateMenu(Gui gui, OfflinePlayer target) {
        gui.setItem(12, ItemBuilder.from(Material.FISHING_ROD).name(Formatters.mini(Formatters.lang().getString("gui.main.category.fishing.title", "Fishing")).decoration(TextDecoration.ITALIC, false)).lore(target == null ? getLore("fishing") : getLore("fishing", target)).asGuiItem(event -> {
            if (target != null && target.getPlayer() != null) {
                if (target.getPlayer().getPersistentDataContainer().has(new NamespacedKey(Statistics.getInstance(), "FISHING_STATS"), PersistentDataType.STRING)) {
                    PlayerStatisticsMenuItems.getItemPreview("fishing", (Player) event.getWhoClicked(), target);
                }
            }
        }));
        gui.setItem(13, ItemBuilder.from(Material.DIAMOND_PICKAXE).name(Formatters.mini(Formatters.lang().getString("gui.main.category.mining.title", "Mining")).decoration(TextDecoration.ITALIC, false)).lore(target == null ? getLore("mining") : getLore("mining", target)).asGuiItem(event -> {
            if (target != null) PlayerStatisticsMenuItems.getItemPreview("mining", (Player) event.getWhoClicked(), target);
        }));
        gui.setItem(14, ItemBuilder.from(Material.DIAMOND_SWORD).name(Formatters.mini(Formatters.lang().getString("gui.main.category.killing.title", "Killing")).decoration(TextDecoration.ITALIC, false)).lore(target == null ? getLore("killing") : getLore("killing", target)).asGuiItem(event -> {
            if (target != null) PlayerStatisticsMenuItems.getItemPreview("killing", (Player) event.getWhoClicked(), target);
        }));
        gui.setItem(15, ItemBuilder.from(Material.LEATHER_BOOTS).name(Formatters.mini(Formatters.lang().getString("gui.main.category.travelling.title", "Traversed Blocks")).decoration(TextDecoration.ITALIC, false)).lore(target == null ? getLore("travelling") : getLore("travelling", target)).asGuiItem(event -> {
            if (target != null) PlayerStatisticsMenuItems.getItemPreview("travelling", (Player) event.getWhoClicked(), target);
        }));
        gui.setItem(16, ItemBuilder.from(Material.STONE).name(Formatters.mini(Formatters.lang().getString("gui.main.category.placing.title", "Placing")).decoration(TextDecoration.ITALIC, false)).lore(target == null ? getLore("placing") : getLore("placing", target)).asGuiItem(event -> {
            if (target != null) PlayerStatisticsMenuItems.getItemPreview("placing", (Player) event.getWhoClicked(), target);
        }));

        gui.getFiller().fill(ItemBuilder.from(Material.BLACK_STAINED_GLASS_PANE).name(Component.text(" ")).asGuiItem());
    }

    private static List<Component> getLore(String type) {
        List<Component> lore = new ArrayList<>();

        String globalStatistic = switch (type) {
            case "fishing" -> String.valueOf(StatisticsManager.getGlobalStatistics().getFishedFish());
            case "mining" -> String.valueOf(StatisticsManager.getGlobalStatistics().getMinedBlocks());
            case "killing" -> String.valueOf(StatisticsManager.getGlobalStatistics().getMobsKilled());
            case "travelling" -> Formatters.getDistanceFormatter(StatisticsManager.getGlobalStatistics().getTraversedBlocks());
            case "placing" -> String.valueOf(StatisticsManager.getGlobalStatistics().getPlacedBlocks());
            default -> "0";
        };

        lore.add(Formatters.mini(Formatters.lang().getString("gui.main.category." + type + ".lore", "<white>总计: <amount>"), "amount", Component.text(globalStatistic)).decoration(TextDecoration.ITALIC, false));

        StatisticsManager.getTopThreeStatistics().forEach((stat, playerHashmap) -> {
            if (Objects.equals(type, stat)) {
                int count = 0;

                for (HashMap.Entry<String, Integer> data : playerHashmap.entrySet()) {
                    if (data.getValue() != 0) {
                        lore.add(Formatters.miniMulti(
                                Formatters.lang().getString(
                                        "gui.main.category." + type + ".top_players." + (count+1), "<yellow>" + (count+1) + "<white>: <player> <amount>"
                                ),
                                List.of("player", "amount"),
                                List.of(Component.text(data.getKey()), Objects.equals(type, "travelling") ? Component.text(Formatters.getDistanceFormatter(data.getValue())) : Component.text(data.getValue())))
                        );
                    }
                    count++;
                }
            }
        });

        return lore;
    }

    private static List<Component> getLore(String type, OfflinePlayer target) {
        List<Component> lore = new ArrayList<>();

        PlayerStatistics stats = target.getPlayer() != null ? (PlayerStatistics) StatisticsManager.getPlayerStatistics().get(target.getUniqueId())
                : (PlayerStatistics) StatisticsManager.getOfflinePlayerStatistics().get(target.getUniqueId());

        String playerStats = switch (type) {
            case "fishing" -> String.valueOf(stats.getFishedFish());
            case "mining" -> String.valueOf(stats.getMinedBlocks());
            case "killing" -> String.valueOf(stats.getMobsKilled());
            case "travelling" -> Formatters.getDistanceFormatter(stats.getTraversedBlocks());
            case "placing" -> String.valueOf(stats.getPlacedBlocks());
            default -> "0";
        };

        lore.add(Formatters.mini(Formatters.lang().getString("gui.main.category." + type + ".lore", "<white>总计: <amount>"), "amount", Component.text(playerStats)).decoration(TextDecoration.ITALIC, false));

        if (Objects.equals(type, "fishing") && target.getPlayer() == null) {
            lore.add(Formatters.mini(Formatters.lang().getString("gui.util.offline_player", "<red>This category doesn't support offline viewing")));
        }

        return lore;
    }

}
