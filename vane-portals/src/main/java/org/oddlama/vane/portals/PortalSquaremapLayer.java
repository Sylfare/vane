package org.oddlama.vane.portals;

import org.oddlama.vane.core.module.Context;
import org.oddlama.vane.core.module.ModuleComponent;
import org.oddlama.vane.portals.portal.Portal;

import xyz.jpenilla.squaremap.api.Squaremap;

public class PortalSquaremapLayer extends ModuleComponent<Portals> {

    Squaremap api;
    PortalSquaremapLayerDelegate delegate;

    // TODO implement enabled config bool
    public PortalSquaremapLayer(Context<Portals> context) {
        super(
			context.group(
				"squaremap",
				"Enable squaremap integration. Public portals will then be shown on a separate squaremap layer."
			)
		);
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
      if(delegate != null) {
        delegate.update_marker(portal);
      }
    }
    
}