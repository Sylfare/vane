package org.oddlama.vane.bedtime;

import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import org.oddlama.vane.annotation.config.ConfigBoolean;
import org.oddlama.vane.annotation.config.ConfigInt;
import org.oddlama.vane.annotation.lang.LangMessage;
import org.oddlama.vane.core.lang.TranslatedMessage;
import org.oddlama.vane.core.module.Context;
import org.oddlama.vane.core.module.ModuleComponent;

public class BedtimeSquaremapLayer extends ModuleComponent<Bedtime>{

    @ConfigInt(def = 5, min = 0, desc = "Layer ordering priority")
	public int config_layer_priority;

	@ConfigBoolean(def = false, desc = "If the layer should be hidden by default.")
	public boolean config_layer_hide;

	@ConfigInt(def = 17, min = 0, desc = "The squaremap marker icon size")
	public int config_icon_size;

    BedtimeSquaremapLayerDelegate delegate;

	@LangMessage
	public TranslatedMessage lang_layer_label;

    public BedtimeSquaremapLayer(Context<Bedtime> context) {
        super(context.group(
			"squaremap",
			"Enable squaremap integration to show player spawnpoints (beds)."));
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

		delegate = new BedtimeSquaremapLayerDelegate(this);
		delegate.on_enable(plugin);
	}

    @Override
    protected void on_disable() {
        if(delegate != null) {
			delegate.on_disable();
			delegate = null;
		}
    }


    public void update_marker(@NotNull Player player) {
        if(delegate != null) {
			delegate.update_marker(player);
		}
    }
    
}
