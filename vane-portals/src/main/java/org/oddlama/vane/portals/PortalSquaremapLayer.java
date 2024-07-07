package org.oddlama.vane.portals;

import java.util.UUID;
import org.oddlama.vane.annotation.config.ConfigBoolean;
import org.oddlama.vane.annotation.config.ConfigInt;
import org.oddlama.vane.annotation.lang.LangMessage;
import org.oddlama.vane.core.lang.TranslatedMessage;
import org.oddlama.vane.core.module.Context;
import org.oddlama.vane.core.module.ModuleComponent;
import org.oddlama.vane.portals.portal.Portal;

import xyz.jpenilla.squaremap.api.Squaremap;

public class PortalSquaremapLayer extends ModuleComponent<Portals> {

	@ConfigInt(def = 5, min = 0, desc = "Layer ordering priority")
	public int config_layer_priority;

	@ConfigBoolean(def = false, desc = "If the layer should be hidden by default.")
	public boolean config_layer_hide;

	@ConfigInt(def = 16, min = 0, desc = "The squaremap marker icon size")
	public int config_icon_size;

	@LangMessage
	public TranslatedMessage lang_layer_label;

	Squaremap api;
	PortalSquaremapLayerDelegate delegate;

	public PortalSquaremapLayer(Context<Portals> context) {
		super(context.group(
			"squaremap",
			"Enable squaremap integration. Public portals will then be shown on a separate squaremap layer."));
	}

	@Override
	protected void on_enable() {
		schedule_next_tick(this::delayed_on_enable);
	}

	public void delayed_on_enable() {
		final var plugin = get_module().getServer().getPluginManager().getPlugin("squaremap");
		if (plugin == null) {
			return;
		}

		delegate = new PortalSquaremapLayerDelegate(this);
		delegate.on_enable(plugin);
	}

	@Override
	protected void on_disable() {
		if (delegate != null) {
			delegate.on_disable();
			delegate = null;
		}
	}

	public void update_marker(final Portal portal) {
		if (delegate != null) {
			delegate.update_marker(portal);
		}
	}

	public void remove_marker(final Portal portal) {
		if (delegate != null) {
			delegate.remove_marker(portal);
		}
	}

	public void remove_marker(final UUID portal_id) {
		if (delegate != null) {
			delegate.remove_marker(portal_id);
		}
	}

}