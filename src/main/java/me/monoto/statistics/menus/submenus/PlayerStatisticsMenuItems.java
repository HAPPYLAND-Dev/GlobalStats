package me.monoto.statistics.menus.submenus;

import dev.triumphteam.gui.builder.item.ItemBuilder;
import dev.triumphteam.gui.guis.Gui;
import dev.triumphteam.gui.guis.GuiItem;
import dev.triumphteam.gui.guis.PaginatedGui;
import me.monoto.statistics.Statistics;
import me.monoto.statistics.menus.GlobalMenu;
import me.monoto.statistics.menus.utils.Pagination;
import me.monoto.statistics.utils.Formatters;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.TextComponent;
import net.kyori.adventure.text.TranslatableComponent;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.format.TextDecoration;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.OfflinePlayer;
import org.bukkit.Statistic;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemFlag;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.inventory.meta.PotionMeta;
import org.bukkit.persistence.PersistentDataType;
import org.bukkit.potion.PotionData;
import org.bukkit.potion.PotionType;
import org.checkerframework.checker.units.qual.C;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;

public class PlayerStatisticsMenuItems {

    public static void getItemPreview (String type, Player player, OfflinePlayer target) {

        String title = "无";
        switch (type) {
            case "fishing" -> title = "钓鱼";
            case "killing" -> title = "击杀";
            case "travelling" -> title = "游走方块";
            case "mining", "placing" -> title = "放置";
        }

        PaginatedGui gui = Gui.paginated().title(Formatters.miniMulti(Formatters.lang().getString("gui.main.title-player", "<black><player> <type> 统计"), List.of("player", "type"), List.of(
                Component.text(Formatters.getPossessionString(Objects.requireNonNull(target.getName()))),
                Component.text(title)
        ))).rows(4).pageSize(27).create();

        Pagination.getPaginatedUtil(gui, target, "player", type);

        gui.setItem(31, ItemBuilder.skull().owner(target).name(Formatters.mini(Formatters.lang().getString("gui.main.player_head.title", "<player>"), "player", Component.text(Formatters.getPossessionString(Objects.requireNonNull(target.getName())))).decoration(TextDecoration.ITALIC, false))
                .asGuiItem(event -> GlobalMenu.initialise((Player) event.getWhoClicked(), target)));

        switch (type) {
            case "fishing" -> getFish(gui, target); // Passes an online players stats if there are any
            case "killing" -> getKills(gui, target);
            case "travelling" -> getMovements(gui, target);
            case "mining", "placing" -> getBlocks(gui, type, target);
        }

        gui.setDefaultClickAction(event -> event.setCancelled(true));

        gui.open(player);
    }

    private static void getFish(PaginatedGui gui, OfflinePlayer target) { // PDC Currently doesn't allow for OfflinePlayers
        if (target != null && target.getPlayer() != null) {
            JSONParser parser = new JSONParser();

            try {
                JSONObject json = (JSONObject) parser.parse(target.getPlayer().getPersistentDataContainer().get(new NamespacedKey(Statistics.getInstance(), "FISHING_STATS"), PersistentDataType.STRING));

                for (Object key : json.keySet()) {
                    ItemStack itemStack = new ItemStack(Material.valueOf(key.toString()), 1);
                    
                    if (Objects.equals(key.toString(), "POTION")) {
                        itemStack = new ItemStack(Material.POTION, 1);
                        ItemMeta itemMeta = itemStack.getItemMeta();
                        PotionMeta potionMeta = (PotionMeta) itemMeta;
                        PotionData potionData = new PotionData(PotionType.WATER);
                        
                        potionMeta.setBasePotionData(potionData);
                        itemStack.setItemMeta(itemMeta);
                    }

                    GuiItem item = ItemBuilder.from(itemStack).lore(Formatters.mini(Formatters.lang().getString("gui.main.fishing.lore", "<white>总计: <amount>"), "amount", Component.text(((Long) json.get(key)).intValue())).decoration(TextDecoration.ITALIC, false)).asGuiItem();
                    gui.addItem(item);
                }
            } catch (Exception exception) {
                exception.printStackTrace();
            }
        }
    }


