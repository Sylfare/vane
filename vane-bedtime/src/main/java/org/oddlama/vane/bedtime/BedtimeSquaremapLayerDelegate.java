package org.oddlama.vane.bedtime;

import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

import javax.imageio.ImageIO;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.OfflinePlayer;
import org.bukkit.World;
import org.bukkit.plugin.Plugin;
import org.checkerframework.checker.nullness.qual.NonNull;
import org.jetbrains.annotations.Nullable;

import xyz.jpenilla.squaremap.api.BukkitAdapter;
import xyz.jpenilla.squaremap.api.Key;
import xyz.jpenilla.squaremap.api.LayerProvider;
import xyz.jpenilla.squaremap.api.MapWorld;
import xyz.jpenilla.squaremap.api.Point;
import xyz.jpenilla.squaremap.api.Squaremap;
import xyz.jpenilla.squaremap.api.SquaremapProvider;
import xyz.jpenilla.squaremap.api.WorldIdentifier;
import xyz.jpenilla.squaremap.api.marker.Icon;
import xyz.jpenilla.squaremap.api.marker.Marker;
import xyz.jpenilla.squaremap.api.marker.MarkerOptions;

public class BedtimeSquaremapLayerDelegate {

    public final Key ICON_KEY = Key.of("vane_beds");

    private final BedtimeSquaremapLayer parent;
    private Squaremap squaremap_api;
    private Map<WorldIdentifier, PlayerLayerProvider> providers = new HashMap<WorldIdentifier, PlayerLayerProvider>(){};

    public BedtimeSquaremapLayerDelegate(BedtimeSquaremapLayer parent) {
        this.parent = parent;
    }

    public Bedtime get_module() {
		return parent.get_module();
	}

    public void on_enable(@Nullable Plugin plugin) {
        get_module().log.info("Enabling dynmap integration");  
        squaremap_api = SquaremapProvider.get();

        this.register_icon();

        // create a provider for each world
        Bukkit.getWorlds().forEach(this::get_provider);
        this.update_all_markers();
    }

    public void register_icon(){
        String filename = "icons" + File.separator + "bed.png";
        File file = new File(get_module().getDataFolder(), filename);
        if(!file.exists()) {
            get_module().saveResource(filename, false);
        }

        try {
            BufferedImage image = ImageIO.read(file);
            squaremap_api.iconRegistry().register(ICON_KEY, image);
        } catch (IOException e) {
            get_module().log.warning("Failed to register bed icon:" + e);
        }
    }

    public void on_disable() {
        get_module().log.info("Disabling squaremap integration");
        squaremap_api = null;
    }

    public void update_marker(final OfflinePlayer player) {
        World world = player.getRespawnLocation().getWorld();
        PlayerLayerProvider provider = get_provider(world);

        if(provider == null) {
            return;
        }

        provider.add(player);
    }

    public void update_all_markers() {
        for(final var player : get_module().get_offline_players_with_valid_name()) {
            update_marker(player);
        }
    }

	public void remove_marker(OfflinePlayer player) {
		PlayerLayerProvider provider = get_provider(player.getRespawnLocation().getWorld());
		provider.remove(id_for(player));
	}

    public void remove_marker(UUID portal_id) {
        var markerData = providers.entrySet().stream().filter(entry -> entry.getValue().data.containsKey(id_for(portal_id))).findFirst();
        if(markerData.isPresent()) {
            markerData.get().getValue().remove(id_for(portal_id));
        }
    }

    public PlayerLayerProvider get_provider(World world) {
        var world_identifier = BukkitAdapter.worldIdentifier(world);
        PlayerLayerProvider provider = this.providers.get(world_identifier);
        if(provider != null) {
            return provider;
        }

        MapWorld mapWorld = squaremap_api.getWorldIfEnabled(world_identifier).orElse(null);
        if (mapWorld == null) {
            return null;
        }
        
        // no provider was found, create one
        provider = new PlayerLayerProvider();
        Key key = Key.of("bedtime");
        mapWorld.layerRegistry().register(key, provider);
        this.providers.put(mapWorld.identifier(), provider);
        return provider;
        
    }

	private String id_for(final UUID portal_id) {
		return portal_id.toString();
	}

	private String id_for(final OfflinePlayer player) {
		return id_for(player.getUniqueId());
	}

	private Point to_point(final OfflinePlayer player) {
		final Location point = player.getRespawnLocation();
		return Point.of(point.x(), point.z());
	}

    class PlayerLayerProvider implements LayerProvider {
        private final Map<String, Data> data = new ConcurrentHashMap<String, Data>();

        @Override
        public @NonNull String getLabel() {
            return parent.lang_layer_label.str();
        }

        @Override
        public int layerPriority() {
            return parent.config_layer_priority;
        }

        @Override
        public @NonNull Collection<Marker> getMarkers() {
            return this.data.values().stream().map(Data::marker).collect(Collectors.toSet());
        }

        public void add(OfflinePlayer player) {
            if(!player.hasPlayedBefore()) {
                return;
            }

            Icon icon = Marker.icon(to_point(player), ICON_KEY, parent.config_icon_size);
            icon.markerOptions(
                MarkerOptions.builder().hoverTooltip(player.getName())
            );
            this.data.put(id_for(player), new Data(icon, ICON_KEY, player.getName()));
        }

        public void remove(String portal_id) {
            if(portal_id == null) {
                return;
            }
            this.data.remove(portal_id);
        }
        
    }

    record Data(Marker marker, Key key, String name) {}
}
