package teams.gui;

import org.bukkit.Material;
import org.bukkit.OfflinePlayer;
import org.bukkit.inventory.ItemFlag;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.inventory.meta.SkullMeta;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * A small helper to make menu items
 */
public class ItemBuilder {

	private final ItemStack item;
	private String name = null;
	private final List<String> lore = new ArrayList<>();
	private boolean glow = false;
	private OfflinePlayer head = null;

	public ItemBuilder(Material material) {
		item = new ItemStack(material);
	}

	public ItemBuilder name(String name) {
		this.name = name;
		return this;
	}

	public ItemBuilder amount(int amount) {
		item.setAmount(Math.max(1, Math.min(64, amount)));
		return this;
	}

	public ItemBuilder lore(String... lines) {
		lore.addAll(Arrays.asList(lines));
		return this;
	}

	public ItemBuilder lore(List<String> lines) {
		lore.addAll(lines);
		return this;
	}

	public ItemBuilder glow(boolean glow) {
		this.glow = glow;
		return this;
	}

	public ItemBuilder head(OfflinePlayer player) {
		this.head = player;
		return this;
	}

	/**
	 * Adds a line like "Left click: Open"
	 */
	public ItemBuilder click(String click, String action) {
		lore.add(Theme.soft() + click + Theme.white() + ": " + Theme.light() + action);
		return this;
	}

	public ItemStack build() {
		ItemMeta meta = item.getItemMeta();
		if (meta == null) {
			return item;
		}
		if (name != null) {
			meta.setDisplayName(name);
		}
		if (!lore.isEmpty()) {
			meta.setLore(lore);
		}
		if (glow) {
			meta.setEnchantmentGlintOverride(true);
		}
		if (head != null && meta instanceof SkullMeta) {
			((SkullMeta) meta).setOwningPlayer(head);
		}
		meta.addItemFlags(ItemFlag.values());
		item.setItemMeta(meta);
		return item;
	}
}