    private static void getKills(PaginatedGui gui, OfflinePlayer target) {
        for (EntityType type : EntityType.values()) {
            try {
                if (target.getStatistic(Statistic.KILL_ENTITY, type) > 0) {
                    int statAmount = target.getStatistic(Statistic.KILL_ENTITY, type);

                    Material material;

                    try {
                        material = Material.valueOf(type + "_SPAWN_EGG");
                    } catch (IllegalArgumentException exception) {
                        material = switch (type) {
                            case IRON_GOLEM -> Material.IRON_INGOT;
                            case SNOWMAN -> Material.SNOWBALL;
                            case WITHER -> Material.WITHER_SKELETON_SKULL;
                            default -> null;
                        };
                    }

                    if (material != null) {
                        GuiItem item = ItemBuilder.from(material).name(Component.translatable(type).decoration(TextDecoration.ITALIC, false))
                                .lore(Formatters.mini(Formatters.lang().getString("gui.main.killing.lore", "<white>总计: <amount>"), "amount", Component.text(statAmount)).decoration(TextDecoration.ITALIC, false)).asGuiItem();
                        gui.addItem(item);
                    }
                }
            } catch (IllegalArgumentException ignored) {} // Catch unknown EntityTypes for example, Player, etc.
        }
    }

    private static void getMovements(PaginatedGui gui, OfflinePlayer target) {
        ArrayList<String> itemNames = new ArrayList<>(Arrays.asList("步行距离", "短跑距离", "游泳距离", "水上行走距离", "水下行走距离", "攀登距离", "蹲下距离", "摔倒距离", "鞘翅飞行距离", "乘船距离", "乘马距离", "乘矿车距离", "乘猪距离", "乘炽足兽距离", "跳跃距离"));
        ArrayList<Statistic> statistics = new ArrayList<>(Arrays.asList(Statistic.WALK_ONE_CM, Statistic.SPRINT_ONE_CM, Statistic.SWIM_ONE_CM, Statistic.WALK_ON_WATER_ONE_CM, Statistic.WALK_UNDER_WATER_ONE_CM, Statistic.CLIMB_ONE_CM, Statistic.CROUCH_ONE_CM, Statistic.FALL_ONE_CM, Statistic.AVIATE_ONE_CM, Statistic.BOAT_ONE_CM, Statistic.HORSE_ONE_CM, Statistic.MINECART_ONE_CM, Statistic.PIG_ONE_CM, Statistic.STRIDER_ONE_CM, Statistic.JUMP));
        ArrayList<Material> materials = new ArrayList<>(Arrays.asList(Material.LEATHER_BOOTS, Material.GOLDEN_BOOTS, Material.WATER_BUCKET, Material.ICE, Material.DIAMOND_HELMET, Material.LADDER, Material.LEATHER_BOOTS, Material.LINGERING_POTION, Material.ELYTRA, Material.OAK_BOAT, Material.DIAMOND_HORSE_ARMOR, Material.MINECART, Material.PIG_SPAWN_EGG, Material.STRIDER_SPAWN_EGG, Material.IRON_BOOTS));

        for (int index = 0; index < itemNames.size(); index++) {
            int statAmount = target.getStatistic(Statistic.JUMP);
            if (statistics.get(index) != Statistic.JUMP) {
                statAmount = (int) Math.floor(target.getStatistic(statistics.get(index))) / 100;
            }

            GuiItem item = ItemBuilder.from(materials.get(index)).lore(Formatters.mini(Formatters.lang().getString("gui.main.killing.lore", "<white>总计: <amount>"), "amount", Component.text(statAmount)).decoration(TextDecoration.ITALIC, false)).flags(ItemFlag.HIDE_POTION_EFFECTS)
                    .name(Component.text(itemNames.get(index)).color(NamedTextColor.WHITE).decoration(TextDecoration.ITALIC, false))
                    .asGuiItem();
            gui.addItem(item);
        }
    }

    private static void getBlocks(PaginatedGui gui, String type, OfflinePlayer target) {
        Statistic statistic = Objects.equals(type, "mining") ? Statistic.MINE_BLOCK : Statistic.USE_ITEM;
        String typeLore = Objects.equals(type, "mining") ? "gui.main.mining.lore" : "gui.main.placing.lore";
        
        for (Material material : Material.values()) {
            if (target.getStatistic(statistic, material) > 0 && material.isBlock()) {
                int statAmount = target.getStatistic(statistic, material);

                GuiItem item = ItemBuilder.from(material).lore(Formatters.mini(Formatters.lang().getString(typeLore, "<white>总计: <amount>"), "amount", Component.text(statAmount)).decoration(TextDecoration.ITALIC, false)).asGuiItem();
                gui.addItem(item);
            }
        }
    }
}
