package org.oddlama.vane.portals;

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
import org.bukkit.World;
import org.bukkit.plugin.Plugin;
import org.checkerframework.checker.nullness.qual.NonNull;
import org.jetbrains.annotations.Nullable;
import org.oddlama.vane.portals.portal.Portal;

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

public class PortalSquaremapLayerDelegate {

    public final Key ICON_KEY = Key.of("vane_portal");

    private final PortalSquaremapLayer parent;
    private Squaremap squaremap_api;
    private Map<WorldIdentifier, PortalLayerProvider> providers = new HashMap<WorldIdentifier, PortalLayerProvider>(){};

    public PortalSquaremapLayerDelegate(PortalSquaremapLayer parent) {
        this.parent = parent;
    }

    public Portals get_module() {
		return parent.get_module();
	}

    public void on_enable(@Nullable Plugin plugin) {
        get_module().log.info("Enabling dynmap integration");  
        squaremap_api = SquaremapProvider.get();

        this.register_icon();

        // create a provider for each world
        Bukkit.getWorlds().forEach(this::get_provider);
        this.load_markers();
    }

    public void register_icon(){
        String filename = "icons" + File.separator + "portal.png";
        File file = new File(get_module().getDataFolder(), filename);
        if(!file.exists()) {
            get_module().saveResource(filename, false);
        }

        try {
            BufferedImage image = ImageIO.read(file);
            squaremap_api.iconRegistry().register(ICON_KEY, image);
        } catch (IOException e) {
            get_module().log.warning("Failed to register portal icon:" + e);
        }
    }

    public void load_markers(){
        get_module().all_available_portals().forEach(portal -> {
            PortalLayerProvider provider = get_provider(portal.spawn().getWorld());
            if (provider == null) {
                return;
            }

            update_marker(portal);     
        });
    }    

    public void on_disable() {
        get_module().log.info("Disabling squaremap integration");
        squaremap_api = null;
    }

    public void update_marker(final Portal portal) {
        World world = portal.spawn().getWorld();
        PortalLayerProvider provider = get_provider(world);

        if(provider == null) {
            return;
        }
        if(portal.visibility() == Portal.Visibility.PRIVATE) {
            remove_marker(portal);
            return;
        }

        provider.add(portal, portal.name());
    }

    public void update_all_markers() {
        for(final var portal : get_module().all_available_portals()) {
            update_marker(portal);
        }
    }

	public void remove_marker(Portal portal) {
		Location portal_location = portal.spawn();
		PortalLayerProvider provider = get_provider(portal_location.getWorld());
		provider.remove(id_for(portal));
	}

    public void remove_marker(UUID portal_id) {
        var markerData = providers.entrySet().stream().filter(entry -> entry.getValue().data.containsKey(id_for(portal_id))).findFirst();
        if(markerData.isPresent()) {
            markerData.get().getValue().remove(id_for(portal_id));
        }
    }

    public PortalLayerProvider get_provider(World world) {
        var world_identifier = BukkitAdapter.worldIdentifier(world);
        PortalLayerProvider provider = this.providers.get(world_identifier);
        if(provider != null) {
            return provider;
        }

        MapWorld mapWorld = squaremap_api.getWorldIfEnabled(world_identifier).orElse(null);
        if (mapWorld == null) {
            return null;
        }
        
        // no provider was found, create one
        provider = new PortalLayerProvider();
        Key key = Key.of("portals");
        mapWorld.layerRegistry().register(key, provider);
        this.providers.put(mapWorld.identifier(), provider);
        return provider;
        
    }

	private String id_for(final UUID portal_id) {
		return portal_id.toString();
	}

	private String id_for(final Portal portal) {
		return id_for(portal.id());
	}

	private Point to_point(final Portal portal) {
		final Location point = portal.spawn();
		return Point.of(point.x(), point.z());
	}

    class PortalLayerProvider implements LayerProvider {
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

        public void add(Portal portal, String name) {
            name = name == null ? "null" : name;
            Icon icon = Marker.icon(to_point(portal), ICON_KEY, parent.config_icon_size);
            icon.markerOptions(
                MarkerOptions.builder().hoverTooltip(name)
            );
            this.data.put(id_for(portal), new Data(icon, ICON_KEY, name));
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
