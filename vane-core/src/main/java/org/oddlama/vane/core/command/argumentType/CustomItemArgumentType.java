package org.oddlama.vane.core.command.argumentType;

import java.util.concurrent.CompletableFuture;
import java.util.stream.Collectors;

import org.bukkit.NamespacedKey;
import org.jetbrains.annotations.NotNull;
import org.oddlama.vane.core.Core;
import org.oddlama.vane.core.item.api.CustomItem;

import com.mojang.brigadier.arguments.ArgumentType;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.brigadier.suggestion.Suggestions;
import com.mojang.brigadier.suggestion.SuggestionsBuilder;

import io.papermc.paper.command.brigadier.MessageComponentSerializer;
import io.papermc.paper.command.brigadier.argument.ArgumentTypes;
import io.papermc.paper.command.brigadier.argument.CustomArgumentType;

public class CustomItemArgumentType implements CustomArgumentType.Converted<CustomItem, NamespacedKey> {

    Core module;

    private CustomItemArgumentType(Core module) {
        this.module = module;
    }

    public static CustomItemArgumentType customItem(Core module) {
        return new CustomItemArgumentType(module);
    }

    @Override
    public @NotNull ArgumentType<NamespacedKey> getNativeType() {
        return ArgumentTypes.namespacedKey();
    }

    @Override
    public @NotNull CustomItem convert(@NotNull NamespacedKey nativeType) throws CommandSyntaxException {
        return this.module.item_registry().all().stream().filter(item -> item.key().equals(nativeType)).findFirst()
                .orElseThrow();
    }

    @Override
    public <Context> @NotNull CompletableFuture<Suggestions> listSuggestions(@NotNull CommandContext<Context> context,
            @NotNull SuggestionsBuilder builder) {
        this.module.item_registry().all().stream().collect(Collectors.toMap(item -> item.key().toString(), CustomItem::displayName))
            .forEach((key, name) -> builder.suggest(key, MessageComponentSerializer.message().serialize(name)));
        return builder.buildFuture();

    }

}
