package org.oddlama.vane.enchantments.enchantments.registry;

import java.util.List;

import org.bukkit.enchantments.Enchantment;
import org.oddlama.vane.enchantments.CustomEnchantmentRegistry;

import io.papermc.paper.registry.RegistryKey;
import io.papermc.paper.registry.TypedKey;
import io.papermc.paper.registry.data.EnchantmentRegistryEntry;
import io.papermc.paper.registry.event.RegistryFreezeEvent;
import io.papermc.paper.registry.keys.ItemTypeKeys;
import net.kyori.adventure.key.Key;

public class AngelRegistry extends CustomEnchantmentRegistry {

    public AngelRegistry(RegistryFreezeEvent<Enchantment, EnchantmentRegistryEntry.Builder> freezeEvent) {
        super("angel", List.of(ItemTypeKeys.ELYTRA), 5);
        this.exclusive_with(List.of(TypedKey.create(RegistryKey.ENCHANTMENT, Key.key(NAMESPACE, "wings"))))
        .register(freezeEvent);
    }
    
}
