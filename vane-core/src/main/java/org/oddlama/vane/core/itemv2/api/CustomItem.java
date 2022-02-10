package org.oddlama.vane.core.itemv2.api;

import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.TranslatableComponent;

/**
 * This is the CustomItem specification that all custom items must implement to be registered with the vane custom-item API.
 */
public interface CustomItem {
	/**
	 * The unique identifier for this custom item. This is the only value that can never be changed after initial item registration.
	 * If you want to deprecate an item, you can force your subclass to always return false in {@link #enabled()}. This will cause existing
	 * items to behave as their base items. To completely remove a custom item from encountered inventories, you can
	 * queue it for removal using {@link CustomItemRegistry#removePermanently(NamespacedKey)}.
	 * The key is the primary identifier of an item and must never change after initial registration.
	 * All other properties can be safely changed between restarts and even on configuration reloads.
	 */
	public NamespacedKey key();

	/**
	 * Return true to indicate that this item is enabled. If this function returns false,
	 * the custom item will be regarded as nonexistent. This means that in this case,
	 * no item-stack updates are executed, and no related events are processed internally.
	 */
	public boolean enabled();

	/**
	 * Returns a version number of this item. Increasing this value will cause {@link #convertExistingStack(ItemStack)}
	 * to be called on any encountered itemstack of this custom item. Useful to force an update of existing
	 * items when you for example change the {@link #updateItemStack(ItemStack)} function to add custom
	 * properties to your custom item.
	 */
	public int version();

	/**
	 * Returns the base material that the custom item is made of.
	 * If this is changed, any encountered item will automatically be updated.
	 *
	 * To ensure that a breakage of the plugin never creates value for players,
	 * use a material with less net-worth than what the custom item provides.
	 * For crafting ingredients, we recommend using an item that has no other use than in crafting.
	 * Generally it is a good idea to pick materials that have an item with similar properties.
	 *
	 * No attempts will be made to remove the vanilla behavior of the base items.
	 * If you require for example a hoe that doesn't till blocks or a carrot
	 * on a stick that doesn't attract pigs, you generally will have to listen
	 * for the corresponding event yourself and cancel it. For some commonly
	 * used base items, vane provides helpers that disable the vanilla functionality
	 * for you (see TODO).
	 */
	// TODO: intercept brewing stand events. hoe events, shovel tilling etc. Is this too much? We will certainly miss certain things.
	// TODO: maybe recommend just using the most mundane, fitting item (no durability -> paper, durability -> warped fungus on a stick), now that we have custom durability, this might be the best way.
	public Material baseMaterial();

	/**
	 * Returns the custom model data used as a selector in a resource pack.
	 * If this is changed, any encountered item will automatically be updated.
	 * We recommend reserving a set of ids using {@link CustomModelDataRegistry#reserveRange(NamespacedKey, int, int)} when your plugin starts.
	 * This allows you freely use and re-use the registerd ids without having to worry about clashes with other plugins.
	 */
	public int customModelData();


	/**
	 * Returns the display name for newly created items of this type.
	 * This will NOT be updated on existing items. If you want that behavior, you
	 * can easily implement it by overriding {@link #updateItemStack(ItemStack)}.
	 */
	public @Nullable Component displayName();

	/**
	 * A custom translation key that will be used to display durability on the item.
	 * Return null to disable durability lore. Arguments to the translatable component
	 * that will be supplied are the current durability (%1$s) and max durability (%2$s).
	 */
	public @Nullable TranslatableComponent durabilityLore();

	/**
	 * The items effective maximum durability. If this returns 0, no changes will be made to the base
	 * item's durability mechanic. If this is set to a value > 0, it requires a base item with durability.
	 * The durability bar of the base item then acts solely as an indicative value of a separately stored durability.
	 * Changes to the item's durability by classical means are automatically reflected in this property.
	 *
	 * If this is value changed while item stacks of this custom item already exist with a different maximum durability,
	 * the affected items will be updated and keep their current durability, but clamped to the new maximum.
	 */
	public int durability();

	// TODO make onItemMend proportional by default. can be disabled by items by catching the event and cancelling it.
	// TODO catch PlayerItemBreakEvent and PlayerItemDamageEvent to modify custom durability.
	// TODO check if handle.hurtAndBreak calls these.
	// TODO make a simple json format to add custom items without code.

	/**
	 * This function will be called when the resource pack is generated, and allows you
	 * to add the item's texture, translation strings or any other client side resources to a pack
	 * that can/will be distributed to players.
	 */
	public void addResources(/*final ResourcePackGenerator rp*/);

	/**
	 * This function will be called when a custom item of this type is newly created,
	 * or when an existing stack needs to be updated. This can include cases where
	 * no base information actually changed, but an item still is considered to
	 * be updated, for example anvil results.
	 */
	default public ItemStack updateItemStack(@NotNull final ItemStack itemStack) {
		return itemStack;
	}
}
