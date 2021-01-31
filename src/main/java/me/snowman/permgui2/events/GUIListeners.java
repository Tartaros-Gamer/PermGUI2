package me.snowman.permgui2.events;

import me.snowman.permgui2.managers.ItemManager;
import me.snowman.permgui2.managers.MenuManager;
import me.snowman.permgui2.managers.PermsManager;
import me.snowman.permgui2.managers.UserManager;
import me.snowman.permgui2.objects.Menu;
import me.snowman.permgui2.objects.MenuItem;
import me.snowman.permgui2.objects.User;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.ItemStack;

public class GUIListeners implements Listener {
    private final MenuManager menuManager;
    private final ItemManager itemManager;
    private final PermsManager permsManager;
    private final UserManager userManager;

    public GUIListeners(MenuManager menuManager, ItemManager itemManager, PermsManager permsManager, UserManager userManager) {
        this.menuManager = menuManager;
        this.permsManager = permsManager;
        this.itemManager = itemManager;
        this.userManager = userManager;
    }

    @EventHandler
    public void onClick(InventoryClickEvent event) {
        Player player = (Player) event.getWhoClicked();
        User user = userManager.getUser(player);
        if (!menuManager.hasMenu(user)) return;
        if (event.getCurrentItem() == null || event.getCurrentItem().getType().equals(Material.AIR)) return;
        Menu menu = menuManager.getMenu(user);
        ItemStack currentItem = event.getCurrentItem();
        MenuItem item = itemManager.getItem(menu, currentItem, currentItem.getItemMeta().getDisplayName());
        System.out.println(item);
        String itemName = item.getName();

        if (menu.getListType() != null) {
            switch (menu.getListType()) {
                case "players" -> user.setTarget(itemName);
                case "plugins" -> user.setPlugin(itemName);
            }
        }
        for (String actions : item.getActions()) {
            String action = actions.substring(0, actions.indexOf(" "));
            String arguments = actions.substring(actions.indexOf(" ") + 1);
            switch (action) {
                case "[OPEN]":
                    Menu openedMenu = menuManager.getMenu(arguments);
                    menuManager.open(user, openedMenu);
                    break;
                case "[CHANGEPERM]":
                    String targetString = arguments.replace("%target%", user.getTarget());
                    if (Bukkit.getServer().getPlayer(targetString) != null) {
                        Player target = Bukkit.getServer().getPlayer(targetString);
                        String perm = ChatColor.stripColor(itemName);
                        if (target.hasPermission(perm)) permsManager.getPerms().playerRemove(null, target, perm);
                        else permsManager.getPerms().playerAdd(null, target, perm);
                        menuManager.open(user, menu);
                        break;
                    }
            }
        }
        event.setCancelled(true);
    }
}
